package com.mentorme.app.ui.auth.sections

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.utils.ErrorUtils
import com.mentorme.app.core.validation.AuthValidator
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.auth.AuthState
import com.mentorme.app.ui.auth.AuthViewModel
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import com.mentorme.app.ui.auth.SmallGlassPillButton
import com.mentorme.app.ui.auth.components.UnauthorizedPopup
import com.mentorme.app.ui.auth.FlowHint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LoginSection(
    onSubmit: (email: String, password: String) -> Unit,
    onGotoRegister: () -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit,
    onNavigateToMenteeHome: () -> Unit,
    onNavigateToMentorHome: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val viewModel: AuthViewModel? = if (LocalInspectionMode.current) {
        null
    } else {
        runCatching { hiltViewModel<AuthViewModel>() }
            .onFailure { Log.e("LoginSection", "Failed to initialize AuthViewModel: ${it.message}") }
            .getOrNull()
    }

    val authState by (
            viewModel?.authState ?: remember { MutableStateFlow(AuthState()) }.asStateFlow()
            ).collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showUnauthorizedPopup by remember { mutableStateOf(false) }

    var navigated by remember { mutableStateOf(false) }

    val justOnboarded by (
            viewModel?.hasJustOnboarded ?: MutableStateFlow(false)
            ).collectAsStateWithLifecycle()

    // Điều hướng/hiển thị theo state
    LaunchedEffect(authState) {
        Log.d(
            "LoginSection",
            "AuthState: auth=${authState.isAuthenticated}, error=${authState.error}, flow=${authState.flowHint}, role=${authState.userRole}, next=${authState.next}"
        )
        if (navigated) return@LaunchedEffect

        when {
            // Đăng nhập thành công
            authState.isAuthenticated && !authState.isLoading -> {
                when (authState.userRole) {
                    UserRole.MENTOR -> onNavigateToMentorHome()
                    UserRole.MENTEE, null -> onNavigateToMenteeHome()
                }
                navigated = true
            }

            // ❗️Luồng điều hướng KHÔNG coi là lỗi UI
            authState.flowHint == FlowHint.RequiresOnboarding -> {
                viewModel?.clearError()
                viewModel?.clearFlowHint()
                showUnauthorizedPopup = false
                navigated = true
                when (authState.next) {
                    "/onboarding/review" -> onNavigateToReview()
                    else -> onNavigateToOnboarding()
                }
            }

            authState.flowHint == FlowHint.PendingApproval -> {
                viewModel?.clearError()
                // Nếu vừa onboard xong hoặc backend trả next=review → điều hướng luôn
                if (justOnboarded || authState.next == "/onboarding/review") {
                    navigated = true
                    viewModel?.hasJustOnboarded?.value = false
                    onNavigateToReview()
                } else {
                    showUnauthorizedPopup = true
                }
            }

            // Lỗi thực sự → hiển thị
            authState.error != null -> {
                val processedError = ErrorUtils.getUserFriendlyErrorMessage(authState.error)
                if (processedError == "pending_approval") {
                    // Nếu ErrorUtils map nhầm: bỏ qua ở đây, flowHint mới là nguồn điều hướng
                    Log.w("LoginSection", "Ignoring 'pending_approval' as user-visible error; expecting flowHint")
                }
            }
        }
    }

    // Popup hiển thị khi tài khoản đang chờ xét duyệt
    if (showUnauthorizedPopup) {
        UnauthorizedPopup(
            isVisible = showUnauthorizedPopup,
            onDismiss = {
                showUnauthorizedPopup = false
                viewModel?.clearError()
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text(
            "Đăng nhập",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Color.White
        )
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                },
                label = "Email",
                placeholder = "your@email.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )

            GlassInput(
                value = password,
                onValueChange = {
                    password = it
                    localError = null
                },
                label = "Mật khẩu",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ẩn" else "Hiện", color = Color.White)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Chỉ hiển thị lỗi khi KHÔNG có flowHint (tức là không ở trạng thái điều hướng)
            val displayError = if (authState.flowHint == null) {
                authState.error?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                    ?: localError
            } else null

            displayError?.let {
                if (!it.contains("unauthorized", ignoreCase = true) &&
                    !it.contains("pending", ignoreCase = true)
                ) {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            BigGlassButton(
                text = if (authState.isLoading) "Đang đăng nhập..." else "Đăng nhập",
                subText = null,
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                enabled = !authState.isLoading,
                onClick = {
                    // Validation
                    if (email.isBlank() || password.isBlank()) {
                        localError = "Vui lòng nhập đầy đủ email và mật khẩu"
                        return@BigGlassButton
                    }

                    val emailError = AuthValidator.validateEmail(email)
                    if (emailError != null) {
                        localError = emailError
                        return@BigGlassButton
                    }

                    localError = null
                    showUnauthorizedPopup = false

                    // Gọi backend login
                    viewModel?.let { vm ->
                        runCatching {
                            vm.clearError()
                            vm.clearFlowHint()
                            vm.signIn(email, password)
                        }.onFailure { ex ->
                            Log.e("LoginSection", "Login error: ${ex.message}", ex)
                            localError = ErrorUtils.getUserFriendlyErrorMessage(ex.message)
                        }
                    } ?: run {
                        // Fallback cho preview mode
                        onSubmit(email, password)
                    }
                }
            )

            TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
                Text("Quên mật khẩu?", color = Color.White.copy(0.85f))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text("Chưa có tài khoản?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng ký ngay", onClick = onGotoRegister)
            TextButton(onClick = onBack) {
                Text("← Quay lại", color = Color.White.copy(0.75f))
            }
        }
    }
}
