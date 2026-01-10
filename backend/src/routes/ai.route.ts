import { Router } from "express";
import { mentorAssistantController, recommendMentorController } from "../controllers/ai.controller";

const router = Router();

router.post("/recommend-mentor", recommendMentorController);
router.post("/mentor-assistant", mentorAssistantController);

export default router;
