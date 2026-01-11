package com.mentorme.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.session.OngoingSessionDialog
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import java.text.NumberFormat
import java.util.Locale
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId

private data class DashboardStat(
    val title: String,
    val value: String,
    val icon: @Composable () -> Unit,
    val trend: String? = null
)

@Composable
fun MentorDashboardScreen(
    vm: com.mentorme.app.ui.profile.ProfileViewModel,
    dashboardVm: MentorDashboardViewModel = hiltViewModel(),
    onViewSchedule: () -> Unit = {},
    onViewStudents: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onJoinSession: (String) -> Unit = {},
    onViewBookingDetail: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {},
    onUpdateProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Collect profile data from ViewModel
    val state by vm.state.collectAsState()
    val profile = state.profile
    val mentorName = profile?.fullName ?: "Mentor"
    
    // ✅ FIX: Refresh dashboard when userId changes (detect account switch)
    val userId = profile?.id
    LaunchedEffect(userId) {
        if (userId != null) {
            android.util.Log.d("MentorDashboard", "🔄 userId changed to: $userId, refreshing dashboard")
            dashboardVm.refresh()
        }
    }

    // Collect dashboard data
    val dashboardState by dashboardVm.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LiquidBackground(Modifier.matchParentSize())

        when (val dState = dashboardState) {
            is DashboardUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is DashboardUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Lỗi tải dữ liệu", color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { dashboardVm.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    mentorName = mentorName,
                    upcomingSessions = dState.upcomingSessions,
                    stats = dState.stats,
                    recentReviews = dState.recentReviews,
                    onViewSchedule = onViewSchedule,
                    onViewStudents = onViewStudents,
                    onViewEarnings = onViewEarnings,
                    onViewReviews = onViewReviews,
                    onJoinSession = onJoinSession,
                    onViewBookingDetail = onViewBookingDetail,
                    onViewAllSessions = onViewAllSessions,
                    onUpdateProfile = onUpdateProfile,
                    onOpenSettings = onOpenSettings
                )
            }
        }

    }
}

@Composable
private fun DashboardContent(
    mentorName: String,
    upcomingSessions: List<UpcomingSessionUi>,
    stats: com.mentorme.app.data.dto.mentor.MentorStatsDto,
    recentReviews: List<com.mentorme.app.data.dto.review.ReviewDto>,
    onViewSchedule: () -> Unit,
    onViewStudents: () -> Unit,
    onViewEarnings: () -> Unit,
    onViewReviews: () -> Unit,
    onJoinSession: (String) -> Unit,
    onViewBookingDetail: (String) -> Unit,
    onViewAllSessions: () -> Unit,
    onUpdateProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Format stats for display
    val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val mentorStats = listOf(
        DashboardStat(
            "Thu nhập",
            nf.format(stats.earnings),
            { Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF34D399)) }
        ),
        DashboardStat(
            "Mentee",
            "${stats.menteeCount}",
            { Icon(Icons.Default.Groups, null, tint = Color(0xFF60A5FA)) }
        ),
        DashboardStat(
            "Đánh giá",
            if (stats.averageRating > 0) {
                "${String.format(Locale.US, "%.1f", stats.averageRating)} ⭐"
            } else {
                "Chưa có"
            },
            { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700)) }
        ),
        DashboardStat(
            "Giờ tư vấn",
            "${String.format(Locale.US, "%.1f", stats.totalHours)}h",
            { Icon(Icons.Default.AccessTime, null, tint = Color.White) }
        )
    )

    var dismissedOngoingSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    val ongoingSession = upcomingSessions.firstOrNull { it.isOngoing && it.canJoin }

    Box(modifier = Modifier.fillMaxSize()) {

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Lịch hẹn sắp tới")
                        if (upcomingSessions.size > 1) {
                            Text(
                                "${upcomingSessions.size} phiên",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.6f)
                            )
                        }
                    }
                    if (upcomingSessions.isNotEmpty()) {
                        SwipeableSessionCards(
                            sessions = upcomingSessions,
                            onJoinSession = onJoinSession,
                            onViewDetail = onViewBookingDetail,
                            onViewCalendar = onViewSchedule
                        )
                    } else {
                        EmptySessionCard(onViewCalendar = onViewSchedule)
                    }
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
                                            "Mentee" -> onViewStudents()
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

            if (recentReviews.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle("Đánh giá mới nhất")
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
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (ongoingSession != null && ongoingSession.id != dismissedOngoingSessionId) {
            OngoingSessionDialog(
                title = "Phiên tư vấn đang diễn ra",
                description = "Bạn đang có phiên tư vấn với ${ongoingSession.menteeName}. Hãy vào phòng để bắt đầu.",
                timeLabel = ongoingSession.time,
                onJoin = {
                    dismissedOngoingSessionId = ongoingSession.id
                    onJoinSession(ongoingSession.id)
                },
                onDismiss = { dismissedOngoingSessionId = ongoingSession.id }
            )
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

/**
 * Swipeable session cards using HorizontalPager
 */
@Composable
private fun SwipeableSessionCards(
    sessions: List<UpcomingSessionUi>,
    onJoinSession: (String) -> Unit,
    onViewDetail: (String) -> Unit,
    onViewCalendar: () -> Unit
) {
    if (sessions.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { sessions.size })
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { page ->
            val session = sessions[page]
            NextSessionCard(
                session = session,
                onJoin = { onJoinSession(session.id) },
                onViewDetail = { onViewDetail(session.id) },
                onViewCalendar = onViewCalendar
            )
        }
        
        // Page indicator dots (only show if more than 1 session)
        if (sessions.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sessions.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFF472B6)
                                else Color.White.copy(0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun NextSessionCard(
    session: UpcomingSessionUi,
    onJoin: () -> Unit,
    onViewDetail: () -> Unit,
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
                    // ✅ FIX: Show correct status text
                    Text(
                        text = when {
                            session.isOngoing -> "Đang diễn ra"
                            session.isStartingSoon -> "Sắp bắt đầu"
                            else -> "Sắp tới"
                        },
                        color = if (session.isOngoing || session.isStartingSoon) Color(0xFFF472B6) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(session.time, color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with image or fallback to initials
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!session.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = session.avatarUrl,
                            contentDescription = "Avatar of ${session.menteeName}",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            session.avatarInitial,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                    enabled = session.canJoin,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.VideoCall, null, Modifier.size(20.dp)) }
                )
                MMGhostButton(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    content = { Text("Xem chi tiết") }
                )
            }
        }
    }
}

@Composable
private fun EmptySessionCard(onViewCalendar: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 24.dp) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CalendarToday,
                null,
                tint = Color.White.copy(0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Chưa có lịch hẹn",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bạn chưa có lịch hẹn nào sắp tới",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.5f)
            )
            Spacer(Modifier.height(16.dp))
            MMGhostButton(
                onClick = onViewCalendar,
                content = { Text("Xem lịch") }
            )
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
private fun ReviewCard(review: com.mentorme.app.data.dto.review.ReviewDto) {
    val fullStars = review.rating.coerceIn(0, 5)

    // Format date from ISO string
    val dateDisplay = try {
        val instant = Instant.parse(review.createdAt)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        formatter.format(instant.atZone(ZoneId.systemDefault()))
    } catch (_: Exception) {
        review.createdAt.take(10) // Fallback to first 10 chars
    }

    // Get mentee name from ReviewUserDto
    val menteeName = review.mentee.name ?: review.mentee.userName

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
                Text(text = menteeName, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = dateDisplay, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            }

            Text(
                text = "\"${review.comment ?: "Không có nhận xét"}\"",
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
