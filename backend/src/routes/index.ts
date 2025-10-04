// path: src/routes/index.ts
import { Router } from 'express';
import userRouter from './user.route';
import profileRouter from './profile.route';
import availabilityRouter from './availability.route';

const router = Router();

router.use('/auth', userRouter);
router.use('/profile', profileRouter);
router.use('/availability', availabilityRouter);

export default router;
