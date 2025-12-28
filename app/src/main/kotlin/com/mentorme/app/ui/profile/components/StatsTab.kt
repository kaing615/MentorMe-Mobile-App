package com.mentorme.app.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.profile.UserProfile
import com.mentorme.app.ui.profile.UserRole
import com.mentorme.app.ui.profile.formatMoneyShortVnd
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun StatsTab(userRole: UserRole, profile: UserProfile) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = if (userRole == UserRole.MENTOR) "Phiên đã dạy" else "Buổi tư vấn",
                value = profile.totalSessions.toString(),
                icon = Icons.Outlined.MenuBook,
                modifier = Modifier.weight(1f)
            )
            val moneyText = if (userRole == UserRole.MENTOR) {
                formatMoneyShortVnd(45_600_000L, withCurrency = true)
            } else {
                formatMoneyShortVnd(profile.totalSpent, withCurrency = true)
            }

            StatCard(
                title = if (userRole == UserRole.MENTOR) "Thu nhập" else "Chi tiêu",
                value = moneyText,
                icon = Icons.Outlined.CreditCard,
                modifier = Modifier.weight(1f)
            )
        }

        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thành tựu", style = MaterialTheme.typography.titleMedium)
                }

                if (userRole == UserRole.MENTOR) {
                    AchievementItem(
                        iconBg = Color(0x33FFD54F),
                        icon = Icons.Outlined.Star,
                        title = "Mentor xuất sắc",
                        subtitle = "Rating 4.9⭐ từ 100+ mentees"
                    )
                    AchievementItem(
                        iconBg = Color(0x334CAF50),
                        icon = Icons.Outlined.MenuBook,
                        title = "Mentor chuyên nghiệp",
                        subtitle = "150+ phiên tư vấn thành công"
                    )
                    AchievementItem(
                        iconBg = Color(0x338E24AA),
                        icon = Icons.Outlined.EmojiEvents,
                        title = "Top Mentor",
                        subtitle = "Top 5% mentor được yêu thích nhất"
                    )
                } else {
                    AchievementItem(
                        iconBg = Color(0x33FFD54F),
                        icon = Icons.Outlined.Star,
                        title = "Học viên tích cực",
                        subtitle = "Hoàn thành 10+ buổi tư vấn"
                    )
                    AchievementItem(
                        iconBg = Color(0x333F51B5),
                        icon = Icons.Outlined.MenuBook,
                        title = "Người học chuyên cần",
                        subtitle = "3 tháng liên tiếp có session"
                    )
                }
            }
        }

        // Spacer để nội dung không bị bottom bar đè khi scroll xuống
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier.height(120.dp), radius = 22.dp) {
        Row(
            Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = .8f)
                )
            }
        }
    }
}

@Composable
private fun AchievementItem(
    iconBg: Color,
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val grad = androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(iconBg.copy(alpha = .9f), Color.White.copy(alpha = .12f))
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(grad),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White) }

        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .75f))
        }
    }
}
