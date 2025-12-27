// filepath: backend/src/controllers/home.controller.ts
import { Request, Response } from "express";
import User from "../models/user.model";
import Booking from "../models/booking.model";
import Profile from "../models/profile.model";
import redis from "../utils/redis";

/**
 * GET /api/v1/home/stats
 * Public endpoint to get app statistics for HomeScreen
 */
export async function getHomeStats(req: Request, res: Response) {
  try {
    // 1. Count mentors (users with role=mentor)
    const mentorCount = await User.countDocuments({ role: "mentor" });

    // 2. Count sessions (bookings with status Confirmed or Completed)
    const sessionCount = await Booking.countDocuments({
      status: { $in: ["Confirmed", "Completed"] },
    });

    // 3. Average rating from Profile.rating.average
    const profiles = await Profile.find(
      { "rating.average": { $exists: true, $gt: 0 } },
      { "rating.average": 1, "rating.count": 1 }
    ).lean();

    let avgRating = 0;
    if (profiles.length > 0) {
      const totalRating = profiles.reduce((sum, p) => sum + (p.rating?.average || 0), 0);
      avgRating = totalRating / profiles.length;
    }

    // 4. Online count from Redis presence keys
    let onlineCount = 0;
    try {
      const keys = await redis.keys("presence:user:*");
      onlineCount = keys.length;
    } catch (err) {
      console.error("Failed to get online count from Redis:", err);
      // Non-blocking, continue with onlineCount = 0
    }

    res.json({
      success: true,
      data: {
        mentorCount,
        sessionCount,
        avgRating: parseFloat(avgRating.toFixed(1)),
        onlineCount,
      },
    });
  } catch (error: any) {
    console.error("Error getting home stats:", error);
    res.status(500).json({
      success: false,
      message: "Failed to get home statistics",
      error: error.message,
    });
  }
}

