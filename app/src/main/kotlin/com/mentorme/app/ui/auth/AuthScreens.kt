package com.mentorme.app.ui.auth

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.auth.sections.*

/* ---------------- Data ---------------- */
data class RegisterPayload(
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole
)

private enum class AuthMode { Welcome, Login, Register, Forgot, Reset, OtpVerification }

/* ---------------- Root Screen ---------------- */
/**
 * AuthScreen (final)
 *
 * - Dùng callbacks để báo yêu cầu điều hướng cho AppNav.
 * - Tránh gọi callback điều hướng lặp lại bằng flags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (RegisterPayload) -> Boolean,
    onResetPassword: (email: String) -> Unit,

    onNavigateToMenteeHome: () -> Unit,
    onNavigateToMentorHome: () -> Unit,
    onNavigateToOnboarding: (token: String?, role: String?) -> Unit,
    onNavigateToReview: () -> Unit,
    onLogout: () -> Unit,

    startInReset: Boolean = false
) {
    var mode by remember { mutableStateOf(if (startInReset) AuthMode.Reset else AuthMode.Welcome) }
    var userEmail by remember { mutableStateOf("") }
    var dialogState by remember { mutableStateOf<OtpDialogState>(OtpDialogState.None) }

    val authViewModel: AuthViewModel? = if (LocalInspectionMode.current) null
    else runCatching { hiltViewModel<AuthViewModel>() }
        .onFailure { Log.e("AuthScreen", "Failed to init AuthViewModel: ${it.message}") }
        .getOrNull()

    val authState by (authViewModel?.authState ?: remember {
        MutableStateFlow(AuthState())
    }.asStateFlow()).collectAsStateWithLifecycle()

    fun parseRoleFromJwt(token: String?): String? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = android.util.Base64.decode(
                parts[1],
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
            )
            val json = org.json.JSONObject(String(payload, Charsets.UTF_8))
            json.optString("role", null)
        } catch (e: Exception) {
            Log.e("AuthScreen", "parseRoleFromJwt error", e)
            null
        }
    }

    // FLAGS to avoid repeated navigation calls
    var lastHandledFlowHint by remember { mutableStateOf<FlowHint?>(null) }
    var handledAuthNavigation by remember { mutableStateOf(false) }

    // When backend requests OTP screen, show it once
    LaunchedEffect(authState.showOtpScreen) {
        if (authState.showOtpScreen && mode != AuthMode.OtpVerification) {
            delay(250)
            mode = AuthMode.OtpVerification
        }
    }

    // Show OTP dialog states
    LaunchedEffect(authState.otpError) {
        authState.otpError?.let {
            dialogState = OtpDialogState.Error(message = it)
        }
    }
    LaunchedEffect(authState.verificationSuccess) {
        if (authState.verificationSuccess) {
            dialogState = OtpDialogState.Success(message = "Bạn đã nhập đúng mã OTP. Sẽ chuyển về đăng nhập.")
        }
    }

    // React to flowHint / authenticated changes, but only once per change
    LaunchedEffect(authState.flowHint, authState.isAuthenticated, authState.userRole, authState.authResponse) {
        val fh = authState.flowHint
        val token = authState.authResponse?.data?.token
        val roleFromVm = authState.userRole?.name?.lowercase()
        val roleFromJwt = parseRoleFromJwt(token)
        val roleResolved = roleFromVm ?: roleFromJwt ?: "mentee"

        if (fh != null && fh != lastHandledFlowHint) {
            // New flow hint -> handle once
            lastHandledFlowHint = fh
            handledAuthNavigation = false

            when (fh) {
                FlowHint.RequiresOnboarding -> {
                    onNavigateToOnboarding(token, roleResolved)
                }
                FlowHint.PendingApproval -> {
                    onNavigateToReview()
                }
                FlowHint.Verifying -> {
                    // OTP / verifying UI handled locally
                }
            }
            return@LaunchedEffect
        }

        // If authenticated and no flow hint, navigate to home once
        if (authState.isAuthenticated && fh == null && !handledAuthNavigation) {
            handledAuthNavigation = true
            when (roleResolved.lowercase()) {
                "mentor" -> onNavigateToMentorHome()
                else -> onNavigateToMenteeHome()
            }
            return@LaunchedEffect
        }

        // If logged out, reset handledAuthNavigation so next sign-in can navigate again
        if (!authState.isAuthenticated) {
            handledAuthNavigation = false
        }
    }

    Scaffold(
        topBar = {
            if (mode != AuthMode.Welcome) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        var pressed by remember { mutableStateOf(false) }
                        com.mentorme.app.ui.theme.LiquidGlassCard(
                            strong = true,
                            radius = 22.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer {
                                    val s = if (pressed) .92f else 1f
                                    scaleX = s; scaleY = s
                                }
                        ) {
                            IconButton(onClick = {
                                pressed = true
                                if (mode == AuthMode.OtpVerification) authViewModel?.hideOtpScreen()
                                mode = AuthMode.Welcome
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                        LaunchedEffect(pressed) {
                            if (pressed) {
                                delay(160); pressed = false
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = {
                        (fadeIn(tween(320)) + scaleIn(initialScale = 0.95f, animationSpec = tween(320)))
                            .togetherWith(fadeOut(tween(160)))
                    },
                    label = "authContent"
                ) { current ->
                    when (current) {
                        AuthMode.Welcome -> WelcomeSection(
                            onGotoLogin = { mode = AuthMode.Login },
                            onGotoRegister = { mode = AuthMode.Register }
                        )

                        AuthMode.Login -> LoginSection(
                            onSubmit = { email, pass ->
                                // gọi ViewModel để thực hiện login (nếu muốn)
                                authViewModel?.signIn(email, pass)
                            },
                            onGotoRegister = { mode = AuthMode.Register },
                            onForgot = { mode = AuthMode.Forgot },
                            onBack = { mode = AuthMode.Welcome },

                            // Các navigation callbacks đơn giản (giữ chữ ký)
                            onNavigateToMenteeHome = onNavigateToMenteeHome,
                            onNavigateToMentorHome = onNavigateToMentorHome,

                            // ==== FIX: lấy token/role từ authState, không truyền 2 tham số vào lambda ====
                            onNavigateToOnboarding = {
                                // Lấy token từ authResponse nếu có
                                val token = authState.authResponse?.data?.token

                                // Nếu ViewModel đã set userRole, dùng nó; nếu chưa, parse JWT; fallback "mentee"
                                val roleStr = authState.userRole?.name?.lowercase()
                                    ?: parseRoleFromJwt(token)
                                    ?: "mentee"

                                onNavigateToOnboarding(token, roleStr)
                            },

                            onNavigateToReview = onNavigateToReview
                        )

                        AuthMode.Register -> RegisterSection(
                            onSubmit = { name, email, pass, role ->
                                if (authViewModel != null) {
                                    if (role == UserRole.MENTOR) authViewModel.signUpMentor(name, email, pass, pass)
                                    else authViewModel.signUp(name, email, pass, pass)
                                } else {
                                    onRegister(RegisterPayload(name, email, pass, role))
                                }
                            },
                            onGotoLogin = { mode = AuthMode.Login },
                            onBack = { mode = AuthMode.Welcome },
                            onEmailSaved = { email -> userEmail = email }
                        )

                        AuthMode.Forgot -> ForgotPasswordSection(
                            onSubmit = { email ->
                                onResetPassword(email)
                                mode = AuthMode.Login
                            },
                            onBack = { mode = AuthMode.Login },
                            onHaveCode = { mode = AuthMode.Reset }
                        )

                        AuthMode.Reset -> ResetPasswordSection(
                            onSubmit = { /* TODO */ mode = AuthMode.Login },
                            onBack = { mode = AuthMode.Login }
                        )

                        AuthMode.OtpVerification -> {
                            OtpVerificationScreen(
                                email = if (userEmail.isNotEmpty()) userEmail else authState.userEmail ?: "your@email.com",
                                onOtpSubmit = { otp ->
                                    authState.otpVerificationId?.let { id ->
                                        authViewModel?.verifyOtp(id, otp)
                                    } ?: run {
                                        dialogState = OtpDialogState.Error(message = "Không có mã xác thực. Vui lòng thử lại.")
                                    }
                                },
                                onResendOtp = { authViewModel?.resendOtp() },
                                onBackToLogin = {
                                    authViewModel?.hideOtpScreen()
                                    mode = AuthMode.Login
                                },
                                onVerificationSuccess = {
                                    authViewModel?.hideOtpScreen()
                                    mode = AuthMode.Login
                                },
                                authState = authState
                            )
                        }
                    }
                }
            }

            when (dialogState) {
                is OtpDialogState.Success -> {
                    val s = dialogState as OtpDialogState.Success
                    OtpPopup(
                        isVisible = true,
                        isSuccess = true,
                        title = s.title,
                        message = s.message,
                        confirmText = "Bắt đầu thiết lập",
                        onConfirm = {
                            dialogState = OtpDialogState.None
                            authViewModel?.hideOtpScreen()
                        },
                        onDismiss = { dialogState = OtpDialogState.None }
                    )
                }
                is OtpDialogState.Error -> {
                    val e = dialogState as OtpDialogState.Error
                    OtpPopup(
                        isVisible = true,
                        isSuccess = false,
                        title = e.title,
                        message = e.message,
                        confirmText = "Đóng",
                        onConfirm = { dialogState = OtpDialogState.None },
                        onDismiss = { dialogState = OtpDialogState.None }
                    )
                }
                OtpDialogState.None -> { /* nothing */ }
            }
        }
    }
}
