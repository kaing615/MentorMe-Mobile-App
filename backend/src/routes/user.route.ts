import { Router } from "express";
import userController from "../controllers/user.controller";
import { validate } from "../handlers/request.handler";
import {
  signInValidator,
  signUpMenteeValidator,
  signUpMentorValidator,
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

// Admin routes
router.post("/admin/login", userController.adminLogin);

router.get("/", userController.getAllUsers);
router.get("/:id", userController.getUserById);
router.put("/:id", userController.updateUser);

export default router;
