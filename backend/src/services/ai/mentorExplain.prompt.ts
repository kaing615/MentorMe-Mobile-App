export function mentorExplainPrompt({
  userGoal,
  mentorProfile,
}: {
  userGoal: string;
  mentorProfile: {
    fullName?: string;
    headline?: string;
    skills?: string[];
    rating?: number;
    experience?: string;
    hourlyRateVnd?: number;
  };
}) {
  return `
Bạn là trợ lý tư vấn mentor cho nền tảng MentorMe.

Nhiệm vụ:
- Viết 2–3 câu NGẮN GỌN
- Giải thích vì sao mentor này phù hợp với nhu cầu người học
- Văn phong thân thiện, dễ hiểu, hướng tới người mới học
- KHÔNG markdown
- KHÔNG emoji
- KHÔNG liệt kê gạch đầu dòng

Thông tin người học:
"${userGoal}"

Thông tin mentor:
- Tên: ${mentorProfile.fullName || "Mentor"}
- Chức danh: ${mentorProfile.headline || ""}
- Kỹ năng: ${(mentorProfile.skills || []).join(", ")}
- Kinh nghiệm: ${mentorProfile.experience || ""}
- Đánh giá: ${mentorProfile.rating || 0}/5
- Giá: ${mentorProfile.hourlyRateVnd || 0} VND/giờ

Viết câu trả lời bằng TIẾNG VIỆT.
`;
}
