import { generateGeminiContent } from "./gemini.client";
import {
  retrieveRelevantKB,
  buildContextFromKB,
} from "../baseKnowledge.service";

export async function answerAppQuestion(userMessage: string): Promise<string> {
  const entries = retrieveRelevantKB(userMessage, 4);
  const systemContext = buildContextFromKB(entries, 1200);

  const prompt = `
Bạn là trợ lý của MentorMe. Dựa trên thông tin nền tảng (dưới đây), trả lời câu hỏi của người dùng bằng TIẾNG VIỆT, ngắn gọn, thân thiện.
Nếu không chắc, nói "Mình không rõ thông tin này — anh/chị vui lòng liên hệ support."

Câu hỏi: "${userMessage}"
`;

  const fullPrompt = systemContext ? `${systemContext}\n\n${prompt}` : prompt;
  const text = await generateGeminiContent(fullPrompt);

  if (!text || text.trim().length === 0) {
    return "Mình không chắc thông tin này — anh/chị vui lòng liên hệ 23521389@gm.uit.edu.vn.";
  }
  return text.trim();
}
