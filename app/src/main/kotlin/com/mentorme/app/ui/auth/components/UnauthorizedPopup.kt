package com.mentorme.app.ui.auth.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UnauthorizedPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    title: String = "Tài khoản đang chờ xét duyệt",
    message: String = "Tài khoản của bạn hiện đang được xét duyệt. Vui lòng quay lại sau khi quá trình xét duyệt hoàn tất. Chúng tôi sẽ thông báo qua email khi tài khoản được kích hoạt."
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.DarkGray.copy(alpha = 0.5f), // ✅ Đồng bộ với OtpPopup
            scrimColor = Color.Black.copy(alpha = 0.75f), // ✅ Đồng bộ với OtpPopup
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = .5f)) // ✅ Đồng bộ với OtpPopup
                )
            }
        ) {
            UnauthorizedBottomSheetContent(
                title = title,
                message = message,
                onConfirm = onDismiss,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun UnauthorizedBottomSheetContent(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(20.dp) // ✅ Tăng padding như OtpPopup
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Tăng spacing như OtpPopup
    ) {
        // ==== Icon + animation giống OtpPopup ====
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(320, easing = EaseOutBack),
            label = "pendingScale"
        )
        val rotate by animateFloatAsState(
            targetValue = 0f,
            animationSpec = tween(320),
            label = "pendingRotate"
        )

        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = Color(0xFFFF9800), // Orange color cho pending
            modifier = Modifier
                .size(52.dp)
                .scale(scale)
                .rotate(rotate)
        )

        // Title - màu trắng như OtpPopup
        Text(
            title,
            color = Color.White, // ✅ Đổi sang màu trắng như OtpPopup
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, // ✅ Thêm Bold như OtpPopup
            textAlign = TextAlign.Center
        )

        // Message - màu trắng nhạt như OtpPopup
        Text(
            message,
            color = Color.White.copy(.75f), // ✅ Đổi sang màu trắng nhạt như OtpPopup
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(6.dp))

        // Actions - chỉ 1 nút primary
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Primary button
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f), // ✅ Như OtpPopup
                    contentColor = Color.White // ✅ Text trắng
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // ✅ Tăng height như OtpPopup
            ) {
                Text("Đã hiểu")
            }
        }
    }
}
