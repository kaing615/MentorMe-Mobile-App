// path: src/routes/index.ts
import { Router } from 'express';
import userRouter from './user.route';
import profileRouter from './profile.route';
import availabilityRouter from './availability.route';
import mentorRouter from './mentor.route';
import bookingRouter from './booking.route';
import paymentRouter from './payment.route';

const router = Router();

router.use('/auth', userRouter);
router.use('/profile', profileRouter);
router.use('/availability', availabilityRouter);
router.use('/mentors', mentorRouter);
router.use('/bookings', bookingRouter);
router.use('/payments', paymentRouter);

export default router;
