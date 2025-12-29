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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.components.InfoRow
import com.mentorme.app.ui.calendar.core.durationMinutes
import com.mentorme.app.core.time.formatIsoToLocalShort
import com.mentorme.app.ui.theme.LiquidGlassCard

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

@Composable
fun SessionsTab(bookings: List<Booking>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tất cả phiên tư vấn", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text("Quản lý và theo dõi tất cả các phiên tư vấn của bạn", color = Color.White.copy(.7f))
    }
    Spacer(Modifier.height(12.dp))

    val all = remember(bookings) {
        bookings.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        all.forEach { b ->
            val topic = b.topic ?: "Phiên tư vấn"
            val menteeLabel = menteeDisplayName(b)

            val (statusColor, statusLabel) = when (b.status) {
                BookingStatus.CONFIRMED -> Color(0xFF22C55E) to "Đã xác nhận"
                BookingStatus.PENDING_MENTOR -> Color(0xFFF59E0B) to "Chờ duyệt"
                BookingStatus.COMPLETED -> Color(0xFF14B8A6) to "Hoàn thành"
                BookingStatus.NO_SHOW_MENTOR -> Color(0xFFF97316) to "No-show mentor"
                BookingStatus.NO_SHOW_MENTEE -> Color(0xFFF97316) to "No-show mentee"
                BookingStatus.NO_SHOW_BOTH -> Color(0xFFF97316) to "No-show cả hai"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Đã hủy"
                BookingStatus.PAYMENT_PENDING -> Color(0xFFF59E0B) to "Chờ thanh toán"
                BookingStatus.FAILED -> Color(0xFFEF4444) to "Thanh toán thất bại"
                BookingStatus.DECLINED -> Color(0xFFEF4444) to "Từ chối"
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
                }
            }
        }
    }
}
