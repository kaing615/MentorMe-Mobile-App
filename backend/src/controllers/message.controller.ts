import { Request, Response } from 'express';
import { asyncHandler } from '../handlers/async.handler';
import { badRequest, forbidden, notFound, ok, unauthorized } from '../handlers/response.handler';
import Booking from '../models/booking.model';
import Message from '../models/message.model';
import Profile from '../models/profile.model';
import User from '../models/user.model';
import { emitToUser } from '../socket';
import cloudinaryService from '../utils/cloudinary';

const MAX_MESSAGE_LENGTH =
  parseInt(process.env.CHAT_MESSAGE_MAX_LENGTH || '2000', 10) || 2000;
const MAX_MESSAGES_WITHOUT_BOOKING = 10;
const SESSION_CHAT_WINDOW_DAYS = 3; // 3 days before and after session
const PRE_SESSION_MESSAGE_LIMIT = 5; // Before session starts
const POST_SESSION_MESSAGE_LIMIT = 3; // After session ends
const WEEKLY_MESSAGE_LIMIT_OUTSIDE_WINDOW = 2;

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
 * GET /api/v1/messages/peer/:peerId
 * List all messages with a specific peer across all bookings
 */
export const getMessagesByPeer = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { peerId } = req.params as { peerId: string };
  const limit = Math.min(Number(req.query.limit) || 200, 500);
  const before = req.query.before ? new Date(String(req.query.before)) : null;

  // Find all bookings between current user and peer
  const bookings = await Booking.find({
    $or: [
      { mentor: userId, mentee: peerId },
      { mentor: peerId, mentee: userId }
    ],
    status: { $nin: ['Cancelled', 'Failed', 'Declined'] }
  }).lean();

  if (bookings.length === 0) {
    return ok(res, []); // No bookings found, return empty array
  }

  const bookingIds = bookings.map(b => b._id);

  // Query messages from all bookings
  const query: any = { booking: { $in: bookingIds } };
  if (before && !Number.isNaN(before.getTime())) {
    query.createdAt = { $lt: before };
  }

  // Check if requesting latest messages (small limit without 'before' cursor)
  const isLatestRequest = !before && limit <= 10;
  
  let messages;
  if (isLatestRequest) {
    // For latest messages, query in descending order then reverse to get chronological order
    const latestMessages = await Message.find(query)
      .sort({ createdAt: -1 })
      .limit(limit)
      .lean();
    messages = latestMessages.reverse();
  } else {
    // Normal pagination - ascending order
    messages = await Message.find(query)
      .sort({ createdAt: 1 })
      .limit(limit)
      .lean();
  }

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

  // Rule: Mentee chat restrictions based on booking status and time window
  if (isMentee) {
    const isConfirmed = booking.status === 'Confirmed';
    const now = new Date();
    const sessionStart = new Date(booking.startTime);
    const sessionEnd = new Date(booking.endTime);
    
    if (!isConfirmed) {
      // Not confirmed: 10 free messages limit
      const menteeMessageCount = await Message.countDocuments({
        booking: bookingId,
        sender: userId,
      });

      if (menteeMessageCount >= MAX_MESSAGES_WITHOUT_BOOKING) {
        return forbidden(
          res,
          `Bạn cần đặt lịch (booking) để tiếp tục nhắn tin với mentor này. Bạn đã sử dụng hết ${MAX_MESSAGES_WITHOUT_BOOKING} tin nhắn miễn phí.`
        );
      }
    } else {
      // Confirmed: Check time windows (pre-session, in-session, post-session, outside)
      const preWindowStartMs = sessionStart.getTime() - (SESSION_CHAT_WINDOW_DAYS * 24 * 60 * 60 * 1000);
      const postWindowEndMs = sessionEnd.getTime() + (SESSION_CHAT_WINDOW_DAYS * 24 * 60 * 60 * 1000);
      const nowMs = now.getTime();
      const sessionStartMs = sessionStart.getTime();
      const sessionEndMs = sessionEnd.getTime();
      
      const isPreSession = nowMs >= preWindowStartMs && nowMs < sessionStartMs;
      const isDuringSession = nowMs >= sessionStartMs && nowMs <= sessionEndMs;
      const isPostSession = nowMs > sessionEndMs && nowMs <= postWindowEndMs;
      const isOutsideWindow = !isPreSession && !isDuringSession && !isPostSession;
      
      if (isDuringSession) {
        // During session: Face-to-face consultation, minimal chat needed
        return forbidden(
          res,
          'Phiên tư vấn đang diễn ra. Vui lòng trao đổi trực tiếp với mentor để được tư vấn tốt nhất.'
        );
      } else if (isPreSession) {
        // Pre-session: 5 messages to prepare
        const preSessionCount = await Message.countDocuments({
          booking: bookingId,
          sender: userId,
          createdAt: { $gte: new Date(preWindowStartMs) },
        });

        if (preSessionCount >= PRE_SESSION_MESSAGE_LIMIT) {
          return forbidden(
            res,
            `Bạn đã sử dụng hết ${PRE_SESSION_MESSAGE_LIMIT} tin nhắn chuẩn bị. ` +
            `Dành câu hỏi chính cho phiên tư vấn để được tư vấn sâu nhất!`
          );
        }
      } else if (isPostSession) {
        // Post-session: 3 messages for follow-up
        const postSessionCount = await Message.countDocuments({
          booking: bookingId,
          sender: userId,
          createdAt: { $gt: sessionEnd },
        });

        if (postSessionCount >= POST_SESSION_MESSAGE_LIMIT) {
          return forbidden(
            res,
            `Bạn đã sử dụng hết ${POST_SESSION_MESSAGE_LIMIT} tin nhắn follow-up. ` +
            `Đặt phiên tư vấn mới nếu cần trao đổi thêm.`
          );
        }
      } else if (isOutsideWindow) {
        // Outside window: 2 messages per week
        const oneWeekAgo = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000));
        
        const recentMessageCount = await Message.countDocuments({
          booking: bookingId,
          sender: userId,
          createdAt: { $gte: oneWeekAgo },
        });

        if (recentMessageCount >= WEEKLY_MESSAGE_LIMIT_OUTSIDE_WINDOW) {
          const oldestRecentMessage = await Message.findOne({
            booking: bookingId,
            sender: userId,
            createdAt: { $gte: oneWeekAgo },
          })
            .sort({ createdAt: 1 })
            .lean();

          const resetDate = oldestRecentMessage
            ? new Date(oldestRecentMessage.createdAt.getTime() + (7 * 24 * 60 * 60 * 1000))
            : now;

          const daysUntilReset = Math.ceil((resetDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000));

          return forbidden(
            res,
            `Bạn đã sử dụng hết ${WEEKLY_MESSAGE_LIMIT_OUTSIDE_WINDOW} tin nhắn/tuần. ` +
            `Hỏi nhiều qua chat sẽ mất giá trị phiên tư vấn. ` +
            `Đợi ${daysUntilReset} ngày hoặc đặt phiên tư vấn mới.`
          );
        }
      }
    }
  }

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

/**
 * GET /api/v1/messages/:bookingId/restriction-info
 * Get chat restriction info for a booking
 */
export const getChatRestrictionInfo = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  const { bookingId } = req.params as { bookingId: string };

  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  if (!isMentor && !isMentee) {
    return forbidden(res, 'Access denied');
  }

  // Calculate restriction info
  const isConfirmed = booking.status === 'Confirmed';
  const now = new Date();
  const sessionStart = new Date(booking.startTime);
  const sessionEnd = new Date(booking.endTime);
  
  const preWindowStartMs = sessionStart.getTime() - (SESSION_CHAT_WINDOW_DAYS * 24 * 60 * 60 * 1000);
  const postWindowEndMs = sessionEnd.getTime() + (SESSION_CHAT_WINDOW_DAYS * 24 * 60 * 60 * 1000);
  const nowMs = now.getTime();
  const sessionStartMs = sessionStart.getTime();
  const sessionEndMs = sessionEnd.getTime();
  
  const isPreSession = nowMs >= preWindowStartMs && nowMs < sessionStartMs;
  const isDuringSession = nowMs >= sessionStartMs && nowMs <= sessionEndMs;
  const isPostSession = nowMs > sessionEndMs && nowMs <= postWindowEndMs;
  const isOutsideWindow = !isPreSession && !isDuringSession && !isPostSession;

  // Count messages
  const myMessageCount = await Message.countDocuments({
    booking: bookingId,
    sender: userId,
  });

  const preSessionCount = await Message.countDocuments({
    booking: bookingId,
    sender: userId,
    createdAt: { $gte: new Date(preWindowStartMs), $lt: sessionStart },
  });

  const postSessionCount = await Message.countDocuments({
    booking: bookingId,
    sender: userId,
    createdAt: { $gt: sessionEnd, $lte: new Date(postWindowEndMs) },
  });

  const oneWeekAgo = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000));
  const weeklyMessageCount = await Message.countDocuments({
    booking: bookingId,
    sender: userId,
    createdAt: { $gte: oneWeekAgo },
  });

  return ok(res, {
    bookingId,
    bookingStatus: booking.status,
    isConfirmed,
    sessionPhase: isDuringSession ? 'during' : isPreSession ? 'pre' : isPostSession ? 'post' : 'outside',
    myMessageCount,
    preSessionCount,
    postSessionCount,
    weeklyMessageCount,
    limits: {
      maxFreeMessages: MAX_MESSAGES_WITHOUT_BOOKING,
      preSessionLimit: PRE_SESSION_MESSAGE_LIMIT,
      postSessionLimit: POST_SESSION_MESSAGE_LIMIT,
      weeklyLimit: WEEKLY_MESSAGE_LIMIT_OUTSIDE_WINDOW,
      sessionWindowDays: SESSION_CHAT_WINDOW_DAYS,
    },
  });
});

/**
 * POST /api/v1/messages/upload
 * Upload file/image for chat message
 */
export const uploadMessageFile = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user?.id ?? (req as any).user?._id;
  if (!userId) {
    return unauthorized(res, 'USER_NOT_AUTHENTICATED');
  }

  if (!req.file) {
    return badRequest(res, 'No file uploaded');
  }

  const { bookingId } = req.body;
  if (!bookingId) {
    return badRequest(res, 'bookingId is required');
  }

  // Verify user has access to this booking
  const booking = await Booking.findById(bookingId).lean();
  if (!booking) {
    return notFound(res, 'Booking not found');
  }

  const isMentor = String(booking.mentor) === String(userId);
  const isMentee = String(booking.mentee) === String(userId);
  if (!isMentor && !isMentee) {
    return forbidden(res, 'Access denied to this booking');
  }

  try {
    const file = req.file;
    const fileType = file.mimetype.startsWith('image/') ? 'image' : 'file';
    
    const uploadResult = await cloudinaryService.uploadFile(file.buffer, {
      folder: `mentor-me-mobile-app/chat-files/${bookingId}`,
      resource_type: 'auto',
      use_filename: true,
    });

    return ok(res, {
      url: uploadResult.secure_url,
      publicId: uploadResult.public_id,
      fileType,
      format: uploadResult.format,
      size: uploadResult.bytes,
      originalName: file.originalname,
    });
  } catch (error: any) {
    console.error('File upload error:', error);
    return badRequest(res, error.message || 'Failed to upload file');
  }
});

export default {
  getMessages,
  getMessagesByPeer,
  sendMessage,
  getChatRestrictionInfo,
  uploadMessageFile,
};
