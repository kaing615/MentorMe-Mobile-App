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
import com.mentorme.app.data.model.UserRole
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
    onAuthed: () -> Unit,
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
                                if (onLogin(email, pass)) onAuthed()
                            },
                            onGotoRegister = { mode = AuthMode.Register },
                            onForgot = { mode = AuthMode.Forgot },
                            onBack = { mode = AuthMode.Welcome }
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
                                ) onAuthed()
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



            /* ---------------- Welcome ---------------- */

@Composable
private fun WelcomeSection(
    onGotoLogin: () -> Unit,
    onGotoRegister: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FloatingLogo(size = 96.dp)

        Text(
            text = "MentorMe",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color.White
            )
        )

        Text(
            "Kết nối mentor & mentee\nPhát triển sự nghiệp cùng nhau",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(0.85f),
                fontStyle = FontStyle.Italic,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BigGlassButton(
                text = "Đăng nhập",
                subText = "Đã có tài khoản? Đăng nhập để tiếp tục",
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = onGotoLogin
            )
            Spacer(Modifier.height(8.dp))
            BigGlassButton(
                text = "Đăng ký",
                subText = "Tạo tài khoản mới để bắt đầu hành trình",
                icon = { Icon(Icons.Outlined.Badge, null, tint = Color.White) },
                onClick = onGotoRegister
            )
        }
    }
}

/* ---------------- Login ---------------- */

@Composable
private fun LoginSection(
    onSubmit: (email: String, password: String) -> Unit,
    onGotoRegister: () -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit
) {
    // Safe ViewModel initialization with error handling
    val viewModel: AuthViewModel? = if (LocalInspectionMode.current) {
        null
    } else {
        runCatching { hiltViewModel<AuthViewModel>() }
            .onFailure { Log.e("AuthScreens", "Failed to initialize AuthViewModel: ${it.message}") }
            .getOrNull()
    }

    val authState by (
            viewModel?.authState ?: remember { MutableStateFlow(AuthState()) }.asStateFlow()
            ).collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    // Handle authentication success
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            // Call the success callback when authentication succeeds
            onSubmit(email, pass)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text("Đăng nhập", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                email, { email = it }, "Email", "your@email.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )
            GlassInput(
                pass, { pass = it }, "Mật khẩu", "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = {
                    TextButton(onClick = { show = !show }) {
                        Text(if (show) "Ẩn" else "Hiện", color = Color.White)
                    }
                },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Show errors with fallback - apply user-friendly filtering
            val displayError = authState.error?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                ?: localError?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }

            displayError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            BigGlassButton(
                text = if (authState.isLoading) "Đang đăng nhập..." else "Đăng nhập",
                subText = null,
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = {
                    // Clear previous errors
                    localError = null

                    // Validate input
                    when {
                        email.isBlank() -> {
                            localError = "Vui lòng nhập email"
                            return@BigGlassButton
                        }
                        !email.contains("@") -> {
                            localError = "Email không hợp lệ"
                            return@BigGlassButton
                        }
                        pass.isBlank() -> {
                            localError = "Vui lòng nhập mật khẩu"
                            return@BigGlassButton
                        }
                    }

                    // Use real authentication via ViewModel if available with error handling
                    viewModel?.let { vm ->
                        kotlin.runCatching {
                            vm.signIn(email, pass)
                        }.onFailure { exception ->
                            Log.e("AuthScreens", "Login error: ${exception.message}", exception)
                            localError = ErrorUtils.getUserFriendlyErrorMessage(exception.message)
                        }
                    } ?: run {
                        // Fallback for when ViewModel is not available
                        localError = "Dịch vụ xác thực không khả dụng. Vui lòng kiểm tra kết nối mạng và thử lại."
                    }


                }
            )

            // Link quên mật khẩu
            TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
                Text("Quên mật khẩu?", color = Color.White.copy(0.85f))
            }
        }

        // Footer CTA
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Text("Chưa có tài khoản?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng ký ngay", onClick = onGotoRegister)
            TextButton(onClick = onBack) { Text("← Quay lại", color = Color.White.copy(0.75f)) }
        }
    }
}

/* ---------------- Register ---------------- */

@Composable
private fun RegisterSection(
    onSubmit: (fullName: String, email: String, password: String, role: UserRole) -> Unit,
    onGotoLogin: () -> Unit,
    onBack: () -> Unit,
    onEmailSaved: (String) -> Unit = {}, // Callback để lưu email
) {
    // Safe initialization of AuthViewModel with error handling
    val viewModel: AuthViewModel? = if (LocalInspectionMode.current) {
        null
    } else {
        runCatching { hiltViewModel<AuthViewModel>() }
            .onFailure { Log.e("AuthScreens", "Failed to initialize AuthViewModel for registration: ${it.message}") }
            .getOrNull()
    }

    val authState by (
            viewModel?.authState ?: remember { MutableStateFlow(AuthState()) }.asStateFlow()
            ).collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.MENTEE) }
    var show by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var localError by remember { mutableStateOf<String?>(null) }

    // Handle authentication success
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            // Call the success callback when registration succeeds
            onSubmit(name, email, pass, role)
        }
    }

    fun validate(): Boolean {
        val e = buildMap {
            if (name.isBlank()) put("name", "Vui lòng nhập họ và tên")
            if (email.isBlank() || !email.contains("@")) put("email", "Email không hợp lệ")
            if (pass.length < 6) put("pass", "Mật khẩu tối thiểu 6 ký tự")
            if (pass != confirm) put("confirm", "Mật khẩu xác nhận không khớp")
        }
        errors = e; return e.isEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()                 // mở bàn phím vẫn đẩy footer lên
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER (không cuộn)
        Spacer(Modifier.height(8.dp))
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))
        Text("Đăng ký", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))

        // BOX KÍNH (đứng yên) + FORM BÊN TRONG (cuộn)
        GlassFormContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)               // khung ngoài chiếm phần còn lại, KHÔNG cuộn
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),          // cuộn bên trong khung
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Bạn muốn trở thành:", color = Color.White.copy(0.9f)) }
                item { RoleSelector(role = role, onRoleChange = { role = it }) }

                item {
                    GlassInput(
                        value = name, onValueChange = { name = it },
                        label = "Họ và tên", placeholder = "Nguyễn Văn A",
                        leading = { Icon(Icons.Outlined.Person, null, tint = Color.White) }
                    )
                    errors["name"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = email, onValueChange = { email = it },
                        label = "Email", placeholder = "you@domain.com",
                        leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
                    )
                    errors["email"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = pass, onValueChange = { pass = it },
                        label = "Mật khẩu", placeholder = "••••••••",
                        leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                        trailing = { TextButton(onClick = { show = !show }) { Text(if (show) "Ẩn" else "Hiện", color = Color.White) } },
                        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    errors["pass"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = confirm, onValueChange = { confirm = it },
                        label = "Xác nhận mật khẩu", placeholder = "••••••••",
                        leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                        trailing = { TextButton(onClick = { show2 = !show2 }) { Text(if (show2) "Ẩn" else "Hiện", color = Color.White) } },
                        visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    errors["confirm"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    BigGlassButton(
                        text = "Tạo tài khoản",
                        subText = null,
                        icon = { Icon(Icons.Outlined.Badge, null, tint = Color.White) },
                        onClick = {
                            if (!validate()) return@BigGlassButton
                            localError = null
                            onEmailSaved(email) // Gọi callback để lưu email
                            viewModel?.let { vm ->
                                runCatching {
                                    if (role == UserRole.MENTOR) {
                                        vm.signUpMentor(name, email, pass, confirm, name)
                                    } else {
                                        vm.signUp(name, email, pass, confirm, name)
                                    }
                                }.onFailure { ex ->
                                    Log.e("AuthScreens", "Registration error: ${ex.message}", ex)
                                    localError = ErrorUtils.getUserFriendlyErrorMessage(ex.message)
                                }
                            } ?: run {
                                // Fallback: gọi onSubmit nếu ko có VM (preview)
                                onSubmit(name, email, pass, role)
                            }
                        }
                    )
                }
            }
        }

        // FOOTER (không cuộn)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Đã có tài khoản?", color = Color.White.copy(0.85f))
            SmallGlassPillButton(text = "Đăng nhập ngay", onClick = onGotoLogin)
            TextButton(onClick = onBack) { Text("← Quay lại", color = Color.White.copy(0.75f)) }
        }
    }
}

/* ---------------- Forgot Password ---------------- */

@Composable
private fun ForgotPasswordSection(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onHaveCode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text("Quên mật khẩu", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))

        Text(
            "Nhập email của bạn để nhận liên kết đặt lại mật khẩu",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(0.8f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = email,
                onValueChange = {
                    email = it
                    error = null
                    success = null
                },
                label = "Email",
                placeholder = "your@email.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )

            // Show error message
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Show success message
            success?.let {
                Text(it, color = Color.Green)
            }

            BigGlassButton(
                text = if (isLoading) "Đang gửi..." else "Gửi liên kết đặt lại",
                subText = null,
                icon = { Icon(Icons.Default.Email, null, tint = Color.White) },
                onClick = {
                    if (email.isBlank()) {
                        error = "Vui lòng nhập email"
                        return@BigGlassButton
                    }

                    if (!email.contains("@")) {
                        error = "Email không hợp lệ"
                        return@BigGlassButton
                    }

                    isLoading = true
                    error = null
                    success = null

                    // Simulate API call
                    try {
                        onSubmit(email)
                        success = "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn"
                    } catch (e: Exception) {
                        error = "Có lỗi xảy ra: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            )

            // Link đến màn Reset (nếu đã có mã)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onHaveCode) {
                    Text("Đã có mã? Đặt lại ngay", color = Color.White.copy(0.85f))
                }
            }
        }

        // Footer
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Text("Nhớ lại mật khẩu?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng nhập ngay", onClick = onBack)
        }
    }
}

/* ---------------- Reset Password ---------------- */

@Composable
private fun ResetPasswordSection(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var resetCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        return when {
            resetCode.isBlank() -> "Vui lòng nhập mã đặt lại"
            resetCode.length < 6 -> "Mã đặt lại phải có ít nhất 6 ký tự"
            newPassword.isBlank() -> "Vui lòng nhập mật khẩu mới"
            newPassword.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
            confirmPassword.isBlank() -> "Vui lòng xác nhận mật khẩu"
            newPassword != confirmPassword -> "Mật khẩu xác nhận không khớp"
            else -> null
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text("Đặt lại mật khẩu", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))

        Text(
            "Nhập mã đặt lại từ email và mật khẩu mới",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(0.8f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = resetCode,
                onValueChange = {
                    resetCode = it
                    error = null
                    success = null
                },
                label = "Mã đặt lại",
                placeholder = "Nhập mã từ email",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )

            GlassInput(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    error = null
                    success = null
                },
                label = "Mật khẩu mới",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ẩn" else "Hiện", color = Color.White)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            )

            GlassInput(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    error = null
                    success = null
                },
                label = "Xác nhận mật khẩu",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = {
                    TextButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Text(if (showConfirmPassword) "Ẩn" else "Hiện", color = Color.White)
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Show error message
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Show success message
            success?.let {
                Text(it, color = Color.Green)
            }

            BigGlassButton(
                text = if (isLoading) "Đang đặt lại..." else "Đặt lại mật khẩu",
                subText = null,
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = {
                    val validationError = validate()
                    if (validationError != null) {
                        error = validationError
                        return@BigGlassButton
                    }

                    isLoading = true
                    error = null
                    success = null

                    // Simulate API call
                    try {
                        onSubmit(newPassword)
                        success = "Mật khẩu đã được đặt lại thành công!"
                    } catch (e: Exception) {
                        error = "Có lỗi xảy ra: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            )
        }

        // Footer
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Text("Quay lại đăng nhập?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng nhập", onClick = onBack)
        }
    }
}
