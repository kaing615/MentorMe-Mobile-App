import { generateGeminiContent } from "../services/ai/gemini.client";

/**
 * Sử dụng AI để phân loại intent thay vì keywords cứng
 * @param message Tin nhắn người dùng
 * @param context Ngữ cảnh cuộc trò chuyện (optional)
 */
export async function classifyIntent(
  message: string,
  context: string = ""
): Promise<"mentor_search" | "app_qa" | "general"> {
  const prompt = `
${context}

Phân loại câu hỏi người dùng vào 1 trong 3 loại:
- "mentor_search": Tìm mentor, học skill, career advice, lộ trình học
- "app_qa": Hỏi về tính năng app, giá cả, chính sách, liên hệ
- "general": Câu hỏi chung chung, chào hỏi, cảm ơn, tạm biệt, không liên quan

Câu hỏi hiện tại: "${message}"

CHỈ TRẢ VỀ 1 TRONG 3 GIÁ TRỊ: mentor_search, app_qa, general
`;

  try {
    const result = await generateGeminiContent(prompt);
    const intent = result.trim().toLowerCase();

    if (intent.includes("mentor_search")) return "mentor_search";
    if (intent.includes("app_qa")) return "app_qa";
    return "general";
  } catch (error) {
    console.error("❌ Intent classification failed:", error);
    // Fallback to keyword matching
    return isMentorRelatedQuestion(message) ? "mentor_search" : "app_qa";
  }
}

export function isMentorRelatedQuestion(message: string): boolean {
  const keywords = [
    "mentor",
    "học",
    "kỹ năng",
    "skill",
    "career",
    "lộ trình",
    "roadmap",
    "backend",
    "frontend",
    "mobile",
    "java",
    "python",
    "react",
  ];

  const q = message.toLowerCase();
  return keywords.some((k) => q.includes(k));
}
