import mongoose from "mongoose";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import Booking from "../models/booking.model";

/**
 * Retry wrapper for transient transaction errors
 */
async function retryTransaction<T>(
  operation: () => Promise<T>,
  maxRetries = 3
): Promise<T> {
  let lastError: any;
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error: any) {
      lastError = error;
      const isTransient =
        error?.errorLabels?.includes("TransientTransactionError") ||
        error?.message?.includes("WriteConflict");
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
 * Helper to get or create a wallet for a user (atomic upsert, race-safe)
 */
async function getOrCreateWallet(
  userId: string,
  currency: "VND" | "USD",
  session?: mongoose.ClientSession
): Promise<any> {
  const userObjectId = new mongoose.Types.ObjectId(userId);

  const wallet = await Wallet.findOneAndUpdate(
    { userId: userObjectId },
    {
      $setOnInsert: {
        userId: userObjectId,
        balanceMinor: 0,
        currency,
        status: "ACTIVE",
      },
    },
    { new: true, upsert: true, session }
  );

  if (!wallet) throw new Error("WALLET_NOT_AVAILABLE");
  if (wallet.status !== "ACTIVE") throw new Error("WALLET_LOCKED");

  return wallet;
}

// Booking statuses that must NEVER accept payment capture
const TERMINAL_BOOKING_STATUSES = new Set([
  "Failed",
  "Cancelled",
  "Declined",
  "Completed",
]);

/**
 * Capture payment from mentee and credit mentor.
 * IMPORTANT:
 * - Should be called when booking is still in PaymentPending.
 * - Must be idempotent (duplicate webhook must not double charge).
 */
export async function captureBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      // Load booking (inside txn)
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) throw new Error("Booking not found");

      // ✅ Guard booking status (prevent capture after expiry/cancel/fail)
      const status = String((booking as any).status);
      if (TERMINAL_BOOKING_STATUSES.has(status)) {
        const err: any = new Error(`BOOKING_TERMINAL:${status}`);
        err.code = "BOOKING_TERMINAL";
        throw err;
      }

      // ✅ Only capture when PaymentPending.
      // If already moved forward (Confirmed/PendingMentor) => idempotent ignore.
      if (status !== "PaymentPending") {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      const menteeId = String((booking as any).mentee);
      const mentorId = String((booking as any).mentor);

      // Determine amount
      const amountMinor =
        (booking as any).priceMinor != null
          ? Number((booking as any).priceMinor)
          : Math.round(Number((booking as any).price));

      if (!Number.isFinite(amountMinor) || amountMinor <= 0) {
        throw new Error("INVALID_AMOUNT");
      }

      const idempotencyKey = `booking_payment:${bookingId}`;

      // Idempotency check: mentee debit tx exists => already captured
      const existingTx = await WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        idempotencyKey,
      }).session(session);

      if (existingTx) {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      // Determine currency
      let currency: "VND" | "USD" = ((booking as any).currency as any) || "VND";

      // Get/create wallets
      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      // If booking currency is missing, use mentee wallet currency
      if (!(booking as any).currency) currency = menteeWallet.currency;

      // Currency match
      if (mentorWallet.currency !== currency) throw new Error("CURRENCY_MISMATCH");

      // Balance check
      if (menteeWallet.balanceMinor < amountMinor) throw new Error("INSUFFICIENT_BALANCE");

      // Debit mentee
      const menteeBalanceBefore = menteeWallet.balanceMinor;
      menteeWallet.balanceMinor -= amountMinor;
      await menteeWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: menteeWallet._id,
            userId: new mongoose.Types.ObjectId(menteeId),
            type: "DEBIT",
            source: "BOOKING_PAYMENT",
            amountMinor,
            currency,
            balanceBeforeMinor: menteeBalanceBefore,
            balanceAfterMinor: menteeWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
          },
        ],
        { session }
      );

      // Credit mentor
      const mentorBalanceBefore = mentorWallet.balanceMinor;
      mentorWallet.balanceMinor += amountMinor;
      await mentorWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: mentorWallet._id,
            userId: new mongoose.Types.ObjectId(mentorId),
            type: "CREDIT",
            source: "BOOKING_PAYMENT",
            amountMinor,
            currency,
            balanceBeforeMinor: mentorBalanceBefore,
            balanceAfterMinor: mentorWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
          },
        ],
        { session }
      );

      await session.commitTransaction();
      session.endSession();
    } catch (error: any) {
      await session.abortTransaction();
      session.endSession();

      // Duplicate idempotencyKey unique index => treat as success
      if (
        error?.code === 11000 &&
        (error?.keyPattern?.idempotencyKey || error?.message?.includes("idempotencyKey"))
      ) {
        return;
      }
      throw error;
    }
  });
}

/**
 * Refund payment to mentee and debit mentor.
 * Called AFTER booking is cancelled/declined.
 * Must be idempotent.
 */
export async function refundBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) throw new Error("Booking not found");

      const menteeId = String((booking as any).mentee);
      const mentorId = String((booking as any).mentor);
      const idempotencyKey = `booking_refund:${bookingId}`;

      // Idempotency check: mentee refund tx exists => already refunded
      const existingRefund = await WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_REFUND",
        idempotencyKey,
      }).session(session);

      if (existingRefund) {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      // Verify original payment exists (mentee DEBIT)
      const originalPayment = await WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        referenceType: "BOOKING",
        referenceId: booking._id,
        type: "DEBIT",
      }).session(session);

      if (!originalPayment) {
        // No payment captured => nothing to refund
        await session.commitTransaction();
        session.endSession();
        return;
      }

      const amountMinor = Number(originalPayment.amountMinor);
      const currency = originalPayment.currency as "VND" | "USD";

      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      if (mentorWallet.currency !== currency) throw new Error("CURRENCY_MISMATCH");
      if (mentorWallet.balanceMinor < amountMinor) throw new Error("MENTOR_INSUFFICIENT_BALANCE");

      // Debit mentor
      const mentorBalanceBefore = mentorWallet.balanceMinor;
      mentorWallet.balanceMinor -= amountMinor;
      await mentorWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: mentorWallet._id,
            userId: new mongoose.Types.ObjectId(mentorId),
            type: "DEBIT",
            source: "BOOKING_REFUND",
            amountMinor,
            currency,
            balanceBeforeMinor: mentorBalanceBefore,
            balanceAfterMinor: mentorWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
          },
        ],
        { session }
      );

      // Refund mentee
      const menteeBalanceBefore = menteeWallet.balanceMinor;
      menteeWallet.balanceMinor += amountMinor;
      await menteeWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: menteeWallet._id,
            userId: new mongoose.Types.ObjectId(menteeId),
            type: "REFUND",
            source: "BOOKING_REFUND",
            amountMinor,
            currency,
            balanceBeforeMinor: menteeBalanceBefore,
            balanceAfterMinor: menteeWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
          },
        ],
        { session }
      );

      await session.commitTransaction();
      session.endSession();
    } catch (error: any) {
      await session.abortTransaction();
      session.endSession();

      if (
        error?.code === 11000 &&
        (error?.keyPattern?.idempotencyKey || error?.message?.includes("idempotencyKey"))
      ) {
        return;
      }
      throw error;
    }
  });
}

export default {
  captureBookingPayment,
  refundBookingPayment,
};
