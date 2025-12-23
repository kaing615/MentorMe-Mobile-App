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
import com.mentorme.app.ui.theme.LiquidGlassCard

private fun formatIsoShort(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val cleaned = iso.trim().replace("T", " ").removeSuffix("Z")
    return if (cleaned.length >= 16) cleaned.substring(0, 16) else cleaned
}

private fun policyRowsFor(booking: Booking): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    val mentorDeadline = formatIsoShort(booking.mentorResponseDeadline)
    val payExpires = formatIsoShort(booking.expiresAt)
    val reminder24h = formatIsoShort(booking.reminder24hSentAt)
    val reminder1h = formatIsoShort(booking.reminder1hSentAt)

    if (!mentorDeadline.isNullOrBlank()) rows.add("Mentor deadline" to mentorDeadline)
    if (!payExpires.isNullOrBlank()) rows.add("Pay expires" to payExpires)
    if (!reminder24h.isNullOrBlank()) rows.add("Reminder 24h" to reminder24h)
    if (!reminder1h.isNullOrBlank()) rows.add("Reminder 1h" to reminder1h)
    if (booking.lateCancel == true) {
        val minutes = booking.lateCancelMinutes?.let { "$it min" } ?: "late"
        rows.add("Late cancel" to minutes)
    }
    return rows
}

@Composable
fun SessionsTab(bookings: List<Booking>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tat ca phien tu van", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text("Quan ly va theo doi tat ca cac phien tu van cua ban", color = Color.White.copy(.7f))
    }
    Spacer(Modifier.height(12.dp))

    val all = remember(bookings) {
        bookings.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        all.forEach { b ->
            val topic = b.topic ?: "Phien tu van"
            val menteeLabel = if (b.menteeId.length > 6) "...${b.menteeId.takeLast(6)}" else b.menteeId

            val (statusColor, statusLabel) = when (b.status) {
                BookingStatus.CONFIRMED -> Color(0xFF22C55E) to "Da xac nhan"
                BookingStatus.PENDING_MENTOR -> Color(0xFFF59E0B) to "Cho duyet"
                BookingStatus.COMPLETED -> Color(0xFF14B8A6) to "Hoan thanh"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Da huy"
                BookingStatus.PAYMENT_PENDING -> Color(0xFFF59E0B) to "Cho thanh toan"
                BookingStatus.FAILED -> Color(0xFFEF4444) to "Thanh toan that bai"
                BookingStatus.DECLINED -> Color(0xFFEF4444) to "Tu choi"
            }
            val (payColor, payLabel) = when (b.status) {
                BookingStatus.PAYMENT_PENDING -> Color(0xFFF59E0B) to "Cho thanh toan"
                BookingStatus.FAILED, BookingStatus.DECLINED -> Color(0xFFEF4444) to "Thanh toan that bai"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Da huy"
                else -> Color(0xFF22C55E) to "Da thanh toan"
            }

            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(topic, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Mentee $menteeLabel", color = Color.White.copy(.85f), style = MaterialTheme.typography.bodySmall)
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
                        InfoChip("Ngay & gio", "${b.date} • ${b.startTime}", Modifier.weight(1f))
                        InfoChip("Thoi luong", "${durationMinutes(b.startTime, b.endTime)} phut", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("Gia tu van", "${b.price.toInt()} d", Modifier.weight(1f))
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
                            Text("Thanh toan", color = Color.White, modifier = Modifier.weight(1f))
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
