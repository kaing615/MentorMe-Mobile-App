import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { randomUUID } from 'crypto';
import { asyncHandler } from '../handlers/async.handler';
import {
  ok,
  created,
  badRequest,
  notFound,
  unauthorized,
  forbidden,
  conflict,
} from '../handlers/response.handler';
import Booking, { TBookingStatus } from '../models/booking.model';
import AvailabilityOccurrence from '../models/availabilityOccurrence.model';
import AvailabilitySlot from '../models/availabilitySlot.model';
import User from '../models/user.model';
import Profile from '../models/profile.model';
import redis from '../utils/redis';
import { getUserInfo } from '../utils/userInfo';
import {
  sendBookingConfirmedEmail,
  sendBookingFailedEmail,
  sendBookingCancelledEmail,
  resendBookingIcsEmail,
  sendBookingPendingEmail,
  sendBookingDeclinedEmail,
  sendBookingReminderEmail,
  BookingEmailData,
} from '../utils/email.service';
import {
  notifyBookingConfirmed,
  notifyBookingFailed,
  notifyBookingCancelled,
  notifyBookingPending,
  notifyBookingDeclined,
  notifyBookingReminder,
} from '../utils/notification.service';
import { generateBookingIcs } from '../utils/ics.service';
import { refundBookingPayment } from '../services/walletBooking.service';

const BOOKING_EXPIRY_MINUTES = parseInt(process.env.BOOKING_EXPIRY_MINUTES || '15', 10) || 15;
const LATE_CANCEL_MINUTES = parseInt(process.env.LATE_CANCEL_MINUTES || '1440', 10) || 1440;
const LATE_CANCEL_ACTION = (process.env.LATE_CANCEL_ACTION || 'block').toLowerCase();
const MENTOR_CONFIRM_DEADLINE_HOURS =
  parseInt(process.env.MENTOR_CONFIRM_DEADLINE_HOURS || '12', 10) || 12;

// Valid state transitions
const VALID_TRANSITIONS: Record<TBookingStatus, TBookingStatus[]> = {
  PaymentPending: ['PendingMentor', 'Confirmed', 'Failed', 'Cancelled'],
  PendingMentor: ['Confirmed', 'Declined', 'Cancelled'],
  Confirmed: ['Completed', 'Cancelled'],
  Failed: [],
  Cancelled: [],
  Declined: [],
  Completed: [],
};

function canTransition(from: TBookingStatus, to: TBookingStatus): boolean {
  return VALID_TRANSITIONS[from]?.includes(to) ?? false;
}

async function buildEmailData(booking: any): Promise<BookingEmailData> {
  const [mentorInfo, menteeInfo] = await Promise.all([
    getUserInfo(String(booking.mentor)),
    getUserInfo(String(booking.mentee)),
  ]);

  return {
    mentorName: mentorInfo.name,
    menteeName: menteeInfo.name,
    mentorEmail: mentorInfo.email,
    menteeEmail: menteeInfo.email,
    startTime: new Date(booking.startTime),
    endTime: new Date(booking.endTime),
    topic: booking.topic,
    meetingLink: booking.meetingLink,
    location: booking.location,
    bookingId: String(booking._id),
    price: booking.price,
  };
}

type UserSummary = {
  fullName?: string;
  avatarUrl?: string;
  email?: string;
  role?: string;
  userName?: string;
};

async function buildUserSummaryMap(userIds: string[]): Promise<Record<string, UserSummary>> {
  const uniqueIds = Array.from(new Set(userIds.map((id) => String(id)).filter(Boolean)));
  if (!uniqueIds.length) return {};

  const [users, profiles] = await Promise.all([
    User.find({ _id: { $in: uniqueIds } }).select('userName name email role').lean(),
    Profile.find({ user: { $in: uniqueIds } }).select('user fullName avatarUrl').lean(),
  ]);

  const map: Record<string, UserSummary> = {};

  for (const user of users) {
    const id = String(user._id);
    map[id] = {
      ...map[id],
      userName: user.userName,
      email: user.email,
      role: (user as any).role,
      fullName: (user as any).name ?? user.userName ?? map[id]?.fullName,
    };
  }

  for (const profile of profiles) {
    const id = String((profile as any).user);
    if (!id) continue;
    map[id] = {
      ...map[id],
      fullName: profile.fullName ?? map[id]?.fullName,
      avatarUrl: profile.avatarUrl ?? map[id]?.avatarUrl,
    };
  }

  return map;
}

function toBookingUser(summary: UserSummary | undefined, id: string) {
  if (!summary) return null;
  return {
    id,
    fullName: summary.fullName ?? null,
    avatar: summary.avatarUrl ?? null,
    email: summary.email ?? null,
    role: summary.role ?? null,
  };
}

function formatBookingResponse(booking: any, userSummaries?: Record<string, UserSummary>) {
  const menteeId = String(booking.mentee);
  const mentorId = String(booking.mentor);
  const menteeSummary = userSummaries?.[menteeId];
  const mentorSummary = userSummaries?.[mentorId];

  return {
    id: String(booking._id),
    menteeId,
    mentorId,
    occurrenceId: String(booking.occurrence),
    date: new Date(booking.startTime).toISOString().split('T')[0],
    startTime: new Date(booking.startTime).toISOString().substring(11, 16),
    endTime: new Date(booking.endTime).toISOString().substring(11, 16),
    startTimeIso: new Date(booking.startTime).toISOString(),
    endTimeIso: new Date(booking.endTime).toISOString(),
    status: booking.status,
    price: booking.price,
    topic: booking.topic ?? null,
    notes: booking.notes ?? null,
    meetingLink: booking.meetingLink ?? null,
    location: booking.location ?? null,
    expiresAt: booking.expiresAt ? new Date(booking.expiresAt).toISOString() : null,
    mentorResponseDeadline: booking.mentorResponseDeadline
      ? new Date(booking.mentorResponseDeadline).toISOString()
      : null,
    reminder24hSentAt: booking.reminder24hSentAt
      ? new Date(booking.reminder24hSentAt).toISOString()
      : null,
    reminder1hSentAt: booking.reminder1hSentAt
      ? new Date(booking.reminder1hSentAt).toISOString()
      : null,
    lateCancel: booking.lateCancel ?? false,
    lateCancelMinutes: booking.lateCancelMinutes ?? null,
    reviewId: booking.reviewId ? String(booking.reviewId) : null,
    reviewedAt: booking.reviewedAt ? new Date(booking.reviewedAt).toISOString() : null,
    createdAt: new Date(booking.createdAt).toISOString(),
    mentorFullName: mentorSummary?.fullName ?? null,
    menteeFullName: menteeSummary?.fullName ?? null,
    mentor: toBookingUser(mentorSummary, mentorId),
    mentee: toBookingUser(menteeSummary, menteeId),
  };
}

/**
 * POST /api/bookings
 * Create a new booking with status = PaymentPending, lock the occurrence
 */
export const createBooking = asyncHandler(async (req: Request, res: Response) => {
  const menteeId = (req as any).user?.id ?? (req as any).user?._id;
  if (!menteeId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { mentorId, occurrenceId, topic, notes } = req.body as {
    mentorId: string;
    occurrenceId: string;
    topic?: string;
    notes?: string;
  };

  // Validate mentor exists
  const mentor = await User.findById(mentorId).lean();
  if (!mentor || (mentor as any).role !== 'mentor') {
    return notFound(res, 'Mentor not found');
  }

  // Acquire Redis lock on occurrence to prevent race conditions.
  // Falls back to MongoDB transaction if Redis is unavailable - MongoDB's
  // unique index on occurrence + transaction provides atomicity protection.
  const lockKey = `booking:lock:${occurrenceId}`;
  const lockToken = randomUUID();
  let lockAcquired = false;
  try {
    if (redis?.isOpen) {
      const lockResult = await redis.set(lockKey, lockToken, { NX: true, PX: 30_000 });
      lockAcquired = lockResult === 'OK';
    } else {
      // Redis unavailable; rely on MongoDB unique constraint + transaction
      lockAcquired = true;
    }
  } catch {
    // Redis error; rely on MongoDB unique constraint + transaction
    lockAcquired = true;
  }

  if (!lockAcquired) {
    return conflict(res, 'Slot is currently being booked by another user');
  }

  try {
    // Find and lock the occurrence
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const occurrence = await AvailabilityOccurrence.findOneAndUpdate(
        {
          _id: occurrenceId,
          mentor: mentorId,
          status: 'open',
        },
        { $set: { status: 'booked' } },
        { new: true, session }
      );

      if (!occurrence) {
        await session.abortTransaction();
        session.endSession();
        return conflict(res, 'Slot is not available or already booked');
      }

      // Check if mentee already has an active booking for this occurrence
      const existingBooking = await Booking.findOne({
        occurrence: occurrenceId,
        status: { $nin: ['Failed', 'Cancelled', 'Declined', 'Completed'] },
      }).session(session);

      if (existingBooking) {
        await session.abortTransaction();
        session.endSession();
        return conflict(res, 'This slot already has an active booking');
      }

      // Get slot price first; fallback to mentor hourly rate if missing
      const slot = await AvailabilitySlot.findById(occurrence.slot)
        .select('priceVnd')
        .session(session)
        .lean();
      const slotPrice = typeof slot?.priceVnd === 'number' ? slot.priceVnd : null;
      let price = 0;
      if (slotPrice != null) {
        price = Math.max(0, slotPrice);
      } else {
        const mentorProfile = await Profile.findOne({ user: mentorId })
          .select('hourlyRateVnd')
          .session(session)
          .lean();
        const hourlyRate = mentorProfile?.hourlyRateVnd ?? 0;
        const durationMs = new Date(occurrence.end).getTime() - new Date(occurrence.start).getTime();
        const durationHours = durationMs / (1000 * 60 * 60);
        price = Math.max(0, hourlyRate * durationHours);
      }

      const expiresAt = new Date(Date.now() + BOOKING_EXPIRY_MINUTES * 60 * 1000);

      const booking = await Booking.create(
        [
          {
            mentee: menteeId,
            mentor: mentorId,
            occurrence: occurrenceId,
            startTime: occurrence.start,
            endTime: occurrence.end,
            price,
            status: 'PaymentPending',
            topic,
            notes,
            expiresAt,
          },
        ],
        { session }
      );

      await session.commitTransaction();
      session.endSession();

      // Schedule expiry check in Redis
      if (redis?.isOpen) {
        const expiryKey = `booking:expiry:${String(booking[0]._id)}`;
        await redis.setEx(expiryKey, BOOKING_EXPIRY_MINUTES * 60, occurrenceId);
      }

      const userSummaries = await buildUserSummaryMap([menteeId, mentorId]);
      return created(
        res,
        formatBookingResponse(booking[0], userSummaries),
        'Booking created, awaiting payment'
      );
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  } finally {
    if (redis?.isOpen) {
      try {
        // Only delete lock if we still own it (prevent deleting another request's lock)
        const luaScript = `
          if redis.call("GET", KEYS[1]) == ARGV[1] then
            return redis.call("DEL", KEYS[1])
          else
            return 0
          end
        `;
        await redis.eval(luaScript, {
          keys: [lockKey],
          arguments: [lockToken],
        });
      } catch {}
    }
  }
});

/**
 * GET /api/bookings
 * List bookings for current user (mentee or mentor role)
 */
export const getBookings = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { role, status, page = 1, limit = 10 } = req.query as {
    role?: 'mentee' | 'mentor';
    status?: TBookingStatus;
    page?: number;
    limit?: number;
  };

  const pageNum = Number(page) || 1;
  const limitNum = Math.min(Number(limit) || 10, 100);
  const skip = (pageNum - 1) * limitNum;

  // Build query based on role filter or user's actual role
  const queryRole = role || (userRole === 'mentor' ? 'mentor' : 'mentee');
  const query: any = queryRole === 'mentor' ? { mentor: userId } : { mentee: userId };

  if (status) {
    query.status = status;
  }

  const [bookings, total] = await Promise.all([
    Booking.find(query)
      .sort({ startTime: -1 })
      .skip(skip)
      .limit(limitNum)
      .lean(),
    Booking.countDocuments(query),
  ]);

  const totalPages = Math.ceil(total / limitNum);

  const userSummaries = await buildUserSummaryMap(
    bookings.flatMap((b) => [String(b.mentee), String(b.mentor)])
  );

  // Set Content-Range header for react-admin pagination
  const start = skip;
  const end = Math.min(skip + bookings.length - 1, total - 1);
  res.set('Content-Range', `bookings ${start}-${end}/${total}`);
  res.set('Access-Control-Expose-Headers', 'Content-Range');

  return ok(res, {
    bookings: bookings.map((booking) => formatBookingResponse(booking, userSummaries)),
    total,
    page: pageNum,
    totalPages,
  });
});

/**
 * GET /api/bookings/:id
 * Get booking details
 */
export const getBookingById = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { id } = req.params;

  const booking = await Booking.findById(id).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Only allow mentee or mentor to view
  if (String(booking.mentee) !== userId && String(booking.mentor) !== userId) {
    return forbidden(res, 'Access denied');
  }

  const userSummaries = await buildUserSummaryMap([
    String(booking.mentee),
    String(booking.mentor),
  ]);
  return ok(res, formatBookingResponse(booking, userSummaries));
});

/**
 * POST /api/bookings/:id/cancel
 * Cancel a booking
 */
export const cancelBooking = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { id } = req.params;
  const { reason } = req.body as { reason?: string };

  const booking = await Booking.findById(id);
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Only mentee or mentor can cancel
  const isMentee = String(booking.mentee) === userId;
  const isMentor = String(booking.mentor) === userId;
  if (!isMentee && !isMentor) {
    return forbidden(res, 'Access denied');
  }

  // Check valid transition
  if (!canTransition(booking.status, 'Cancelled')) {
    return badRequest(res, `Cannot cancel booking with status ${booking.status}`);
  }

  const now = new Date();
  const startTime = new Date(booking.startTime);
  if (startTime.getTime() <= now.getTime()) {
    return badRequest(res, 'Cannot cancel after the session has started');
  }

  const minutesToStart = Math.floor((startTime.getTime() - now.getTime()) / 60000);
  if (LATE_CANCEL_MINUTES > 0 && minutesToStart < LATE_CANCEL_MINUTES) {
    if (LATE_CANCEL_ACTION === 'block') {
      return badRequest(
        res,
        `Late cancellation is not allowed within ${LATE_CANCEL_MINUTES} minutes of the session`
      );
    }
    booking.lateCancel = true;
    booking.lateCancelMinutes = Math.max(0, minutesToStart);
  }

  // Update booking status
  booking.status = 'Cancelled';
  booking.cancelledBy = new mongoose.Types.ObjectId(userId);
  booking.cancelReason = reason;
  await booking.save();

  // Release the occurrence
  await AvailabilityOccurrence.updateOne(
    { _id: booking.occurrence },
    { $set: { status: 'open' } }
  );

  // Send notifications and emails
  try {
    const emailData = await buildEmailData(booking);
    await Promise.all([
      sendBookingCancelledEmail(emailData, isMentor),
      notifyBookingCancelled(
        {
          bookingId: String(booking._id),
          mentorId: String(booking.mentor),
          menteeId: String(booking.mentee),
          mentorName: emailData.mentorName,
          menteeName: emailData.menteeName,
          startTime: new Date(booking.startTime),
        },
        isMentor
      ),
    ]);
  } catch (err) {
    console.error('Failed to send cancellation notifications:', err);
  }

  // Refund payment to mentee (best effort)
  try {
    await refundBookingPayment(String(booking._id));
  } catch (err) {
    console.error('Failed to refund booking payment:', err);
  }


  const userSummaries = await buildUserSummaryMap([
    String(booking.mentee),
    String(booking.mentor),
  ]);
  return ok(res, formatBookingResponse(booking, userSummaries), 'Booking cancelled');
});

/**
 * POST /api/bookings/:id/resend-ics
 * Resend ICS calendar file for confirmed booking
 */
export const resendIcs = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { id } = req.params;

  const booking = await Booking.findById(id).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Only mentee can request resend
  if (String(booking.mentee) !== userId) {
    return forbidden(res, 'Access denied');
  }

  if (booking.status !== 'Confirmed') {
    return badRequest(res, 'ICS can only be sent for confirmed bookings');
  }

  const emailData = await buildEmailData(booking);

  const icsContent = generateBookingIcs(
    String(booking._id),
    emailData.mentorName,
    emailData.mentorEmail,
    emailData.menteeName,
    emailData.menteeEmail,
    new Date(booking.startTime),
    new Date(booking.endTime),
    booking.meetingLink,
    booking.location,
    booking.topic
  );

  try {
    await resendBookingIcsEmail(emailData, icsContent);
  } catch (err) {
    console.error('Failed to resend ICS:', err);
    return badRequest(res, 'Failed to send calendar invite');
  }

  return ok(res, { sent: true }, 'Calendar invite sent');
});

/**
 * POST /api/bookings/:id/mentor-confirm
 * Mentor confirms a pending booking
 */
export const mentorConfirmBooking = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }
  if (userRole !== 'mentor') return forbidden(res, 'Mentor access required');

  const { id } = req.params;

  const booking = await Booking.findById(id);
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  if (String(booking.mentor) !== userId) {
    return forbidden(res, 'Access denied');
  }

  if (booking.status !== 'PendingMentor') {
    return badRequest(res, 'Booking is not awaiting mentor confirmation');
  }

  await confirmBooking(String(booking._id));

  const updated = await Booking.findById(id).lean();
  if (!updated) return ok(res, null, 'Booking confirmed');
  const userSummaries = await buildUserSummaryMap([
    String(updated.mentee),
    String(updated.mentor),
  ]);
  return ok(res, formatBookingResponse(updated, userSummaries), 'Booking confirmed');
});

/**
 * POST /api/bookings/:id/mentor-decline
 * Mentor declines a pending booking
 */
export const mentorDeclineBooking = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }
  if (userRole !== 'mentor') return forbidden(res, 'Mentor access required');

  const { id } = req.params;
  const { reason } = req.body as { reason?: string };

  const booking = await Booking.findById(id);
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  if (String(booking.mentor) !== userId) {
    return forbidden(res, 'Access denied');
  }

  if (booking.status !== 'PendingMentor') {
    return badRequest(res, 'Booking is not awaiting mentor confirmation');
  }

  await declineBooking(String(booking._id), reason, userId);

  const updated = await Booking.findById(id).lean();
  if (!updated) return ok(res, null, 'Booking declined');
  const userSummaries = await buildUserSummaryMap([
    String(updated.mentee),
    String(updated.mentor),
  ]);
  return ok(res, formatBookingResponse(updated, userSummaries), 'Booking declined');
});

/**
 * Confirm booking (called after successful payment)
 */
export async function confirmBooking(bookingId: string): Promise<void> {
  const booking = await Booking.findById(bookingId);
  if (!booking) {
    throw new Error('Booking not found');
  }

  if (!canTransition(booking.status, 'Confirmed')) {
    throw new Error(`Cannot confirm booking with status ${booking.status}`);
  }

  booking.status = 'Confirmed';
  booking.expiresAt = undefined;
  booking.mentorResponseDeadline = undefined;
  await booking.save();

  // Send notifications and emails (best effort)
  try {
    const emailData = await buildEmailData(booking);

    const icsContent = generateBookingIcs(
      String(booking._id),
      emailData.mentorName,
      emailData.mentorEmail,
      emailData.menteeName,
      emailData.menteeEmail,
      new Date(booking.startTime),
      new Date(booking.endTime),
      booking.meetingLink,
      booking.location,
      booking.topic
    );

    await Promise.all([
      sendBookingConfirmedEmail(emailData, icsContent),
      notifyBookingConfirmed({
        bookingId: String(booking._id),
        mentorId: String(booking.mentor),
        menteeId: String(booking.mentee),
        mentorName: emailData.mentorName,
        menteeName: emailData.menteeName,
        startTime: new Date(booking.startTime),
      }),
    ]);
  } catch (err) {
    console.error('Failed to send confirmation notifications:', err);
  }
}

/**
 * Mark booking as awaiting mentor confirmation
 */
export async function markBookingPendingMentor(bookingId: string): Promise<void> {
  const booking = await Booking.findById(bookingId);
  if (!booking) {
    throw new Error('Booking not found');
  }

  if (!canTransition(booking.status, 'PendingMentor')) {
    throw new Error(`Cannot mark booking pending with status ${booking.status}`);
  }

  const deadlineMs = Math.min(
    Date.now() + MENTOR_CONFIRM_DEADLINE_HOURS * 60 * 60 * 1000,
    new Date(booking.startTime).getTime()
  );

  booking.status = 'PendingMentor';
  booking.expiresAt = undefined;
  booking.mentorResponseDeadline = new Date(deadlineMs);
  await booking.save();

  try {
    const emailData = await buildEmailData(booking);
    await Promise.all([
      sendBookingPendingEmail(emailData, booking.mentorResponseDeadline),
      notifyBookingPending({
        bookingId: String(booking._id),
        mentorId: String(booking.mentor),
        menteeId: String(booking.mentee),
        mentorName: emailData.mentorName,
        menteeName: emailData.menteeName,
        startTime: new Date(booking.startTime),
      }),
    ]);
  } catch (err) {
    console.error('Failed to send pending booking notifications:', err);
  }
}

/**
 * Decline a booking (mentor decision or auto-decline)
 */
export async function declineBooking(
  bookingId: string,
  reason?: string,
  declinedById?: string
): Promise<void> {
  const booking = await Booking.findById(bookingId);
  if (!booking) {
    throw new Error('Booking not found');
  }

  if (!canTransition(booking.status, 'Declined')) {
    throw new Error(`Cannot decline booking with status ${booking.status}`);
  }

  booking.status = 'Declined';
  booking.mentorResponseDeadline = undefined;
  if (declinedById) {
    booking.cancelledBy = new mongoose.Types.ObjectId(declinedById);
  }
  if (reason) {
    booking.cancelReason = reason;
  }
  await booking.save();

  await AvailabilityOccurrence.updateOne(
    { _id: booking.occurrence },
    { $set: { status: 'open' } }
  );

  try {
    const emailData = await buildEmailData(booking);
    await Promise.all([
      sendBookingDeclinedEmail(emailData),
      notifyBookingDeclined({
        bookingId: String(booking._id),
        mentorId: String(booking.mentor),
        menteeId: String(booking.mentee),
        mentorName: emailData.mentorName,
        menteeName: emailData.menteeName,
        startTime: new Date(booking.startTime),
      }),
    ]);
  } catch (err) {
    console.error('Failed to send decline notifications:', err);
  }

  // Refund payment to mentee (best effort)
  try {
    await refundBookingPayment(String(booking._id));
  } catch (err) {
    console.error('Failed to refund booking payment:', err);
  }
}

/**
 * Fail booking (called on payment failure or timeout)
 */
export async function failBooking(bookingId: string): Promise<void> {
  const booking = await Booking.findById(bookingId);
  if (!booking) {
    throw new Error('Booking not found');
  }

  if (!canTransition(booking.status, 'Failed')) {
    throw new Error(`Cannot fail booking with status ${booking.status}`);
  }

  booking.status = 'Failed';
  await booking.save();

  // Release the occurrence
  await AvailabilityOccurrence.updateOne(
    { _id: booking.occurrence },
    { $set: { status: 'open' } }
  );

  // Send notifications and emails
  try {
    const emailData = await buildEmailData(booking);
    await Promise.all([
      sendBookingFailedEmail(emailData),
      notifyBookingFailed({
        bookingId: String(booking._id),
        mentorId: String(booking.mentor),
        menteeId: String(booking.mentee),
        mentorName: emailData.mentorName,
        menteeName: emailData.menteeName,
        startTime: new Date(booking.startTime),
      }),
    ]);
  } catch (err) {
    console.error('Failed to send failure notifications:', err);
  }
}

/**
 * Process expired bookings (can be called by cron job or scheduled task)
 */
export async function processExpiredBookings(): Promise<number> {
  const now = new Date();
  const expiredBookings = await Booking.find({
    status: 'PaymentPending',
    expiresAt: { $lte: now },
  });

  let failedCount = 0;
  for (const booking of expiredBookings) {
    try {
      await failBooking(String(booking._id));
      failedCount++;
    } catch (err) {
      console.error(`Failed to expire booking ${booking._id}:`, err);
    }
  }

  return failedCount;
}

/**
 * Auto-decline pending mentor bookings past the response deadline.
 */
export async function processPendingMentorBookings(): Promise<number> {
  const now = new Date();
  const pendingBookings = await Booking.find({
    status: 'PendingMentor',
    mentorResponseDeadline: { $lte: now },
  });

  let declinedCount = 0;
  for (const booking of pendingBookings) {
    try {
      await declineBooking(String(booking._id), 'Mentor did not respond in time');
      declinedCount++;
    } catch (err) {
      console.error(`Failed to auto-decline booking ${booking._id}:`, err);
    }
  }

  return declinedCount;
}

/**
 * Send reminder emails/notifications for upcoming confirmed sessions.
 */
export async function processBookingReminders(): Promise<{ reminded24h: number; reminded1h: number }> {
  const now = new Date();
  const windowMinutes = parseInt(process.env.REMINDER_WINDOW_MINUTES || '10', 10) || 10;

  const reminderWindows = [
    { hours: 24, field: 'reminder24hSentAt' },
    { hours: 1, field: 'reminder1hSentAt' },
  ] as const;

  const results = { reminded24h: 0, reminded1h: 0 };

  for (const window of reminderWindows) {
    const field = window.field;
    const minMs = (window.hours * 60 - windowMinutes) * 60 * 1000;
    const maxMs = (window.hours * 60 + windowMinutes) * 60 * 1000;
    const start = new Date(now.getTime() + minMs);
    const end = new Date(now.getTime() + maxMs);

    const bookings = await Booking.find({
      status: 'Confirmed',
      startTime: { $gte: start, $lte: end },
      [field]: { $exists: false },
    });

    for (const booking of bookings) {
      try {
        const emailData = await buildEmailData(booking);
        await Promise.all([
          sendBookingReminderEmail(emailData, window.hours),
          notifyBookingReminder({
            bookingId: String(booking._id),
            mentorId: String(booking.mentor),
            menteeId: String(booking.mentee),
            mentorName: emailData.mentorName,
            menteeName: emailData.menteeName,
            startTime: new Date(booking.startTime),
          }),
        ]);

        await Booking.updateOne(
          { _id: booking._id, [field]: { $exists: false } },
          { $set: { [field]: new Date() } }
        );

        if (window.hours === 24) results.reminded24h++;
        if (window.hours === 1) results.reminded1h++;
      } catch (err) {
        console.error(`Failed to send reminder for booking ${booking._id}:`, err);
      }
    }
  }

  return results;
}

/**
 * POST /bookings/:id/complete
 * Complete a confirmed booking (mentor-only)
 * Transitions: Confirmed -> Completed
 */
export const completeBooking = asyncHandler(async (req: Request, res: Response) => {
  const bookingId = req.params.id;
  const rawUserId = (req as any).user?.id ?? (req as any).user?._id;
  const userRole = (req as any).user?.role;

  if (!rawUserId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }
  const userId = String(rawUserId);

  if (userRole !== 'mentor') {
    return forbidden(res, 'ONLY_MENTOR_CAN_COMPLETE');
  }

  if (!mongoose.Types.ObjectId.isValid(bookingId)) {
    return badRequest(res, 'INVALID_BOOKING_ID');
  }

  const booking = await Booking.findById(bookingId);
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Check mentor ownership
  if (String(booking.mentor) !== userId) {
    return forbidden(res, 'ONLY_BOOKING_MENTOR_CAN_COMPLETE');
  }

  // Check current status
  if (booking.status !== 'Confirmed') {
    return badRequest(res, 'BOOKING_MUST_BE_CONFIRMED');
  }

  // Check if session has ended (optional based on env flag)
  const allowEarlyComplete = process.env.ALLOW_EARLY_COMPLETE === 'true';
  const now = new Date();
  const endTime = new Date(booking.endTime);
  if (!allowEarlyComplete && now < endTime) {
    return badRequest(res, 'BOOKING_NOT_ENDED_YET');
  }

  // Validate state transition
  if (!canTransition(booking.status, 'Completed')) {
    return badRequest(res, `Cannot transition from ${booking.status} to Completed`);
  }

  // Update booking status
  booking.status = 'Completed';
  await booking.save();

  // Build response with user summaries
  const userSummaries = await buildUserSummaryMap([String(booking.mentee), String(booking.mentor)]);

  return ok(res, formatBookingResponse(booking, userSummaries), 'Booking completed successfully');
});

export default {
  createBooking,
  getBookings,
  getBookingById,
  cancelBooking,
  resendIcs,
  mentorConfirmBooking,
  mentorDeclineBooking,
  completeBooking,
  confirmBooking,
  markBookingPendingMentor,
  declineBooking,
  failBooking,
  processExpiredBookings,
  processPendingMentorBookings,
  processBookingReminders,
};
