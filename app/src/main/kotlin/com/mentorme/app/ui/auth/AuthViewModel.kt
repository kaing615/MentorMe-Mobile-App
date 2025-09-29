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
import com.mentorme.app.core.datastore.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signUpMentorUseCase: SignUpMentorUseCase,
    private val signInUseCase: SignInUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val hasJustOnboarded = MutableStateFlow(false)

    private suspend fun saveAndConfirmToken(token: String) {
        dataStoreManager.saveToken(token)
        Log.d(TAG, "💾 Token saved request: $token")

        // đợi token thực sự được ghi
        var confirmed: String? = null
        repeat(5) { // thử lại tối đa 5 lần, mỗi lần 100ms
            delay(100)
            confirmed = dataStoreManager.getToken().first()
            if (!confirmed.isNullOrBlank()) return@repeat
        }
        Log.d(TAG, "📦 Token confirmed in DataStore: $confirmed")
    }

    fun signUp(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String? = null
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🔥 SIGNUP CALLED - EMAIL: $email")
            _authState.value = _authState.value.copy(isLoading = true, error = null, flowHint = null)

            when (val result = signUpUseCase(username, email, password, confirmPassword)) {
                is AppResult.Success -> {
                    Log.d(TAG, "✅ SignUp success: ${result.data}")

                    val verificationId = result.data.data?.let { extractVerificationId(it) }
                    val token = result.data.data?.token
                    if (!token.isNullOrBlank()) {
                        saveAndConfirmToken(token)
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null,
                        originalSignUpData = OriginalSignUpData(username, email, password, false),
                        flowHint = FlowHint.Verifying
                    )
                }
                is AppResult.Error -> handleAuthError(result.throwable)
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
            _authState.value = _authState.value.copy(isLoading = true, error = null, flowHint = null)

            when (val result = signUpMentorUseCase(username, email, password, confirmPassword)) {
                is AppResult.Success -> {
                    Log.d(TAG, "✅ SignUpMentor success: ${result.data}")
                    val verificationId = result.data.data?.let { extractVerificationId(it) }
                    val token = result.data.data?.token
                    if (!token.isNullOrBlank()) {
                        saveAndConfirmToken(token)
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        showOtpScreen = true,
                        otpVerificationId = verificationId,
                        userEmail = email,
                        otpError = null,
                        originalSignUpData = OriginalSignUpData(username, email, password, true),
                        flowHint = FlowHint.Verifying
                    )
                }
                is AppResult.Error -> handleAuthError(result.throwable)
                AppResult.Loading -> Unit
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔥 SIGNIN CALLED - EMAIL: $email")
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                flowHint = null
            )

            when (val result = signInUseCase.invoke(email, password)) {
                is AppResult.Success -> {
                    val data = result.data.data
                    val token = data?.token

                    if (!token.isNullOrBlank()) {
                        saveAndConfirmToken(token)
                    }

                    val roleStrFromData = data?.role
                    val roleStr = roleStrFromData ?: parseRoleFromJwt(token)
                    val role = if (roleStr == "mentor") UserRole.MENTOR else UserRole.MENTEE

                    val isActive = data?.status == "active"
                    val authenticated = isActive && !token.isNullOrBlank()
                    val pendingApproval = data?.status == "pending-mentor"
                    val onboarding = data?.status == "onboarding"
                    val verifying = data?.status == "verifying"

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        authResponse = result.data,
                        isAuthenticated = authenticated,
                        userRole = role,
                        // dùng flowHint để UI điều hướng, KHÔNG gán vào error
                        flowHint = when {
                            verifying -> FlowHint.Verifying
                            onboarding -> FlowHint.RequiresOnboarding
                            pendingApproval -> FlowHint.PendingApproval
                            else -> null
                        },
                        next = data?.next,
                        showOtpScreen = verifying,
                        userEmail = if (verifying) email else _authState.value.userEmail,
                        otpVerificationId = if (verifying) data?.verificationId else _authState.value.otpVerificationId
                    )
                }

                is AppResult.Error -> handleAuthError(result.throwable)
                AppResult.Loading -> Unit
            }
        }
    }

    private fun handleAuthError(throwable: String?) {
        val raw = throwable ?: "Unknown error"
        Log.e(TAG, "❌ Auth failed: $raw")
        val friendly = ErrorUtils.getUserFriendlyErrorMessage(raw)
        _authState.value = _authState.value.copy(
            isLoading = false,
            // chỉ dùng error cho hiển thị – không đụng flowHint
            error = if (friendly.isNullOrBlank()) raw else friendly
        )
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
                    Log.d(TAG, "✅ OTP verification success: ${result.data}")

                    // Sau khi OTP xác minh thành công → gọi lại signIn để lấy token
                    val email = _authState.value.userEmail
                    val original = _authState.value.originalSignUpData

                    if (email != null && original != null) {
                        Log.d(TAG, "📡 Auto sign-in after OTP verify with email=$email")
                        signIn(email, original.password)
                    } else {
                        Log.w(TAG, "⚠️ Không có email/password để sign-in lại sau OTP")
                    }

                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        showOtpScreen = false,
                        showVerificationDialog = true,
                        verificationSuccess = true,
                        verificationMessage = "Xác minh email thành công! Đang đăng nhập...",
                        flowHint = null
                    )
                }

                is AppResult.Error -> {
                    val errMsg: String = result.throwable ?: "OTP verification failed"
                    Log.e(TAG, "OTP verification failed: $errMsg")
                    val friendly = ErrorUtils.getUserFriendlyErrorMessage(errMsg)
                    _authState.value = _authState.value.copy(
                        isOtpVerifying = false,
                        otpError = friendly,
                        showVerificationDialog = true,
                        verificationSuccess = false,
                        verificationMessage = friendly
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

    fun dismissVerificationDialog() {
        _authState.value = _authState.value.copy(
            showVerificationDialog = false,
            verificationMessage = null
        )

        // Nếu verification thành công, reset toàn bộ OTP screen
        if (_authState.value.verificationSuccess) {
            hideOtpScreen()
        } else {
            // Nếu thất bại, chỉ reset dialog để người dùng có thể nhập lại
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
            isLoading = false,
            flowHint = null
        )
        Log.d(TAG, "User redirected to login screen after successful email verification")
    }

    fun clearOtpError() {
        _authState.value = _authState.value.copy(otpError = null)
    }

    // Dùng khi chuẩn bị gọi signIn/SignUp mới hoặc sau khi điều hướng xong
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearFlowHint() {
        _authState.value = _authState.value.copy(flowHint = null)
    }

    // Helper to extract verificationId from AuthData
    private fun extractVerificationId(authData: com.mentorme.app.data.dto.auth.AuthData): String? {
        return authData.verificationId
    }

    private fun parseRoleFromJwt(token: String?): String? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = android.util.Base64.decode(
                parts[1].replace('-', '+').replace('_', '/'),
                android.util.Base64.DEFAULT
            )
            val json = org.json.JSONObject(String(payload, Charsets.UTF_8))
            json.optString("role", null) // "mentor" | "mentee"
        } catch (e: Exception) {
            null
        }
    }

}

// ================== State & Models ==================

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authResponse: AuthResponse? = null,

    // ❗️Chỉ dành cho thông báo lỗi hiển thị cho người dùng
    val error: String? = null,

    val userRole: UserRole? = null,
    val showOtpScreen: Boolean = false,
    val otpVerificationId: String? = null,
    val userEmail: String? = null,
    val otpError: String? = null,
    val isOtpVerifying: Boolean = false,
    val showVerificationDialog: Boolean = false,
    val verificationSuccess: Boolean = false,
    val verificationMessage: String? = null,
    val originalSignUpData: OriginalSignUpData? = null,

    val next: String? = null,

    // ❗️Dành cho điều hướng (không phải lỗi)
    val flowHint: FlowHint? = null
)

data class OriginalSignUpData(
    val userName: String,
    val email: String,
    val password: String,
    val isMentor: Boolean
)

enum class FlowHint {
    RequiresOnboarding,
    PendingApproval,
    Verifying
}
