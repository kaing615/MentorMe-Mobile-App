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

router.post("/signup", validate, userController.signUpMentee);
router.post("/signup-mentor", validate, userController.signUpAsMentor);
router.post("/signin", validate, userController.signIn);
router.post("/verify-otp", validate, userController.verifyEmailOtp);

export default router;
