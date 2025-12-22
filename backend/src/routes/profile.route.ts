import { Router } from "express";
import multer from "multer";
import profileController from "../controllers/profile.controller";
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

router.get("/me", auth, profileController.getMyProfile);

router.put(
  "/me",
  auth,
  upload.single("avatar"),
  profileController.updateMyProfile
);

router.get("/:id", profileController.getPublicProfile);

// Admin routes
router.get(
  "/",
  auth,
  requireRoles("admin", "root"),
  profileController.getAllProfiles
);

router.delete(
  "/:id",
  auth,
  requireRoles("admin", "root"),
  profileController.deleteProfile
);

export default router;
