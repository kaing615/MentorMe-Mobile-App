import {
  Document,
  HydratedDocument,
  model,
  Schema,
  Types,
} from "mongoose";

export interface IUser extends Document {
  _id: Types.ObjectId;
  userName: string;
  name?: string;
  email: string;
  passwordHash: string;
  role: "mentee" | "mentor" | "admin" | "root";
  status: "active" | "pending-mentor" | "verifying" | "onboarding";
  isBlocked: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const userSchema: Schema<IUser> = new Schema(
  {
    userName: { type: String, required: true },
    name: { type: String },
    email: { type: String, required: true, unique: true },
    passwordHash: { type: String, required: true },
    role: {
      type: String,
      enum: ["mentee", "mentor", "admin", "root"],
      default: "mentee",
    },
    status: {
      type: String,
      enum: ["active", "verifying", "pending-mentor", "onboarding"],
      default: "verifying",
    },
    isBlocked: { type: Boolean, default: false },
  },
  { timestamps: true }
);

export type UserDoc = HydratedDocument<IUser>;
const User = model<IUser>("User", userSchema);
export default User;
