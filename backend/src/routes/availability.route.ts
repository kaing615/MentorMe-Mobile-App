// path: src/routes/availability.route.ts
import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { createSlot, publishSlot, publishBatch, getPublicCalendar } from '../controllers/availability.controller';
import { createSlotRules, publishSlotRules, calendarQueryRules } from '../middlewares/validators/slot.validator';

const router = Router();

// Create (draft)
router.post('/slots', auth, requireRoles('mentor'), createSlotRules, validate, createSlot);

// Publish (one-off, chưa RRULE)
router.post('/slots/:id/publish', auth, requireRoles('mentor'), publishSlotRules, validate, publishSlot);

// Publish batch (sequential or concurrent)
router.post('/slots/publish-batch', auth, requireRoles('mentor'), publishBatch);

// Public calendar (read-only)
router.get('/calendar/:mentorId', calendarQueryRules, validate, getPublicCalendar);

export default router;
