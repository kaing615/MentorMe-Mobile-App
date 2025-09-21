package com.mentorme.app.domain.usecase.auth

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String? = null
    ): AppResult<AuthResponse> {
        return authRepository.signUp(
            SignUpRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                displayName = displayName
            )
        )
    }
}

class SignUpMentorUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String? = null
    ): AppResult<AuthResponse> {
        return authRepository.signUpMentor(
            SignUpRequest(
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                displayName = displayName
            )
        )
    }
}

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): AppResult<AuthResponse> {
        return authRepository.signIn(
            SignInRequest(
                email = email,
                password = password
            )
        )
    }
}

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        verificationId: String,
        otp: String
    ): AppResult<AuthResponse> {
        return authRepository.verifyOtp(
            VerifyOtpRequest(
                verificationId = verificationId,
                code = otp  // Backend expects 'code' field, not 'otp'
            )
        )
    }
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<AuthResponse> {
        return authRepository.signOut()
    }
}
