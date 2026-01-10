import { Queue, QueueEvents, Worker } from 'bullmq';
import {
    processBookingReminders,
    processExpiredBookings,
    processPendingMentorBookings,
} from '../controllers/booking.controller';
import noShowService from '../services/noShow.service';
import { processNoShowBookings } from '../services/session.service';
import { notifyBookingNoShow } from '../utils/notification.service';
import { getUserInfo } from '../utils/userInfo';

const QUEUE_NAME = process.env.BOOKING_QUEUE_NAME || 'booking-jobs';
const JOB_NAME = 'booking-tick';

let queue: Queue | null = null;
let worker: Worker | null = null;
let queueEvents: QueueEvents | null = null;

function getConnectionOptions() {
  return {
    host: process.env.REDIS_HOST,
    port: Number(process.env.REDIS_PORT) || 10938,
    username: 'default',
    password: process.env.REDIS_PASSWORD,
    maxRetriesPerRequest: null,
  };
}

async function runBookingJobs() {
  const timestamp = new Date().toISOString();
  console.log(`\n[${timestamp}] BOOKING_JOBS: Starting cycle`);
  
  try {
    const [expiredCount, declinedCount, reminders, noShowCount, noShowResults] = await Promise.all([
      processExpiredBookings(),
      processPendingMentorBookings(),
      processBookingReminders(),
      processNoShowBookings(async ({ booking, noShowBy }) => {
        const [mentorInfo, menteeInfo] = await Promise.all([
          getUserInfo(String(booking.mentor)),
          getUserInfo(String(booking.mentee)),
        ]);

        await notifyBookingNoShow({
          bookingId: String(booking._id),
          mentorId: String(booking.mentor),
          menteeId: String(booking.mentee),
          mentorName: mentorInfo.name,
          menteeName: menteeInfo.name,
          startTime: new Date(booking.startTime),
          noShowBy,
        });
      }),
      // New no-show detection service
      noShowService.checkAllNoShows(),
    ]);

    if (
      expiredCount ||
      declinedCount ||
      noShowCount ||
      noShowResults.length ||
      reminders.reminded24h ||
      reminders.reminded1h
    ) {
      console.log(
        `[${new Date().toISOString()}] BOOKING_JOBS: Processed - expired=${expiredCount}, declined=${declinedCount}, noShow=${noShowCount}, newNoShows=${noShowResults.length}, reminders24h=${reminders.reminded24h}, reminders1h=${reminders.reminded1h}`
      );
      
      // Log no-show details
      if (noShowResults.length > 0) {
        console.log(`[${new Date().toISOString()}] BOOKING_JOBS: No-show details:`, 
          noShowResults.map(r => ({
            bookingId: r.bookingId,
            status: r.status,
            refund: r.refundAmount,
            fee: r.platformFee,
          }))
        );
      }
    } else {
      console.log(`[${new Date().toISOString()}] BOOKING_JOBS: No bookings needed processing`);
    }
  } catch (error) {
    console.error(`[${new Date().toISOString()}] BOOKING_JOBS: Error during processing:`, error);
    throw error;
  }
}

export async function startBookingJobs() {
  if (queue || worker) {
    console.log('[BOOKING_JOBS] Already running, skipping initialization');
    return;
  }

  const connection = getConnectionOptions();
  const intervalSeconds = parseInt(process.env.BOOKING_JOB_INTERVAL_SECONDS || '60', 10) || 60;
  
  console.log(`[BOOKING_JOBS] Initializing with interval: ${intervalSeconds} seconds`);
  console.log(`[BOOKING_JOBS] Queue: ${QUEUE_NAME}, Redis: ${connection.host}:${connection.port}`);
  
  // Create queue first
  queue = new Queue(QUEUE_NAME, { connection });
  console.log('[BOOKING_JOBS] Queue created');
  
  // Create QueueEvents for debugging
  queueEvents = new QueueEvents(QUEUE_NAME, { connection });
  
  queueEvents.on('added', ({ jobId }) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: QueueEvent - Job ${jobId} added to queue`);
  });
  
  queueEvents.on('waiting', ({ jobId }) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: QueueEvent - Job ${jobId} is waiting`);
  });
  
  queueEvents.on('active', ({ jobId }) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: QueueEvent - Job ${jobId} is active`);
  });
  
  queueEvents.on('completed', ({ jobId }) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: QueueEvent - Job ${jobId} completed`);
  });
  
  queueEvents.on('failed', ({ jobId, failedReason }) => {
    console.error(`[${new Date().toISOString()}] BOOKING_JOBS: QueueEvent - Job ${jobId} failed: ${failedReason}`);
  });
  
  console.log('[BOOKING_JOBS] QueueEvents created');
  
  // Remove any existing repeatable jobs first to ensure clean slate
  try {
    const repeatableJobs = await queue.getRepeatableJobs();
    console.log(`[BOOKING_JOBS] Found ${repeatableJobs.length} existing repeatable jobs`);
    for (const job of repeatableJobs) {
      await queue.removeRepeatableByKey(job.key);
      console.log(`[BOOKING_JOBS] Removed existing repeatable job: ${job.key} (name: ${job.name})`);
    }
  } catch (err) {
    console.warn('[BOOKING_JOBS] Could not clean up existing jobs:', err);
  }

  // Create worker
  worker = new Worker(
    QUEUE_NAME,
    async (job) => {
      const timestamp = new Date().toISOString();
      
      // Force immediate log output
      process.stdout.write(`[${timestamp}] BOOKING_JOBS: >>> Worker processing job ${job.id} <<<\n`);
      console.log(`[${timestamp}] BOOKING_JOBS: Job name: "${job.name}", Expected: "${JOB_NAME}"`);
      console.log(`[${timestamp}] BOOKING_JOBS: Job data:`, JSON.stringify(job.data));
      
      if (job.name !== JOB_NAME) {
        console.log(`[${timestamp}] BOOKING_JOBS: Skipping - job name mismatch`);
        return;
      }
      
      console.log(`[${timestamp}] BOOKING_JOBS: Executing job processor...`);
      await runBookingJobs();
      console.log(`[${timestamp}] BOOKING_JOBS: Job execution completed`);
    },
    { connection, concurrency: 1 }
  );

  worker.on('active', (job) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: Worker active - processing job ${job.id}`);
  });

  worker.on('ready', () => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: Worker ready and listening`);
  });

  worker.on('error', (err) => {
    console.error(`[${new Date().toISOString()}] BOOKING_JOBS: Worker error:`, err);
  });

  worker.on('completed', (job) => {
    console.log(`[${new Date().toISOString()}] BOOKING_JOBS: Job ${job.id} completed successfully`);
  });

  worker.on('failed', (job, err) => {
    console.error(`[${new Date().toISOString()}] BOOKING_JOBS: Job ${job?.id} failed:`, err);
  });

  worker.on('stalled', (jobId) => {
    console.warn(`[${new Date().toISOString()}] BOOKING_JOBS: Job ${jobId} stalled`);
  });

  console.log('[BOOKING_JOBS] Worker created, waiting for ready state...');

  // Wait for worker to be ready
  await new Promise(resolve => setTimeout(resolve, 2000));

  // Check queue connection
  const waiting = await queue.getWaiting();
  const active = await queue.getActive();
  console.log(`[BOOKING_JOBS] Queue status - waiting: ${waiting.length}, active: ${active.length}`);

  // Add repeatable job
  await queue.add(
    JOB_NAME,
    {},
    {
      repeat: { every: intervalSeconds * 1000 },
      removeOnComplete: true,
      removeOnFail: 100,
    }
  );

  console.log(`[BOOKING_JOBS] Added repeatable job '${JOB_NAME}' (interval: ${intervalSeconds}s)`);

  // Add an immediate job to test the system
  const testJob = await queue.add(
    JOB_NAME,
    {},
    {
      removeOnComplete: true,
    }
  );
  console.log(`[BOOKING_JOBS] Added immediate test job ${testJob.id}`);
  
  // Check job state
  const jobState = await testJob.getState();
  console.log(`[BOOKING_JOBS] Test job ${testJob.id} state: ${jobState}`);
  
  console.log('[BOOKING_JOBS] Initialization complete - cronjob is now running');
}

export async function stopBookingJobs() {
  if (worker) {
    await worker.close();
    worker = null;
  }
  if (queueEvents) {
    await queueEvents.close();
    queueEvents = null;
  }
  if (queue) {
    await queue.close();
    queue = null;
  }
}
