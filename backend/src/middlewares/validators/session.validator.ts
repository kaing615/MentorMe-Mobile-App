import { param } from 'express-validator';

export const sessionBookingIdRules = [
  param('bookingId').isMongoId().withMessage('bookingId must be a valid booking ID'),
];

export default {
  sessionBookingIdRules,
};
