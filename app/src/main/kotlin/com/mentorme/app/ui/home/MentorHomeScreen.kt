package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

// ===== Data Models cho Mentor =====
data class MentorStats(
    val totalSessions: Int,
    val totalStudents: Int,
    val averageRating: Double,
    val totalEarnings: Int
)

data class UpcomingSession(
    val id: String,
    val studentName: String,
    val topic: String,
    val dateTime: String,
    val duration: Int,
    val type: String
)

data class RecentReview(
    val studentName: String,
    val rating: Double,
    val comment: String,
    val date: String
)

// ===== Sample Data cho Mentor =====
private val mentorStats = MentorStats(
    totalSessions = 127,
    totalStudents = 89,
    averageRating = 4.8,
    totalEarnings = 25600000
)

private val upcomingSessions = listOf(
    UpcomingSession(
        id = "1",
        studentName = "Nguyễn Văn A",
        topic = "Career Development",
        dateTime = "Hôm nay, 14:00",
        duration = 60,
        type = "Video Call"
    ),
    UpcomingSession(
        id = "2",
        studentName = "Trần Thị B",
        topic = "Technical Interview",
        dateTime = "Mai, 10:00",
        duration = 90,
        type = "Video Call"
    ),
    UpcomingSession(
        id = "3",
        studentName = "Lê Văn C",
        topic = "Resume Review",
        dateTime = "T7, 16:00",
        duration = 45,
        type = "Chat"
    )
)

private val recentReviews = listOf(
    RecentReview(
        studentName = "Phạm Minh D",
        rating = 5.0,
        comment = "Mentor rất tận tâm và chuyên nghiệp. Tôi đã học được rất nhiều!",
        date = "2 ngày trước"
    ),
    RecentReview(
        studentName = "Hoàng Thị E",
        rating = 4.8,
        comment = "Buổi tư vấn rất bổ ích, giúp tôi định hướng rõ ràng hơn.",
        date = "1 tuần trước"
    )
)

private data class QuickAction(val title: String, val subtitle: String, val icon: @Composable () -> Unit)
private val quickActions = listOf(
    QuickAction("Lịch hẹn", "Quản lý buổi tư vấn") { Icon(Icons.Default.CalendarToday, null, tint = Color.White) },
    QuickAction("Học viên", "Xem danh sách mentee") { Icon(Icons.Default.People, null, tint = Color.White) },
    QuickAction("Thu nhập", "Theo dõi doanh thu") { Icon(Icons.Default.AttachMoney, null, tint = Color.White) },
    QuickAction("Đánh giá", "Xem feedback từ học viên") { Icon(Icons.Default.Star, null, tint = Color.White) },
)

@Composable
fun MentorHomeScreen(
    onViewSchedule: () -> Unit = {},
    onViewStudents: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onJoinSession: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onUpdateProfile: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = 75.dp
        )
    ) {
        item { MentorHeroSection() }

        item { SectionTitle("Thống kê của bạn") }
        item { MentorStatsSection(mentorStats) }

        item { SectionTitle("Thao tác nhanh") }
        items(quickActions.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { action ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        radius = 22.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .liquidGlass(radius = 12.dp),
                                contentAlignment = Alignment.Center
                            ) { action.icon() }

                            Column {
                                Text(
                                    action.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    action.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            SectionHeader(
                title = "Buổi tư vấn sắp tới",
                subtitle = "Các buổi hẹn trong tuần này"
            )
        }
        items(upcomingSessions) { session ->
            UpcomingSessionCard(
                session = session,
                onJoinSession = { onJoinSession(session.id) }
            )
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MMButton(
                    text = "Xem tất cả lịch hẹn",
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    onClick = onViewAllSessions
                )
            }
        }

        item { SectionTitle("Đánh giá gần đây") }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(recentReviews) { review ->
                    RecentReviewCard(review = review)
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun MentorHeroSection() {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        radius = 28.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF667eea).copy(0.3f),
                            Color(0xFF764ba2).copy(0.3f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Chào mừng trở lại! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Hôm nay bạn có ${upcomingSessions.size} buổi tư vấn",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(0.9f)
                )
                Text(
                    "Tiếp tục truyền cảm hứng và hỗ trợ học viên của bạn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.8f)
                )
            }
        }
    }
}

@Composable
private fun MentorStatsSection(stats: MentorStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "${stats.totalSessions}",
            subtitle = "Buổi tư vấn",
            icon = { Icon(Icons.Default.Event, null, tint = Color.White) }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "${stats.totalStudents}",
            subtitle = "Học viên",
            icon = { Icon(Icons.Default.People, null, tint = Color.White) }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "${stats.averageRating}⭐",
            subtitle = "Đánh giá TB",
            icon = { Icon(Icons.Default.Star, null, tint = Color.White) }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier.height(90.dp),
        radius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .liquidGlass(radius = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(16.dp)) { icon() }
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.8f)
                )
            }
        }
    }
}

@Composable
private fun UpcomingSessionCard(
    session: UpcomingSession,
    onJoinSession: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        session.topic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.9f)
                    )
                    Text(
                        "${session.dateTime} • ${session.duration} phút",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.7f)
                    )
                }
                MMPrimaryButton(
                    onClick = onJoinSession
                ) {
                    Text("Tham gia")
                }
            }
        }
    }
}

@Composable
private fun RecentReviewCard(review: RecentReview) {
    LiquidGlassCard(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        radius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        review.studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "⭐ ${review.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.9f)
                    )
                }
                Text(
                    review.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.8f),
                    maxLines = 2
                )
            }
            Text(
                review.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.6f)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(0.8f)
        )
    }
}
