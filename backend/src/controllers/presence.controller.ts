// filepath: backend/src/controllers/presence.controller.ts
import { Response } from "express";
import { AuthRequest } from "../middlewares/auth.middleware";
import redis from "../utils/redis";

/**
 * POST /api/v1/presence/ping
 * Auth required - Update user presence in Redis
 * Sets key with 120s TTL to track online users
 */
export async function pingPresence(req: AuthRequest, res: Response) {
  try {
    const userId = req.user?.id;
    if (!userId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const key = `presence:user:${userId}`;
    const lastSeenKey = `presence:lastSeen:${userId}`;
    const ttl = 120; // 2 minutes

    // Set presence key with TTL
    await redis.setEx(key, ttl, "1");
    
    // Update last seen timestamp (persistent, no TTL)
    const now = new Date().toISOString();
    await redis.set(lastSeenKey, now);

    res.json({
      success: true,
      data: {
        userId,
        expiresIn: ttl,
      },
    });
  } catch (error: any) {
    console.error("Error pinging presence:", error);
    res.status(500).json({
      success: false,
      message: "Failed to update presence",
      error: error.message,
    });
  }
}

/**
 * POST /api/v1/presence/lookup
 * Auth required - Lookup online status for a list of user IDs
 */
export async function lookupPresence(req: AuthRequest, res: Response) {
  try {
    const requesterId = req.user?.id;
    if (!requesterId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const rawIds = Array.isArray((req.body as any)?.userIds)
      ? (req.body as any).userIds
      : [];
    const normalized = Array.from(
      new Set(
        rawIds
          .map((value: unknown) => String(value).trim())
          .filter((value: string) => value.length > 0)
      )
    ).slice(0, 200);

    if (normalized.length === 0) {
      return res.json({
        success: true,
        data: {
          onlineUserIds: [],
        },
      });
    }

    const keys = normalized.map((id) => `presence:user:${id}`);
    const values = await redis.mGet(keys);
    const onlineUserIds = normalized.filter((id, index) => values[index] != null);

    res.json({
      success: true,
      data: {
        onlineUserIds,
      },
    });
  } catch (error: any) {
    console.error("Error looking up presence:", error);
    res.status(500).json({
      success: false,
      message: "Failed to lookup presence",
      error: error.message,
    });
  }
}

/**
 * POST /api/v1/presence/lastSeen
 * Auth required - Get last seen time for a list of user IDs
 */
export async function getLastSeen(req: AuthRequest, res: Response) {
  try {
    const requesterId = req.user?.id;
    if (!requesterId) {
      return res.status(401).json({
        success: false,
        message: "Unauthorized",
      });
    }

    const rawIds = Array.isArray((req.body as any)?.userIds)
      ? (req.body as any).userIds
      : [];
    const normalized = Array.from(
      new Set(
        rawIds
          .map((value: unknown) => String(value).trim())
          .filter((value: string) => value.length > 0)
      )
    ).slice(0, 200);

    if (normalized.length === 0) {
      return res.json({
        success: true,
        data: {},
      });
    }

    const keys = normalized.map((id) => `presence:lastSeen:${id}`);
    const values = await redis.mGet(keys);
    
    const lastSeenMap: Record<string, string | null> = {};
    for (let i = 0; i < normalized.length; i++) {
      const id = String(normalized[i]);
      const value = values[i];
      lastSeenMap[id] = value ? String(value) : null;
    }

    res.json({
      success: true,
      data: lastSeenMap,
    });
  } catch (error: any) {
    console.error("Error getting last seen:", error);
    res.status(500).json({
      success: false,
      message: "Failed to get last seen",
      error: error.message,
    });
  }
}
