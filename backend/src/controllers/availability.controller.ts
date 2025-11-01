// path: src/controllers/availability.controller.ts
import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import {
  ok,
  created,
  badRequest,
  notFound,
  conflict,
  forbidden,
} from '../handlers/response.handler';
import AvailabilitySlot from '../models/availabilitySlot.model';
import AvailabilityOccurrence from '../models/availabilityOccurrence.model';
import * as RRuleLib from 'rrule';
import redis from '../utils/redis';

// Hàm tiện ích kiểm tra trùng thời gian
const isOverlap = (aStart: Date, aEnd: Date, bStart: Date, bEnd: Date) =>
  aStart < bEnd && bStart < aEnd;

// REPLACED: self-aware conflict helper
async function hasConflict(
  mentor: any,
  start: Date,
  end: Date,
  opts?: { excludeSlotId?: any; excludeOccIds?: any[] }
) {
  const q: any = {
    mentor,
    start: { $lt: end },
    end:   { $gt: start },
    status:{ $in: ['open', 'booked'] },
  };
  if (opts?.excludeSlotId) q.slot = { $ne: opts.excludeSlotId };
  if (opts?.excludeOccIds?.length) q._id = { $nin: opts.excludeOccIds };
  const hit = await AvailabilityOccurrence.findOne(q).select('start end');
  return !!hit && (start < hit.end && hit.start < end);
}

// (old conflict helper removed per exact edit instructions)

/**
 * POST /availability/slots
 * Create availability slot (draft)
 */
export const createSlot = asyncHandler(async (req: Request, res: Response) => {
  // auth middleware sets req.user.id; some code expects req.user._id
  const mentorId = (req as any).user?.id ?? (req as any).user?._id;
  if (!mentorId) return badRequest(res, 'Unauthorized');

  const {
    title,
    description,
    timezone,
    start,
    end,
    rrule,
    exdates,
    bufferBeforeMin = 0,
    bufferAfterMin = 0,
    visibility = 'public',
    publishHorizonDays = 90,
  } = req.body as {
    title?: string;
    description?: string;
    timezone: string;
    start?: string;
    end?: string;
    rrule?: string | null;
    exdates?: string[];
    bufferBeforeMin?: number;
    bufferAfterMin?: number;
    visibility?: 'public' | 'private';
    publishHorizonDays?: number;
  };

  // Defensive future-time checks (30s skew)
  const nowSkew = new Date(Date.now() + 30_000);
  if (start) {
    const s = new Date(start);
    if (s < nowSkew) return badRequest(res, 'start must be in the future');
  }
  if (end) {
    const e = new Date(end);
    if (e < nowSkew) return badRequest(res, 'end must be in the future');
  }

  const doc = await AvailabilitySlot.create({
    mentor: mentorId,
    title,
    description,
    timezone,
    start: start ? new Date(start) : undefined,
    end: end ? new Date(end) : undefined,
    rrule: rrule ?? null,
    exdates: Array.isArray(exdates)
      ? exdates.map((d) => new Date(d))
      : [],
    bufferBeforeMin,
    bufferAfterMin,
    visibility,
    status: 'draft',
    publishHorizonDays,
  });

  return created(res, { slot: doc });
});

/**
 * POST /availability/slots/:id/publish
 */
export async function publishSlotLogic(
  slotId: string,
  mentorId: string
): Promise<{
  published: true;
  occurrencesCreated: number;
  skippedConflict: number;
  rrule: string | null;
  horizonDays: number;
  message?: string;
}> {
  const slot = await AvailabilitySlot.findById(slotId);
  if (!slot) throw new Error('Slot not found');
  if (String(slot.mentor) !== String(mentorId)) throw new Error('Not owner of slot');
  if (!slot.start || !slot.end) throw new Error('Slot start/end is required');
  if (slot.status === 'archived') throw new Error('Archived slot cannot be published');
  if (slot.status === 'published') {
    return {
      published: true,
      occurrencesCreated: 0,
      skippedConflict: 0,
      rrule: slot.rrule ?? null,
      horizonDays: slot.publishHorizonDays ?? 90,
      message: 'Already published',
    };
  }

  const baseStart = new Date(slot.start);
  const baseEnd = new Date(slot.end);
  const durationMs = baseEnd.getTime() - baseStart.getTime();
  const bufBefore = slot.bufferBeforeMin ?? 0;
  const bufAfter = slot.bufferAfterMin ?? 0;
  const horizonDays = slot.publishHorizonDays ?? 90;

  // Optional Redis mutex per-mentor to simulate race control
  const lockKey = `availability:publish:${String(slot.mentor)}`;
  const lockVal = `${Date.now()}-${Math.random()}`;
  let locked = false;
  try {
    if (redis?.isOpen) {
      const res = await redis.set(lockKey, lockVal, { NX: true, PX: 60_000 });
      locked = res === 'OK';
    }
  } catch {
    // ignore redis failures
  }
  if (!locked && redis?.isOpen) {
    // If lock exists, politely ask client to retry later (idempotent-like)
    return {
      published: true,
      occurrencesCreated: 0,
      skippedConflict: 0,
      rrule: slot.rrule ?? null,
      horizonDays,
      message: 'Publishing in progress, please retry',
    };
  }

  try {
    // Nếu không có rrule => one-off (soft-skip conflicts)
    if (!slot.rrule) {
      let occurrencesCreated = 0;
      let skippedConflict = 0;
      const checkStart = new Date(baseStart.getTime() - bufBefore * 60_000);
      const checkEnd = new Date(baseEnd.getTime() + bufAfter * 60_000);
      if (await hasConflict(slot.mentor, checkStart, checkEnd)) {
        // Soft-skip
        skippedConflict++;
      } else {
        try {
          await AvailabilityOccurrence.create({
            slot: slot._id,
            mentor: slot.mentor,
            start: baseStart,
            end: baseEnd,
            visibility: slot.visibility,
            status: 'open',
            capacity: 1,
          });
          occurrencesCreated++;
        } catch (e: any) {
          if (e?.code !== 11000) throw e; // duplicate unique key => treat as exists
        }
      }
      slot.status = 'published';
      await slot.save();
      return {
        published: true,
        occurrencesCreated,
        skippedConflict,
        rrule: null,
        horizonDays,
      };
    }

    // RRULE case
    const rawRRule = (slot.rrule || '').trim();
    let rruleDates: Date[] = [];
    const horizonEnd = new Date(baseStart.getTime() + horizonDays * 24 * 60 * 60 * 1000);

    function manualExpand(ruleStr: string): Date[] {
      // Hỗ trợ subset: FREQ=DAILY|WEEKLY; BYDAY (tuỳ chọn); COUNT (tuỳ chọn); INTERVAL (tuỳ chọn); UNTIL (tuỳ chọn)
      const parts = ruleStr.split(';').map(p => p.trim()).filter(Boolean);
      const kv: Record<string,string> = {};
      for (const p of parts) {
        const [k,v] = p.split('=');
        if (k && v) kv[k.toUpperCase()] = v.toUpperCase();
      }
      const freq = kv['FREQ'];
      if (!freq) return [];
      const interval = Math.max(1, parseInt(kv['INTERVAL'] || '1', 10) || 1);
      const count = kv['COUNT'] ? Math.max(0, parseInt(kv['COUNT'],10)||0) : 0;
      // UNTIL: lấy nguyên giá trị từ chuỗi gốc để không bị upper-case
      const untilStr = (ruleStr.split(';').find(s => s.toUpperCase().startsWith('UNTIL=')) || '').split('=')[1] || '';
      const untilDate = untilStr ? new Date(untilStr) : null;

      // Xác định limitEnd: có UNTIL -> UNTIL; có COUNT -> null; else -> horizonEnd
      const limitEnd: Date | null = untilDate ? untilDate : (count > 0 ? null : horizonEnd);

      const bydayRaw = kv['BYDAY'] ? kv['BYDAY'].split(',').map(s=>s.trim()).filter(Boolean) : [];
      const dayMap: Record<string,number> = { MO:1, TU:2, WE:3, TH:4, FR:5, SA:6, SU:0 };
      const results: Date[] = [];
      let cursor = new Date(baseStart);
      const pushIf = (d: Date) => {
        if (d.getTime() < baseStart.getTime()) return;
        if (limitEnd && d.getTime() > limitEnd.getTime()) return;
        results.push(new Date(d));
      };
      if (freq === 'DAILY') {
        let i = 0;
        while (true) {
          pushIf(cursor);
          if (count && results.length >= count) break;
          if (limitEnd && cursor.getTime() >= limitEnd.getTime()) break;
          cursor = new Date(cursor.getTime() + interval * 24 * 60 * 60 * 1000);
          if (++i > 10000) break;
        }
      } else if (freq === 'WEEKLY') {
        const targetDays = bydayRaw.length ? bydayRaw : [ ['SU','MO','TU','WE','TH','FR','SA'][baseStart.getUTCDay()] ];
        let weekStart = new Date(Date.UTC(baseStart.getUTCFullYear(), baseStart.getUTCMonth(), baseStart.getUTCDate()));
        weekStart.setUTCDate(weekStart.getUTCDate() - weekStart.getUTCDay());
        let loops = 0;
        while (true) {
          for (const dCode of targetDays) {
            const wd = dayMap[dCode];
            if (wd === undefined) continue;
            const occ = new Date(weekStart.getTime());
            occ.setUTCDate(weekStart.getUTCDate() + wd);
            occ.setUTCHours(baseStart.getUTCHours(), baseStart.getUTCMinutes(), baseStart.getUTCHours(), baseStart.getUTCMilliseconds());
            if (occ.getTime() >= baseStart.getTime()) {
              pushIf(occ);
              if (count && results.length >= count) break;
            }
          }
          if (count && results.length >= count) break;
          if (limitEnd && weekStart.getTime() >= limitEnd.getTime()) break;
          weekStart = new Date(weekStart.getTime() + interval * 7 * 24 * 60 * 60 * 1000);
          if (++loops > 1000) break;
        }
        results.sort((a,b)=>a.getTime()-b.getTime());
        if (count && results.length > count) results.splice(count);
      }
      return results;
    }

    try {
      const opts = RRuleLib.RRule.parseString(rawRRule);
      opts.dtstart = baseStart;
      const rule = new RRuleLib.RRule(opts);
      const hasCount = typeof (opts as any).count === 'number' && (opts as any).count > 0;
      const hasUntil = !!(opts as any).until;
      if (hasCount) {
        rruleDates = rule.all();
      } else if (hasUntil) {
        rruleDates = rule.all();
      } else {
        rruleDates = rule.between(baseStart, horizonEnd, true);
      }
    } catch (e) {
      rruleDates = manualExpand(rawRRule);
      if (!rruleDates.length) {
        throw new Error('Invalid RRULE format');
      }
    }

    // Áp dụng exdates nếu có
    const exdateISOSet = new Set((slot.exdates ?? []).map((d) => new Date(d).toISOString()));
    let created = 0;
    let skippedConflict = 0;
    for (const occStart of rruleDates) {
      if (occStart.getTime() < baseStart.getTime()) continue;
      if (exdateISOSet.has(occStart.toISOString())) continue;
      const occEnd = new Date(occStart.getTime() + durationMs);
      const checkStart = new Date(occStart.getTime() - bufBefore * 60_000);
      const checkEnd = new Date(occEnd.getTime() + bufAfter * 60_000);
      const conflictHit = await hasConflict(slot.mentor, checkStart, checkEnd);
      if (conflictHit) {
        skippedConflict++;
        continue;
      }
      try {
        await AvailabilityOccurrence.create({
          slot: slot._id,
          mentor: slot.mentor,
          start: occStart,
          end: occEnd,
          visibility: slot.visibility,
          status: 'open',
          capacity: 1,
        });
        created++;
      } catch (e: any) {
        if (e?.code === 11000) continue; // duplicate skip
        throw e;
      }
    }

    slot.status = 'published';
    await slot.save();
    return {
      published: true,
      occurrencesCreated: created,
      skippedConflict,
      rrule: slot.rrule ?? null,
      horizonDays,
    };
  } finally {
    if (locked && redis?.isOpen) {
      try { await redis.del(lockKey); } catch {}
    }
  }
}

export const publishSlot = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = String((req as any).user?.id ?? (req as any).user?._id);
  const { id } = req.params as { id: string };
  try {
    const result = await publishSlotLogic(id, mentorId);
    return ok(res, result);
  } catch (e: any) {
    const msg = String(e?.message || e);
    if (msg === 'Slot not found') return notFound(res, msg);
    if (msg === 'Not owner of slot') return forbidden(res, msg);
    if (msg === 'Slot start/end is required') return badRequest(res, msg);
    if (msg === 'Archived slot cannot be published') return badRequest(res, msg);
    if (msg === 'Invalid RRULE format') return badRequest(res, msg);
    throw e; // let global handler deal with unexpected errors
  }
});

export const publishBatch = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = String((req as any).user?.id ?? (req as any).user?._id);
  const { ids, concurrent = false } = (req.body || {}) as { ids: string[]; concurrent?: boolean };
  if (!Array.isArray(ids) || ids.length === 0) {
    return badRequest(res, 'ids must be a non-empty array');
  }
  if (concurrent) {
    const results = await Promise.allSettled(ids.map((id) => publishSlotLogic(id, mentorId)));
    const mapped = results.map((r, i) =>
      r.status === 'fulfilled'
        ? { id: ids[i], ok: true, result: r.value }
        : { id: ids[i], ok: false, error: String((r as any).reason?.message ?? r) }
    );
    return ok(res, { mode: 'concurrent', results: mapped });
  } else {
    const mapped: any[] = [];
    for (const id of ids) {
      try {
        const out = await publishSlotLogic(id, mentorId);
        mapped.push({ id, ok: true, result: out });
      } catch (e: any) {
        mapped.push({ id, ok: false, error: String(e?.message || e) });
      }
    }
    return ok(res, { mode: 'sequential', results: mapped });
  }
});

/**
 * DELETE /availability/slots/:id
 * Hard delete a slot if it has no future booked occurrences
 */
export const deleteSlot = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = String((req as any).user?.id ?? (req as any).user?._id);
  const { id } = req.params as { id: string };

  const slot = await AvailabilitySlot.findById(id);
  if (!slot) return notFound(res, 'Slot not found');
  if (String(slot.mentor) !== String(mentorId)) return forbidden(res, 'Not owner of slot');

  const now = new Date();
  const hasBooked = await AvailabilityOccurrence.exists({
    slot: slot._id,
    start: { $gte: now },
    status: 'booked',
  });

  if (hasBooked) {
    return conflict(res, 'slot has booked occurrences');
  }

  // Remove future occurrences (open/closed)
  await AvailabilityOccurrence.deleteMany({ slot: slot._id, start: { $gte: now } });
  // Hard delete slot
  await AvailabilitySlot.deleteOne({ _id: slot._id });

  return res.status(204).send();
});

/**
 * PATCH /availability/slots/:id
 * Update slot meta and optionally pause/resume occurrences
 */
export const updateSlot = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = String((req as any).user?.id ?? (req as any).user?._id);
  const { id } = req.params as { id: string };

  const slot = await AvailabilitySlot.findById(id);
  if (!slot) return notFound(res, 'Slot not found');
  if (String(slot.mentor) !== String(mentorId)) return forbidden(res, 'Not owner of slot');

  // Added: capture previous values for change detection
  const prevStart = slot.start ? new Date(slot.start) : undefined;
  const prevEnd   = slot.end   ? new Date(slot.end)   : undefined;
  const prevRRule = slot.rrule ?? null;

  const { title, description, timezone, start, end, visibility, action } = (req.body || {}) as {
    title?: string;
    description?: string;
    timezone?: string;
    start?: string;
    end?: string;
    visibility?: 'public' | 'private';
    action?: 'pause' | 'resume';
  };

  // Apply partial meta updates
  if (typeof title === 'string') slot.title = title;
  if (typeof description === 'string') slot.description = description;
  if (typeof timezone === 'string' && timezone.trim()) slot.timezone = timezone.trim();
  if (typeof visibility === 'string') slot.visibility = visibility;

  // Handle start/end updates with validation relative to each other
  let nextStart: Date | undefined = slot.start ? new Date(slot.start) : undefined;
  let nextEnd: Date | undefined = slot.end ? new Date(slot.end) : undefined;
  if (typeof start === 'string') nextStart = new Date(start);
  if (typeof end === 'string') nextEnd = new Date(end);
  if (typeof start === 'string' || typeof end === 'string') {
    if (nextStart && nextEnd) {
      if (!(nextEnd.getTime() > nextStart.getTime())) {
        return badRequest(res, 'end must be greater than start');
      }
    }
    if (typeof start === 'string') slot.start = nextStart as any;
    if (typeof end === 'string') slot.end = nextEnd as any;
  }

  // Defensive future-time checks (30s skew) only for provided fields
  const nowSkew = new Date(Date.now() + 30_000);
  if (typeof start === 'string') {
    if (nextStart && nextStart < nowSkew) return badRequest(res, 'start must be in the future');
  }
  if (typeof end === 'string') {
    if (nextEnd && nextEnd < nowSkew) return badRequest(res, 'end must be in the future');
  }

  // Pause/Resume actions impacting future occurrences
  const nowAct = new Date();
  if (action === 'pause') {
    slot.status = 'archived';
    await AvailabilityOccurrence.updateMany(
      { slot: slot._id, start: { $gte: nowAct } },
      { $set: { status: 'closed' } }
    );
  } else if (action === 'resume') {
    slot.status = 'published';
    await AvailabilityOccurrence.updateMany(
      { slot: slot._id, start: { $gte: nowAct }, status: 'closed' },
      { $set: { status: 'open' } }
    );
  }

  // Optional: allow rrule/exdates patch-through
  if (typeof (req.body as any).rrule === 'string') slot.rrule = (req.body as any).rrule.trim();
  if (Array.isArray((req.body as any).exdates)) {
    slot.exdates = (req.body as any).exdates.map((d: string) => new Date(d));
  }

  await slot.save();

  // Detect time/rrule change and propagate to occurrences
  const now = new Date();
  const startChanged = (!!prevStart && !!slot.start && prevStart.getTime() !== new Date(slot.start).getTime()) || (!!prevStart !== !!slot.start);
  const endChanged   = (!!prevEnd   && !!slot.end   && prevEnd.getTime()   !== new Date(slot.end).getTime())   || (!!prevEnd   !== !!slot.end);
  const rruleChanged = (prevRRule ?? null) !== (slot.rrule ?? null);
  const timeChanged  = startChanged || endChanged;

  if (timeChanged || rruleChanged) {
    const baseStart = slot.start ? new Date(slot.start) : undefined;
    const baseEnd   = slot.end   ? new Date(slot.end)   : undefined;
    if (!baseStart || !baseEnd || !(baseEnd > baseStart)) return badRequest(res, 'invalid start/end after update');
    const bStart: Date = baseStart as Date;
    const bEnd: Date = baseEnd as Date;

    const durationMs = bEnd.getTime() - bStart.getTime();
    const bufBefore  = slot.bufferBeforeMin ?? 0;
    const bufAfter   = slot.bufferAfterMin  ?? 0;

    if (!slot.rrule) {
      // ONE-OFF: move a single future occurrence if not booked
      const occ = await AvailabilityOccurrence.findOne({
        slot: slot._id,
        start: { $gte: now },
        status: { $in: ['open', 'closed'] }
      }).sort({ start: 1 });

      if (occ) {
        const newStart = new Date(baseStart);
        const newEnd   = new Date(baseEnd);
        const checkStart = new Date(newStart.getTime() - bufBefore * 60_000);
        const checkEnd   = new Date(newEnd.getTime()   + bufAfter  * 60_000);
        const overlap = await hasConflict(slot.mentor, checkStart, checkEnd, {
          excludeSlotId: slot._id,
          excludeOccIds: [occ._id]
        });
        if (overlap) return conflict(res, 'time overlaps another occurrence');
        if (occ.status === 'open' || occ.status === 'closed') {
          occ.start = newStart as any;
          occ.end   = newEnd as any;
          await occ.save();
        }
      }
    } else {
      // RRULE: rebuild future (preserve booked)
      await AvailabilityOccurrence.deleteMany({
        slot: slot._id,
        start: { $gte: now },
        status: { $in: ['open', 'closed'] }
      });

      const horizonDays = slot.publishHorizonDays ?? 90;
      const horizonEnd  = new Date(bStart.getTime() + horizonDays * 24 * 60 * 60 * 1000);

      let rruleDates: Date[] = [];
      const rawRRule = (slot.rrule || '').trim();

      function manualExpand(ruleStr: string, anchor: Date): Date[] {
        const parts = ruleStr.split(';').map(s => s.trim()).filter(Boolean);
        const kv: Record<string,string> = {};
        for (const p of parts) { const [k,v] = p.split('='); if (k && v) kv[k.toUpperCase()] = v.toUpperCase(); }
        const freq = kv['FREQ']; if (!freq) return [];
        const interval = Math.max(1, parseInt(kv['INTERVAL'] || '1', 10) || 1);
        const count = kv['COUNT'] ? Math.max(0, parseInt(kv['COUNT'],10)||0) : 0;
        const untilStr = (ruleStr.split(';').find(s => s.toUpperCase().startsWith('UNTIL=')) || '').split('=')[1] || '';
        const untilDate = untilStr ? new Date(untilStr) : null;
        const limitEnd  = untilDate ? untilDate : (count > 0 ? null : horizonEnd);

        const bydayRaw = kv['BYDAY'] ? kv['BYDAY'].split(',').map(s => s.trim()).filter(Boolean) : [];
        const dayMap: Record<string,number> = { MO:1, TU:2, WE:3, TH:4, FR:5, SA:6, SU:0 };
        const out: Date[] = [];
        let cursor = new Date(anchor);
        const pushIf = (d: Date) => { if (d >= anchor && (!limitEnd || d <= limitEnd)) out.push(new Date(d)); };
        if (freq === 'DAILY') {
          for (let i=0;;i++){
            pushIf(cursor);
            if (count && out.length >= count) break;
            if (limitEnd && cursor >= limitEnd) break;
            cursor = new Date(cursor.getTime() + interval * 86400000);
            if (i > 10000) break;
          }
        } else if (freq === 'WEEKLY') {
          const targetDays = bydayRaw.length ? bydayRaw : [ ['SU','MO','TU','WE','TH','FR','SA'][anchor.getUTCDay()] ];
          let weekStart = new Date(Date.UTC(anchor.getUTCFullYear(), anchor.getUTCMonth(), anchor.getUTCDate()));
          weekStart.setUTCDate(weekStart.getUTCDate() - weekStart.getUTCDay());
          for (let loops=0;;loops++){
            for (const dCode of targetDays) {
              const wd = dayMap[dCode]; if (wd === undefined) continue;
              const occ = new Date(weekStart.getTime());
              occ.setUTCDate(weekStart.getUTCDate() + wd);
              occ.setUTCHours(anchor.getUTCHours(), anchor.getUTCMinutes(), anchor.getUTCSeconds(), anchor.getUTCMilliseconds());
              if (occ >= anchor) pushIf(occ);
            }
            if ((count && out.length >= count) || (limitEnd && weekStart >= limitEnd)) break;
            weekStart = new Date(weekStart.getTime() + interval * 7 * 86400000);
            if (loops > 1000) break;
          }
          out.sort((a,b)=>a.getTime()-b.getTime());
          if (count && out.length > count) out.splice(count);
        }
        return out;
      }

      try {
        const opts = RRuleLib.RRule.parseString(rawRRule);
        opts.dtstart = bStart as Date;
        const rule = new RRuleLib.RRule(opts);
        if (typeof (opts as any).count === 'number' && (opts as any).count > 0) rruleDates = rule.all();
        else if ((opts as any).until) rruleDates = rule.all();
        else rruleDates = rule.between(bStart as Date, horizonEnd, true);
      } catch {
        rruleDates = manualExpand(rawRRule, bStart);
      }

      const exdateISOSet = new Set((slot.exdates ?? []).map((d) => new Date(d).toISOString()));
      for (const occStart of rruleDates) {
        if (occStart < bStart) continue;
        if (exdateISOSet.has(occStart.toISOString())) continue;
        const occEnd = new Date(occStart.getTime() + durationMs);
        const checkStart = new Date(occStart.getTime() - bufBefore * 60_000);
        const checkEnd   = new Date(occEnd.getTime()   + bufAfter  * 60_000);
        const conflictHit = await hasConflict(slot.mentor, checkStart, checkEnd, { excludeSlotId: slot._id });
        if (conflictHit) continue;
        try {
          await AvailabilityOccurrence.create({
            slot: slot._id,
            mentor: slot.mentor,
            start: occStart,
            end: occEnd,
            visibility: slot.visibility,
            status: 'open',
            capacity: 1,
          });
        } catch (e: any) {
          if (e?.code !== 11000) throw e;
        }
      }
    }
  }

  const data = {
    id: String(slot._id),
    title: slot.title ?? null,
    description: slot.description ?? null,
    start: slot.start ?? null,
    end: slot.end ?? null,
    timezone: slot.timezone,
    visibility: slot.visibility,
    status: slot.status,
  };
  return ok(res, data);
});

/**
 * GET /availability/calendar/:mentorId?from=&to=
 * Public read calendar
 */
export const getPublicCalendar = asyncHandler(
  async (req: Request, res: Response) => {
    const { mentorId } = req.params as { mentorId: string };
    const { from, to, includeClosed } = req.query as { from: string; to: string; includeClosed?: any };
    const wantClosed = String(includeClosed) === 'true';

    // Validate from/to: ISO & from < to
    if (!from || !to) {
      return badRequest(res, 'Invalid or missing from/to (ISO UTC expected)');
    }
    const fromDate = new Date(from);
    const toDate = new Date(to);
    if (isNaN(fromDate.getTime()) || isNaN(toDate.getTime()) || fromDate >= toDate) {
      return badRequest(res, 'Invalid or missing from/to (ISO UTC expected)');
    }

    const statusFilter = wantClosed ? ['open', 'closed'] : ['open'];
    const docs = await AvailabilityOccurrence.find({
      mentor: mentorId,
      start: { $lt: toDate },
      end: { $gt: fromDate },
      visibility: 'public',
      status: { $in: statusFilter },
    })
      .select('start end status slot')
      .populate({
        path: 'slot',
        select: 'title description visibility status',
      })
      .lean();

    const items = (docs || []).map((doc: any) => {
      const slot = doc.slot && typeof doc.slot === 'object' ? doc.slot : null;
      return {
        id: String(doc._id),
        start: doc.start,
        end: doc.end,
        status: doc.status,
        title: slot?.title ?? null,
        description: slot?.description ?? null,
        // Cung cấp meta slot để FE có thể parse [type=...] và hiển thị Video/Trực tiếp
        slot: slot
          ? {
              id: String(slot._id ?? doc.slot),
              title: slot.title ?? null,
              description: slot.description ?? null,
            }
          : { id: String(doc.slot ?? ''), title: null, description: null },
      };
    });

    return ok(res, { items });
  }
);
