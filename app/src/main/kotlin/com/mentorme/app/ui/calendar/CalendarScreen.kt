package com.mentorme.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.core.time.formatIsoToLocalShort
import java.util.Calendar
import androidx.compose.ui.graphics.Brush

// ---------- Helpers (API 24 friendly) ----------
private fun todayDate(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}

private fun nowHHmm(): String {
    val c = Calendar.getInstance()
    return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
}

private fun hhmmToMinutes(hhmm: String): Int {
    val p = hhmm.split(":")
    return p[0].toInt() * 60 + p[1].toInt()
}

private fun minutesToHHmm(mins: Int): String {
    val h = (mins / 60) % 24
    val m = mins % 60
    return "%02d:%02d".format(if (h < 0) h + 24 else h, if (m < 0) m + 60 else m)
}

private fun addMinutes(hhmm: String, plus: Int) = minutesToHHmm(hhmmToMinutes(hhmm) + plus)

private fun isFutureOrNow(date: String, time: String, nowDate: String, nowTime: String) =
    when {
        date > nowDate -> true
        date < nowDate -> false
        else -> time >= nowTime
    }

private fun isPast(date: String, time: String, nowDate: String, nowTime: String) =
    when {
        date < nowDate -> true
        date > nowDate -> false
        else -> time < nowTime
    }

// ---------- UI - HomeScreen Style ----------
private enum class CalTab(val label: String) {
    Upcoming("Sắp tới"),
    Pending("Chờ duyệt"),
    Completed("Hoàn thành"),
    Cancelled("Đã hủy")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    bookings: List<Booking> = MockData.mockBookings,
    onJoinSession: (Booking) -> Unit = {},
    onRate: (Booking) -> Unit = {},
    onRebook: (Booking) -> Unit = {},
    onPay: (Booking) -> Unit = {},
    onCancel: (Booking) -> Unit = {}
) {
    var active by remember { mutableStateOf(CalTab.Upcoming) }

    val nowDate = remember { todayDate() }
    val nowTime = remember { nowHHmm() }

    // Phân loại bookings theo status và thời gian
    val upcoming = remember(bookings, nowDate, nowTime) {
        bookings.filter {
            it.status == BookingStatus.CONFIRMED &&
                    isFutureOrNow(it.date, it.startTime, nowDate, nowTime)
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    val pending = remember(bookings) {
        bookings.filter {
            it.status == BookingStatus.PENDING_MENTOR ||
                    it.status == BookingStatus.PAYMENT_PENDING
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    val completed = remember(bookings, nowDate, nowTime) {
        bookings.filter {
            it.status == BookingStatus.COMPLETED ||
                    (it.status == BookingStatus.CONFIRMED && isPast(it.date, it.endTime, nowDate, nowTime))
        }.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    val cancelled = remember(bookings) {
        bookings.filter {
            it.status == BookingStatus.CANCELLED ||
                    it.status == BookingStatus.DECLINED ||
                    it.status == BookingStatus.FAILED
        }.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    // Layout như HomeScreen - LazyColumn thay vì Scaffold
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
            bottom = 100.dp
        )
    ) {
        // Header
        item {
            Text(
                "Lịch hẹn",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Tabs - HomeScreen style
        item {
            LiquidGlassCard(radius = 22.dp) {
                TabRow(
                    selectedTabIndex = active.ordinal,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { positions ->
                        if (positions.isNotEmpty()) {
                            val pos = positions[active.ordinal]
                            Box(
                                Modifier
                                    .tabIndicatorOffset(pos)
                                    .fillMaxHeight()
                                    .padding(6.dp)
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            Brush.linearGradient(
                                                listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
                                            )
                                        ),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                            )
                        }
                    }
                ) {
                    CalTab.values().forEachIndexed { i, tab ->
                        Tab(
                            selected = i == active.ordinal,
                            onClick = { active = tab },
                            text = {
                                Text(
                                    tab.label,
                                    color = Color.White,
                                    fontWeight = if (i == active.ordinal) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        // Content based on selected tab
        val list = when (active) {
            CalTab.Upcoming -> upcoming
            CalTab.Pending -> pending
            CalTab.Completed -> completed
            CalTab.Cancelled -> cancelled
        }

        if (list.isEmpty()) {
            item {
                EmptyState(active)
            }
        } else {
            items(list, key = { it.id }) { booking ->
                BookingCard(
                    booking = booking,
                    onJoin = onJoinSession,
                    onRate = onRate,
                    onRebook = onRebook,
                    onPay = onPay,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun EmptyState(tab: CalTab) {
    val (emoji, text) = when (tab) {
        CalTab.Upcoming -> "📅" to "Chưa có lịch sắp tới"
        CalTab.Pending -> "⏳" to "Không có booking chờ duyệt"
        CalTab.Completed -> "✅" to "Chưa có phiên hoàn thành"
        CalTab.Cancelled -> "❌" to "Không có lịch đã hủy"
    }

    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                Text(emoji, fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onJoin: (Booking) -> Unit,
    onRate: (Booking) -> Unit,
    onRebook: (Booking) -> Unit,
    onCancel: (Booking) -> Unit,
    onPay: (Booking) -> Unit
) {
    val mentor = remember(booking.mentorId) {
        MockData.mockMentors.firstOrNull { it.id == booking.mentorId }
    }
    val mentorLabel = mentor?.fullName ?: "Mentor ${booking.mentorId.takeLast(6)}"
    val mentorAvatar = mentor?.avatar

    val dateToday = todayDate()
    val now = nowHHmm()

    val canJoin = booking.status == BookingStatus.CONFIRMED &&
            booking.date == dateToday &&
            (now >= addMinutes(booking.startTime, -10)) &&
            (now <= booking.endTime)

    val lateCancelLabel = if (booking.lateCancel == true) {
        val minutes = booking.lateCancelMinutes
        if (minutes != null) "Hủy muộn (hủy trước giờ $minutes phút)" else "Hủy muộn"
    } else {
        null
    }

    val mentorDeadline = formatIsoToLocalShort(booking.mentorResponseDeadline)
    val payExpires = formatIsoToLocalShort(booking.expiresAt)
    val reminder24h = formatIsoToLocalShort(booking.reminder24hSentAt)
    val reminder1h = formatIsoToLocalShort(booking.reminder1hSentAt)
    val policyLabels = listOfNotNull(
        if (booking.status == BookingStatus.PENDING_MENTOR) mentorDeadline?.let { "Mentor deadline: $it" } else null,
        if (booking.status == BookingStatus.PAYMENT_PENDING) payExpires?.let { "Pay expires: $it" } else null,
        reminder24h?.let { "Reminder 24h: $it" },
        reminder1h?.let { "Reminder 1h: $it" }
    )

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 22.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header v?i Avatar và Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .liquidGlass(radius = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (mentorAvatar != null) {
                        AsyncImage(
                            model = mentorAvatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = mentorLabel.split(" ").map { it.first() }.take(2).joinToString(""),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mentorLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${booking.date} • ${booking.startTime}–${booking.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                StatusPill(booking.status)
            }

            if (booking.status == BookingStatus.CANCELLED && lateCancelLabel != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                        .border(BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(lateCancelLabel, color = Color.White)
                }
            }

            // Duration và Price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val minutes = hhmmToMinutes(booking.endTime) - hhmmToMinutes(booking.startTime)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .liquidGlass(radius = 12.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${minutes} phút", color = Color.White)
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .liquidGlass(radius = 12.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${booking.price.toInt()} đ", color = Color.White)
                }
            }

            if (policyLabels.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    policyLabels.forEach { label ->
                        PolicyTag(label)
                    }
                }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (booking.status) {
                    BookingStatus.PAYMENT_PENDING -> {
                        MMButton(
                            text = "Thanh toán (test)",
                            onClick = { onPay(booking) },
                            modifier = Modifier.weight(1f)
                        )
                        MMGhostButton(
                            text = "Hủy",
                            onClick = { onCancel(booking) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BookingStatus.PENDING_MENTOR -> {
                        MMButton(
                            text = "Hủy yêu cầu",
                            onClick = { onCancel(booking) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Close, null, tint = Color.White) }
                        )
                    }
                    BookingStatus.CONFIRMED -> {
                        MMButton(
                            text = if (canJoin) "Tham gia" else "Chưa tới giờ",
                            onClick = { onJoin(booking) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = Color.White) }
                        )
                        MMGhostButton(
                            text = "Hủy",
                            onClick = { onCancel(booking) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BookingStatus.COMPLETED -> {
                        MMButton(
                            text = "Đánh giá",
                            onClick = { onRate(booking) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color.White) }
                        )
                        MMGhostButton(
                            text = "Đặt lại",
                            onClick = { onRebook(booking) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BookingStatus.CANCELLED,
                    BookingStatus.DECLINED -> {
                        MMButton(
                            text = "Đặt lại",
                            onClick = { onRebook(booking) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BookingStatus.FAILED -> {
                        MMButton(
                            text = "Thử lại",
                            onClick = { onPay(booking) },
                            modifier = Modifier.weight(1f)
                        )
                        MMGhostButton(
                            text = "Đặt lại",
                            onClick = { onRebook(booking) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: BookingStatus) {
    val (label, dot, emoji) = when (status) {
        BookingStatus.PAYMENT_PENDING -> Triple("Chờ thanh toán", Color(0xFFF59E0B), "💳")
        BookingStatus.PENDING_MENTOR -> Triple("Chờ mentor", Color(0xFFF59E0B), "⏳")
        BookingStatus.CONFIRMED -> Triple("Xác nhận", Color(0xFF10B981), "✅")
        BookingStatus.COMPLETED -> Triple("Hoàn thành", Color(0xFF8B5CF6), "🎉")
        BookingStatus.CANCELLED -> Triple("Đã hủy", Color(0xFFEF4444), "❌")
        BookingStatus.DECLINED -> Triple("Từ chối", Color(0xFFEF4444), "🚫")
        BookingStatus.FAILED -> Triple("Thất bại", Color(0xFFEF4444), "⚠️")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .liquidGlass(radius = 14.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Text(emoji, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Text(
                label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PolicyTag(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun MMGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.liquidGlass(radius = 16.dp),
        border = null,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}




