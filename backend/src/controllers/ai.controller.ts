import { Request, Response } from "express";
import { analyzeMentorIntent } from "../services/ai/mentorRecommend.service";
import { recommendMentors } from "../services/mentor/mentorRecommend.pipeline";
import { answerAppQuestion } from "../services/ai/appQA.service";
import { isMentorRelatedQuestion } from "../utils/aiIntentRouter";

export async function recommendMentorController(req: Request, res: Response) {
  const { message } = req.body;

  if (!message || typeof message !== "string") {
    return res.status(400).json({
      success: false,
      message: "Message is required",
      data: null,
    });
  }

  try {
    if (!isMentorRelatedQuestion(message)) {
      const answer = await answerAppQuestion(message);
      return res.json({
        success: true,
        message: null,
        data: {
          type: "app_qa",
          answer,
        },
      });
    }

    const aiResult = await analyzeMentorIntent(message);
    const mentors = await recommendMentors(aiResult, message);

    return res.json({
      success: true,
      message: null,
      data: {
        type: "mentor_recommend",
        ai: aiResult,
        mentors,
      },
    });
  } catch (err) {
    console.error("❌ RECOMMEND CONTROLLER ERROR", err);
    return res.status(500).json({
      success: false,
      message: "Internal server error",
      data: null,
    });
  }
}
