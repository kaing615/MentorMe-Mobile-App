import { Schema, model, Types, Document } from 'mongoose';

export type TSessionStatus = 'waiting' | 'active' | 'ended' | 'no_show';

export type TSessionEndReason =
  | 'completed'
  | 'ended_by_mentor'
  | 'ended_by_mentee'
  | 'no_show_mentor'
  | 'no_show_mentee'
  | 'no_show_both'
  | 'auto_end';

export interface IQosReport {
  timestamp: Date;
  rttMs?: number;
  jitterMs?: number;
  packetLoss?: number;
  bitrateKbps?: number;
}

export interface IQosSummary {
  samples: number;
  last?: IQosReport;
  avgRttMs?: number;
  avgJitterMs?: number;
  avgPacketLoss?: number;
  avgBitrateKbps?: number;
  lastUpdatedAt?: Date;
}

export interface ISessionLog extends Document {
  _id: Types.ObjectId;
  booking: Types.ObjectId;
  mentor: Types.ObjectId;
  mentee: Types.ObjectId;
  scheduledStart: Date;
  scheduledEnd: Date;
  actualStart?: Date;
  actualEnd?: Date;
  durationSec?: number;
  status: TSessionStatus;
  endReason?: TSessionEndReason;
  waitingRoomMs?: number;
  mentorJoinAt?: Date;
  menteeJoinAt?: Date;
  mentorAdmitAt?: Date;
  mentorDisconnects?: number;
  menteeDisconnects?: number;
  qos?: {
    mentor?: IQosSummary;
    mentee?: IQosSummary;
  };
  createdAt: Date;
  updatedAt: Date;
}

const QoSReportSchema = new Schema<IQosReport>(
  {
    timestamp: { type: Date, required: true },
    rttMs: { type: Number, min: 0 },
    jitterMs: { type: Number, min: 0 },
    packetLoss: { type: Number, min: 0, max: 100 },
    bitrateKbps: { type: Number, min: 0 },
  },
  { _id: false }
);

const QoSSummarySchema = new Schema<IQosSummary>(
  {
    samples: { type: Number, required: true, min: 0, default: 0 },
    last: { type: QoSReportSchema },
    avgRttMs: { type: Number, min: 0 },
    avgJitterMs: { type: Number, min: 0 },
    avgPacketLoss: { type: Number, min: 0, max: 100 },
    avgBitrateKbps: { type: Number, min: 0 },
    lastUpdatedAt: { type: Date },
  },
  { _id: false }
);

const SessionLogSchema = new Schema<ISessionLog>(
  {
    booking: { type: Schema.Types.ObjectId, ref: 'Booking', required: true, unique: true },
    mentor: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    mentee: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    scheduledStart: { type: Date, required: true },
    scheduledEnd: { type: Date, required: true },
    actualStart: { type: Date },
    actualEnd: { type: Date },
    durationSec: { type: Number, min: 0 },
    status: {
      type: String,
      enum: ['waiting', 'active', 'ended', 'no_show'],
      default: 'waiting',
    },
    endReason: {
      type: String,
      enum: [
        'completed',
        'ended_by_mentor',
        'ended_by_mentee',
        'no_show_mentor',
        'no_show_mentee',
        'no_show_both',
        'auto_end',
      ],
    },
    waitingRoomMs: { type: Number, min: 0 },
    mentorJoinAt: { type: Date },
    menteeJoinAt: { type: Date },
    mentorAdmitAt: { type: Date },
    mentorDisconnects: { type: Number, min: 0, default: 0 },
    menteeDisconnects: { type: Number, min: 0, default: 0 },
    qos: {
      mentor: { type: QoSSummarySchema },
      mentee: { type: QoSSummarySchema },
    },
  },
  { timestamps: true }
);

SessionLogSchema.index({ booking: 1 }, { unique: true });
SessionLogSchema.index({ mentor: 1, scheduledStart: -1 });
SessionLogSchema.index({ mentee: 1, scheduledStart: -1 });
SessionLogSchema.index({ status: 1, scheduledStart: -1 });

export default model<ISessionLog>('SessionLog', SessionLogSchema);
