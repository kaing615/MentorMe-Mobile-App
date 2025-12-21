import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";

export const getAllBookings = asyncHandler(async (req: Request, res: Response) => {
  const { filter = '{}', range = '[0,9]', sort = '["startTime","DESC"]' } = req.query;
  
  // Tạm thời trả về mảng rỗng vì chưa có model Booking
  const bookings: any[] = [];
  const total = 0;
  
  const [start, end] = JSON.parse(range as string);
  
  res.set('Content-Range', `bookings ${start}-${end}/${total}`);
  res.set('Access-Control-Expose-Headers', 'Content-Range');
  return res.json(bookings);
});

export const getBookingById = asyncHandler(async (req: Request, res: Response) => {
  return res.json({ id: req.params.id, status: "pending" });
});

export const updateBooking = asyncHandler(async (req: Request, res: Response) => {
  return res.json({ id: req.params.id, ...req.body });
});

export default {
  getAllBookings,
  getBookingById,
  updateBooking,
};
