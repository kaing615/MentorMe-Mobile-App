import { Request, Response } from "express";
import TopUpIntent from "../models/topupIntent.model";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import User from "../models/user.model";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import mongoose from "mongoose";
import { sendPushToUser } from "../utils/push.service";
import Notification from "../models/notification.model";
import { maskAccountNumber } from "../utils/maskAccount";

const { ok, created, badRequest, forbidden, notFound } = responseHandler;

export type WalletTransactionReferenceType =
  | "BOOKING"
  | "PAYOUT"
  | "TOPUP"
  | null;

function createReferenceCode(): string {
  return `MM${Date.now().toString(36)}${Math.random()
    .toString(36)
    .slice(2, 6)}`.toUpperCase();
}

export const createTopUpIntent = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user?.id;
    if (!userId) return forbidden(res, "Unauthorized");

    const amount = Number(req.body?.amount ?? 0);
    if (!amount || amount <= 0) return badRequest(res, "Invalid amount");

    const referenceCode = createReferenceCode();
    const qrImageUrl =
      process.env.TOPUP_QR_IMAGE_URL ||
      "https://cdn.mentorme.vn/qr/company_account.png";

    const user = await User.findById(userId).lean();
    const username = user?.userName ?? user?.email ?? "user";

    const note = `Nap ${amount} - ${username}`;

    const intent = await TopUpIntent.create({
      user: userId,
      amount,
      currency: "VND",
      note,
      qrImageUrl,
      referenceCode,
      status: "PENDING",
    });

    return created(res, {
      id: intent._id,
      amount: intent.amount,
      currency: intent.currency,
      note: intent.note,
      qrImageUrl: intent.qrImageUrl,
      referenceCode: intent.referenceCode,
      status: intent.status,
    });
  }
);

export const confirmTopUpTransferred = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user?.id;
    if (!userId) return forbidden(res, "Unauthorized");

    const intentId = req.params.id;
    const intent = await TopUpIntent.findOne({ _id: intentId, user: userId });
    if (!intent) return notFound(res, "TopUp intent not found");

    if (intent.status == "SUBMITTED") {
      return ok(
        res,
        { id: intentId, status: intent.status, idempotent: true },
        "Already submitted or processed"
      );
    }

    intent.status = "SUBMITTED";
    await intent.save();

    // notify admins (optional) - create admin notification or use admin channel
    return ok(
      res,
      { id: intent._id, status: intent.status },
      "Marked as submitted"
    );
  }
);

export const getMyTopUpIntents = asyncHandler(async (req, res) => {
  const userId = (req as any).user?.id;
  if (!userId) return forbidden(res, "Unauthorized");

  const items = await TopUpIntent.find({ user: userId }).sort({
    createdAt: -1,
  });

  return ok(res, {
    items: items.map((it) => ({
      id: it.id,
      amount: it.amount,
      currency: it.currency,
      note: it.note,
      status: it.status,
      qrImageUrl: it.qrImageUrl,
      referenceCode: it.referenceCode,
      createdAt: it.createdAt,
    })),
  });
});

// Admin endpoints
export const listPendingTopUpsAdmin = asyncHandler(
  async (req: Request, res: Response) => {
    const role = (req as any).user?.role;
    if (!role || !["admin", "root"].includes(role))
      return forbidden(res, "Unauthorized");

    const items = await TopUpIntent.find({
      status: { $in: ["PENDING", "SUBMITTED"] },
    })
      .sort({ createdAt: -1 })
      .populate("user", "userName email");
    return ok(res, { items }, "OK");
  }
);

export const approveTopUpAdmin = asyncHandler(
  async (req: Request, res: Response) => {
    const role = (req as any).user?.role;
    if (!role || !["admin", "root"].includes(role))
      return forbidden(res, "Unauthorized");

    const id = req.params.id;
    const adminId = (req as any).user?.id;
    const session = await mongoose.startSession();
    session.startTransaction();
    try {
      const intent = await TopUpIntent.findById(id).session(session);
      if (!intent) {
        await session.abortTransaction();
        return notFound(res, "Not found");
      }
      if (intent.status === "APPROVED") {
        await session.commitTransaction();
        session.endSession();
        return ok(res, { id, status: intent.status }, "Already approved");
      }

      // get or create user's wallet
      let wallet = await Wallet.findOne({ userId: intent.user }).session(
        session
      );
      if (!wallet) {
        wallet = new Wallet({
          userId: intent.user,
          currency: intent.currency ?? "VND",
          balanceMinor: 0,
          status: "ACTIVE",
        });
        await wallet.save({ session });
      }
      if (wallet.status !== "ACTIVE") {
        await session.abortTransaction();
        session.endSession();
        return badRequest(res, "WALLET_LOCKED");
      }

      const balanceBefore = wallet.balanceMinor;
      const amountMinor = intent.amount;
      const balanceAfter = balanceBefore + amountMinor;
      wallet.balanceMinor = balanceAfter;
      await wallet.save({ session });

      // create wallet transaction (idempotency: reference to intent.id)
      const tx = await WalletTransaction.create(
        [
          {
            walletId: wallet._id,
            userId: intent.user,
            type: "CREDIT",
            source: "MANUAL_TOPUP",
            amountMinor,
            currency: intent.currency ?? "VND",
            balanceBeforeMinor: balanceBefore,
            balanceAfterMinor: balanceAfter,
            referenceType: "TOPUP",
            referenceId: intent._id,
            idempotencyKey: String(intent._id),
            metadata: { note: intent.note },
          },
        ],
        { session }
      );

      intent.status = "APPROVED";
      intent.reviewedBy = adminId;
      intent.reviewedAt = new Date();
      await intent.save({ session });

      await session.commitTransaction();
      session.endSession();

      // send push/notification to user
      try {
        await sendPushToUser(String(intent.user), {
          title: "Nạp tiền thành công",
          body: `Số tiền ${amountMinor} VND đã được cộng vào ví của bạn.`,
          data: {
            type: "TOPUP_SUCCESS",
            notificationId: String(intent._id),
            topupId: String(intent._id),
          },
        });

        // ✅ Create notification record
        await Notification.create({
          user: intent.user,
          type: "TOPUP_SUCCESS",
          title: "Nạp tiền thành công",
          body: `Số tiền ${amountMinor} VND đã được cộng vào ví của bạn.`,
          data: {
            topupId: String(intent._id),
            amount: amountMinor,
            currency: "VND",
          },
          read: false,
        });
      } catch (err) {
        console.error("push failed", err);
      }

      return ok(
        res,
        { id: intent._id, status: intent.status, tx: tx[0] },
        "Approved"
      );
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  }
);

export const rejectTopUpAdmin = asyncHandler(
  async (req: Request, res: Response) => {
    const role = (req as any).user?.role;
    if (!role || !["admin", "root"].includes(role))
      return forbidden(res, "Unauthorized");

    const id = req.params.id;
    const note = String(req.body?.reason ?? "").trim();

    const intent = await TopUpIntent.findById(id);
    if (!intent) return notFound(res, "Not found");
    if (intent.status === "APPROVED")
      return badRequest(res, "Already approved");

    intent.status = "REJECTED";
    intent.reviewedBy = (req as any).user?.id;
    intent.reviewedAt = new Date();
    await intent.save();

    // optional: notify user
    try {
      await sendPushToUser(String(intent.user), {
        title: "Nạp tiền bị từ chối",
        body: `Yêu cầu nạp ${intent.amount} VND đã bị từ chối${
          note ? `: ${note}` : ""
        }`,
        data: { topupId: String(intent._id) },
      });

      await Notification.create({
        user: intent.user,
        type: "TOPUP_REJECTED",
        title: "Nạp tiền bị từ chối",
        body: `Yêu cầu nạp ${intent.amount} VND đã bị từ chối${
          note ? `: ${note}` : ""
        }`,
        data: { topupId: String(intent._id) },
        read: false,
      });
    } catch (err) {
      console.error("Reject topup notify failed:", err);
    }
    return ok(res, { id: intent._id, status: intent.status }, "Rejected");
  }
);
