import { Router } from "express";
import multer from "multer";
import profileController, {
  createRequiredProfile,
} from "../controllers/profile.controller";
import { auth, requireRoles } from "../middlewares/auth.middleware";

const router = Router();

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 },
});

router.post(
  "/required",
  auth,
  upload.single("avatar"),
  profileController.createRequiredProfile
);
router.get("/user/:userName", profileController.getProfileByUserName);
router.get("/id/:userId", profileController.getProfileByUserId);
router.get("/mentor", profileController.getAllMentors);
router.get("/me", auth, profileController.getMe);
router.put(
  "/",
  auth,
  upload.single("avatar"),
  profileController.updateUserProfile
);

export default router;
