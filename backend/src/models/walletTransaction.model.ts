import { Schema, model, Types, Document } from "mongoose";
import { WalletCurrency } from "./wallet.model";

export type WalletTransactionType = "CREDIT" | "DEBIT" | "REFUND";
export type WalletTransactionSource =
  | "BOOKING_EARN"
  | "MANUAL_TOPUP"
  | "MANUAL_WITHDRAW"
  | "BOOKING_PAYMENT"
  | "BOOKING_REFUND"
  | "MENTOR_PAYOUT"
  | "MENTOR_PAYOUT_REFUND";

export type WalletTransactionReferenceType = "BOOKING" | "PAYOUT" | null;

export interface IWalletTransaction extends Document {
  walletId: Types.ObjectId;
  userId: Types.ObjectId;
  type: WalletTransactionType;
  source: WalletTransactionSource;
  amountMinor: number;
  currency: WalletCurrency;
  balanceBeforeMinor: number;
  balanceAfterMinor: number;
  referenceType?: WalletTransactionReferenceType;
  referenceId?: Types.ObjectId | null;
  idempotencyKey?: string | null;
  metadata?: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
}

const WalletTransactionSchema = new Schema<IWalletTransaction>(
  {
    walletId: {
      type: Schema.Types.ObjectId,
      ref: "Wallet",
      required: true,
      index: true,
    },
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    type: {
      type: String,
      enum: ["CREDIT", "DEBIT", "REFUND"],
      required: true,
    },
    source: {
      type: String,
      enum: [
        "MANUAL_TOPUP",
        "MANUAL_WITHDRAW",
        "BOOKING_PAYMENT",
        "BOOKING_EARN",
        "BOOKING_REFUND",
        "MENTOR_PAYOUT",
        "MENTOR_PAYOUT_REFUND",
      ],
      required: true,
    },
    amountMinor: {
      type: Number,
      required: true,
      min: 1,
    },
    currency: {
      type: String,
      enum: ["VND", "USD"],
      required: true,
      default: "VND",
    },
    balanceBeforeMinor: {
      type: Number,
      required: true,
      min: 0,
    },
    balanceAfterMinor: {
      type: Number,
      required: true,
      min: 0,
    },
    referenceType: {
      type: String,
      enum: ["BOOKING", "PAYOUT", null],
      default: null,
      index: true,
    },
    referenceId: {
      type: Schema.Types.ObjectId,
      default: null,
      index: true,
    },
    idempotencyKey: {
      type: String,
      default: undefined, //fix duplicate key error for docs without this field
      index: true,
    },
    metadata: {
      type: Schema.Types.Mixed,
      default: {},
    },
  },
  {
    timestamps: true,
  }
);

WalletTransactionSchema.index({ userId: 1, createdAt: -1 });
WalletTransactionSchema.index({ walletId: 1, createdAt: -1 });
WalletTransactionSchema.index({ referenceType: 1, referenceId: 1, source: 1 });
WalletTransactionSchema.index(
  { userId: 1, source: 1, idempotencyKey: 1 },
  { unique: true, sparse: true }
);

WalletTransactionSchema.set("toJSON", {
  virtuals: true,
  versionKey: false,
  transform: (_doc, ret) => {
    ret.id = ret._id;
    delete ret._id;
    return ret;
  },
});

const WalletTransaction = model<IWalletTransaction>(
  "WalletTransaction",
  WalletTransactionSchema
);

export default WalletTransaction;
