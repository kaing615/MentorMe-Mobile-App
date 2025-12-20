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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
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
    val date: String = "",
    val time: String = "",
    val durationMin: Int = 60,
    val notes: String = "",
    val hourlyRate: Double = 0.0
)

data class PriceBreakdown(val subtotal: Double, val tax: Double, val fee: Double, val total: Double)

private fun calcPrice(rate: Double, minutes: Int): PriceBreakdown {
    val sub = rate * minutes / 60.0
    val tax = sub * 0.10
    val fee = 2.99
    return PriceBreakdown(sub, tax, fee, sub + tax + fee)
}

private fun toIsoNowUtc(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}

private fun plusMinutesHHmm(startHHmm: String, minutes: Int): String {
    return try {
        val parts = startHHmm.split(":")
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
        cal.set(Calendar.MINUTE, parts[1].toInt())
        cal.add(Calendar.MINUTE, minutes)
        String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    } catch (e: Exception) {
        "00:00"
    }
}

private fun buildBookingWebSchema(draft: BookingDraft, menteeId: String, price: PriceBreakdown): Booking {
    val end = plusMinutesHHmm(draft.time, draft.durationMin)
    return Booking(
        id = System.currentTimeMillis().toString(),
        menteeId = menteeId,
        mentorId = draft.mentorId,
        date = draft.date,
        startTime = draft.time,
        endTime = end,
        status = BookingStatus.PAYMENT_PENDING,
        price = price.total,
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

@Composable private fun PriceCardHomeStyle(p: PriceBreakdown) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 24.dp) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("💰 Tóm tắt chi phí", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Tạm tính: \$${"%.2f".format(p.subtotal)}", color = Color.White.copy(alpha = 0.85f))
            Text("Thuế (10%): \$${"%.2f".format(p.tax)}", color = Color.White.copy(alpha = 0.85f))
            Text("Phí nền tảng: \$${"%.2f".format(p.fee)}", color = Color.White.copy(alpha = 0.85f))
            Divider(color = Color.White.copy(alpha = 0.3f))
            Text("Tổng: \$${"%.2f".format(p.total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
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
    availableDates: List<String>,
    availableTimes: List<String>,
    onNext: (BookingDraft) -> Unit,
    onClose: (() -> Unit)? = null
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(60) }
    var notes by remember { mutableStateOf("") }
    val price = remember(duration, mentor.hourlyRate) { calcPrice(mentor.hourlyRate, duration) }

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
                    Text("👨‍🏫", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Column {
                        Text("${mentor.fullName} • ${mentor.experience}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Giá: \$${"%.2f".format(mentor.hourlyRate)}/giờ • ${"%.1f".format(mentor.rating)}★", color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item { SectionTitle("Chọn ngày") }
        item { ChipRowHomeStyle(availableDates, date, { date = it }, "📅") }
        item { SectionTitle("Chọn giờ") }
        item { ChipRowHomeStyle(availableTimes, time, { time = it }, "🕐") }
        item { SectionTitle("Thời lượng") }
        item { DurationRowHomeStyle(listOf(30, 60, 90, 120), duration) { duration = it } }
        item { SectionTitle("Ghi chú (tuỳ chọn)") }
        item { LiquidGlassTextFieldHomeStyle(notes, { notes = it }, "Nhập ghi chú cho mentor...", Modifier.fillMaxWidth(), 3) }
        item { PriceCardHomeStyle(price) }

        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MMPrimaryButton(onClick = {
                    if (date.isNotBlank() && time.isNotBlank()) {
                        onNext(BookingDraft(mentor.id, date, time, duration, notes, mentor.hourlyRate))
                    }
                }) { Text("Tiếp tục") }
            }
        }
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
    val price = remember(draft) { calcPrice(draft.hourlyRate, draft.durationMin) }

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
                        Text("${draft.date} • ${draft.time} → ${plusMinutesHHmm(draft.time, draft.durationMin)} • ${draft.durationMin} phút", color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item { PriceCardHomeStyle(price) }

        if (draft.notes.isNotBlank()) {
            item {
                LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📝 Ghi chú của bạn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(draft.notes, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MMPrimaryButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Quay lại") }
                MMPrimaryButton(onClick = {
                    val booking = buildBookingWebSchema(draft, currentUserId, price)
                    onConfirmed(booking)
                }, modifier = Modifier.weight(1f)) { Text("Xác nhận") }
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
                        Text("Price: \$${"%.2f".format(booking.price)}", color = Color.White.copy(alpha = 0.85f))
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
        BookingStatus.PAYMENT_PENDING -> Triple(Color(0xFFF59E0B), "Payment Pending", "⏳")
        BookingStatus.CONFIRMED -> Triple(Color(0xFF10B981), "Confirmed", "✅")
        BookingStatus.COMPLETED -> Triple(Color(0xFF8B5CF6), "Completed", "🎉")
        BookingStatus.CANCELLED -> Triple(Color(0xFFEF4444), "Cancelled", "❌")
        BookingStatus.FAILED -> Triple(Color(0xFFDC2626), "Failed", "⚠️")
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
