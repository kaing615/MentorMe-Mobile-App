import geminiClient from "../../utils/gemini";

export async function generateGeminiContent(prompt: string): Promise<string> {
  try {
    const response = await geminiClient.client.models.generateContent({
      model: geminiClient.GEMINI_MODEL,
      contents: prompt,
    });

    return response.text ?? "";
  } catch (error: any) {
    console.error("❌ GEMINI GENERATE ERROR");
    console.error(error?.message || error);
    throw new Error("Gemini generateContent failed");
  }
}
