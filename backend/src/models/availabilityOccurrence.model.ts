// path: src/models/availabilityOccurrence.model.ts
import { Schema, model, Types } from 'mongoose';

export type TOccurrenceStatus = 'open' | 'booked' | 'closed';

export interface IAvailabilityOccurrence {
  slot: Types.ObjectId;
  mentor: Types.ObjectId;
  start: Date; // UTC
  end: Date;   // UTC
  visibility: 'public' | 'private';
  status: TOccurrenceStatus;
  capacity: number;
}

const OccurrenceSchema = new Schema<IAvailabilityOccurrence>(
  {
    slot:   { type: Schema.Types.ObjectId, ref: 'AvailabilitySlot', required: true, index: true },
    mentor: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    start:  { type: Date, required: true, index: true },
    end:    { type: Date, required: true, index: true },
    visibility: { type: String, enum: ['public', 'private'], default: 'public' },
    status: { type: String, enum: ['open', 'booked', 'closed'], default: 'open', index: true },
    capacity: { type: Number, default: 1 }
  },
  { timestamps: true }
);

// Chặn trùng khung giờ cho cùng mentor
OccurrenceSchema.index({ mentor: 1, start: 1, end: 1 }, { unique: true });

// Performance indexes for queries and overlap checks
OccurrenceSchema.index({ mentor: 1, start: 1, end: 1, status: 1 });
OccurrenceSchema.index({ slot: 1, start: 1, status: 1 });

export default model<IAvailabilityOccurrence>('AvailabilityOccurrence', OccurrenceSchema);
