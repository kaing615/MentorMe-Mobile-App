package com.mentorme.app.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

/* ---- OPTIONAL: nếu muốn show tên/avatar mentor ----
 * Truyền vào hàm getMentorBrief để map từ mentorId -> (name, avatarUrl)
 */
data class MentorBrief(val name: String, val avatarUrl: String? = null)

@Composable
fun BookingsTabScreen(
    bookings: List<Booking>,
    onConfirm: (Booking) -> Unit,
    onCancel: (Booking) -> Unit,
    onOpen: (Booking) -> Unit,
    getMentorBrief: (String) -> MentorBrief? = { null }
) {
    val tabs = listOf(
        BookingStatus.CONFIRMED to "Sắp tới",
        BookingStatus.PAYMENT_PENDING to "Chờ thanh toán",
        BookingStatus.COMPLETED to "Hoàn thành",
        BookingStatus.CANCELLED to "Đã hủy",
    )

    var selectedIdx by remember { mutableIntStateOf(0) }
    val selectedStatus = tabs[selectedIdx].first

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // segmented tabs trong glass-card
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            TabRow(
                selectedTabIndex = selectedIdx,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(positions[selectedIdx])
                            .padding(horizontal = 28.dp),
                        color = Color.White.copy(alpha = .85f),
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { i, (_, label) ->
                    Tab(
                        selected = selectedIdx == i,
                        onClick = { selectedIdx = i },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f),
                        text = {
                            Text(
                                label,
                                fontWeight = if (selectedIdx == i) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val filtered = remember(bookings, selectedStatus) {
            bookings.filter { it.status == selectedStatus }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filtered, key = { it.id }) { bk ->
                BookingItemCard(
                    booking = bk,
                    brief = getMentorBrief(bk.mentorId),
                    onConfirm = { onConfirm(bk) },
                    onCancel = { onCancel(bk) },
                    onOpen = { onOpen(bk) }
                )
            }
        }
    }
}

@Composable
private fun BookingItemCard(
    booking: Booking,
    brief: MentorBrief?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onOpen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val mentorName = listOf(
        brief?.name,
        booking.mentorFullName,
        booking.mentor?.fullName
    ).firstOrNull { !it.isNullOrBlank() }?.trim()
        ?: "Mentor #${booking.mentorId.takeLast(4)}"

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        radius = 22.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Row 1: tên mentor + thời gian + nút xác nhận
            Row(verticalAlignment = Alignment.CenterVertically) {

                // ô avatar glass (placeholder emoji cho gọn)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .liquidGlass(radius = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤")
                }

                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        text = mentorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${booking.date} • ${booking.startTime}–${booking.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // nút chính: dùng MMPrimaryButton (không có tham số compact)
                MMPrimaryButton(onClick = onConfirm) { Text("Xác nhận") }
            }

            // Row 2: thời lượng (tính từ start–end) + giá
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Thời lượng: ${durationMinutes(booking.startTime, booking.endTime)} phút",
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Giá: \$${"%.2f".format(booking.price)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Row 3: “Chưa tới giờ” (expand) + Hủy (ghost)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text(if (expanded) "▼ Chưa tới giờ" else "▶ Chưa tới giờ")
                }

                Spacer(Modifier.weight(1f))

                // ghost button: OutlinedButton viền trắng mờ
                OutlinedButton(
                    onClick = onCancel,
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f) // ✅ Sử dụng Color thay vì Brush
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Hủy") }
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Text(
                        "Bạn sẽ nhận nhắc hẹn trước giờ bắt đầu 15 phút.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/* Helpers */

private fun durationMinutes(start: String, end: String): Int {
    // start/end dạng "HH:mm"
    return try {
        val (sh, sm) = start.split(":").map { it.toInt() }
        val (eh, em) = end.split(":").map { it.toInt() }
        val diff = (eh * 60 + em) - (sh * 60 + sm)
        if (diff < 0) diff + 24 * 60 else diff
    } catch (_: Exception) { 0 }
}
