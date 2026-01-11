import { Router } from 'express';
import { approveTopUpAdmin, confirmTopUpTransferred, createTopUpIntent, getMyTopUpIntents, listAllTopUpsAdmin, listPendingTopUpsAdmin, rejectTopUpAdmin } from '../controllers/walletTopup.controller';
import { auth, requireRoles } from '../middlewares/auth.middleware';

const router = Router();

router.post('/topup-intent', auth, createTopUpIntent);
router.post('/topup-intent/:id/confirm', auth, confirmTopUpTransferred);
router.get('/topup-intents/me', auth, getMyTopUpIntents);

// admin
router.get('/admin/topups/pending', auth, requireRoles('admin'), listPendingTopUpsAdmin);
router.get('/admin/topups', auth, requireRoles('admin'), listAllTopUpsAdmin);
router.post('/admin/topups/:id/approve', auth, requireRoles('admin'), approveTopUpAdmin);
router.post('/admin/topups/:id/reject', auth, requireRoles('admin'), rejectTopUpAdmin);

export default router;
