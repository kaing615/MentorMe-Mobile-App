package com.mentorme.app.data.network.api.auth

import com.mentorme.app.data.dto.auth.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/signup-mentor")
    suspend fun signUpMentor(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/signin")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<AuthResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<AuthResponse>

    @POST("auth/signout")
    suspend fun signOut(): Response<AuthResponse>
}
