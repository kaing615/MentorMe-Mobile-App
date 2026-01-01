package com.mentorme.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CalendarToday // ✅ NEW: For empty state icon
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

private fun durationMinutes(start: String, end: String): Int {
    val diff = hhmmToMinutes(end) - hhmmToMinutes(start)
    return if (diff < 0) diff + 24 * 60 else diff
}

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
enum class CalendarTab(val label: String) {
    Upcoming("Sắp tới"),
    Pending("Chờ duyệt"),
    Completed("Hoàn thành"),
    Cancelled("Đã hủy");

    companion object {
        fun fromRouteArg(arg: String?): CalendarTab {
            return when (arg?.lowercase()) {
                "pending" -> Pending
                "completed" -> Completed
                "cancelled", "canceled" -> Cancelled
                "upcoming" -> Upcoming
                else -> Upcoming
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    bookings: List<Booking> = MockData.mockBookings,
    startTab: CalendarTab = CalendarTab.Upcoming,
    onJoinSession: (Booking) -> Unit = {},
    onRate: (Booking) -> Unit = {},
    onRebook: (Booking) -> Unit = {},
    onPay: (Booking) -> Unit = {},
    onCancel: (Booking) -> Unit = {},
    onOpen: (Booking) -> Unit = {}
) {
    var active by rememberSaveable(startTab) { mutableStateOf(startTab) }

    val nowDate = remember { todayDate() }
    val nowTime = remember { nowHHmm() }

    // ✅ FIX: Phân loại bookings với logic đúng - check endTime thay vì startTime
    val upcoming = remember(bookings, nowDate, nowTime) {
        bookings.filter { booking ->
            if (booking.status != BookingStatus.CONFIRMED) return@filter false

            // ✅ Check endTime thay vì startTime để tránh bug session 01:00 sáng
            val sessionEnded = when {
                booking.date < nowDate -> true // Ngày đã qua
                booking.date > nowDate -> false // Ngày chưa tới
                else -> { // Cùng ngày - so sánh endTime
                    val endMins = hhmmToMinutes(booking.endTime)
                    val nowMins = hhmmToMinutes(nowTime)
                    endMins <= nowMins // Session kết thúc nếu endTime <= now
                }
            }

            !sessionEnded // Chỉ lấy sessions chưa kết thúc
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    val pending = remember(bookings) {
        bookings.filter {
            it.status == BookingStatus.PENDING_MENTOR ||
                    it.status == BookingStatus.PAYMENT_PENDING
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    // ✅ FIX: Completed bao gồm cả CONFIRMED sessions đã kết thúc (endTime đã qua)
    val completed = remember(bookings, nowDate, nowTime) {
        bookings.filter { booking ->
            // Case 1: Status là COMPLETED hoặc NO_SHOW
            if (booking.status == BookingStatus.COMPLETED ||
                booking.status == BookingStatus.NO_SHOW_MENTOR ||
                booking.status == BookingStatus.NO_SHOW_MENTEE ||
                booking.status == BookingStatus.NO_SHOW_BOTH) {
                return@filter true
            }

            // Case 2: CONFIRMED nhưng đã qua endTime
            if (booking.status == BookingStatus.CONFIRMED) {
                val sessionEnded = when {
                    booking.date < nowDate -> true
                    booking.date > nowDate -> false
                    else -> {
                        val endMins = hhmmToMinutes(booking.endTime)
                        val nowMins = hhmmToMinutes(nowTime)
                        endMins <= nowMins
                    }
                }
                return@filter sessionEnded
            }

            false
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
                    CalendarTab.values().forEachIndexed { i, tab ->
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
            CalendarTab.Upcoming -> upcoming
            CalendarTab.Pending -> pending
            CalendarTab.Completed -> completed
            CalendarTab.Cancelled -> cancelled
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
                    onCancel = onCancel,
                    onOpen = onOpen
                )
            }
        }
    }
}

@Composable
private fun EmptyState(tab: CalendarTab) {
    // ✅ NEW: Use Material Icons instead of emoji
    val (icon, text) = when (tab) {
        CalendarTab.Upcoming -> Icons.Default.CalendarToday to "Chưa có lịch sắp tới"
        CalendarTab.Pending -> Icons.Default.HourglassEmpty to "Không có booking chờ duyệt"
        CalendarTab.Completed -> Icons.Default.CheckCircle to "Chưa có phiên hoàn thành"
        CalendarTab.Cancelled -> Icons.Default.Cancel to "Không có lịch đã hủy"
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
                // ✅ NEW: Show Material Icon instead of emoji
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(0.8f),
                    modifier = Modifier.size(20.dp)
                )
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
    onPay: (Booking) -> Unit,
    onOpen: (Booking) -> Unit
) {
    val fallbackMentor = remember(booking.mentorId) {
        MockData.mockMentors.firstOrNull { it.id == booking.mentorId }
    }
    val mentorLabel = listOf(
        booking.mentorFullName,
        booking.mentor?.fullName,
        fallbackMentor?.fullName
    ).firstOrNull { !it.isNullOrBlank() }?.trim()
        ?: "Mentor ${booking.mentorId.takeLast(6)}"
    val mentorAvatar = booking.mentor?.avatar ?: fallbackMentor?.avatar

    val dateToday = todayDate()
    val now = nowHHmm()

    // ✅ FIX: Logic canJoin giống Mentor - check session chưa kết thúc và trong khoảng 15 phút trước
    val sessionEnded = when {
        booking.date < dateToday -> true // Ngày đã qua
        booking.date > dateToday -> false // Ngày chưa tới
        else -> { // Cùng ngày
            val endMins = hhmmToMinutes(booking.endTime)
            val nowMins = hhmmToMinutes(now)
            endMins <= nowMins // Đã qua giờ kết thúc
        }
    }

    val canJoin = booking.status == BookingStatus.CONFIRMED &&
            !sessionEnded &&
            booking.date == dateToday && // Phải trong hôm nay
            run {
                val nowMins = hhmmToMinutes(now)
                val startMins = hhmmToMinutes(booking.startTime)
                // Có thể join trong khoảng 15 phút trước đến hết giờ session
                nowMins >= (startMins - 15)
            }

    // ✅ Check if starting soon (trong vòng 30 phút)
    val isStartingSoon = booking.status == BookingStatus.CONFIRMED &&
            !sessionEnded &&
            booking.date == dateToday &&
            run {
                val nowMins = hhmmToMinutes(now)
                val startMins = hhmmToMinutes(booking.startTime)
                nowMins >= (startMins - 30) && nowMins < startMins
            }

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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(booking) },
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

                // ✅ Pass sessionEnded to StatusPill
                StatusPill(booking.status, sessionEnded = sessionEnded)
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
                val minutes = durationMinutes(booking.startTime, booking.endTime)

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
                    Text("\$${"%.2f".format(booking.price)}", color = Color.White)
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
                        if (!sessionEnded) {
                            // Session chưa kết thúc - hiển thị nút Vào cuộc
                            MMButton(
                                text = if (canJoin) "Vào cuộc" else "Chưa tới giờ",
                                onClick = { if (canJoin) onJoin(booking) },
                                enabled = canJoin,
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = Color.White) }
                            )
                            MMGhostButton(
                                text = "Hủy",
                                onClick = { onCancel(booking) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Session đã kết thúc - hiển thị như COMPLETED
                            if (booking.reviewId == null) {
                                MMButton(
                                    text = "Đánh giá",
                                    onClick = { onRate(booking) },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color.White) }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Đã đánh giá",
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.5f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            MMGhostButton(
                                text = "Đặt lại",
                                onClick = { onRebook(booking) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    BookingStatus.COMPLETED -> {
                        // Only show review button if not yet reviewed
                        if (booking.reviewId == null) {
                            MMButton(
                                text = "Đánh giá",
                                onClick = { onRate(booking) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color.White) }
                            )
                        } else {
                            // Show disabled-style button for already reviewed
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Đã đánh giá",
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.5f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        MMGhostButton(
                            text = "Đặt lại",
                            onClick = { onRebook(booking) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BookingStatus.NO_SHOW_MENTOR,
                    BookingStatus.NO_SHOW_MENTEE,
                    BookingStatus.NO_SHOW_BOTH -> {
                        MMGhostButton(
                            text = "Đặt lại",
                            onClick = { onRebook(booking) },
                            modifier = Modifier.fillMaxWidth()
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
private fun StatusPill(status: BookingStatus, sessionEnded: Boolean = false) {
    // ✅ Override: CONFIRMED but ended → show as COMPLETED
    val (label, dotColor, icon) = when {
        status == BookingStatus.CONFIRMED && sessionEnded ->
            Triple("Hoàn thành", Color(0xFF8B5CF6), Icons.Default.Star)
        status == BookingStatus.PAYMENT_PENDING ->
            Triple("Chờ thanh toán", Color(0xFFF59E0B), Icons.Default.CreditCard)
        status == BookingStatus.PENDING_MENTOR ->
            Triple("Chờ mentor", Color(0xFFF59E0B), Icons.Default.HourglassEmpty)
        status == BookingStatus.CONFIRMED ->
            Triple("Xác nhận", Color(0xFF10B981), Icons.Default.CheckCircle)
        status == BookingStatus.COMPLETED ->
            Triple("Hoàn thành", Color(0xFF8B5CF6), Icons.Default.Star)
        status == BookingStatus.NO_SHOW_MENTOR ->
            Triple("No-show mentor", Color(0xFFF97316), Icons.Default.Warning)
        status == BookingStatus.NO_SHOW_MENTEE ->
            Triple("No-show mentee", Color(0xFFF97316), Icons.Default.Warning)
        status == BookingStatus.NO_SHOW_BOTH ->
            Triple("No-show cả hai", Color(0xFFF97316), Icons.Default.Warning)
        status == BookingStatus.CANCELLED ->
            Triple("Đã hủy", Color(0xFFEF4444), Icons.Default.Cancel)
        status == BookingStatus.DECLINED ->
            Triple("Từ chối", Color(0xFFEF4444), Icons.Default.Block)
        status == BookingStatus.FAILED ->
            Triple("Thất bại", Color(0xFFEF4444), Icons.Default.Error)
        else ->
            Triple("Unknown", Color.Gray, Icons.Default.Error)
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = dotColor,
                modifier = Modifier.size(16.dp)
            )
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




