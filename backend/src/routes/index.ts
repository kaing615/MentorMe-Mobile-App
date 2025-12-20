// path: src/routes/index.ts
import { Router } from 'express';
import userRouter from './user.route';
import profileRouter from './profile.route';
import availabilityRouter from './availability.route';
import mentorRouter from './mentor.route';
import bookingRouter from './booking.route';

const router = Router();

router.use('/auth', userRouter);
router.use('/profile', profileRouter);
router.use('/availability', availabilityRouter);
router.use('/mentors', mentorRouter);
router.use('/', bookingRouter);

export default router;
