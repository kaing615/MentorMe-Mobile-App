import { Request, Response } from "express";
import mongoose from "mongoose";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Booking from "../models/booking.model";
import Wallet from "../models/wallet.model";
import Review from "../models/review.model";

const { ok, forbidden } = responseHandler;

function getUserId(req: Request): string | null {
  return ((req as any).user?.id ?? (req as any).user?._id ?? null) as string | null;
}

/**
 * GET /mentors/me/stats
 * Lấy thống kê của mentor trong tháng hiện tại:
 * - Thu nhập (earnings) từ wallet
 * - Số lượng mentee unique
 * - Đánh giá trung bình
 * - Tổng số giờ tư vấn
 */
export const getMyStats = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  // Tính thời gian đầu và cuối tháng hiện tại
  const now = new Date();
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999);

  // 1. Thu nhập: Lấy từ wallet balance
  const wallet = await Wallet.findOne({ userId: mentorId });
  const earnings = wallet?.balanceMinor ?? 0;

  // 2. Số mentee unique trong tháng này
  const bookingsThisMonth = await Booking.find({
    mentor: new mongoose.Types.ObjectId(mentorId),
    status: { $in: ["Confirmed", "Completed"] },
    createdAt: { $gte: startOfMonth, $lte: endOfMonth }
  });

  const uniqueMentees = new Set(
    bookingsThisMonth.map(b => b.mentee.toString())
  );
  const menteeCount = uniqueMentees.size;

  // 3. Đánh giá trung bình trong tháng này
  const reviewsThisMonth = await Review.find({
    mentor: new mongoose.Types.ObjectId(mentorId),
    createdAt: { $gte: startOfMonth, $lte: endOfMonth }
  });

  const averageRating = reviewsThisMonth.length > 0
    ? reviewsThisMonth.reduce((sum, r) => sum + r.rating, 0) / reviewsThisMonth.length
    : 0;

  // 4. Tổng số giờ tư vấn trong tháng này
  // Tính từ startTime và endTime (Date objects)
  let totalHours = 0;
  for (const booking of bookingsThisMonth) {
    if (booking.startTime && booking.endTime) {
      const durationMs = booking.endTime.getTime() - booking.startTime.getTime();
      const durationHours = durationMs / (1000 * 60 * 60); // Convert ms to hours
      totalHours += durationHours;
    }
  }

  return ok(res, {
    earnings,
    menteeCount,
    averageRating: Math.round(averageRating * 10) / 10, // Round to 1 decimal
    totalHours: Math.round(totalHours * 10) / 10
  });
});

export default {
  getMyStats
};

