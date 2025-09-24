package com.mentorme.app.ui.auth

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.utils.ErrorUtils
import com.mentorme.app.core.validation.AuthValidator
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.auth.sections.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyColumn

/* ---------------- Data ---------------- */

data class RegisterPayload(
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole
)

private enum class AuthMode { Welcome, Login, Register, Forgot, Reset, OtpVerification }

/* ---------------- Root Screen ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (RegisterPayload) -> Boolean,
    onResetPassword: (email: String) -> Unit,
    onNavigateToMenteeHome: () -> Unit,
    onNavigateToMentorHome: () -> Unit,
    startInReset: Boolean = false
) {
    var mode by remember { mutableStateOf(if (startInReset) AuthMode.Reset else AuthMode.Welcome) }
    var userEmail by remember { mutableStateOf("") }
    var dialogState by remember { mutableStateOf<OtpDialogState>(OtpDialogState.None) }

    // ViewModel & state
    val authViewModel: AuthViewModel? = if (LocalInspectionMode.current) null
    else runCatching { hiltViewModel<AuthViewModel>() }
        .onFailure {
            Log.e(
                "AuthScreens",
                "Failed to init AuthViewModel: ${it.message}"
            )
        }
        .getOrNull()

    val authState by (authViewModel?.authState ?: remember {
        MutableStateFlow(
            AuthState()
        )
    }.asStateFlow())
        .collectAsStateWithLifecycle()

    // Điều hướng mở OTP khi server yêu cầu
    LaunchedEffect(authState.showOtpScreen) {
        if (authState.showOtpScreen && mode != AuthMode.OtpVerification) {
            // Add longer delay to ensure all layout placement is complete before navigation
            delay(300)
            mode = AuthMode.OtpVerification
        }
    }

    // ===== Popup state ở CẤP CHA (overlay full-screen) =====
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Lỗi OTP -> show Error (và có thể "consume" lỗi nếu bạn có hàm trong VM)
    LaunchedEffect(authState.otpError) {
        val e = authState.otpError
        if (!e.isNullOrBlank()) {
            dialogState = OtpDialogState.Error(message = e)
            // authViewModel?.clearOtpError() // nếu có
        }
    }

    // Thành công -> show Success
    LaunchedEffect(authState.verificationSuccess) {
        if (authState.verificationSuccess) {
            dialogState = OtpDialogState.Success(message = "Bạn đã nhập đúng mã OTP. Sẽ chuyển về đăng nhập.")
            // authViewModel?.ackVerification() // nếu có
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
                                    val s = if (pressed) .92f else 1f; scaleX =
                                    s; scaleY = s
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
        // ===== OUTER BOX: KHÔNG padding -> cho popup phủ full-screen =====
        Box(modifier = Modifier.fillMaxSize()) {

            // ===== CONTENT chỉ nhận padding =====
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
                        (fadeIn(tween(400)) + scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(400)
                        ))
                            .togetherWith(fadeOut(tween(200)))
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
                                if (onLogin(email, pass)) {
                                    // Fallback navigation - shouldn't be reached with proper backend
                                    onNavigateToMenteeHome()
                                }
                            },
                            onGotoRegister = { mode = AuthMode.Register },
                            onForgot = { mode = AuthMode.Forgot },
                            onBack = { mode = AuthMode.Welcome },
                            onNavigateToMenteeHome = onNavigateToMenteeHome,
                            onNavigateToMentorHome = onNavigateToMentorHome
                        )

                        AuthMode.Register -> RegisterSection(
                            onSubmit = { name, email, pass, role ->
                                if (onRegister(
                                        RegisterPayload(
                                            name,
                                            email,
                                            pass,
                                            role
                                        )
                                    )
                                ) {
                                    // Will be handled by OTP flow
                                }
                            },
                            onGotoLogin = { mode = AuthMode.Login },
                            onBack = { mode = AuthMode.Welcome },
                            onEmailSaved = { email -> userEmail = email }
                        )

                        AuthMode.Forgot -> ForgotPasswordSection(
                            onSubmit = { email ->
                                onResetPassword(email); mode = AuthMode.Login
                            },
                            onBack = { mode = AuthMode.Login },
                            onHaveCode = { mode = AuthMode.Reset }
                        )

                        AuthMode.Reset -> ResetPasswordSection(
                            onSubmit = { /* TODO */ mode = AuthMode.Login },
                            onBack = { mode = AuthMode.Login }
                        )

                        AuthMode.OtpVerification -> {
                            // Lưu ý: OtpVerificationScreen KHÔNG cần tự vẽ popup nữa.
                            OtpVerificationScreen(
                                email = if (userEmail.isNotEmpty()) userEmail else "your@email.com",
                                onOtpSubmit = { otp ->
                                    authState.otpVerificationId?.let { id ->
                                        authViewModel?.verifyOtp(id, otp)
                                    }
                                },
                                onResendOtp = { authViewModel?.resendOtp() },
                                onBackToLogin = {
                                    authViewModel?.hideOtpScreen()
                                    mode = AuthMode.Login
                                },
                                onVerificationSuccess = {
                                    authViewModel?.goBackToLoginAfterVerification()
                                    mode = AuthMode.Login
                                },
                                authState = authState
                            )
                        }
                    }
                }
            }

            /* ======= Popup OTP: CHỈ 1 nơi vẽ ======= */
            when (dialogState) {
                is OtpDialogState.Success -> {
                    OtpPopup(
                        isVisible = true,
                        isSuccess = true,
                        title = (dialogState as OtpDialogState.Success).title,
                        message = (dialogState as OtpDialogState.Success).message,
                        confirmText = "OK",
                        onConfirm = {
                            dialogState = OtpDialogState.None
                            authViewModel?.goBackToLoginAfterVerification()
                            mode = AuthMode.Login
                        },
                        onDismiss = { dialogState = OtpDialogState.None }
                    )
                }
                is OtpDialogState.Error -> {
                    OtpPopup(
                        isVisible = true,
                        isSuccess = false,
                        title = (dialogState as OtpDialogState.Error).title,
                        message = (dialogState as OtpDialogState.Error).message,
                        confirmText = "Thử lại",
                        onConfirm = {
                            dialogState = OtpDialogState.None
                        },
                        onDismiss = { dialogState = OtpDialogState.None }
                    )
                }
                OtpDialogState.None -> {
                    // Không hiển thị popup
                }
            }
        }
    }
}
