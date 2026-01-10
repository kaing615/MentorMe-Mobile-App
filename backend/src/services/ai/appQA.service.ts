import { generateGeminiContent } from "./gemini.client";
import {
  retrieveRelevantKB,
  buildContextFromKB,
} from "../baseKnowledge.service";

export async function answerAppQuestion(userMessage: string): Promise<string> {
  try {
    const entries = retrieveRelevantKB(userMessage); // Lấy toàn bộ KB
    const systemContext = buildContextFromKB(entries, 2000); // Tăng maxChars

    const prompt = `
Bạn là trợ lý ảo thông minh của MentorMe - nền tảng kết nối mentor và mentee.

NHIỆM VỤ:
- Trả lời câu hỏi của người dùng dựa trên Thông tin nền tảng bên dưới
- Nếu người dùng hỏi về: người sáng lập, founder, ai tạo ra, team, nhóm phát triển → Trả lời thông tin về người sáng lập
- Nếu hỏi về giá cả, chi phí, phí, pricing → Trả lời chính sách giá
- Nếu hỏi về tính năng, làm gì được, booking, đặt lịch → Trả lời tính năng
- Nếu hỏi về liên hệ, hỗ trợ, support, email → Trả lời thông tin liên hệ
- Trả lời bằng TIẾNG VIỆT, tự nhiên, thân thiện, ngắn gọn (2-4 câu)
- Nếu không có thông tin → "Mình chưa có thông tin này, anh/chị liên hệ support@mentorme.com nhé"

THÔNG TIN NỀN TẢNG:
${systemContext}

CÂU HỎI: "${userMessage}"

TRẢ LỜI:`;

    const text = await generateGeminiContent(prompt);

    if (!text || text.trim().length === 0) {
      return "Mình chưa hiểu câu hỏi này lắm. Anh/chị có thể liên hệ 23521389@gm.uit.edu.vn để được hỗ trợ trực tiếp nhé!";
    }
    return text.trim();
  } catch (error) {
    console.error("❌ APP QA SERVICE ERROR:", error);
    return "Mình đang gặp chút vấn đề kỹ thuật. Anh/chị vui lòng thử lại sau nhé! 🙏";
  }
}
