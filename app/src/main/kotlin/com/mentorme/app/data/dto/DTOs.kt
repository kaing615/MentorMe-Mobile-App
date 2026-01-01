package com.mentorme.app.data.dto

import com.google.gson.annotations.SerializedName
import com.mentorme.app.data.model.*

// Auth DTOs
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String
)

data class AuthResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: User
)

// Mentor DTOs
data class MentorListResponse(
    @SerializedName("mentors")
    val mentors: List<Mentor>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

// ✅ Thêm field id (occurrenceId)
data class AvailabilitySlot(
    @SerializedName("id")
    val id: String?  = null,
    @SerializedName("date")
    val date: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("priceVnd")
    val priceVnd: Double? = null,
    @SerializedName("isAvailable")
    val isAvailable: Boolean
)

// Booking DTOs
data class CreateBookingRequest(
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("occurrenceId")  // ✅ Sửa từ scheduledAt sang occurrenceId
    val occurrenceId: String,
    @SerializedName("topic")
    val topic: String?  = null,  // ✅ Optional
    @SerializedName("notes")
    val notes: String? = null
)

data class UpdateBookingRequest(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("scheduledAt")
    val scheduledAt: String? = null,
    @SerializedName("notes")
    val notes: String? = null
)

data class MentorDeclineRequest(
    @SerializedName("reason")
    val reason: String? = null
)

data class PaymentWebhookRequest(
    @SerializedName("event")
    val event: String,
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("paymentId")
    val paymentId: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class BookingListResponse(
    @SerializedName("bookings")
    val bookings: List<Booking>,
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("totalPages")
    val totalPages: Int = 0
)

data class RatingRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("feedback")
    val feedback: String? = null
)

data class ResendIcsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)
data class CancelBookingRequest(
    @SerializedName("reason")
    val reason: String? = null
)

// Message DTOs
data class Message(
    @SerializedName("id")
    val id: String,
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("sender")
    val sender: User,
    @SerializedName("content")
    val content: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("messageType")
    val messageType: String = "text"
)

data class SendMessageRequest(
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("messageType")
    val messageType: String = "text"
)
