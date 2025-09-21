package com.mentorme.app.domain.usecase.auth

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.repository.AuthRepository
import javax.inject.Inject

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
        email: String,
        otp: String
    ): AppResult<AuthResponse> {
        return authRepository.verifyOtp(
            VerifyOtpRequest(
                email = email,
                otp = otp
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
