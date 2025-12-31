import { createAdapter } from "@socket.io/redis-adapter";
import { createHash } from "crypto";
import type { Server as HttpServer } from "http";
import jwt from "jsonwebtoken";
import { createClient } from "redis";
import { Socket, Server as SocketIOServer } from "socket.io";
import Booking from "../models/booking.model";
import User from "../models/user.model";
import {
  getSessionActualStart,
  getSessionStateTtlSeconds,
  isWithinSessionWindow,
  recordSessionAdmit,
  recordSessionDisconnect,
  recordSessionEnd,
  recordSessionJoin,
  recordSessionQoS,
} from "../services/session.service";
import redis from "../utils/redis";

type SocketUser = {
  id: string;
  email?: string;
  role?: string;
};

type JwtPayload = {
  jti?: string;
  id: string;
  email: string;
  role: string;
};

type SessionJoinPayload = {
  jti?: string;
  bookingId: string;
  userId: string;
  role: "mentor" | "mentee";
};

type SessionState = {
  bookingId: string;
  role: "mentor" | "mentee";
  live: boolean;
};

type SocketAck = (payload: { ok: boolean; message?: string; data?: unknown }) => void;

type SignalPayload = {
  bookingId: string;
  data: unknown;
};

type QosPayload = {
  bookingId: string;
  stats: Record<string, unknown>;
};

let io: SocketIOServer | null = null;
let pubClient: ReturnType<typeof createClient> | null = null;
let subClient: ReturnType<typeof createClient> | null = null;

const hashToken = (token: string) =>
  createHash("sha256").update(token).digest("hex");

function getRedisClientOptions() {
  const host = process.env.SOCKET_REDIS_HOST || process.env.REDIS_HOST;
  const port = Number(process.env.SOCKET_REDIS_PORT || process.env.REDIS_PORT || 10938);
  const username =
    process.env.SOCKET_REDIS_USERNAME || process.env.REDIS_USERNAME || "default";
  const password = process.env.SOCKET_REDIS_PASSWORD || process.env.REDIS_PASSWORD;

  return {
    username,
    password,
    socket: {
      host,
      port,
    },
  };
}

function getCorsOptions() {
  const raw = process.env.SOCKET_CORS_ORIGIN;
  if (!raw) {
    return { origin: true, credentials: true };
  }
  const origins = raw
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean);

  if (origins.includes("*")) {
    return { origin: "*", credentials: false };
  }

  return { origin: origins, credentials: true };
}

function extractToken(socket: Socket): string | null {
  const authToken = (socket.handshake.auth as any)?.token;
  if (typeof authToken === "string" && authToken.trim()) {
    return authToken.trim();
  }

  const headerToken = socket.handshake.headers?.authorization;
  if (typeof headerToken === "string" && headerToken.startsWith("Bearer ")) {
    return headerToken.slice(7).trim();
  }

  const queryToken = (socket.handshake.query as any)?.token;
  if (typeof queryToken === "string" && queryToken.trim()) {
    return queryToken.trim();
  }

  if (Array.isArray(queryToken) && queryToken[0]) {
    return String(queryToken[0]).trim();
  }

  return null;
}

async function authenticateSocket(token: string): Promise<SocketUser> {
  if (!process.env.JWT_SECRET) {
    throw new Error("JWT_SECRET not configured");
  }

  const payload = jwt.verify(token, process.env.JWT_SECRET, {
    issuer: "mentorme",
    audience: "mentorme-mobile",
    algorithms: ["HS256"],
    clockTolerance: 5,
  }) as JwtPayload;

  const jtiKey = payload.jti ? `bl:jwt:jti:${payload.jti}` : null;
  const hashKey = `bl:jwt:${hashToken(token)}`;

  const [jtiHit, hashHit, dbUser] = await Promise.all([
    jtiKey ? redis.get(jtiKey) : null,
    redis.get(hashKey),
    User.findById(payload.id).select("isBlocked").lean(),
  ]);

  if (jtiHit || hashHit) {
    throw new Error("Token revoked");
  }
  if (!dbUser) {
    throw new Error("Unauthorized");
  }
  if ((dbUser as any).isBlocked) {
    throw new Error("User blocked");
  }

  return { id: payload.id, email: payload.email, role: payload.role };
}

function getSessionJoinSecret() {
  return process.env.SESSION_JOIN_JWT_SECRET || process.env.JWT_SECRET;
}

function verifySessionJoinToken(token: string): SessionJoinPayload {
  const secret = getSessionJoinSecret();
  if (!secret) {
    throw new Error("SESSION_JOIN_JWT_SECRET not configured");
  }

  const payload = jwt.verify(token, secret, {
    issuer: "mentorme",
    audience: "mentorme-session",
    algorithms: ["HS256"],
    clockTolerance: 5,
  }) as SessionJoinPayload;

  if (!payload?.bookingId || !payload?.userId || !payload?.role) {
    throw new Error("Invalid join token");
  }

  if (payload.role !== "mentor" && payload.role !== "mentee") {
    throw new Error("Invalid join token role");
  }

  return payload;
}

function getSessionRooms(bookingId: string) {
  return {
    liveRoom: `session:${bookingId}:live`,
    waitingRoom: `session:${bookingId}:waiting`,
  };
}

function getSessionAdmitKey(bookingId: string) {
  return `session:admitted:${bookingId}`;
}

function respond(callback: SocketAck | undefined, payload: { ok: boolean; message?: string; data?: unknown }) {
  if (typeof callback === "function") {
    callback(payload);
  }
}

async function setupRedisAdapter(socketServer: SocketIOServer) {
  const enabled =
    (process.env.SOCKET_REDIS_ENABLED || "true").toLowerCase() !== "false";
  if (!enabled) return;

  if (!process.env.SOCKET_REDIS_HOST && !process.env.REDIS_HOST) {
    throw new Error(
      "Socket.IO Redis adapter enabled but SOCKET_REDIS_HOST/REDIS_HOST is missing"
    );
  }

  try {
    const options = getRedisClientOptions();
    pubClient = createClient(options);
    subClient = pubClient.duplicate();
    await Promise.all([pubClient.connect(), subClient.connect()]);
    socketServer.adapter(createAdapter(pubClient, subClient));
    console.log("Socket.IO Redis adapter enabled");
  } catch (err) {
    if (pubClient?.isOpen) {
      await pubClient.quit();
    }
    if (subClient?.isOpen) {
      await subClient.quit();
    }
    pubClient = null;
    subClient = null;
    throw err;
  }
}

export async function initSocket(server: HttpServer) {
  if (io) return io;

  io = new SocketIOServer(server, {
    cors: getCorsOptions(),
  });

  io.use(async (socket, next) => {
    try {
      const token = extractToken(socket);
      if (!token) {
        return next(new Error("Unauthorized"));
      }
      const user = await authenticateSocket(token);
      socket.data.user = user;
      return next();
    } catch (err) {
      return next(new Error("Unauthorized"));
    }
  });

  io.on("connection", (socket) => {
    const user = socket.data.user as SocketUser | undefined;
    if (user?.id) {
      socket.join(`user:${user.id}`);
      
      // Set user online in Redis with TTL
      const presenceKey = `presence:user:${user.id}`;
      redis.setEx(presenceKey, 120, "1").catch((err) => {
        console.error("Failed to set user online:", err);
      });
      
      // Notify other users that this user is online
      socket.broadcast.emit("user:online", { userId: user.id });
    }

    const leaveSessionRooms = (bookingId: string) => {
      const rooms = getSessionRooms(bookingId);
      socket.leave(rooms.liveRoom);
      socket.leave(rooms.waitingRoom);
    };

    const relaySignal = (event: string, payload: SignalPayload) => {
      const session = socket.data.session as SessionState | undefined;
      if (!session || session.bookingId !== payload.bookingId || !session.live) return;
      const rooms = getSessionRooms(session.bookingId);
      socket.to(rooms.liveRoom).emit(event, {
        bookingId: session.bookingId,
        fromUserId: user?.id,
        fromRole: session.role,
        data: payload.data,
      });
    };

    const readNumber = (value: unknown, min = 0, max?: number) => {
      if (typeof value !== "number" || Number.isNaN(value)) return undefined;
      const safe = Math.max(min, value);
      if (typeof max === "number") return Math.min(max, safe);
      return safe;
    };

    socket.on("session:join", async (payload: { token?: string }, callback?: SocketAck) => {
      console.log(`[Session] Received session:join event, payload:`, payload);
      
      try {
        if (!payload?.token || typeof payload.token !== "string") {
          console.log(`[Session] JOIN_TOKEN_REQUIRED - invalid payload`);
          return respond(callback, { ok: false, message: "JOIN_TOKEN_REQUIRED" });
        }

        console.log(`[Session] Verifying join token...`);
        const joinPayload = verifySessionJoinToken(payload.token);
        console.log(`[Session] Token verified - userId: ${joinPayload.userId}, bookingId: ${joinPayload.bookingId}, role: ${joinPayload.role}`);
        
        const socketUser = socket.data.user as SocketUser | undefined;

        if (!socketUser || String(socketUser.id) !== String(joinPayload.userId)) {
          console.log(`[Session] UNAUTHORIZED - socketUser: ${socketUser?.id}, tokenUser: ${joinPayload.userId}`);
          return respond(callback, { ok: false, message: "UNAUTHORIZED" });
        }

        const booking = await Booking.findById(joinPayload.bookingId).lean();
        if (!booking) {
          console.log(`[Session] BOOKING_NOT_FOUND - bookingId: ${joinPayload.bookingId}`);
          return respond(callback, { ok: false, message: "BOOKING_NOT_FOUND" });
        }

        if (booking.status !== "Confirmed") {
          return respond(callback, { ok: false, message: "SESSION_NOT_READY" });
        }

        const isMentor = String(booking.mentor) === String(joinPayload.userId);
        const isMentee = String(booking.mentee) === String(joinPayload.userId);
        if (!isMentor && !isMentee) {
          return respond(callback, { ok: false, message: "ACCESS_DENIED" });
        }

        if (joinPayload.role === "mentor" && !isMentor) {
          return respond(callback, { ok: false, message: "ROLE_MISMATCH" });
        }
        if (joinPayload.role === "mentee" && !isMentee) {
          return respond(callback, { ok: false, message: "ROLE_MISMATCH" });
        }

        const now = new Date();
        if (!isWithinSessionWindow(now, new Date(booking.startTime), new Date(booking.endTime))) {
          return respond(callback, { ok: false, message: "SESSION_WINDOW_CLOSED" });
        }

        const currentSession = socket.data.session as SessionState | undefined;
        if (currentSession && currentSession.bookingId !== String(booking._id)) {
          leaveSessionRooms(currentSession.bookingId);
        }

        const bookingId = String(booking._id);
        const rooms = getSessionRooms(bookingId);
        
        // Mentee always needs fresh admission for each session
        // Don't use cached admit status from Redis/DB
        let admitted = false;
        
        // Only check if mentor is already in the session (meaning mentee should auto-admit)
        if (joinPayload.role === "mentee") {
          const liveRoomSockets = await io.in(rooms.liveRoom).fetchSockets();
          // Auto-admit mentee only if mentor is already in liveRoom
          admitted = liveRoomSockets.some((s: any) => s.data?.session?.role === "mentor");
          console.log(`[Session] Mentee joining - mentor already present: ${admitted}`);
        }

        if (joinPayload.role === "mentor") {
          socket.join(rooms.liveRoom);
          socket.data.session = { bookingId, role: "mentor", live: true };
        } else if (admitted) {
          socket.join(rooms.liveRoom);
          socket.data.session = { bookingId, role: "mentee", live: true };
        } else {
          socket.join(rooms.waitingRoom);
          socket.data.session = { bookingId, role: "mentee", live: false };
        }

        await recordSessionJoin(booking as any, joinPayload.role);

        // Get actual session start time if session is already active (for reconnecting users)
        const actualStart = await getSessionActualStart(bookingId);
        const sessionStartedAt = actualStart ? actualStart.toISOString() : null;

        respond(callback, { ok: true, data: { bookingId, role: joinPayload.role, admitted, sessionStartedAt } });
        socket.emit("session:joined", { bookingId, role: joinPayload.role, admitted, sessionStartedAt });
        
        console.log(`[Session] ${joinPayload.role} joined booking ${bookingId}, admitted: ${admitted}, sessionStartedAt: ${sessionStartedAt}`);

        if (joinPayload.role === "mentee" && !admitted) {
          socket.emit("session:waiting", { bookingId });
          console.log(`[Session] Sent waiting event to mentee for booking ${bookingId}`);
        }

        // If mentor just joined, check if there's a mentee waiting and notify mentor
        if (joinPayload.role === "mentor") {
          const waitingRoom = rooms.waitingRoom;
          const waitingSockets = await io.in(waitingRoom).fetchSockets();
          console.log(`[Session] Mentor joined, checking waiting room. Found ${waitingSockets.length} waiting sockets`);
          
          if (waitingSockets.length > 0) {
            // There's a mentee waiting, notify the mentor who just joined
            waitingSockets.forEach((waitingSocket: any) => {
              const waitingUser = waitingSocket.data?.user;
              if (waitingUser) {
                console.log(`[Session] Notifying mentor about waiting mentee: ${waitingUser.id}`);
                socket.emit("session:participant-joined", {
                  bookingId,
                  userId: waitingUser.id,
                  role: "mentee",
                });
              }
            });
          }
        }

        // Notify other participants in BOTH rooms (waiting and live)
        const participantEvent = {
          bookingId,
          userId: socketUser.id,
          role: joinPayload.role,
        };
        
        console.log(`[Session] Emitting participant-joined to liveRoom and waitingRoom:`, participantEvent);
        socket.to(rooms.liveRoom).emit("session:participant-joined", participantEvent);
        socket.to(rooms.waitingRoom).emit("session:participant-joined", participantEvent);
      } catch (err) {
        return respond(callback, { ok: false, message: "SESSION_JOIN_FAILED" });
      }
    });

    socket.on("session:admit", async (payload: { bookingId?: string }, callback?: SocketAck) => {
      try {
        const bookingId =
          payload?.bookingId || (socket.data.session as SessionState | undefined)?.bookingId;
        const session = socket.data.session as SessionState | undefined;

        if (!bookingId || !session || session.bookingId !== bookingId) {
          return respond(callback, { ok: false, message: "SESSION_NOT_JOINED" });
        }

        if (session.role !== "mentor") {
          return respond(callback, { ok: false, message: "MENTOR_ONLY" });
        }

        const booking = await Booking.findById(bookingId).lean();
        if (!booking) {
          return respond(callback, { ok: false, message: "BOOKING_NOT_FOUND" });
        }

        if (booking.status !== "Confirmed") {
          return respond(callback, { ok: false, message: "SESSION_NOT_READY" });
        }

        if (String(booking.mentor) !== String(user?.id)) {
          return respond(callback, { ok: false, message: "ACCESS_DENIED" });
        }

        const admittedAt = new Date();
        const ttlSeconds = getSessionStateTtlSeconds(new Date(booking.endTime));
        try {
          await redis.setEx(getSessionAdmitKey(bookingId), ttlSeconds, "1");
        } catch {}

        await recordSessionAdmit(booking as any, admittedAt);
        
        // Get actual session start time after admission (for the timer)
        const actualStart = await getSessionActualStart(bookingId);
        const sessionStartedAt = actualStart ? actualStart.toISOString() : admittedAt.toISOString();

        const rooms = getSessionRooms(bookingId);
        const waitingSockets = await io?.in(rooms.waitingRoom).fetchSockets();
        if (waitingSockets && waitingSockets.length) {
          for (const waitingSocket of waitingSockets) {
            waitingSocket.leave(rooms.waitingRoom);
            waitingSocket.join(rooms.liveRoom);
            if (waitingSocket.data.session) {
              (waitingSocket.data.session as SessionState).live = true;
            }
            waitingSocket.emit("session:admitted", {
              bookingId,
              admittedAt: admittedAt.toISOString(),
              sessionStartedAt,
            });
          }
        }

        io?.to(rooms.liveRoom).emit("session:ready", { bookingId });
        respond(callback, { ok: true, data: { bookingId, sessionStartedAt } });
      } catch (err) {
        return respond(callback, { ok: false, message: "SESSION_ADMIT_FAILED" });
      }
    });

    socket.on("session:leave", (payload: { bookingId?: string }, callback?: SocketAck) => {
      const session = socket.data.session as SessionState | undefined;
      const bookingId = payload?.bookingId || session?.bookingId;
      if (!bookingId || !session || session.bookingId !== bookingId) {
        return respond(callback, { ok: false, message: "SESSION_NOT_JOINED" });
      }

      const rooms = getSessionRooms(bookingId);
      socket.leave(rooms.liveRoom);
      socket.leave(rooms.waitingRoom);
      socket.data.session = undefined;

      socket.to(rooms.liveRoom).emit("session:participant-left", {
        bookingId,
        userId: user?.id,
        role: session.role,
      });

      return respond(callback, { ok: true });
    });

    socket.on("session:end", async (payload: { bookingId?: string }, callback?: SocketAck) => {
      const session = socket.data.session as SessionState | undefined;
      const bookingId = payload?.bookingId || session?.bookingId;
      if (!bookingId || !session || session.bookingId !== bookingId) {
        return respond(callback, { ok: false, message: "SESSION_NOT_JOINED" });
      }

      const endReason = session.role === "mentor" ? "ended_by_mentor" : "ended_by_mentee";
      await recordSessionEnd(bookingId, endReason);

      const rooms = getSessionRooms(bookingId);
      io?.to(rooms.liveRoom).emit("session:ended", {
        bookingId,
        endedBy: session.role,
      });

      try {
        await redis.del(getSessionAdmitKey(bookingId));
      } catch {}

      return respond(callback, { ok: true });
    });

    socket.on("signal:offer", (payload: SignalPayload) => relaySignal("signal:offer", payload));
    socket.on("signal:answer", (payload: SignalPayload) => relaySignal("signal:answer", payload));
    socket.on("signal:ice", (payload: SignalPayload) => relaySignal("signal:ice", payload));

    // In-call chat handler
    socket.on("session:chat", (payload: { 
      bookingId?: string; 
      message?: string; 
      senderId?: string;
      senderName?: string;
      timestamp?: number;
    }) => {
      const session = socket.data.session as SessionState | undefined;
      const bookingId = payload?.bookingId || session?.bookingId;
      
      if (!bookingId || !session || session.bookingId !== bookingId) {
        console.log("[Session:Chat] No valid session for chat message");
        return;
      }
      
      if (!payload?.message?.trim()) {
        console.log("[Session:Chat] Empty message, ignoring");
        return;
      }
      
      const chatPayload = {
        bookingId,
        senderId: payload.senderId || user?.id || "unknown",
        senderName: payload.senderName || session.role || "User",
        message: payload.message.trim(),
        timestamp: payload.timestamp || Date.now()
      };
      
      console.log(`[Session:Chat] Relaying chat message in ${bookingId}:`, chatPayload);
      
      // Relay to other participants in the live room
      const rooms = getSessionRooms(bookingId);
      socket.to(rooms.liveRoom).emit("session:chat", chatPayload);
    });

    socket.on("session:qos", async (payload: QosPayload) => {
      const session = socket.data.session as SessionState | undefined;
      if (!session || !session.live || session.bookingId !== payload?.bookingId) return;

      const stats = payload.stats || {};
      const report = {
        timestamp: new Date(),
        rttMs: readNumber(stats.rttMs),
        jitterMs: readNumber(stats.jitterMs),
        packetLoss: readNumber(stats.packetLoss, 0, 100),
        bitrateKbps: readNumber(stats.bitrateKbps),
      };

      await recordSessionQoS(session.bookingId, session.role, report);
    });

    socket.on("disconnect", async () => {
      const session = socket.data.session as SessionState | undefined;
      if (!session) return;

      await recordSessionDisconnect(session.bookingId, session.role);

      const rooms = getSessionRooms(session.bookingId);
      socket.to(rooms.liveRoom).emit("session:participant-left", {
        bookingId: session.bookingId,
        userId: user?.id,
        role: session.role,
      });
      
      // Handle user going offline
      if (user?.id) {
        const presenceKey = `presence:user:${user.id}`;
        // Delete presence key to mark user as offline
        redis.del(presenceKey).catch((err) => {
          console.error("Failed to delete user presence:", err);
        });
        
        // Notify other users that this user is offline
        socket.broadcast.emit("user:offline", { userId: user.id });
      }
    });
  });

  try {
    await setupRedisAdapter(io);
  } catch (err) {
    io.close();
    io = null;
    throw err;
  }

  return io;
}

export function emitToUser(userId: string, event: string, payload: unknown) {
  if (!io) return;
  io.to(`user:${userId}`).emit(event, payload);
}

export async function closeSocket() {
  if (io) {
    io.close();
    io = null;
  }
  if (pubClient?.isOpen) {
    await pubClient.quit();
    pubClient = null;
  }
  if (subClient?.isOpen) {
    await subClient.quit();
    subClient = null;
  }
}
