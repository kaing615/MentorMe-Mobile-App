import Profile from "../../models/profile.model";
import { MentorRecommendAIResult } from "../ai/mentorRecommend.service";

export async function matchMentorsFromDB(
  ai: MentorRecommendAIResult,
  limit = 50
) {
  const profileQuery: any = {
    profileCompleted: true,
  };

  // Match skills
  if (ai.skills?.length) {
    profileQuery.skills = { $in: ai.skills };
  }

  // Match topic (text search fallback)
  if (!ai.skills?.length && ai.topic) {
    profileQuery.$text = { $search: ai.topic };
  }

  // Match price
  if (ai.priceRange?.max && ai.priceRange.max > 0) {
    profileQuery.hourlyRateVnd = {
      $gte: ai.priceRange.min,
      $lte: ai.priceRange.max,
    };
  }

  return Profile.find(profileQuery)
    .populate({
      path: "user",
      match: {
        role: "mentor",
        isBlocked: false,
        status: "active",
      },
      select: "userName name",
    })
    .limit(limit)
    .lean();
}
