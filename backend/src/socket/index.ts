import type { Server as HttpServer } from "http";
import { createHash } from "crypto";
import jwt from "jsonwebtoken";
import { Server as SocketIOServer, Socket } from "socket.io";
import { createAdapter } from "@socket.io/redis-adapter";
import { createClient } from "redis";
import redis from "../utils/redis";
import User from "../models/user.model";

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
    }
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
