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

data class AvailabilitySlot(
    @SerializedName("date")
    val date: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("isAvailable")
    val isAvailable: Boolean
)

// Booking DTOs
data class CreateBookingRequest(
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("scheduledAt")
    val scheduledAt: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("topic")
    val topic: String,
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

data class BookingListResponse(
    @SerializedName("bookings")
    val bookings: List<Booking>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

data class RatingRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("feedback")
    val feedback: String? = null
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
