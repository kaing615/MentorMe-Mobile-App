import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

type Knowledge = {
  id: string;
  title: string;
  content: string;
  tags?: string[];
};

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

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

function removeDiacritics(str: string): string {
  return str
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

export function retrieveRelevantKB(query: string, topK = 3): Knowledge[] {
  const kb = loadKB();
  if (kb.length === 0) return [];
  return kb;
}

export function buildContextFromKB(
  entries: Knowledge[],
  maxChars = 2000  // Tăng lên để đủ context
): string {
  if (!entries || entries.length === 0) return "";
  
  let out = "";
  for (const e of entries) {
    const section = `\n[${e.title}]\n${e.content}\n`;
    if ((out + section).length > maxChars) break;
    out += section;
  }
  return out.trim();
}
