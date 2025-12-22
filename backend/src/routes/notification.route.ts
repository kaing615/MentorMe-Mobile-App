// path: src/routes/notification.route.ts
import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import {
  registerDeviceToken,
  unregisterDeviceToken,
  sendTestPush,
  sendPushToUserId,
} from '../controllers/notification.controller';
import {
  registerDeviceTokenRules,
  unregisterDeviceTokenRules,
  sendTestPushRules,
  sendPushToUserIdRules,
} from '../middlewares/validators/notification.validator';

const router = Router();

// Device token registration
router.post('/devices', auth, registerDeviceTokenRules, validate, registerDeviceToken);
router.post(
  '/devices/unregister',
  auth,
  unregisterDeviceTokenRules,
  validate,
  unregisterDeviceToken
);

// Push helpers
router.post('/push/test', auth, sendTestPushRules, validate, sendTestPush);
router.post(
  '/push',
  auth,
  requireRoles('admin', 'root'),
  sendPushToUserIdRules,
  validate,
  sendPushToUserId
);

export default router;
