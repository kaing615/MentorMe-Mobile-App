package com.mentorme.app.data.dto.profile

import com.google.gson.annotations.SerializedName

data class MePayload(
    @SerializedName("user") val user: UserMeDto?,
    @SerializedName("profile") val profile: ProfileDto?
)

data class UserMeDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)
