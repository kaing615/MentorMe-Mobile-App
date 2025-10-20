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

async function hasConflict(mentor: any, start: Date, end: Date) {
  const conflictOcc = await AvailabilityOccurrence.findOne({
    mentor,
    start: { $lt: end },
    end: { $gt: start },
    status: { $in: ['open', 'booked'] },
  }).select('start end');
  if (!conflictOcc) return false;
  return isOverlap(start, end, conflictOcc.start, conflictOcc.end);
}

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
            occ.setUTCHours(baseStart.getUTCHours(), baseStart.getUTCMinutes(), baseStart.getUTCSeconds(), baseStart.getUTCMilliseconds());
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
 * GET /availability/calendar/:mentorId?from=&to=
 * Public read calendar
 */
export const getPublicCalendar = asyncHandler(
  async (req: Request, res: Response) => {
    const { mentorId } = req.params as { mentorId: string };
    const { from, to } = req.query as { from: string; to: string };

    // Validate from/to: ISO & from < to
    if (!from || !to) {
      return badRequest(res, 'Invalid or missing from/to (ISO UTC expected)');
    }
    const fromDate = new Date(from);
    const toDate = new Date(to);
    if (isNaN(fromDate.getTime()) || isNaN(toDate.getTime()) || fromDate >= toDate) {
      return badRequest(res, 'Invalid or missing from/to (ISO UTC expected)');
    }

    const items = await AvailabilityOccurrence.find({
      mentor: mentorId,
      start: { $lt: toDate },
      end: { $gt: fromDate },
      visibility: 'public',
      status: 'open',
    })
      .select('start end status slot')
      .lean();

    return ok(res, { items });
  }
);
