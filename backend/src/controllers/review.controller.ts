import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { asyncHandler } from '../handlers/async.handler';
import {
  ok,
  created,
  badRequest,
  notFound,
  unauthorized,
  forbidden,
  conflict,
  internalServerError,
} from '../handlers/response.handler';
import Review from '../models/review.model';
import Booking from '../models/booking.model';
import Profile from '../models/profile.model';
import User from '../models/user.model';

/**
 * Retry wrapper for transient transaction errors (reused from walletBooking.service.ts)
 */
async function retryTransaction<T = void>(
  operation: () => Promise<T>,
  maxRetries = 3
): Promise<T> {
  let lastError: any;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error: any) {
      lastError = error;

      // Do NOT retry duplicate key errors (11000) - these are business logic conflicts
      if (error?.code === 11000) {
        throw error;
      }

      // Robust transient detection:
      // - Prefer hasErrorLabel if available
      // - Fallback to errorLabels array
      // - Also include WriteConflict message (can happen without labels)
      const hasLabel =
        typeof error?.hasErrorLabel === 'function' &&
        (error.hasErrorLabel('TransientTransactionError') ||
          error.hasErrorLabel('UnknownTransactionCommitResult'));

      const hasArrayLabel =
        error?.errorLabels?.includes('TransientTransactionError') ||
        error?.errorLabels?.includes('UnknownTransactionCommitResult');

      const hasWriteConflict = String(error?.message ?? '').includes('WriteConflict');

      const isTransient = hasLabel || hasArrayLabel || hasWriteConflict;

      if (isTransient && attempt < maxRetries) {
        await new Promise((resolve) => setTimeout(resolve, 50 * attempt));
        continue;
      }

      break;
    }
  }

  throw lastError;
}


/**
 * POST /bookings/:id/review
 * Create a review for a completed booking
 * Auth: mentee only (booking owner)
 * 
 * Business rules:
 * - Only booking's mentee can create review
 * - Booking must have status = 'Completed'
 * - One review per booking (enforced by unique index + transaction re-check)
 * - Atomically updates: Review doc, Booking.reviewId/reviewedAt, Profile.rating
 * - Uses transaction retry for transient errors (WriteConflict, TransientTransactionError)
 */
export const createReview = asyncHandler(async (req: Request, res: Response) => {
  const bookingId = req.params.id;
  const { rating, comment } = req.body;
  
  // Extract userId safely (handle both id and _id patterns)
  const rawUserId = (req as any).user?.id ?? (req as any).user?._id;
  if (!rawUserId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }
  const userId = String(rawUserId);

  // Validate bookingId format
  if (!mongoose.Types.ObjectId.isValid(bookingId)) {
    return badRequest(res, 'INVALID_BOOKING_ID');
  }

  let reviewId: mongoose.Types.ObjectId | null = null;

  try {
    // C) Wrap transaction in retry logic for transient errors
    await retryTransaction(async () => {
      // Reset reviewId at start of each attempt to avoid stale state
      reviewId = null;
      const session = await mongoose.startSession();
      
      try {
        await session.withTransaction(async () => {
          // Re-fetch booking inside transaction to prevent race conditions
          const booking = await Booking.findById(bookingId).session(session);
          if (!booking) {
            throw new Error('BOOKING_NOT_FOUND');
          }

          // Check ownership: only mentee can review
          if (String(booking.mentee) !== userId) {
            throw new Error('ONLY_MENTEE_CAN_REVIEW');
          }

          // Check booking status: must be Completed
          if (booking.status !== 'Completed') {
            throw new Error('BOOKING_NOT_COMPLETED');
          }

          // Check if already reviewed (transaction-safe check)
          if (booking.reviewId) {
            throw new Error('REVIEW_ALREADY_EXISTS');
          }

          // Create review document
          const review = new Review({
            booking: bookingId,
            mentee: booking.mentee,
            mentor: booking.mentor,
            rating,
            comment,
          });
          await review.save({ session });
          reviewId = review._id;

          // Update booking with reviewId and reviewedAt
          booking.reviewId = review._id;
          booking.reviewedAt = new Date();
          await booking.save({ session });

          // Update mentor's profile rating (atomic calculation)
          const profile = await Profile.findOne({ user: booking.mentor }).session(session);
          if (!profile) {
            throw new Error('MENTOR_PROFILE_NOT_FOUND');
          }

          // Calculate new aggregated rating: newAvg = (oldAvg * oldCount + newRating) / newCount
          const oldCount = profile.rating?.count || 0;
          const oldAverage = profile.rating?.average || 0;
          const newCount = oldCount + 1;
          const rawAverage = (oldAverage * oldCount + rating) / newCount;
          
          // Clamp rating to [1, 5] and round to 1 decimal place
          const clampedAverage = Math.max(1, Math.min(5, rawAverage));
          const roundedAverage = Math.round(clampedAverage * 10) / 10;

          profile.rating = {
            average: roundedAverage,
            count: newCount,
          };
          await profile.save({ session });

          // Transaction will auto-commit if no errors thrown
        });
      } finally {
        session.endSession();
      }
    });

    // B) Guard against null reviewId after transaction
    if (!reviewId) {
      return internalServerError(res, 'REVIEW_CREATE_FAILED');
    }

    // Transaction committed successfully - now populate for response
    const populatedReview = await Review.findById(reviewId)
      .populate('mentee', 'userName name email')
      .populate('mentor', 'userName name email')
      .lean();

    if (!populatedReview) {
      return internalServerError(res, 'REVIEW_CREATE_FAILED');
    }

    // Return consistent response shape
    return created(res, { review: populatedReview }, 'Review created successfully');
    
  } catch (error: any) {
    // Handle specific business rule errors
    if (error.message === 'BOOKING_NOT_FOUND') {
      return notFound(res, 'BOOKING_NOT_FOUND');
    }
    if (error.message === 'ONLY_MENTEE_CAN_REVIEW') {
      return forbidden(res, 'ONLY_MENTEE_CAN_REVIEW');
    }
    if (error.message === 'BOOKING_NOT_COMPLETED') {
      return badRequest(res, 'BOOKING_NOT_COMPLETED');
    }
    if (error.message === 'MENTOR_PROFILE_NOT_FOUND') {
      return notFound(res, 'MENTOR_PROFILE_NOT_FOUND');
    }
    
    // D) Make idempotent: on conflict, fetch and return existing review (no 409)
    if (error.message === 'REVIEW_ALREADY_EXISTS' || error.code === 11000) {
      // Fetch existing review instead of returning conflict
      try {
        const existingReview = await Review.findOne({ booking: bookingId })
          .populate('mentee', 'userName name email')
          .populate('mentor', 'userName name email')
          .lean();
        
        if (existingReview) {
          // Return as if created successfully (idempotent behavior)
          return ok(res, { review: existingReview }, 'Review already exists');
        }
        
        // If review not found after conflict, fall through to generic conflict
        return conflict(res, 'REVIEW_ALREADY_EXISTS');
      } catch (fetchError) {
        // Failed to fetch existing review - return conflict
        return conflict(res, 'REVIEW_ALREADY_EXISTS');
      }
    }
    
    throw error; // Re-throw for asyncHandler to catch
  }
});

/**
 * GET /mentors/:id/reviews
 * Get all reviews for a mentor (public endpoint)
 * Query params: limit (default 20), cursor (ObjectId for pagination)
 * 
 * Pagination: cursor-based using _id descending (newest first)
 */
export const getMentorReviews = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = req.params.id;
  const limit = parseInt(req.query.limit as string) || 20;
  const cursor = req.query.cursor as string | undefined;

  // Validate mentorId format
  if (!mongoose.Types.ObjectId.isValid(mentorId)) {
    return badRequest(res, 'INVALID_MENTOR_ID');
  }

  // Check if mentor exists and has mentor role
  const mentor = await User.findById(mentorId).select('role').lean();
  if (!mentor) {
    return notFound(res, 'MENTOR_NOT_FOUND');
  }
  if (mentor.role !== 'mentor') {
    return badRequest(res, 'USER_IS_NOT_MENTOR');
  }

  // Build query with cursor-based pagination using _id (consistent with sort)
  const query: any = { mentor: mentorId };
  if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
    query._id = { $lt: cursor }; // Get reviews older than cursor
  }

  // Fetch reviews sorted by _id desc (newest first, matches createdAt order)
  const reviews = await Review.find(query)
    .sort({ _id: -1 })
    .limit(limit + 1) // Fetch one extra to check hasMore
    .populate('mentee', 'userName name')
    .lean();

  // Pagination logic
  const hasMore = reviews.length > limit;
  const results = hasMore ? reviews.slice(0, limit) : reviews;
  const nextCursor = hasMore ? String(results[results.length - 1]._id) : null;

  return ok(res, {
    reviews: results,
    pagination: {
      hasMore,
      nextCursor,
      limit,
    },
  });
});

/**
 * GET /reviews/me
 * Get all reviews created by the authenticated user (mentee)
 * Query params: limit (default 20), cursor (ObjectId for pagination)
 * 
 * Pagination: cursor-based using _id descending (newest first)
 */
export const getMyReviews = asyncHandler(async (req: Request, res: Response) => {
  // Extract userId safely
  const rawUserId = (req as any).user?.id ?? (req as any).user?._id;
  if (!rawUserId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }
  const userId = String(rawUserId);

  const limit = parseInt(req.query.limit as string) || 20;
  const cursor = req.query.cursor as string | undefined;

  // Build query with cursor-based pagination using _id
  const query: any = { mentee: userId };
  if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
    query._id = { $lt: cursor };
  }

  // Fetch reviews sorted by _id desc (newest first)
  const reviews = await Review.find(query)
    .sort({ _id: -1 })
    .limit(limit + 1)
    .populate('mentor', 'userName name')
    .populate('booking', 'startTime endTime topic')
    .lean();

  // Pagination logic
  const hasMore = reviews.length > limit;
  const results = hasMore ? reviews.slice(0, limit) : reviews;
  const nextCursor = hasMore ? String(results[results.length - 1]._id) : null;

  return ok(res, {
    reviews: results,
    pagination: {
      hasMore,
      nextCursor,
      limit,
    },
  });
});
