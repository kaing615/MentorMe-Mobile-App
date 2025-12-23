import { Schema, model, Types, Document } from "mongoose";
import { WalletCurrency } from "./wallet.model";

export type PayoutStatus =
  | "PENDING"
  | "APPROVED"
  | "PROCESSING"
  | "PAID"
  | "FAILED"
  | "CANCELLED";

export interface IMentorPayoutRequest extends Document {
  mentorId: Types.ObjectId;
  amountMinor: number;
  currency: WalletCurrency;
  status: PayoutStatus;
  attemptCount: number;
  externalId?: string | null;
  idempotencyKey?: string | null;
  metadata?: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
}

const MentorPayoutRequestSchema = new Schema<IMentorPayoutRequest>(
  {
    mentorId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
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
    status: {
      type: String,
      enum: ["PENDING", "APPROVED", "PROCESSING", "PAID", "FAILED", "CANCELLED"],
      required: true,
      default: "PENDING",
      index: true,
    },
    attemptCount: {
      type: Number,
      required: true,
      default: 0,
    },
    externalId: {
      type: String,
      default: null,
      index: true,
    },
    idempotencyKey: {
      type: String,
      default: null,
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

MentorPayoutRequestSchema.index(
  { mentorId: 1, idempotencyKey: 1 },
  { unique: true, sparse: true }
);

MentorPayoutRequestSchema.set("toJSON", {
  virtuals: true,
  versionKey: false,
  transform: (_doc, ret) => {
    ret.id = ret._id;
    delete ret._id;
    return ret;
  },
});

const MentorPayoutRequest = model<IMentorPayoutRequest>(
  "MentorPayoutRequest",
  MentorPayoutRequestSchema
);

export default MentorPayoutRequest;
