package com.mentorme.app.ui.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class UserUi(
    val name: String,
    val avatar: String? = null,
    val role: String = "mentee"
)

@Composable
fun HeaderBar(
    user: Any?, // Giữ type trung tính để tránh phụ thuộc model của dự án
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Không vẽ nền nào ở ngoài: chỉ padding theo status bar
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(elevation = 8.dp, shape = shape, clip = false)
            .clip(shape)
            // NỀN kính: màu trắng rất nhạt (không che nội dung phía sau)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), shape),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo / Tiêu đề
        Row(
            modifier = Modifier
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dấu chấm "online"
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00E5FF).copy(alpha = 0.85f)
                )
            ) { }
            Text(
                text = "MentorMe",
                modifier = Modifier.padding(start = 8.dp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Actions
        if (user != null) {
            Row(
                modifier = Modifier.padding(end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMessagesClick) {
                    BadgedBox(badge = { Badge { Text("1") } }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Tin nhắn",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(badge = { Badge { Text("2") } }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Cá nhân",
                        tint = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onLoginClick) { Text("Đăng nhập") }
                Button(onClick = onRegisterClick) { Text("Đăng ký") }
            }
        }
    }
}
