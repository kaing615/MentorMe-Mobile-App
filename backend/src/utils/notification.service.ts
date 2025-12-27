// path: src/utils/notification.service.ts
import Notification, { INotification, TNotificationType } from '../models/notification.model';
import { Types } from 'mongoose';
import { emitToUser } from '../socket';
import { sendPushToUser } from './push.service';

type CreateNotificationOptions = {
  dedupeKey?: string;
};

export interface NotificationData {
  bookingId: string;
  mentorId: string;
  menteeId: string;
  mentorName: string;
  menteeName: string;
  startTime: Date;
}

export interface PaymentNotificationData extends NotificationData {
  amount?: number;
  currency?: string;
  paymentId?: string;
  status?: string;
  event?: string;
}

type NotificationPayload = {
  id: string;
  userId: string;
  type: TNotificationType;
  title: string;
  body: string;
  data?: Record<string, unknown>;
  read: boolean;
  createdAt: string | null;
};

function toNotificationPayload(doc: INotification): NotificationPayload {
  return {
    id: String(doc._id),
    userId: String(doc.user),
    type: doc.type,
    title: doc.title,
    body: doc.body,
    data: doc.data ?? undefined,
    read: doc.read,
    createdAt: doc.createdAt ? doc.createdAt.toISOString() : null,
  };
}

function stringifyValue(value: unknown): string | undefined {
  if (value === undefined || value === null) return undefined;
  if (value instanceof Date) return value.toISOString();
  if (typeof value === 'string') return value;
  if (typeof value === 'number' || typeof value === 'boolean') return String(value);
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function buildPushData(
  type: TNotificationType,
  notificationId: string,
  data?: Record<string, unknown>
) {
  const payload: Record<string, string> = {
    type,
    notificationId,
  };

  if (!data) return payload;

  for (const [key, value] of Object.entries(data)) {
    const stringValue = stringifyValue(value);
    if (stringValue !== undefined) {
      payload[key] = stringValue;
    }
  }

  return payload;
}

function isDuplicateKeyError(error: unknown) {
  const err = error as { code?: number; message?: string };
  return err?.code === 11000 || Boolean(err?.message?.includes('E11000'));
}

function buildPaymentDedupeKey(type: 'payment_success' | 'payment_failed', userId: string, bookingId: string) {
  return `${type}:${userId}:${bookingId}`;
}

export async function createNotification(
  userId: string | Types.ObjectId,
  type: TNotificationType,
  title: string,
  body: string,
  data?: Record<string, unknown>,
  options?: CreateNotificationOptions
) {
  let created: INotification | null = null;

  if (options?.dedupeKey) {
    const existing = await Notification.findOne({ dedupeKey: options.dedupeKey });
    if (existing) return toNotificationPayload(existing);
  }

  try {
    created = await Notification.create({
      user: userId,
      type,
      title,
      body,
      data,
      read: false,
      dedupeKey: options?.dedupeKey,
    });
  } catch (error) {
    if (options?.dedupeKey && isDuplicateKeyError(error)) {
      const existing = await Notification.findOne({ dedupeKey: options.dedupeKey });
      return existing ? toNotificationPayload(existing) : null;
    }
    throw error;
  }

  if (!created) return null;

  const payload = toNotificationPayload(created);
  emitToUser(payload.userId, 'notifications:new', payload);

  const pushData = buildPushData(type, payload.id, data);
  void sendPushToUser(payload.userId, { title, body, data: pushData }).catch((err) => {
    console.warn('Failed to send push notification:', err);
  });

  return payload;
}

export async function notifyBookingConfirmed(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateStr = startTime.toISOString();

  // Notify mentee
  await createNotification(
    menteeId,
    'booking_confirmed',
    'Booking Confirmed',
    `Your session with ${mentorName} is confirmed for ${dateStr}`,
    { bookingId, mentorId, startTime: dateStr }
  );

  // Notify mentor
  await createNotification(
    mentorId,
    'booking_confirmed',
    'New Booking Confirmed',
    `${menteeName} has booked a session with you on ${dateStr}`,
    { bookingId, menteeId, startTime: dateStr }
  );
}

export async function notifyBookingFailed(data: NotificationData) {
  const { bookingId, mentorName, menteeId, startTime } = data;
  const dateStr = startTime.toISOString();

  await createNotification(
    menteeId,
    'booking_failed',
    'Booking Failed',
    `Your booking with ${mentorName} could not be completed. The slot has been released.`,
    { bookingId, startTime: dateStr }
  );
}

export async function notifyBookingCancelled(data: NotificationData, cancelledByMentor: boolean) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateStr = startTime.toISOString();
  const cancelledBy = cancelledByMentor ? mentorName : menteeName;

  // Notify mentee
  await createNotification(
    menteeId,
    'booking_cancelled',
    'Booking Cancelled',
    `Your session with ${mentorName} has been cancelled by ${cancelledBy}.`,
    { bookingId, mentorId, startTime: dateStr, cancelledBy }
  );

  // Notify mentor
  await createNotification(
    mentorId,
    'booking_cancelled',
    'Booking Cancelled',
    `Session with ${menteeName} has been cancelled by ${cancelledBy}.`,
    { bookingId, menteeId, startTime: dateStr, cancelledBy }
  );
}

export async function notifyBookingReminder(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateStr = startTime.toISOString();

  await createNotification(
    menteeId,
    'booking_reminder',
    'Upcoming Session',
    `Reminder: your session with ${mentorName} starts at ${dateStr}.`,
    { bookingId, mentorId, startTime: dateStr }
  );

  await createNotification(
    mentorId,
    'booking_reminder',
    'Upcoming Session',
    `Reminder: your session with ${menteeName} starts at ${dateStr}.`,
    { bookingId, menteeId, startTime: dateStr }
  );
}

export async function notifyBookingPending(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateStr = startTime.toISOString();

  await createNotification(
    menteeId,
    'booking_pending',
    'Booking Awaiting Confirmation',
    `Your booking with ${mentorName} is awaiting mentor confirmation for ${dateStr}.`,
    { bookingId, mentorId, startTime: dateStr }
  );

  await createNotification(
    mentorId,
    'booking_pending',
    'Confirm New Booking',
    `${menteeName} requested a session on ${dateStr}. Please confirm or decline.`,
    { bookingId, menteeId, startTime: dateStr }
  );
}

export async function notifyBookingDeclined(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateStr = startTime.toISOString();

  await createNotification(
    menteeId,
    'booking_declined',
    'Booking Declined',
    `Your booking with ${mentorName} for ${dateStr} was declined.`,
    { bookingId, mentorId, startTime: dateStr }
  );

  await createNotification(
    mentorId,
    'booking_declined',
    'Booking Declined',
    `You declined the session with ${menteeName} on ${dateStr}.`,
    { bookingId, menteeId, startTime: dateStr }
  );
}

export async function notifyPaymentSuccess(data: PaymentNotificationData) {
  const {
    bookingId,
    mentorId,
    menteeId,
    mentorName,
    menteeName,
    startTime,
    amount,
    currency,
    paymentId,
    status,
    event,
  } = data;
  const dateStr = startTime.toISOString();
  const payload = {
    bookingId,
    mentorId,
    menteeId,
    startTime: dateStr,
    amount,
    currency,
    paymentId,
    status,
    event,
  };

  await createNotification(
    menteeId,
    'payment_success',
    'Payment Successful',
    `Payment successful for your session with ${mentorName} on ${dateStr}.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_success', menteeId, bookingId) }
  );

  await createNotification(
    mentorId,
    'payment_success',
    'Payment Received',
    `Payment received for session with ${menteeName} on ${dateStr}.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_success', mentorId, bookingId) }
  );
}

export async function notifyPaymentFailed(data: PaymentNotificationData) {
  const {
    bookingId,
    mentorId,
    menteeId,
    mentorName,
    menteeName,
    startTime,
    amount,
    currency,
    paymentId,
    status,
    event,
  } = data;
  const dateStr = startTime.toISOString();
  const payload = {
    bookingId,
    mentorId,
    menteeId,
    startTime: dateStr,
    amount,
    currency,
    paymentId,
    status,
    event,
  };

  await createNotification(
    menteeId,
    'payment_failed',
    'Payment Failed',
    `Payment failed for your session with ${mentorName}. Please try again.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_failed', menteeId, bookingId) }
  );

  await createNotification(
    mentorId,
    'payment_failed',
    'Payment Failed',
    `Payment failed for session with ${menteeName}.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_failed', mentorId, bookingId) }
  );
}
