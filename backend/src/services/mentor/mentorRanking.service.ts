import { MentorRecommendAIResult } from "../ai/mentorRecommend.service";

export function rankMentors(profiles: any[], ai: MentorRecommendAIResult) {
  return profiles
    .filter((p) => p.user)
    .map((profile) => {
      let score = 0;

      const ratingAvg = profile.rating?.average || 0;
      const ratingCount = profile.rating?.count || 0;

      score += ratingAvg * 3;
      score += Math.log(ratingCount + 1);

      if (ai.skills?.length && profile.skills?.length) {
        const matchedSkills = profile.skills.filter((s: string) =>
          ai.skills.includes(s)
        );
        score += matchedSkills.length * 2;
      }

      const topic = ai.topic?.toLowerCase();
      if (topic) {
        const fields = [profile.headline, profile.jobTitle, profile.category]
          .join(" ")
          .toLowerCase();

        if (fields.includes(topic)) {
          score += 2;
        }
      }

      if (profile.hourlyRateVnd) {
        score -= profile.hourlyRateVnd / 200_000;
      }

      return {
        ...profile,
        _score: Number(score.toFixed(2)),
      };
    })
    .sort((a, b) => b._score - a._score);
}
