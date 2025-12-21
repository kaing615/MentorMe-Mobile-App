// path: src/utils/email.service.ts
import sgMail from './sendGrid';

export interface BookingEmailData {
  mentorName: string;
  menteeName: string;
  mentorEmail: string;
  menteeEmail: string;
  startTime: Date;
  endTime: Date;
  topic?: string;
  meetingLink?: string;
  location?: string;
  bookingId: string;
  price: number;
}

const formatDateTime = (d: Date) =>
  d.toLocaleString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'UTC',
    timeZoneName: 'short',
  });

async function sendEmail(
  to: string,
  subject: string,
  text: string,
  html: string,
  attachments?: { content: string; filename: string; type: string; disposition: string }[]
) {
  const from = process.env.EMAIL_FROM || 'kainguyen615@gmail.com';
  await sgMail.send({
    to,
    from,
    subject,
    text,
    html,
    attachments,
    categories: ['booking'],
  });
}

export async function sendBookingConfirmedEmail(data: BookingEmailData, icsContent: string) {
  const { mentorName, menteeName, menteeEmail, mentorEmail, startTime, endTime, topic, meetingLink, location, bookingId, price } = data;
  const dateStr = formatDateTime(startTime);
  const endStr = formatDateTime(endTime);
  const locationOrLink = meetingLink || location || 'To be announced';

  const subject = `MentorMe • Booking Confirmed - ${dateStr}`;

  // Email to mentee
  const menteeText = `Hi ${menteeName},\n\nYour booking with ${mentorName} is confirmed!\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nLocation/Link: ${locationOrLink}\nPrice: $${price.toFixed(2)}\nBooking ID: ${bookingId}\n\nWe've attached a calendar invite (.ics) for your convenience.\n\n— MentorMe Team`;

  const menteeHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#10B981;">✅ Booking Confirmed</h2>
  <p>Hi ${menteeName},</p>
  <p>Your booking with <strong>${mentorName}</strong> is confirmed!</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Location/Link:</td><td style="padding:4px 8px;">${locationOrLink}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Price:</td><td style="padding:4px 8px;">$${price.toFixed(2)}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p>We've attached a calendar invite (.ics) for your convenience.</p>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  const attachment = {
    content: Buffer.from(icsContent).toString('base64'),
    filename: `mentorme-booking-${bookingId}.ics`,
    type: 'text/calendar',
    disposition: 'attachment',
  };

  await sendEmail(menteeEmail, subject, menteeText, menteeHtml, [attachment]);

  // Email to mentor
  const mentorText = `Hi ${mentorName},\n\nA new booking has been confirmed!\n\nMentee: ${menteeName}\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;

  const mentorHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#10B981;">✅ New Booking Confirmed</h2>
  <p>Hi ${mentorName},</p>
  <p>A new booking has been confirmed with <strong>${menteeName}</strong>!</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Mentee:</td><td style="padding:4px 8px;">${menteeName}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(mentorEmail, `MentorMe • New Booking - ${dateStr}`, mentorText, mentorHtml, [attachment]);
}

export async function sendBookingFailedEmail(data: BookingEmailData) {
  const { menteeName, menteeEmail, mentorName, startTime, bookingId } = data;
  const dateStr = formatDateTime(startTime);

  const subject = `MentorMe • Booking Failed - ${dateStr}`;
  const text = `Hi ${menteeName},\n\nUnfortunately, your booking with ${mentorName} could not be completed.\n\nBooking ID: ${bookingId}\nScheduled: ${dateStr}\n\nThe time slot has been released and you can try booking again.\n\n— MentorMe Team`;
  const html = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#EF4444;">❌ Booking Failed</h2>
  <p>Hi ${menteeName},</p>
  <p>Unfortunately, your booking with <strong>${mentorName}</strong> could not be completed.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Scheduled:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
  </table>
  <p>The time slot has been released and you can try booking again.</p>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(menteeEmail, subject, text, html);
}

export async function sendBookingCancelledEmail(data: BookingEmailData, cancelledByMentor: boolean) {
  const { mentorName, menteeName, menteeEmail, mentorEmail, startTime, bookingId, price } = data;
  const dateStr = formatDateTime(startTime);
  const cancelledBy = cancelledByMentor ? mentorName : menteeName;

  const subject = `MentorMe • Booking Cancelled - ${dateStr}`;

  // Email to mentee
  const menteeText = `Hi ${menteeName},\n\nYour booking has been cancelled.\n\nCancelled by: ${cancelledBy}\nMentor: ${mentorName}\nScheduled: ${dateStr}\nBooking ID: ${bookingId}\n\nIf a payment was made, a refund will be processed.\n\n— MentorMe Team`;
  const menteeHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#EF4444;">❌ Booking Cancelled</h2>
  <p>Hi ${menteeName},</p>
  <p>Your booking has been cancelled.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Cancelled by:</td><td style="padding:4px 8px;">${cancelledBy}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Mentor:</td><td style="padding:4px 8px;">${mentorName}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Scheduled:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p>If a payment was made, a refund will be processed.</p>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(menteeEmail, subject, menteeText, menteeHtml);

  // Email to mentor
  const mentorText = `Hi ${mentorName},\n\nA booking has been cancelled.\n\nCancelled by: ${cancelledBy}\nMentee: ${menteeName}\nScheduled: ${dateStr}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;
  const mentorHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#EF4444;">❌ Booking Cancelled</h2>
  <p>Hi ${mentorName},</p>
  <p>A booking has been cancelled.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Cancelled by:</td><td style="padding:4px 8px;">${cancelledBy}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Mentee:</td><td style="padding:4px 8px;">${menteeName}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Scheduled:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(mentorEmail, subject, mentorText, mentorHtml);
}

export async function resendBookingIcsEmail(data: BookingEmailData, icsContent: string) {
  const { menteeName, menteeEmail, mentorName, startTime, bookingId } = data;
  const dateStr = formatDateTime(startTime);

  const subject = `MentorMe • Calendar Invite - ${dateStr}`;
  const text = `Hi ${menteeName},\n\nHere's your calendar invite for your session with ${mentorName}.\n\nDate: ${dateStr}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;
  const html = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#3B82F6;">📅 Calendar Invite</h2>
  <p>Hi ${menteeName},</p>
  <p>Here's your calendar invite for your session with <strong>${mentorName}</strong>.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  const attachment = {
    content: Buffer.from(icsContent).toString('base64'),
    filename: `mentorme-booking-${bookingId}.ics`,
    type: 'text/calendar',
    disposition: 'attachment',
  };

  await sendEmail(menteeEmail, subject, text, html, [attachment]);
}

export async function sendBookingReminderEmail(data: BookingEmailData, hoursLeft: number) {
  const { mentorName, menteeName, menteeEmail, mentorEmail, startTime, endTime, topic, meetingLink, location, bookingId } = data;
  const dateStr = formatDateTime(startTime);
  const endStr = formatDateTime(endTime);
  const locationOrLink = meetingLink || location || 'To be announced';

  const subject = `MentorMe • Session Reminder (${hoursLeft}h) - ${dateStr}`;

  const menteeText = `Hi ${menteeName},\n\nReminder: your session with ${mentorName} starts in about ${hoursLeft} hour(s).\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nLocation/Link: ${locationOrLink}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;
  const menteeHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#3B82F6;">Session Reminder</h2>
  <p>Hi ${menteeName},</p>
  <p>Reminder: your session with <strong>${mentorName}</strong> starts in about ${hoursLeft} hour(s).</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Location/Link:</td><td style="padding:4px 8px;">${locationOrLink}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(menteeEmail, subject, menteeText, menteeHtml);

  const mentorText = `Hi ${mentorName},\n\nReminder: your session with ${menteeName} starts in about ${hoursLeft} hour(s).\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;
  const mentorHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#3B82F6;">Session Reminder</h2>
  <p>Hi ${mentorName},</p>
  <p>Reminder: your session with <strong>${menteeName}</strong> starts in about ${hoursLeft} hour(s).</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(mentorEmail, subject, mentorText, mentorHtml);
}

export async function sendBookingPendingEmail(data: BookingEmailData, deadline?: Date) {
  const { mentorName, menteeName, menteeEmail, mentorEmail, startTime, endTime, topic, meetingLink, location, bookingId } = data;
  const dateStr = formatDateTime(startTime);
  const endStr = formatDateTime(endTime);
  const deadlineStr = deadline ? formatDateTime(deadline) : null;
  const locationOrLink = meetingLink || location || 'To be announced';

  const subject = `MentorMe • Booking Awaiting Confirmation - ${dateStr}`;

  const menteeText = `Hi ${menteeName},\n\nYour booking with ${mentorName} is awaiting mentor confirmation.\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nLocation/Link: ${locationOrLink}\nBooking ID: ${bookingId}\n${deadlineStr ? `Mentor response deadline: ${deadlineStr}\n` : ''}\n— MentorMe Team`;
  const menteeHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#F59E0B;">Booking Awaiting Confirmation</h2>
  <p>Hi ${menteeName},</p>
  <p>Your booking with <strong>${mentorName}</strong> is awaiting mentor confirmation.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Location/Link:</td><td style="padding:4px 8px;">${locationOrLink}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
    ${deadlineStr ? `<tr><td style="padding:4px 8px;color:#6b7280;">Response by:</td><td style="padding:4px 8px;">${deadlineStr}</td></tr>` : ''}
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(menteeEmail, subject, menteeText, menteeHtml);

  const mentorText = `Hi ${mentorName},\n\n${menteeName} requested a session and is waiting for your confirmation.\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nLocation/Link: ${locationOrLink}\nBooking ID: ${bookingId}\n${deadlineStr ? `Please respond by: ${deadlineStr}\n` : ''}\n— MentorMe Team`;
  const mentorHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#F59E0B;">Confirm New Booking</h2>
  <p>Hi ${mentorName},</p>
  <p><strong>${menteeName}</strong> requested a session and is waiting for your confirmation.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Location/Link:</td><td style="padding:4px 8px;">${locationOrLink}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
    ${deadlineStr ? `<tr><td style="padding:4px 8px;color:#6b7280;">Response by:</td><td style="padding:4px 8px;">${deadlineStr}</td></tr>` : ''}
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(mentorEmail, subject, mentorText, mentorHtml);
}

export async function sendBookingDeclinedEmail(data: BookingEmailData) {
  const { mentorName, menteeName, menteeEmail, mentorEmail, startTime, endTime, topic, meetingLink, location, bookingId } = data;
  const dateStr = formatDateTime(startTime);
  const endStr = formatDateTime(endTime);
  const locationOrLink = meetingLink || location || 'To be announced';

  const subject = `MentorMe • Booking Declined - ${dateStr}`;

  const menteeText = `Hi ${menteeName},\n\nYour booking with ${mentorName} was declined.\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nLocation/Link: ${locationOrLink}\nBooking ID: ${bookingId}\n\nYou can pick another slot or mentor anytime.\n\n— MentorMe Team`;
  const menteeHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#EF4444;">Booking Declined</h2>
  <p>Hi ${menteeName},</p>
  <p>Your booking with <strong>${mentorName}</strong> was declined.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Location/Link:</td><td style="padding:4px 8px;">${locationOrLink}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p>You can pick another slot or mentor anytime.</p>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(menteeEmail, subject, menteeText, menteeHtml);

  const mentorText = `Hi ${mentorName},\n\nYou declined a booking request from ${menteeName}.\n\nDate: ${dateStr}\nEnd: ${endStr}\nTopic: ${topic || 'N/A'}\nBooking ID: ${bookingId}\n\n— MentorMe Team`;
  const mentorHtml = `
<div style="font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:600px;margin:0 auto;">
  <h2 style="color:#EF4444;">Booking Declined</h2>
  <p>Hi ${mentorName},</p>
  <p>You declined the booking request from <strong>${menteeName}</strong>.</p>
  <table style="margin:16px 0;border-collapse:collapse;">
    <tr><td style="padding:4px 8px;color:#6b7280;">Date:</td><td style="padding:4px 8px;">${dateStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">End:</td><td style="padding:4px 8px;">${endStr}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Topic:</td><td style="padding:4px 8px;">${topic || 'N/A'}</td></tr>
    <tr><td style="padding:4px 8px;color:#6b7280;">Booking ID:</td><td style="padding:4px 8px;">${bookingId}</td></tr>
  </table>
  <p style="color:#6b7280;font-size:12px;margin-top:24px;">— MentorMe Team</p>
</div>`;

  await sendEmail(mentorEmail, subject, mentorText, mentorHtml);
}
