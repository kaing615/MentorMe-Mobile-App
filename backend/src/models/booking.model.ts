// path: src/models/booking.model.ts
import { Schema, model, Types, Document } from 'mongoose';

export type TBookingStatus =
  | 'PaymentPending'
  | 'PendingMentor'
  | 'Confirmed'
  | 'Failed'
  | 'Cancelled'
  | 'Declined'
  | 'Completed';

export interface IBooking extends Document {
  _id: Types.ObjectId;
  mentee: Types.ObjectId;
  mentor: Types.ObjectId;
  occurrence: Types.ObjectId;
  startTime: Date;
  endTime: Date;
  price: number;
  status: TBookingStatus;
  topic?: string;
  notes?: string;
  meetingLink?: string;
  location?: string;
  expiresAt?: Date;
  mentorResponseDeadline?: Date;
  reminder24hSentAt?: Date;
  reminder1hSentAt?: Date;
  cancelledBy?: Types.ObjectId;
  cancelReason?: string;
  lateCancel?: boolean;
  lateCancelMinutes?: number;
  reviewId?: Types.ObjectId;
  reviewedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

const BookingSchema = new Schema<IBooking>(
  {
    mentee: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    mentor: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    occurrence: { type: Schema.Types.ObjectId, ref: 'AvailabilityOccurrence', required: true },
    startTime: { type: Date, required: true, index: true },
    endTime: { type: Date, required: true },
    price: { type: Number, required: true, min: 0 },
    status: {
      type: String,
      enum: ['PaymentPending', 'PendingMentor', 'Confirmed', 'Failed', 'Cancelled', 'Declined', 'Completed'],
      default: 'PaymentPending',
      index: true,
    },
    topic: { type: String, trim: true },
    notes: { type: String, trim: true },
    meetingLink: { type: String, trim: true },
    location: { type: String, trim: true },
    expiresAt: { type: Date, index: true },
    mentorResponseDeadline: { type: Date, index: true },
    reminder24hSentAt: { type: Date },
    reminder1hSentAt: { type: Date },
    cancelledBy: { type: Schema.Types.ObjectId, ref: 'User' },
    cancelReason: { type: String, trim: true },
    lateCancel: { type: Boolean, default: false },
    lateCancelMinutes: { type: Number, min: 0 },
    reviewId: { type: Schema.Types.ObjectId, ref: 'Review' },
    reviewedAt: { type: Date },
  },
  { timestamps: true }
);

// Unique constraint: only one active booking per occurrence (exclude terminal statuses)
BookingSchema.index(
  { occurrence: 1 },
  {
    unique: true,
    partialFilterExpression: { status: { $nin: ['Failed', 'Cancelled', 'Declined', 'Completed'] } },
  }
);

// Composite indexes for queries
BookingSchema.index({ mentor: 1, status: 1, startTime: 1 });
BookingSchema.index({ mentee: 1, status: 1, startTime: 1 });

export default model<IBooking>('Booking', BookingSchema);
