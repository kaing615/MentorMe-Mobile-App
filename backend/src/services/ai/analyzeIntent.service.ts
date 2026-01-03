import { generateGeminiContent } from "./gemini.client";
import { safeJsonParse } from "../../utils/safeJsonParse";

interface MentorIntentResult {
  intent: string;
  skills: string[];
  level: string;
}

export async function analyzeMentorIntent(
  userMessage: string
): Promise<MentorIntentResult | null> {
  const prompt = `
Phân tích intent người dùng và trả về JSON:
{
  "intent": "",
  "skills": [],
  "level": ""
}

Câu hỏi: "${userMessage}"
`;

  const aiText = await generateGeminiContent(prompt);

  return safeJsonParse<MentorIntentResult>(aiText);
}
