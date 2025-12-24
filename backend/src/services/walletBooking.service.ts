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
        error.errorLabels?.includes("TransientTransactionError") ||
        error.message?.includes("WriteConflict");
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

  if (!wallet) {
    throw new Error("WALLET_NOT_AVAILABLE");
  }

  if (wallet.status !== "ACTIVE") {
    throw new Error("WALLET_LOCKED");
  }

  return wallet;
}


/**
 * Capture payment from mentee and credit mentor when booking is confirmed
 * Should be called BEFORE updating booking status to Confirmed/PendingMentor
 */
export async function captureBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      // Load booking
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) {
        throw new Error("Booking not found");
      }

      const menteeId = String(booking.mentee);
      const mentorId = String(booking.mentor);

      // Determine amountMinor safely
      const amountMinor = (booking as any).priceMinor
        ? (booking as any).priceMinor
        : Math.round(booking.price);
      if (amountMinor <= 0) {
        throw new Error("INVALID_AMOUNT");
      }

      const idempotencyKey = `booking_payment:${bookingId}`;

      // Check idempotency - if transaction already exists, return success
      let existingTxQuery = WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        idempotencyKey,
      });
      if (session) existingTxQuery = existingTxQuery.session(session);
      const existingTx = await existingTxQuery;

      if (existingTx) {
        // Already captured
        await session.commitTransaction();
        session.endSession();
        return;
      }

      // Determine currency from booking if available, else from mentee wallet
      let currency: "VND" | "USD" = (booking as any).currency || "VND";

      // Get or create wallets
      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      // Use menteeWallet currency if booking doesn't have one
      if (!(booking as any).currency) {
        currency = menteeWallet.currency;
      }

      // Validate currencies match
      if (mentorWallet.currency !== currency) {
        throw new Error("CURRENCY_MISMATCH");
      }

      // Check mentee has sufficient balance
      if (menteeWallet.balanceMinor < amountMinor) {
        throw new Error("INSUFFICIENT_BALANCE");
      }

      // Debit mentee
      const menteeBalanceBefore = menteeWallet.balanceMinor;
      menteeWallet.balanceMinor -= amountMinor;
      await menteeWallet.save({ session });

      // Create mentee debit transaction
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

      // Create mentor credit transaction
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
      // If duplicate key error on idempotencyKey unique index, treat as success
      if (
        error.code === 11000 &&
        (error.keyPattern?.idempotencyKey ||
          error.message?.includes("idempotencyKey"))
      ) {
        return;
      }
      throw error;
    }
  });
}

/**
 * Refund payment to mentee and debit mentor when booking is cancelled/declined
 * Should be called AFTER updating booking status to Cancelled/Declined
 */
export async function refundBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      // Load booking
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) {
        throw new Error("Booking not found");
      }

      const menteeId = String(booking.mentee);
      const mentorId = String(booking.mentor);
      const idempotencyKey = `booking_refund:${bookingId}`;

      // Check idempotency - if refund transaction already exists, return success
      let existingRefundQuery = WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_REFUND",
        idempotencyKey,
      });
      if (session) existingRefundQuery = existingRefundQuery.session(session);
      const existingRefund = await existingRefundQuery;

      if (existingRefund) {
        // Already refunded
        await session.commitTransaction();
        session.endSession();
        return;
      }

      // Verify original payment exists
      let originalPaymentQuery = WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        referenceType: "BOOKING",
        referenceId: booking._id,
        type: "DEBIT",
      });
      if (session) originalPaymentQuery = originalPaymentQuery.session(session);
      const originalPayment = await originalPaymentQuery;

      if (!originalPayment) {
        // No payment to refund - this is fine for bookings that failed before payment capture
        await session.commitTransaction();
        session.endSession();
        return;
      }

      // Use original payment amount and currency
      const amountMinor = originalPayment.amountMinor;
      const currency = originalPayment.currency;

      // Get wallets
      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      // Validate currencies match
      if (mentorWallet.currency !== currency) {
        throw new Error("CURRENCY_MISMATCH");
      }

      // Check mentor has sufficient balance
      if (mentorWallet.balanceMinor < amountMinor) {
        throw new Error("MENTOR_INSUFFICIENT_BALANCE");
      }

      // Debit mentor
      const mentorBalanceBefore = mentorWallet.balanceMinor;
      mentorWallet.balanceMinor -= amountMinor;
      await mentorWallet.save({ session });

      // Create mentor debit transaction
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

      // Create mentee refund transaction
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
      // If duplicate key error on idempotencyKey unique index, treat as success
      if (
        error.code === 11000 &&
        (error.keyPattern?.idempotencyKey ||
          error.message?.includes("idempotencyKey"))
      ) {
        return;
      }
      throw error;
    }
  });
}
