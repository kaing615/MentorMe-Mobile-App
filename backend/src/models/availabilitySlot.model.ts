// path: src/models/availabilitySlot.model.ts
import { Schema, model, Types } from 'mongoose';

export type TAvailabilityVisibility = 'public' | 'private';
export type TAvailabilityStatus = 'draft' | 'published' | 'archived';

export interface IAvailabilitySlot {
  mentor: Types.ObjectId;
  title?: string;
  description?: string;
  timezone: string;
  start?: Date;
  end?: Date;
  rrule?: string | null;
  exdates?: Date[];
  bufferBeforeMin?: number;
  bufferAfterMin?: number;
  visibility: TAvailabilityVisibility;
  status: TAvailabilityStatus;
  publishHorizonDays?: number;
}

const AvailabilitySlotSchema = new Schema<IAvailabilitySlot>(
  {
    mentor: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    title: String,
    description: String,
    timezone: { type: String, required: true },
    start: Date,
    end: Date,
    rrule: { type: String, default: null },
    exdates: [{ type: Date }],
    bufferBeforeMin: { type: Number, default: 0 },
    bufferAfterMin: { type: Number, default: 0 },
    visibility: { type: String, enum: ['public', 'private'], default: 'public' },
    status: { type: String, enum: ['draft', 'published', 'archived'], default: 'draft', index: true },
    publishHorizonDays: { type: Number, default: 90 }
  },
  { timestamps: true }
);

export default model<IAvailabilitySlot>('AvailabilitySlot', AvailabilitySlotSchema);
