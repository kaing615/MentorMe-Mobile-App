import { Router } from 'express';
import { getChatRestrictionInfo, getMessages, sendMessage } from '../controllers/message.controller';
import { validate } from '../handlers/request.handler';
import { auth } from '../middlewares/auth.middleware';
import { messageBookingIdRules, sendMessageRules } from '../middlewares/validators/message.validator';

const router = Router();

// Get chat restriction info for a booking (must be before /:bookingId)
router.get('/:bookingId/restriction-info', auth, messageBookingIdRules, validate, getChatRestrictionInfo);

// List messages for a booking
router.get('/:bookingId', auth, messageBookingIdRules, validate, getMessages);

// Send a message
router.post('/', auth, sendMessageRules, validate, sendMessage);

export default router;
