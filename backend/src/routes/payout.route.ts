import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import payoutController from "../controllers/payout.controller";
import { createPayoutRequestValidator } from "../middlewares/validators/payout.validator";

const router = Router();

// Mentor role (lowercase as per user.model.ts)
const MENTOR_ROLE = "mentor";

router.post(
  "/requests",
  auth,
  requireRoles(MENTOR_ROLE),
  createPayoutRequestValidator,
  payoutController.createPayoutRequest
);

router.get(
  "/requests/me",
  auth,
  requireRoles(MENTOR_ROLE),
  payoutController.listMyPayoutRequests
);

export default router;
