import { Request, Response } from "express";
import mongoose from "mongoose";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Wallet from "../models/wallet.model";
import WalletTransaction from "../models/walletTransaction.model";
import {
  mapWalletToDto,
  mapWalletTransactionToDto,
} from "../utils/mappers/wallet.mapper";
import { maskAccountNumber } from "../utils/maskAccount";
import PaymentMethod from "../models/paymentMethod.model";

const { ok, created, badRequest, forbidden } = responseHandler;

function getUserId(req: Request): string | null {
  return ((req as any).user?.id ?? (req as any).user?._id ?? null) as string | null;
}

// GET /wallet/me
export const getMyWallet = asyncHandler(async (req: Request, res: Response) => {
  const userId = getUserId(req);
  if (!userId) return forbidden(res, "Unauthorized");

  const wallet = await Wallet.findOne({ userId });

  return ok(res, mapWalletToDto(wallet));
});

// helper: get or create wallet within a session
async function getOrCreateWallet(
  userId: string,
  currency: "VND" | "USD",
  session: mongoose.ClientSession
) {
  let wallet = await Wallet.findOne({ userId }).session(session);

  if (!wallet) {
    wallet = new Wallet({
      userId,
      currency,
      balanceMinor: 0,
      status: "ACTIVE",
    });
    await wallet.save({ session });
  }

  if (wallet.status !== "ACTIVE") {
    throw new Error("WALLET_LOCKED");
  }

  return wallet;
}

// POST /wallet/topups/mock
export const mockTopup = asyncHandler(async (req: Request, res: Response) => {
  const userId = getUserId(req);
  if (!userId) return forbidden(res, "Unauthorized");

  const amount = Number(req.body?.amount ?? 0);
  const currency = (req.body?.currency ?? "VND") as "VND" | "USD";
  const clientRequestId = String(req.body?.clientRequestId ?? "").trim();

  if (!amount || amount <= 0 || !clientRequestId) {
    return badRequest(res, "amount and clientRequestId are required");
  }

  const idempotencyKey = clientRequestId;

  // Check idempotent
  const existingTx = await WalletTransaction.findOne({
    userId,
    source: "MANUAL_TOPUP",
    idempotencyKey,
  });

  if (existingTx) {
    const wallet = await Wallet.findById(existingTx.walletId);
    return ok(res, {
      idempotent: true,
      wallet: mapWalletToDto(wallet),
      transaction: mapWalletTransactionToDto(existingTx),
    });
  }

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const wallet = await getOrCreateWallet(userId, currency, session);

    const balanceBefore = wallet.balanceMinor;
    const amountMinor = amount; // for VND: 1 unit = 1 VND
    const balanceAfter = balanceBefore + amountMinor;

    wallet.balanceMinor = balanceAfter;
    await wallet.save({ session });

    const [tx] = await WalletTransaction.create(
    [
      {
        walletId: wallet._id,
        userId,
        type: "CREDIT",
        source: "MANUAL_TOPUP",
        amountMinor,
        currency,
        balanceBeforeMinor: balanceBefore,
        balanceAfterMinor: balanceAfter,
        idempotencyKey,
      },
    ],
    { session }
  );

  await session.commitTransaction();
  session.endSession();

  return created(res, {
    idempotent: false,
    wallet: mapWalletToDto(wallet),
    transaction: mapWalletTransactionToDto(tx),
  });

  } catch (err: any) {
    await session.abortTransaction();
    session.endSession();

    if (err?.message === "WALLET_LOCKED") {
      return badRequest(res, "Wallet is locked");
    }

    // handle unique constraint on (userId, source, idempotencyKey)
    if (err?.code === 11000) {
      const tx = await WalletTransaction.findOne({
        userId,
        source: "MANUAL_TOPUP",
        idempotencyKey,
      });
      const wallet = tx ? await Wallet.findById(tx.walletId) : null;

      return ok(res, {
        idempotent: true,
        wallet: mapWalletToDto(wallet),
        transaction: tx ? mapWalletTransactionToDto(tx) : null,
      });
    }

    throw err;
  }
});

// POST /wallet/debits/mock
export const mockDebit = asyncHandler(async (req: Request, res: Response) => {
  const userId = getUserId(req);
  if (!userId) return forbidden(res, "Unauthorized");

  const amount = Number(req.body?.amount ?? 0);
  const currency = (req.body?.currency ?? "VND") as "VND" | "USD";
  const clientRequestId = String(req.body?.clientRequestId ?? "").trim();

  if (!amount || amount <= 0 || !clientRequestId) {
    return badRequest(res, "amount and clientRequestId are required");
  }

  const idempotencyKey = clientRequestId;

  const existingTx = await WalletTransaction.findOne({
    userId,
    source: "MANUAL_WITHDRAW",
    idempotencyKey,
  });

  if (existingTx) {
    const wallet = await Wallet.findById(existingTx.walletId);
    return ok(res, {
      idempotent: true,
      wallet: mapWalletToDto(wallet),
      transaction: mapWalletTransactionToDto(existingTx),
    });
  }

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const wallet = await getOrCreateWallet(userId, currency, session);

    const balanceBefore = wallet.balanceMinor;
    const amountMinor = amount;

    if (balanceBefore < amountMinor) {
      await session.abortTransaction();
      session.endSession();
      return badRequest(res, "INSUFFICIENT_BALANCE");
    }

    const balanceAfter = balanceBefore - amountMinor;

    wallet.balanceMinor = balanceAfter;
    await wallet.save({ session });

    const [tx] = await WalletTransaction.create(
      [
        {
          walletId: wallet._id,
          userId,
          type: "DEBIT",
          source: "MANUAL_WITHDRAW",
          amountMinor,
          currency,
          balanceBeforeMinor: balanceBefore,
          balanceAfterMinor: balanceAfter,
          idempotencyKey,
        },
      ],
      { session }
    );

    await session.commitTransaction();
    session.endSession();

    return created(res, {
      idempotent: false,
      wallet: mapWalletToDto(wallet),
      transaction: mapWalletTransactionToDto(tx),
    });
  } catch (err: any) {
    await session.abortTransaction();
    session.endSession();

    if (err?.message === "WALLET_LOCKED") {
      return badRequest(res, "Wallet is locked");
    }

    if (err?.code === 11000) {
      const tx = await WalletTransaction.findOne({
        userId,
        source: "MANUAL_WITHDRAW",
        idempotencyKey,
      });
      const wallet = tx ? await Wallet.findById(tx.walletId) : null;

      return ok(res, {
        idempotent: true,
        wallet: mapWalletToDto(wallet),
        transaction: tx ? mapWalletTransactionToDto(tx) : null,
      });
    }

    throw err;
  }
});

// GET /wallet/transactions
export const listTransactions = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = getUserId(req);
    if (!userId) return forbidden(res, "Unauthorized");

    const limit = Math.min(
      50,
      Math.max(1, Number(req.query.limit ?? 20))
    );
    const cursor = (req.query.cursor as string | undefined) ?? undefined;
    const type = (req.query.type as string | undefined) ?? undefined;
    const source = (req.query.source as string | undefined) ?? undefined;

    const query: any = { userId };

    if (type) query.type = type;
    if (source) query.source = source;
    if (cursor) {
      query.createdAt = { $lt: new Date(cursor) };
    }

    const results = await WalletTransaction.find(query)
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
      items: items.map(mapWalletTransactionToDto),
      nextCursor,
    });
  }
);

// POST /wallet/debits
export const withdraw = asyncHandler(async (req: Request, res: Response) => {
  const userId = getUserId(req);
  if (!userId) return forbidden(res, "Unauthorized");

  const amount = Number(req.body?.amount ?? 0);
  const paymentMethodId = req.body?.paymentMethodId
    ? String(req.body.paymentMethodId).trim()
    : null;

  const paymentMethod = paymentMethodId
    ? await PaymentMethod.findOne({ _id: paymentMethodId, userId })
    : await PaymentMethod.findOne({ userId, isDefault: true });

  if (!paymentMethod) {
    return badRequest(res, "PAYMENT_METHOD_REQUIRED");
  }

  const MIN_WITHDRAW_AMOUNT = 50000;
  if (amount < MIN_WITHDRAW_AMOUNT) {
    return badRequest(res, "MIN_WITHDRAW_50K");
  }

  const currency = (req.body?.currency ?? "VND") as "VND" | "USD";
  const clientRequestId = String(req.body?.clientRequestId ?? "").trim();
  const payoutInfo = req.body?.payoutInfo ?? null; // optional payout details (bank account, momo id, ...)

  if (!amount || amount <= 0 || !clientRequestId) {
    return badRequest(res, "amount and clientRequestId are required");
  }

  const idempotencyKey = clientRequestId;

  // If there's an existing identical withdraw tx (idempotency), return it
  const existingTx = await WalletTransaction.findOne({
    userId,
    source: "MANUAL_WITHDRAW",
    idempotencyKey,
  });

  if (existingTx) {
    const wallet = await Wallet.findById(existingTx.walletId);
    return ok(res, {
      idempotent: true,
      wallet: mapWalletToDto(wallet),
      transaction: mapWalletTransactionToDto(existingTx),
    });
  }

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const wallet = await getOrCreateWallet(userId, currency, session);

    const balanceBefore = wallet.balanceMinor;
    const amountMinor = amount;

    if (balanceBefore < amountMinor) {
      await session.abortTransaction();
      session.endSession();
      return badRequest(res, "INSUFFICIENT_BALANCE");
    }

    // Subtract immediately (optimistic) to avoid double-spend
    const balanceAfter = balanceBefore - amountMinor;
    wallet.balanceMinor = balanceAfter;
    await wallet.save({ session });

    const [tx] = await WalletTransaction.create(
      [
        {
          walletId: wallet._id,
          userId,
          type: "DEBIT",
          source: "MANUAL_WITHDRAW",
          amountMinor,
          currency,
          balanceBeforeMinor: balanceBefore,
          balanceAfterMinor: balanceAfter,
          idempotencyKey,
          metadata: {
            payoutInfo: {
              paymentMethodId: paymentMethod._id,
              type: paymentMethod.type,
              provider: paymentMethod.provider,
              accountMasked: maskAccountNumber(paymentMethod.accountNumber),
            },
            providerStatus: "PENDING",
          },
        },
      ],
      { session }
    );

    // Here: trigger actual payout to provider (async). Do NOT block UI on slow provider.
    // Example: push to queue / call payout service
    // await payoutService.enqueuePayout({ txId: tx._id, amount: amountMinor, payoutInfo });

    await session.commitTransaction();
    session.endSession();

    return created(res, {
      idempotent: false,
      wallet: mapWalletToDto(wallet),
      transaction: mapWalletTransactionToDto(tx),
    });
  } catch (err: any) {
    await session.abortTransaction();
    session.endSession();

    if (err?.message === "WALLET_LOCKED") {
      return badRequest(res, "Wallet is locked");
    }

    if (err?.code === 11000) {
      // idempotency race
      const tx = await WalletTransaction.findOne({
        userId,
        source: "MANUAL_WITHDRAW",
        idempotencyKey,
      });
      const wallet = tx ? await Wallet.findById(tx.walletId) : null;

      return ok(res, {
        idempotent: true,
        wallet: mapWalletToDto(wallet),
        transaction: tx ? mapWalletTransactionToDto(tx) : null,
      });
    }

    throw err;
  }
});

export default {
  getMyWallet,
  mockTopup,
  mockDebit,
  listTransactions,
  withdraw,
};
