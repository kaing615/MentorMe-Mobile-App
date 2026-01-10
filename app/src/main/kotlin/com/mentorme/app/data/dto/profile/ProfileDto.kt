package com.mentorme.app.data.dto.profile

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ProfileCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CreatedProfilePayload?
)

data class CreatedProfilePayload(
    @SerializedName("profile") val profile: ProfileDto?,
    @SerializedName("updatedStatus") val updatedStatus: String?,
    @SerializedName("next") val next: String?
)

data class PublicProfilePayload(
    @SerializedName("profile") val profile: ProfileDto?
)

data class ProfileDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("user") val user: String?,

    // ✅ Basic info
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,

    // ✅ Mentor-specific
    @SerializedName("jobTitle") val jobTitle: String?,
    @SerializedName("headline") val headline: String?,
    @SerializedName("hourlyRateVnd") val hourlyRateVnd: Int?, // ✅ NEW: Giá mỗi giờ
    @SerializedName("experience") val experience: String?,
    @SerializedName("education") val education: String?,

    // ✅ Content
    @SerializedName("bio") val bio: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("mentorReason") val mentorReason: String?,
    @SerializedName("greatestAchievement") val greatestAchievement: String?,
    @SerializedName("introVideo") val introVideo: String?,
    @SerializedName("goal") val goal: String?,

    // ✅ Skills & Languages
    @SerializedName("skills") val skills: List<String>?,
    @SerializedName("languages") val languages: List<String>?,

    // ✅ Links & Status
    @SerializedName("links") val links: LinksDto?,
    @SerializedName("profileCompleted") val profileCompleted: Boolean?
)

data class LinksDto(
    @SerializedName("website") val website: String?,
    @SerializedName("twitter") val twitter: String?,
    @SerializedName("linkedin") val linkedin: String?,
    @SerializedName("github") val github: String?,
    @SerializedName("youtube") val youtube: String?,
    @SerializedName("facebook") val facebook: String?
)

data class ProfileSheetState(
    val mentorId: String,
    val role: String? = null
)

