import sgMail from "@sendgrid/mail";

type SendGridMailer = Pick<typeof sgMail, "send">;

const env = (process.env.NODE_ENV || "").toLowerCase();
const emailDisabled =
  env === "test" || (process.env.EMAIL_DISABLED || "").toLowerCase() === "true";

const mailer: SendGridMailer = (() => {
  if (emailDisabled) {
    console.warn(
      "SendGrid disabled (EMAIL_DISABLED=true or NODE_ENV=test); skipping initialization."
    );
    return { send: async () => ({}) as any };
  }

  const sendGridApiKey = process.env.SENDGRID_API_KEY;
  if (!sendGridApiKey) {
    throw new Error("SendGrid API key is not configured");
  }

  sgMail.setApiKey(sendGridApiKey);
  return sgMail;
})();

export default mailer;
