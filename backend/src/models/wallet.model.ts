import { Schema, model, Types, Document } from "mongoose";

export type WalletCurrency = "VND" | "USD";
export type WalletStatus = "ACTIVE" | "LOCKED";

export interface IWallet extends Document {
  userId: Types.ObjectId;
  balanceMinor: number;
  currency: WalletCurrency;
  status: WalletStatus;
  createdAt: Date;
  updatedAt: Date;
}

const WalletSchema = new Schema<IWallet>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
      index: true,
    },
    balanceMinor: {
      type: Number,
      required: true,
      default: 0,
      // Removed min: 0 to allow negative balance in test mode
    },
    currency: {
      type: String,
      enum: ["VND", "USD"],
      required: true,
      default: "VND",
    },
    status: {
      type: String,
      enum: ["ACTIVE", "LOCKED"],
      required: true,
      default: "ACTIVE",
    },
  },
  {
    timestamps: true,
  }
);

// Match existing models' toJSON style (rename _id -> id, remove __v)
WalletSchema.set("toJSON", {
  virtuals: true,
  versionKey: false,
  transform: (_doc, ret) => {
    ret.id = ret._id;
    delete ret._id;
    return ret;
  },
});

const Wallet = model<IWallet>("Wallet", WalletSchema);
export default Wallet;
