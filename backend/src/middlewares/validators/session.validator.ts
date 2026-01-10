import { param, query } from 'express-validator';

export const sessionBookingIdRules = [
  param('bookingId').isMongoId().withMessage('bookingId must be a valid booking ID'),
];

export const adminSessionListRules = [
  query('status')
    .optional()
    .isIn(['waiting', 'active', 'ended', 'no_show'])
    .withMessage('invalid status'),
  query('bookingId').optional().isMongoId().withMessage('bookingId must be a valid booking ID'),
  query('mentorId').optional().isMongoId().withMessage('mentorId must be a valid ID'),
  query('menteeId').optional().isMongoId().withMessage('menteeId must be a valid ID'),
  query('from').optional().isISO8601().withMessage('from must be a valid ISO date'),
  query('to').optional().isISO8601().withMessage('to must be a valid ISO date'),
  query('page').optional().isInt({ min: 1 }).toInt(),
  query('limit').optional().isInt({ min: 1, max: 100 }).toInt(),
];

export default {
  sessionBookingIdRules,
  adminSessionListRules,
};
