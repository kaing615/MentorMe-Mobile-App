import { Request, Response } from "express";
import mongoose from "mongoose";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import MentorPayoutRequest, {
  IMentorPayoutRequest,
} from "../models/mentorPayoutRequest.model";

const { ok, created, badRequest, notFound, forbidden } = responseHandler;

function getUserId(req: Request): string | null {
  return ((req as any).user?.id ?? (req as any).user?._id ?? null) as
    | string
    | null;
}

function getUserRole(req: Request): string | null {
  return ((req as any).user?.role ?? null) as string | null;
}

// DTO + Mapper
interface MentorPayoutRequestDto {
  id: string;
  amount: number;
  currency: "VND" | "USD";
  status: string;
  attemptCount: number;
  externalId: string | null;
  createdAt: string;
  updatedAt: string;
}

function mapPayoutToDto(p: IMentorPayoutRequest): MentorPayoutRequestDto {
  const doc = p as any;
  return {
    id: doc.id ?? doc._id?.toString() ?? "",
    amount: p.amountMinor,
    currency: p.currency,
    status: p.status,
    attemptCount: p.attemptCount,
    externalId: p.externalId ?? null,
    createdAt: p.createdAt.toISOString(),
    updatedAt: p.updatedAt.toISOString(),
  };
}

/**
 * POST /payouts/requests
 * Mentor creates a payout request
 */
export const createPayoutRequest = asyncHandler(
  async (req: Request, res: Response) => {
    const mentorId = getUserId(req);
    if (!mentorId) return forbidden(res, "Unauthorized");

    const amount = Number(req.body?.amount ?? 0);
    const currency = (req.body?.currency ?? "VND") as "VND" | "USD";
    const clientRequestId = String(req.body?.clientRequestId ?? "").trim();

    if (!amount || amount <= 0 || !clientRequestId) {
      return badRequest(res, "amount and clientRequestId are required");
    }

    const amountMinor = amount;
    const idempotencyKey = clientRequestId;

    // Check wallet balance
    const wallet = await Wallet.findOne({ userId: mentorId });
    const currentBalance = wallet?.balanceMinor ?? 0;

    if (currentBalance < amountMinor) {
      return badRequest(res, "INSUFFICIENT_BALANCE");
    }

    // Check idempotency
    const existingPayout = await MentorPayoutRequest.findOne({
      mentorId,
      idempotencyKey,
    });

    if (existingPayout) {
      return ok(res, {
        idempotent: true,
        payout: mapPayoutToDto(existingPayout),
      });
    }

    // Create payout request (status = PENDING, wallet not debited yet)
    const payout = await MentorPayoutRequest.create({
      mentorId,
      amountMinor,
      currency,
      status: "PENDING",
      idempotencyKey,
      attemptCount: 0,
    });

    return created(res, {
      idempotent: false,
      payout: mapPayoutToDto(payout),
    });
  }
);

/**
 * GET /payouts/requests/me
 * Mentor list their own payout requests
 */
export const listMyPayoutRequests = asyncHandler(
  async (req: Request, res: Response) => {
    const mentorId = getUserId(req);
    if (!mentorId) return forbidden(res, "Unauthorized");

    const limit = Math.min(50, Math.max(1, Number(req.query.limit ?? 20)));
    const cursor = (req.query.cursor as string | undefined) ?? undefined;
    const status = (req.query.status as string | undefined) ?? undefined;

    const query: any = { mentorId };

    if (status) query.status = status;
    if (cursor) {
      query.createdAt = { $lt: new Date(cursor) };
    }

    const results = await MentorPayoutRequest.find(query)
      .sort({ createdAt: -1 })
      .limit(limit + 1);

    let nextCursor: string | null = null;
    let items = results;

    if (results.length > limit) {
      const lastVisible = results[limit - 1];
      nextCursor = lastVisible.createdAt.toISOString();
      items = results.slice(0, limit);
    }

    return ok(res, {
      items: items.map(mapPayoutToDto),
      nextCursor,
    });
  }
);

/**
 * GET /admin/payouts
 * Admin list all payout requests
 */
export const adminListPayoutRequests = asyncHandler(
  async (req: Request, res: Response) => {
    const limit = Math.min(50, Math.max(1, Number(req.query.limit ?? 20)));
    const cursor = (req.query.cursor as string | undefined) ?? undefined;
    const status = (req.query.status as string | undefined) ?? undefined;
    const mentorId = (req.query.mentorId as string | undefined) ?? undefined;

    const query: any = {};

    if (status) query.status = status;
    if (mentorId) query.mentorId = mentorId;
    if (cursor) {
      query.createdAt = { $lt: new Date(cursor) };
    }

    const results = await MentorPayoutRequest.find(query)
      .sort({ createdAt: -1 })
      .limit(limit + 1);

    let nextCursor: string | null = null;
    let items = results;

    if (results.length > limit) {
      const lastVisible = results[limit - 1];
      nextCursor = lastVisible.createdAt.toISOString();
      items = results.slice(0, limit);
    }

    return ok(res, {
      items: items.map(mapPayoutToDto),
      nextCursor,
    });
  }
);

/**
 * POST /admin/payouts/:id/approve
 * Admin approves a payout request and debits the mentor wallet
 */
export const adminApprovePayout = asyncHandler(
  async (req: Request, res: Response) => {
    const payoutId = req.params.id;

    if (!payoutId || !mongoose.Types.ObjectId.isValid(payoutId)) {
      return badRequest(res, "Invalid payout ID");
    }

    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      // Load payout
      const payout = await MentorPayoutRequest.findById(payoutId).session(
        session
      );

      if (!payout) {
        await session.abortTransaction();
        session.endSession();
        return notFound(res, "Payout request not found");
      }

      // Only allow approve if status is PENDING
      if (payout.status !== "PENDING") {
        await session.abortTransaction();
        session.endSession();
        return badRequest(
          res,
          `Cannot approve payout with status ${payout.status}`
        );
      }

      // Load mentor wallet
      const wallet = await Wallet.findOne({
        userId: payout.mentorId,
      }).session(session);

      if (!wallet) {
        await session.abortTransaction();
        session.endSession();
        return badRequest(res, "WALLET_NOT_AVAILABLE");
      }

      if (wallet.status !== "ACTIVE") {
        await session.abortTransaction();
        session.endSession();
        return badRequest(res, "WALLET_NOT_AVAILABLE");
      }

      // Check balance
      if (wallet.balanceMinor < payout.amountMinor) {
        await session.abortTransaction();
        session.endSession();
        return badRequest(res, "INSUFFICIENT_BALANCE");
      }

      // Debit wallet
      const balanceBefore = wallet.balanceMinor;
      const balanceAfter = balanceBefore - payout.amountMinor;

      wallet.balanceMinor = balanceAfter;
      await wallet.save({ session });

      // Build idempotency key per payout to avoid duplicate key on (userId, source, idempotencyKey)
      const payoutIdStr =
        (payout as any)._id?.toString?.() ??
        (payout as any).id?.toString?.() ??
        "";

      // Create wallet transaction
      await WalletTransaction.create(
        [
          {
            walletId: wallet._id,
            userId: payout.mentorId,
            type: "DEBIT",
            source: "MENTOR_PAYOUT",
            amountMinor: payout.amountMinor,
            currency: payout.currency,
            balanceBeforeMinor: balanceBefore,
            balanceAfterMinor: balanceAfter,
            referenceType: "PAYOUT",
            referenceId: payout._id,
            idempotencyKey: `payout:${payoutIdStr}`,
          },
        ],
        { session }
      );

      // Update payout status
      payout.status = "PROCESSING";
      payout.attemptCount += 1;

      if (!payout.externalId) {
        const doc = payout as any;
        payout.externalId = `PO-${doc._id?.toString() ?? ""}`;
      }

      await payout.save({ session });

      await session.commitTransaction();
      session.endSession();

      return ok(res, {
        payout: mapPayoutToDto(payout),
      });
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  }
);

/**
 * POST /admin/payouts/:id/retry
 * Admin retries a failed/processing payout
 */
export const adminRetryPayout = asyncHandler(
  async (req: Request, res: Response) => {
    const payoutId = req.params.id;

    if (!payoutId || !mongoose.Types.ObjectId.isValid(payoutId)) {
      return badRequest(res, "Invalid payout ID");
    }

    const payout = await MentorPayoutRequest.findById(payoutId);

    if (!payout) {
      return notFound(res, "Payout request not found");
    }

    // Only allow retry for FAILED or PROCESSING
    if (payout.status !== "FAILED" && payout.status !== "PROCESSING") {
      return badRequest(
        res,
        `Cannot retry payout with status ${payout.status}`
      );
    }

    payout.status = "PROCESSING";
    payout.attemptCount += 1;
    await payout.save();

    return ok(res, {
      payout: mapPayoutToDto(payout),
    });
  }
);

/**
 * POST /webhooks/payout-provider
 * Mock webhook from payout provider
 */
export const handleMockPayoutWebhook = asyncHandler(
  async (req: Request, res: Response) => {
    const externalId = String(req.body?.externalId ?? "").trim();
    const status = String(req.body?.status ?? "").trim();

    if (!externalId || !status) {
      return badRequest(res, "externalId and status are required");
    }

    // Find payout by externalId
    const payout = await MentorPayoutRequest.findOne({ externalId });

    if (!payout) {
      return notFound(res, "Payout not found");
    }

    // Idempotency: if already PAID or FAILED, return current state
    if (payout.status === "PAID" || payout.status === "FAILED") {
      return ok(res, {
        message: "Webhook already processed",
        payout: mapPayoutToDto(payout),
      });
    }

    // Only process webhook when payout is in PROCESSING state
    // This prevents refunding money for payouts that haven't been debited yet
    if (payout.status !== "PROCESSING") {
      return ok(res, {
        message: `Ignoring webhook status ${status} for payout in status ${payout.status}`,
        payout: mapPayoutToDto(payout),
      });
    }

    if (status === "PAID") {
      // Simply mark as PAID (wallet already debited at approval)
      payout.status = "PAID";
      await payout.save();

      return ok(res, {
        message: "Payout marked as PAID",
        payout: mapPayoutToDto(payout),
      });
    } else if (status === "FAILED") {
      // Refund to mentor wallet (IDEMPOTENT + SAFE)
      const session = await mongoose.startSession();
      session.startTransaction();

      try {
        const payoutIdStr =
          (payout as any)._id?.toString?.() ??
          (payout as any).id?.toString?.() ??
          "";

        const refundKey = `payout_refund:${payoutIdStr}`;

        // ✅ Idempotency gate FIRST (inside transaction)
        const existingRefundTx = await WalletTransaction.findOne({
          userId: payout.mentorId,
          source: "MENTOR_PAYOUT_REFUND",
          idempotencyKey: refundKey,
        }).session(session);

        if (existingRefundTx) {
          // Already refunded, only ensure payout marked FAILED
          payout.status = "FAILED";
          await payout.save({ session });

          await session.commitTransaction();
          session.endSession();

          return ok(res, {
            message: "Refund already processed (idempotent)",
            payout: mapPayoutToDto(payout),
          });
        }

        // Load wallet
        const wallet = await Wallet.findOne({
          userId: payout.mentorId,
        }).session(session);

        if (!wallet) {
          await session.abortTransaction();
          session.endSession();
          return badRequest(res, "WALLET_NOT_AVAILABLE");
        }

        // ✅ BONUS: wallet status check
        if (wallet.status !== "ACTIVE") {
          await session.abortTransaction();
          session.endSession();
          return badRequest(res, "WALLET_LOCKED");
        }

        // Credit back the amount
        const balanceBefore = wallet.balanceMinor;
        const balanceAfter = balanceBefore + payout.amountMinor;

        wallet.balanceMinor = balanceAfter;
        await wallet.save({ session });

        // Insert refund tx
        await WalletTransaction.create(
          [
            {
              walletId: wallet._id,
              userId: payout.mentorId,
              type: "REFUND",
              source: "MENTOR_PAYOUT_REFUND",
              amountMinor: payout.amountMinor,
              currency: payout.currency,
              balanceBeforeMinor: balanceBefore,
              balanceAfterMinor: balanceAfter,
              referenceType: "PAYOUT",
              referenceId: payout._id,
              idempotencyKey: refundKey,
            },
          ],
          { session }
        );

        // Mark payout as FAILED
        payout.status = "FAILED";
        await payout.save({ session });

        await session.commitTransaction();
        session.endSession();

        return ok(res, {
          message: "Payout marked as FAILED and amount refunded",
          payout: mapPayoutToDto(payout),
        });
      } catch (err: any) {
        await session.abortTransaction();
        session.endSession();

        // If duplicate key on idempotencyKey, treat as idempotent
        if (
          err?.code === 11000 &&
          (err.keyPattern?.idempotencyKey ||
            err.message?.includes("idempotencyKey"))
        ) {
          // best-effort mark FAILED (outside txn)
          await MentorPayoutRequest.updateOne(
            { _id: payout._id },
            { $set: { status: "FAILED" } }
          );
          const latest = await MentorPayoutRequest.findById(payout._id);

          return ok(res, {
            message: "Refund already processed (idempotent)",
            payout: latest ? mapPayoutToDto(latest as any) : mapPayoutToDto(payout),
          });
        }

        throw err;
      }
    } else {
      return badRequest(res, "Invalid status");
    }
  }
);

export default {
  createPayoutRequest,
  listMyPayoutRequests,
  adminListPayoutRequests,
  adminApprovePayout,
  adminRetryPayout,
  handleMockPayoutWebhook,
};
