import mongoose, { Document, HydratedDocument, model, Schema, Types } from "mongoose";

export type BookingStatus = 
  | "PaymentPending" 
  | "Confirmed" 
  | "Failed" 
  | "Cancelled"
  | "Completed";

export interface IBooking extends Document {
  _id: Types.ObjectId;
  menteeId: Types.ObjectId;
  mentorId: Types.ObjectId;
  scheduleId?: Types.ObjectId;
  occurrenceId?: Types.ObjectId;
  scheduledAt: Date;
  startTime: Date;
  endTime: Date;
  duration: number;
  price: number;
  topic: string;
  notes?: string;
  status: BookingStatus;
  createdAt: Date;
  updatedAt: Date;
  expiresAt?: Date;
  meetingLink?: string;
}

const bookingSchema: Schema<IBooking> = new Schema(
  {
    menteeId: { type: Schema.Types.ObjectId, ref: "User", required: true, index: true },
    mentorId: { type: Schema.Types.ObjectId, ref: "User", required: true, index: true },
    scheduleId: { type: Schema.Types.ObjectId, ref: "AvailabilitySlot" },
    occurrenceId: { type: Schema.Types.ObjectId, ref: "AvailabilityOccurrence" },
    scheduledAt: { type: Date, required: true, index: true },
    startTime: { type: Date, required: true },
    endTime: { type: Date, required: true },
    duration: { type: Number, required: true },
    price: { type: Number, required: true },
    topic: { type: String, required: true },
    notes: { type: String },
    status: {
      type: String,
      enum: ["PaymentPending", "Confirmed", "Failed", "Cancelled", "Completed"],
      default: "PaymentPending",
      required: true,
      index: true,
    },
    expiresAt: { type: Date, index: true },
    meetingLink: { type: String },
  },
  { timestamps: true }
);

// Compound index for preventing double bookings
bookingSchema.index({ mentorId: 1, startTime: 1, endTime: 1 });
bookingSchema.index({ occurrenceId: 1, status: 1 });

export type BookingDoc = HydratedDocument<IBooking>;
const Booking = model<IBooking>("Booking", bookingSchema);
export default Booking;
