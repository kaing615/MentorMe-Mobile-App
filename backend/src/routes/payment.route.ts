// path: src/routes/payment.route.ts
import { Router } from 'express';
import { paymentWebhook } from '../controllers/payment.controller';

const router = Router();

// Payment gateway webhook (no auth - validated by signature in production)
router.post('/webhook', paymentWebhook);

export default router;
