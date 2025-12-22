import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import walletController from "../controllers/wallet.controller";
import {
  mockTopupValidator,
  mockDebitValidator,
  listTransactionsValidator,
} from "../middlewares/validators/wallet.validator";

const router = Router();

// Mentee role (lowercase as per user.model.ts)
const MENTEE_ROLE = "mentee";

router.get("/me", auth, requireRoles(MENTEE_ROLE), walletController.getMyWallet);

router.post(
  "/topups/mock",
  auth,
  requireRoles(MENTEE_ROLE),
  mockTopupValidator,
  walletController.mockTopup
);

router.post(
  "/debits/mock",
  auth,
  requireRoles(MENTEE_ROLE),
  mockDebitValidator,
  walletController.mockDebit
);

router.get(
  "/transactions",
  auth,
  requireRoles(MENTEE_ROLE),
  listTransactionsValidator,
  walletController.listTransactions
);

export default router;
