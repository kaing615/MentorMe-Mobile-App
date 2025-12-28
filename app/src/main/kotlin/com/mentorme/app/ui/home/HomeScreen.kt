package com.mentorme.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.ui.components.ui.GlassOverlay
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.mentors.MentorCard
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
        item {
            HeroSection(
                onSearch = onSearch,
                onlineCount = uiState.onlineCount,
                avgRating = uiState.avgRating
            )
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
                            .height(90.dp),
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
            MentorCard(
                mentor = mentor,
                onViewProfile = {
                    selectedMentor = mentor
                    showDetail = true
                },
                onBookSession = {
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
                        onMessage = { _ -> /* TODO */ }
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
