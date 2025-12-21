import {
  processBookingReminders,
  processExpiredBookings,
  processPendingMentorBookings,
} from '../controllers/booking.controller';

let intervalId: NodeJS.Timeout | null = null;
let isRunning = false;

async function runBookingJobs() {
  if (isRunning) return;
  isRunning = true;
  try {
    const [expiredCount, declinedCount, reminders] = await Promise.all([
      processExpiredBookings(),
      processPendingMentorBookings(),
      processBookingReminders(),
    ]);

    if (expiredCount || declinedCount || reminders.reminded24h || reminders.reminded1h) {
      console.log(
        `Booking jobs: expired=${expiredCount}, declined=${declinedCount}, reminders24h=${reminders.reminded24h}, reminders1h=${reminders.reminded1h}`
      );
    }
  } catch (err) {
    console.error('Booking jobs failed:', err);
  } finally {
    isRunning = false;
  }
}

export function startBookingJobs() {
  if (intervalId) return;
  const intervalSeconds = parseInt(process.env.BOOKING_JOB_INTERVAL_SECONDS || '60', 10) || 60;
  intervalId = setInterval(runBookingJobs, intervalSeconds * 1000);
  void runBookingJobs();
}

export function stopBookingJobs() {
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
  }
}
