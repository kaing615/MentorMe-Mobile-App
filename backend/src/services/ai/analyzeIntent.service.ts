import { generateGeminiContent } from "./gemini.client";
import { safeJsonParse } from "../../utils/safeJsonParse";
import { mentorIntentSystemContext } from "./mentorIntent.system";

export interface MentorIntentResult {
  intent: string;
  skills: string[];
  level: "beginner" | "intermediate" | "advanced";
}

export async function analyzeMentorIntent(
  userMessage: string
): Promise<MentorIntentResult> {
  const prompt = `
CHỈ TRẢ VỀ JSON HỢP LỆ. KHÔNG TEXT. KHÔNG GIẢI THÍCH.

JSON FORMAT:
{
  "intent": "string",
  "skills": ["string"],
  "level": "beginner | intermediate | advanced"
}

CÂU HỎI NGƯỜI DÙNG:
"${userMessage}"

QUY TẮC:
- Nếu không rõ intent → "find_mentor"
- Nếu không có kỹ năng → []
- Nếu không xác định level → "beginner"
`;

  const aiText = await generateGeminiContent(prompt, mentorIntentSystemContext);

  const parsed = safeJsonParse<MentorIntentResult>(aiText);

  if (!parsed) {
    return {
      intent: "find_mentor",
      skills: [],
      level: "beginner",
    };
  }

  return parsed;
}
