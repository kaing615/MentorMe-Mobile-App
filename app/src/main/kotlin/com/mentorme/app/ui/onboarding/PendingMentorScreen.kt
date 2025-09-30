package com.mentorme.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.auth.BigGlassButton
import com.mentorme.app.ui.auth.FloatingLogo
import com.mentorme.app.ui.auth.GlassFormContainer

@Composable
fun PendingApprovalScreen(
    onRefreshStatus: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .25f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            FloatingLogo(size = 76.dp)
            Spacer(Modifier.height(10.dp))

            Text(
                "Đang chờ xét duyệt",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color.White
            )
            Text(
                "Hồ sơ mentor của bạn đã được gửi 📩",
                color = Color.White.copy(.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(18.dp))

            GlassFormContainer {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Cảm ơn bạn đã đăng ký trở thành Mentor!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Hồ sơ của bạn đang được chúng tôi xem xét.\nBạn sẽ nhận được thông báo khi tài khoản được duyệt.",
                        color = Color.White.copy(.75f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))
                    BigGlassButton(
                        text = "Làm mới trạng thái",
                        subText = null,
                        icon = {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        },
                        onClick = onRefreshStatus
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBackToLogin) {
                Text("← Quay lại đăng nhập", color = Color.White.copy(0.8f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
