import { matchMentorsFromDB } from "./mentorMatch.service";
import { rankMentors } from "./mentorRanking.service";
import { explainMentorFit } from "./mentorExplain.service";
import { explainMentorWithAI } from "../ai/mentorExplainAI.service";
import { createIntentHash } from "../../utils/intentHash";
import {
  getCachedExplain,
  setCachedExplain,
} from "../cache/mentorExplain.cache";
import { MentorRecommendAIResult } from "../ai/mentorRecommend.service";

export async function recommendMentors(
  aiResult: MentorRecommendAIResult,
  userMessage: string
) {
  const matched = await matchMentorsFromDB(aiResult);
  const ranked = rankMentors(matched, aiResult);

  const intentHash = createIntentHash(aiResult);

  const TOP_AI_EXPLAIN = 3;

  return Promise.all(
    ranked.slice(0, 10).map(async (profile, index) => {
      const mentorId = profile.user._id.toString();
      const cacheKey = `mentor:explain:${mentorId}:${intentHash}`;

      let explanation: string | null = null;

      if (index < TOP_AI_EXPLAIN) {
        explanation = await getCachedExplain(cacheKey);

        if (!explanation) {
          explanation = await explainMentorWithAI({
            userMessage,
            profile,
          });

          if (explanation) {
            await setCachedExplain(cacheKey, explanation);
          }
        }
      }

      if (!explanation) {
        explanation = explainMentorFit(profile, aiResult).join(". ") + ".";
      }

      return {
        mentorId,
        userName: profile.user.userName,
        fullName: profile.fullName,
        headline: profile.headline,
        avatarUrl: profile.avatarUrl,
        hourlyRateVnd: profile.hourlyRateVnd,
        rating: profile.rating,
        score: profile._score,
        explanation,
      };
    })
  );
}
