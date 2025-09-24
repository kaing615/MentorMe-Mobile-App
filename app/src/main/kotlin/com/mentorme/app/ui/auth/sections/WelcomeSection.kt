package com.mentorme.app.ui.auth.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo

@Composable
fun WelcomeSection(
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
