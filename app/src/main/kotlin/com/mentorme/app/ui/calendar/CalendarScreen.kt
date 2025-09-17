package com.mentorme.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.components.ui.LiquidGlassCard
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import java.util.Calendar

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
    val p = hhmm.split(":"); return p[0].toInt() * 60 + p[1].toInt()
}
private fun minutesToHHmm(mins: Int): String {
    val h = (mins / 60) % 24; val m = mins % 60
    return "%02d:%02d".format(if (h < 0) h + 24 else h, if (m < 0) m + 60 else m)
}
private fun addMinutes(hhmm: String, plus: Int) = minutesToHHmm(hhmmToMinutes(hhmm) + plus)

private fun isFutureOrNow(date: String, time: String, nowDate: String, nowTime: String) =
    when { date > nowDate -> true; date < nowDate -> false; else -> time >= nowTime }

private fun isPast(date: String, time: String, nowDate: String, nowTime: String) =
    when { date < nowDate -> true; date > nowDate -> false; else -> time < nowTime }

// ---------- UI ----------
private enum class CalTab(val label: String) { Upcoming("Sắp tới"), Pending("Chờ duyệt"), Completed("Hoàn thành"), Cancelled("Đã hủy") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    bookings: List<Booking> = MockData.mockBookings, // nếu chưa có, đổi sang emptyList()
    onJoinSession: (Booking) -> Unit = {},
    onRate: (Booking) -> Unit = {},
    onRebook: (Booking) -> Unit = {},
    onCancel: (Booking) -> Unit = {}
) {
    var active by remember { mutableStateOf(CalTab.Upcoming) }

    // thời điểm hiện tại
    val nowDate = remember { todayDate() }
    val nowTime = remember { nowHHmm() }

    // phân loại theo status + thời gian
    val upcoming = remember(bookings, nowDate, nowTime) {
        bookings.filter {
            it.status == BookingStatus.CONFIRMED &&
                    isFutureOrNow(it.date, it.startTime, nowDate, nowTime)
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }
    val pending = remember(bookings) {
        bookings.filter { it.status == BookingStatus.PENDING }
            .sortedWith(compareBy({ it.date }, { it.startTime }))
    }
    val completed = remember(bookings, nowDate, nowTime) {
        bookings.filter {
            it.status == BookingStatus.COMPLETED ||
                    (it.status == BookingStatus.CONFIRMED && isPast(it.date, it.endTime, nowDate, nowTime))
        }.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }
    val cancelled = remember(bookings) {
        bookings.filter { it.status == BookingStatus.CANCELLED }
            .sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    // scaffold trong suốt để lộ nền “liquid” của app (giống Home)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch hẹn") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tabs đặt trong glass nhẹ
            LiquidGlassCard {
                TabRow(selectedTabIndex = active.ordinal, containerColor = MaterialTheme.colorScheme.surface.copy(0f)) {
                    CalTab.values().forEachIndexed { i, tab ->
                        Tab(selected = i == active.ordinal, onClick = { active = tab }, text = { Text(tab.label) })
                    }
                }
            }

            val list = when (active) {
                CalTab.Upcoming -> upcoming
                CalTab.Pending -> pending
                CalTab.Completed -> completed
                CalTab.Cancelled -> cancelled
            }

            if (list.isEmpty()) {
                EmptyState(active)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { b ->
                        BookingGlassCard(
                            booking = b,
                            onJoin = onJoinSession,
                            onRate = onRate,
                            onRebook = onRebook,
                            onCancel = onCancel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(tab: CalTab) {
    val text = when (tab) {
        CalTab.Upcoming -> "Chưa có lịch sắp tới"
        CalTab.Pending -> "Không có booking chờ duyệt"
        CalTab.Completed -> "Chưa có phiên hoàn thành"
        CalTab.Cancelled -> "Không có lịch đã hủy"
    }
    LiquidGlassCard(strong = true, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BookingGlassCard(
    booking: Booking,
    onJoin: (Booking) -> Unit,
    onRate: (Booking) -> Unit,
    onRebook: (Booking) -> Unit,
    onCancel: (Booking) -> Unit
) {
    // tra cứu mentor để hiển thị
    val mentor = remember(booking.mentorId) { MockData.mockMentors.firstOrNull { it.id == booking.mentorId } }
    val mentorName = mentor?.fullName ?: "Mentor"
    val mentorAvatar = mentor?.avatar

    // Join: từ 10’ trước giờ bắt đầu tới khi kết thúc (chỉ cùng ngày)
    val dateToday = todayDate()
    val now = nowHHmm()
    val canJoin =
        booking.status == BookingStatus.CONFIRMED &&
                booking.date == dateToday &&
                (now >= addMinutes(booking.startTime, -10)) &&
                (now <= booking.endTime)

    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = mentorAvatar, contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(mentorName, fontWeight = FontWeight.SemiBold)
                Text(
                    "${booking.date} • ${booking.startTime}–${booking.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(booking.status)
        }

        Spacer(Modifier.height(12.dp))

        // Meta
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            val minutes = hhmmToMinutes(booking.endTime) - hhmmToMinutes(booking.startTime)
            Text("Thời lượng: ${minutes} phút")
            Spacer(Modifier.weight(1f))
            Text("Giá: \$${"%.2f".format(booking.price)}", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(12.dp))

        // Actions (nút primary = MMPrimaryButton để match Home)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (booking.status) {
                BookingStatus.PENDING -> {
                    OutlinedButton(onClick = { onCancel(booking) }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp)); Text("Hủy yêu cầu")
                    }
                }
                BookingStatus.CONFIRMED -> {
                    MMPrimaryButton(onClick = { onJoin(booking) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp)); Text(if (canJoin) "Tham gia" else "Chưa tới giờ")
                    }
                    OutlinedButton(onClick = { onCancel(booking) }, modifier = Modifier.weight(1f)) { Text("Hủy") }
                }
                BookingStatus.COMPLETED -> {
                    MMPrimaryButton(onClick = { onRate(booking) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp)); Text("Đánh giá")
                    }
                    OutlinedButton(onClick = { onRebook(booking) }, modifier = Modifier.weight(1f)) { Text("Đặt lại") }
                }
                BookingStatus.CANCELLED -> {
                    MMPrimaryButton(onClick = { onRebook(booking) }, modifier = Modifier.weight(1f)) { Text("Đặt lại") }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: BookingStatus) {
    val (label, color) = when (status) {
        BookingStatus.PENDING   -> "Chờ duyệt"   to MaterialTheme.colorScheme.tertiary
        BookingStatus.CONFIRMED -> "Xác nhận"    to MaterialTheme.colorScheme.primary
        BookingStatus.COMPLETED -> "Hoàn thành"  to MaterialTheme.colorScheme.secondary
        BookingStatus.CANCELLED -> "Đã hủy"      to MaterialTheme.colorScheme.error
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color)
    )
}
