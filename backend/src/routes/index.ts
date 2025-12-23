// path: src/routes/index.ts
import { Router } from 'express';
import userRouter from './user.route';
import profileRouter from './profile.route';
import availabilityRouter from './availability.route';
import mentorRouter from './mentor.route';
import walletRouter from './wallet.route';
import payoutRouter from './payout.route';
import adminPayoutRouter from './adminPayout.route';
import webhookRouter from './webhook.route';

const router = Router();

router.use('/auth', userRouter);
router.use('/profile', profileRouter);
router.use('/availability', availabilityRouter);
router.use('/mentors', mentorRouter);
router.use('/wallet', walletRouter);
router.use('/payouts', payoutRouter);
router.use('/admin/payouts', adminPayoutRouter);
router.use('/webhooks', webhookRouter);

export default router;
