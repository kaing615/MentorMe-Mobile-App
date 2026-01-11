import mongoose from "mongoose";
import Booking from "../models/booking.model";
import SessionLog from "../models/sessionLog.model";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import { notifyNoShowWithRefund } from "../utils/notification.service";
import { getUserInfo } from "../utils/userInfo";

/* -------------------- Constants -------------------- */

const PLATFORM_FEE_PERCENTAGE = 20; // 20% platform fee for NoShowBoth
const SESSION_GRACE_MINUTES = parseInt(process.env.SESSION_GRACE_MINUTES || "15", 10);

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

  return wallet;
}

/* =====================================================
   NO-SHOW DETECTION & HANDLING
   ===================================================== */

export type NoShowResult = {
  bookingId: string;
  status: "NoShowMentor" | "NoShowMentee" | "NoShowBoth";
  refundAmount?: number;
  platformFee?: number;
};

/**
 * Check if a session is no-show and determine the type
 * Returns null if session is not no-show (both parties joined)
 */
export async function detectNoShow(bookingId: string): Promise<NoShowResult | null> {
  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    throw new Error("BOOKING_NOT_FOUND");
  }

  // Only check Confirmed bookings
  if (booking.status !== "Confirmed") {
    return null;
  }

  const now = new Date();
  const graceDeadline = new Date(
    new Date(booking.startTime).getTime() + SESSION_GRACE_MINUTES * 60 * 1000
  );

  // Check if grace period has passed
  if (now < graceDeadline) {
    return null; // Too early to determine no-show
  }

  // Check session log for attendance
  const sessionLog = await SessionLog.findOne({ booking: booking._id }).lean();

  const mentorJoined = !!sessionLog?.mentorJoinAt;
  const menteeJoined = !!sessionLog?.menteeJoinAt;

  // Both joined - not a no-show
  if (mentorJoined && menteeJoined) {
    return null;
  }

  // Determine no-show type
  let status: "NoShowMentor" | "NoShowMentee" | "NoShowBoth";

  if (!mentorJoined && !menteeJoined) {
    status = "NoShowBoth";
  } else if (!mentorJoined) {
    status = "NoShowMentor";
  } else {
    status = "NoShowMentee";
  }

  return {
    bookingId: String(booking._id),
    status,
  };
}

/**
 * Process no-show: Update booking status and handle wallet transactions
 * 
 * TH1 (NoShowMentee): Mentor keeps 100% - No action needed (money already in mentor wallet)
 * TH2 (NoShowMentor): Mentee gets 100% refund - Deduct from mentor, refund to mentee
 * TH3 (NoShowBoth): Mentee gets 80% refund - Deduct 100% from mentor, refund 80% to mentee, platform keeps 20%
 */
export async function processNoShow(bookingId: string, sendNotifications = true): Promise<NoShowResult> {
  const noShowResult = await detectNoShow(bookingId);

  if (!noShowResult) {
    throw new Error("NOT_A_NO_SHOW");
  }

  const { status } = noShowResult;

  await retryTransaction(async () => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const booking = await Booking.findById(bookingId).session(session);
      if (!booking) throw new Error("BOOKING_NOT_FOUND");

      // Check if already processed
      if (["NoShowMentor", "NoShowMentee", "NoShowBoth"].includes(booking.status)) {
        await session.commitTransaction();
        session.endSession();
        return;
      }

      const menteeId = String(booking.mentee);
      const mentorId = String(booking.mentor);
      const bookingPrice = Number(booking.price);
      const currency = "VND"; // Default currency

      // TH1: Mentee no-show - Mentor keeps money (already credited)
      if (status === "NoShowMentee") {
        booking.status = "NoShowMentee";
        await booking.save({ session });

        await SessionLog.updateOne(
          { booking: booking._id },
          { 
            status: "no_show",
            endReason: "no_show_mentee",
            actualEnd: new Date(),
          }
        ).session(session);

        await session.commitTransaction();
        session.endSession();

        noShowResult.refundAmount = 0;
        
        // Send notifications after transaction commits
        if (sendNotifications) {
          try {
            const [mentorInfo, menteeInfo] = await Promise.all([
              getUserInfo(mentorId),
              getUserInfo(menteeId),
            ]);

            await notifyNoShowWithRefund({
              bookingId,
              mentorId,
              menteeId,
              mentorName: mentorInfo.name,
              menteeName: menteeInfo.name,
              startTime: new Date(booking.startTime),
              status: "NoShowMentee",
              refundAmount: 0,
            });
          } catch (notifError) {
            console.error("[NoShow] Error sending notifications:", notifError);
          }
        }
        
        return;
      }

      // TH2: Mentor no-show - Full refund to mentee (100%)
      if (status === "NoShowMentor") {
        const refundIdempotencyKey = `no_show_refund:${bookingId}`;
        const penaltyIdempotencyKey = `no_show_penalty:${bookingId}`;

        // Check if refund already processed
        const existingRefund = await WalletTransaction.findOne({
          userId: new mongoose.Types.ObjectId(menteeId),
          source: "NO_SHOW_REFUND",
          idempotencyKey: refundIdempotencyKey,
        }).session(session);

        if (existingRefund) {
          booking.status = "NoShowMentor";
          await booking.save({ session });
          await session.commitTransaction();
          session.endSession();
          return;
        }

        const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
        const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

        // Debit mentor wallet (penalty for no-show)
        const mentorBefore = mentorWallet.balanceMinor;
        if (mentorWallet.balanceMinor < bookingPrice) {
          throw new Error("MENTOR_INSUFFICIENT_BALANCE");
        }

        mentorWallet.balanceMinor -= bookingPrice;
        await mentorWallet.save({ session });

        await WalletTransaction.create(
          [
            {
              walletId: mentorWallet._id,
              userId: mentorWallet.userId,
              type: "DEBIT",
              source: "NO_SHOW_PENALTY",
              amountMinor: bookingPrice,
              currency,
              balanceBeforeMinor: mentorBefore,
              balanceAfterMinor: mentorWallet.balanceMinor,
              referenceType: "BOOKING",
              referenceId: booking._id,
              idempotencyKey: penaltyIdempotencyKey,
              description: "Phạt do mentor vắng mặt (no-show)",
            },
          ],
          { session }
        );

        // Credit mentee wallet (100% refund)
        const menteeBefore = menteeWallet.balanceMinor;
        menteeWallet.balanceMinor += bookingPrice;
        await menteeWallet.save({ session });

        await WalletTransaction.create(
          [
            {
              walletId: menteeWallet._id,
              userId: menteeWallet.userId,
              type: "REFUND",
              source: "NO_SHOW_REFUND",
              amountMinor: bookingPrice,
              currency,
              balanceBeforeMinor: menteeBefore,
              balanceAfterMinor: menteeWallet.balanceMinor,
              referenceType: "BOOKING",
              referenceId: booking._id,
              idempotencyKey: refundIdempotencyKey,
              description: "Hoàn tiền do mentor vắng mặt",
            },
          ],
          { session }
        );

        booking.status = "NoShowMentor";
        await booking.save({ session });

        await SessionLog.updateOne(
          { booking: booking._id },
          { 
            status: "no_show",
            endReason: "no_show_mentor",
            actualEnd: new Date(),
          }
        ).session(session);

        await session.commitTransaction();
        session.endSession();

        noShowResult.refundAmount = bookingPrice;
        
        // Send notifications after transaction commits
        if (sendNotifications) {
          try {
            const [mentorInfo, menteeInfo] = await Promise.all([
              getUserInfo(mentorId),
              getUserInfo(menteeId),
            ]);

            await notifyNoShowWithRefund({
              bookingId,
              mentorId,
              menteeId,
              mentorName: mentorInfo.name,
              menteeName: menteeInfo.name,
              startTime: new Date(booking.startTime),
              status: "NoShowMentor",
              refundAmount: bookingPrice,
            });
          } catch (notifError) {
            console.error("[NoShow] Error sending notifications:", notifError);
          }
        }
        
        return;
      }

      // TH3: Both no-show - Partial refund to mentee (80%)
      if (status === "NoShowBoth") {
        const refundIdempotencyKey = `no_show_refund:${bookingId}`;
        const penaltyIdempotencyKey = `no_show_penalty:${bookingId}`;

        // Check if refund already processed
        const existingRefund = await WalletTransaction.findOne({
          userId: new mongoose.Types.ObjectId(menteeId),
          source: "NO_SHOW_REFUND",
          idempotencyKey: refundIdempotencyKey,
        }).session(session);

        if (existingRefund) {
          booking.status = "NoShowBoth";
          await booking.save({ session });
          await session.commitTransaction();
          session.endSession();
          return;
        }

        const refundPercentage = 100 - PLATFORM_FEE_PERCENTAGE;
        const refundAmount = Math.floor((bookingPrice * refundPercentage) / 100);
        const platformFee = bookingPrice - refundAmount;

        const menteeWallet = await getOrCreateWallet(menteeId, currency, session);
        const mentorWallet = await getOrCreateWallet(mentorId, currency, session);

        // Debit mentor wallet (100% penalty)
        const mentorBefore = mentorWallet.balanceMinor;
        if (mentorWallet.balanceMinor < bookingPrice) {
          throw new Error("MENTOR_INSUFFICIENT_BALANCE");
        }

        mentorWallet.balanceMinor -= bookingPrice;
        await mentorWallet.save({ session });

        await WalletTransaction.create(
          [
            {
              walletId: mentorWallet._id,
              userId: mentorWallet.userId,
              type: "DEBIT",
              source: "NO_SHOW_PENALTY",
              amountMinor: bookingPrice,
              currency,
              balanceBeforeMinor: mentorBefore,
              balanceAfterMinor: mentorWallet.balanceMinor,
              referenceType: "BOOKING",
              referenceId: booking._id,
              idempotencyKey: penaltyIdempotencyKey,
              description: "Phạt do cả hai vắng mặt (no-show)",
            },
          ],
          { session }
        );

        // Credit mentee wallet (80% refund)
        const menteeBefore = menteeWallet.balanceMinor;
        menteeWallet.balanceMinor += refundAmount;
        await menteeWallet.save({ session });

        await WalletTransaction.create(
          [
            {
              walletId: menteeWallet._id,
              userId: menteeWallet.userId,
              type: "REFUND",
              source: "NO_SHOW_REFUND",
              amountMinor: refundAmount,
              currency,
              balanceBeforeMinor: menteeBefore,
              balanceAfterMinor: menteeWallet.balanceMinor,
              referenceType: "BOOKING",
              referenceId: booking._id,
              idempotencyKey: refundIdempotencyKey,
              description: `Hoàn ${refundPercentage}% do cả hai vắng mặt (trừ ${PLATFORM_FEE_PERCENTAGE}% phí)`,
            },
          ],
          { session }
        );

        // Platform fee is implicitly kept (not recorded as a transaction)

        booking.status = "NoShowBoth";
        await booking.save({ session });

        await SessionLog.updateOne(
          { booking: booking._id },
          { 
            status: "no_show",
            endReason: "no_show_both",
            actualEnd: new Date(),
          }
        ).session(session);

        await session.commitTransaction();
        session.endSession();

        noShowResult.refundAmount = refundAmount;
        noShowResult.platformFee = platformFee;
        
        // Send notifications after transaction commits
        if (sendNotifications) {
          try {
            const [mentorInfo, menteeInfo] = await Promise.all([
              getUserInfo(mentorId),
              getUserInfo(menteeId),
            ]);

            await notifyNoShowWithRefund({
              bookingId,
              mentorId,
              menteeId,
              mentorName: mentorInfo.name,
              menteeName: menteeInfo.name,
              startTime: new Date(booking.startTime),
              status: "NoShowBoth",
              refundAmount,
              platformFee,
            });
          } catch (notifError) {
            console.error("[NoShow] Error sending notifications:", notifError);
          }
        }
        
        return;
      }

      await session.commitTransaction();
      session.endSession();
    } catch (error: any) {
      await session.abortTransaction();
      session.endSession();

      // Handle idempotency key duplicate
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

  return noShowResult;
}

/**
 * Check all confirmed bookings that should have started and detect no-shows
 * This is meant to be called by a cron job
 */
export async function checkAllNoShows(): Promise<NoShowResult[]> {
  const now = new Date();
  const graceDeadline = new Date(now.getTime() - SESSION_GRACE_MINUTES * 60 * 1000);

  // Find all Confirmed bookings that should have started + grace period
  const bookings = await Booking.find({
    status: "Confirmed",
    startTime: { $lte: graceDeadline },
  })
    .select("_id")
    .lean();

  const results: NoShowResult[] = [];

  for (const booking of bookings) {
    try {
      const result = await processNoShow(String(booking._id));
      results.push(result);
    } catch (error: any) {
      console.error(`[NoShow] Error processing booking ${booking._id}:`, error.message);
      // Continue with next booking
    }
  }

  return results;
}

export default {
  detectNoShow,
  processNoShow,
  checkAllNoShows,
};
