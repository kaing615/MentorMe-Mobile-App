import { body, param, query } from 'express-validator';

export const createReviewRules = [
  param('id').isMongoId().withMessage('Booking ID must be a valid ID'),
  body('rating')
    .isInt({ min: 1, max: 5 })
    .toInt()
    .withMessage('Rating must be an integer between 1 and 5'),
  body('comment')
    .optional()
    .isString()
    .trim()
    .isLength({ max: 1000 })
    .withMessage('Comment must be at most 1000 characters'),
];

export const getMentorReviewsRules = [
  param('id').isMongoId().withMessage('Mentor ID must be a valid ID'),
  query('limit').optional().isInt({ min: 1, max: 100 }).toInt(),
  query('cursor').optional().isMongoId().withMessage('Cursor must be a valid ID'),
];

export const getMyReviewsRules = [
  query('limit').optional().isInt({ min: 1, max: 100 }).toInt(),
  query('cursor').optional().isMongoId().withMessage('Cursor must be a valid ID'),
];
