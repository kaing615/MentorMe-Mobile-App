interface ConversationMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
}

/**
 * Lưu trữ context cuộc trò chuyện trong memory (hoặc Redis nếu có)
 * để AI có thể tham khảo ngữ cảnh các tin nhắn trước đó
 */
export class ConversationContext {
  private static readonly TTL = 3600; // 1 hour (in seconds)
  private static readonly MAX_MESSAGES = 10; // Giữ 10 tin nhắn gần nhất

  // In-memory storage (có thể thay bằng Redis sau)
  private static conversations: Map<string, ConversationMessage[]> = new Map();

  /**
   * Thêm tin nhắn mới vào lịch sử cuộc trò chuyện
   */
  static async addMessage(
    userId: string,
    role: "user" | "assistant",
    content: string
  ): Promise<void> {
    const message: ConversationMessage = {
      role,
      content,
      timestamp: Date.now(),
    };

    try {
      const history = await this.getHistory(userId);
      history.push(message);

      // Giữ lại 10 tin nhắn gần nhất
      const trimmed = history.slice(-this.MAX_MESSAGES);

      this.conversations.set(userId, trimmed);
    } catch (error) {
      console.error("❌ Failed to add conversation message:", error);
    }
  }

  /**
   * Lấy lịch sử cuộc trò chuyện của user
   */
  static async getHistory(userId: string): Promise<ConversationMessage[]> {
    try {
      const history = this.conversations.get(userId) || [];

      // Lọc bỏ các tin nhắn quá cũ (> TTL)
      const now = Date.now();
      const filtered = history.filter(
        (msg) => now - msg.timestamp < this.TTL * 1000
      );

      if (filtered.length !== history.length) {
        this.conversations.set(userId, filtered);
      }

      return filtered;
    } catch (error) {
      console.error("❌ Failed to get conversation history:", error);
      return [];
    }
  }

  /**
   * Tạo context prompt cho AI từ lịch sử cuộc trò chuyện
   */
  static async getContextPrompt(userId: string): Promise<string> {
    const history = await this.getHistory(userId);
    if (history.length === 0) return "";

    const contextLines = history.map((msg) => {
      const label = msg.role === "user" ? "Người dùng" : "AI";
      // Truncate nội dung quá dài
      const content =
        msg.content.length > 200
          ? msg.content.substring(0, 200) + "..."
          : msg.content;
      return `${label}: ${content}`;
    });

    return `
NGỮ CẢNH CUỘC TRÒ CHUYỆN TRƯỚC ĐÓ:
${contextLines.join("\n")}

---
`;
  }

  /**
   * Xóa lịch sử cuộc trò chuyện của user
   */
  static async clear(userId: string): Promise<void> {
    this.conversations.delete(userId);
  }

  /**
   * Cleanup: Xóa các conversation đã quá hạn (chạy định kỳ)
   */
  static cleanupExpired(): void {
    const now = Date.now();
    for (const [userId, messages] of this.conversations.entries()) {
      const filtered = messages.filter(
        (msg) => now - msg.timestamp < this.TTL * 1000
      );

      if (filtered.length === 0) {
        this.conversations.delete(userId);
      } else if (filtered.length !== messages.length) {
        this.conversations.set(userId, filtered);
      }
    }
  }
}

// Cleanup định kỳ mỗi 30 phút
setInterval(() => {
  ConversationContext.cleanupExpired();
}, 30 * 60 * 1000);
