import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import { badRequest, forbidden, notFound, ok, unauthorized } from '../handlers/response.handler';
import Booking from '../models/booking.model';
import Message from '../models/message.model';
import User from '../models/user.model';
import Profile from '../models/profile.model';
import { emitToUser } from '../socket';

const MAX_MESSAGE_LENGTH =
  parseInt(process.env.CHAT_MESSAGE_MAX_LENGTH || '2000', 10) || 2000;

type SenderPayload = {
  id: string;
  email?: string | null;
  fullName?: string | null;
  avatar?: string | null;
  role?: string | null;
  createdAt?: string | null;
};

async function buildSenderPayload(userId: string): Promise<SenderPayload | null> {
  const [user, profile] = await Promise.all([
    User.findById(userId).select('email userName name role createdAt').lean(),
    Profile.findOne({ user: userId }).select('fullName avatarUrl').lean(),
  ]);

  if (!user) return null;

  return {
    id: String(user._id),
    email: user.email ?? null,
    fullName: profile?.fullName ?? (user as any).name ?? user.userName ?? null,
    avatar: profile?.avatarUrl ?? null,
    role: (user as any).role ?? null,
    createdAt: user.createdAt ? new Date(user.createdAt).toISOString() : null,
  };
}

function formatMessage(message: any, sender?: SenderPayload | null) {
  return {
    id: String(message._id),
    bookingId: String(message.booking),
    senderId: String(message.sender),
    receiverId: String(message.receiver),
    content: message.content,
    messageType: message.messageType,
    timestamp: message.createdAt ? new Date(message.createdAt).toISOString() : null,
    sender: sender ?? null,
  };
}

/**
 * GET /api/v1/messages/:bookingId
 * List messages for a booking
 */
export const getMessages = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { bookingId } = req.params as { bookingId: string };
  const limit = Math.min(Number(req.query.limit) || 50, 200);
  const before = req.query.before ? new Date(String(req.query.before)) : null;

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  if (!isMentor && !isMentee) {
    return forbidden(res, 'Access denied');
  }

  const query: any = { booking: bookingId };
  if (before && !Number.isNaN(before.getTime())) {
    query.createdAt = { $lt: before };
  }

  const messages = await Message.find(query)
    .sort({ createdAt: 1 })
    .limit(limit)
    .lean();

  const senderIds = Array.from(new Set(messages.map((m) => String(m.sender))));
  const senderMap = new Map<string, SenderPayload | null>();
  await Promise.all(
    senderIds.map(async (id) => {
      senderMap.set(id, await buildSenderPayload(id));
    })
  );

  return ok(
    res,
    messages.map((message) => formatMessage(message, senderMap.get(String(message.sender))))
  );
});

/**
 * POST /api/v1/messages
 * Send a message for a booking
 */
export const sendMessage = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { bookingId, content, messageType = 'text' } = req.body as {
    bookingId?: string;
    content?: string;
    messageType?: string;
  };

  if (!bookingId) {
    return badRequest(res, 'bookingId is required');
  }

  if (!content || typeof content !== 'string' || !content.trim()) {
    return badRequest(res, 'content is required');
  }

  if (content.length > MAX_MESSAGE_LENGTH) {
    return badRequest(res, `content must be at most ${MAX_MESSAGE_LENGTH} characters`);
  }

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  if (!isMentor && !isMentee) {
    return forbidden(res, 'Access denied');
  }

  const receiverId = isMentor ? String(booking.mentee) : String(booking.mentor);

  const created = await Message.create({
    booking: bookingId,
    sender: userId,
    receiver: receiverId,
    content: content.trim(),
    messageType: messageType === 'image' || messageType === 'file' ? messageType : 'text',
  });

  const sender = await buildSenderPayload(String(userId));
  const payload = formatMessage(created, sender);

  emitToUser(receiverId, 'chat:message', payload);
  emitToUser(String(userId), 'chat:message', payload);

  return ok(res, payload, 'Message sent');
});

export default {
  getMessages,
  sendMessage,
};
