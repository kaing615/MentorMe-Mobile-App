package com.mentorme.app.data.dto.session

import com.google.gson.annotations.SerializedName

data class SessionJoinResponse(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    @SerializedName("role")
    val role: String? = null
)
