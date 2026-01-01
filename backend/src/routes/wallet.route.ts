import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import walletController from "../controllers/wallet.controller";
import { validate } from "../handlers/request.handler";

import {
  mockTopupValidator,
  mockDebitValidator,
  listTransactionsValidator,
} from "../middlewares/validators/wallet.validator";

const router = Router();

// Both mentee and mentor can access wallet
const MENTEE_ROLE = "mentee";
const MENTOR_ROLE = "mentor";

router.get("/me", auth, requireRoles(MENTEE_ROLE, MENTOR_ROLE), walletController.getMyWallet);

router.post(
  "/topups/mock",
  auth,
  requireRoles(MENTEE_ROLE),
  mockTopupValidator,
  validate,
  walletController.mockTopup
);

router.post(
  "/debits/mock",
  auth,
  requireRoles(MENTEE_ROLE),
  mockDebitValidator,
  validate,
  walletController.mockDebit
);

router.get(
  "/transactions",
  auth,
  requireRoles(MENTEE_ROLE),
  listTransactionsValidator,
  validate,
  walletController.listTransactions
);

router.post(
  "/debits",
  auth,
  requireRoles(MENTEE_ROLE),
  mockDebitValidator,
  validate,
  walletController.withdraw
);

export default router;
