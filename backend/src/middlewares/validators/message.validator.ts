import { body, param, query } from 'express-validator';

export const messageBookingIdRules = [
  param('bookingId').isMongoId().withMessage('bookingId must be a valid booking ID'),
  query('limit').optional().isInt({ min: 1, max: 200 }).toInt(),
  query('before').optional().isISO8601().withMessage('before must be ISO-8601 date'),
];

export const sendMessageRules = [
  body('bookingId').isMongoId().withMessage('bookingId must be a valid booking ID'),
  body('content')
    .isString()
    .trim()
    .isLength({ min: 1, max: 2000 })
    .withMessage('content must be 1-2000 characters'),
  body('messageType')
    .optional()
    .isIn(['text', 'image', 'file'])
    .withMessage('messageType must be text, image, or file'),
];

export default {
  messageBookingIdRules,
  sendMessageRules,
};
