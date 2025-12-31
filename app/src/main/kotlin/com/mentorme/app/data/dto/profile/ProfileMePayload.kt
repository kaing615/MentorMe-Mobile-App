package com.mentorme.app.data.dto.profile

import com.google.gson.annotations.SerializedName

/**
 * Response từ GET /profile/me
 * Structure: { success, message, data:  { profile: {... } } }
 */
data class ProfileMePayload(
    @SerializedName("profile")
    val profile: ProfileMeDto?
)

/**
 * Profile object trong /profile/me response
 * Khác với ProfileDto ở /auth/me vì field "user" là object chứ không phải string
 */
data class ProfileMeDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("user")  // ✅ Đây là OBJECT, không phải string
    val user: UserEmbedDto?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("avatarPublicId")
    val avatarPublicId: String?,

    @SerializedName("languages")
    val languages: List<String>?,

    @SerializedName("links")
    val links: LinksDto?,

    @SerializedName("jobTitle")
    val jobTitle: String?,

    @SerializedName("hourlyRateVnd")
    val hourlyRateVnd: Int?,  // ✅ NEW: Giá mỗi giờ

    @SerializedName("experience")
    val experience: String?,

    @SerializedName("headline")
    val headline: String?,

    @SerializedName("mentorReason")
    val mentorReason: String?,

    @SerializedName("greatestAchievement")
    val greatestAchievement: String?,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("introVideo")
    val introVideo:  String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("goal")
    val goal: String?,

    @SerializedName("education")
    val education: String?,

    @SerializedName("skills")
    val skills: List<String>?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("profileCompleted")
    val profileCompleted: Boolean?
)

/**
 * User object embedded trong ProfileMeDto
 */
data class UserEmbedDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("status")
    val status: String?
)
