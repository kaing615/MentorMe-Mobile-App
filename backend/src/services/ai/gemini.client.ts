import geminiClient from "../../utils/gemini";

export async function generateGeminiContent(
  prompt: string,
  systemContext?: string
): Promise<string> {
  try {
    const fullPrompt =
      systemContext && systemContext.trim().length > 0
        ? `${systemContext}\n\n${prompt}`
        : prompt;

    const response = await geminiClient.client.models.generateContent({
      model: geminiClient.GEMINI_MODEL,
      contents: fullPrompt,
    });

    return response.text ?? "";
  } catch (error: any) {
    console.error("❌ GEMINI GENERATE ERROR");
    console.error(error?.message || error);
    throw new Error("Gemini generateContent failed");
  }
}
