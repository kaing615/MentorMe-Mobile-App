import { Router } from "express";
import {
  createBooking,
  getBookings,
  getBookingById,
  cancelBooking,
  resendICS,
  handlePaymentWebhook,
  releaseExpiredBookings,
} from "../controllers/booking.controller";
import { auth } from "../middlewares/auth.middleware";

const router = Router();

// Protected booking routes (require authentication)
router.post("/bookings", auth, createBooking);
router.get("/bookings", auth, getBookings);
router.get("/bookings/:id", auth, getBookingById);
router.post("/bookings/:id/cancel", auth, cancelBooking);
router.post("/bookings/:id/resend-ics", auth, resendICS);

// Payment webhook (no auth required, should be secured with signature verification in production)
// TODO: Add webhook signature verification for production security
router.post("/payments/webhook", handlePaymentWebhook);

// Admin/cron route for releasing expired bookings
// TODO: Restrict to admin users or secure with proper authentication in production
router.post("/bookings/release-expired", releaseExpiredBookings);

export default router;
