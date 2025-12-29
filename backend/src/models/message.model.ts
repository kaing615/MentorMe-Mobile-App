import { Schema, model, Types, Document } from 'mongoose';

export type TMessageType = 'text' | 'image' | 'file';

export interface IMessage extends Document {
  _id: Types.ObjectId;
  booking: Types.ObjectId;
  sender: Types.ObjectId;
  receiver: Types.ObjectId;
  content: string;
  messageType: TMessageType;
  read: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const MessageSchema = new Schema<IMessage>(
  {
    booking: { type: Schema.Types.ObjectId, ref: 'Booking', required: true, index: true },
    sender: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    receiver: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    content: { type: String, required: true, trim: true, maxlength: 2000 },
    messageType: {
      type: String,
      enum: ['text', 'image', 'file'],
      default: 'text',
    },
    read: { type: Boolean, default: false, index: true },
  },
  { timestamps: true }
);

MessageSchema.index({ booking: 1, createdAt: 1 });
MessageSchema.index({ receiver: 1, read: 1, createdAt: -1 });

export default model<IMessage>('Message', MessageSchema);
