import { Router } from "express";
import bookingController from "../controllers/booking.controller";

const router = Router();

router.get("/", bookingController.getAllBookings);
router.get("/:id", bookingController.getBookingById);
router.put("/:id", bookingController.updateBooking);

export default router;
