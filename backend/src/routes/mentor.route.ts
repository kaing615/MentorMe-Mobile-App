import { Router } from "express";
import mentorController from "../controllers/mentor.controller";

const router = Router();

// GET /api/v1/mentors
router.get("/", mentorController.listMentors);
// GET /api/v1/mentors/:id
router.get("/:id", mentorController.getMentorById);

export default router;
