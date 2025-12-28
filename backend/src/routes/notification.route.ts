// path: src/routes/notification.route.ts
import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import {
  registerDeviceToken,
  unregisterDeviceToken,
  sendTestPush,
  sendPushToUserId,
  listDeviceTokens,
  listNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
  getNotificationPreferences,
  updateNotificationPreferences,
} from '../controllers/notification.controller';
import {
  registerDeviceTokenRules,
  unregisterDeviceTokenRules,
  sendTestPushRules,
  sendPushToUserIdRules,
  listDeviceTokensRules,
  listNotificationsRules,
  markNotificationReadRules,
  updatePreferencesRules,
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
router.get(
  '/devices',
  auth,
  requireRoles('admin', 'root'),
  listDeviceTokensRules,
  validate,
  listDeviceTokens
);

// Notifications history
router.get('/', auth, listNotificationsRules, validate, listNotifications);
router.get('/unread-count', auth, getUnreadCount);
router.post('/read-all', auth, markAllNotificationsRead);
router.patch('/:id/read', auth, markNotificationReadRules, validate, markNotificationRead);
router.get('/preferences', auth, getNotificationPreferences);
router.patch('/preferences', auth, updatePreferencesRules, validate, updateNotificationPreferences);

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
