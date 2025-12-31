import { Schema, model, Types, Document } from 'mongoose';

export interface ITopUpIntent extends Document {
  user: Types.ObjectId;
  amount: number;
  currency: string;
  note: string;
  status: 'PENDING' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';
  qrImageUrl?: string;
  referenceCode: string;
  reviewedBy?: Types.ObjectId;
  reviewedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

const TopUpIntentSchema = new Schema<ITopUpIntent>({
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  amount: { type: Number, required: true },
  currency: { type: String, default: 'VND' },
  note: { type: String, required: true },
  status: { type: String, enum: ['PENDING','SUBMITTED','APPROVED','REJECTED'], default: 'PENDING', index: true },
  qrImageUrl: { type: String, default: null },
  referenceCode: { type: String, required: true, trim: true, index: true, unique: true },
  reviewedBy: { type: Schema.Types.ObjectId, ref: 'User', default: null },
  reviewedAt: { type: Date, default: null }
}, { timestamps: true });

TopUpIntentSchema.set('toJSON', {
  virtuals: true,
  versionKey: false,
  transform: (_doc, ret) => { ret.id = ret._id; delete ret._id; return ret; }
});

export default model<ITopUpIntent>('TopUpIntent', TopUpIntentSchema);
