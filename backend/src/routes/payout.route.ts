import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import payoutController from "../controllers/payout.controller";
import { createPayoutRequestValidator } from "../middlewares/validators/payout.validator";
import { validate } from "../handlers/request.handler";


const router = Router();

const MENTOR_ROLE = "mentor";

router.post(
  "/requests",
  auth,
  requireRoles(MENTOR_ROLE),
  createPayoutRequestValidator,
  validate,
  payoutController.createPayoutRequest
);

router.get(
  "/requests/me",
  auth,
  requireRoles(MENTOR_ROLE),
  payoutController.listMyPayoutRequests
);

export default router;
