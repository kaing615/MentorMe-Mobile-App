// app/src/main/java/com/mentorme/app/data/model/Models.kt
package com.mentorme.app.data.model

import com.google.gson.annotations.SerializedName

// -------- Roles từ web: 'mentee' | 'mentor'
enum class UserRole {
    @SerializedName("mentee")
    MENTEE,

    @SerializedName("mentor")
    MENTOR
}

// -------- User (map từ web: id, email, fullName, avatar?, role, createdAt)
data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("role")
    val role: UserRole,
    @SerializedName("createdAt")
    val createdAt: String // ISO-8601 string (map từ Date bên web)
)

// -------- Mentor extends User (web): thêm bio, skills, hourlyRate,...
// Kotlin không kế thừa data class thuận tiện => nhân bản các trường chung cho dễ dùng API
data class Mentor(
    // Trường chung từ User
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("role")
    val role: UserRole = UserRole.MENTOR,
    @SerializedName("createdAt")
    val createdAt: String,

    // Trường mở rộng của Mentor
    @SerializedName("bio")
    val bio: String,
    @SerializedName("skills")
    val skills: List<String>,
    @SerializedName("hourlyRate")
    val hourlyRate: Double,
    @SerializedName("rating")
    val rating: Double,
    @SerializedName("totalReviews")
    val totalReviews: Int,
    @SerializedName("availability")
    val availability: List<AvailabilitySlot>,
    @SerializedName("verified")
    val verified: Boolean,
    @SerializedName("experience")
    val experience: String,
    @SerializedName("education")
    val education: String,
    @SerializedName("languages")
    val languages: List<String>
)

// -------- AvailabilitySlot
data class AvailabilitySlot(
    @SerializedName("id")
    val id: String,
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("date")
    val date: String,        // "YYYY-MM-DD"
    @SerializedName("startTime")
    val startTime: String,   // "HH:mm"
    @SerializedName("endTime")
    val endTime: String,     // "HH:mm"
    @SerializedName("isBooked")
    val isBooked: Boolean
)

// -------- Booking status from API: 'PaymentPending' | 'Confirmed' | 'Failed' | 'Cancelled' | 'Completed'
enum class BookingStatus {
    @SerializedName("PaymentPending")
    PAYMENT_PENDING,
    @SerializedName("Confirmed")
    CONFIRMED,
    @SerializedName("Failed")
    FAILED,
    @SerializedName("Cancelled")
    CANCELLED,
    @SerializedName("Completed")
    COMPLETED,
    // Legacy statuses for backward compatibility
    @SerializedName("pending")
    PENDING
}

// -------- Booking (API response fields)
data class Booking(
    @SerializedName("id")
    val id: String,
    @SerializedName("menteeId")
    val menteeId: String,
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("occurrenceId")
    val occurrenceId: String? = null,
    @SerializedName("date")
    val date: String,          // "YYYY-MM-DD"
    @SerializedName("startTime")
    val startTime: String,     // "HH:mm"
    @SerializedName("endTime")
    val endTime: String,       // "HH:mm"
    @SerializedName("startTimeIso")
    val startTimeIso: String? = null,
    @SerializedName("endTimeIso")
    val endTimeIso: String? = null,
    @SerializedName("status")
    val status: BookingStatus,
    @SerializedName("price")
    val price: Double,
    @SerializedName("topic")
    val topic: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("meetingLink")
    val meetingLink: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String      // ISO-8601 string
)

// -------- Review
data class Review(
    @SerializedName("id")
    val id: String,
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("menteeId")
    val menteeId: String,
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("createdAt")
    val createdAt: String
)

// -------- Message
enum class MessageType {
    @SerializedName("text")
    TEXT,
    @SerializedName("file")
    FILE,
    @SerializedName("image")
    IMAGE
}

data class Message(
    @SerializedName("id")
    val id: String,
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("receiverId")
    val receiverId: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("type")
    val type: MessageType,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("read")
    val read: Boolean
)

data class Profile(
    @SerializedName("jobTitle")
    val jobTitle: String?,
    @SerializedName("location")
    val location: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("skills")
    val skills: String,            // ví dụ: "kotlin,compose"
    @SerializedName("experience")
    val experience: String?,
    @SerializedName("headline")
    val headline: String?,
    @SerializedName("mentorReason")
    val mentorReason: String?,
    @SerializedName("greatestAchievement")
    val greatestAchievement: String?,
    @SerializedName("introVideo")
    val introVideo: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("goal")
    val goal: String?,
    @SerializedName("education")
    val education: String?,
    @SerializedName("languages")
    val languages: String,         // ví dụ: "vi,en"
    @SerializedName("website")
    val website: String?,
    @SerializedName("twitter")
    val twitter: String?,
    @SerializedName("linkedin")
    val linkedin: String?,
    @SerializedName("github")
    val github: String?,
    @SerializedName("youtube")
    val youtube: String?,
    @SerializedName("facebook")
    val facebook: String?,
    @SerializedName("avatarPath")
    val avatarPath: String?,       // file local (optional)
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)
