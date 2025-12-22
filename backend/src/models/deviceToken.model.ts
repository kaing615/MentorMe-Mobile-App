import { Schema, model, Types, Document } from 'mongoose';

export type TDevicePlatform = 'android' | 'ios' | 'web';

export interface IDeviceToken extends Document {
  _id: Types.ObjectId;
  user: Types.ObjectId;
  token: string;
  platform?: TDevicePlatform;
  deviceId?: string;
  lastSeenAt: Date;
  createdAt: Date;
  updatedAt: Date;
}

const DeviceTokenSchema = new Schema<IDeviceToken>(
  {
    user: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    token: { type: String, required: true, unique: true, index: true },
    platform: { type: String, enum: ['android', 'ios', 'web'] },
    deviceId: { type: String, trim: true },
    lastSeenAt: { type: Date, default: () => new Date() },
  },
  { timestamps: true }
);

DeviceTokenSchema.index({ user: 1, platform: 1, updatedAt: -1 });

export default model<IDeviceToken>('DeviceToken', DeviceTokenSchema);
