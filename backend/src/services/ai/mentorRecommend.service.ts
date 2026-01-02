import { generateGeminiContent } from "./gemini.client";
import { mentorRecommendPrompt } from "./mentorRecommend.prompt";
import { safeJsonParse } from "../../utils/safeJsonParse";

export interface MentorRecommendAIResult {
  intent: string;
  skills: string[];
  topic: string;
  level: "beginner" | "intermediate" | "advanced";
  priceRange: {
    min: number;
    max: number;
  };
  language: "vi" | "en";
}

async function callAI(prompt: string): Promise<MentorRecommendAIResult | null> {
  const text = await generateGeminiContent(prompt);
  return safeJsonParse<MentorRecommendAIResult>(text);
}

export async function analyzeMentorIntent(
  userMessage: string
): Promise<MentorRecommendAIResult> {
  const prompt = mentorRecommendPrompt(userMessage);

  let result = await callAI(prompt);
  if (result) return result;

  const retryPrompt = `
CHỈ TRẢ JSON. KHÔNG TEXT.

${prompt}
`;

  result = await callAI(retryPrompt);
  if (result) return result;

  // fallback cuối cùng (KHÔNG BAO GIỜ crash backend)
  return {
    intent: "find_mentor",
    skills: [],
    topic: "",
    level: "beginner",
    priceRange: { min: 0, max: 0 },
    language: "vi",
  };
}
