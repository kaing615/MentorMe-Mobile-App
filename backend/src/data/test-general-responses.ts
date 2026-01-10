/**
 * Demo test cho general responses
 */

// Mock function từ controller
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
    return "Tôi có thể giúp bạn:\n\n🎯 **Tìm Mentor:**\n• Gợi ý mentor theo kỹ năng (Backend, Frontend, Mobile...)\n• Lọc theo giá, đánh giá, kinh nghiệm\n• Giải thích tại sao mentor phù hợp với bạn\n\n💡 **Trả lời câu hỏi:**\n• Cách đặt lịch và thanh toán\n• Chính sách hoàn tiền\n• Tính năng video call, chat\n• Và nhiều câu hỏi khác!\n\nBạn muốn bắt đầu từ đâu? 😊";
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

  // Fallback
  return 'Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn. 🤔\n\n**Bạn có thể hỏi tôi về:**\n\n🎯 **Tìm Mentor:**\n• "Tìm mentor Java cho người mới"\n• "Gợi ý mentor Backend giá dưới 200k"\n\n💡 **Thông tin App:**\n• "Làm sao để đăng ký mentor?"\n• "Chính sách hoàn tiền như thế nào?"\n• "App có những tính năng gì?"\n\n🔧 **Hỗ trợ:**\n• "Làm sao để đặt lịch?"\n• "Tôi muốn nạp tiền vào ví"\n\nHãy thử hỏi lại theo cách khác nhé! 😊';
}

// Test cases
console.log("🧪 Testing General Responses\n");
console.log("=".repeat(80));

const testCases = [
  "Xin chào",
  "Hello",
  "Cảm ơn bạn",
  "Thanks!",
  "Tạm biệt",
  "Bye",
  "Bạn là ai?",
  "Bạn làm được gì?",
  "Bạn giỏi quá!",
  "Tôi muốn tìm mentor về blockchain", // Should fallback
];

testCases.forEach((testCase, index) => {
  console.log(`\n[${index + 1}] User: "${testCase}"`);
  console.log("-".repeat(80));
  const response = getGeneralResponse(testCase);
  console.log(`AI: ${response}`);
  console.log("=".repeat(80));
});

console.log("\n✅ All test cases executed!");
