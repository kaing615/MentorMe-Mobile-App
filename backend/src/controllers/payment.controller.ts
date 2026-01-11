import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";
import { badRequest, notFound, ok } from "../handlers/response.handler";
import Booking from "../models/booking.model";
import { captureBookingPayment } from "../services/walletBooking.service";
import {
    notifyPaymentFailed,
    notifyPaymentSuccess,
    PaymentNotificationData,
} from "../utils/notification.service";
import { getUserInfo } from "../utils/userInfo";
import {
    confirmBooking,
    failBooking,
    markBookingPendingMentor,
} from "./booking.controller";

const TERMINAL_BOOKING_STATUSES = new Set([
  "Failed",
  "Cancelled",
  "Declined",
  "Completed",
]);

const NON_TERMINAL_CAPTURE_ERRORS = new Set([
  "INSUFFICIENT_BALANCE",
]);

const TERMINAL_CAPTURE_ERRORS = new Set([
  "INVALID_AMOUNT",
  "CURRENCY_MISMATCH",
  "WALLET_LOCKED",
]);

function isBookingTerminalError(err: any): boolean {
  const message = String(err?.message ?? "");
  return err?.code === "BOOKING_TERMINAL" || message.startsWith("BOOKING_TERMINAL:");
}

async function buildPaymentNotificationData(
  booking: any,
  extra: { paymentId?: string; status?: string; event?: string }
): Promise<PaymentNotificationData> {
  const [mentorInfo, menteeInfo] = await Promise.all([
    getUserInfo(String(booking.mentor)),
    getUserInfo(String(booking.mentee)),
  ]);

  return {
    bookingId: String(booking._id),
    mentorId: String(booking.mentor),
    menteeId: String(booking.mentee),
    mentorName: mentorInfo.name,
    menteeName: menteeInfo.name,
    startTime: new Date(booking.startTime),
    amount: typeof booking.price === "number" ? booking.price : Number(booking.price),
    currency: (booking as any).currency,
    paymentId: extra.paymentId,
    status: extra.status,
    event: extra.event,
  };
}

/**
 * POST /api/payments/webhook
 * Handle payment gateway webhook events
 *
 * Rules:
 * - payment.success: only acts when booking is PaymentPending
 * - if booking is terminal or already moved state -> return 200 ignored (idempotent)
 */
export const paymentWebhook = asyncHandler(async (req: Request, res: Response) => {
  const { event, bookingId, paymentId, status } = req.body as {
    event: string;
    bookingId: string;
    paymentId?: string;
    status?: string;
    metadata?: Record<string, unknown>;
  };

  console.log(`[WEBHOOK] Received payment webhook: event=${event}, bookingId=${bookingId}, paymentId=${paymentId}, status=${status}`);

  if (!event || !bookingId) {
    return badRequest(res, "event and bookingId are required");
  }

  // ✅ Do NOT lean(): we want fresh status checks (and possible re-fetch)
  let booking = await Booking.findById(bookingId);
  if (!booking) return notFound(res, "Booking not found");
  
  console.log(`[WEBHOOK] Booking found: id=${bookingId}, status=${(booking as any).status}`);

  const bookingStatus = String((booking as any).status);
  const isSuccessEvent = event === "payment.success" || event === "payment.completed";

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
          console.log(`[WEBHOOK] Ignoring - booking not PaymentPending: status=${bookingStatus}`);
          return ok(res, {
            processed: true,
            ignored: true,
            reason: `booking status=${bookingStatus}`,
            event,
            bookingId,
          });
        }

        console.log(`[WEBHOOK] Capturing payment for booking ${bookingId}...`);
        // Capture wallet transfer (idempotent)
        await captureBookingPayment(bookingId);
        console.log(`[WEBHOOK] Payment captured successfully for booking ${bookingId}`);

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

        console.log(`[WEBHOOK] Sending payment success notification for booking ${bookingId}...`);
        try {
          const paymentNotificationData = await buildPaymentNotificationData(booking, {
            paymentId,
            status,
            event,
          });
          await notifyPaymentSuccess(paymentNotificationData);
          console.log(`[WEBHOOK] Payment success notification sent for booking ${bookingId}`);
        } catch (err) {
          console.error("[WEBHOOK] Failed to send payment success notification:", err);
        }

        break;
      }

      case "payment.failed":
      case "payment.expired":
      case "payment.cancelled": {
        // Wallet-based system:
        // Gateway failure does NOT decide booking fate
        return ok(res, {
          processed: true,
          ignored: true,
          reason: `gateway_${event}_ignored`,
          bookingId,
          event,
        });
      }

      default:
        // ignore
        break;
    }
  } catch (err: any) {
    const errorMessage = String(err?.message ?? "");
    console.error(`[WEBHOOK] Error during webhook processing for ${bookingId}:`, err);
    console.error(`[WEBHOOK] Error message: ${errorMessage}`);

    if (isBookingTerminalError(err)) {
      return ok(res, {
        processed: true,
        ignored: true,
        reason: errorMessage || "booking is terminal",
        event,
        bookingId,
      });
    }

    // ===== WALLET NOT ENOUGH MONEY (NON-TERMINAL) =====
if (isSuccessEvent && errorMessage === "INSUFFICIENT_BALANCE") {
  console.log(`[WEBHOOK] Insufficient balance for booking ${bookingId} - keeping PaymentPending`);
  
  // Notify user about insufficient balance
  try {
    const paymentNotificationData = await buildPaymentNotificationData(booking, {
      paymentId,
      status: 'insufficient_balance',
      event,
    });
    await notifyPaymentFailed(paymentNotificationData);
    console.log(`[WEBHOOK] Insufficient balance notification sent for booking ${bookingId}`);
  } catch (notifyErr) {
    console.error("[WEBHOOK] Failed to send insufficient balance notification:", notifyErr);
  }

  return ok(res, {
    processed: true,
    ignored: true,
    reason: "wallet_insufficient_balance",
    bookingId,
    event,
  });
}

  // ===== TERMINAL WALLET / PAYMENT ERRORS =====
  if (isSuccessEvent && TERMINAL_CAPTURE_ERRORS.has(errorMessage)) {
    console.log(`[WEBHOOK] Terminal error ${errorMessage} for booking ${bookingId} - failing booking`);
    if (bookingStatus === "PaymentPending") {
      try {
        await failBooking(bookingId);
      } catch (failErr: any) {
        if (!String(failErr?.message ?? "").includes("Cannot fail booking")) {
          console.error("Failed to mark booking failed:", failErr);
        }
      }
    }

    try {
      const paymentNotificationData = await buildPaymentNotificationData(booking, {
        paymentId,
        status,
        event,
      });
      await notifyPaymentFailed(paymentNotificationData);
    } catch (notifyErr) {
      console.error("Failed to send payment failed notification:", notifyErr);
    }

    return badRequest(res, errorMessage);
  }


    console.error(`Failed to process payment webhook for ${bookingId}:`, errorMessage);

    const responseMessage =
      process.env.NODE_ENV === "development"
        ? `Failed to process webhook: ${errorMessage || "Unknown error"}`
        : "Failed to process webhook";

    return badRequest(res, responseMessage);
  }

  return ok(res, { processed: true, event, bookingId });
});

export const payBookingByWallet = asyncHandler(async (req, res) => {
  const bookingId = req.params.id;
  const userId = (req as any).user.id;

  console.log(`💳 [payBookingByWallet] Called by user ${userId} for booking ${bookingId}`);

  const booking = await Booking.findById(bookingId);
  if (!booking) {
    console.log(`❌ [payBookingByWallet] Booking ${bookingId} not found`);
    return notFound(res, "Booking not found");
  }

  console.log(`📋 [payBookingByWallet] Booking status: ${booking.status}, mentee: ${booking.mentee}, mentor: ${booking.mentor}, price: ${booking.price}`);

  if (booking.status !== "PaymentPending") {
    console.log(`❌ [payBookingByWallet] Booking not payable - status is ${booking.status}`);
    return badRequest(res, "Booking is not payable");
  }

  console.log(`💰 [payBookingByWallet] Calling captureBookingPayment...`);
  await captureBookingPayment(bookingId);
  console.log(`✅ [payBookingByWallet] captureBookingPayment completed`);

  const updated = await Booking.findById(bookingId);
  if (!updated) return notFound(res, "Booking not found");

  const needMentorConfirm =
    (process.env.MENTOR_CONFIRM_REQUIRED || "false").toLowerCase() === "true";

  console.log(`🔄 [payBookingByWallet] needMentorConfirm=${needMentorConfirm}, updated status=${updated.status}`);

  if (needMentorConfirm) {
    if (String((updated as any).status) !== "PendingMentor") {
      console.log(`📤 [payBookingByWallet] Marking booking as PendingMentor`);
      await markBookingPendingMentor(bookingId);
    }
  } else {
    if (String((updated as any).status) !== "Confirmed") {
      console.log(`✅ [payBookingByWallet] Confirming booking`);
      await confirmBooking(bookingId);
    }
  }

  console.log(`✅ [payBookingByWallet] Payment completed for booking ${bookingId}`);

  return ok(res, {
    success: true,
    bookingId,
  });
});

export default {
  paymentWebhook,
};
