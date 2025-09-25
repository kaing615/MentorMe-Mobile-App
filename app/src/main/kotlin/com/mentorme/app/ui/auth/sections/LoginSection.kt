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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LoginSection(
    onSubmit: (email: String, password: String) -> Unit,
    onGotoRegister: () -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit,
    onNavigateToMenteeHome: () -> Unit,
    onNavigateToMentorHome: () -> Unit
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

    // Xử lý kết quả đăng nhập từ backend
    LaunchedEffect(authState) {
        Log.d("LoginSection", "AuthState changed: isAuthenticated=${authState.isAuthenticated}, error=${authState.error}, userRole=${authState.userRole}")

        when {
            // Đăng nhập thành công
            authState.isAuthenticated && !authState.isLoading -> {
                Log.d("LoginSection", "Login successful, navigating to home for role: ${authState.userRole}")
                // Điều hướng dựa trên role của user
                when (authState.userRole) {
                    UserRole.MENTOR -> {
                        Log.d("LoginSection", "Navigating to MENTOR home")
                        onNavigateToMentorHome()
                    }
                    UserRole.MENTEE -> {
                        Log.d("LoginSection", "Navigating to MENTEE home")
                        onNavigateToMenteeHome()
                    }
                    else -> {
                        Log.w("LoginSection", "Unknown role: ${authState.userRole}, defaulting to MENTEE home")
                        onNavigateToMenteeHome() // Default to mentee home
                    }
                }
            }
            // Tài khoản chờ xét duyệt (kiểm tra error message từ ErrorUtils)
            authState.error != null -> {
                val processedError = ErrorUtils.getUserFriendlyErrorMessage(authState.error)
                Log.d("LoginSection", "Original error: ${authState.error}")
                Log.d("LoginSection", "Processed error: $processedError")

                if (processedError == "pending_approval") {
                    Log.d("LoginSection", "Showing unauthorized popup for pending approval")
                    showUnauthorizedPopup = true
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

            // Hiển thị lỗi
            val displayError = authState.error?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                ?: localError
            displayError?.let {
                if (!it.contains("unauthorized", ignoreCase = true) && !it.contains("pending", ignoreCase = true)) {
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
