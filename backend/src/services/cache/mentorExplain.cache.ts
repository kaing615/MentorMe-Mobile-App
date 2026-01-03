import redis from "../../utils/redis";

const TTL = 60 * 60 * 24 * 7;

export async function getCachedExplain(key: string): Promise<string | null> {
  try {
    return await redis.get(key);
  } catch (err) {
    console.error("❌ Redis GET error", err);
    return null;
  }
}

export async function setCachedExplain(
  key: string,
  value: string
): Promise<void> {
  try {
    await redis.set(key, value, { EX: TTL });
  } catch (err) {
    console.error("❌ Redis SET error", err);
  }
}
