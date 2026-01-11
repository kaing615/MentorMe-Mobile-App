import mongoose from "mongoose";
import Booking from "../models/booking.model";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import { emitToUser } from "../socket";

/* -------------------- Retry helper -------------------- */

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
        await new Promise((r) => setTimeout(r, 50 * attempt));
        continue;
      }
      break;
    }
  }
  throw lastError;
}

/* -------------------- Wallet helper -------------------- */

async function getOrCreateWallet(
  userId: string,
  currency: "VND" | "USD",
  session?: mongoose.ClientSession
) {
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

/* -------------------- Constants -------------------- */

const TERMINAL_BOOKING_STATUSES = new Set([
  "Failed",
  "Cancelled",
  "Declined",
  "Completed",
]);

/* -------------------- Custom Error -------------------- */

export class InsufficientBalanceError extends Error {
  requiredTopup: number;
  currentBalance: number;

  constructor(requiredTopup: number, currentBalance: number) {
    super("INSUFFICIENT_BALANCE");
    this.requiredTopup = requiredTopup;
    this.currentBalance = currentBalance;
  }
}

/* =====================================================
   CAPTURE BOOKING PAYMENT
   - Trừ tiền trực tiếp từ ví mentee
   - Nếu không đủ -> throw InsufficientBalanceError
   ===================================================== */

export async function captureBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) throw new Error("BOOKING_NOT_FOUND");

      const status = String((booking as any).status);
      if (TERMINAL_BOOKING_STATUSES.has(status)) {
        const err: any = new Error(`BOOKING_TERMINAL:${status}`);
        err.code = "BOOKING_TERMINAL";
        throw err;
      }

      // Idempotent: chỉ capture khi PaymentPending
      if (status !== "PaymentPending") {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      const menteeId = String((booking as any).mentee);
      const mentorId = String((booking as any).mentor);

      const amountMinor =
        (booking as any).priceMinor != null
          ? Number((booking as any).priceMinor)
          : Math.round(Number((booking as any).price));

      if (!Number.isFinite(amountMinor) || amountMinor <= 0) {
        throw new Error("INVALID_AMOUNT");
      }

      const idempotencyKey = `booking_payment:${bookingId}`;

      // Idempotency check
      const existed = await WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        idempotencyKey,
      }).session(session);

      if (existed) {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      let currency: "VND" | "USD" =
        ((booking as any).currency as any) || "VND";

      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      if (mentorWallet.currency !== currency) {
        throw new Error("CURRENCY_MISMATCH");
      }

      /* --------- CORE LOGIC: CHECK BALANCE --------- */

      // TESTING: Skip balance check if SKIP_PAYMENT_BALANCE_CHECK=true
      const skipBalanceCheck = (process.env.SKIP_PAYMENT_BALANCE_CHECK || "false").toLowerCase() === "true";
      
      if (!skipBalanceCheck && menteeWallet.balanceMinor < amountMinor) {
        const requiredTopup = amountMinor - menteeWallet.balanceMinor;
        throw new InsufficientBalanceError(
          requiredTopup,
          menteeWallet.balanceMinor
        );
      }

      /* --------- DEBIT MENTEE --------- */

      const menteeBefore = menteeWallet.balanceMinor;
      menteeWallet.balanceMinor -= amountMinor;
      await menteeWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: menteeWallet._id,
            userId: menteeWallet.userId,
            type: "DEBIT",
            source: "BOOKING_PAYMENT",
            amountMinor,
            currency,
            balanceBeforeMinor: menteeBefore,
            balanceAfterMinor: menteeWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
            description: "Thanh toán booking",
          },
        ],
        { session }
      );

      /* --------- CREDIT MENTOR --------- */

      const mentorBefore = mentorWallet.balanceMinor;
      mentorWallet.balanceMinor += amountMinor;
      await mentorWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: mentorWallet._id,
            userId: mentorWallet.userId,
            type: "CREDIT",
            source: "BOOKING_EARN",
            amountMinor,
            currency,
            balanceBeforeMinor: mentorBefore,
            balanceAfterMinor: mentorWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
            description: "Thu nhập từ booking",
          },
        ],
        { session }
      );

      console.log(`✅ [captureBookingPayment] Created BOOKING_EARN transaction for mentor ${mentorId}, amount: ${amountMinor}, bookingId: ${bookingId}`);

      await session.commitTransaction();
      session.endSession();

      // Emit socket event to notify mentor about booking payment
      emitToUser(mentorId, "booking:changed", { bookingId });
      console.log(`✅ [captureBookingPayment] Emitted booking:changed to mentor ${mentorId}`);
    } catch (error: any) {
      await session.abortTransaction();
      session.endSession();

      if (
        error?.code === 11000 &&
        (error?.keyPattern?.idempotencyKey ||
          error?.message?.includes("idempotencyKey"))
      ) {
        return;
      }
      throw error;
    }
  });
}

/* =====================================================
   REFUND BOOKING PAYMENT
   - Mentee hủy booking sau khi đã thanh toán: hoàn 80%
   - Mentor giữ 20% như phí bồi thường cho slot bị chiếm
   - Mentor chỉ bị trừ 80% (trả lại cho mentee)
   ===================================================== */

export async function refundBookingPayment(bookingId: string): Promise<void> {
  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) throw new Error("BOOKING_NOT_FOUND");

      const menteeId = String((booking as any).mentee);
      const mentorId = String((booking as any).mentor);
      const idempotencyKey = `booking_refund:${bookingId}`;

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

      const originalPayment = await WalletTransaction.findOne({
        userId: new mongoose.Types.ObjectId(menteeId),
        source: "BOOKING_PAYMENT",
        referenceType: "BOOKING",
        referenceId: booking._id,
        type: "DEBIT",
      }).session(session);

      if (!originalPayment) {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      const amountMinor = Number(originalPayment.amountMinor);
      const currency = originalPayment.currency as "VND" | "USD";

      // Tính toán: Hoàn 80% cho mentee, Mentor giữ 20% phí slot
      const refundRate = 0.8; // 80%
      const refundAmount = Math.round(amountMinor * refundRate);
      const mentorFee = amountMinor - refundAmount; // 20% - phí slot bị chiếm

      const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
      const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

      // Mentor chỉ bị trừ 80% (hoàn lại cho mentee), giữ 20% phí slot
      if (mentorWallet.balanceMinor < refundAmount) {
        throw new Error("MENTOR_INSUFFICIENT_BALANCE");
      }

      // Debit mentor (80% - trả lại cho mentee)
      const mentorBefore = mentorWallet.balanceMinor;
      mentorWallet.balanceMinor -= refundAmount;
      await mentorWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: mentorWallet._id,
            userId: mentorWallet.userId,
            type: "DEBIT",
            source: "BOOKING_REFUND",
            amountMinor: refundAmount,
            currency,
            balanceBeforeMinor: mentorBefore,
            balanceAfterMinor: mentorWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
            description: `Hoàn tiền booking bị hủy (Giữ lại ${mentorFee} ${currency} phí slot)`,
          },
        ],
        { session }
      );

      // Refund mentee (80%)
      const menteeBefore = menteeWallet.balanceMinor;
      menteeWallet.balanceMinor += refundAmount;
      await menteeWallet.save({ session });

      await WalletTransaction.create(
        [
          {
            walletId: menteeWallet._id,
            userId: menteeWallet.userId,
            type: "REFUND",
            source: "BOOKING_REFUND",
            amountMinor: refundAmount,
            currency,
            balanceBeforeMinor: menteeBefore,
            balanceAfterMinor: menteeWallet.balanceMinor,
            referenceType: "BOOKING",
            referenceId: booking._id,
            idempotencyKey,
            description: `Hoàn tiền booking 80% (Phí hủy: ${mentorFee} ${currency})`,
          },
        ],
        { session }
      );

      console.log(`✅ [refundBookingPayment] Mentor keeps ${mentorFee} ${currency} (20% slot fee), debited ${refundAmount} ${currency} (80%) to refund mentee, bookingId: ${bookingId}`);

      await session.commitTransaction();
      session.endSession();
    } catch (error: any) {
      await session.abortTransaction();
      session.endSession();

      if (
        error?.code === 11000 &&
        (error?.keyPattern?.idempotencyKey ||
          error?.message?.includes("idempotencyKey"))
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
