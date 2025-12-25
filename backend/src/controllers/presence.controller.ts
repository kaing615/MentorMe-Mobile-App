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
    const ttl = 120; // 2 minutes

    // Set presence key with TTL
    await redis.setEx(key, ttl, "1");

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

