import { Schema, model, Types, Document } from "mongoose";

export type PaymentMethodType = "BANK" | "EWALLET";

export interface IPaymentMethod extends Document {
  userId: Types.ObjectId;
  type: PaymentMethodType;
  provider: string;
  accountName: string;
  accountNumber: string;
  isDefault: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const PaymentMethodSchema = new Schema<IPaymentMethod>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    type: {
      type: String,
      enum: ["BANK", "EWALLET"],
      required: true,
    },
    provider: {
      type: String,
      required: true,
    },
    accountName: {
      type: String,
      required: true,
    },
    accountNumber: {
      type: String,
      required: true,
    },
    isDefault: {
      type: Boolean,
      default: false,
    },
  },
  { timestamps: true }
);

PaymentMethodSchema.index({ userId: 1 });

export default model<IPaymentMethod>(
  "PaymentMethod",
  PaymentMethodSchema
);
