import { Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { randomUUID } from 'crypto';
import { asyncHandler } from '../handlers/async.handler';
import { ok, badRequest, notFound, forbidden, unauthorized } from '../handlers/response.handler';
import Booking from '../models/booking.model';
import SessionLog from '../models/sessionLog.model';
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

export default {
  createJoinToken,
  getSessionLog,
};
