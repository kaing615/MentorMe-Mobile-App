import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import { validate } from "../handlers/request.handler";
import mentorApplicationController from "../controllers/mentorApplication.controller";
import userController from "../controllers/user.controller";
import {
  mentorApplicationIdRules,
  mentorApplicationListRules,
  mentorApplicationUpdateRules,
} from "../middlewares/validators/mentorApplication.validator";

const router = Router();

router.get(
  "/",
  auth,
  requireRoles("admin", "root"),
  mentorApplicationListRules,
  validate,
  mentorApplicationController.getMentorApplications
);

router.get(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  mentorApplicationIdRules,
  validate,
  mentorApplicationController.getMentorApplicationById
);

router.put(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  mentorApplicationIdRules,
  mentorApplicationUpdateRules,
  validate,
  mentorApplicationController.updateMentorApplication
);

router.put(
  "/:id/approve",
  auth,
  requireRoles("admin", "root"),
  mentorApplicationIdRules,
  validate,
  userController.approveMentor
);

router.put(
  "/:id/reject",
  auth,
  requireRoles("admin", "root"),
  mentorApplicationIdRules,
  validate,
  userController.rejectMentor
);

export default router;
