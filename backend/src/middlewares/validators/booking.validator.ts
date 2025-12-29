// path: src/middlewares/validators/booking.validator.ts
import { body, param, query } from 'express-validator';

export const createBookingRules = [
  body('mentorId').isMongoId().withMessage('mentorId must be a valid ID'),
  body('occurrenceId').isMongoId().withMessage('occurrenceId must be a valid ID'),
  body('topic').optional().isString().trim().isLength({ max: 200 }).withMessage('topic must be at most 200 characters'),
  body('notes').optional().isString().trim().isLength({ max: 1000 }).withMessage('notes must be at most 1000 characters'),
];

export const getBookingsRules = [
  query('role').optional().isIn(['mentee', 'mentor']).withMessage('role must be mentee or mentor'),
  query('status')
    .optional()
    .isIn([
      'PaymentPending',
      'PendingMentor',
      'Confirmed',
      'Failed',
      'Cancelled',
      'Declined',
      'Completed',
      'NoShowMentor',
      'NoShowMentee',
      'NoShowBoth',
    ])
    .withMessage('invalid status'),
  query('page').optional().isInt({ min: 1 }).toInt(),
  query('limit').optional().isInt({ min: 1, max: 100 }).toInt(),
];

export const bookingIdRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
];

export const cancelBookingRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
  body('reason').optional().isString().trim().isLength({ max: 500 }).withMessage('reason must be at most 500 characters'),
];

export const resendIcsRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
];

export const mentorConfirmRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
];

export const mentorDeclineRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
  body('reason').optional().isString().trim().isLength({ max: 500 }).withMessage('reason must be at most 500 characters'),
];

export const completeBookingRules = [
  param('id').isMongoId().withMessage('id must be a valid booking ID'),
];
