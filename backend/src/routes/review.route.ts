import { Router } from 'express';
import { auth } from '../middlewares/auth.middleware';
import {
  createReview,
  getMentorReviews,
  getMyReviews,
} from '../controllers/review.controller';
import {
  createReviewRules,
  getMentorReviewsRules,
  getMyReviewsRules,
} from '../middlewares/validators/review.validator';
import { validate } from '../handlers/request.handler';

const router = Router();

// POST /bookings/:id/review - Create review for a booking (mentee only)
router.post(
  '/bookings/:id/review',
  auth,
  createReviewRules,
  validate,
  createReview
);

// GET /mentors/:id/reviews - Get all reviews for a mentor (public)
router.get(
  '/mentors/:id/reviews',
  getMentorReviewsRules,
  validate,
  getMentorReviews
);

// GET /reviews/me - Get my reviews (mentee)
router.get(
  '/reviews/me',
  auth,
  getMyReviewsRules,
  validate,
  getMyReviews
);

export default router;
