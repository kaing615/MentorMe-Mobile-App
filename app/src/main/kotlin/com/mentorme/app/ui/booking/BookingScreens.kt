@file:OptIn(ExperimentalLayoutApi::class)

package com.mentorme.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.*
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.mentorme.app.ui.components.ui.MMPrimaryButton

/* ---------- Models & Helpers ---------- */

data class BookingDraft(
    val mentorId: String,
    val occurrenceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val priceVnd: Long,
    val notes: String = ""
)

private fun toIsoNowUtc(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}

private fun formatVnd(amount: Long): String {
    val nf = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return nf.format(amount)
}

private fun buildBookingWebSchema(draft: BookingDraft, menteeId: String): Booking {
    return Booking(
        id = System.currentTimeMillis().toString(),
        menteeId = menteeId,
        mentorId = draft.mentorId,
        date = draft.date,
        startTime = draft.startTime,
        endTime = draft.endTime,
        status = BookingStatus.CONFIRMED,
        price = draft.priceVnd.toDouble(),
        notes = draft.notes.ifBlank { null },
        createdAt = toIsoNowUtc()
    )
}

/* ---------- Reusable LiquidGlass UI ---------- */

@Composable private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable private fun ChipRowHomeStyle(
    options: List<String>, selected: String, onSelect: (String) -> Unit, emoji: String = "📅"
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            LiquidGlassCard(
                modifier = Modifier.height(50.dp).clickable { onSelect(opt) },
                radius = 22.dp,
                alpha = if (selected == opt) 0.25f else 0.15f,
                borderAlpha = if (selected == opt) 0.4f else 0.3f
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(emoji, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Text(
                        opt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = if (selected == opt) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable private fun DurationRowHomeStyle(values: List<Int>, value: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        values.forEach { d ->
            LiquidGlassCard(
                modifier = Modifier.height(50.dp).clickable { onSelect(d) },
                radius = 22.dp,
                alpha = if (value == d) 0.25f else 0.15f,
                borderAlpha = if (value == d) 0.4f else 0.3f
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        if (d == 60) "1 giờ" else "$d phút",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = if (value == d) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable private fun PriceCardHomeStyle(priceVnd: Long) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 24.dp) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Tổng chi phí",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Divider(color = Color.White.copy(alpha = 0.3f))
            Text(
                formatVnd(priceVnd),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E)
            )
            Text(
                "Giá này do mentor thiết lập cho khung giờ này.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable private fun LiquidGlassTextFieldHomeStyle(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, minLines: Int = 1
) {
    LiquidGlassCard(modifier = modifier, radius = 24.dp) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color.White
            )
        )
    }
}

/* ---------- Screens ---------- */

@Composable
fun BookingChooseTimeScreen(
    mentor: Mentor,
    slots: List<BookingFlowViewModel.TimeSlotUi>,
    loading: Boolean,
    errorMessage: String?,
    onNext: (BookingDraft) -> Unit,
    onClose: (() -> Unit)? = null
) {
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .windowInsetsPadding(
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            )
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Đặt lịch tư vấn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                onClose?.let { IconButton(onClick = it) { Icon(Icons.Default.Close, null, tint = Color.White) } }
            }
        }

        item {
            LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("👋", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Column {
                        Text("${mentor.fullName} • ${mentor.experience}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Giá theo lịch đã đặt", color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item { SectionTitle("Chọn khung giờ") }

        when {
            loading -> {
                item { Text("Đang tải lịch trống...", color = Color.White.copy(alpha = 0.85f)) }
            }
            !errorMessage.isNullOrBlank() -> {
                item { Text(errorMessage, color = Color.White.copy(alpha = 0.85f)) }
            }
            slots.isEmpty() -> {
                item { Text("Chưa có lịch trống trong 30 ngày tới", color = Color.White.copy(alpha = 0.85f)) }
            }
            else -> {
                items(slots) { slot ->
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("${slot.date} • ${slot.startTime} - ${slot.endTime}", color = Color.White)
                            Text(formatVnd(slot.priceVnd), color = Color.White.copy(alpha = 0.85f))
                            MMPrimaryButton(onClick = {
                                onNext(
                                    BookingDraft(
                                        mentorId = mentor.id,
                                        occurrenceId = slot.occurrenceId,
                                        date = slot.date,
                                        startTime = slot.startTime,
                                        endTime = slot.endTime,
                                        priceVnd = slot.priceVnd,
                                        notes = notes
                                    )
                                )
                            }) { Text("Đặt lịch") }
                        }
                    }
                }
            }
        }

        item { SectionTitle("Ghi chú (tùy chọn)") }
        item { LiquidGlassTextFieldHomeStyle(notes, { notes = it }, "Nhập ghi chú cho mentor...", Modifier.fillMaxWidth(), 3) }
    }
}

@Composable
fun BookingSummaryScreen(
    mentor: Mentor,
    draft: BookingDraft,
    currentUserId: String,
    onConfirmed: (Booking) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Text("Xác nhận đặt lịch", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White)
                    Column {
                        Text(mentor.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${draft.date} • ${draft.startTime} - ${draft.endTime}", color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item { PriceCardHomeStyle(draft.priceVnd) }

        if (draft.notes.isNotBlank()) {
            item {
                LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Ghi chú của bạn",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(draft.notes, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary button: Confirm payment (Professional Blue)
                Button(
                    onClick = {
                        val booking = buildBookingWebSchema(draft, currentUserId)
                        onConfirmed(booking)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Xác nhận đặt lịch",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Secondary button: Back (Glass style)
                Surface(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "Quay lại",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingsScreenSimple(bookings: List<Booking>, onOpen: (Booking) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp)
    ) {
        item { Text("Lịch đặt của tôi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White) }

        items(bookings) { booking ->
            LiquidGlassCard(modifier = Modifier.fillMaxWidth().clickable { onOpen(booking) }, radius = 22.dp) {
                Row(Modifier.fillMaxSize().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📚", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Column(Modifier.weight(1f)) {
                        Text("Mentor Session", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${booking.date} • ${booking.startTime}–${booking.endTime}", color = Color.White.copy(alpha = 0.85f))
                        Text("Giá: ${formatVnd(booking.price.toLong())}", color = Color.White.copy(alpha = 0.85f))
                    }
                    StatusChipHomeStyle(booking.status)
                }
            }
        }
    }
}

@Composable
private fun StatusChipHomeStyle(status: BookingStatus) {
    val (color, text, emoji) = when (status) {
        BookingStatus.PAYMENT_PENDING -> Triple(Color(0xFFF59E0B), "Payment Pending", "💳")
        BookingStatus.PENDING_MENTOR   -> Triple(Color(0xFF3B82F6), "Pending", "⏳")
        BookingStatus.CONFIRMED -> Triple(Color(0xFF10B981), "Confirmed", "✅")
        BookingStatus.COMPLETED -> Triple(Color(0xFF8B5CF6), "Completed", "🎉")
        BookingStatus.NO_SHOW_MENTOR -> Triple(Color(0xFFF97316), "No-show mentor", "⚠️")
        BookingStatus.NO_SHOW_MENTEE -> Triple(Color(0xFFF97316), "No-show mentee", "⚠️")
        BookingStatus.NO_SHOW_BOTH -> Triple(Color(0xFFF97316), "No-show both", "⚠️")
        BookingStatus.CANCELLED -> Triple(Color(0xFFEF4444), "Cancelled", "❌")
        BookingStatus.DECLINED  -> Triple(Color(0xFFEF4444), "Declined", "🚫")
        BookingStatus.FAILED    -> Triple(Color(0xFFDC2626), "Failed", "⚠️")
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji)
            Text(text, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}


