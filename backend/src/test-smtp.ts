import transporter from "./utils/emailService";
import dotenv from "dotenv";

dotenv.config();

async function testSMTP() {
  console.log("🔍 Testing SMTP configuration...");
  console.log("SMTP_HOST:", process.env.SMTP_HOST);
  console.log("SMTP_PORT:", process.env.SMTP_PORT);
  console.log("SMTP_USER:", process.env.SMTP_USER);
  console.log(
    "SMTP_PASSWORD:",
    process.env.SMTP_PASSWORD ? "✅ Set (hidden)" : "❌ Not set"
  );

  try {
    // Test 1: Verify connection
    console.log("\n📡 Verifying SMTP connection...");
    await transporter.verify();
    console.log("✅ SMTP connection verified!");

    // Test 2: Send test email
    console.log("\n📧 Sending test email.. .");
    const info = await transporter.sendMail({
      from: `"MentorMe Test" <${process.env.SMTP_USER}>`,
      to: process.env.SMTP_USER, // Send to yourself
      subject: "SMTP Test - MentorMe",
      text: "If you receive this, SMTP is working!",
      html: "<b>If you receive this, SMTP is working!</b>",
    });

    console.log("✅ Test email sent! ");
    console.log("Message ID:", info.messageId);
  } catch (error: any) {
    console.error("❌ SMTP test failed:", error.message);
    console.error("Full error:", error);
  }
}

testSMTP();
