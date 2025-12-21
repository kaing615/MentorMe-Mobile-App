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

export default {
  registerDeviceToken,
  unregisterDeviceToken,
  sendTestPush,
  sendPushToUserId,
};
