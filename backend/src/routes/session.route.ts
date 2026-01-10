import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { createJoinToken, getSessionLog, checkNoShow, checkAllNoShows } from '../controllers/session.controller';
import { sessionBookingIdRules } from '../middlewares/validators/session.validator';

const router = Router();

// Create a short-lived join token for WebRTC sessions
router.post('/:bookingId/join-token', auth, sessionBookingIdRules, validate, createJoinToken);

// Read session log for a booking
router.get('/:bookingId/log', auth, sessionBookingIdRules, validate, getSessionLog);

// Check and process no-show for a specific booking (admin/system only)
router.post('/:bookingId/check-no-show', auth, requireRoles('admin', 'root'), sessionBookingIdRules, validate, checkNoShow);

// Batch check all confirmed bookings for no-show (admin/system only)
router.post('/check-all-no-shows', auth, requireRoles('admin', 'root'), checkAllNoShows);

export default router;
