import { Router } from "express";
import paymentMethodController from "../controllers/paymentMethod.controller";
import { auth } from "../middlewares/auth.middleware";

const router = Router();

router.use(auth);

router.post(
  "/",
  paymentMethodController.createPaymentMethod
);

router.get(
  "/me",
  paymentMethodController.listMyPaymentMethods
);

router.put(
  "/:id",
  paymentMethodController.updatePaymentMethod
);

router.delete(
  "/:id",
  paymentMethodController.deletePaymentMethod
);

export default router;
