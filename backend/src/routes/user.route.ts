import { requireRoles, auth } from "../middlewares/auth.middleware";
import { Router } from "express";
import responseHandler from "../handlers/response.handler";
import { validate } from "../handlers/request.handler";
import userController from "../controllers/user.controller";
import {
  signUpMenteeValidator,
  signUpMentorValidator,
  signInValidator,
  verifyOtpValidator,
} from "../middlewares/validators/user.validator";

const router = Router();

router.post(
  "/signup",
  signUpMenteeValidator,
  validate,
  userController.signUpMentee
);
router.post(
  "/signup-mentor",
  signUpMentorValidator,
  validate,
  userController.signUpAsMentor
);
router.post("/signin", signInValidator, validate, userController.signIn);
router.post(
  "/verify-otp",
  verifyOtpValidator,
  validate,
  userController.verifyEmailOtp
);
router.post("/signout", userController.signOut);

export default router;
