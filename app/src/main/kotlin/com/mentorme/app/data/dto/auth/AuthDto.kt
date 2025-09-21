package com.mentorme.app.data.dto.auth

import com.google.gson.annotations.SerializedName

// Request DTOs
data class SignUpRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String,
    @SerializedName("displayName")
    val displayName: String? = null
)

data class SignInRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

// Response DTOs
data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AuthData? = null
)

data class AuthData(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("token")
    val token: String? = null
)

data class UserDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean,
    @SerializedName("role")
    val role: String,
    @SerializedName("avatar")
    val avatar: String? = null
)
