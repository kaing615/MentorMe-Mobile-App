import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import mentorStatsController from "../controllers/mentor.stats.controller";

const router = Router();

// GET /mentors/me/stats - Get mentor dashboard stats
router.get(
  "/me/stats",
  auth,
  requireRoles("mentor"),
  mentorStatsController.getMyStats
);

export default router;

