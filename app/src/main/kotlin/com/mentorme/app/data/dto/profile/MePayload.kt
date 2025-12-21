package com.mentorme.app.data.dto.profile

import com.google.gson.annotations.SerializedName

/**
 * Response từ GET /auth/me
 */
data class MePayload(
    @SerializedName("user")
    val user: MeUserDto?,

    @SerializedName("profile")
    val profile: ProfileDto?  // ✅ Sử dụng ProfileDto đã có
)

/**
 * User object trong MePayload response
 * Khác với ProfileDto. user (là String userId)
 */
data class MeUserDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?,  // "mentor" | "mentee"

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("createdAt")
    val createdAt: String?
)