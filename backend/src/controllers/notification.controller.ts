import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import { ok, badRequest, forbidden, notFound } from '../handlers/response.handler';
import DeviceToken from '../models/deviceToken.model';
import Notification from '../models/notification.model';
import User from '../models/user.model';
import { sendPushToUser } from '../utils/push.service';

export const registerDeviceToken = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { token, platform, deviceId } = req.body as {
    token?: string;
    platform?: string;
    deviceId?: string;
  };

  if (!token) {
    return badRequest(res, 'token is required');
  }

  const normalizedPlatform =
    platform && ['android', 'ios', 'web'].includes(platform.toLowerCase())
      ? platform.toLowerCase()
      : undefined;

  const doc = await DeviceToken.findOneAndUpdate(
    { token },
    {
      $set: {
        user: userId,
        token,
        platform: normalizedPlatform,
        deviceId,
        lastSeenAt: new Date(),
      },
    },
    { upsert: true, new: true }
  );

  return ok(res, { id: String(doc._id), token: doc.token }, 'Token registered');
});

export const unregisterDeviceToken = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { token } = req.body as { token?: string };
  let removed = 0;

  if (token) {
    const result = await DeviceToken.deleteOne({ token, user: userId });
    removed = result.deletedCount ?? 0;
  } else {
    const result = await DeviceToken.deleteMany({ user: userId });
    removed = result.deletedCount ?? 0;
  }

  return ok(res, { removed }, 'Token removed');
});

export const sendTestPush = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { title, body, data } = req.body as {
    title?: string;
    body?: string;
    data?: Record<string, string>;
  };

  const result = await sendPushToUser(userId, {
    title: title || 'MentorMe',
    body: body || 'This is a test notification.',
    data,
  });

  return ok(res, result, 'Push sent');
});

export const sendPushToUserId = asyncHandler(async (req: Request, res: Response) => {
  const requesterRole = (req as any).user?.role;
  if (!requesterRole || !['admin', 'root'].includes(requesterRole)) {
    return forbidden(res, 'Forbidden');
  }

  const { userId, title, body, data } = req.body as {
    userId?: string;
    title?: string;
    body?: string;
    data?: Record<string, string>;
  };

  if (!userId || !title || !body) {
    return badRequest(res, 'userId, title, and body are required');
  }

  const result = await sendPushToUser(userId, { title, body, data });
  return ok(res, result, 'Push sent');
});

const maskToken = (token: string) => (token ? `...${token.slice(-6)}` : '');

export const listDeviceTokens = asyncHandler(async (req: Request, res: Response) => {
  const requesterRole = (req as any).user?.role;
  if (!requesterRole || !['admin', 'root'].includes(requesterRole)) {
    return forbidden(res, 'Forbidden');
  }

  const { userId, platform, includeToken } = req.query as {
    userId?: string;
    platform?: string;
    includeToken?: string | boolean;
  };
  const limit = Math.min(parseInt(String(req.query.limit || '50'), 10) || 50, 200);
  const page = Math.max(parseInt(String(req.query.page || '1'), 10) || 1, 1);

  const filter: any = {};
  if (userId) filter.user = userId;
  if (platform) filter.platform = String(platform).toLowerCase();

  const [total, docs] = await Promise.all([
    DeviceToken.countDocuments(filter),
    DeviceToken.find(filter)
      .sort({ updatedAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .populate({ path: 'user', select: 'email userName role status' })
      .lean(),
  ]);

  const includeFull = String(includeToken) === 'true';
  const items = docs.map((doc: any) => {
    const user = doc.user && typeof doc.user === 'object'
      ? {
          id: String(doc.user._id ?? doc.user.id ?? doc.user),
          email: doc.user.email ?? null,
          userName: doc.user.userName ?? null,
          role: doc.user.role ?? null,
          status: doc.user.status ?? null,
        }
      : null;

    return {
      id: String(doc._id),
      userId: String(doc.user?._id ?? doc.user ?? ''),
      token: includeFull ? doc.token : undefined,
      tokenMasked: maskToken(doc.token),
      platform: doc.platform ?? null,
      deviceId: doc.deviceId ?? null,
      lastSeenAt: doc.lastSeenAt ? new Date(doc.lastSeenAt).toISOString() : null,
      createdAt: doc.createdAt ? new Date(doc.createdAt).toISOString() : null,
      updatedAt: doc.updatedAt ? new Date(doc.updatedAt).toISOString() : null,
      user,
    };
  });

  return ok(res, { items, total, page, limit }, 'OK');
});

export const listNotifications = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { read, type } = req.query as {
    read?: string | boolean;
    type?: string;
  };

  const limit = Math.min(parseInt(String(req.query.limit || '20'), 10) || 20, 100);
  const page = Math.max(parseInt(String(req.query.page || '1'), 10) || 1, 1);

  const filter: any = { user: userId };
  if (typeof read !== 'undefined') {
    const readValue = String(read) === 'true';
    filter.read = readValue;
  }
  if (type) filter.type = type;

  const [total, docs] = await Promise.all([
    Notification.countDocuments(filter),
    Notification.find(filter)
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(limit)
      .lean(),
  ]);

  const items = docs.map((doc: any) => ({
    id: String(doc._id),
    type: doc.type,
    title: doc.title,
    body: doc.body,
    data: doc.data ?? null,
    read: Boolean(doc.read),
    createdAt: doc.createdAt ? new Date(doc.createdAt).toISOString() : null,
  }));

  return ok(res, { items, total, page, limit }, 'OK');
});

export const getUnreadCount = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const count = await Notification.countDocuments({ user: userId, read: false });
  return ok(res, { unread: count }, 'OK');
});

export const markNotificationRead = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { id } = req.params;
  const notification = await Notification.findOne({ _id: id, user: userId });

  if (!notification) return notFound(res, 'Notification not found');

  if (!notification.read) {
    notification.read = true;
    notification.readAt = new Date();
    await notification.save();
  }

  return ok(
    res,
    { id: String(notification._id), read: notification.read },
    'Notification marked as read'
  );
});

export const markAllNotificationsRead = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const result = await Notification.updateMany(
    { user: userId, read: false },
    { $set: { read: true, readAt: new Date() } }
  );

  return ok(res, { updated: result.modifiedCount ?? 0 }, 'Notifications marked as read');
});

const defaultPrefs = {
  pushBooking: true,
  pushPayment: true,
  pushMessage: true,
  pushSystem: true,
};

export const getNotificationPreferences = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const user = await User.findById(userId).lean();
  if (!user) return notFound(res, 'User not found');

  return ok(res, { ...defaultPrefs, ...(user as any).notificationPrefs }, 'OK');
});

export const updateNotificationPreferences = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id;
  if (!userId) return badRequest(res, 'Unauthorized');

  const { pushBooking, pushPayment, pushMessage, pushSystem } = req.body as {
    pushBooking?: boolean;
    pushPayment?: boolean;
    pushMessage?: boolean;
    pushSystem?: boolean;
  };

  const update: Record<string, boolean> = {};
  if (typeof pushBooking === 'boolean') update['notificationPrefs.pushBooking'] = pushBooking;
  if (typeof pushPayment === 'boolean') update['notificationPrefs.pushPayment'] = pushPayment;
  if (typeof pushMessage === 'boolean') update['notificationPrefs.pushMessage'] = pushMessage;
  if (typeof pushSystem === 'boolean') update['notificationPrefs.pushSystem'] = pushSystem;

  const user = Object.keys(update).length
    ? await User.findByIdAndUpdate(userId, { $set: update }, { new: true }).lean()
    : await User.findById(userId).lean();

  if (!user) return notFound(res, 'User not found');

  return ok(res, { ...defaultPrefs, ...(user as any).notificationPrefs }, 'Updated');
});

export default {
  registerDeviceToken,
  unregisterDeviceToken,
  sendTestPush,
  sendPushToUserId,
  listDeviceTokens,
  listNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
  getNotificationPreferences,
  updateNotificationPreferences,
};
