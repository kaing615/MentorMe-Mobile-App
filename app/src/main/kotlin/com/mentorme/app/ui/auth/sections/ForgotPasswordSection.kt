package com.mentorme.app.ui.auth.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer
import com.mentorme.app.ui.auth.GlassInput
import com.mentorme.app.ui.auth.SmallGlassPillButton

@Composable
fun ForgotPasswordSection(
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

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            success?.let { Text(it, color = Color.Green) }

            BigGlassButton(
                text = if (isLoading) "Đang gửi..." else "Gửi liên kết đặt lại",
                subText = null,
                icon = { Icon(Icons.Default.Email, null, tint = Color.White) },
                enabled = !isLoading,
                onClick = {
                    if (email.isBlank()) {
                        error = "Vui lòng nhập email"; return@BigGlassButton
                    }
                    if (!email.contains("@")) {
                        error = "Email không hợp lệ"; return@BigGlassButton
                    }

                    isLoading = true
                    error = null
                    success = null
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onHaveCode) {
                    Text("Đã có mã? Đặt lại ngay", color = Color.White.copy(0.85f))
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Text("Nhớ lại mật khẩu?", color = Color.White.copy(0.85f))
            SmallGlassPillButton("Đăng nhập ngay", onClick = onBack)
        }
    }
}
