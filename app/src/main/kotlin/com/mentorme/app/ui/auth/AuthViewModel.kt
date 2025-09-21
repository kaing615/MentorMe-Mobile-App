package com.mentorme.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.ErrorUtils
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

                    // Extract verificationId from response data
                    val verificationId = result.data.data?.let {
                        // Assuming the response contains verificationId in data field
                        extractVerificationId(it)
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        // Show OTP screen after successful signup
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "SignUp failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = ErrorUtils.getUserFriendlyErrorMessage(result.throwable.message)
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

                    // Extract verificationId from response data
                    val verificationId = result.data.data?.let {
                        extractVerificationId(it)
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        // Show OTP screen after successful mentor signup
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "SignUpMentor failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = ErrorUtils.getUserFriendlyErrorMessage(result.throwable.message)
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 SIGNIN CALLED - EMAIL: $email 🔥🔥🔥")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signInUseCase.invoke(email, password)) {
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
                        error = ErrorUtils.getUserFriendlyErrorMessage(result.throwable.message)
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    // OTP Verification Functions
    fun verifyOtp(verificationId: String, otp: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥🔥🔥 VERIFY OTP CALLED - ID: $verificationId, OTP: $otp 🔥🔥🔥")
            _authState.value = _authState.value.copy(
                isOtpVerifying = true,
                otpError = null
            )

            when (val result = verifyOtpUseCase.invoke(verificationId, otp)) {
                is AppResult.Success -> {
                    Log.d(TAG, "OTP verification success: ${result.data.message}")
                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        authResponse = result.data,
                        showOtpScreen = false,
                        showVerificationDialog = true,
                        verificationSuccess = true,
                        verificationMessage = "Email đã được xác minh thành công! Bạn có thể đăng nhập ngay bây giờ.",
                        isAuthenticated = result.data.success
                    )
                }
                is AppResult.Error -> {
                    Log.e(TAG, "OTP verification failed: ${result.throwable.message}", result.throwable)
                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        otpError = ErrorUtils.getUserFriendlyErrorMessage(result.throwable.message),
                        showVerificationDialog = true,
                        verificationSuccess = false,
                        verificationMessage = ErrorUtils.getUserFriendlyErrorMessage(result.throwable.message)
                    )
                }
                AppResult.Loading -> {
                    // Already set loading state above
                }
            }
        }
    }

    fun resendOtp() {
        val currentEmail = _authState.value.userEmail
        if (currentEmail != null) {
            // Re-trigger signup to get new OTP
            // This assumes we store the signup data, or we need a separate resend API
            Log.d(TAG, "Resending OTP for email: $currentEmail")
            _authState.value = _authState.value.copy(
                otpError = null,
                isOtpVerifying = false
            )
            // Note: You might need to implement a separate resend OTP API call here
        }
    }

    fun hideOtpScreen() {
        _authState.value = _authState.value.copy(
            showOtpScreen = false,
            otpVerificationId = null,
            userEmail = null,
            otpError = null
        )
    }

    fun hideVerificationDialog() {
        _authState.value = _authState.value.copy(
            showVerificationDialog = false,
            verificationMessage = null
        )
    }

    fun clearOtpError() {
        _authState.value = _authState.value.copy(otpError = null)
    }

    // Helper function to extract verificationId from AuthData
    private fun extractVerificationId(authData: com.mentorme.app.data.dto.auth.AuthData): String? {
        return authData.verificationId
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authResponse: AuthResponse? = null,
    val error: String? = null,
    // OTP verification states
    val showOtpScreen: Boolean = false,
    val otpVerificationId: String? = null,
    val userEmail: String? = null,
    val otpError: String? = null,
    val isOtpVerifying: Boolean = false,
    val showVerificationDialog: Boolean = false,
    val verificationSuccess: Boolean = false,
    val verificationMessage: String? = null
)
