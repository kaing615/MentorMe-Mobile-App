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

/**
 * GET /mentors/me/stats/weekly
 * Lấy thu nhập theo từng ngày trong tuần hiện tại (7 ngày gần nhất)
 * Trả về mảng 7 phần tử tương ứng với 7 ngày (từ thứ 2 đến chủ nhật)
 */
export const getWeeklyEarnings = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  const now = new Date();
  const startOfWeek = new Date(now);
  startOfWeek.setDate(now.getDate() - now.getDay() + 1); // Monday
  startOfWeek.setHours(0, 0, 0, 0);

  // Tạo mảng 7 ngày
  const dailyEarnings: number[] = [];

  for (let i = 0; i < 7; i++) {
    const dayStart = new Date(startOfWeek);
    dayStart.setDate(startOfWeek.getDate() + i);
    dayStart.setHours(0, 0, 0, 0);

    const dayEnd = new Date(dayStart);
    dayEnd.setHours(23, 59, 59, 999);

    // Lấy các booking đã hoàn thành trong ngày này
    const bookingsInDay = await Booking.find({
      mentor: new mongoose.Types.ObjectId(mentorId),
      status: "Completed",
      updatedAt: { $gte: dayStart, $lte: dayEnd } // Dùng updatedAt vì đó là lúc booking completed
    });

    // Tính tổng thu nhập trong ngày
    const dailyTotal = bookingsInDay.reduce((sum, booking) => {
      return sum + (booking.priceMinor ?? 0);
    }, 0);

    dailyEarnings.push(dailyTotal);
  }

  return ok(res, {
    weeklyEarnings: dailyEarnings // Array of 7 numbers (Mon-Sun)
  });
});

/**
 * GET /mentors/me/stats/yearly
 * Lấy thu nhập theo từng tháng trong năm hiện tại (12 tháng)
 * Trả về mảng 12 phần tử tương ứng với 12 tháng (từ tháng 1 đến tháng 12)
 */
export const getYearlyEarnings = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  const now = new Date();
  const currentYear = now.getFullYear();

  // Tạo mảng 12 tháng
  const monthlyEarnings: number[] = [];

  for (let month = 0; month < 12; month++) {
    const monthStart = new Date(currentYear, month, 1, 0, 0, 0, 0);
    const monthEnd = new Date(currentYear, month + 1, 0, 23, 59, 59, 999);

    // Lấy các booking đã hoàn thành trong tháng này
    const bookingsInMonth = await Booking.find({
      mentor: new mongoose.Types.ObjectId(mentorId),
      status: "Completed",
      updatedAt: { $gte: monthStart, $lte: monthEnd }
    });

    // Tính tổng thu nhập trong tháng
    const monthlyTotal = bookingsInMonth.reduce((sum, booking) => {
      return sum + (booking.priceMinor ?? 0);
    }, 0);

    monthlyEarnings.push(monthlyTotal);
  }

  return ok(res, {
    yearlyEarnings: monthlyEarnings, // Array of 12 numbers (Jan-Dec)
    year: currentYear
  });
});

/**
 * GET /mentors/me/stats/overall
 * Lấy thống kê tổng quan của mentor (toàn bộ thời gian, không giới hạn tháng)
 * - Đánh giá trung bình (tất cả)
 * - Tổng số mentee unique (tất cả)
 * - Tổng số giờ tư vấn (tất cả)
 */
export const getOverallStats = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  // 1. Tất cả bookings đã confirmed hoặc completed
  const allBookings = await Booking.find({
    mentor: new mongoose.Types.ObjectId(mentorId),
    status: { $in: ["Confirmed", "Completed"] }
  });

  // 2. Số mentee unique
  const uniqueMentees = new Set(
    allBookings.map(b => b.mentee.toString())
  );
  const totalMentees = uniqueMentees.size;

  // 3. Đánh giá trung bình (tất cả reviews)
  const allReviews = await Review.find({
    mentor: new mongoose.Types.ObjectId(mentorId)
  });

  const averageRating = allReviews.length > 0
    ? allReviews.reduce((sum, r) => sum + r.rating, 0) / allReviews.length
    : 0;

  // 4. Tổng số giờ tư vấn
  let totalHours = 0;
  for (const booking of allBookings) {
    if (booking.startTime && booking.endTime) {
      const durationMs = booking.endTime.getTime() - booking.startTime.getTime();
      const durationHours = durationMs / (1000 * 60 * 60);
      totalHours += durationHours;
    }
  }

  return ok(res, {
    averageRating: Math.round(averageRating * 10) / 10,
    totalMentees,
    totalHours: Math.round(totalHours * 10) / 10
  });
});

export default {
  getMyStats,
  getWeeklyEarnings,
  getYearlyEarnings,
  getOverallStats
};

