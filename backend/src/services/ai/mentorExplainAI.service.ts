import { generateGeminiContent } from "./gemini.client";
import { mentorExplainPrompt } from "./mentorExplain.prompt";
import {
  retrieveRelevantKB,
  buildContextFromKB,
} from "../baseKnowledge.service";

export async function explainMentorWithAI({
  userMessage,
  profile,
}: {
  userMessage: string;
  profile: any;
}): Promise<string | null> {
  try {
    const kbEntries = retrieveRelevantKB(userMessage, 3);
    const systemContext = buildContextFromKB(kbEntries);

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

    const text = await generateGeminiContent(prompt, systemContext);

    if (!text || text.length < 20) return null;
    return text.trim();
  } catch (err) {
    console.error("❌ AI EXPLAIN FAILED", err);
    return null;
  }
}
