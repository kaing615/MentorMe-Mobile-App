package com.mentorme.app.ui.auth.sections

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
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
import com.mentorme.app.ui.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterSection(
    onSubmit: (userName: String, email: String, password: String, role: UserRole) -> Unit,
    onGotoLogin: () -> Unit,
    onBack: () -> Unit,
    onEmailSaved: (String) -> Unit = {}
) {
    val viewModel: AuthViewModel? = if (LocalInspectionMode.current) null
    else runCatching { hiltViewModel<AuthViewModel>() }
        .onFailure { Log.e("RegisterSection", "Failed to init AuthViewModel: ${it.message}") }
        .getOrNull()

    val authState by (viewModel?.authState
        ?: remember { MutableStateFlow(AuthState()) }.asStateFlow())
        .collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.MENTEE) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var localError by remember { mutableStateOf<String?>(null) }

    fun validateField(fieldName: String, value: String) {
        val newErrors = errors.toMutableMap()
        when (fieldName) {
            "name" -> AuthValidator.validateUserName(value)?.let { newErrors["name"] = it }
                ?: newErrors.remove("name")

            "email" -> AuthValidator.validateEmail(value)?.let { newErrors["email"] = it }
                ?: newErrors.remove("email")

            "password" -> {
                AuthValidator.validatePassword(value)?.let { newErrors["password"] = it }
                    ?: newErrors.remove("password")
                if (confirmPassword.isNotEmpty()) {
                    AuthValidator.validatePasswordConfirmation(value, confirmPassword)
                        ?.let { newErrors["confirm"] = it } ?: newErrors.remove("confirm")
                }
            }

            "confirm" -> AuthValidator.validatePasswordConfirmation(password, value)
                ?.let { newErrors["confirm"] = it } ?: newErrors.remove("confirm")
        }
        errors = newErrors
    }

    fun validate(): Boolean {
        val validationErrors =
            AuthValidator.getAllSignUpErrors(username, email, password, confirmPassword)
        if (validationErrors.isNotEmpty()) {
            val map = mutableMapOf<String, String>()
            validationErrors.forEach { e ->
                when {
                    e.contains("người dùng", true) -> map["name"] = e
                    e.contains("Email", true) -> map["email"] = e
                    e.contains("Mật khẩu phải có", true) -> map["password"] = e
                    e.contains("xác nhận", true) -> map["confirm"] = e
                }
            }
            errors = map; return false
        }
        errors = emptyMap(); return true
    }

    // ❗ Bỏ bottomBar; ta để nút trong form & dùng imePadding/navigationBarsPadding ngoài cùng
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { pad ->
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .imePadding()
                    .navigationBarsPadding(),
                state = listState,
                // ⚙️ Giảm spacing chung
                verticalArrangement = Arrangement.spacedBy(8.dp),
                // ⚙️ Giảm padding đáy và bỏ top padding
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                // ⚙️ Chỉ đệm status bar cho header, không cho cả list
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding() // << chỉ ở đây
                            .padding(top = 4.dp), // nhẹ thôi
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FloatingLogo(size = 56.dp)          // ⚙️ nhỏ hơn 80 -> 56
                        Spacer(Modifier.height(6.dp))       // ⚙️ 12 -> 6
                        Text(
                            "Đăng ký",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,               // ⚙️ 28 -> 26
                            color = Color.White
                        )
                    }
                }

                item {
                    GlassFormContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)     // ⚙️ gom form vào, tránh khoảng trống hai bên
                            .animateContentSize()
                    ) {
                        // ⚙️ giảm khoảng cách bên trong form
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                            Text("Bạn muốn trở thành:", color = Color.White.copy(0.9f))
                            RoleSelector(role = role, onRoleChange = { role = it })

                            fun focusMod(): Modifier {
                                val bring = BringIntoViewRequester()
                                return Modifier
                                    .bringIntoViewRequester(bring)
                                    .onFocusChanged { st -> if (st.isFocused) scope.launch { bring.bringIntoView() } }
                            }

                            GlassInput(
                                modifier = focusMod(),
                                value = username,
                                onValueChange = { username = it; validateField("name", it) },
                                label = "Tên người dùng",
                                placeholder = "nguyenvana",
                                leading = { Icon(Icons.Outlined.Person, null, tint = Color.White) }
                            )
                            errors["name"]?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            GlassInput(
                                modifier = focusMod(),
                                value = email,
                                onValueChange = { email = it; validateField("email", it) },
                                label = "Email",
                                placeholder = "you@domain.com",
                                leading = { Icon(Icons.Filled.Email, null, tint = Color.White) }
                            )
                            errors["email"]?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            GlassInput(
                                modifier = focusMod(),
                                value = password,
                                onValueChange = { password = it; validateField("password", it) },
                                label = "Mật khẩu",
                                placeholder = "••••••••",
                                leading = { Icon(Icons.Filled.Lock, null, tint = Color.White) },
                                trailing = {
                                    TextButton(onClick = { showPassword = !showPassword }) {
                                        Text(
                                            if (showPassword) "Ẩn" else "Hiện",
                                            color = Color.White
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                            )
                            errors["password"]?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            GlassInput(
                                modifier = focusMod(),
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it; validateField(
                                    "confirm",
                                    it
                                )
                                },
                                label = "Xác nhận mật khẩu",
                                placeholder = "••••••••",
                                leading = { Icon(Icons.Filled.Lock, null, tint = Color.White) },
                                trailing = {
                                    TextButton(onClick = {
                                        showConfirmPassword = !showConfirmPassword
                                    }) {
                                        Text(
                                            if (showConfirmPassword) "Ẩn" else "Hiện",
                                            color = Color.White
                                        )
                                    }
                                },
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
                            )
                            errors["confirm"]?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            val displayError =
                                authState.error?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                                    ?: localError?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                            displayError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                            // ⚙️ bỏ Spacer(8.dp) — không cần
                            BigGlassButton(
                                text = if (authState.isLoading) "Đang tạo tài khoản..." else "Tạo tài khoản",
                                subText = null,
                                icon = { Icon(Icons.Outlined.Badge, null, tint = Color.White) },
                                enabled = !authState.isLoading,
                                onClick = {
                                    if (!validate()) return@BigGlassButton
                                    localError = null
                                    onEmailSaved(email)
                                    viewModel?.let { vm ->
                                        runCatching {
                                            if (role == UserRole.MENTOR)
                                                vm.signUpMentor(
                                                    username,
                                                    email,
                                                    password,
                                                    confirmPassword,
                                                    username
                                                )
                                            else
                                                vm.signUp(
                                                    username,
                                                    email,
                                                    password,
                                                    confirmPassword,
                                                    username
                                                )
                                        }.onFailure { ex ->
                                            Log.e(
                                                "RegisterSection",
                                                "Registration error: ${ex.message}",
                                                ex
                                            )
                                            localError =
                                                ErrorUtils.getUserFriendlyErrorMessage(ex.message)
                                        }
                                    } ?: onSubmit(username, email, password, role)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, bottom = 4.dp), // ⚙️ gọn hơn
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Đã có tài khoản?", color = Color.White.copy(0.85f))
                        SmallGlassPillButton(text = "Đăng nhập ngay", onClick = onGotoLogin)
                        TextButton(onClick = onBack) {
                            Text(
                                "← Quay lại",
                                color = Color.White.copy(0.75f)
                            )
                        }
                    }
                }

                // ⚙️ bỏ Spacer(8dp) cuối list (không cần)
            }
        }
    }
}
