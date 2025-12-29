import { Router } from 'express';
import { auth } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { getMessages, sendMessage } from '../controllers/message.controller';
import { messageBookingIdRules, sendMessageRules } from '../middlewares/validators/message.validator';

const router = Router();

// List messages for a booking
router.get('/:bookingId', auth, messageBookingIdRules, validate, getMessages);

// Send a message
router.post('/', auth, sendMessageRules, validate, sendMessage);

export default router;
