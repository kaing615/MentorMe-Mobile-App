// path: src/utils/notification.service.ts
import Notification, { TNotificationType } from '../models/notification.model';
import { Types } from 'mongoose';

export interface NotificationData {
  bookingId: string;
  mentorId: string;
  menteeId: string;
  mentorName: string;
  menteeName: string;
  startTime: Date;
}

export async function createNotification(
  userId: string | Types.ObjectId,
  type: TNotificationType,
  title: string,
  body: string,
  data?: Record<string, unknown>
) {
  await Notification.create({
    user: userId,
    type,
    title,
    body,
    data,
    read: false,
  });
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
