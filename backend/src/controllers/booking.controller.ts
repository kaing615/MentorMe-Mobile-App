import { Request, Response } from "express";
import mongoose from "mongoose";
import Booking, { BookingStatus } from "../models/booking.model";
import User from "../models/user.model";
import AvailabilityOccurrence from "../models/availabilityOccurrence.model";
import { generateICS } from "../utils/ics";
import {
  sendBookingConfirmationEmail,
  sendBookingFailedEmail,
  sendBookingCancelledEmail,
} from "../utils/email";

// Booking expiry time: bookings in PaymentPending status expire after this duration
const BOOKING_EXPIRY_MINUTES = 15;

/**
 * Create a new booking with PaymentPending status
 * Locks the slot immediately using transactions
 * 
 * Flow:
 * 1. Validate mentor exists
 * 2. Find available occurrence
 * 3. Check for conflicting bookings (prevent double booking)
 * 4. Lock occurrence by setting status to "booked"
 * 5. Create booking with PaymentPending status and expiry time
 * 6. Commit transaction (atomic operation)
 */
export const createBooking = async (req: Request, res: Response) => {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const { mentorId, scheduledAt, duration, topic, notes } = req.body;
    const menteeId = (req as any).user.id;

    if (!mentorId || !scheduledAt || !duration || !topic) {
      await session.abortTransaction();
      return res.status(400).json({ error: "Missing required fields" });
    }

    // Parse dates
    const startTime = new Date(scheduledAt);
    const endTime = new Date(startTime.getTime() + duration * 60000);
    const expiresAt = new Date(Date.now() + BOOKING_EXPIRY_MINUTES * 60000);

    // Verify mentor exists
    const mentor = await User.findById(mentorId).session(session);
    if (!mentor || mentor.role !== "mentor") {
      await session.abortTransaction();
      return res.status(404).json({ error: "Mentor not found" });
    }

    // Find available occurrence for this time slot
    const occurrence = await AvailabilityOccurrence.findOne({
      mentor: new mongoose.Types.ObjectId(mentorId),
      start: startTime,
      status: "open",
    }).session(session);

    // Check for conflicting bookings (prevent double booking)
    const conflictingBooking = await Booking.findOne({
      mentorId: new mongoose.Types.ObjectId(mentorId),
      status: { $in: ["PaymentPending", "Confirmed"] },
      $or: [
        { startTime: { $lt: endTime }, endTime: { $gt: startTime } },
      ],
    }).session(session);

    if (conflictingBooking) {
      await session.abortTransaction();
      return res.status(409).json({ error: "Time slot is already booked" });
    }

    // Lock the occurrence if found
    if (occurrence) {
      occurrence.status = "booked";
      await occurrence.save({ session });
    }

    // Calculate price (you can customize this logic)
    const hourlyRate = 50; // Default rate, should fetch from mentor profile
    const price = (hourlyRate * duration) / 60;

    // Create booking
    const booking = new Booking({
      menteeId: new mongoose.Types.ObjectId(menteeId),
      mentorId: new mongoose.Types.ObjectId(mentorId),
      occurrenceId: occurrence?._id,
      scheduledAt: startTime,
      startTime,
      endTime,
      duration,
      price,
      topic,
      notes,
      status: "PaymentPending",
      expiresAt,
    });

    await booking.save({ session });
    await session.commitTransaction();

    // Return booking data
    res.status(201).json({
      id: booking._id.toString(),
      menteeId: booking.menteeId.toString(),
      mentorId: booking.mentorId.toString(),
      date: startTime.toISOString().split("T")[0],
      startTime: startTime.toISOString().split("T")[1].substring(0, 5),
      endTime: endTime.toISOString().split("T")[1].substring(0, 5),
      status: booking.status,
      price: booking.price,
      notes: booking.notes,
      createdAt: booking.createdAt.toISOString(),
    });
  } catch (error) {
    await session.abortTransaction();
    console.error("Error creating booking:", error);
    res.status(500).json({ error: "Failed to create booking" });
  } finally {
    session.endSession();
  }
};

/**
 * Get bookings for current user (mentee or mentor)
 * Supports filtering by status and role
 */
export const getBookings = async (req: Request, res: Response) => {
  try {
    const userId = (req as any).user.id;
    const userRole = (req as any).user.role;
    const { status, role, page = 1, limit = 10 } = req.query;

    const query: any = {};

    // Filter by role
    if (role === "mentee" || userRole === "mentee") {
      query.menteeId = new mongoose.Types.ObjectId(userId);
    } else if (role === "mentor" || userRole === "mentor") {
      query.mentorId = new mongoose.Types.ObjectId(userId);
    } else {
      // If no role specified, get all bookings for this user
      query.$or = [
        { menteeId: new mongoose.Types.ObjectId(userId) },
        { mentorId: new mongoose.Types.ObjectId(userId) },
      ];
    }

    // Filter by status
    if (status) {
      query.status = status;
    }

    const skip = (Number(page) - 1) * Number(limit);
    const bookings = await Booking.find(query)
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(Number(limit))
      .populate("menteeId", "userName email")
      .populate("mentorId", "userName email");

    const total = await Booking.countDocuments(query);

    res.json({
      bookings: bookings.map((b) => ({
        id: b._id.toString(),
        menteeId: b.menteeId.toString(),
        mentorId: b.mentorId.toString(),
        date: b.startTime.toISOString().split("T")[0],
        startTime: b.startTime.toISOString().split("T")[1].substring(0, 5),
        endTime: b.endTime.toISOString().split("T")[1].substring(0, 5),
        status: b.status,
        price: b.price,
        notes: b.notes,
        createdAt: b.createdAt.toISOString(),
      })),
      total,
      page: Number(page),
      totalPages: Math.ceil(total / Number(limit)),
    });
  } catch (error) {
    console.error("Error fetching bookings:", error);
    res.status(500).json({ error: "Failed to fetch bookings" });
  }
};

/**
 * Get booking by ID
 */
export const getBookingById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const userId = (req as any).user.id;

    const booking = await Booking.findById(id)
      .populate("menteeId", "userName email")
      .populate("mentorId", "userName email");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Check authorization
    if (
      booking.menteeId._id.toString() !== userId &&
      booking.mentorId._id.toString() !== userId
    ) {
      return res.status(403).json({ error: "Not authorized" });
    }

    res.json({
      id: booking._id.toString(),
      menteeId: booking.menteeId._id.toString(),
      mentorId: booking.mentorId._id.toString(),
      date: booking.startTime.toISOString().split("T")[0],
      startTime: booking.startTime.toISOString().split("T")[1].substring(0, 5),
      endTime: booking.endTime.toISOString().split("T")[1].substring(0, 5),
      status: booking.status,
      price: booking.price,
      notes: booking.notes,
      createdAt: booking.createdAt.toISOString(),
    });
  } catch (error) {
    console.error("Error fetching booking:", error);
    res.status(500).json({ error: "Failed to fetch booking" });
  }
};

/**
 * Cancel a booking and release the slot
 */
export const cancelBooking = async (req: Request, res: Response) => {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const { id } = req.params;
    const userId = (req as any).user.id;

    const booking = await Booking.findById(id)
      .populate("menteeId", "userName email")
      .populate("mentorId", "userName email")
      .session(session);

    if (!booking) {
      await session.abortTransaction();
      return res.status(404).json({ error: "Booking not found" });
    }

    // Check authorization
    if (
      booking.menteeId._id.toString() !== userId &&
      booking.mentorId._id.toString() !== userId
    ) {
      await session.abortTransaction();
      return res.status(403).json({ error: "Not authorized" });
    }

    // Validate state transition
    if (!isValidTransition(booking.status, "Cancelled")) {
      await session.abortTransaction();
      return res.status(400).json({
        error: `Cannot cancel booking with status ${booking.status}`,
      });
    }

    // Update booking status
    booking.status = "Cancelled";
    await booking.save({ session });

    // Release the slot
    if (booking.occurrenceId) {
      await AvailabilityOccurrence.findByIdAndUpdate(
        booking.occurrenceId,
        { status: "open" },
        { session }
      );
    }

    await session.commitTransaction();

    // Send cancellation emails
    try {
      const mentee = booking.menteeId as any;
      const mentor = booking.mentorId as any;

      await Promise.all([
        sendBookingCancelledEmail(mentee.email, mentee.userName, {
          id: booking._id.toString(),
          topic: booking.topic,
          startTime: booking.startTime,
        }),
        sendBookingCancelledEmail(mentor.email, mentor.userName, {
          id: booking._id.toString(),
          topic: booking.topic,
          startTime: booking.startTime,
        }),
      ]);
    } catch (emailError) {
      console.error("Error sending cancellation emails:", emailError);
      // Don't fail the request if email fails
    }

    res.json({
      id: booking._id.toString(),
      status: booking.status,
      message: "Booking cancelled successfully",
    });
  } catch (error) {
    await session.abortTransaction();
    console.error("Error cancelling booking:", error);
    res.status(500).json({ error: "Failed to cancel booking" });
  } finally {
    session.endSession();
  }
};

/**
 * Resend ICS calendar file for confirmed bookings
 */
export const resendICS = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const userId = (req as any).user.id;

    const booking = await Booking.findById(id)
      .populate("menteeId", "userName email")
      .populate("mentorId", "userName email");

    if (!booking) {
      return res.status(404).json({ error: "Booking not found" });
    }

    // Check authorization
    if (
      booking.menteeId._id.toString() !== userId &&
      booking.mentorId._id.toString() !== userId
    ) {
      return res.status(403).json({ error: "Not authorized" });
    }

    // Only send ICS for confirmed bookings
    if (booking.status !== "Confirmed") {
      return res.status(400).json({
        error: "ICS can only be sent for confirmed bookings",
      });
    }

    const mentee = booking.menteeId as any;
    const mentor = booking.mentorId as any;

    // Generate ICS
    const icsContent = generateICS(booking, mentor.userName, mentee.userName);

    // Send email with ICS
    const recipient = booking.menteeId._id.toString() === userId ? mentee : mentor;
    await sendBookingConfirmationEmail(
      recipient.email,
      recipient.userName,
      {
        id: booking._id.toString(),
        mentorName: mentor.userName,
        menteeName: mentee.userName,
        topic: booking.topic,
        startTime: booking.startTime,
        endTime: booking.endTime,
        meetingLink: booking.meetingLink,
      },
      icsContent
    );

    res.json({ message: "ICS file sent successfully" });
  } catch (error) {
    console.error("Error resending ICS:", error);
    res.status(500).json({ error: "Failed to resend ICS" });
  }
};

/**
 * Payment webhook handler
 */
export const handlePaymentWebhook = async (req: Request, res: Response) => {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const { bookingId, status, transactionId } = req.body;

    if (!bookingId || !status) {
      await session.abortTransaction();
      return res.status(400).json({ error: "Missing required fields" });
    }

    const booking = await Booking.findById(bookingId)
      .populate("menteeId", "userName email")
      .populate("mentorId", "userName email")
      .session(session);

    if (!booking) {
      await session.abortTransaction();
      return res.status(404).json({ error: "Booking not found" });
    }

    if (status === "success") {
      // Validate transition
      if (!isValidTransition(booking.status, "Confirmed")) {
        await session.abortTransaction();
        return res.status(400).json({
          error: `Cannot confirm booking with status ${booking.status}`,
        });
      }

      booking.status = "Confirmed";
      booking.expiresAt = undefined;
      await booking.save({ session });

      await session.commitTransaction();

      // Send confirmation emails with ICS
      try {
        const mentee = booking.menteeId as any;
        const mentor = booking.mentorId as any;
        const icsContent = generateICS(booking, mentor.userName, mentee.userName);

        await Promise.all([
          sendBookingConfirmationEmail(
            mentee.email,
            mentee.userName,
            {
              id: booking._id.toString(),
              mentorName: mentor.userName,
              menteeName: mentee.userName,
              topic: booking.topic,
              startTime: booking.startTime,
              endTime: booking.endTime,
              meetingLink: booking.meetingLink,
            },
            icsContent
          ),
          sendBookingConfirmationEmail(
            mentor.email,
            mentor.userName,
            {
              id: booking._id.toString(),
              mentorName: mentor.userName,
              menteeName: mentee.userName,
              topic: booking.topic,
              startTime: booking.startTime,
              endTime: booking.endTime,
              meetingLink: booking.meetingLink,
            },
            icsContent
          ),
        ]);
      } catch (emailError) {
        console.error("Error sending confirmation emails:", emailError);
      }

      res.json({ status: "success", bookingStatus: booking.status });
    } else {
      // Payment failed
      if (!isValidTransition(booking.status, "Failed")) {
        await session.abortTransaction();
        return res.status(400).json({
          error: `Cannot fail booking with status ${booking.status}`,
        });
      }

      booking.status = "Failed";
      await booking.save({ session });

      // Release slot
      if (booking.occurrenceId) {
        await AvailabilityOccurrence.findByIdAndUpdate(
          booking.occurrenceId,
          { status: "open" },
          { session }
        );
      }

      await session.commitTransaction();

      // Send failure email
      try {
        const mentee = booking.menteeId as any;
        await sendBookingFailedEmail(mentee.email, mentee.userName, {
          id: booking._id.toString(),
          topic: booking.topic,
          reason: "Payment failed",
        });
      } catch (emailError) {
        console.error("Error sending failure email:", emailError);
      }

      res.json({ status: "failed", bookingStatus: booking.status });
    }
  } catch (error) {
    await session.abortTransaction();
    console.error("Error handling payment webhook:", error);
    res.status(500).json({ error: "Failed to process payment webhook" });
  } finally {
    session.endSession();
  }
};

/**
 * Auto-release expired bookings (called by cron job or manually)
 */
export const releaseExpiredBookings = async (req: Request, res: Response) => {
  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const now = new Date();
    const expiredBookings = await Booking.find({
      status: "PaymentPending",
      expiresAt: { $lte: now },
    }).session(session);

    for (const booking of expiredBookings) {
      booking.status = "Failed";
      await booking.save({ session });

      // Release slot
      if (booking.occurrenceId) {
        await AvailabilityOccurrence.findByIdAndUpdate(
          booking.occurrenceId,
          { status: "open" },
          { session }
        );
      }

      // Send failure email
      try {
        const mentee = await User.findById(booking.menteeId);
        if (mentee) {
          await sendBookingFailedEmail(mentee.email, mentee.userName, {
            id: booking._id.toString(),
            topic: booking.topic,
            reason: "Payment timeout",
          });
        }
      } catch (emailError) {
        console.error("Error sending timeout email:", emailError);
      }
    }

    await session.commitTransaction();
    res.json({
      message: `Released ${expiredBookings.length} expired bookings`,
      count: expiredBookings.length,
    });
  } catch (error) {
    await session.abortTransaction();
    console.error("Error releasing expired bookings:", error);
    res.status(500).json({ error: "Failed to release expired bookings" });
  } finally {
    session.endSession();
  }
};

/**
 * Validate booking status transitions
 */
function isValidTransition(
  currentStatus: BookingStatus,
  newStatus: BookingStatus
): boolean {
  const validTransitions: Record<BookingStatus, BookingStatus[]> = {
    PaymentPending: ["Confirmed", "Failed", "Cancelled"],
    Confirmed: ["Completed", "Cancelled"],
    Failed: [], // Cannot transition from failed
    Cancelled: [], // Cannot transition from cancelled
    Completed: [], // Cannot transition from completed
  };

  return validTransitions[currentStatus]?.includes(newStatus) ?? false;
}
