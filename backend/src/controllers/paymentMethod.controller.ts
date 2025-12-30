import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import PaymentMethod from "../models/paymentMethod.model";
import { maskAccountNumber } from "../utils/maskAccount";
import mongoose from "mongoose";

const { ok, created, badRequest, notFound, forbidden } = responseHandler;

function getUserId(req: Request): string | null {
  return ((req as any).user?.id ?? (req as any).user?._id ?? null) as string | null;
}

/**
 * POST /payment-methods
 * Add payment method
 */
export const createPaymentMethod = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = getUserId(req);
    if (!userId) return forbidden(res, "Unauthorized");

    const {
      type,
      provider,
      accountName,
      accountNumber,
      isDefault = false,
    } = req.body ?? {};

    if (!type || !provider || !accountName || !accountNumber) {
      return badRequest(res, "MISSING_FIELDS");
    }

    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      const count = await PaymentMethod.countDocuments({ userId }).session(session);
      const finalIsDefault = count === 0 ? true : isDefault;

      if (finalIsDefault) {
        await PaymentMethod.updateMany(
          { userId },
          { $set: { isDefault: false } },
          { session }
        );
      }

      console.log("👉 createPaymentMethod START");
      const method = await PaymentMethod.create(
        [
          {
            userId,
            type,
            provider,
            accountName,
            accountNumber,
            isDefault: finalIsDefault,
          },
        ],
        { session }
      );
      console.log("👉 createPaymentMethod END");

      await session.commitTransaction();
      session.endSession();

      return created(res, {
        ...method[0].toJSON(),
        accountNumberMasked: maskAccountNumber(method[0].accountNumber),
        accountNumber: undefined,
      });

    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  }
);

/**
 * GET /payment-methods/me
 * List my payment methods
 */
export const listMyPaymentMethods = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = getUserId(req);
    if (!userId) return forbidden(res, "Unauthorized");

    const methods = await PaymentMethod.find({ userId }).sort({
      isDefault: -1,
      createdAt: -1,
    });

    return ok(
      res,
      methods.map((m: any) => ({
        ...m.toJSON(),
        accountNumberMasked: maskAccountNumber(m.accountNumber),
        accountNumber: undefined,
      }))
    );
  }
);

/**
 * PUT /payment-methods/:id
 * Update payment method
 */
export const updatePaymentMethod = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = getUserId(req);
    if (!userId) return forbidden(res, "Unauthorized");

    const id = req.params.id;
    if (!mongoose.Types.ObjectId.isValid(id)) {
      return badRequest(res, "INVALID_ID");
    }

    const method = await PaymentMethod.findOne({ _id: id, userId });
    if (!method) return notFound(res, "PAYMENT_METHOD_NOT_FOUND");

    const {
      provider,
      accountName,
      accountNumber,
      isDefault,
    } = req.body ?? {};

    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      if (isDefault === true) {
        await PaymentMethod.updateMany(
          { userId },
          { $set: { isDefault: false } },
          { session }
        );
        method.isDefault = true;
      }

      if (provider) method.provider = provider;
      if (accountName) method.accountName = accountName;
      if (accountNumber) method.accountNumber = accountNumber;

      await method.save({ session });

      await session.commitTransaction();
      session.endSession();

      return ok(res, {
        ...method.toJSON(),
        accountNumberMasked: maskAccountNumber(method.accountNumber),
        accountNumber: undefined,
      });
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      throw err;
    }
  }
);

/**
 * DELETE /payment-methods/:id
 * Delete payment method
 */
export const deletePaymentMethod = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = getUserId(req);
    if (!userId) return forbidden(res, "Unauthorized");

    const id = req.params.id;
    if (!mongoose.Types.ObjectId.isValid(id)) {
      return badRequest(res, "INVALID_ID");
    }

    const method = await PaymentMethod.findOne({ _id: id, userId });
    if (!method) return notFound(res, "PAYMENT_METHOD_NOT_FOUND");

    const count = await PaymentMethod.countDocuments({ userId });

    if (count <= 1) {
      return badRequest(res, "CANNOT_DELETE_LAST_PAYMENT_METHOD");
    }

    await method.deleteOne();

    // nếu xóa default → set cái khác làm default
    if (method.isDefault) {
      const another = await PaymentMethod.findOne({ userId });
      if (another) {
        another.isDefault = true;
        await another.save();
      }
    }

    return ok(res, { deleted: true });
  }
);

export default {
  createPaymentMethod,
  listMyPaymentMethods,
  updatePaymentMethod,
  deletePaymentMethod,
};
