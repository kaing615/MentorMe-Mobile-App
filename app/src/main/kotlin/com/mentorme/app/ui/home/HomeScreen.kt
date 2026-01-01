package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.ui.components.ui.GlassOverlay
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.session.ActiveSessionBanner
import com.mentorme.app.ui.search.components.BookSessionContent
import com.mentorme.app.ui.search.components.MentorDetailSheet
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

// ===== Data Models for static content =====

data class SuccessStory(
    val title: String,
    val quote: String,
    val author: String,
    val icon: ImageVector,
    val bgGradient: Brush
)

// ===== Sample Data =====
private data class QuickStat(val title: String, val subtitle: String, val icon: @Composable () -> Unit)

private data class Category(val name: String, val icon: ImageVector)
private val categories = listOf(
    Category("Technology", CategoryIcons.getIcon("technology")),
    Category("Business", CategoryIcons.getIcon("business")),
    Category("Design", CategoryIcons.getIcon("design")),
    Category("Marketing", CategoryIcons.getIcon("marketing")),
    Category("Finance", CategoryIcons.getIcon("finance")),
    Category("Career", CategoryIcons.getIcon("career")),
)

private data class MentorUi(val name: String, val role: String, val rating: Double)

// Sample top mentors for fallback only
private val sampleTopMentors = listOf(
    MentorUi("Nguyễn An", "Software Engineer", 4.9),
    MentorUi("Trần Minh", "Product Manager", 4.8),
    MentorUi("Lê Lan",    "UX Designer", 4.9),
    MentorUi("Hoàng Quân","Data Scientist", 4.7)
)


val successStories = listOf(
    SuccessStory(
        title = "Từ Junior lên Senior trong 18 tháng",
        quote = "Nhờ mentor, tôi đã nắm vững kiến thức và kỹ năng cần thiết để thăng tiến nhanh chóng",
        author = "Phạm Minh Tuấn",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        bgGradient = Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))
    ),
    SuccessStory(
        title = "Chuyển ngành thành công",
        quote = "Với sự hướng dẫn tận tình, tôi đã chuyển từ Marketing sang Tech một cách suôn sẻ",
        author = "Nguyễn Thu Hà",
        icon = Icons.Default.WorkspacePremium,
        bgGradient = Brush.linearGradient(listOf(Color(0xFFf093fb), Color(0xFFf5576c)))
    )
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSearch: (String) -> Unit = {},
    onNavigateToMentors: () -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {}, // ✅ NEW: Navigate with category filter
    onJoinSession: (String) -> Unit = {},
    onMessage: (String) -> Unit = {}, //  NEW: Callback to open chat with mentorId
    onBookSlot: (
        mentor: Mentor,
        occurrenceId: String,
        date: String,
        startTime: String,
        endTime: String,
        priceVnd: Long,
        note: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onOverlayOpened: () -> Unit = {},
    onOverlayClosed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // ===== Bottom sheet state (same as SearchScreen) =====
    var showDetail by rememberSaveable { mutableStateOf(false) }
    var showBooking by rememberSaveable { mutableStateOf(false) }
    var selectedMentor by remember { mutableStateOf<Mentor?>(null) }

    val blurOn = showDetail || showBooking
    val blurRadius = if (blurOn) 8.dp else 0.dp

    // Notify parent when overlay state changes
    androidx.compose.runtime.LaunchedEffect(blurOn) {
        if (blurOn) onOverlayOpened() else onOverlayClosed()
    }

    // Create dynamic stats from uiState
    val quickStats = remember(uiState.mentorCount, uiState.sessionCount, uiState.avgRating) {
        listOf(
            QuickStat(
                formatCompactNumber(uiState.mentorCount),
                "Mentor chất lượng"
            ) { Icon(Icons.Filled.Star, null, tint = Color.White) },
            QuickStat(
                formatCompactNumber(uiState.sessionCount),
                "Buổi tư vấn"
            ) { Icon(Icons.Filled.CalendarMonth, null, tint = Color.White) },
            QuickStat(
                if (uiState.avgRating > 0) "%.1f★".format(uiState.avgRating) else "0★",
                "Đánh giá trung bình"
            ) { Icon(Icons.Filled.ThumbUp, null, tint = Color.White) },
            QuickStat(
                "< 2h",
                "Phản hồi nhanh"
            ) { Icon(Icons.Filled.Timelapse, null, tint = Color.White) },
        )
    }

    Box(Modifier.fillMaxSize()) {
        // LAYER A: Main content (blur when modal shown)
        Box(
            Modifier
                .fillMaxSize()
                .blur(blurRadius)
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
            bottom = 110.dp
        )
    ) {
        // Welcome Section - "Xin chào, [Tên]"
        item {
            MenteeWelcomeSection(menteeName = uiState.userName)
        }

        // Show upcoming sessions OR HeroSection (not both)
        if (uiState.upcomingSessions.isNotEmpty()) {
            // Show upcoming sessions cards
            item {
                MenteeUpcomingSessionsSection(
                    sessions = uiState.upcomingSessions,
                    onJoinSession = onJoinSession,
                    onViewCalendar = { /* Navigate to calendar */ }
                )
            }
        } else {
            // Show HeroSection when no upcoming sessions
            item {
                HeroSection(
                    onSearch = onSearch,
                    onlineCount = uiState.onlineCount,
                    avgRating = uiState.avgRating
                )
            }
        }

        // Show loading state
        if (uiState.isLoading && !uiState.isRefreshing) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Show error state
        if (uiState.errorMessage != null && !uiState.isLoading) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    radius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Đã có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        MMButton(
                            text = "Thử lại",
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                            onClick = { viewModel.refresh() }
                        )
                    }
                }
            }
        }

        // Show content when loaded
        if (!uiState.isLoading || uiState.isRefreshing) {

        // Active Session Banner - hiển thị khi có mentee đang đợi
        uiState.waitingSession?.let { session ->
            item {
                ActiveSessionBanner(
                    bookingId = session.bookingId,
                    menteeName = session.menteeName,
                    onJoinSession = onJoinSession,
                    onDismiss = { viewModel.dismissWaitingSession() }
                )
            }
        }

        item { SectionTitle("Thống kê nhanh") }
        items(quickStats.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { stat ->
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
                            ) { stat.icon() }

                            Column {
                                Text(
                                    stat.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    stat.subtitle,
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

        item { SectionTitle("Khám phá theo lĩnh vực") }
        items(categories.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { cat ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .noIndicationClickable {
                                // ✅ Navigate to search with this category as filter
                                onNavigateToCategory(cat.name)
                            },
                        radius = 22.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .liquidGlass(radius = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Text(
                                cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            SectionHeader(
                title = "Mentor nổi bật",
                subtitle = "Kết nối với những mentor hàng đầu"
            )
        }
        items(uiState.featuredMentors) { mentor ->
            HomeMentorCard(
                mentor = mentor,
                onViewProfile = {
                    // ✅ Trigger GlassOverlay with MentorDetailSheet
                    selectedMentor = mentor
                    showDetail = true
                },
                onBookSession = {
                    // ✅ Trigger GlassOverlay with BookSessionContent
                    selectedMentor = mentor
                    showBooking = true
                }
            )
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MMButton(
                    text = "Xem tất cả mentor",
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    onClick = onNavigateToMentors
                )
            }
        }
        item { SectionTitle("Mentor được đánh giá cao") }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(uiState.topMentors) { m ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .width(240.dp)
                            .height(140.dp),
                        radius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    m.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    m.role,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .liquidGlass(radius = 14.dp)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text("⭐ ${m.rating}", color = Color.White) }

                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .liquidGlass(radius = 14.dp)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text("Đặt lịch", color = Color.White) }
                            }
                        }
                    }
                }
            }
        }

        // Success Stories
        item {
            SectionHeader(
                title = "Câu chuyện thành công",
                subtitle = "Những thành tựu từ học viên của chúng tôi"
            )
        }
        items(successStories) { story ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 22.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(story.bgGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(story.icon, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(story.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(story.quote, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        Text("- ${story.author}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }

        } // End of content loading condition

        item { Spacer(Modifier.height(8.dp)) }
    } // End LazyColumn
    } // End blur Box

    // LAYER B: Glass overlay with bottom sheets (same as SearchScreen)
    GlassOverlay(
        visible = blurOn,
        onDismiss = { showDetail = false; showBooking = false },
        formModifier = Modifier.fillMaxSize().padding(12.dp)
    ) {
        selectedMentor?.let { mentor ->
            when {
                showDetail -> {
                    MentorDetailSheet(
                        mentorId = mentor.id,
                        mentor = mentor,
                        onClose = { showDetail = false },
                        onBookNow = { _ ->
                            showDetail = false
                            showBooking = true
                        },
                        onMessage = { mentorId ->
                            //  Close sheet first
                            showDetail = false
                            //  Trigger parent callback
                            onMessage(mentorId)
                        }
                    )
                }
                showBooking -> {
                    BookSessionContent(
                        mentor = mentor,
                        onClose = { showBooking = false },
                        onConfirm = { occurrenceId, date, startTime, endTime, priceVnd, note ->
                            showBooking = false
                            onBookSlot(
                                mentor,
                                occurrenceId,
                                date,
                                startTime,
                                endTime,
                                priceVnd,
                                note
                            )
                        }
                    )
                }
            }
        }
    }
    } // End outer Box
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
private fun MenteeWelcomeSection(menteeName: String) {
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
                text = menteeName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Swipeable upcoming sessions for mentee
 */
@Composable
private fun MenteeUpcomingSessionsSection(
    sessions: List<MenteeUpcomingSessionUi>,
    onJoinSession: (String) -> Unit,
    onViewCalendar: () -> Unit
) {
    if (sessions.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { sessions.size })

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Lịch hẹn sắp tới")
            if (sessions.size > 1) {
                Text(
                    "${sessions.size} phiên",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.6f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { page ->
            val session = sessions[page]
            MenteeSessionCard(
                session = session,
                onJoin = { onJoinSession(session.id) },
                onViewCalendar = onViewCalendar
            )
        }

        // Page indicator dots
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
                                if (isSelected) Color(0xFF60A5FA)
                                else Color.White.copy(0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MenteeSessionCard(
    session: MenteeUpcomingSessionUi,
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
                            .background(Color(0xFF60A5FA).copy(0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Videocam, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (session.isStartingSoon) "Đang diễn ra" else "Sắp tới",
                        color = if (session.isStartingSoon) Color(0xFF60A5FA) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(session.time, color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
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
                            contentDescription = "Avatar of ${session.mentorName}",
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
                        session.mentorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        session.topic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.7f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                com.mentorme.app.ui.components.ui.MMGhostButton(
                    onClick = onViewCalendar,
                    modifier = Modifier.weight(1f),
                    content = { Text("Xem lịch") }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}


// ===== HOME MENTOR CARD - Premium Glassmorphism (Private component) =====
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeMentorCard(
    mentor: Mentor,
    onViewProfile: () -> Unit,
    onBookSession: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 22.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Row 1: Avatar + Name + Title + Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                val avatarUrl = mentor.imageUrl.trim()
                val initials = mentor.name
                    .trim()
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull() }
                    .take(2)
                    .joinToString("")

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Name + Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mentor.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = mentor.role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    if (mentor.company.isNotBlank()) {
                        Text(
                            text = mentor.company,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Rating pill (top-right)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${mentor.rating}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Row 2: Skills (FlowRow with glass chips)
            if (mentor.skills.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mentor.skills.take(4).forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    if (mentor.skills.size > 4) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+${mentor.skills.size - 4}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            // Row 3: Action Buttons (✅ CRITICAL - Matching MentorDetailSheet style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 2: View Profile (Ghost style)
                Surface(
                    onClick = onViewProfile,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            "Xem hồ sơ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Button 1: Book Session (✅ Vibrant Blue #2563EB)
                if (mentor.isAvailable) {
                    Button(
                        onClick = onBookSession,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB), // ✅ Vibrant Blue
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Đặt lịch",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ===== Helper function for clickable without ripple effect =====
private fun Modifier.noIndicationClickable(onClick: () -> Unit) = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}

