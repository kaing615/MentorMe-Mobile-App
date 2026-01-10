import { generateGeminiContent } from "../services/ai/gemini.client";

/**
 * Sử dụng AI để phân loại intent thay vì keywords cứng
 * @param message Tin nhắn người dùng
 * @param context Ngữ cảnh cuộc trò chuyện (optional)
 */
export async function classifyIntent(
  message: string,
  context?: string
): Promise<"mentor_recommend" | "app_qa" | "general"> {
  const lowerMsg = message.toLowerCase().trim();

  // 🔥 Quick keyword check trước để tiết kiệm API calls
  if (
    lowerMsg.includes("mentor") ||
    lowerMsg.includes("tìm") ||
    lowerMsg.includes("gợi ý") ||
    lowerMsg.includes("recommend")
  ) {
    return "mentor_recommend"; // ✅ Đổi thành mentor_recommend
  }

  if (
    lowerMsg.includes("app") ||
    lowerMsg.includes("tính năng") ||
    lowerMsg.includes("giá") ||
    lowerMsg.includes("pricing") ||
    lowerMsg.includes("founder") ||
    lowerMsg.includes("sáng lập") ||
    lowerMsg.includes("sang lap") ||
    lowerMsg.includes("liên hệ") ||
    lowerMsg.includes("contact")
  ) {
    return "app_qa";
  }

  // Nếu không match keyword thì mới gọi AI
  try {
    const prompt = `
Phân loại ý định của người dùng vào 1 trong 3 loại:
- "mentor_recommend": Tìm kiếm, gợi ý mentor
- "app_qa": Hỏi về ứng dụng (tính năng, giá cả, founder, liên hệ)
- "general": Chào hỏi, câu hỏi chung

${context ? context : ""}

Tin nhắn: "${message}"

Trả về ĐÚNG 1 TRONG 3 TỪ: mentor_recommend, app_qa, general
`;

    const result = await generateGeminiContent(prompt);
    const intent = result.trim().toLowerCase();

    if (intent.includes("mentor")) return "mentor_recommend";
    if (intent.includes("app") || intent.includes("qa")) return "app_qa";
    return "general";
  } catch (error) {
    console.error("❌ Intent classification failed:", error);
    return "general"; // Fallback khi API fail
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
