package com.mentorme.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: UserRole,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

enum class UserRole {
    @SerializedName("student")
    STUDENT,
    @SerializedName("mentor")
    MENTOR,
    @SerializedName("admin")
    ADMIN
}

data class Mentor(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("user")
    val user: User,
    @SerializedName("expertise")
    val expertise: List<String>,
    @SerializedName("experience")
    val experience: String,
    @SerializedName("hourlyRate")
    val hourlyRate: Double,
    @SerializedName("rating")
    val rating: Double,
    @SerializedName("totalBookings")
    val totalBookings: Int,
    @SerializedName("isAvailable")
    val isAvailable: Boolean,
    @SerializedName("languages")
    val languages: List<String>,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class Booking(
    @SerializedName("id")
    val id: String,
    @SerializedName("mentorId")
    val mentorId: String,
    @SerializedName("studentId")
    val studentId: String,
    @SerializedName("mentor")
    val mentor: Mentor? = null,
    @SerializedName("student")
    val student: User? = null,
    @SerializedName("scheduledAt")
    val scheduledAt: String,
    @SerializedName("duration")
    val duration: Int, // minutes
    @SerializedName("status")
    val status: BookingStatus,
    @SerializedName("topic")
    val topic: String,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("meetingUrl")
    val meetingUrl: String? = null,
    @SerializedName("rating")
    val rating: Int? = null,
    @SerializedName("feedback")
    val feedback: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

enum class BookingStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("confirmed")
    CONFIRMED,
    @SerializedName("in_progress")
    IN_PROGRESS,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("cancelled")
    CANCELLED
}
