package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private data class QuickStat(val title: String, val subtitle: String)

private data class Category(val name: String, val icon: ImageVector)
private val categories = listOf(
    Category("Technology", CategoryIcons.getIcon("technology")),
    Category("Business", CategoryIcons.getIcon("business")),
    Category("Design", CategoryIcons.getIcon("design")),
    Category("Marketing", CategoryIcons.getIcon("marketing")),
    Category("Finance", CategoryIcons.getIcon("finance")),
    Category("Career", CategoryIcons.getIcon("career")),
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
    onNavigateToSchedule: () -> Unit = {}, // ✅ NEW: Navigate to schedule/calendar
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
            ),
            QuickStat(
                formatCompactNumber(uiState.sessionCount),
                "Buổi tư vấn"
            ),
            QuickStat(
                if (uiState.avgRating > 0) "%.1f★".format(uiState.avgRating) else "0★",
                "Đánh giá trung bình"
            ),
            QuickStat(
                "< 2h",
                "Phản hồi nhanh"
            )
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
        verticalArrangement = Arrangement.spacedBy(20.dp), // Increased from 12dp
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 110.dp
        )
    ) {
        // Welcome Section - "Xin chào, [Tên]"
        item {
            MenteeWelcomeSection(menteeName = uiState.userName)
        }

        // Show upcoming sessions OR HeroSection
        if (uiState.upcomingSessions.isNotEmpty()) {
            item {
                MenteeUpcomingSessionsSection(
                    sessions = uiState.upcomingSessions,
                    onJoinSession = onJoinSession,
                    onViewCalendar = onNavigateToSchedule
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

        // Calculate chunked list ONCE to get proper indices
        val chunkedStats = quickStats.chunked(2)

        items(chunkedStats) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Get row index to calculate proper statIndex
                val rowIndex = chunkedStats.indexOf(row)

                row.forEachIndexed { index, stat ->
                    val statIndex = rowIndex * 2 + index

                    // UNIQUE colors for each stat - all different
                    val iconColor = when (statIndex) {
                        0 -> Color(0xFFFFAA00) // Bright Orange/Amber for Mentors (People icon)
                        1 -> Color(0xFF00E5FF) // Electric Cyan for Sessions (Calendar icon)
                        2 -> Color(0xFFFFD700) // Bright Gold/Yellow for Rating (Star icon)
                        else -> Color(0xFF00FF88) // Bright Neon Green for Response (Message icon)
                    }

                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(145.dp) // ✅ Tăng từ 130dp → 145dp để tránh text bị cắt
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color.Black.copy(alpha = 0.25f),
                                spotColor = Color.Black.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        radius = 22.dp
                    ) {
                        // Left-aligned layout with better padding
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp), // Increased padding for breathing room
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // ✅ UNIQUE ICONS - all different
                            Icon(
                                imageVector = when (statIndex) {
                                    0 -> Icons.Filled.People // ✅ People icon for Mentors
                                    1 -> Icons.Filled.CalendarMonth // ✅ Calendar for Sessions
                                    2 -> Icons.Filled.Star // ✅ Star for Rating
                                    else -> Icons.AutoMirrored.Filled.Message // ✅ Message for Quick Response
                                },
                                contentDescription = null,
                                tint = iconColor, // UNIQUE color for each
                                modifier = Modifier.size(36.dp)
                            )

                            // Text column - left aligned with proper spacing
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    stat.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    stat.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 2,
                                    lineHeight = 22.sp // ✅ Tăng từ 20sp → 22sp để text không bị cắt
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
                    // NEON/Highly saturated colors - MUST POP against dark blue background
                    val categoryColor = when (cat.name) {
                        "Technology" -> Color(0xFF00F5FF) // Electric NEON Cyan/Aqua
                        "Business" -> Color(0xFFBB6BFF) // Bright NEON Purple
                        "Design" -> Color(0xFFFF1493) // Deep NEON Pink/Magenta
                        "Marketing" -> Color(0xFFFF6600) // Vibrant NEON Orange
                        "Finance" -> Color(0xFF00FF7F) // Bright NEON Spring Green
                        "Career" -> Color(0xFFFFD700) // Bright NEON Gold
                        else -> Color(0xFF00E5FF) // Electric Cyan default
                    }

                    // Subtitle for each category
                    val categorySubtitle = when (cat.name) {
                        "Technology" -> "Java, Python, AI"
                        "Business" -> "Startup, Management"
                        "Design" -> "UI/UX, Graphic"
                        "Marketing" -> "SEO, Content, Ads"
                        "Finance" -> "Investment, Trading"
                        "Career" -> "Resume, Interview"
                        else -> "Tư vấn chuyên nghiệp"
                    }

                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp) // Increased to prevent clipping
                            .noIndicationClickable {
                                onNavigateToCategory(cat.name)
                            }
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color.Black.copy(alpha = 0.3f),
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            ),
                        radius = 22.dp
                    ) {
                        // Left-aligned column layout with proper padding
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp), // Increased padding
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Icon WITHOUT background - NEON color to POP!
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = categoryColor, // NEON saturated color
                                modifier = Modifier.size(36.dp) // Larger icon
                            )

                            // Text column - left aligned
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    categorySubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(uiState.topMentors) { m ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .width(250.dp) // Increased from 240dp
                            .height(150.dp) // Increased from 140dp
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color.Black.copy(alpha = 0.25f),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        radius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp), // Increased from 14dp
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    m.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White // Full white
                                )
                                Text(
                                    m.role,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f), // Increased
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Rating badge với haze gradient
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFFD700).copy(alpha = 0.40f), // Gold
                                                    Color(0xFFFFA500).copy(alpha = 0.30f)  // Orange
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFFFD700).copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                                            spotColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700), // Gold star
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "%.1f".format(m.rating), // ✅ Làm tròn đến 1 chữ số thập phân
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Đặt lịch",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    ),
                radius = 24.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp), // Increased from 16dp
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp) // Increased from 48dp
                            .clip(CircleShape)
                            .background(story.bgGradient)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.3f),
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            story.icon,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            story.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White, // Full white
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            story.quote,
                            style = MaterialTheme.typography.bodyMedium, // Larger from bodySmall
                            color = Color.White.copy(alpha = 0.85f), // Better contrast
                            lineHeight = 20.sp
                        )
                        Text(
                            "- ${story.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA726), // Gold accent
                            fontWeight = FontWeight.Medium
                        )
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
        formModifier = Modifier.fillMaxSize().padding(4.dp) // ✅ Giảm padding 12→4dp để sheet rộng hơn
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.height(8.dp)) // Extra whitespace above
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Light
        )
    }
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

        // Online status box
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


// ===== HOME MENTOR CARD - FEATURED PROFILE EDITORIAL STYLE =====
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeMentorCard(
    mentor: Mentor,
    onViewProfile: () -> Unit,
    onBookSession: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(28.dp)
            ),
        radius = 28.dp
    ) {
        // Removed duplicate inner border Box since we now have border on LiquidGlassCard
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FEATURED BADGE - Gradient Haze Effect
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.35f), // Gold with transparency
                                    Color(0xFFFFA500).copy(alpha = 0.25f)  // Orange with transparency
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.5f), // Gold border
                            shape = RoundedCornerShape(8.dp)
                        )
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(8.dp),
                            ambientColor = Color(0xFFFFD700).copy(alpha = 0.4f), // Gold glow
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFFFD700), // Gold icon
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "FEATURED",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // ROW: Avatar + Name/Role (cùng hàng)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top // Đổi từ CenterVertically
                ) {
                    // Avatar + Rating (column layout - rating BELOW avatar)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val avatarUrl = mentor.imageUrl.trim()
                        val initials = mentor.name
                            .trim()
                            .split(Regex("\\s+"))
                            .filter { it.isNotBlank() }
                            .mapNotNull { it.firstOrNull() }
                            .take(2)
                            .joinToString("")

                        // Avatar - KHÔNG có rating che mặt
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.Black.copy(alpha = 0.4f),
                                    spotColor = Color.Black.copy(alpha = 0.4f)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Always show initials as fallback
                            Text(
                                text = initials,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Load avatar on top if URL exists
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
                            }
                        }

                        // Rating badge - Haze gradient effect
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = 0.35f), // Gold
                                            Color(0xFFFFA500).copy(alpha = 0.25f)  // Orange
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFD700).copy(alpha = 0.6f), // Gold border
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                                    spotColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700), // Solid Yellow star
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "%.1f".format(mentor.rating), // ✅ Làm tròn đến 1 chữ số thập phân
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Name + Role
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = mentor.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            lineHeight = MaterialTheme.typography.headlineMedium.fontSize * 1.1
                        )

                        Text(
                            text = mentor.role,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        if (mentor.company.isNotBlank()) {
                            Text(
                                text = mentor.company,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Skills
                if (mentor.skills.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mentor.skills.take(5).forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (mentor.skills.size > 5) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "+${mentor.skills.size - 5}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // ACTION BUTTONS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PRIMARY: Book Session - Professional Blue (matching SearchScreen)
                    if (mentor.isAvailable) {
                        Button(
                            onClick = onBookSession,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB), // ✅ Professional Blue from SearchScreen
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.VideoCall,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Đặt lịch ngay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // SECONDARY: View Profile
                    Surface(
                        onClick = onViewProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                "Xem hồ sơ đầy đủ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
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

