import sgMail from "@sendgrid/mail";

const sendGridApiKey = process.env.SENDGRID_API_KEY;

if (!sendGridApiKey) {
  throw new Error("SendGrid API key is not configured");
}

sgMail.setApiKey(sendGridApiKey);

export default sgMail;
