// path: src/controllers/payment.controller.ts
import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import { ok, badRequest, notFound } from '../handlers/response.handler';
import Booking from '../models/booking.model';
import { confirmBooking, failBooking } from './booking.controller';

/**
 * POST /api/payments/webhook
 * Handle payment gateway webhook events
 */
export const paymentWebhook = asyncHandler(async (req: Request, res: Response) => {
  const { event, bookingId, paymentId, status, metadata } = req.body as {
    event: string;
    bookingId: string;
    paymentId?: string;
    status?: string;
    metadata?: Record<string, unknown>;
  };

  if (!event || !bookingId) {
    return badRequest(res, 'event and bookingId are required');
  }

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  try {
    switch (event) {
      case 'payment.success':
      case 'payment.completed':
        await confirmBooking(bookingId);
        console.log(`Payment confirmed for booking ${bookingId}`);
        break;

      case 'payment.failed':
      case 'payment.expired':
      case 'payment.cancelled':
        await failBooking(bookingId);
        console.log(`Payment failed for booking ${bookingId}, reason: ${event}`);
        break;

      default:
        console.log(`Unhandled payment event: ${event} for booking ${bookingId}`);
    }
  } catch (err: any) {
    console.error(`Failed to process payment webhook for ${bookingId}:`, err?.message);
    return badRequest(res, 'Failed to process webhook');
  }

  return ok(res, { processed: true, event, bookingId });
});

export default {
  paymentWebhook,
};
