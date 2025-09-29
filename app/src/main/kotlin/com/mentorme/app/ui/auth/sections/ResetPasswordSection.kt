package com.mentorme.app.ui.auth.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import com.mentorme.app.ui.auth.SmallGlassPillButton

@Composable
fun ResetPasswordSection(
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

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            success?.let { Text(it, color = Color.Green) }

            BigGlassButton(
                text = if (isLoading) "Đang đặt lại..." else "Đặt lại mật khẩu",
                subText = null,
                icon = { Icon(Icons.Default.Lock, null, tint = Color.White) },
                enabled = !isLoading,
                onClick = {
                    val validationError = validate()
                    if (validationError != null) {
                        error = validationError
                        return@BigGlassButton
                    }

                    isLoading = true
                    error = null
                    success = null
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Text("Quay lại đăng nhập?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng nhập", onClick = onBack)
        }
    }
}
