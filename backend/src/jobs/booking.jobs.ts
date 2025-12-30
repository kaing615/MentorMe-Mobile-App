import { Queue, Worker } from 'bullmq';
import {
  processBookingReminders,
  processExpiredBookings,
  processPendingMentorBookings,
} from '../controllers/booking.controller';
import { processNoShowBookings } from '../services/session.service';
import { notifyBookingNoShow } from '../utils/notification.service';
import { getUserInfo } from '../utils/userInfo';

const QUEUE_NAME = process.env.BOOKING_QUEUE_NAME || 'booking-jobs';
const JOB_NAME = 'booking-tick';

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

async function runBookingJobs() {
  const [expiredCount, declinedCount, reminders, noShowCount] = await Promise.all([
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
  ]);

  if (
    expiredCount ||
    declinedCount ||
    noShowCount ||
    reminders.reminded24h ||
    reminders.reminded1h
  ) {
    console.log(
      `Booking jobs: expired=${expiredCount}, declined=${declinedCount}, noShow=${noShowCount}, reminders24h=${reminders.reminded24h}, reminders1h=${reminders.reminded1h}`
    );
  }
}

export async function startBookingJobs() {
  if (queue || worker) return;

  const connection = getConnectionOptions();
  queue = new Queue(QUEUE_NAME, { connection });
  const intervalSeconds = parseInt(process.env.BOOKING_JOB_INTERVAL_SECONDS || '60', 10) || 60;
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
      await runBookingJobs();
    },
    { connection, concurrency: 1 }
  );

  worker.on('error', (err) => {
    console.error('Booking jobs worker error:', err);
  });
}

export async function stopBookingJobs() {
  if (worker) {
    await worker.close();
    worker = null;
  }
  if (queue) {
    await queue.close();
    queue = null;
  }
}
