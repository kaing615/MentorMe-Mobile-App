import { Router } from "express";
import payoutController from "../controllers/payout.controller";
import { mockPayoutWebhookValidator } from "../middlewares/validators/payout.validator";
import { validate } from "../handlers/request.handler";

const router = Router();

// Mock webhook - unauthenticated for dev/testing
router.post(
  "/payout-provider",
  mockPayoutWebhookValidator,
  validate,
  payoutController.handleMockPayoutWebhook
);

export default router;
