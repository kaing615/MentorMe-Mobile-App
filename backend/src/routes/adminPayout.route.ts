import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import payoutController from "../controllers/payout.controller";
import { validate } from "../handlers/request.handler";
import { param } from "express-validator";


const router = Router();

// Admin role (lowercase as per user.model.ts)
const ADMIN_ROLE = "admin";
// Payout ID param validator
const payoutIdParamValidator = [
  param("id").isMongoId().withMessage("Invalid payout id"),
];


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
  payoutIdParamValidator,
  validate,
  payoutController.adminApprovePayout
);

router.post(
  "/:id/retry",
  auth,
  requireRoles(ADMIN_ROLE),
  payoutIdParamValidator,
  validate,
  payoutController.adminRetryPayout
);

export default router;
