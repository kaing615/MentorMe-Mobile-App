package com.mentorme.app.data.dto.auth

import com.google.gson.annotations.SerializedName

// Request DTOs
data class SignUpRequest(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class SignInRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class VerifyOtpRequest(
    @SerializedName("verificationId")
    val verificationId: String,
    @SerializedName("code")
    val code: String
)

data class ResendOtpRequest(
    @SerializedName("email")
    val email: String
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
    val user: UserDto? = null,
    @SerializedName("token")
    val token: String? = null,
    // Fields for OTP verification from signup response
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("userName")
    val userName: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("verificationId")
    val verificationId: String? = null,
    @SerializedName("expiresIn")
    val expiresIn: Int? = null
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
