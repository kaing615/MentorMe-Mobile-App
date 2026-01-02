import { generateGeminiContent } from "./gemini.client";
import { mentorExplainPrompt } from "./mentorExplain.prompt";

export async function explainMentorWithAI({
  userMessage,
  profile,
}: {
  userMessage: string;
  profile: any;
}): Promise<string | null> {
  try {
    const prompt = mentorExplainPrompt({
      userGoal: userMessage,
      mentorProfile: {
        fullName: profile.fullName,
        headline: profile.headline,
        skills: profile.skills,
        rating: profile.rating?.average,
        experience: profile.experience,
        hourlyRateVnd: profile.hourlyRateVnd,
      },
    });

    const text = await generateGeminiContent(prompt);

    if (!text || text.length < 20) return null;

    return text.trim();
  } catch (err) {
    console.error("❌ AI EXPLAIN FAILED", err);
    return null;
  }
}
