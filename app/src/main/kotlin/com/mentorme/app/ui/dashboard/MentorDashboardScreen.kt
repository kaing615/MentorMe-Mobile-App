package com.mentorme.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

private data class DashboardStat(
    val title: String,
    val value: String,
    val icon: @Composable () -> Unit,
    val trend: String? = null
)

private data class UpcomingSession(
    val id: String,
    val menteeName: String,
    val topic: String,
    val time: String,
    val avatarInitial: String,
    val isStartingSoon: Boolean = false
)

private data class RecentReview(
    val menteeName: String,
    val rating: Double,
    val comment: String,
    val date: String
)

private val mentorStats = listOf(
    DashboardStat("Thu nhập", "15.6M ₫", { Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF34D399)) }, "+8%"),
    DashboardStat("Học viên", "128", { Icon(Icons.Default.Groups, null, tint = Color(0xFF60A5FA)) }, "+12"),
    DashboardStat("Đánh giá", "4.9 ⭐", { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700)) }),
    DashboardStat("Giờ dạy", "45h", { Icon(Icons.Default.AccessTime, null, tint = Color.White) })
)

private val nextSession = UpcomingSession(
    id = "s1",
    menteeName = "Phạm Tuấn Anh",
    topic = "Mock Interview: System Design",
    time = "10:00 - 11:00 hôm nay",
    avatarInitial = "P",
    isStartingSoon = true
)

private val recentReviews = listOf(
    RecentReview("Lê Thu Hà", 5.0, "Mentor cực kỳ nhiệt tình, giải thích dễ hiểu!", "Hôm qua"),
    RecentReview("Trần Văn B", 4.8, "Kiến thức sâu rộng, tuy nhiên hơi nhanh một chút.", "2 ngày trước"),
    RecentReview("Nguyễn C", 5.0, "Rất đáng tiền, cảm ơn anh đã định hướng career path.", "1 tuần trước")
)

@Composable
fun MentorDashboardScreen(
    vm: com.mentorme.app.ui.profile.ProfileViewModel,
    onViewSchedule: () -> Unit = {},
    onViewStudents: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onJoinSession: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onUpdateProfile: () -> Unit = {},

    // ✅ NEW: mở thẳng tab "Cài đặt" trong profile
    onOpenSettings: () -> Unit = {},

    modifier: Modifier = Modifier
) {
    // Collect profile data from ViewModel
    val state by vm.state.collectAsState()
    val profile = state.profile
    val mentorName = profile?.fullName ?: "Mentor"
    
    Box(modifier = modifier.fillMaxSize()) {
        LiquidBackground(Modifier.matchParentSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
        ) {
            item { MentorWelcomeSection(mentorName = mentorName) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("Lịch hẹn sắp tới")
                    NextSessionCard(
                        session = nextSession,
                        onJoin = { onJoinSession(nextSession.id) },
                        onViewCalendar = onViewSchedule
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("Tổng quan tháng này")

                    mentorStats.chunked(2).forEachIndexed { index, row ->
                        if (index > 0) Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { stat ->
                                StatCardItem(
                                    stat = stat,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (stat.title) {
                                            "Thu nhập" -> onViewEarnings()
                                            "Học viên" -> onViewStudents()
                                            "Đánh giá" -> onViewReviews()
                                            else -> onViewAllSessions()
                                        }
                                    }
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("Thao tác nhanh")
                    LiquidGlassCard(radius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionItem(Icons.Default.EditCalendar, "Sửa lịch", onViewSchedule)
                            QuickActionItem(Icons.Default.Person, "Hồ sơ", onUpdateProfile)
                            QuickActionItem(Icons.Default.AccountBalanceWallet, "Rút tiền", onViewEarnings)

                            // ✅ đổi sang onOpenSettings để mở đúng tab “Cài đặt”
                            QuickActionItem(Icons.Default.Settings, "Cài đặt", onOpenSettings)
                        }
                    }
                }
            }

            item {
                // ✅ Gap giữa header và card bị rộng chủ yếu do TextButton default minHeight=48dp
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Đánh giá mới nhất")

                        // ✅ giảm height/padding của TextButton để Row không bị cao
                        TextButton(
                            onClick = onViewReviews,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Xem tất cả", color = Color.White.copy(0.7f))
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(recentReviews) { review -> ReviewCard(review) }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MentorWelcomeSection(mentorName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Xin chào,",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = mentorName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .liquidGlass()
                .background(Color(0xFF22C55E).copy(0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(Modifier.width(6.dp))
                Text("Online", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun NextSessionCard(
    session: UpcomingSession,
    onJoin: () -> Unit,
    onViewCalendar: () -> Unit
) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 24.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF472B6).copy(0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Videocam, null, tint = Color(0xFFF472B6), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (session.isStartingSoon) "Đang diễn ra" else "Sắp tới",
                        color = if (session.isStartingSoon) Color(0xFFF472B6) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(session.time, color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        session.avatarInitial,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.menteeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        session.topic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MMButton(
                    text = "Vào cuộc",
                    onClick = onJoin,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.VideoCall, null, Modifier.size(20.dp)) }
                )
                MMGhostButton(
                    onClick = onViewCalendar,
                    modifier = Modifier.weight(1f),
                    content = { Text("Xem chi tiết") }
                )
            }
        }
    }
}

@Composable
private fun StatCardItem(
    stat: DashboardStat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(modifier = modifier.height(120.dp), radius = 20.dp) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) { stat.icon() }

                    if (stat.trend != null) {
                        Text(
                            text = stat.trend,
                            color = Color(0xFF34D399),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stat.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.08f))
                .border(1.dp, Color.White.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.9f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewCard(review: RecentReview) {
    val fullStars = review.rating.toInt().coerceIn(0, 5)

    LiquidGlassCard(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        radius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = review.menteeName, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = review.date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            }

            Text(
                text = "\"${review.comment}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontStyle = FontStyle.Italic
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    val tint = if (index < fullStars) Color(0xFFFFD700) else Color.Gray.copy(0.5f)
                    Icon(Icons.Default.Star, null, tint = tint, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(text = review.rating.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 4.dp, bottom = 0.dp)
    )
}
