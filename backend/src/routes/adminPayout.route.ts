import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import payoutController from "../controllers/payout.controller";

const router = Router();

// Admin role (lowercase as per user.model.ts)
const ADMIN_ROLE = "admin";

router.get(
  "/",
  auth,
  requireRoles(ADMIN_ROLE),
  payoutController.adminListPayoutRequests
);

router.post(
  "/:id/approve",
  auth,
  requireRoles(ADMIN_ROLE),
  payoutController.adminApprovePayout
);

router.post(
  "/:id/retry",
  auth,
  requireRoles(ADMIN_ROLE),
  payoutController.adminRetryPayout
);

export default router;
