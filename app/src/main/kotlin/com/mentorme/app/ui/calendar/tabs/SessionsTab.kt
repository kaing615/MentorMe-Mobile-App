package com.mentorme.app.ui.calendar.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.core.durationMinutes
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun SessionsTab(bookings: List<Booking>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💬  Tất cả phiên tư vấn", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text("Quản lý và theo dõi tất cả các phiên tư vấn của bạn", color = Color.White.copy(.7f))
    }
    Spacer(Modifier.height(12.dp))

    val all = remember(bookings) {
        bookings.sortedWith(compareByDescending<Booking> { it.date }.thenByDescending { it.startTime })
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        all.forEach { b ->
            val extra = MockData.bookingExtras[b.id]
            val topic = extra?.topic ?: "Phiên tư vấn"
            val sessionType = extra?.sessionType ?: "video"
            val isPaid = extra?.paymentStatus == "paid"
            val menteeName = MockData.currentMenteeName
            val mentorName = MockData.mentorNameById(b.mentorId)

            val (statusColor, statusLabel) = when (b.status) {
                BookingStatus.CONFIRMED -> Color(0xFF22C55E) to "✅ Đã xác nhận"
                BookingStatus.PENDING   -> Color(0xFFF59E0B) to "⏳ Chờ duyệt"
                BookingStatus.COMPLETED -> Color(0xFF14B8A6) to "🎉 Hoàn thành"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "❌ Đã hủy"
                BookingStatus.PAYMENT_PENDING -> TODO()
                BookingStatus.FAILED -> TODO()
            }

            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text((if (sessionType == "in-person") "🤝 " else "💻 ") + topic,
                                color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("👤 Với $menteeName   •   👨‍🏫 $mentorName",
                                color = Color.White.copy(.85f), style = MaterialTheme.typography.bodySmall)
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
                        InfoChip("📅 Ngày & giờ", "${b.date} • ${b.startTime}", Modifier.weight(1f))
                        InfoChip("⏱️ Thời lượng", "${durationMinutes(b.startTime, b.endTime)} phút", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("💎 Giá tư vấn", "${b.price.toInt()} đ", Modifier.weight(1f))
                        InfoChip("🎯 Hình thức", if (sessionType == "in-person") "🤝 Trực tiếp" else "💻 Video Call", Modifier.weight(1f))
                    }

                    LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💳 Thanh toán", color = Color.White, modifier = Modifier.weight(1f))
                            val (payColor, payLabel) = if (isPaid)
                                Color(0xFF22C55E) to "✅ Đã thanh toán"
                            else Color(0xFFF59E0B) to "⏳ Chờ thanh toán"

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
