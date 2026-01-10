import { Schema, model, Types, Document } from "mongoose";

export type TReportStatus = "open" | "investigating" | "resolved";
export type TReportTargetType = "user" | "review" | "message" | "file";

export interface IReport extends Document {
  _id: Types.ObjectId;
  reporter: Types.ObjectId;
  targetType: TReportTargetType;
  targetId: string;
  type?: string;
  note?: string;
  status: TReportStatus;
  createdAt: Date;
  updatedAt: Date;
}

const ReportSchema = new Schema<IReport>(
  {
    reporter: { type: Schema.Types.ObjectId, ref: "User", required: true },
    targetType: {
      type: String,
      enum: ["user", "review", "message", "file"],
      required: true,
    },
    targetId: { type: String, required: true, trim: true },
    type: { type: String, trim: true, default: "" },
    note: { type: String, trim: true, default: "" },
    status: {
      type: String,
      enum: ["open", "investigating", "resolved"],
      default: "open",
    },
  },
  { timestamps: true }
);

ReportSchema.index({ status: 1, createdAt: -1 });
ReportSchema.index({ targetType: 1, targetId: 1 });

const Report = model<IReport>("Report", ReportSchema);
export default Report;
