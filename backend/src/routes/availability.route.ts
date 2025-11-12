// path: src/routes/availability.route.ts
import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { createSlot, publishSlot, publishBatch, getPublicCalendar, updateSlot, deleteSlot } from '../controllers/availability.controller';
import { createSlotRules, publishSlotRules, calendarQueryRules, updateSlotRules } from '../middlewares/validators/slot.validator';

const router = Router();

// Create (draft)
router.post('/slots', auth, requireRoles('mentor'), createSlotRules, validate, createSlot);

// Publish (one-off, chưa RRULE)
router.post('/slots/:id/publish', auth, requireRoles('mentor'), publishSlotRules, validate, publishSlot);

// Publish batch (sequential or concurrent)
router.post('/slots/publish-batch', auth, requireRoles('mentor'), publishBatch);

// Update slot meta + pause/resume
router.patch('/slots/:id', auth, requireRoles('mentor'), updateSlotRules, validate, updateSlot);

// Delete slot (hard delete if no future booked)
router.delete('/slots/:id', auth, requireRoles('mentor'), publishSlotRules, validate, deleteSlot);

// Public calendar (read-only)
router.get('/calendar/:mentorId', calendarQueryRules, validate, getPublicCalendar);

export default router;
