package com.mentorme.app.domain.usecase.auth

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.validation.AuthValidator
import com.mentorme.app.data.dto.auth.SignUpRequest
import com.mentorme.app.data.dto.auth.SignInRequest
import com.mentorme.app.data.dto.auth.VerifyOtpRequest
import com.mentorme.app.data.dto.auth.ResendOtpRequest
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AppResult<AuthResponse> {
        // Validate input data using Konform AuthValidator
        val validationResult = AuthValidator.validateSignUpData(userName, email, password, confirmPassword)
        if (validationResult != null) {
            return AppResult.Error(validationResult)
        }

        return authRepository.signUp(
            SignUpRequest(
                userName = userName,
                email = email,
                password = password
            )
        )
    }
}

class SignUpMentorUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AppResult<AuthResponse> {
        // Validate input data using Konform AuthValidator
        val validationResult = AuthValidator.validateSignUpData(userName, email, password, confirmPassword)
        if (validationResult != null) {
            return AppResult.Error(validationResult)
        }

        return authRepository.signUpMentor(
            SignUpRequest(
                userName = userName,
                email = email,
                password = password
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
        // Validate input data using Konform AuthValidator
        val validationResult = AuthValidator.validateSignInData(email, password)
        if (validationResult != null) {
            return AppResult.Error(validationResult)
        }

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
        // Validate input data using Konform AuthValidator
        val validationResult = AuthValidator.validateOtpData(verificationId, otp)
        if (validationResult != null) {
            return AppResult.Error(validationResult)
        }

        return authRepository.verifyOtp(
            VerifyOtpRequest(
                verificationId = verificationId,
                code = otp
            )
        )
    }
}

class ResendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String
    ): AppResult<AuthResponse> {
        // Validate input data using Konform AuthValidator
        val validationResult = AuthValidator.validateEmail(email)
        if (validationResult != null) {
            return AppResult.Error(validationResult)
        }

        return authRepository.resendOtp(
            ResendOtpRequest(
                email = email
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
