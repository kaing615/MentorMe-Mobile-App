import { Request, Response } from "express";
import { analyzeMentorIntent } from "../services/ai/mentorRecommend.service";
import { recommendMentors } from "../services/mentor/mentorRecommend.pipeline";
import { answerAppQuestion } from "../services/ai/appQA.service";
import { classifyIntent, classifyMentorIntent } from "../utils/aiIntentRouter";
import { ConversationContext } from "../services/ai/conversationContext.service";

export async function recommendMentorController(req: Request, res: Response) {
  const { message } = req.body;
  const userId = "anonymous"; // Tạm thời dùng anonymous

  if (!message || typeof message !== "string") {
    return res.status(400).json({
      success: false,
      message: "Message is required",
      data: null,
    });
  }

  try {
    // ✅ Tạm comment để test
    // await ConversationContext.addMessage(userId, "user", message);
    // const contextPrompt = await ConversationContext.getContextPrompt(userId);
    const contextPrompt = ""; // Tạm để rỗng

    const intent = await classifyIntent(message, contextPrompt);

    // General response
    if (intent === "general") {
      return res.json({
        success: true,
        message: null,
        data: {
          type: "general_response",
          answer: getGeneralResponse(message),
          suggestions: ["Tìm mentor Java", "App có gì?"],
        },
      });
    }

    // App QA
    if (intent === "app_qa") {
      const answer = await answerAppQuestion(message);

      // ✅ Lưu phản hồi của AI
      await ConversationContext.addMessage(userId, "assistant", answer);

      return res.json({
        success: true,
        message: null,
        data: {
          type: "app_qa",
          answer,
          suggestions: ["Làm sao đăng ký mentor?", "Chính sách hoàn tiền?"],
        },
      });
    }

    // Mentor recommend
    const aiResult = await analyzeMentorIntent(message);
    const mentors = await recommendMentors(aiResult, message);

    return res.json({
      success: true,
      message: null,
      data: {
        type: "mentor_recommend",
        ai: aiResult,
        mentors,
        suggestions:
          mentors.length > 0
            ? ["Xem chi tiết", "Đặt lịch"]
            : ["Thử giá cao hơn", "Tìm mentor khác"],
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

export async function mentorAssistantController(req: Request, res: Response) {
  const { message } = req.body;
  const userId = "anonymous";

  if (!message || typeof message !== "string") {
    return res.status(400).json({
      success: false,
      message: "Message is required",
      data: null,
    });
  }

  try {
    const contextPrompt = "";
    const intent = await classifyMentorIntent(message, contextPrompt);

    if (intent === "app_qa") {
      const answer = await answerAppQuestion(message);

      await ConversationContext.addMessage(userId, "assistant", answer);

      return res.json({
        success: true,
        message: null,
        data: {
          type: "app_qa",
          answer,
          suggestions: [
            "Cách cập nhật lịch rảnh?",
            "Rút tiền về ngân hàng thế nào?",
            "Mentor xác nhận booking ra sao?",
          ],
        },
      });
    }

    return res.json({
      success: true,
      message: null,
      data: {
        type: "general_response",
        answer: getGeneralResponse(message),
        suggestions: [
          "Cách tạo lịch rảnh?",
          "Chính sách payout cho mentor",
          "Hủy hoặc đổi lịch thế nào?",
        ],
      },
    });
  } catch (err) {
    console.error("❌ MENTOR ASSISTANT ERROR", err);
    return res.status(500).json({
      success: false,
      message: "Internal server error",
      data: null,
    });
  }
}

/**
 * Xử lý các câu hỏi chung chung (chào hỏi, cảm ơn, v.v.)
 * @param message Tin nhắn người dùng
 * @returns Câu trả lời phù hợp
 */
function getGeneralResponse(message: string): string {
  const lower = message.toLowerCase();

  // Chào hỏi
  if (
    lower.includes("xin chào") ||
    lower.includes("hello") ||
    lower.includes("hi") ||
    lower.includes("chào") ||
    lower.includes("hey")
  ) {
    return "Xin chào! 👋 Tôi là trợ lý AI của MentorMe.\n\nTôi có thể giúp bạn:\n• Tìm mentor phù hợp theo kỹ năng và ngân sách\n• Trả lời các câu hỏi về tính năng app\n• Giải đáp chính sách và quy định\n\nBạn muốn tôi hỗ trợ điều gì? 😊";
  }

  // Cảm ơn
  if (
    lower.includes("cảm ơn") ||
    lower.includes("thanks") ||
    lower.includes("thank you") ||
    lower.includes("cám ơn")
  ) {
    return "Rất vui được giúp đỡ bạn! 😊\n\nNếu cần hỗ trợ thêm về:\n• Tìm mentor phù hợp\n• Tính năng app\n• Chính sách thanh toán\n\nĐừng ngại hỏi nhé!";
  }

  // Tạm biệt
  if (
    lower.includes("tạm biệt") ||
    lower.includes("bye") ||
    lower.includes("goodbye") ||
    lower.includes("hẹn gặp lại")
  ) {
    return "Tạm biệt! Chúc bạn học tập vui vẻ và hiệu quả với MentorMe! 👋\n\nHẹn gặp lại bạn sớm! 🚀";
  }

  // Hỏi về khả năng
  if (
    lower.includes("bạn là ai") ||
    lower.includes("bạn là gì") ||
    lower.includes("what are you") ||
    lower.includes("who are you")
  ) {
    return "Tôi là trợ lý AI thông minh của MentorMe! 🤖\n\nTôi được phát triển dựa trên công nghệ Gemini AI để:\n• Hiểu nhu cầu học tập của bạn\n• Gợi ý mentor phù hợp nhất\n• Trả lời mọi thắc mắc về MentorMe\n\nHãy thử hỏi tôi bất cứ điều gì! 😊";
  }

  // Câu hỏi về tính năng chung
  if (
    lower.includes("làm được gì") ||
    lower.includes("giúp gì") ||
    lower.includes("what can you do")
  ) {
    return "Tôi có thể giúp bạn:\n\n🎯 Tìm Mentor:\n• Gợi ý mentor theo kỹ năng (Backend, Frontend, Mobile...)\n• Lọc theo giá, đánh giá, kinh nghiệm\n• Giải thích tại sao mentor phù hợp với bạn\n\n💡 Trả lời câu hỏi:\n• Cách đặt lịch và thanh toán\n• Chính sách hoàn tiền\n• Tính năng video call, chat\n• Và nhiều câu hỏi khác!\n\nBạn muốn bắt đầu từ đâu? 😊";
  }

  // Khen ngợi
  if (
    lower.includes("giỏi") ||
    lower.includes("tuyệt") ||
    lower.includes("hay") ||
    lower.includes("good job") ||
    lower.includes("great")
  ) {
    return "Cảm ơn bạn! 🥰 Tôi rất vui khi giúp được bạn!\n\nNếu có thêm câu hỏi nào về MentorMe, cứ hỏi tôi nhé!";
  }

  // Fallback - không hiểu
  return 'Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn. 🤔\n\nBạn có thể hỏi tôi về:\n\n🎯 Tìm Mentor:\n• "Tìm mentor Java cho người mới"\n• "Gợi ý mentor Backend giá dưới 200k"\n\n💡 Thông tin App:\n• "Làm sao để đăng ký mentor?"\n• "Chính sách hoàn tiền như thế nào?"\n• "App có những tính năng gì?"\n\n🔧 Hỗ trợ:\n• "Làm sao để đặt lịch?"\n• "Tôi muốn nạp tiền vào ví"\n\nHãy thử hỏi lại theo cách khác nhé! 😊';
}
