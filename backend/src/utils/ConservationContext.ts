import { redisClient } from "../../utils/redis";

interface ConversationMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
}

/**
 * Lưu trữ context cuộc trò chuyện để AI có thể tham khảo
 */
export class ConversationContext {
  private static readonly TTL = 3600; // 1 hour
  private static readonly MAX_MESSAGES = 10; // Giữ 10 tin nhắn gần nhất

  static async addMessage(
    userId: string,
    role: "user" | "assistant",
    content: string
  ): Promise<void> {
    const key = `conversation:${userId}`;
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

      await redisClient.setex(key, this.TTL, JSON.stringify(trimmed));
    } catch (error) {
      console.error("❌ Failed to add conversation message:", error);
    }
  }

  static async getHistory(userId: string): Promise<ConversationMessage[]> {
    const key = `conversation:${userId}`;
    try {
      const data = await redisClient.get(key);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error("❌ Failed to get conversation history:", error);
      return [];
    }
  }

  static async getContextPrompt(userId: string): Promise<string> {
    const history = await this.getHistory(userId);
    if (history.length === 0) return "";

    const contextLines = history.map((msg) => {
      const label = msg.role === "user" ? "Người dùng" : "AI";
      return `${label}: ${msg.content}`;
    });

    return `
NGỮ CẢNH CUỘC TRÒ CHUYỆN TRƯỚC ĐÓ:
${contextLines.join("\n")}

---
`;
  }

  static async clear(userId: string): Promise<void> {
    const key = `conversation:${userId}`;
    await redisClient.del(key);
  }
}
