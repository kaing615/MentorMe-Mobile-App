package com.mentorme.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class NotificationTypeStyle(
    val icon: ImageVector,
    val color: Color,
    val label: String
)

internal fun notificationTypeStyle(type: NotificationType): NotificationTypeStyle {
    return when (type) {
        NotificationType.BOOKING_CONFIRMED -> NotificationTypeStyle(
            Icons.Outlined.CheckCircle,
            Color(0xFF34D399),
            "Đặt lịch"
        )
        NotificationType.BOOKING_REMINDER -> NotificationTypeStyle(
            Icons.Outlined.AccessTime,
            Color(0xFFFBBF24),
            "Nhắc lịch"
        )
        NotificationType.BOOKING_CANCELLED -> NotificationTypeStyle(
            Icons.Outlined.EventBusy,
            Color(0xFFF87171),
            "Hủy lịch"
        )
        NotificationType.BOOKING_PENDING -> NotificationTypeStyle(
            Icons.Outlined.AccessTime,
            Color(0xFFFBBF24),
            "Chờ xác nhận"
        )
        NotificationType.BOOKING_DECLINED -> NotificationTypeStyle(
            Icons.Outlined.EventBusy,
            Color(0xFFF87171),
            "Từ chối"
        )
        NotificationType.BOOKING_FAILED -> NotificationTypeStyle(
            Icons.Outlined.EventBusy,
            Color(0xFFF87171),
            "Thất bại"
        )
        NotificationType.PAYMENT_SUCCESS -> NotificationTypeStyle(
            Icons.Outlined.CheckCircle,
            Color(0xFF34D399),
            "Thanh toán"
        )
        NotificationType.PAYMENT_FAILED -> NotificationTypeStyle(
            Icons.Outlined.EventBusy,
            Color(0xFFF87171),
            "Thanh toán lỗi"
        )
        NotificationType.MESSAGE -> NotificationTypeStyle(
            Icons.Outlined.Message,
            Color(0xFF60A5FA),
            "Tin nhắn"
        )
        NotificationType.SYSTEM -> NotificationTypeStyle(
            Icons.Outlined.Notifications,
            Color(0xFFA78BFA),
            "Hệ thống"
        )
    }
}

internal fun relativeTime(timestamp: Long): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val minutes = diffMs / 60000L
    val hours = diffMs / 3600000L
    val days = diffMs / 86400000L
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "Vừa xong"
    }
}

internal fun formatNotificationTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi", "VN"))
    return formatter.format(Date(timestamp))
}

@Composable
internal fun NotificationStatusPill(
    read: Boolean,
    modifier: Modifier = Modifier
) {
    val label = if (read) "Đã đọc" else "Chưa đọc"
    val accent = if (read) Color.White else Color(0xFF22D3EE)
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
internal fun NotificationTypePill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
