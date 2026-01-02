export function safeJsonParse<T>(text: string): T | null {
  if (!text) return null;

  try {
    let cleaned = text
      .trim()
      .replace(/^```json/i, "")
      .replace(/^```/, "")
      .replace(/```$/, "")
      .trim();

    const firstBrace = cleaned.indexOf("{");
    const lastBrace = cleaned.lastIndexOf("}");

    if (firstBrace !== -1 && lastBrace !== -1) {
      cleaned = cleaned.slice(firstBrace, lastBrace + 1);
    }

    return JSON.parse(cleaned);
  } catch (e) {
    console.error("❌ JSON PARSE FAILED");
    console.error(text);
    return null;
  }
}
