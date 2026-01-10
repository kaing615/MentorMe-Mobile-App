import fs from "fs";
import path from "path";

type Knowledge = {
  id: string;
  title: string;
  content: string;
  tags?: string[];
};

const KB_PATH = path.join(__dirname, "../data/baseKnowledge.json");

let cache: Knowledge[] | null = null;

function loadKB(): Knowledge[] {
  if (cache) return cache;
  try {
    const raw = fs.readFileSync(KB_PATH, "utf-8");
    cache = JSON.parse(raw) as Knowledge[];
    return cache;
  } catch (err) {
    console.error("Load baseKnowledge error", err);
    cache = [];
    return cache;
  }
}

export function retrieveRelevantKB(query: string, topK = 3): Knowledge[] {
  const kb = loadKB();
  if (!query || kb.length === 0) return [];

  const q = query.toLowerCase();

  // fast path: nếu hỏi về founder, trả ngay entry founder lên đầu
  const founderKeywords = [
    "founder",
    "người sáng lập",
    "ai sáng lập",
    "người tạo",
  ];
  const isFounder = founderKeywords.some((k) => q.includes(k));
  const prioritized: Knowledge[] = [];
  if (isFounder) {
    const f = kb.find(
      (e) => (e.tags || []).includes("founder") || e.id === "founder"
    );
    if (f) prioritized.push(f);
  }

  const tokens = q.split(/\W+/).filter(Boolean);

  const scored = kb
    .filter((k) => !prioritized.includes(k))
    .map((entry) => {
      const text = `${entry.title} ${entry.content} ${(entry.tags || []).join(
        " "
      )}`.toLowerCase();
      let score = 0;
      for (const t of tokens) if (text.includes(t)) score += 1;
      return { entry, score };
    });

  scored.sort((a, b) => b.score - a.score);
  const rest = scored
    .filter((s) => s.score > 0)
    .slice(0, Math.max(0, topK - prioritized.length))
    .map((s) => s.entry);

  return [...prioritized, ...rest];
}

export function buildContextFromKB(
  entries: Knowledge[],
  maxChars = 800
): string {
  if (!entries || entries.length === 0) return "";
  let out = "Thông tin nền tảng MentorMe (tóm tắt):\n";
  for (const e of entries) {
    const snippet = e.content.trim();
    const line = `- ${e.title}: ${snippet}\n`;
    if ((out + line).length > maxChars) break;
    out += line;
  }
  return out.trim();
}
