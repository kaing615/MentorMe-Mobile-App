// path: src/routes/payment.route.ts
import { Router } from 'express';
import { payBookingByWallet, paymentWebhook } from '../controllers/payment.controller';
import { auth } from '../middlewares/auth.middleware';

const router = Router();

// Payment gateway webhook (no auth - validated by signature in production)
router.post('/webhook', paymentWebhook);
router.post("/bookings/:id/pay", auth, payBookingByWallet);

export default router;
