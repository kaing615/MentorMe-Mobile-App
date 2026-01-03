package com.mentorme.app.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.NotificationPreferences
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun SettingsTab(
    onOpenSettings: (() -> Unit)?,
    onOpenNotifications: (() -> Unit)? = null,
    notificationPreferences: NotificationPreferences,
    onUpdateNotificationPreferences: (NotificationPreferences) -> Unit,
    onOpenCSBM: (() -> Unit)? = null,
    onOpenDKSD: (() -> Unit)? = null,
    onOpenLHHT: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    var emailEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }
    val allPushEnabled = notificationPreferences.pushBooking &&
        notificationPreferences.pushPayment &&
        notificationPreferences.pushMessage &&
        notificationPreferences.pushSystem

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding() // ✅ Handle system navigation bar
            .padding(bottom = 110.dp), // Padding AFTER verticalScroll
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thông báo", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                SettingSwitchRow(
                    "Email thông báo",
                    "Nhận email về booking và tin nhắn",
                    checked = emailEnabled,
                    onCheckedChange = { emailEnabled = it }
                )
                SettingSwitchRow(
                    "Thông báo push",
                    "Nhận thông báo trên thiết bị",
                    checked = allPushEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateNotificationPreferences(
                            notificationPreferences.copy(
                                pushBooking = enabled,
                                pushPayment = enabled,
                                pushMessage = enabled,
                                pushSystem = enabled
                            )
                        )
                    }
                )
                Text(
                    "Tùy chọn thông báo push",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                SettingSwitchRow(
                    "Booking",
                    "Xác nhận, nhắc lịch, hủy",
                    checked = notificationPreferences.pushBooking,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushBooking = it))
                    }
                )
                SettingSwitchRow(
                    "Thanh toán",
                    "Thành công hoặc thất bại",
                    checked = notificationPreferences.pushPayment,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushPayment = it))
                    }
                )
                SettingSwitchRow(
                    "Tin nhắn",
                    "Thông báo chat mới",
                    checked = notificationPreferences.pushMessage,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushMessage = it))
                    }
                )
                SettingSwitchRow(
                    "Hệ thống",
                    "Cập nhật chung",
                    checked = notificationPreferences.pushSystem,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushSystem = it))
                    }
                )
                SettingSwitchRow(
                    "Tin nhắn marketing",
                    "Nhận thông tin khuyến mãi",
                    checked = marketingEnabled,
                    onCheckedChange = { marketingEnabled = it }
                )
                SettingLinkItem(Icons.Outlined.Notifications, "Xem thông báo") {
                    onOpenNotifications?.invoke()
                }
            }
        }

        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Security, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Bảo mật", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                MMPrimaryButton(onClick = { onOpenSettings?.invoke() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Settings, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Cài đặt nâng cao")
                }
            }
        }

        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Khác", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                SettingLinkItem(Icons.Outlined.PrivacyTip, "Chính sách bảo mật") {
                    onOpenCSBM?.invoke()
                }
                SettingLinkItem(Icons.AutoMirrored.Outlined.Article, "Điều khoản sử dụng") {
                    onOpenDKSD?.invoke()
                }
                SettingLinkItem(Icons.Outlined.SupportAgent, "Liên hệ hỗ trợ") {
                    onOpenLHHT?.invoke()
                }
            }
        }

        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tài khoản",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                MMPrimaryButton(
                    onClick = { onLogout?.invoke() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất")
                }
            }
        }
    }
}

@Composable
internal fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .7f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.White.copy(alpha = .35f),
                uncheckedThumbColor = Color.White.copy(alpha = .7f),
                uncheckedTrackColor = Color.White.copy(alpha = .2f)
            )
        )
    }
}

@Composable
private fun SettingLinkItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    MMGhostButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(label, color = Color.White, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
