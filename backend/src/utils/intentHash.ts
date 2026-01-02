import crypto from "crypto";

export function createIntentHash(aiResult: {
  topic?: string;
  skills?: string[];
  level?: string;
}) {
  const raw = JSON.stringify({
    topic: aiResult.topic || "",
    skills: aiResult.skills || [],
    level: aiResult.level || "",
  });

  return crypto.createHash("md5").update(raw).digest("hex");
}
