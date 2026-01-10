import { randomUUID } from 'crypto';
import { Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { asyncHandler } from '../handlers/async.handler';
import { badRequest, forbidden, notFound, ok, unauthorized } from '../handlers/response.handler';
import Booking from '../models/booking.model';
import SessionLog from '../models/sessionLog.model';
import noShowService from '../services/noShow.service';
import { ensureSessionLog, getSessionWindow, isWithinSessionWindow } from '../services/session.service';

const SESSION_JOIN_TTL_MINUTES =
  parseInt(process.env.SESSION_JOIN_TTL_MINUTES || '10', 10) || 10;

function formatSessionLog(log: any) {
  return {
    id: String(log._id),
    bookingId: String(log.booking),
    mentorId: String(log.mentor),
    menteeId: String(log.mentee),
    scheduledStart: log.scheduledStart ? new Date(log.scheduledStart).toISOString() : null,
    scheduledEnd: log.scheduledEnd ? new Date(log.scheduledEnd).toISOString() : null,
    actualStart: log.actualStart ? new Date(log.actualStart).toISOString() : null,
    actualEnd: log.actualEnd ? new Date(log.actualEnd).toISOString() : null,
    durationSec: log.durationSec ?? null,
    status: log.status,
    endReason: log.endReason ?? null,
    waitingRoomMs: log.waitingRoomMs ?? null,
    mentorJoinAt: log.mentorJoinAt ? new Date(log.mentorJoinAt).toISOString() : null,
    menteeJoinAt: log.menteeJoinAt ? new Date(log.menteeJoinAt).toISOString() : null,
    mentorAdmitAt: log.mentorAdmitAt ? new Date(log.mentorAdmitAt).toISOString() : null,
    mentorDisconnects: log.mentorDisconnects ?? 0,
    menteeDisconnects: log.menteeDisconnects ?? 0,
    qos: log.qos ?? null,
    createdAt: log.createdAt ? new Date(log.createdAt).toISOString() : null,
    updatedAt: log.updatedAt ? new Date(log.updatedAt).toISOString() : null,
  };
}

export const createJoinToken = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { bookingId } = req.params as { bookingId: string };

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  if (!isMentor && !isMentee) {
    return forbidden(res, 'Access denied');
  }

  if (booking.status !== 'Confirmed') {
    return badRequest(res, 'Session is not ready for join');
  }

  const now = new Date();
  if (!isWithinSessionWindow(now, new Date(booking.startTime), new Date(booking.endTime))) {
    const window = getSessionWindow(new Date(booking.startTime), new Date(booking.endTime));
    return badRequest(
      res,
      {
        openAt: window.openAt.toISOString(),
        closeAt: window.closeAt.toISOString(),
      },
      'Session join window closed'
    );
  }

  const joinSecret = process.env.SESSION_JOIN_JWT_SECRET || process.env.JWT_SECRET;
  if (!joinSecret) {
    return badRequest(res, 'SESSION_JOIN_JWT_SECRET is not configured');
  }

  const window = getSessionWindow(new Date(booking.startTime), new Date(booking.endTime));
  const nowMs = Date.now();
  const ttlMs = SESSION_JOIN_TTL_MINUTES * 60 * 1000;
  const expMs = Math.min(nowMs + ttlMs, window.closeAt.getTime());
  if (expMs <= nowMs) {
    return badRequest(res, 'Session join window closed');
  }

  const ttlSeconds = Math.max(1, Math.floor((expMs - nowMs) / 1000));
  const token = jwt.sign(
    {
      jti: randomUUID(),
      bookingId: String(booking._id),
      userId: String(userId),
      role: isMentor ? 'mentor' : 'mentee',
    },
    joinSecret,
    {
      issuer: 'mentorme',
      audience: 'mentorme-session',
      expiresIn: ttlSeconds,
    }
  );

  await ensureSessionLog(booking as any);

  return ok(res, {
    token,
    expiresAt: new Date(expMs).toISOString(),
    role: isMentor ? 'mentor' : 'mentee',
  });
});

export const getSessionLog = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { bookingId } = req.params as { bookingId: string };

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  const isAdmin = userRole === 'admin' || userRole === 'root';

  if (!isMentor && !isMentee && !isAdmin) {
    return forbidden(res, 'Access denied');
  }

  const log = await SessionLog.findOne({ booking: bookingId }).lean();
  if (!log) {
    return ok(res, null, 'No session log available');
  }

  return ok(res, formatSessionLog(log));
});

export const adminListSessions = asyncHandler(async (req: Request, res: Response) => {
  const {
    status,
    bookingId,
    mentorId,
    menteeId,
    from,
    to,
    page = 1,
    limit = 25,
  } = req.query as {
    status?: string;
    bookingId?: string;
    mentorId?: string;
    menteeId?: string;
    from?: string;
    to?: string;
    page?: number;
    limit?: number;
  };

  const pageNum = Number(page) || 1;
  const limitNum = Math.min(Number(limit) || 25, 100);
  const skip = (pageNum - 1) * limitNum;

  const query: any = {};
  if (status) query.status = status;
  if (bookingId) query.booking = bookingId;
  if (mentorId) query.mentor = mentorId;
  if (menteeId) query.mentee = menteeId;
  if (from || to) {
    query.scheduledStart = {};
    if (from) query.scheduledStart.$gte = new Date(from);
    if (to) query.scheduledStart.$lte = new Date(to);
  }

  const [logs, total] = await Promise.all([
    SessionLog.find(query)
      .sort({ scheduledStart: -1 })
      .skip(skip)
      .limit(limitNum)
      .lean(),
    SessionLog.countDocuments(query),
  ]);

  const totalPages = Math.ceil(total / limitNum);
  const start = skip;
  const end = Math.min(skip + logs.length - 1, total - 1);
  res.set('Content-Range', `sessions ${start}-${end}/${total}`);
  res.set('Access-Control-Expose-Headers', 'Content-Range');

  return ok(res, {
    sessions: logs.map((log) => formatSessionLog(log)),
    total,
    page: pageNum,
    totalPages,
  });
});

/**
 * Check and process no-show for a specific booking
 * Can be triggered manually by admin or automatically by cron job
 */
export const checkNoShow = asyncHandler(async (req: Request, res: Response) => {
  const { bookingId } = req.params as { bookingId: string };

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Detect no-show
  const noShowResult = await noShowService.detectNoShow(bookingId);

  if (!noShowResult) {
    return ok(res, null, 'Not a no-show - both parties joined or grace period not passed');
  }

  // Process no-show
  const result = await noShowService.processNoShow(bookingId);

  return ok(res, {
    bookingId: result.bookingId,
    status: result.status,
    refundAmount: result.refundAmount,
    platformFee: result.platformFee,
    message: getNoShowMessage(result.status),
  });
});

/**
 * Batch check all confirmed bookings for no-show
 * Should be called by cron job
 */
export const checkAllNoShows = asyncHandler(async (req: Request, res: Response) => {
  const results = await noShowService.checkAllNoShows();

  return ok(res, {
    processed: results.length,
    results: results.map((r) => ({
      bookingId: r.bookingId,
      status: r.status,
      refundAmount: r.refundAmount,
      platformFee: r.platformFee,
    })),
  });
});

function getNoShowMessage(status: string): string {
  switch (status) {
    case 'NoShowMentee':
      return 'Mentee không tham gia - Mentor giữ 100% tiền';
    case 'NoShowMentor':
      return 'Mentor không tham gia - Hoàn 100% cho mentee';
    case 'NoShowBoth':
      return 'Cả hai không tham gia - Hoàn 80% cho mentee, platform giữ 20%';
    default:
      return 'No-show processed';
  }
}

export default {
  createJoinToken,
  getSessionLog,
  adminListSessions,
  checkNoShow,
  checkAllNoShows,
};
