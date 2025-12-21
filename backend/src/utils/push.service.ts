import fs from 'fs';
import path from 'path';
import admin from 'firebase-admin';
import DeviceToken from '../models/deviceToken.model';

export interface PushPayload {
  title: string;
  body: string;
  data?: Record<string, string>;
}

export interface PushResult {
  sent: number;
  failed: number;
  invalid: number;
  skipped: number;
}

const MAX_TOKENS_PER_BATCH = 500;
let cachedApp: admin.app.App | null = null;

function parseServiceAccount(raw: string) {
  try {
    return JSON.parse(raw);
  } catch {
    try {
      const decoded = Buffer.from(raw, 'base64').toString('utf8');
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }
}

function loadServiceAccount() {
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  if (raw) {
    return parseServiceAccount(raw);
  }

  const filePath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (filePath) {
    const resolved = path.isAbsolute(filePath)
      ? filePath
      : path.resolve(process.cwd(), filePath);
    const content = fs.readFileSync(resolved, 'utf8');
    return parseServiceAccount(content);
  }

  return null;
}

function getFirebaseApp(): admin.app.App | null {
  if (cachedApp) return cachedApp;
  if (admin.apps.length > 0) {
    cachedApp = admin.apps[0]!;
    return cachedApp;
  }

  const serviceAccount = loadServiceAccount();
  if (!serviceAccount) {
    console.warn('FCM disabled: missing service account credentials');
    return null;
  }

  cachedApp = admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  return cachedApp;
}

function normalizeData(data?: Record<string, string>) {
  if (!data) return undefined;
  const normalized: Record<string, string> = {};
  for (const [key, value] of Object.entries(data)) {
    normalized[key] = String(value);
  }
  return normalized;
}

function chunk<T>(items: T[], size: number) {
  const batches: T[][] = [];
  for (let i = 0; i < items.length; i += size) {
    batches.push(items.slice(i, i + size));
  }
  return batches;
}

export async function sendPushToTokens(
  tokens: string[],
  payload: PushPayload
): Promise<PushResult> {
  if (tokens.length === 0) {
    return { sent: 0, failed: 0, invalid: 0, skipped: 0 };
  }

  const app = getFirebaseApp();
  if (!app) {
    return { sent: 0, failed: 0, invalid: 0, skipped: tokens.length };
  }

  const messaging = admin.messaging(app);
  const data = normalizeData(payload.data);
  let sent = 0;
  let failed = 0;
  let invalid = 0;

  for (const batch of chunk(tokens, MAX_TOKENS_PER_BATCH)) {
    const response = await messaging.sendEachForMulticast({
      tokens: batch,
      notification: {
        title: payload.title,
        body: payload.body,
      },
      data,
    });

    sent += response.successCount;
    failed += response.failureCount;

    const invalidTokens: string[] = [];
    response.responses.forEach((res, index) => {
      if (!res.success) {
        const code = res.error?.code;
        if (
          code === 'messaging/invalid-registration-token' ||
          code === 'messaging/registration-token-not-registered'
        ) {
          invalidTokens.push(batch[index]);
        }
      }
    });

    if (invalidTokens.length > 0) {
      invalid += invalidTokens.length;
      await DeviceToken.deleteMany({ token: { $in: invalidTokens } });
    }
  }

  return { sent, failed, invalid, skipped: 0 };
}

export async function sendPushToUser(
  userId: string,
  payload: PushPayload
): Promise<PushResult> {
  const tokens = await DeviceToken.find({ user: userId })
    .select('token')
    .lean();
  const tokenList = tokens.map((t) => t.token).filter(Boolean);
  return sendPushToTokens(tokenList, payload);
}
