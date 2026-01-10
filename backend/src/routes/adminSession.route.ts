import { Router } from 'express';
import { auth, requireRoles } from '../middlewares/auth.middleware';
import { validate } from '../handlers/request.handler';
import { adminSessionListRules } from '../middlewares/validators/session.validator';
import { adminListSessions } from '../controllers/session.controller';

const router = Router();

router.get(
  '/',
  auth,
  requireRoles('admin', 'root'),
  adminSessionListRules,
  validate,
  adminListSessions
);

export default router;
