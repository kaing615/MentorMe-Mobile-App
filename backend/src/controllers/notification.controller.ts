import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import { ok, badRequest, forbidden } from '../handlers/response.handler';
import DeviceToken from '../models/deviceToken.model';
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

export default {
  registerDeviceToken,
  unregisterDeviceToken,
  sendTestPush,
  sendPushToUserId,
  listDeviceTokens,
};
