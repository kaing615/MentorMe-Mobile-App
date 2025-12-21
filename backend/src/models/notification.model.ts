// path: src/models/notification.model.ts
import { Schema, model, Types, Document } from 'mongoose';

export type TNotificationType =
  | 'booking_confirmed'
  | 'booking_failed'
  | 'booking_cancelled'
  | 'booking_reminder'
  | 'booking_pending'
  | 'booking_declined';

export interface INotification extends Document {
  _id: Types.ObjectId;
  user: Types.ObjectId;
  type: TNotificationType;
  title: string;
  body: string;
  data?: Record<string, unknown>;
  read: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const NotificationSchema = new Schema<INotification>(
  {
    user: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    type: {
      type: String,
      enum: [
        'booking_confirmed',
        'booking_failed',
        'booking_cancelled',
        'booking_reminder',
        'booking_pending',
        'booking_declined',
      ],
      required: true,
    },
    title: { type: String, required: true, trim: true },
    body: { type: String, required: true, trim: true },
    data: { type: Schema.Types.Mixed },
    read: { type: Boolean, default: false, index: true },
  },
  { timestamps: true }
);

NotificationSchema.index({ user: 1, read: 1, createdAt: -1 });

export default model<INotification>('Notification', NotificationSchema);
