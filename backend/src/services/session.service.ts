import Booking, { TBookingStatus } from '../models/booking.model';
import SessionLog, {
  IQosReport,
  IQosSummary,
  ISessionLog,
  TSessionEndReason,
} from '../models/sessionLog.model';

type BookingLike = {
  _id: any;
  mentor: any;
  mentee: any;
  startTime: Date;
  endTime: Date;
};

const SESSION_JOIN_EARLY_MINUTES =
  parseInt(process.env.SESSION_JOIN_EARLY_MINUTES || '10', 10) || 10;
const SESSION_JOIN_LATE_MINUTES =
  parseInt(process.env.SESSION_JOIN_LATE_MINUTES || '15', 10) || 15;
const SESSION_GRACE_MINUTES =
  parseInt(process.env.SESSION_GRACE_MINUTES || '10', 10) || 10;
const SESSION_STATE_TTL_MINUTES =
  parseInt(process.env.SESSION_STATE_TTL_MINUTES || '360', 10) || 360;

export function getSessionWindow(startTime: Date, endTime: Date) {
  const earlyMs = SESSION_JOIN_EARLY_MINUTES * 60 * 1000;
  const lateMs = SESSION_JOIN_LATE_MINUTES * 60 * 1000;

  return {
    openAt: new Date(startTime.getTime() - earlyMs),
    closeAt: new Date(endTime.getTime() + lateMs),
  };
}

export function isWithinSessionWindow(now: Date, startTime: Date, endTime: Date) {
  const window = getSessionWindow(startTime, endTime);
  return now >= window.openAt && now <= window.closeAt;
}

export function getSessionGraceDeadline(startTime: Date) {
  return new Date(startTime.getTime() + SESSION_GRACE_MINUTES * 60 * 1000);
}

export function getSessionStateTtlSeconds(endTime?: Date | null) {
  const bufferMs = SESSION_STATE_TTL_MINUTES * 60 * 1000;
  const now = Date.now();
  const endMs = endTime ? endTime.getTime() : now;
  const ttlMs = Math.max(30 * 60 * 1000, endMs - now + bufferMs);
  return Math.max(60, Math.ceil(ttlMs / 1000));
}

export async function ensureSessionLog(booking: BookingLike) {
  return SessionLog.findOneAndUpdate(
    { booking: booking._id },
    {
      $setOnInsert: {
        booking: booking._id,
        mentor: booking.mentor,
        mentee: booking.mentee,
        scheduledStart: booking.startTime,
        scheduledEnd: booking.endTime,
        status: 'waiting',
      },
    },
    { upsert: true, new: true }
  );
}

export async function recordSessionJoin(
  booking: BookingLike,
  role: 'mentor' | 'mentee',
  joinedAt = new Date()
) {
  const field = role === 'mentor' ? 'mentorJoinAt' : 'menteeJoinAt';
  await ensureSessionLog(booking);
  await SessionLog.updateOne(
    { booking: booking._id, [field]: { $exists: false } },
    { $set: { [field]: joinedAt } }
  );
  return maybeActivateSession(String(booking._id));
}

export async function recordSessionAdmit(booking: BookingLike, admittedAt = new Date()) {
  await ensureSessionLog(booking);
  await SessionLog.updateOne(
    { booking: booking._id, mentorAdmitAt: { $exists: false } },
    { $set: { mentorAdmitAt: admittedAt } }
  );
  return maybeActivateSession(String(booking._id), admittedAt);
}

export async function maybeActivateSession(bookingId: string, admittedAt?: Date) {
  const log = await SessionLog.findOne({ booking: bookingId });
  if (!log) return null;
  if (log.actualStart || log.status === 'active') return log;
  if (!log.mentorJoinAt || !log.menteeJoinAt) return log;

  const admitTime = admittedAt || log.mentorAdmitAt;
  if (!admitTime) return log;

  const startAtMs = Math.max(
    admitTime.getTime(),
    log.mentorJoinAt.getTime(),
    log.menteeJoinAt.getTime()
  );
  const actualStart = new Date(startAtMs);
  const waitingRoomMs = Math.max(0, actualStart.getTime() - log.menteeJoinAt.getTime());

  log.actualStart = actualStart;
  log.status = 'active';
  log.waitingRoomMs = waitingRoomMs;
  await log.save();

  return log;
}

export async function recordSessionEnd(
  bookingId: string,
  endReason: TSessionEndReason,
  endedAt = new Date()
) {
  const log = await SessionLog.findOne({ booking: bookingId });
  if (!log) return null;
  if (log.status === 'no_show') return log;
  if (log.actualEnd) return log;

  log.actualEnd = endedAt;
  log.endReason = endReason;
  log.status = endReason.startsWith('no_show') ? 'no_show' : 'ended';

  if (log.actualStart) {
    const durationSec = Math.max(
      0,
      Math.floor((endedAt.getTime() - log.actualStart.getTime()) / 1000)
    );
    log.durationSec = durationSec;
  } else {
    log.durationSec = 0;
  }

  await log.save();
  return log;
}

export async function recordSessionDisconnect(bookingId: string, role: 'mentor' | 'mentee') {
  const field = role === 'mentor' ? 'mentorDisconnects' : 'menteeDisconnects';
  await SessionLog.updateOne(
    { booking: bookingId },
    { $inc: { [field]: 1 } }
  );
}

function updateAverage(currentAvg: number | undefined, samples: number, nextValue?: number) {
  if (typeof nextValue !== 'number' || Number.isNaN(nextValue)) return currentAvg;
  if (typeof currentAvg !== 'number' || Number.isNaN(currentAvg) || samples <= 0) {
    return nextValue;
  }
  return (currentAvg * samples + nextValue) / (samples + 1);
}

export async function recordSessionQoS(
  bookingId: string,
  role: 'mentor' | 'mentee',
  report: IQosReport
) {
  const log = await SessionLog.findOne({ booking: bookingId }).lean<ISessionLog>();
  if (!log) return null;

  const now = report.timestamp || new Date();
  const summary: IQosSummary = log.qos?.[role] || { samples: 0 };
  const samples = summary.samples || 0;

  const nextSummary: IQosSummary = {
    samples: samples + 1,
    last: {
      timestamp: now,
      rttMs: report.rttMs,
      jitterMs: report.jitterMs,
      packetLoss: report.packetLoss,
      bitrateKbps: report.bitrateKbps,
    },
    avgRttMs: updateAverage(summary.avgRttMs, samples, report.rttMs),
    avgJitterMs: updateAverage(summary.avgJitterMs, samples, report.jitterMs),
    avgPacketLoss: updateAverage(summary.avgPacketLoss, samples, report.packetLoss),
    avgBitrateKbps: updateAverage(summary.avgBitrateKbps, samples, report.bitrateKbps),
    lastUpdatedAt: now,
  };

  await SessionLog.updateOne(
    { booking: bookingId },
    { $set: { [`qos.${role}`]: nextSummary } }
  );

  return nextSummary;
}

export async function processNoShowBookings(
  onNoShow?: (data: { booking: BookingLike; noShowBy: 'mentor' | 'mentee' | 'both' }) => Promise<void> | void
) {
  const now = new Date();
  const cutoff = new Date(now.getTime() - SESSION_GRACE_MINUTES * 60 * 1000);

  const bookings = await Booking.find({
    status: 'Confirmed',
    startTime: { $lte: cutoff },
  });

  let updatedCount = 0;

  for (const booking of bookings) {
    const log = (await ensureSessionLog(booking)) || (await SessionLog.findOne({ booking: booking._id }));
    if (!log) continue;
    if (log.status === 'ended' || log.status === 'no_show' || log.actualStart) continue;

    const mentorJoined = Boolean(log.mentorJoinAt);
    const menteeJoined = Boolean(log.menteeJoinAt);
    const admitted = Boolean(log.mentorAdmitAt);

    if (mentorJoined && menteeJoined && admitted) continue;

    let bookingStatus: TBookingStatus;
    let endReason: TSessionEndReason;
    let noShowBy: 'mentor' | 'mentee' | 'both';

    if (mentorJoined && menteeJoined && !admitted) {
      bookingStatus = 'NoShowMentor';
      endReason = 'no_show_mentor';
      noShowBy = 'mentor';
    } else if (!mentorJoined && !menteeJoined) {
      bookingStatus = 'NoShowBoth';
      endReason = 'no_show_both';
      noShowBy = 'both';
    } else if (!mentorJoined) {
      bookingStatus = 'NoShowMentor';
      endReason = 'no_show_mentor';
      noShowBy = 'mentor';
    } else {
      bookingStatus = 'NoShowMentee';
      endReason = 'no_show_mentee';
      noShowBy = 'mentee';
    }

    const updateResult = await Booking.updateOne(
      { _id: booking._id, status: 'Confirmed' },
      { $set: { status: bookingStatus } }
    );

    if ((updateResult as any)?.modifiedCount === 0) {
      continue;
    }

    await recordSessionEnd(String(booking._id), endReason, now);
    if (onNoShow) {
      try {
        await onNoShow({ booking, noShowBy });
      } catch (err) {
        console.error('Failed to send no-show notification:', err);
      }
    }
    updatedCount += 1;
  }

  return updatedCount;
}
