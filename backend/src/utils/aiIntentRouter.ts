export function isMentorRelatedQuestion(message: string): boolean {
  const keywords = [
    "mentor",
    "học",
    "kỹ năng",
    "trình độ",
    "backend",
    "frontend",
    "mobile",
    "java",
    "node",
    "react",
    "lộ trình",
    "học gì",
  ];

  const q = message.toLowerCase();
  return keywords.some((k) => q.includes(k));
}
