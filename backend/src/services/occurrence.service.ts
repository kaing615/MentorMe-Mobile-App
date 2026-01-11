// Service to manage availability occurrences (auto-close past occurrences)
import AvailabilityOccurrence from '../models/availabilityOccurrence.model';

/**
 * Auto-delete past occurrences that are still marked as "open"
 * This prevents mentors from appearing as "available" for slots that have already passed
 * We delete (not close) to keep the database clean
 */
export async function closePastOccurrences(): Promise<number> {
  const now = new Date();

  const result = await AvailabilityOccurrence.deleteMany({
    status: 'open',
    start: { $lt: now } // Occurrence start time is in the past
  });

  return result.deletedCount || 0;
}

/**
 * Get count of occurrences that need to be closed
 */
export async function countPastOpenOccurrences(): Promise<number> {
  const now = new Date();

  return await AvailabilityOccurrence.countDocuments({
    status: 'open',
    start: { $lt: now }
  });
}

