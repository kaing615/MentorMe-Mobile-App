package com.mentorme.app.domain.usecase.auth

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.SignUpRequest
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
