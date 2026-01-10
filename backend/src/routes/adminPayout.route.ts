import { Router } from "express";
import { param } from "express-validator";
import payoutController from "../controllers/payout.controller";
import { validate } from "../handlers/request.handler";
import { auth, requireRoles } from "../middlewares/auth.middleware";

const router = Router();

// Admin and root roles (lowercase as per user.model.ts)
const ADMIN_ROLES = ["admin", "root"];
// Payout ID param validator
const payoutIdParamValidator = [
  param("id").isMongoId().withMessage("Invalid payout id"),
];

router.get(
  "/",
  auth,
  requireRoles(...ADMIN_ROLES),
  payoutController.adminListPayoutRequests
);

router.post(
  "/:id/approve",
  auth,
  requireRoles(...ADMIN_ROLES),
  payoutIdParamValidator,
  validate,
  payoutController.adminApprovePayout
);

router.post(
  "/:id/reject",
  auth,
  requireRoles(...ADMIN_ROLES),
  payoutController.adminRejectPayout
);

router.post(
  "/:id/retry",
  auth,
  requireRoles(...ADMIN_ROLES),
  payoutIdParamValidator,
  validate,
  payoutController.adminRetryPayout
);

export default router;
