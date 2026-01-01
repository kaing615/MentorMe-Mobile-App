import { Router } from "express";
import { auth, requireRoles } from "../middlewares/auth.middleware";
import mentorStatsController from "../controllers/mentor.stats.controller";

const router = Router();

// GET /mentors/me/stats - Get mentor dashboard stats (monthly)
router.get(
  "/me/stats",
  auth,
  requireRoles("mentor"),
  mentorStatsController.getMyStats
);

// GET /mentors/me/stats/weekly - Get weekly earnings (7 days)
router.get(
  "/me/stats/weekly",
  auth,
  requireRoles("mentor"),
  mentorStatsController.getWeeklyEarnings
);

// GET /mentors/me/stats/yearly - Get yearly earnings (12 months)
router.get(
  "/me/stats/yearly",
  auth,
  requireRoles("mentor"),
  mentorStatsController.getYearlyEarnings
);

// GET /mentors/me/stats/overall - Get overall stats (all time)
router.get(
  "/me/stats/overall",
  auth,
  requireRoles("mentor"),
  mentorStatsController.getOverallStats
);

export default router;

