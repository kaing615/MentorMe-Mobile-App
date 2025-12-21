// path: src/utils/ics.service.ts

export interface IcsEventData {
  uid: string;
  summary: string;
  description: string;
  startTime: Date;
  endTime: Date;
  location?: string;
  organizerName: string;
  organizerEmail: string;
  attendeeName: string;
  attendeeEmail: string;
}

function formatIcsDate(d: Date): string {
  return d.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';
}

function escapeIcsText(text: string): string {
  return text.replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\n/g, '\\n');
}

export function generateIcsContent(data: IcsEventData): string {
  const {
    uid,
    summary,
    description,
    startTime,
    endTime,
    location,
    organizerName,
    organizerEmail,
    attendeeName,
    attendeeEmail,
  } = data;

  const now = new Date();
  const dtstamp = formatIcsDate(now);
  const dtstart = formatIcsDate(startTime);
  const dtend = formatIcsDate(endTime);

  const lines: string[] = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//MentorMe//Booking//EN',
    'CALSCALE:GREGORIAN',
    'METHOD:REQUEST',
    'BEGIN:VEVENT',
    `UID:${uid}@mentorme.app`,
    `DTSTAMP:${dtstamp}`,
    `DTSTART:${dtstart}`,
    `DTEND:${dtend}`,
    `SUMMARY:${escapeIcsText(summary)}`,
    `DESCRIPTION:${escapeIcsText(description)}`,
  ];

  if (location) {
    lines.push(`LOCATION:${escapeIcsText(location)}`);
  }

  lines.push(
    `ORGANIZER;CN=${escapeIcsText(organizerName)}:mailto:${organizerEmail}`,
    `ATTENDEE;CN=${escapeIcsText(attendeeName)};RSVP=TRUE:mailto:${attendeeEmail}`,
    'STATUS:CONFIRMED',
    'SEQUENCE:0',
    'END:VEVENT',
    'END:VCALENDAR'
  );

  return lines.join('\r\n');
}

export function generateBookingIcs(
  bookingId: string,
  mentorName: string,
  mentorEmail: string,
  menteeName: string,
  menteeEmail: string,
  startTime: Date,
  endTime: Date,
  meetingLink?: string,
  location?: string,
  topic?: string
): string {
  const summary = `MentorMe Session: ${mentorName} & ${menteeName}`;
  const descriptionParts = [
    `Booking ID: ${bookingId}`,
    `Mentor: ${mentorName}`,
    `Mentee: ${menteeName}`,
  ];

  if (topic) {
    descriptionParts.push(`Topic: ${topic}`);
  }

  if (meetingLink) {
    descriptionParts.push(`Meeting Link: ${meetingLink}`);
  }

  if (location && !meetingLink) {
    descriptionParts.push(`Location: ${location}`);
  }

  return generateIcsContent({
    uid: `booking-${bookingId}`,
    summary,
    description: descriptionParts.join('\n'),
    startTime,
    endTime,
    location: meetingLink || location,
    organizerName: mentorName,
    organizerEmail: mentorEmail,
    attendeeName: menteeName,
    attendeeEmail: menteeEmail,
  });
}
