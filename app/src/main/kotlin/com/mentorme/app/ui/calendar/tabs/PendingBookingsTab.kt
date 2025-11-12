package com.mentorme.app.ui.calendar.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.calendar.components.InfoRow
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.liquidGlass
import kotlinx.coroutines.launch

private fun durationMinutes(start: String, end: String): Int {
    fun toMin(hhmm: String) = hhmm.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
    return toMin(end) - toMin(start)
}

@Composable
fun PendingBookingsTab(
    bookings: List<Booking>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    // === Snackbar host (notify) ===
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Lọc + sort các booking chờ duyệt
    val pending = remember(bookings) {
        bookings.filter { it.status == BookingStatus.PENDING }
            .sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    // Mình bọc toàn bộ tab trong Box để có chỗ đặt SnackbarHost
    Box(modifier = Modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏱️  Booking chờ duyệt",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Các yêu cầu đặt lịch từ mentee đang chờ bạn phản hồi",
                color = Color.White.copy(0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // === EMPTY STATE ===
            if (pending.isEmpty()) {
                LiquidGlassCard(
                    radius = 22.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Không padding “ăn” vào nội dung, chỉ spacing nhẹ bên trong Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp) // cao tối thiểu để nhìn cân đối
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Biểu tượng / avatar tròn nhẹ như figma (tuỳ thay thế Icon của bạn)
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🕒", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "⏳ Không có booking chờ duyệt",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tất cả các yêu cầu đặt lịch đã được xử lý. Hãy kiểm tra lại sau!",
                                color = Color.White.copy(.8f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                return@Column
            }

            // === LIST CÁC BOOKING CHỜ DUYỆT ===
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pending.forEach { b ->
                    val extra = MockData.bookingExtras[b.id]
                    val topic = extra?.topic ?: "Booking"
                    val sessionType = extra?.sessionType ?: "video"
                    val isPaid = extra?.paymentStatus == "paid"
                    val menteeName = MockData.currentMenteeName
                    val mentorName = MockData.mentorNameById(b.mentorId)

                    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "📝 $topic",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "👤 Với $menteeName   •   👨‍🏫 $mentorName",
                                        color = Color.White.copy(.85f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                val pillColor = Color(0xFFF59E0B)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(pillColor.copy(.25f))
                                        .border(BorderStroke(1.dp, pillColor.copy(.45f)), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) { Text("⏳ Chờ duyệt", color = Color.White, fontWeight = FontWeight.Medium) }
                            }

                            // Info
                            InfoRow("📅 Ngày & giờ", "${b.date} • ${b.startTime}")
                            InfoRow("⏱️ Thời lượng", "${durationMinutes(b.startTime, b.endTime)} phút")
                            InfoRow("💎 Giá tư vấn", "${b.price.toInt()} đ")
                            InfoRow(
                                "🎯 Hình thức",
                                if (sessionType == "in-person") "🤝 Trực tiếp" else "💻 Video Call"
                            )

                            // Payment
                            LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Trạng thái thanh toán",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Mentee đã hoàn tất thanh toán",
                                            color = Color.White.copy(.7f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    val (bg, label) = if (isPaid)
                                        Color(0xFF22C55E) to "✅ Đã thanh toán"
                                    else
                                        Color(0xFFF59E0B) to "⏳ Chờ thanh toán"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bg.copy(.25f))
                                            .border(BorderStroke(1.dp, bg.copy(.45f)), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) { Text(label, color = Color.White) }
                                }
                            }

                            // Actions
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MMButton(
                                    text = "✅ Chấp nhận booking",
                                    onClick = {
                                        onAccept(b.id)
                                        // Hiện notify
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã chấp nhận booking!")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                                MMGhostButton(
                                    text = "❌ Từ chối",
                                    onClick = { onReject(b.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar host (đặt cuối Box để nổi trên cùng)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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