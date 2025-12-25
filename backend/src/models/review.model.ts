import { Schema, model, Types, Document } from 'mongoose';

export interface IReview extends Document {
  _id: Types.ObjectId;
  booking: Types.ObjectId;
  mentee: Types.ObjectId;
  mentor: Types.ObjectId;
  rating: number;
  comment?: string;
  createdAt: Date;
  updatedAt: Date;
}

const ReviewSchema = new Schema<IReview>(
  {
    booking: {
      type: Schema.Types.ObjectId,
      ref: 'Booking',
      required: true,
      unique: true, // Enforce 1 review per booking
      index: true,
    },
    mentee: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    mentor: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    rating: {
      type: Number,
      required: true,
      min: 1,
      max: 5,
    },
    comment: {
      type: String,
      trim: true,
      maxlength: 1000,
    },
  },
  { timestamps: true }
);

// Compound index for listing mentor reviews (newest first)
ReviewSchema.index({ mentor: 1, createdAt: -1 });

// Compound index for listing mentee's own reviews
ReviewSchema.index({ mentee: 1, createdAt: -1 });

export default model<IReview>('Review', ReviewSchema);
