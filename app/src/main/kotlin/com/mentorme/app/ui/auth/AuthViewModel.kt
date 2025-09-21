package com.mentorme.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.domain.usecase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signUpMentorUseCase: SignUpMentorUseCase,
    private val signInUseCase: SignInUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String? = null
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 SIGNUP CALLED - EMAIL: $email 🔥🔥🔥")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signUpUseCase(username, email, password, confirmPassword, displayName)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignUp success: ${result.data.message}")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = result.data.success
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "SignUp failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error occurred"
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun signUpMentor(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String? = null
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 SIGNUP MENTOR CALLED - EMAIL: $email 🔥🔥🔥")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signUpMentorUseCase(username, email, password, confirmPassword, displayName)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignUpMentor success: ${result.data.message}")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = result.data.success
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "SignUpMentor failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error occurred"
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        Log.e(TAG, "🔥🔥🔥 SIGNIN BUTTON PRESSED - EMAIL: $email 🔥🔥🔥")
        Log.e(TAG, "🔥🔥🔥 VIEWMODEL SIGNIN CALLED 🔥🔥🔥")

        viewModelScope.launch {
            Log.d(TAG, "Starting signIn for email: $email")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signInUseCase(email, password)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignIn success: ${result.data.message}")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = result.data.success
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "SignIn failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error occurred"
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 VERIFY OTP CALLED - EMAIL: $email 🔥🔥🔥")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = verifyOtpUseCase(email, otp)) {
                is AppResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = result.data.success
                    )
                }
                is AppResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error occurred"
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 SIGNOUT CALLED 🔥🔥🔥")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signOutUseCase()) {
                is AppResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = false
                    )
                }
                is AppResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error occurred"
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authResponse: AuthResponse? = null,
    val error: String? = null
)
