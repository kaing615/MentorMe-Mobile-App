package com.mentorme.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private enum class AuthMode { Welcome, Login, Register, Forgot, Reset }

/* ---------------- Root Screen ---------------- */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (RegisterPayload) -> Boolean,
    onResetPassword: (email: String) -> Unit,   // dùng cho màn Forgot
    onAuthed: () -> Unit,
    startInReset: Boolean = false               // có thể mở thẳng màn Reset khi deeplink
) {
    var mode by remember { mutableStateOf(if (startInReset) AuthMode.Reset else AuthMode.Welcome) }

    Scaffold(
        topBar = {
            if (mode != AuthMode.Welcome) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        // Back liquid glass (viền đẹp, không mất)
                        var pressed by remember { mutableStateOf(false) }
                        com.mentorme.app.ui.theme.LiquidGlassCard(
                            strong = true,
                            radius = 22.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer { val s = if (pressed) .92f else 1f; scaleX = s; scaleY = s }
                        ) {
                            IconButton(onClick = { pressed = true; mode = AuthMode.Welcome }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        }
                        LaunchedEffect(pressed) { if (pressed) { delay(160); pressed = false } }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    fadeIn(tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)) with
                            fadeOut(tween(200))
                },
                label = "authContent"
            ) { current ->
                when (current) {
                    AuthMode.Welcome -> WelcomeSection(
                        onGotoLogin = { mode = AuthMode.Login },
                        onGotoRegister = { mode = AuthMode.Register }
                    )
                    AuthMode.Login -> LoginSection(
                        onSubmit = { email, pass -> if (onLogin(email, pass)) onAuthed() },
                        onGotoRegister = { mode = AuthMode.Register },
                        onForgot = { mode = AuthMode.Forgot },
                        onBack = { mode = AuthMode.Welcome }
                    )
                    AuthMode.Register -> RegisterSection(
                        onSubmit = { name, email, pass, role ->
                            if (onRegister(RegisterPayload(name, email, pass, role))) onAuthed()
                        },
                        onGotoLogin = { mode = AuthMode.Login },
                        onBack = { mode = AuthMode.Welcome },
                    )
                    AuthMode.Forgot -> ForgotPasswordSection(
                        onSubmit = { email -> onResetPassword(email); mode = AuthMode.Login },
                        onBack = { mode = AuthMode.Login },
                        onHaveCode = { mode = AuthMode.Reset }
                    )
                    AuthMode.Reset -> ResetPasswordSection(
                        onSubmit = { /* TODO: call API đặt lại mật khẩu bằng token */ mode = AuthMode.Login },
                        onBack = { mode = AuthMode.Login }
                    )
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
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            BigGlassButton(
                text = if (loading) "Đang đăng nhập..." else "Đăng nhập",
                subText = null,
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = {
                    if (email.isBlank() || pass.isBlank()) {
                        error = "Vui lòng nhập đầy đủ email và mật khẩu"
                        return@BigGlassButton
                    }
                    loading = true
                    onSubmit(email, pass)
                    loading = false
                    error = null
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
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.MENTEE) }
    var show by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

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
                        onClick = { if (validate()) onSubmit(name, email, pass, role) }
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
    var error by remember { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text("Quên mật khẩu", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color.White)
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "your@email.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            BigGlassButton(
                text = "Gửi liên kết đặt lại mật khẩu",
                icon = { Icon(Icons.Default.Email, null, tint = Color.White) },
                onClick = {
                    if (email.isBlank()) {
                        error = "Vui lòng nhập email"
                    } else {
                        error = null
                        onSubmit(email)
                    }
                }
            )

            // Đi tới màn Reset (trường hợp có sẵn mã / deeplink nội bộ)
            TextButton(onClick = onHaveCode, modifier = Modifier.align(Alignment.End)) {
                Text("Đã có mã? Đặt lại ngay", color = Color.White.copy(0.85f))
            }
        }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("← Quay lại", color = Color.White.copy(0.75f))
        }
    }
}

/* ---------------- Reset Password ---------------- */

@Composable
fun ResetPasswordSection(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))

        Text("Đặt lại mật khẩu", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color.White)
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = pass,
                onValueChange = { pass = it },
                label = "Mật khẩu mới",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = { TextButton(onClick = { show = !show }) { Text(if (show) "Ẩn" else "Hiện", color = Color.White) } },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
            )

            GlassInput(
                value = confirm,
                onValueChange = { confirm = it },
                label = "Xác nhận mật khẩu",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = { TextButton(onClick = { show2 = !show2 }) { Text(if (show2) "Ẩn" else "Hiện", color = Color.White) } },
                visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            BigGlassButton(
                text = "Đặt lại mật khẩu",
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = {
                    if (pass.length < 6) {
                        error = "Mật khẩu tối thiểu 6 ký tự"
                        return@BigGlassButton
                    }
                    if (pass != confirm) {
                        error = "Mật khẩu xác nhận không khớp"
                        return@BigGlassButton
                    }
                    error = null
                    onSubmit(pass)
                }
            )
        }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("← Quay lại", color = Color.White.copy(0.75f))
        }
    }
}
