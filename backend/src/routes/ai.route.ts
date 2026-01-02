import { Router } from "express";
import { recommendMentorController } from "../controllers/ai.controller";

const router = Router();

router.post("/recommend-mentor", recommendMentorController);

export default router;
