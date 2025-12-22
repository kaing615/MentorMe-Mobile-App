import { Router } from "express";
import userController from "../controllers/user.controller";
import { validate } from "../handlers/request.handler";
import { auth, requireRoles } from "../middlewares/auth.middleware";
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

router.get(
  "/",
  auth,
  requireRoles("admin", "root"),
  userController.getAllUsers
);
router.get("/me", auth, userController.getCurrentUser);
router.post(
  "/",
  auth,
  requireRoles("admin", "root"),
  userController.createUser
);
router.put(
  "/change-password",
  auth,
  requireRoles("admin", "root"),
  userController.changeMyPassword
);
router.get(
  "/pending-mentors/count",
  auth,
  requireRoles("admin", "root"),
  userController.getPendingMentorsCount
);
router.get(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  userController.getUserById
);
router.put(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  userController.updateUser
);
router.put(
  "/:id/password",
  auth,
  requireRoles("admin", "root"),
  userController.changeUserPassword
);
router.put(
  "/:id/approve",
  auth,
  requireRoles("admin", "root"),
  userController.approveMentor
);
router.put(
  "/:id/reject",
  auth,
  requireRoles("admin", "root"),
  userController.rejectMentor
);
router.delete(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  userController.deleteUser
);

export default router;
