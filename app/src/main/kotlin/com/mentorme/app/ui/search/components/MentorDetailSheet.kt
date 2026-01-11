package com.mentorme.app.ui.search.components

import android.app.Activity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mentorme.app.ui.home.Mentor as HomeMentor
import com.mentorme.app.ui.utils.normalizeLanguageLabels
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.profile.ProfileDto
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.domain.usecase.profile.GetPublicProfileUseCase
import com.mentorme.app.domain.usecase.review.GetMentorReviewsUseCase
import com.mentorme.app.data.dto.availability.slotPriceVndOrNull
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

// ===== Time helpers =====
private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

/** Convert ISO-UTC start/end -> "yyyy-MM-dd • HH:mm - HH:mm" in local time zone */
private fun formatSlotWindow(
    startIsoUtc: String,
    endIsoUtc: String,
    zone: ZoneId = ZoneId.systemDefault()
): String {
    val startZ = Instant.parse(startIsoUtc).atZone(zone)
    val endZ   = Instant.parse(endIsoUtc).atZone(zone)
    val dayStr   = DATE_FMT.format(startZ)                 // theo ngày bắt đầu
    val startStr = TIME_FMT.format(startZ)
    val endStr   = TIME_FMT.format(endZ)                   // <-- dùng endZ thật
    return "$dayStr • $startStr - $endStr"
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MentorDetailDeps {
    fun getPublicCalendarUseCase(): GetPublicCalendarUseCase
    fun getPublicProfileUseCase(): GetPublicProfileUseCase
    fun getMentorReviewsUseCase(): GetMentorReviewsUseCase
}

@Composable
fun MentorDetailSheet(
    mentorId: String,
    mentor: HomeMentor,
    onClose: () -> Unit,
    onBookNow: (String) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current
    val deps = remember(context) { EntryPointAccessors.fromApplication(context, MentorDetailDeps::class.java) }
    val getCalendar = remember { deps.getPublicCalendarUseCase() }
    val getProfile = remember { deps.getPublicProfileUseCase() }

    var slots by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<ProfileDto?>(null) }
    var profileLoading by remember { mutableStateOf(false) }

    // ✅ IMMERSIVE MODE: Hide Status Bar when this sheet is shown
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }

        insetsController?.apply {
            // Hide status bar
            hide(WindowInsetsCompat.Type.statusBars())
            // Make it show again with swipe
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // ✅ RESTORE: Show status bar when leaving this screen
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    LaunchedEffect(mentorId) {
        loading = true
        // Window UTC: [today local 00:00 .. +30d 23:59:59] rồi chuyển sang UTC string
        val zone = ZoneId.systemDefault()
        val todayStartLocal = LocalDate.now(zone).atStartOfDay(zone)
        val fromIsoUtc = todayStartLocal.toInstant().toString()
        val toIsoUtc = todayStartLocal.plusDays(30).withHour(23).withMinute(59).withSecond(59).toInstant().toString()

        Logx.d("Search") { "load calendar for id=$mentorId from=$fromIsoUtc to=$toIsoUtc" }
        when (val res = getCalendar(mentorId, fromIsoUtc, toIsoUtc, includeClosed = true)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                val items = res.data
                val nf = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                slots = items.mapNotNull { item ->
                    val s = item.start ?: return@mapNotNull null
                    val e = item.end ?: return@mapNotNull null
                    val base = runCatching { formatSlotWindow(s, e) }.getOrNull() ?: return@mapNotNull null
                    val priceVnd = item.slotPriceVndOrNull()?.toLong() ?: 0L
                    if (priceVnd > 0) "$base • ${nf.format(priceVnd)}" else base
                }
                loading = false
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
                Logx.d("Search") { "calendar error: ${res.throwable}" }
                slots = emptyList()
                loading = false
            }
            com.mentorme.app.core.utils.AppResult.Loading -> Unit
        }
    }

    LaunchedEffect(mentorId) {
        val idSafe = mentorId.trim()
        if (idSafe.isBlank()) {
            profile = null
            profileLoading = false
            return@LaunchedEffect
        }
        profileLoading = true
        profile = null
        when (val res = getProfile(idSafe)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                profile = res.data
                profileLoading = false
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
                Logx.d("Search") { "profile error: ${res.throwable}" }
                profileLoading = false
            }
            com.mentorme.app.core.utils.AppResult.Loading -> profileLoading = true
        }
    }

    MentorDetailContent(
        mentor = mentor,
        profile = profile,
        profileLoading = profileLoading,
        onClose = onClose,
        onBookNow = onBookNow,
        onMessage = { onMessage(mentor.id) },
        slots = slots,
        loading = loading
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MentorDetailContent(
    mentor: HomeMentor,
    profile: ProfileDto?,
    profileLoading: Boolean,
    onClose: () -> Unit,
    onBookNow: (String) -> Unit,
    onMessage: (String) -> Unit,
    slots: List<String>,
    loading: Boolean
) {
    var activeTab by remember { mutableStateOf(0) }

    // Pre-compute display values
    val profileName = profile?.fullName?.trim().orEmpty()
    val displayName = if (profileName.isNotBlank()) profileName else mentor.name
    val headline = profile?.headline?.trim().takeIf { !it.isNullOrBlank() }
    val jobTitle = profile?.jobTitle?.trim().takeIf { !it.isNullOrBlank() }
    val baseRole = headline ?: jobTitle ?: mentor.role.ifBlank { "Mentor" }
    val subtitle = listOfNotNull(
        baseRole.takeIf { it.isNotBlank() },
        mentor.company.trim().takeIf { it.isNotBlank() },
        profile?.location?.trim().takeIf { !it.isNullOrBlank() }
    ).joinToString(" • ")

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp) // ✅ Tăng padding ngang từ 16→20dp
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp) // ✅ Tăng spacing từ 16→20dp
        ) {
            // Header - matching BookSessionSheet style
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Chi tiết mentor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null)
                }
            }

            // Mentor Summary Card - matching style of BookSessionSheet's MentorSummaryCard
            MentorDetailSummaryCard(
                mentor = mentor,
                profile = profile,
                profileLoading = profileLoading,
                displayName = displayName,
                subtitle = subtitle
            )

            // Action Buttons - Professional Blue Primary + Taller Height (56.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // PRIMARY: Book Now (Professional Blue, Solid, High contrast)
                Button(
                    onClick = { onBookNow(mentor.id) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đặt lịch", fontWeight = FontWeight.SemiBold)
                }

                // SECONDARY: Message (Subtle)
                Surface(
                    onClick = { onMessage(mentor.id) },
                    modifier = Modifier
                        .weight(0.38f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Message",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Segmented tabs
            SegmentedTabs(
                titles = listOf("Tổng quan", "Kinh nghiệm", "Đánh giá", "Lịch trống"),
                selectedIndex = activeTab,
                onSelect = { activeTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Content for each tab
            when (activeTab) {
                0 -> OverviewTab(mentor, profile)
                1 -> ExperienceTab(profile)
                2 -> ReviewsTab(mentorId = mentor.id)
                3 -> ScheduleTab(slots = slots, loading = loading, onBookNow = { onBookNow(mentor.id) })
            }
        }
    }
}

// New component to match BookSessionSheet's MentorSummaryCard style
@Composable
private fun MentorDetailSummaryCard(
    mentor: HomeMentor,
    profile: ProfileDto?,
    profileLoading: Boolean,
    displayName: String,
    subtitle: String
) {
    // ✅ Rating always from mentor object (from MentorCardDto)
    // ✅ HourlyRate: prefer profile data if available
    val displayRating = mentor.rating
    val displayHourlyRate = profile?.hourlyRateVnd?.toLong() ?: mentor.hourlyRate.toLong()

    com.mentorme.app.ui.theme.LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 24.dp,
        alpha = 0.18f,
        borderAlpha = 0.35f
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Name and subtitle
            Text(
                if (profileLoading && displayName == "Loading...") "Đang tải..." else displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            // Rating & Price chips - Show loading state if needed
            if (profileLoading && profile == null && mentor.rating == 0.0) {
                // Loading state
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(0.16f)
                    ) {
                        Text(
                            "Đang tải...",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(0.6f)
                        )
                    }
                }
            } else {
                // Data loaded
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(0.16f)
                    ) {
                        Text(
                            "⭐ ${"%.1f".format(displayRating)}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(0.16f)
                    ) {
                        val nf = java.text.NumberFormat.getInstance(Locale("vi", "VN"))
                        Text(
                            "₫${nf.format(displayHourlyRate)}/giờ",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

//  Clean, borderless segmented tabs với underline indicator
@Composable
fun SegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.08f), // Lớp bọc ngoài duy nhất
        shape = outerShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp), // ✅ Tăng padding ngang 8→12dp
            horizontalArrangement = Arrangement.spacedBy(8.dp), // ✅ Spacing giữa tabs
            verticalAlignment = Alignment.CenterVertically
        ) {
            titles.forEachIndexed { index, title ->
                val selected = index == selectedIndex

                // ✅ Column để chứa Text + Underline
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.15.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // ✅ UNDERLINE indicator thay vì background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                if (selected) Color(0xFF60A5FA) else Color.Transparent,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun OverviewTab(mentor: HomeMentor, profile: ProfileDto?) {
    val summaryText = profile?.bio?.trim().takeIf { !it.isNullOrBlank() }
        ?: profile?.description?.trim().takeIf { !it.isNullOrBlank() }
        ?: profile?.headline?.trim().takeIf { !it.isNullOrBlank() }
        ?: "Senior ${mentor.role} với nhiều kinh nghiệm thực chiến. Chia sẻ về lộ trình, kỹ năng và định hướng."
    val skills = (profile?.skills?.filter { it.isNotBlank() } ?: mentor.skills).filter { it.isNotBlank() }
    val languages = profile?.languages
        ?.let { normalizeLanguageLabels(it) }
        ?.ifEmpty { listOf("Tiếng Việt", "Tiếng Anh") }
        ?: listOf("Tiếng Việt", "Tiếng Anh")
    val location = profile?.location?.trim()?.takeIf { it.isNotBlank() } ?: "Online"

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp) // ✅ Tăng spacing từ 16→18dp
    ) {
        // Bio/Description - Use LiquidGlassCard to match ReviewItem style
        com.mentorme.app.ui.theme.LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 16.dp
        ) {
            Text(
                summaryText,
                modifier = Modifier.padding(20.dp), // ✅ Tăng padding từ 16→20dp cho text rộng hơn
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 24.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )
            )
        }

        // Skills - Use LiquidGlassCard
        if (skills.isNotEmpty()) {
            com.mentorme.app.ui.theme.LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp), // ✅ Tăng padding từ 16→20dp
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Chuyên môn",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        skills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    skill,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info rows - Use LiquidGlassCard
        com.mentorme.app.ui.theme.LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp), // ✅ Tăng padding từ 16→20dp
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Languages
                InfoRow(
                    icon = Icons.Default.Language,
                    label = "Ngôn ngữ",
                    value = languages.joinToString(", ")
                )

                // Location
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Địa điểm",
                    value = location
                )

                // Format
                InfoRow(
                    icon = Icons.Default.VideoCall,
                    label = "Hình thức",
                    value = "Online"
                )
            }
        }
    }
}

// Helper composable for clean info rows
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}

@Composable
private fun ExperienceTab(profile: ProfileDto?) {
    data class ExperienceEntry(
        val type: String, // "work" or "education"
        val content: String
    )

    val entries = mutableListOf<ExperienceEntry>()

    profile?.experience?.trim()?.takeIf { it.isNotBlank() }?.let {
        entries.add(ExperienceEntry("work", it))
    }
    profile?.education?.trim()?.takeIf { it.isNotBlank() }?.let {
        entries.add(ExperienceEntry("education", it))
    }

    if (entries.isEmpty()) {
        entries.addAll(
            listOf(
                ExperienceEntry(
                    "work",
                    "Senior Software Engineer • 2020 - nay • Tech Company\nPhát triển hệ thống backend phục vụ hàng triệu người dùng."
                ),
                ExperienceEntry(
                    "work",
                    "Software Engineer • 2018 - 2020 • Startup\nXây dựng sản phẩm từ giai đoạn đầu, làm việc trong team nhỏ."
                )
            )
        )
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Tăng spacing từ 12→16dp
    ) {
        entries.forEach { entry ->
            // Use LiquidGlassCard to match ReviewItem style
            com.mentorme.app.ui.theme.LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), // ✅ Tăng padding từ 16→20dp
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Icon based on type
                    Icon(
                        imageVector = if (entry.type == "education")
                            Icons.Default.School
                        else
                            Icons.Default.Work,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )

                    // Content
                    Text(
                        entry.content,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewsTab(mentorId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deps = remember(context) { EntryPointAccessors.fromApplication(context, MentorDetailDeps::class.java) }
    val getReviews = remember { deps.getMentorReviewsUseCase() }
    val scope = rememberCoroutineScope()

    var reviews by remember { mutableStateOf<List<com.mentorme.app.data.dto.review.ReviewDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(false) }
    var nextCursor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mentorId) {
        isLoading = true
        when (val res = getReviews(mentorId, limit = 20, cursor = null)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                val (reviewList, pagination) = res.data
                reviews = reviewList
                hasMore = pagination.hasMore
                nextCursor = pagination.nextCursor
                isLoading = false
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
                Logx.d("ReviewsTab") { "Failed to load reviews: ${res.throwable}" }
                reviews = emptyList()
                isLoading = false
            }
            com.mentorme.app.core.utils.AppResult.Loading -> Unit
        }
    }

    val loadMore = {
        if (!isLoading && hasMore && nextCursor != null) {
            scope.launch {
                isLoading = true
                when (val res = getReviews(mentorId, limit = 20, cursor = nextCursor)) {
                    is com.mentorme.app.core.utils.AppResult.Success -> {
                        val (moreReviews, pagination) = res.data
                        reviews = reviews + moreReviews
                        hasMore = pagination.hasMore
                        nextCursor = pagination.nextCursor
                        isLoading = false
                    }
                    is com.mentorme.app.core.utils.AppResult.Error -> {
                        isLoading = false
                    }
                    com.mentorme.app.core.utils.AppResult.Loading -> Unit
                }
            }
        }
    }

    // Use Column instead of LazyColumn to avoid nested scroll
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Tăng spacing từ 12→16dp
    ) {
        if (reviews.isEmpty() && !isLoading) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có đánh giá nào",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        } else {
            // Display reviews
            reviews.forEach { review ->
                com.mentorme.app.ui.review.ReviewItem(review = review)
            }

            // Load more button
            if (hasMore && !isLoading) {
                TextButton(
                    onClick = loadMore,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Xem thêm đánh giá",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTab(slots: List<String>, loading: Boolean, onBookNow: () -> Unit) {
    val display: List<String> =
        when {
            loading -> listOf("Đang tải lịch trống...")
            slots.isEmpty() -> listOf("Chưa có lịch trống trong 30 ngày tới")
            else -> slots
        }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Tăng spacing từ 12→16dp
    ) {
        display.forEach { s ->
            // Use LiquidGlassCard to match ReviewItem style
            com.mentorme.app.ui.theme.LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 16.dp
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp) // ✅ Tăng padding từ 16→20dp
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // ✅ Thêm spacing giữa text và button
                ) {
                    Text(
                        s,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                    if (!loading && slots.isNotEmpty()) {
                        Button(
                            onClick = onBookNow,
                            modifier = Modifier.heightIn(min = 46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đặt lịch", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
