package com.mentorme.app.ui.search.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.home.Mentor as HomeMentor
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.profile.ProfileDto
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.domain.usecase.profile.GetPublicProfileUseCase
import com.mentorme.app.data.dto.availability.slotPriceVndOrNull
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    val deps = remember(context) { EntryPointAccessors.fromApplication(context, MentorDetailDeps::class.java) }
    val getCalendar = remember { deps.getPublicCalendarUseCase() }
    val getProfile = remember { deps.getPublicProfileUseCase() }

    var slots by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<ProfileDto?>(null) }
    var profileLoading by remember { mutableStateOf(false) }

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

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✨ Chi tiết mentor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            Spacer(Modifier.height(8.dp))

            // Top summary
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    mentor.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${mentor.role} • ${mentor.company}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⭐ ${"%.1f".format(mentor.rating)}", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(0.14f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.White.copy(0.28f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${mentor.hourlyRate} VNĐ/giờ", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(0.14f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.White.copy(0.28f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Stacked CTAs
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onBookNow(mentor.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Đặt lịch ngay")
                    }
                    OutlinedButton(
                        onClick = { onMessage(mentor.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Nhắn tin")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Edge-to-edge segmented tabs (pill)
            SegmentedTabs(
                titles = listOf("Tổng quan", "Kinh nghiệm", "Đánh giá", "Lịch trống"),
                selectedIndex = activeTab,
                onSelect = { activeTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Content
            when (activeTab) {
                0 -> OverviewTab(mentor)
                1 -> ExperienceTab()
                2 -> ReviewsTab()
                3 -> ScheduleTab(slots = slots, loading = loading, onBookNow = { onBookNow(mentor.id) })
            }
        }
    }
}

// ✅ Thay thế toàn bộ SegmentedTabs hiện tại bằng phiên bản này
@Composable
fun SegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.White.copy(alpha = 0.12f),
        shape = outerShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            titles.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val innerShape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                        .clip(innerShape)
                        .background(if (selected) Color.White.copy(alpha = 0.28f) else Color.Transparent)
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun OverviewTab(mentor: HomeMentor) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Senior ${mentor.role} với nhiều kinh nghiệm thực chiến. Chia sẻ về lộ trình, kỹ năng và định hướng.",
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )
            }
        }
        if (mentor.skills.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    mentor.skills.forEach {
                        AssistChip(
                            onClick = {},
                            label = { Text(it, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                            modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.14f)
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = Color.White.copy(alpha = 0.28f),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
        items(listOf("Ngôn ngữ: Tiếng Việt, English", "Hình thức: Online")) { line ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(line, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ExperienceTab() {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            listOf(
                "Senior Software Engineer • 2020 - nay • Tech Company\nPhát triển hệ thống backend phục vụ hàng triệu người dùng.",
                "Software Engineer • 2018 - 2020 • Startup\nXây dựng sản phẩm từ giai đoạn đầu, làm việc trong team nhỏ."
            )
        ) { s ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) { Text(s, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)) }
        }
    }
}

@Composable
private fun ReviewsTab() {
    val reviews = listOf(
        "Mentor tận tâm, giúp tôi rõ career path. Highly recommended! — 15/01/2024",
        "Session bổ ích, kinh nghiệm thực tế, chia sẻ chi tiết. — 10/01/2024",
        "Professional & friendly. Sẽ book thêm. — 05/01/2024",
    )
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reviews) { r ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(r, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
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

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(display) { s ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s, modifier = Modifier.weight(1f))
                    if (!loading && slots.isNotEmpty()) {
                        Button(
                            onClick = onBookNow,
                            modifier = Modifier.fillMaxWidth(0.38f).heightIn(min = 46.dp)
                        ) { Text("Đặt lịch") }
                    }
                }
            }
        }
    }
}
