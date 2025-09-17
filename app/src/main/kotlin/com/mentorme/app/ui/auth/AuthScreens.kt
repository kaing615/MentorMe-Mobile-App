package com.mentorme.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.geometry.Offset

/* ---------------- Data ---------------- */

data class RegisterPayload(
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole
)

private enum class AuthMode { Welcome, Login, Register }

/* ---------------- Root Screen ---------------- */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (RegisterPayload) -> Boolean,
    onAuthed: () -> Unit
) {
    var mode by remember { mutableStateOf(AuthMode.Welcome) }

    Scaffold(
        topBar = {
            if (mode != AuthMode.Welcome) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        LiquidGlassCard(
                            strong = true,
                            radius = 28.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(onClick = { mode = AuthMode.Welcome }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        onSubmit = { email, pass ->
                            if (onLogin(email, pass)) onAuthed()
                        }
                    )
                    AuthMode.Register -> RegisterSection(
                        onSubmit = { name, email, pass, role ->
                            if (onRegister(RegisterPayload(name, email, pass, role))) onAuthed()
                        }
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
        LiquidGlassCard(strong = true, modifier = Modifier.size(96.dp), radius = 48.dp) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Favorite, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        Text(
            text = "MentorMe",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color.White,
                shadow = Shadow(
                    color = Color.White.copy(alpha = 0.5f),
                    offset = Offset(0f, 2f),
                    blurRadius = 10f
                )
            )
        )

        Text(
            "Kết nối Mentor & Mentee\nPhát triển sự nghiệp cùng nhau",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(0.85f),
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.5.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BigGlassButton(text = "Đăng nhập", icon = { Icon(Icons.Default.Lock, null, tint = Color.White) }, onClick = onGotoLogin)
            Spacer(Modifier.height(10.dp))
            BigGlassButton(text = "Đăng ký", icon = { Icon(Icons.Outlined.Badge, null, tint = Color.White) }, onClick = onGotoRegister)
        }
    }
}

/* ---------------- Glass Form Container ---------------- */

@Composable
private fun GlassFormContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlassCard(
        strong = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        radius = 28.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

/* ---------------- Glass Input ---------------- */

@Composable
private fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(0.8f)) },
        placeholder = { Text(placeholder, color = Color.White.copy(0.5f)) },
        leadingIcon = leading,
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedBorderColor = Color.White.copy(alpha = 0.35f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            cursorColor = Color.White,
            focusedLabelColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/* ---------------- Big Glass Button ---------------- */

@Composable
private fun BigGlassButton(
    text: String,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
            contentColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .liquidGlass(radius = 32.dp)
            .then(
                if (pressed) Modifier.background(
                    Brush.radialGradient(listOf(Color.White.copy(0.25f), Color.Transparent))
                ) else Modifier
            ),
        interactionSource = remember { MutableInteractionSource() },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(300)
            pressed = false
        }
    }
}

/* ---------------- Role Selector ---------------- */

@Composable
private fun RoleSelector(
    role: UserRole,
    onRoleChange: (UserRole) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoleOptionCard(
            title = "Mentee",
            selected = role == UserRole.MENTEE,
            onClick = { onRoleChange(UserRole.MENTEE) }
        )
        RoleOptionCard(
            title = "Mentor",
            selected = role == UserRole.MENTOR,
            onClick = { onRoleChange(UserRole.MENTOR) }
        )
    }
}

@Composable
private fun RowScope.RoleOptionCard(   // 👈 đổi: RowScope
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val goldGradient = Brush.linearGradient(
        listOf(Color(0xFFFFD700), Color(0xFFFFB347), Color(0xFFFF8C00))
    )

    LiquidGlassCard(
        strong = true,
        radius = 18.dp,
        modifier = Modifier
            .weight(1f)              // 👈 giờ hợp lệ
            .height(55.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (selected) goldGradient else Brush.linearGradient(
                        listOf(Color.White.copy(0.1f), Color.White.copy(0.05f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.Black else Color.White
                )
            )
        }
    }
}


/* ---------------- Login ---------------- */

@Composable
private fun LoginSection(
    onSubmit: (email: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Đăng nhập",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color.White
            )
        )
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            GlassInput(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "your@email.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )
            GlassInput(
                value = pass,
                onValueChange = { pass = it },
                label = "Mật khẩu",
                placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = { TextButton(onClick = { show = !show }) { Text(if (show) "Ẩn" else "Hiện", color = Color.White) } },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Quên mật khẩu?", color = Color.White.copy(0.7f))
            }
            BigGlassButton(
                text = if (loading) "Đang đăng nhập..." else "Đăng nhập",
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                onClick = {
                    if (loading) return@BigGlassButton
                    loading = true
                    onSubmit(email, pass)
                    loading = false
                }
            )
        }
    }
}

/* ---------------- Register ---------------- */

@Composable
private fun RegisterSection(
    onSubmit: (fullName: String, email: String, password: String, role: UserRole) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf(UserRole.MENTEE) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var loading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        val e = buildMap {
            if (name.isBlank()) put("name", "Vui lòng nhập họ và tên")
            if (email.isBlank() || !email.contains("@") || !email.contains(".")) put("email", "Email không hợp lệ")
            if (pass.length < 6) put("pass", "Mật khẩu tối thiểu 6 ký tự")
            if (pass != confirm) put("confirm", "Mật khẩu xác nhận không khớp")
        }
        errors = e; return e.isEmpty()
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Đăng ký",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color.White
            )
        )
        Spacer(Modifier.height(20.dp))

        GlassFormContainer {
            Text("Bạn muốn trở thành:", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.9f))
            RoleSelector(role = role, onRoleChange = { role = it })

            GlassInput(
                value = name, onValueChange = { name = it },
                label = "Họ và tên", placeholder = "Nguyễn Văn A",
                leading = { Icon(Icons.Outlined.Person, null, tint = Color.White) }
            )
            errors["name"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            GlassInput(
                value = email, onValueChange = { email = it },
                label = "Email", placeholder = "you@domain.com",
                leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
            )
            errors["email"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            GlassInput(
                value = pass, onValueChange = { pass = it },
                label = "Mật khẩu", placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = { TextButton(onClick = { show = !show }) { Text(if (show) "Ẩn" else "Hiện", color = Color.White) } },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation()
            )
            errors["pass"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            GlassInput(
                value = confirm, onValueChange = { confirm = it },
                label = "Xác nhận mật khẩu", placeholder = "••••••••",
                leading = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                trailing = { TextButton(onClick = { show2 = !show2 }) { Text(if (show2) "Ẩn" else "Hiện", color = Color.White) } },
                visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation()
            )
            errors["confirm"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            BigGlassButton(
                text = if (loading) "Đang đăng ký..." else "Tạo tài khoản",
                icon = { Icon(Icons.Outlined.Badge, null, tint = Color.White) },
                onClick = {
                    if (loading) return@BigGlassButton
                    if (!validate()) return@BigGlassButton
                    loading = true
                    onSubmit(name, email, pass, role)
                    loading = false
                }
            )
        }
    }
}
