import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";
import { ok, badRequest, notFound } from "../handlers/response.handler";
import Booking from "../models/booking.model";
import {
  confirmBooking,
  failBooking,
  markBookingPendingMentor,
} from "./booking.controller";
import { captureBookingPayment } from "../services/walletBooking.service";

const TERMINAL_BOOKING_STATUSES = new Set([
  "Failed",
  "Cancelled",
  "Declined",
  "Completed",
]);

/**
 * POST /api/payments/webhook
 * Handle payment gateway webhook events
 *
 * Rules:
 * - payment.success: only acts when booking is PaymentPending
 * - if booking is terminal or already moved state -> return 200 ignored (idempotent)
 */
export const paymentWebhook = asyncHandler(async (req: Request, res: Response) => {
  const { event, bookingId } = req.body as {
    event: string;
    bookingId: string;
    paymentId?: string;
    status?: string;
    metadata?: Record<string, unknown>;
  };

  if (!event || !bookingId) {
    return badRequest(res, "event and bookingId are required");
  }

  // ✅ Do NOT lean(): we want fresh status checks (and possible re-fetch)
  let booking = await Booking.findById(bookingId);
  if (!booking) return notFound(res, "Booking not found");

  const bookingStatus = String((booking as any).status);

  try {
    switch (event) {
      case "payment.success":
      case "payment.completed": {
        // ✅ Terminal -> ignore (gateway shouldn't retry forever)
        if (TERMINAL_BOOKING_STATUSES.has(bookingStatus)) {
          return ok(res, {
            processed: true,
            ignored: true,
            reason: `booking is ${bookingStatus}`,
            event,
            bookingId,
          });
        }

        // ✅ Only accept success when PaymentPending
        if (bookingStatus !== "PaymentPending") {
          return ok(res, {
            processed: true,
            ignored: true,
            reason: `booking status=${bookingStatus}`,
            event,
            bookingId,
          });
        }

        // Capture wallet transfer (idempotent)
        await captureBookingPayment(bookingId);

        // Re-fetch booking (avoid stale)
        booking = await Booking.findById(bookingId);
        if (!booking) return notFound(res, "Booking not found");

        const needMentorConfirm =
          (process.env.MENTOR_CONFIRM_REQUIRED || "false").toLowerCase() === "true";

        // If already moved state by another webhook call, keep idempotent
        if (needMentorConfirm) {
          if (String((booking as any).status) !== "PendingMentor") {
            await markBookingPendingMentor(bookingId);
          }
        } else {
          if (String((booking as any).status) !== "Confirmed") {
            await confirmBooking(bookingId);
          }
        }

        break;
      }

      case "payment.failed":
      case "payment.expired":
      case "payment.cancelled": {
        // Idempotent: if already terminal, do nothing
        if (!TERMINAL_BOOKING_STATUSES.has(bookingStatus)) {
          await failBooking(bookingId);
        }
        break;
      }

      default:
        // ignore
        break;
    }
  } catch (err: any) {
    console.error(`Failed to process payment webhook for ${bookingId}:`, err?.message);

    const errorMessage =
      process.env.NODE_ENV === "development"
        ? `Failed to process webhook: ${err?.message || "Unknown error"}`
        : "Failed to process webhook";

    return badRequest(res, errorMessage);
  }

  return ok(res, { processed: true, event, bookingId });
});

export default {
  paymentWebhook,
};
