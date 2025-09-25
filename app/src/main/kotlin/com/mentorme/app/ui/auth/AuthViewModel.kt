package com.mentorme.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.ErrorUtils
import com.mentorme.app.data.dto.auth.AuthResponse
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.domain.usecase.auth.SignInUseCase
import com.mentorme.app.domain.usecase.auth.SignOutUseCase
import com.mentorme.app.domain.usecase.auth.SignUpMentorUseCase
import com.mentorme.app.domain.usecase.auth.SignUpUseCase
import com.mentorme.app.domain.usecase.auth.VerifyOtpUseCase
import com.mentorme.app.domain.usecase.auth.ResendOtpUseCase
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
    private val resendOtpUseCase: ResendOtpUseCase,
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
            Log.d(TAG, "🔥 SIGNUP CALLED - EMAIL: $email")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signUpUseCase(username, email, password, confirmPassword)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignUp success: ${result.data}")

                    val verificationId = result.data.data?.let { extractVerificationId(it) }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null,
                        originalSignUpData = OriginalSignUpData(username, email, password, false)
                    )
                }
                is AppResult.Error -> {
                    val errMsg: String = result.throwable ?: "Unknown error"
                    Log.e(TAG, "SignUp failed: $errMsg")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                    )
                }
                AppResult.Loading -> Unit
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
            Log.d(TAG, "🔥 SIGNUP MENTOR CALLED - EMAIL: $email")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signUpMentorUseCase(username, email, password, confirmPassword)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignUpMentor success: ${result.data}")

                    val verificationId = result.data.data?.let { extractVerificationId(it) }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null,
                        originalSignUpData = OriginalSignUpData(username, email, password, true)
                    )
                }
                is AppResult.Error -> {
                    val errMsg: String = result.throwable ?: "Unknown error"
                    Log.e(TAG, "SignUpMentor failed: $errMsg")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                    )
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥 SIGNIN CALLED - EMAIL: $email")
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = signInUseCase.invoke(email, password)) {
                is AppResult.Success -> {
                    Log.d(TAG, "SignIn success: ${result.data}")
                    Log.d(TAG, "SignIn success data: ${result.data.data}")

                    // Extract user role từ response data với logging chi tiết
                    val userRole = try {
                        result.data.data?.let { data ->
                            Log.d(TAG, "Extracting role from data.role: ${data.role}")
                            when (data.role) {
                                "mentor" -> {
                                    Log.d(TAG, "Setting userRole to MENTOR")
                                    UserRole.MENTOR
                                }
                                "mentee" -> {
                                    Log.d(TAG, "Setting userRole to MENTEE")
                                    UserRole.MENTEE
                                }
                                else -> {
                                    Log.d(TAG, "Unknown role: ${data.role}, defaulting to MENTEE")
                                    UserRole.MENTEE
                                }
                            }
                        } ?: run {
                            Log.w(TAG, "No data found in response, defaulting to MENTEE")
                            UserRole.MENTEE
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to extract user role: ${e.message}", e)
                        UserRole.MENTEE
                    }

                    Log.d(TAG, "Final userRole set to: $userRole")

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = result.data.success,
                        userRole = userRole
                    )
                }
                is AppResult.Error -> {
                    val errMsg: String = result.throwable ?: "Unknown error"
                    Log.e(TAG, "SignIn failed: $errMsg")

                    // Xử lý đặc biệt cho pending approval - không qua ErrorUtils
                    val finalError = if (errMsg.contains("Account pending approval", ignoreCase = true)) {
                        "pending_approval"
                    } else {
                        ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = finalError
                    )
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun verifyOtp(verificationId: String, otp: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥 VERIFY OTP CALLED - ID: $verificationId, OTP: $otp")
            _authState.value = _authState.value.copy(
                isOtpVerifying = true,
                otpError = null
            )

            when (val result = verifyOtpUseCase.invoke(verificationId, otp)) {
                is AppResult.Success -> {
                    Log.d(TAG, "OTP verification success: ${result.data}")
                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        authResponse = result.data,
                        showOtpScreen = false,
                        showVerificationDialog = true,
                        verificationSuccess = true,
                        verificationMessage = "Email đã được xác minh thành công! Bạn có thể đăng nhập ngay bây giờ.",
                        // Không set isAuthenticated = true vì người dùng chỉ mới xác minh email, chưa đăng nhập
                        isAuthenticated = false
                    )
                }
                is AppResult.Error -> {
                    val errMsg: String = result.throwable ?: "OTP verification failed"
                    Log.e(TAG, "OTP verification failed: $errMsg")
                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        otpError = ErrorUtils.getUserFriendlyErrorMessage(errMsg),
                        showVerificationDialog = true,
                        verificationSuccess = false,
                        verificationMessage = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                    )
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun resendOtp() {
        val originalData = _authState.value.originalSignUpData
        val currentEmail = _authState.value.userEmail

        if (originalData != null) {
            Log.d(TAG, "Resending OTP using original signup data for email: ${originalData.email}")
            _authState.value = _authState.value.copy(
                otpError = null,
                isOtpVerifying = false
            )

            viewModelScope.launch {
                // Gọi lại chính xác endpoint signup/signup-mentor với thông tin gốc
                val result = if (originalData.isMentor) {
                    signUpMentorUseCase(originalData.userName, originalData.email, originalData.password, originalData.password)
                } else {
                    signUpUseCase(originalData.userName, originalData.email, originalData.password, originalData.password)
                }

                when (result) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Resend OTP success: ${result.data}")
                        // Update verification ID if backend returns a new one
                        val newVerificationId = result.data.data?.let { extractVerificationId(it) }
                        if (newVerificationId != null) {
                            _authState.value = _authState.value.copy(otpVerificationId = newVerificationId)
                        }
                    }
                    is AppResult.Error -> {
                        val errMsg: String = result.throwable ?: "Unknown error"
                        Log.e(TAG, "Resend OTP failed: $errMsg")
                        _authState.value = _authState.value.copy(
                            otpError = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                        )
                    }
                    AppResult.Loading -> Unit
                }
            }
        } else if (currentEmail != null) {
            // Fallback: nếu không có originalSignUpData, sử dụng ResendOtpUseCase
            Log.d(TAG, "Resending OTP using fallback method for email: $currentEmail")
            _authState.value = _authState.value.copy(
                otpError = null,
                isOtpVerifying = false
            )

            viewModelScope.launch {
                when (val result = resendOtpUseCase(currentEmail)) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Resend OTP success: ${result.data}")
                    }
                    is AppResult.Error -> {
                        val errMsg: String = result.throwable ?: "Unknown error"
                        Log.e(TAG, "Resend OTP failed: $errMsg")
                        _authState.value = _authState.value.copy(
                            otpError = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                        )
                    }
                    AppResult.Loading -> Unit
                }
            }
        } else {
            Log.e(TAG, "Cannot resend OTP: No signup data or email found")
            _authState.value = _authState.value.copy(
                otpError = "Không thể gửi lại mã OTP. Vui lòng thử đăng ký lại."
            )
        }
    }

    fun hideOtpScreen() {
        _authState.value = _authState.value.copy(
            showOtpScreen = false,
            otpVerificationId = null,
            userEmail = null,
            otpError = null,
            isOtpVerifying = false,
            showVerificationDialog = false,
            verificationSuccess = false,
            verificationMessage = null,
            originalSignUpData = null
        )
    }

    // Thêm method để xử lý đóng verification dialog
    fun dismissVerificationDialog() {
        _authState.value = _authState.value.copy(
            showVerificationDialog = false,
            verificationMessage = null
        )

        // Nếu verification thành công, reset toàn bộ OTP screen
        if (_authState.value.verificationSuccess) {
            hideOtpScreen()
        }
        // Nếu thất bại, chỉ reset dialog để người dùng có thể nhập lại
        else {
            _authState.value = _authState.value.copy(
                otpError = null,
                verificationSuccess = false
            )
        }
    }

    fun goBackToLoginAfterVerification() {
        // Reset toàn bộ auth state về trạng thái ban đầu để người dùng có thể đăng nhập
        _authState.value = _authState.value.copy(
            showVerificationDialog = false,
            verificationMessage = null,
            showOtpScreen = false,
            otpVerificationId = null,
            userEmail = null,
            otpError = null,
            isOtpVerifying = false,
            verificationSuccess = false,
            isAuthenticated = false,
            authResponse = null,
            error = null,
            isLoading = false
        )
        Log.d(TAG, "User redirected to login screen after successful email verification")
    }

    fun clearOtpError() {
        _authState.value = _authState.value.copy(otpError = null)
    }

    // Thêm method clearError
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    // Helper to extract verificationId from AuthData
    private fun extractVerificationId(authData: com.mentorme.app.data.dto.auth.AuthData): String? {
        return authData.verificationId
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authResponse: AuthResponse? = null,
    val error: String? = null,
    val userRole: UserRole? = null, // Thêm userRole
    // OTP verification states
    val showOtpScreen: Boolean = false,
    val otpVerificationId: String? = null,
    val userEmail: String? = null,
    val otpError: String? = null,
    val isOtpVerifying: Boolean = false,
    val showVerificationDialog: Boolean = false,
    val verificationSuccess: Boolean = false,
    val verificationMessage: String? = null,
    // Original signup data
    val originalSignUpData: OriginalSignUpData? = null
)

// Data class to hold original signup data
data class OriginalSignUpData(
    val userName: String,
    val email: String,
    val password: String,
    val isMentor: Boolean
)
