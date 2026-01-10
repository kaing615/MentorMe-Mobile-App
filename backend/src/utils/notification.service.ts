// path: src/utils/notification.service.ts
import { Types } from 'mongoose';
import Notification, { INotification, TNotificationType } from '../models/notification.model';
import User from '../models/user.model';
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

export interface NoShowNotificationData extends NotificationData {
  noShowBy: 'mentor' | 'mentee' | 'both';
}

export interface NoShowRefundData {
  bookingId: string;
  mentorId: string;
  menteeId: string;
  mentorName: string;
  menteeName: string;
  startTime: Date;
  status: 'NoShowMentor' | 'NoShowMentee' | 'NoShowBoth';
  refundAmount?: number;
  platformFee?: number;
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

function formatDateTimeVi(date: Date) {
  return date.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour12: false,
  });
}

function isPushEnabledForType(
  prefs: any,
  type: TNotificationType
) {
  const safe = prefs || {};
  if (type.startsWith('booking_')) return safe.pushBooking !== false;
  if (type.startsWith('payment_')) return safe.pushPayment !== false;
  if ((type as string) === 'message') return safe.pushMessage !== false;
  return safe.pushSystem !== false;
}

async function shouldSendPush(
  userId: string | Types.ObjectId,
  type: TNotificationType
) {
  const user = await User.findById(userId, { notificationPrefs: 1 }).lean();
  if (!user) return true;
  return isPushEnabledForType((user as any).notificationPrefs, type);
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
  console.log(`[NOTIFICATION] Emitting socket event to user ${payload.userId}: type=${type}, title=${title}`);
  emitToUser(payload.userId, 'notifications:new', payload);
  console.log(`[NOTIFICATION] Socket event emitted for user ${payload.userId}`);

  if (await shouldSendPush(payload.userId, type)) {
    const pushData = buildPushData(type, payload.id, data);
    void sendPushToUser(payload.userId, { title, body, data: pushData }).catch((err) => {
      console.warn('Failed to send push notification:', err);
    });
  }

  return payload;
}

export async function notifyBookingConfirmed(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  // Notify mentee
  await createNotification(
    menteeId,
    'booking_confirmed',
    'Lịch hẹn đã xác nhận',
    `Mentor ${mentorName} đã xác nhận lịch lúc ${dateStr}.`,
    { bookingId, mentorId, startTime: dateIso }
  );

  // Notify mentor
  await createNotification(
    mentorId,
    'booking_confirmed',
    'Lịch hẹn mới',
    `${menteeName} đã đặt lịch lúc ${dateStr}.`,
    { bookingId, menteeId, startTime: dateIso }
  );
}

export async function notifyBookingFailed(data: NotificationData) {
  const { bookingId, mentorName, menteeId, startTime } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  await createNotification(
    menteeId,
    'booking_failed',
    'Không thể đặt lịch',
    `Lịch với ${mentorName} không thành công. Khung giờ đã được mở lại.`,
    { bookingId, startTime: dateIso }
  );
}

export async function notifyBookingCancelled(data: NotificationData, cancelledByMentor: boolean) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateIso = startTime.toISOString();
  const cancelledBy = cancelledByMentor ? mentorName : menteeName;

  // Notify mentee
  await createNotification(
    menteeId,
    'booking_cancelled',
    'Lịch hẹn đã hủy',
    `Lịch với ${mentorName} đã bị hủy bởi ${cancelledBy}.`,
    { bookingId, mentorId, startTime: dateIso, cancelledBy }
  );

  // Notify mentor
  await createNotification(
    mentorId,
    'booking_cancelled',
    'Lịch hẹn đã hủy',
    `Lịch với ${menteeName} đã bị hủy bởi ${cancelledBy}.`,
    { bookingId, menteeId, startTime: dateIso, cancelledBy }
  );
}

export async function notifyBookingReminder(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  await createNotification(
    menteeId,
    'booking_reminder',
    'Nhắc lịch hẹn',
    `Lịch với ${mentorName} bắt đầu lúc ${dateStr}.`,
    { bookingId, mentorId, startTime: dateIso }
  );

  await createNotification(
    mentorId,
    'booking_reminder',
    'Nhắc lịch hẹn',
    `Lịch với ${menteeName} bắt đầu lúc ${dateStr}.`,
    { bookingId, menteeId, startTime: dateIso }
  );
}

export async function notifyBookingPending(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  await createNotification(
    menteeId,
    'booking_pending',
    'Chờ mentor xác nhận',
    `Lịch với ${mentorName} lúc ${dateStr} đang chờ xác nhận.`,
    { bookingId, mentorId, startTime: dateIso }
  );

  await createNotification(
    mentorId,
    'booking_pending',
    'Yêu cầu lịch hẹn',
    `${menteeName} muốn đặt lịch lúc ${dateStr}. Vui lòng xác nhận hoặc từ chối.`,
    { bookingId, menteeId, startTime: dateIso }
  );
}

export async function notifyBookingDeclined(data: NotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  await createNotification(
    menteeId,
    'booking_declined',
    'Lịch hẹn bị từ chối',
    `Mentor ${mentorName} đã từ chối lịch lúc ${dateStr}.`,
    { bookingId, mentorId, startTime: dateIso }
  );

  await createNotification(
    mentorId,
    'booking_declined',
    'Đã từ chối lịch hẹn',
    `Bạn đã từ chối lịch với ${menteeName} lúc ${dateStr}.`,
    { bookingId, menteeId, startTime: dateIso }
  );
}

export async function notifyBookingNoShow(data: NoShowNotificationData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime, noShowBy } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  const mentorTitle = 'Session no-show';
  const menteeTitle = 'Session no-show';

  let mentorBody = `Session with ${menteeName} at ${dateStr} was marked as no-show.`;
  let menteeBody = `Session with ${mentorName} at ${dateStr} was marked as no-show.`;

  if (noShowBy === 'mentor') {
    mentorBody = `You were marked as no-show for the session with ${menteeName} at ${dateStr}.`;
    menteeBody = `Mentor ${mentorName} did not join the session at ${dateStr}.`;
  } else if (noShowBy === 'mentee') {
    mentorBody = `Mentee ${menteeName} did not join the session at ${dateStr}.`;
    menteeBody = `You were marked as no-show for the session with ${mentorName} at ${dateStr}.`;
  }

  const payload = { bookingId, mentorId, menteeId, startTime: dateIso, noShowBy };

  await createNotification(menteeId, 'booking_no_show', menteeTitle, menteeBody, payload);
  await createNotification(mentorId, 'booking_no_show', mentorTitle, mentorBody, payload);
}

/**
 * Notify about no-show with refund details
 */
export async function notifyNoShowWithRefund(data: NoShowRefundData) {
  const { bookingId, mentorId, menteeId, mentorName, menteeName, startTime, status, refundAmount, platformFee } = data;
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);

  const formatAmount = (amount?: number) => {
    if (!amount) return '0đ';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  if (status === 'NoShowMentee') {
    // TH1: Mentee vắng - Mentor giữ tiền
    await createNotification(
      menteeId,
      'no_show_penalty',
      'Vắng mặt phiên tư vấn',
      `Bạn đã không tham gia phiên tư vấn với ${mentorName} lúc ${dateStr}. Mentor sẽ giữ toàn bộ số tiền.`,
      { bookingId, mentorId, startTime: dateIso, status }
    );

    await createNotification(
      mentorId,
      'no_show_mentor_keeps',
      'Mentee vắng mặt',
      `${menteeName} không tham gia phiên tư vấn lúc ${dateStr}. Bạn vẫn giữ toàn bộ số tiền.`,
      { bookingId, menteeId, startTime: dateIso, status }
    );
  } else if (status === 'NoShowMentor') {
    // TH2: Mentor vắng - Hoàn 100% cho mentee
    await createNotification(
      menteeId,
      'no_show_refund',
      'Hoàn tiền do mentor vắng mặt',
      `Mentor ${mentorName} không tham gia phiên tư vấn lúc ${dateStr}. Bạn đã được hoàn ${formatAmount(refundAmount)} (100%).`,
      { bookingId, mentorId, startTime: dateIso, status, refundAmount }
    );

    await createNotification(
      mentorId,
      'no_show_penalty',
      'Vi phạm: Vắng mặt phiên tư vấn',
      `Bạn đã không tham gia phiên tư vấn với ${menteeName} lúc ${dateStr}. ${formatAmount(refundAmount)} đã được hoàn cho mentee.`,
      { bookingId, menteeId, startTime: dateIso, status, refundAmount }
    );
  } else if (status === 'NoShowBoth') {
    // TH3: Cả hai vắng - Hoàn 80% cho mentee, platform giữ 20%
    await createNotification(
      menteeId,
      'no_show_partial_refund',
      'Hoàn tiền một phần',
      `Cả hai không tham gia phiên tư vấn với ${mentorName} lúc ${dateStr}. Bạn đã được hoàn ${formatAmount(refundAmount)} (80%). Phí xử lý: ${formatAmount(platformFee)} (20%).`,
      { bookingId, mentorId, startTime: dateIso, status, refundAmount, platformFee }
    );

    await createNotification(
      mentorId,
      'no_show_penalty',
      'Vi phạm: Vắng mặt phiên tư vấn',
      `Cả hai không tham gia phiên tư vấn với ${menteeName} lúc ${dateStr}. Bạn bị trừ toàn bộ số tiền, mentee được hoàn 80%.`,
      { bookingId, menteeId, startTime: dateIso, status, refundAmount, platformFee }
    );
  }
}

export async function notifyPaymentSuccess(data: PaymentNotificationData) {
  console.log(`[NOTIFICATION] notifyPaymentSuccess called for booking ${data.bookingId}`);
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
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);
  const payload = {
    bookingId,
    mentorId,
    menteeId,
    startTime: dateIso,
    amount,
    currency,
    paymentId,
    status,
    event,
  };

  console.log(`[NOTIFICATION] Creating notification for mentee ${menteeId}...`);
  await createNotification(
    menteeId,
    'payment_success',
    'Thanh toán thành công',
    `Bạn đã thanh toán lịch với ${mentorName} lúc ${dateStr}.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_success', menteeId, bookingId) }
  );

  console.log(`[NOTIFICATION] Creating notification for mentor ${mentorId}...`);
  await createNotification(
    mentorId,
    'payment_success',
    'Đã nhận thanh toán',
    `Đã nhận thanh toán lịch với ${menteeName} lúc ${dateStr}.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_success', mentorId, bookingId) }
  );
  console.log(`[NOTIFICATION] Payment success notifications created for booking ${bookingId}`);
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
  const dateIso = startTime.toISOString();
  const dateStr = formatDateTimeVi(startTime);
  const payload = {
    bookingId,
    mentorId,
    menteeId,
    startTime: dateIso,
    amount,
    currency,
    paymentId,
    status,
    event,
  };

  // Check if it's insufficient balance
  const isInsufficientBalance = status === 'insufficient_balance';
  const menteeTitle = isInsufficientBalance ? 'Số dư không đủ' : 'Thanh toán thất bại';
  const menteeBody = isInsufficientBalance 
    ? `Số dư ví không đủ để thanh toán lịch với ${mentorName}. Vui lòng nạp thêm tiền và thử lại.`
    : `Thanh toán lịch với ${mentorName} không thành công. Vui lòng thử lại.`;

  await createNotification(
    menteeId,
    'payment_failed',
    menteeTitle,
    menteeBody,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_failed', menteeId, bookingId) }
  );

  await createNotification(
    mentorId,
    'payment_failed',
    'Thanh toán thất bại',
    `Thanh toán lịch với ${menteeName} không thành công.`,
    payload,
    { dedupeKey: buildPaymentDedupeKey('payment_failed', mentorId, bookingId) }
  );
}
