import { Request, Response } from "express";
import { analyzeMentorIntent } from "../services/ai/mentorRecommend.service";
import { recommendMentors } from "../services/mentor/mentorRecommend.pipeline";

export async function recommendMentorController(req: Request, res: Response) {
  const { message } = req.body;

  const aiResult = await analyzeMentorIntent(message);
  const mentors = await recommendMentors(aiResult, message);

  return res.json({
    success: true,
    ai: aiResult,
    mentors,
  });
}
