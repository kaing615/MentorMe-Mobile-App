import { Router } from 'express';
import { auth } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { createJoinToken, getSessionLog } from '../controllers/session.controller';
import { sessionBookingIdRules } from '../middlewares/validators/session.validator';

const router = Router();

// Create a short-lived join token for WebRTC sessions
router.post('/:bookingId/join-token', auth, sessionBookingIdRules, validate, createJoinToken);

// Read session log for a booking
router.get('/:bookingId/log', auth, sessionBookingIdRules, validate, getSessionLog);

export default router;
