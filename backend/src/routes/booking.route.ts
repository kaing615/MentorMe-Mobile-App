// path: src/routes/booking.route.ts
import { Router } from 'express';
import { auth } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import {
  createBooking,
  getBookings,
  getBookingById,
  cancelBooking,
  resendIcs,
  mentorConfirmBooking,
  mentorDeclineBooking,
} from '../controllers/booking.controller';
import {
  createBookingRules,
  getBookingsRules,
  bookingIdRules,
  cancelBookingRules,
  resendIcsRules,
  mentorConfirmRules,
  mentorDeclineRules,
} from '../middlewares/validators/booking.validator';

const router = Router();

// Create booking (mentee only in practice)
router.post('/', auth, createBookingRules, validate, createBooking);

// List bookings for current user
router.get('/', auth, getBookingsRules, validate, getBookings);

// Get booking details
router.get('/:id', auth, bookingIdRules, validate, getBookingById);

// Cancel booking
router.post('/:id/cancel', auth, cancelBookingRules, validate, cancelBooking);

// Resend ICS calendar file
router.post('/:id/resend-ics', auth, resendIcsRules, validate, resendIcs);

// Mentor confirm/decline pending booking
router.post('/:id/mentor-confirm', auth, mentorConfirmRules, validate, mentorConfirmBooking);
router.post('/:id/mentor-decline', auth, mentorDeclineRules, validate, mentorDeclineBooking);

export default router;
