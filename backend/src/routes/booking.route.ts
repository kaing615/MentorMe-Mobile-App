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
import { requireAuth } from "../middlewares/auth.middleware";

const router = Router();

// Protected booking routes (require authentication)
router.post("/bookings", requireAuth, createBooking);
router.get("/bookings", requireAuth, getBookings);
router.get("/bookings/:id", requireAuth, getBookingById);
router.post("/bookings/:id/cancel", requireAuth, cancelBooking);
router.post("/bookings/:id/resend-ics", requireAuth, resendICS);

// Payment webhook (no auth required, should be secured with signature verification in production)
router.post("/payments/webhook", handlePaymentWebhook);

// Admin/cron route for releasing expired bookings
router.post("/bookings/release-expired", releaseExpiredBookings);

export default router;
