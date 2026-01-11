import { Request, Response } from "express";
import mongoose from "mongoose";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Booking from "../models/booking.model";
import Review from "../models/review.model";
import WalletTransaction from "../models/walletTransaction.model";

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

  // 1. Thu nhập: Tính từ WalletTransaction với source BOOKING_EARN trong tháng này
  const earningTransactions = await WalletTransaction.find({
    userId: new mongoose.Types.ObjectId(mentorId),
    source: "BOOKING_EARN",
    createdAt: { $gte: startOfMonth, $lte: endOfMonth }
  });

  const penaltyTransactions = await WalletTransaction.find({
    userId: new mongoose.Types.ObjectId(mentorId),
    source: "NO_SHOW_PENALTY",
    createdAt: { $gte: startOfMonth, $lte: endOfMonth }
  });

  const totalEarnings = earningTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);
  const totalPenalties = penaltyTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);
  const earnings = totalEarnings - totalPenalties;

  console.log(`📊 [getMyStats] Mentor ${mentorId}:`);
  console.log(`  - Found ${earningTransactions.length} BOOKING_EARN transactions: ${totalEarnings}`);
  console.log(`  - Found ${penaltyTransactions.length} NO_SHOW_PENALTY transactions: ${totalPenalties}`);
  console.log(`  - Net earnings: ${earnings} (${earnings / 1000} VND)`);
  console.log(`  - Period: ${startOfMonth.toISOString()} to ${endOfMonth.toISOString()}`);

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
 * Lấy thu nhập theo từng ngày trong 7 ngày gần nhất
 * Trả về mảng 7 phần tử tương ứng với 7 ngày gần nhất
 *
 * ✅ UPDATED: Đổi từ tuần lịch sang 7 ngày gần nhất
 */
export const getWeeklyEarnings = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  const now = new Date();
  
  // Thay vì tính từ thứ 2, tính từ 7 ngày trước
  const sevenDaysAgo = new Date(now);
  sevenDaysAgo.setDate(now.getDate() - 6); // 6 ngày trước + hôm nay = 7 ngày
  sevenDaysAgo.setHours(0, 0, 0, 0);

  console.log('📊 [getWeeklyEarnings] Last 7 days:', sevenDaysAgo.toISOString().split('T')[0], 'to', now.toISOString().split('T')[0]);

  // Tạo mảng 7 ngày
  const dailyEarnings: number[] = [];

  for (let i = 0; i < 7; i++) {
    const dayStart = new Date(sevenDaysAgo);
    dayStart.setDate(sevenDaysAgo.getDate() + i);
    dayStart.setHours(0, 0, 0, 0);

    const dayEnd = new Date(dayStart);
    dayEnd.setHours(23, 59, 59, 999);

    // Tính từ WalletTransaction với source BOOKING_EARN trong ngày này
    const earningTransactions = await WalletTransaction.find({
      userId: new mongoose.Types.ObjectId(mentorId),
      source: "BOOKING_EARN",
      createdAt: { $gte: dayStart, $lte: dayEnd }
    });

    const dailyTotal = earningTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);

    console.log(`📊 Day ${i + 1}:`, dayStart.toISOString().split('T')[0], '- earnings:', dailyTotal, 'from', earningTransactions.length, 'transactions');
    dailyEarnings.push(dailyTotal);
  }

  console.log('📊 [getWeeklyEarnings] Result:', dailyEarnings);

  return ok(res, {
    weeklyEarnings: dailyEarnings // Array of 7 numbers (last 7 days)
  });
});

/**
 * GET /mentors/me/stats/yearly
 * Lấy thu nhập theo từng tháng trong năm hiện tại (12 tháng)
 * Trả về mảng 12 phần tử tương ứng với 12 tháng (từ tháng 1 đến tháng 12)
 *
 * ✅ UPDATED: Tính từ booking Confirmed/Completed đã qua endTime (đã diễn ra xong)
 */
export const getYearlyEarnings = asyncHandler(async (req: Request, res: Response) => {
  const mentorId = getUserId(req);
  if (!mentorId) return forbidden(res, "Unauthorized");

  const now = new Date();
  const currentYear = now.getFullYear();

  console.log('📊 [getYearlyEarnings] Year:', currentYear);

  // Tạo mảng 12 tháng
  const monthlyEarnings: number[] = [];

  for (let month = 0; month < 12; month++) {
    const monthStart = new Date(currentYear, month, 1, 0, 0, 0, 0);
    const monthEnd = new Date(currentYear, month + 1, 0, 23, 59, 59, 999);

    // ✅ FIXED: Tính từ WalletTransaction (BOOKING_EARN - NO_SHOW_PENALTY) trong tháng này
    const earningTransactions = await WalletTransaction.find({
      userId: new mongoose.Types.ObjectId(mentorId),
      source: "BOOKING_EARN",
      createdAt: { $gte: monthStart, $lte: monthEnd }
    });

    const penaltyTransactions = await WalletTransaction.find({
      userId: new mongoose.Types.ObjectId(mentorId),
      source: "NO_SHOW_PENALTY",
      createdAt: { $gte: monthStart, $lte: monthEnd }
    });

    const totalEarnings = earningTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);
    const totalPenalties = penaltyTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);
    const monthlyTotal = totalEarnings - totalPenalties;

    console.log(`📊 Month ${month + 1}:`, monthlyTotal, '(', totalEarnings, '-', totalPenalties, ') from', earningTransactions.length, '+', penaltyTransactions.length, 'transactions');
    monthlyEarnings.push(monthlyTotal);
  }

  console.log('📊 [getYearlyEarnings] Result:', monthlyEarnings);

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

  // 5. Tổng thu nhập: Tính từ tất cả WalletTransaction với source BOOKING_EARN
  const allEarningTransactions = await WalletTransaction.find({
    userId: new mongoose.Types.ObjectId(mentorId),
    source: "BOOKING_EARN"
  });

  const totalEarnings = allEarningTransactions.reduce((sum, tx) => sum + tx.amountMinor, 0);

  console.log(`📊 [getOverallStats] Mentor ${mentorId}:`);
  console.log(`  - Total earnings: ${totalEarnings} (${totalEarnings / 1000} VND)`);
  console.log(`  - Total mentees: ${totalMentees}`);
  console.log(`  - Average rating: ${averageRating}`);
  console.log(`  - Total hours: ${totalHours}`);

  return ok(res, {
    averageRating: Math.round(averageRating * 10) / 10,
    totalMentees,
    totalHours: Math.round(totalHours * 10) / 10,
    totalEarnings
  });
});

export default {
  getMyStats,
  getWeeklyEarnings,
  getYearlyEarnings,
  getOverallStats
};

