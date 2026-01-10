import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import {
  adminGetBookingsRules,
  adminUpdateBookingRules,
  bookingIdRules,
} from '../middlewares/validators/booking.validator';
import {
  adminGetBookingById,
  adminListBookings,
  adminUpdateBooking,
} from '../controllers/booking.controller';

const router = Router();

router.get(
  '/',
  auth,
  requireRoles('admin', 'root'),
  adminGetBookingsRules,
  validate,
  adminListBookings
);

router.get(
  '/:id',
  auth,
  requireRoles('admin', 'root'),
  bookingIdRules,
  validate,
  adminGetBookingById
);

router.put(
  '/:id',
  auth,
  requireRoles('admin', 'root'),
  bookingIdRules,
  adminUpdateBookingRules,
  validate,
  adminUpdateBooking
);

export default router;
