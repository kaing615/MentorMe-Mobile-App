import sgMail from "./sendGrid";

interface EmailParams {
  to: string;
  subject: string;
  html: string;
  attachments?: Array<{
    content: string;
    filename: string;
    type: string;
    disposition: string;
  }>;
}

/**
 * Send email using SendGrid
 */
export async function sendEmail(params: EmailParams): Promise<void> {
  const msg = {
    to: params.to,
    from: process.env.SENDGRID_FROM_EMAIL || "noreply@mentorme.app",
    subject: params.subject,
    html: params.html,
    attachments: params.attachments,
  };

  try {
    await sgMail.send(msg);
    console.log(`Email sent to ${params.to}: ${params.subject}`);
  } catch (error) {
    console.error("Error sending email:", error);
    throw error;
  }
}

/**
 * Send booking confirmation email with ICS attachment
 */
export async function sendBookingConfirmationEmail(
  recipientEmail: string,
  recipientName: string,
  bookingDetails: {
    id: string;
    mentorName: string;
    menteeName: string;
    topic: string;
    startTime: Date;
    endTime: Date;
    meetingLink?: string;
  },
  icsContent?: string
): Promise<void> {
  const startTime = bookingDetails.startTime.toLocaleString("en-US", {
    dateStyle: "full",
    timeStyle: "short",
    timeZone: "UTC",
  });

  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #4f46e5;">Your Booking is Confirmed! 🎉</h2>
      <p>Hi ${recipientName},</p>
      <p>Your mentoring session has been confirmed. Here are the details:</p>
      
      <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
        <p><strong>Booking ID:</strong> ${bookingDetails.id}</p>
        <p><strong>Topic:</strong> ${bookingDetails.topic}</p>
        <p><strong>Mentor:</strong> ${bookingDetails.mentorName}</p>
        <p><strong>Mentee:</strong> ${bookingDetails.menteeName}</p>
        <p><strong>Date & Time:</strong> ${startTime} (UTC)</p>
        ${bookingDetails.meetingLink ? `<p><strong>Meeting Link:</strong> <a href="${bookingDetails.meetingLink}">${bookingDetails.meetingLink}</a></p>` : ""}
      </div>
      
      <p>A calendar invite is attached to this email. Add it to your calendar to get a reminder!</p>
      <p>We look forward to your session!</p>
      <p>Best regards,<br/>The MentorMe Team</p>
    </div>
  `;

  const attachments = icsContent
    ? [
        {
          content: Buffer.from(icsContent).toString("base64"),
          filename: `booking-${bookingDetails.id}.ics`,
          type: "text/calendar",
          disposition: "attachment",
        },
      ]
    : undefined;

  await sendEmail({
    to: recipientEmail,
    subject: `Booking Confirmed: ${bookingDetails.topic}`,
    html,
    attachments,
  });
}

/**
 * Send booking failed email
 */
export async function sendBookingFailedEmail(
  recipientEmail: string,
  recipientName: string,
  bookingDetails: {
    id: string;
    topic: string;
    reason: string;
  }
): Promise<void> {
  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #dc2626;">Booking Failed</h2>
      <p>Hi ${recipientName},</p>
      <p>Unfortunately, your booking could not be completed.</p>
      
      <div style="background-color: #fef2f2; padding: 20px; border-radius: 8px; margin: 20px 0;">
        <p><strong>Booking ID:</strong> ${bookingDetails.id}</p>
        <p><strong>Topic:</strong> ${bookingDetails.topic}</p>
        <p><strong>Reason:</strong> ${bookingDetails.reason}</p>
      </div>
      
      <p>Please try booking again or contact support if you need assistance.</p>
      <p>Best regards,<br/>The MentorMe Team</p>
    </div>
  `;

  await sendEmail({
    to: recipientEmail,
    subject: `Booking Failed: ${bookingDetails.topic}`,
    html,
  });
}

/**
 * Send booking cancelled email
 */
export async function sendBookingCancelledEmail(
  recipientEmail: string,
  recipientName: string,
  bookingDetails: {
    id: string;
    topic: string;
    startTime: Date;
  }
): Promise<void> {
  const startTime = bookingDetails.startTime.toLocaleString("en-US", {
    dateStyle: "full",
    timeStyle: "short",
    timeZone: "UTC",
  });

  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
      <h2 style="color: #f59e0b;">Booking Cancelled</h2>
      <p>Hi ${recipientName},</p>
      <p>A booking has been cancelled:</p>
      
      <div style="background-color: #fffbeb; padding: 20px; border-radius: 8px; margin: 20px 0;">
        <p><strong>Booking ID:</strong> ${bookingDetails.id}</p>
        <p><strong>Topic:</strong> ${bookingDetails.topic}</p>
        <p><strong>Originally Scheduled:</strong> ${startTime} (UTC)</p>
      </div>
      
      <p>The time slot has been released and is now available for rebooking.</p>
      <p>Best regards,<br/>The MentorMe Team</p>
    </div>
  `;

  await sendEmail({
    to: recipientEmail,
    subject: `Booking Cancelled: ${bookingDetails.topic}`,
    html,
  });
}
