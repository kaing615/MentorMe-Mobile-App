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

export default router;
