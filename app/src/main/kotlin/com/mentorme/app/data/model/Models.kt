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

// -------- Booking status from API: 'PaymentPending' | 'PendingMentor' | 'Confirmed' | 'Failed' | 'Cancelled' | 'Declined' | 'Completed' | 'NoShow*'
enum class BookingStatus {
    @SerializedName(value = "PaymentPending", alternate = ["paymentPending", "payment_pending"])
    PAYMENT_PENDING,

    @SerializedName(
        value = "PendingMentor",
        alternate = ["pending-mentor", "pending_mentor", "pending"]
    )
    PENDING_MENTOR,

    @SerializedName(value = "Confirmed", alternate = ["confirmed"])
    CONFIRMED,

    @SerializedName(value = "Completed", alternate = ["completed"])
    COMPLETED,

    @SerializedName(value = "Cancelled", alternate = ["cancelled"])
    CANCELLED,

    @SerializedName(value = "Failed", alternate = ["failed"])
    FAILED,

    @SerializedName(value = "Declined", alternate = ["declined"])
    DECLINED,

    @SerializedName(value = "NoShowMentor", alternate = ["no_show_mentor", "no-show-mentor"])
    NO_SHOW_MENTOR,

    @SerializedName(value = "NoShowMentee", alternate = ["no_show_mentee", "no-show-mentee"])
    NO_SHOW_MENTEE,

    @SerializedName(value = "NoShowBoth", alternate = ["no_show_both", "no-show-both"])
    NO_SHOW_BOTH
}

// -------- Booking user summary (nested in booking payloads)
data class BookingUserSummary(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName(
        value = "fullName",
        alternate = ["full_name", "fullname", "name", "userName", "username", "displayName"]
    )
    val fullName: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: UserRole? = null,
    @SerializedName("user")
    val user: BookingUserAccount? = null,
    @SerializedName("profile")
    val profile: BookingUserProfile? = null
)

data class BookingUserAccount(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName(
        value = "fullName",
        alternate = ["full_name", "fullname", "name", "displayName"]
    )
    val fullName: String? = null,
    @SerializedName(value = "userName", alternate = ["username"])
    val userName: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null
)

data class BookingUserProfile(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName(
        value = "fullName",
        alternate = ["full_name", "fullname", "name", "displayName"]
    )
    val fullName: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

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
    val notes: String?,
    @SerializedName("meetingLink")
    val meetingLink: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    @SerializedName("mentorResponseDeadline")
    val mentorResponseDeadline: String? = null,
    @SerializedName("reminder24hSentAt")
    val reminder24hSentAt: String? = null,
    @SerializedName("reminder1hSentAt")
    val reminder1hSentAt: String? = null,
    @SerializedName("lateCancel")
    val lateCancel: Boolean? = null,
    @SerializedName("lateCancelMinutes")
    val lateCancelMinutes: Int? = null,
    @SerializedName("reviewId")
    val reviewId: String? = null,
    @SerializedName("reviewedAt")
    val reviewedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,     // ISO-8601
    @SerializedName("rating")
    val rating: Int?  = null,
    @SerializedName("feedback")
    val feedback: String?  = null,
    @SerializedName(
        value = "mentorFullName",
        alternate = ["mentorName", "mentor_full_name", "mentor_fullName", "mentor_name"]
    )
    val mentorFullName: String? = null,
    @SerializedName(
        value = "mentor",
        alternate = ["mentorUser", "mentorProfile", "mentorInfo"]
    )
    val mentor: BookingUserSummary? = null,
    @SerializedName(
        value = "menteeFullName",
        alternate = ["menteeName", "mentee_full_name", "mentee_fullName", "mentee_name"]
    )
    val menteeFullName: String? = null,
    @SerializedName(
        value = "mentee",
        alternate = ["menteeUser", "menteeProfile", "menteeInfo"]
    )
    val mentee: BookingUserSummary? = null
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

