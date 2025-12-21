import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { asyncHandler } from '../handlers/async.handler';
import {
  ok,
  created,
  badRequest,
  notFound,
  forbidden,
  conflict,
} from '../handlers/response.handler';
import Booking, { TBookingStatus } from '../models/booking.model';
import AvailabilityOccurrence from '../models/availabilityOccurrence.model';
import User from '../models/user.model';
import Profile from '../models/profile.model';
import redis from '../utils/redis';
import {
  sendBookingConfirmedEmail,
  sendBookingFailedEmail,
  sendBookingCancelledEmail,
  resendBookingIcsEmail,
  BookingEmailData,
} from '../utils/email.service';
import {
  notifyBookingConfirmed,
  notifyBookingFailed,
  notifyBookingCancelled,
} from '../utils/notification.service';
import { generateBookingIcs } from '../utils/ics.service';

const BOOKING_EXPIRY_MINUTES = parseInt(process.env.BOOKING_EXPIRY_MINUTES || '15', 10) || 15;

// Valid state transitions
const VALID_TRANSITIONS: Record<TBookingStatus, TBookingStatus[]> = {
  PaymentPending: ['Confirmed', 'Failed', 'Cancelled'],
  Confirmed: ['Completed', 'Cancelled'],
  Failed: [],
  Cancelled: [],
  Completed: [],
};

function canTransition(from: TBookingStatus, to: TBookingStatus): boolean {
  return VALID_TRANSITIONS[from]?.includes(to) ?? false;
}

async function getUserInfo(userId: string) {
  const [user, profile] = await Promise.all([
    User.findById(userId).select('email userName').lean(),
    Profile.findOne({ user: userId }).select('fullName').lean(),
  ]);
  return {
    email: user?.email ?? '',
    name: profile?.fullName ?? user?.userName ?? 'User',
  };
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

function formatBookingResponse(booking: any) {
  return {
    id: String(booking._id),
    menteeId: String(booking.mentee),
    mentorId: String(booking.mentor),
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
    createdAt: new Date(booking.createdAt).toISOString(),
  };
}

/**
 * POST /api/bookings
 * Create a new booking with status = PaymentPending, lock the occurrence
 */
export const createBooking = asyncHandler(async (req: Request, res: Response) => {
  const menteeId = (req as any).user?.id;
  if (!menteeId) return badRequest(res, 'Unauthorized');

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
  let lockAcquired = false;
  try {
    if (redis?.isOpen) {
      const lockResult = await redis.set(lockKey, menteeId, { NX: true, PX: 30_000 });
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
        status: { $nin: ['Failed', 'Cancelled'] },
      }).session(session);

      if (existingBooking) {
        await session.abortTransaction();
        session.endSession();
        return conflict(res, 'This slot already has an active booking');
      }

      // Get mentor profile for pricing
      const mentorProfile = await Profile.findOne({ user: mentorId }).select('hourlyRateVnd').lean();
      const hourlyRate = mentorProfile?.hourlyRateVnd ?? 0;
      const durationMs = new Date(occurrence.end).getTime() - new Date(occurrence.start).getTime();
      const durationHours = durationMs / (1000 * 60 * 60);
      const price = hourlyRate * durationHours;

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

      return created(res, formatBookingResponse(booking[0]), 'Booking created, awaiting payment');
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  } finally {
    if (redis?.isOpen) {
      try {
        await redis.del(lockKey);
      } catch {}
    }
  }
});

/**
 * GET /api/bookings
 * List bookings for current user (mentee or mentor role)
 */
export const getBookings = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  const userRole = (req as any).user?.role;
  if (!userId) return badRequest(res, 'Unauthorized');

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

  return ok(res, {
    bookings: bookings.map(formatBookingResponse),
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
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { id } = req.params;

  const booking = await Booking.findById(id).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  // Only allow mentee or mentor to view
  if (String(booking.mentee) !== userId && String(booking.mentor) !== userId) {
    return forbidden(res, 'Access denied');
  }

  return ok(res, formatBookingResponse(booking));
});

/**
 * POST /api/bookings/:id/cancel
 * Cancel a booking
 */
export const cancelBooking = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  const userRole = (req as any).user?.role;
  if (!userId) return badRequest(res, 'Unauthorized');

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

  return ok(res, formatBookingResponse(booking), 'Booking cancelled');
});

/**
 * POST /api/bookings/:id/resend-ics
 * Resend ICS calendar file for confirmed booking
 */
export const resendIcs = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

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
  await booking.save();

  // Send notifications and emails
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

export default {
  createBooking,
  getBookings,
  getBookingById,
  cancelBooking,
  resendIcs,
  confirmBooking,
  failBooking,
  processExpiredBookings,
};
