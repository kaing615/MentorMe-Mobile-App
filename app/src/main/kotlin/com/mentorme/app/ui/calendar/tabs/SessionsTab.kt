package com.mentorme.app.ui.calendar.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.components.InfoRow
import com.mentorme.app.ui.calendar.core.durationMinutes
import com.mentorme.app.core.time.formatIsoToLocalShort
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun policyRowsFor(booking: Booking): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    val mentorDeadline = formatIsoToLocalShort(booking.mentorResponseDeadline)
    val payExpires = formatIsoToLocalShort(booking.expiresAt)
    val reminder24h = formatIsoToLocalShort(booking.reminder24hSentAt)
    val reminder1h = formatIsoToLocalShort(booking.reminder1hSentAt)

    if (!mentorDeadline.isNullOrBlank()) rows.add("Hạn phản hồi mentor" to mentorDeadline)
    if (!payExpires.isNullOrBlank()) rows.add("Hết hạn thanh toán" to payExpires)
    if (!reminder24h.isNullOrBlank()) rows.add("Nhắc 24h" to reminder24h)
    if (!reminder1h.isNullOrBlank()) rows.add("Nhắc 1h" to reminder1h)
    if (booking.lateCancel == true) {
        val minutes = booking.lateCancelMinutes
        val label = if (minutes != null) "(hủy trước giờ $minutes phút)" else "(hủy muộn)"
        rows.add("Hủy muộn" to label)
    }
    return rows
}

private fun menteeDisplayName(booking: Booking): String {
    val menteeId = booking.menteeId.trim()
    val displayName = sequenceOf(
        booking.menteeFullName,
        booking.mentee?.profile?.fullName,
        booking.mentee?.fullName,
        booking.mentee?.user?.fullName,
        booking.mentee?.user?.userName
    )
        .mapNotNull { it?.trim() }
        .firstOrNull { it.isNotEmpty() && it != menteeId }

    return displayName ?: if (menteeId.length > 6) "...${menteeId.takeLast(6)}" else menteeId
}

private fun canJoinSession(booking: Booking): Boolean {
    if (booking.status != BookingStatus.CONFIRMED) return false

    try {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val bookingDate = LocalDate.parse(booking.date)

        // Parse times - handle both "H:mm" and "HH:mm" formats
        val startTime = try {
            LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("H:mm"))
        }

        val endTime = try {
            LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("H:mm"))
        }

        // ✅ Handle sessions that span midnight (e.g., 23:00 - 01:00)
        val endDate = if (endTime.isBefore(startTime) || endTime == startTime) {
            bookingDate.plusDays(1)
        } else {
            bookingDate
        }

        val bookingStartDateTime = bookingDate.atTime(startTime).atZone(zoneId).toInstant()
        val bookingEndDateTime = endDate.atTime(endTime).atZone(zoneId).toInstant()

        // ✅ DEBUG: Log time values
        android.util.Log.d("SessionsTab",
            "canJoin check: bookingId=${booking.id}, " +
            "date=${booking.date}, " +
            "startTime=${booking.startTime}, " +
            "endTime=${booking.endTime}, " +
            "now=${now}, " +
            "bookingStart=${bookingStartDateTime}, " +
            "bookingEnd=${bookingEndDateTime}, " +
            "isNotEnded=${now.isBefore(bookingEndDateTime)}"
        )

        // Can only join if:
        // 1. Session hasn't ended yet (now < endTime)
        // 2. Within 20 minutes before start time (matching backend SESSION_JOIN_EARLY_MINUTES)
        val isNotEnded = now.isBefore(bookingEndDateTime)
        val canJoinTime = now.isAfter(bookingStartDateTime.minus(java.time.Duration.ofMinutes(20)))

        return isNotEnded && canJoinTime
    } catch (e: Exception) {
        android.util.Log.e("SessionsTab", "Error in canJoinSession for booking ${booking.id}: ${e.message}", e)
        return false
    }
}

private fun isSessionStartingSoon(booking: Booking): Boolean {
    if (booking.status != BookingStatus.CONFIRMED) return false

    try {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val bookingDate = LocalDate.parse(booking.date)

        // Parse times - handle both "H:mm" and "HH:mm" formats
        val startTime = try {
            LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("H:mm"))
        }

        val endTime = try {
            LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("H:mm"))
        }

        // ✅ Handle sessions that span midnight
        val endDate = if (endTime.isBefore(startTime) || endTime == startTime) {
            bookingDate.plusDays(1)
        } else {
            bookingDate
        }

        val bookingStartDateTime = bookingDate.atTime(startTime).atZone(zoneId).toInstant()
        val bookingEndDateTime = endDate.atTime(endTime).atZone(zoneId).toInstant()

        // Starting soon only if:
        // 1. Session hasn't ended yet
        // 2. Within 30 minutes before start to end time
        val isNotEnded = now.isBefore(bookingEndDateTime)
        val isWithinStartWindow = now.isAfter(bookingStartDateTime.minus(java.time.Duration.ofMinutes(30)))

        return isNotEnded && isWithinStartWindow
    } catch (e: Exception) {
        android.util.Log.e("SessionsTab", "Error in isSessionStartingSoon for booking ${booking.id}: ${e.message}", e)
        return false
    }
}

@Composable
fun SessionsTab(
    bookings: List<Booking>,
    onJoinSession: (String) -> Unit = {},
    onViewDetail: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tất cả phiên tư vấn", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text("Quản lý và theo dõi tất cả các phiên tư vấn của bạn", color = Color.White.copy(.7f))
    }
    Spacer(Modifier.height(12.dp))

    // ✅ NEW SORTING LOGIC:
    // 1. CONFIRMED sessions that haven't ended yet (upcoming) - sorted by date/time ascending
    // 2. Other sessions - sorted by date/time descending
    val now = Instant.now()
    val zoneId = ZoneId.systemDefault()

    val (upcomingConfirmed, otherSessions) = remember(bookings) {
        val upcoming = mutableListOf<Booking>()
        val others = mutableListOf<Booking>()

        bookings.forEach { booking ->
            if (booking.status == BookingStatus.CONFIRMED) {
                try {
                    val bookingDate = LocalDate.parse(booking.date)

                    // Parse times - handle both formats
                    val startTime = try {
                        LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        LocalTime.parse(booking.startTime, DateTimeFormatter.ofPattern("H:mm"))
                    }

                    val endTime = try {
                        LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        LocalTime.parse(booking.endTime, DateTimeFormatter.ofPattern("H:mm"))
                    }

                    // ✅ Handle sessions that span midnight
                    val endDate = if (endTime.isBefore(startTime) || endTime == startTime) {
                        bookingDate.plusDays(1)
                    } else {
                        bookingDate
                    }

                    val bookingEndDateTime = endDate.atTime(endTime).atZone(zoneId).toInstant()

                    if (bookingEndDateTime.isAfter(now)) {
                        upcoming.add(booking)
                    } else {
                        others.add(booking)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SessionsTab", "Error parsing booking ${booking.id}: ${e.message}", e)
                    others.add(booking)
                }
            } else {
                others.add(booking)
            }
        }

        // Sort upcoming by date/time ascending (soonest first)
        upcoming.sortWith(compareBy<Booking> { it.date }.thenBy { it.startTime })

        // Sort others by date/time descending (newest first)
        others.sortWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })

        upcoming to others
    }

    val allSorted = upcomingConfirmed + otherSessions

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        allSorted.forEach { b ->
            val topic = b.topic ?: "Phiên tư vấn"
            val menteeLabel = menteeDisplayName(b)

            // ✅ Check if CONFIRMED session has actually ended -> display as COMPLETED
            val isSessionEnded = if (b.status == BookingStatus.CONFIRMED) {
                try {
                    val bookingDate = LocalDate.parse(b.date)
                    val endTime = try {
                        LocalTime.parse(b.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        LocalTime.parse(b.endTime, DateTimeFormatter.ofPattern("H:mm"))
                    }
                    val startTime = try {
                        LocalTime.parse(b.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        LocalTime.parse(b.startTime, DateTimeFormatter.ofPattern("H:mm"))
                    }

                    // Handle midnight crossover
                    val endDate = if (endTime.isBefore(startTime) || endTime == startTime) {
                        bookingDate.plusDays(1)
                    } else {
                        bookingDate
                    }

                    val bookingEndDateTime = endDate.atTime(endTime).atZone(zoneId).toInstant()
                    now.isAfter(bookingEndDateTime)
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }

            val (statusColor, statusLabel) = when {
                // ✅ Override: CONFIRMED but ended -> show as COMPLETED
                b.status == BookingStatus.CONFIRMED && isSessionEnded ->
                    Color(0xFF14B8A6) to "Đã hoàn thành"
                b.status == BookingStatus.CONFIRMED ->
                    Color(0xFF22C55E) to "Đã xác nhận"
                b.status == BookingStatus.PENDING_MENTOR ->
                    Color(0xFFF59E0B) to "Chờ duyệt"
                b.status == BookingStatus.COMPLETED ->
                    Color(0xFF14B8A6) to "Hoàn thành"
                b.status == BookingStatus.NO_SHOW_MENTOR ->
                    Color(0xFFF97316) to "No-show mentor"
                b.status == BookingStatus.NO_SHOW_MENTEE ->
                    Color(0xFFF97316) to "No-show mentee"
                b.status == BookingStatus.NO_SHOW_BOTH ->
                    Color(0xFFF97316) to "No-show cả hai"
                b.status == BookingStatus.CANCELLED ->
                    Color(0xFFEF4444) to "Đã hủy"
                b.status == BookingStatus.PAYMENT_PENDING ->
                    Color(0xFFF59E0B) to "Chờ thanh toán"
                b.status == BookingStatus.FAILED ->
                    Color(0xFFEF4444) to "Thanh toán thất bại"
                b.status == BookingStatus.DECLINED ->
                    Color(0xFFEF4444) to "Từ chối"
                else ->
                    Color(0xFF22C55E) to "Đã xác nhận"
            }
            val (payColor, payLabel) = when (b.status) {
                BookingStatus.PAYMENT_PENDING -> Color(0xFFF59E0B) to "Chờ thanh toán"
                BookingStatus.FAILED, BookingStatus.DECLINED -> Color(0xFFEF4444) to "Thanh toán thất bại"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Đã hủy"
                BookingStatus.NO_SHOW_MENTOR,
                BookingStatus.NO_SHOW_MENTEE,
                BookingStatus.NO_SHOW_BOTH -> Color(0xFFF97316) to "No-show"
                else -> Color(0xFF22C55E) to "Đã thanh toán"
            }

            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(topic, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Mentee: $menteeLabel", color = Color.White.copy(.85f), style = MaterialTheme.typography.bodySmall)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(.25f))
                                .border(BorderStroke(1.dp, statusColor.copy(.45f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) { Text(statusLabel, color = Color.White) }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("Ngày & giờ", "${b.date} • ${b.startTime}", Modifier.weight(1f))
                        InfoChip("Thời lượng", "${durationMinutes(b.startTime, b.endTime)} phút", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("Giá tư vấn", "${b.price.toInt()} đ", Modifier.weight(1f))
                        InfoChip("Mentee", menteeLabel, Modifier.weight(1f))
                    }

                    val policies = policyRowsFor(b)
                    if (policies.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            policies.forEach { (title, value) ->
                                InfoRow(title, value)
                            }
                        }
                    }

                    LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Thanh toán", color = Color.White, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(payColor.copy(.25f))
                                    .border(BorderStroke(1.dp, payColor.copy(.45f)), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) { Text(payLabel, color = Color.White) }
                        }
                    }

                    // ✅ Add Join Session and View Detail buttons for CONFIRMED sessions
                    // BUT: Only show Join button if session hasn't ended yet
                    if (b.status == BookingStatus.CONFIRMED && !isSessionEnded) {
                        val canJoin = canJoinSession(b)
                        val isStartingSoon = isSessionStartingSoon(b)

                        // Show "Starting Soon" indicator if applicable
                        if (isStartingSoon) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF472B6).copy(0.2f))
                                    .border(1.dp, Color(0xFFF472B6).copy(0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "⚡ Phiên học sắp bắt đầu",
                                    color = Color(0xFFF472B6),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MMButton(
                                text = "Vào cuộc",
                                onClick = { onJoinSession(b.id) },
                                enabled = canJoin,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Outlined.Videocam, null, Modifier.size(20.dp))
                                }
                            )
                            MMGhostButton(
                                onClick = { onViewDetail(b.id) },
                                modifier = Modifier.weight(1f),
                                content = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Info, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Chi tiết")
                                    }
                                }
                            )
                        }
                    } else {
                        // For sessions that ended or non-CONFIRMED, only show View Detail button
                        MMGhostButton(
                            onClick = { onViewDetail(b.id) },
                            modifier = Modifier.fillMaxWidth(),
                            content = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Info, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Xem chi tiết")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
