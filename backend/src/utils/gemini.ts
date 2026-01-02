import { GoogleGenAI } from "@google/genai";

const client = new GoogleGenAI({});
const GEMINI_MODEL = "gemini-2.5-flash";

export default {
  client,
  GEMINI_MODEL,
};
