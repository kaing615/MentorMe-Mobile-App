import { MentorRecommendAIResult } from "../ai/mentorRecommend.service";

export function explainMentorFit(profile: any, ai: MentorRecommendAIResult) {
  const reasons: string[] = [];

  if (profile.rating?.average >= 4.5) {
    reasons.push("Được đánh giá rất cao");
  }

  if (profile.rating?.count >= 20) {
    reasons.push("Có nhiều đánh giá từ học viên");
  }

  const matchedSkills =
    ai.skills?.filter((s) => profile.skills?.includes(s)) || [];

  if (matchedSkills.length) {
    reasons.push(`Phù hợp kỹ năng: ${matchedSkills.join(", ")}`);
  }

  if (ai.topic) {
    const topic = ai.topic.toLowerCase();
    const fields = [profile.headline, profile.jobTitle, profile.category]
      .join(" ")
      .toLowerCase();

    if (fields.includes(topic)) {
      reasons.push(`Chuyên về ${ai.topic}`);
    }
  }

  if (profile.hourlyRateVnd > 0) {
    reasons.push(`Giá ${profile.hourlyRateVnd.toLocaleString("vi-VN")}đ/giờ`);
  }

  return reasons;
}
