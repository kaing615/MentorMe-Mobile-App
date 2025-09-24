package com.mentorme.app.ui.auth.sections

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mentorme.app.ui.auth.AuthState
import com.mentorme.app.ui.auth.AuthViewModel
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import com.mentorme.app.ui.auth.RoleSelector
import com.mentorme.app.ui.auth.SmallGlassPillButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun RegisterSection(
    onSubmit: (userName: String, email: String, password: String, role: UserRole) -> Unit,
    onGotoLogin: () -> Unit,
    onBack: () -> Unit,
    onEmailSaved: (String) -> Unit = {}
) {
    val viewModel: AuthViewModel? = if (LocalInspectionMode.current) {
        null
    } else {
        runCatching { hiltViewModel<AuthViewModel>() }
            .onFailure { Log.e("RegisterSection", "Failed to initialize AuthViewModel: ${it.message}") }
            .getOrNull()
    }

    val authState by (
        viewModel?.authState ?: remember { MutableStateFlow(AuthState()) }.asStateFlow()
    ).collectAsStateWithLifecycle()

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
            "name" -> {
                val error = AuthValidator.validateUserName(value)
                if (error != null) {
                    newErrors["name"] = error
                } else {
                    newErrors.remove("name")
                }
            }
            "email" -> {
                val error = AuthValidator.validateEmail(value)
                if (error != null) {
                    newErrors["email"] = error
                } else {
                    newErrors.remove("email")
                }
            }
            "password" -> {
                val error = AuthValidator.validatePassword(value)
                if (error != null) {
                    newErrors["password"] = error
                } else {
                    newErrors.remove("password")
                }
                // Kiểm tra lại confirm password khi password thay đổi
                if (confirmPassword.isNotEmpty()) {
                    val confirmError = AuthValidator.validatePasswordConfirmation(value, confirmPassword)
                    if (confirmError != null) {
                        newErrors["confirm"] = confirmError
                    } else {
                        newErrors.remove("confirm")
                    }
                }
            }
            "confirm" -> {
                val error = AuthValidator.validatePasswordConfirmation(password, value)
                if (error != null) {
                    newErrors["confirm"] = error
                } else {
                    newErrors.remove("confirm")
                }
            }
        }
        errors = newErrors
    }

    fun validate(): Boolean {
        val validationErrors = AuthValidator.getAllSignUpErrors(username, email, password, confirmPassword)

        if (validationErrors.isNotEmpty()) {
            val errorMap = mutableMapOf<String, String>()

            validationErrors.forEach { error ->
                when {
                    error.contains("người dùng", ignoreCase = true) -> errorMap["name"] = error
                    error.contains("Email", ignoreCase = true) -> errorMap["email"] = error
                    error.contains("Mật khẩu phải có", ignoreCase = true) -> errorMap["password"] = error
                    error.contains("xác nhận", ignoreCase = true) -> errorMap["confirm"] = error
                }
            }

            errors = errorMap
            return false
        }

        errors = emptyMap()
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        FloatingLogo(size = 80.dp)
        Spacer(Modifier.height(12.dp))
        Text("Đăng ký", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))

        GlassFormContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Bạn muốn trở thành:", color = Color.White.copy(0.9f)) }
                item { RoleSelector(role = role, onRoleChange = { role = it }) }

                item {
                    GlassInput(
                        value = username,
                        onValueChange = {
                            username = it
                            validateField("name", it)
                        },
                        label = "Tên người dùng",
                        placeholder = "nguyenvana",
                        leading = { Icon(Icons.Outlined.Person, null, tint = Color.White) }
                    )
                    errors["name"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = email,
                        onValueChange = {
                            email = it
                            validateField("email", it)
                        },
                        label = "Email",
                        placeholder = "you@domain.com",
                        leading = { Icon(Icons.Default.Email, null, tint = Color.White) }
                    )
                    errors["email"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = password,
                        onValueChange = {
                            password = it
                            validateField("password", it)
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
                    errors["password"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
                    GlassInput(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            validateField("confirm", it)
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
                    errors["confirm"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                item {
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
                                    if (role == UserRole.MENTOR) {
                                        vm.signUpMentor(username, email, password, confirmPassword, username)
                                    } else {
                                        vm.signUp(username, email, password, confirmPassword, username)
                                    }
                                }.onFailure { ex ->
                                    Log.e("RegisterSection", "Registration error: ${ex.message}", ex)
                                    localError = ErrorUtils.getUserFriendlyErrorMessage(ex.message)
                                }
                            } ?: run {
                                onSubmit(username, email, password, role)
                            }
                        }
                    )
                }
            }

            val displayError = authState.error?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }
                ?: localError?.let { ErrorUtils.getUserFriendlyErrorMessage(it) }

            displayError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

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
