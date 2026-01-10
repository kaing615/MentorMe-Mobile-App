import { Queue, Worker } from 'bullmq';
import Notification from '../models/notification.model';

const QUEUE_NAME = process.env.NOTIFICATION_CLEANUP_QUEUE_NAME || 'notification-cleanup';
const JOB_NAME = 'notification-cleanup';
const DEFAULT_INTERVAL_SECONDS = 6 * 60 * 60;
const DAY_MS = 24 * 60 * 60 * 1000;

let queue: Queue | null = null;
let worker: Worker | null = null;

function getConnectionOptions() {
  return {
    host: process.env.REDIS_HOST,
    port: Number(process.env.REDIS_PORT) || 10938,
    username: 'default',
    password: process.env.REDIS_PASSWORD,
    maxRetriesPerRequest: null,
  };
}

async function runNotificationCleanup() {
  const retentionDays = parseInt(process.env.NOTIFICATION_RETENTION_DAYS || '3', 10) || 3;
  const cutoff = new Date(Date.now() - retentionDays * DAY_MS);

  const result = await Notification.deleteMany({
    read: true,
    $or: [
      { readAt: { $lte: cutoff } },
      { readAt: { $exists: false }, updatedAt: { $lte: cutoff } },
    ],
  });

  if (result.deletedCount && result.deletedCount > 0) {
    console.log(`[Notification cleanup] Removed ${result.deletedCount} old read notifications.`);
  }
}

export async function startNotificationCleanupJobs() {
  if (queue || worker) return;

  const connection = getConnectionOptions();
  queue = new Queue(QUEUE_NAME, { connection });
  const intervalSeconds =
    parseInt(process.env.NOTIFICATION_CLEANUP_INTERVAL_SECONDS || String(DEFAULT_INTERVAL_SECONDS), 10) ||
    DEFAULT_INTERVAL_SECONDS;

  await queue.add(
    JOB_NAME,
    {},
    {
      jobId: JOB_NAME,
      repeat: { every: intervalSeconds * 1000 },
      removeOnComplete: true,
      removeOnFail: 100,
    }
  );

  worker = new Worker(
    QUEUE_NAME,
    async (job) => {
      if (job.name !== JOB_NAME) return;
      await runNotificationCleanup();
    },
    { connection, concurrency: 1 }
  );

  worker.on('error', (err) => {
    console.error('Notification cleanup worker error:', err);
  });
}

export async function stopNotificationCleanupJobs() {
  if (worker) {
    await worker.close();
    worker = null;
  }
  if (queue) {
    await queue.close();
    queue = null;
  }
}
