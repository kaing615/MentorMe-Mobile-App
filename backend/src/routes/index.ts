// path: src/routes/index.ts
import { Router } from 'express';
import availabilityRouter from './availability.route';
import bookingRouter from './booking.route';
import mentorRouter from './mentor.route';
import notificationRouter from './notification.route';
import walletRouter from './wallet.route';
import paymentRouter from './payment.route';
import profileRouter from './profile.route';
import reportRouter from './report.route';
import userRouter from './user.route';

const router = Router();

router.use('/auth', userRouter);
router.use('/users', userRouter);
router.use('/bookings', bookingRouter);
router.use('/reports', reportRouter);
router.use('/profile', profileRouter);
router.use('/availability', availabilityRouter);
router.use('/mentors', mentorRouter);
router.use('/wallet', walletRouter);
router.use('/bookings', bookingRouter);
router.use('/notifications', notificationRouter);
router.use('/payments', paymentRouter);

export default router;
