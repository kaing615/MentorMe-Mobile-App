import { IBooking } from "../models/booking.model";

/**
 * Generate ICS (iCalendar) file content for a booking
 */
export function generateICS(booking: IBooking, mentorName: string, menteeName: string): string {
  const now = new Date();
  const timestamp = now.toISOString().replace(/[-:]/g, "").split(".")[0] + "Z";
  
  const startDate = new Date(booking.startTime);
  const endDate = new Date(booking.endTime);
  
  const formatDate = (date: Date): string => {
    return date.toISOString().replace(/[-:]/g, "").split(".")[0] + "Z";
  };

  const uid = `booking-${booking._id}@mentorme.app`;
  const summary = `MentorMe Session: ${booking.topic}`;
  const description = `Booking ID: ${booking._id}\\nMentor: ${mentorName}\\nMentee: ${menteeName}\\nTopic: ${booking.topic}${booking.notes ? `\\nNotes: ${booking.notes}` : ""}`;
  const location = booking.meetingLink || "Online";

  const ics = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//MentorMe//Booking Calendar//EN",
    "CALSCALE:GREGORIAN",
    "METHOD:REQUEST",
    "BEGIN:VEVENT",
    `UID:${uid}`,
    `DTSTAMP:${timestamp}`,
    `DTSTART:${formatDate(startDate)}`,
    `DTEND:${formatDate(endDate)}`,
    `SUMMARY:${summary}`,
    `DESCRIPTION:${description}`,
    `LOCATION:${location}`,
    "STATUS:CONFIRMED",
    "SEQUENCE:0",
    "END:VEVENT",
    "END:VCALENDAR",
  ].join("\r\n");

  return ics;
}

/**
 * Generate ICS filename for download
 */
export function generateICSFilename(bookingId: string): string {
  return `mentorme-booking-${bookingId}.ics`;
}
