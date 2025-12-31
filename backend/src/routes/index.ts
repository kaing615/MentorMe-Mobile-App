import { Router } from "express";

import userRouter from "./user.route";
import bookingRouter from "./booking.route";
import availabilityRouter from "./availability.route";
import mentorRouter from "./mentor.route";
import mentorStatsRouter from "./mentor.stats.route";
import walletRouter from "./wallet.route";
import walletTopupRouter from "./walletTopup.route";

import payoutRouter from "./payout.route";
import adminPayoutRouter from "./adminPayout.route";
import webhookRouter from "./webhook.route";

import paymentRouter from "./payment.route";
import profileRouter from "./profile.route";
import reportRouter from "./report.route";
import homeRouter from "./home.route";
import presenceRouter from "./presence.route";
import reviewRouter from "./review.route";
import notificationRouter from "./notification.route";
import paymentMethodRouter from "./paymentMethod.route";
import sessionRouter from "./session.route";
import messageRouter from "./message.route";

const router = Router();

// Auth + user
router.use("/auth", userRouter);
router.use("/users", userRouter);

// Core booking / profile
router.use("/bookings", bookingRouter);
router.use("/reports", reportRouter);
router.use("/profile", profileRouter);
router.use("/availability", availabilityRouter);
router.use("/mentors", mentorStatsRouter);
router.use("/mentors", mentorRouter);

// Wallet & payments
router.use("/wallet", walletRouter);
router.use("/wallet", walletTopupRouter);
router.use("/payments", paymentRouter);
router.use("/payment-methods", paymentMethodRouter);

// Payout mentor & webhooks
router.use("/payouts", payoutRouter);
router.use("/admin/payouts", adminPayoutRouter);
router.use("/webhooks", webhookRouter);

// Home stats & presence
router.use("/home", homeRouter);
router.use("/presence", presenceRouter);

// Reviews
router.use("/", reviewRouter);

// Notifications (device tokens, push)
router.use("/notifications", notificationRouter);

// Sessions (WebRTC)
router.use("/sessions", sessionRouter);

// Messages (chat)
router.use("/messages", messageRouter);

export default router;
