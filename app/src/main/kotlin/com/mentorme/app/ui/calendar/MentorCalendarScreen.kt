package com.mentorme.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.input.TextFieldValue
import java.text.NumberFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.components.ui.MMButton
import java.util.Calendar
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
// MMButton + size enum (đúng package của repo bạn)
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMButtonSize

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
//import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import com.mentorme.app.ui.common.MMGhostButton

//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.foundation.layout.only
//import androidx.compose.foundation.layout.windowInsetsPadding

// ======= TAB ENUM =======
private enum class MentorTab(val label: String) {
    Availability("📅 Lịch trống"),
    Bookings("📋 Booking"),
    Sessions("💬 Phiên học")
}

private fun hhmmToMinutes(hhmm: String) =
    hhmm.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
private fun durationMinutes(start: String, end: String) =
    hhmmToMinutes(end) - hhmmToMinutes(start)

// ======= SCREEN =======
@Composable
fun MentorCalendarScreen(
    onViewSession: (String) -> Unit = {},
    onCreateSession: () -> Unit = {},
    onUpdateAvailability: () -> Unit = {},
    onCancelSession: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(MentorTab.Availability) }
    val bookings = remember { MockData.mockBookings }

    // Insets: top = status bar (cuộn cùng nội dung), bottom = nav + dashboard
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dashboardHeight = 88.dp
    val bottomPadding = bottomInset + dashboardHeight

    Column(
        modifier = modifier
            .fillMaxSize()
            // Không cố định top; cho top padding theo status bar để cuộn cùng
            .padding(top = topInset, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Quản lý lịch Mentor",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Quản lý lịch trống và các buổi hẹn với mentee",
            color = Color.White.copy(0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))

        // Stats (dùng price & status sẵn có)
        val completedEarnings = bookings
            .filter { it.status == BookingStatus.COMPLETED }
            .sumOf { it.price.toLong() }.toInt()
        val pendingEarnings = bookings
            .filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED }
            .sumOf { it.price.toLong() }.toInt()

        StatsOverview(
            availabilityOpen = 0,
            confirmedCount = bookings.count { it.status == BookingStatus.CONFIRMED },
            totalPaid = completedEarnings,
            totalPending = pendingEarnings
        )

        Spacer(Modifier.height(10.dp))

        // Tabs
        SegmentedTabs(
            active = activeTab,
            pendingCount = bookings.count { it.status == BookingStatus.PENDING },
            onChange = { activeTab = it },
        )

        Spacer(Modifier.height(12.dp))

        // Tab content
        when (activeTab) {
            MentorTab.Availability -> {
                AvailabilityTabSection(
                    onEdit = { },
                    onToggle = { },
                    onDelete = { }
                )
            }
            MentorTab.Bookings -> {
                PendingBookingsTab(bookings = bookings)
            }
            MentorTab.Sessions -> {
                SessionsTab(bookings = bookings)
            }
        }

        // chừa chỗ đáy để né dashboard
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun CenteredPill(
    text: String,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg.copy(.25f))
            .border(BorderStroke(1.dp, bg.copy(.45f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
private fun AvailabilityTabSection(
    onEdit: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    // ---- mock giống TSX ----
    data class AvailabilitySlot(
        val id: String,
        val date: String,      // YYYY-MM-DD
        val startTime: String, // HH:MM
        val endTime: String,   // HH:MM
        val duration: Int,     // minutes
        val description: String?,
        val isActive: Boolean,
        val sessionType: String, // "video" | "in-person"
        val isBooked: Boolean
    )
    val numberFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")) }
    val HOURLY = 100_000

    var slots by remember {
        mutableStateOf(
            listOf(
                AvailabilitySlot("1","2024-01-15","09:00","10:00",60,"React/NextJS Consultation", true,"video", true),
                AvailabilitySlot("2","2024-01-16","14:00","15:30",90,"System Design & Architecture", true,"in-person", false),
                AvailabilitySlot("3","2024-01-17","10:30","11:30",60,"Career Guidance", true,"video", false),
            )
        )
    }

    // ---- dialog state ----
    var showAdd by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("video") }
    var desc by remember { mutableStateOf(TextFieldValue("")) }
    var typeMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Header + nút ✨ Thêm lịch (giống TSX)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3348A6FF))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White) }

                Spacer(Modifier.width(8.dp))
                Text(
                    "📅 Lịch trống của bạn",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.weight(1f))
            MMPrimaryButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("✨ Thêm lịch", color = Color.White)
            }
        }

        // Empty state
        if (slots.isEmpty()) {
            LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x3348A6FF)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White.copy(.7f)) }
                    Spacer(Modifier.height(8.dp))
                    Text("📅 Chưa có lịch trống", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Hãy thêm lịch trống để mentee có thể đặt hẹn tư vấn cá nhân với bạn!",
                        color = Color.White.copy(.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // KHÔNG dùng LazyColumn con -> tránh nested scroll
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                slots.forEach { slot ->
                    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (slot.sessionType == "video") Color(0x332467F1) else Color(0x3322C55E)),
                                    contentAlignment = Alignment.Center
                                ) { Text(if (slot.sessionType == "video") "💻" else "🤝") }

                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = slot.description ?: "Phiên ${if (slot.sessionType=="video") "Video Call" else "Trực tiếp"}",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "📅 ${slot.date}  •  ${slot.startTime} - ${slot.endTime}",
                                        color = Color.White.copy(.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                val badgeBg = when {
                                    !slot.isActive -> Color(0xFF6B7280)
                                    slot.isBooked  -> Color(0xFFEF4444)
                                    else           -> Color(0xFF22C55E)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badgeBg.copy(.25f))
                                        .border(BorderStroke(1.dp, badgeBg.copy(.45f)), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        when {
                                            !slot.isActive -> "⏸️ Tạm dừng"
                                            slot.isBooked  -> "📅 Đã đặt"
                                            else           -> "✨ Còn trống"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip("⏱️ Thời lượng", "${slot.duration} phút", Modifier.weight(1f))
                                    InfoChip("💎 Giá tư vấn", numberFormat.format((HOURLY * slot.duration) / 60), Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip("🎯 Hình thức", if (slot.sessionType=="video") "💻 Video Call" else "🤝 Trực tiếp", Modifier.weight(1f))
                                    InfoChip("📊 Trạng thái", if (slot.isBooked) "📅 Đã đặt" else "✨ Trống", Modifier.weight(1f))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val canDelete = !slot.isBooked
                                MMButton(text = "✏️ Sửa", onClick = { onEdit(slot.id) }, size = MMButtonSize.Compact)
                                MMButton(
                                    text = if (slot.isActive) "⏸️ Tạm dừng" else "▶️ Kích hoạt",
                                    onClick = { onToggle(slot.id) },
                                    size = MMButtonSize.Compact
                                )
                                MMButton(
                                    text = "🗑️ Xóa",
                                    onClick = { if (canDelete) onDelete(slot.id) },
                                    size = MMButtonSize.Compact,
                                    modifier = if (canDelete) Modifier else Modifier.alpha(0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- Dialog thêm lịch ----
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                TextButton(onClick = {
                    val st = runCatching { java.time.LocalTime.parse(start) }.getOrNull()
                    val et = runCatching { java.time.LocalTime.parse(end) }.getOrNull()
                    if (date.isBlank() || st == null || et == null) { showAdd = false; return@TextButton }
                    val duration = java.time.Duration.between(st, et).toMinutes().toInt().coerceAtLeast(0)
                    if (duration < 30) { showAdd = false; return@TextButton }

                    val newSlot = AvailabilitySlot(
                        id = System.currentTimeMillis().toString(),
                        date = date,
                        startTime = start,
                        endTime = end,
                        duration = duration,
                        description = desc.text.ifBlank { null },
                        isActive = true,
                        sessionType = type,
                        isBooked = false
                    )
                    slots = slots + newSlot
                    date = ""; start = ""; end = ""; type = "video"; desc = TextFieldValue("")
                    showAdd = false
                }) { Text("✨ Thêm lịch") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("❌ Hủy") } },
            title = { Text("✨ Thêm lịch trống mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("📅 Ngày (YYYY-MM-DD)") })
                    Box {
                        OutlinedTextField(
                            value = if (type == "video") "💻 Video Call" else "🤝 Trực tiếp",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("🎯 Loại phiên") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass()
                                .clickable { typeMenu = true }
                        )
                        DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                            DropdownMenuItem(text = { Text("💻 Video Call") }, onClick = { type = "video"; typeMenu = false })
                            DropdownMenuItem(text = { Text("🤝 Trực tiếp") }, onClick = { type = "in-person"; typeMenu = false })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = start, onValueChange = { start = it },
                            label = { Text("🕐 Giờ bắt đầu (HH:MM)") }, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = end, onValueChange = { end = it },
                            label = { Text("🕐 Giờ kết thúc (HH:MM)") }, modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = desc, onValueChange = { desc = it },
                        label = { Text("📝 Mô tả (tùy chọn)") }, modifier = Modifier.fillMaxWidth(), minLines = 3
                    )
                    val numberFormat = NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
                    val HOURLY = 100_000
                    LiquidGlassCard(radius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("💎 Giá niêm yết", color = Color.White, fontWeight = FontWeight.Medium)
                            Text("${numberFormat.format(HOURLY)}/giờ", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            Text("ℹ️ Giá tự động tính theo thời lượng phiên tư vấn của bạn", color = Color.White.copy(.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun InfoChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    center: Boolean = false
) {
    LiquidGlassCard(radius = 16.dp, modifier = modifier.height(68.dp)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                title,
                color = Color.White.copy(.85f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = if (center) TextAlign.Center else TextAlign.Start
            )
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = if (center) TextAlign.Center else TextAlign.Start
            )
        }
    }
}



// ======= 1) Ô THỐNG KÊ =======
@Composable
private fun StatsOverview(
    availabilityOpen: Int,
    confirmedCount: Int,
    totalPaid: Int,
    totalPending: Int,
) {
    val vi = java.util.Locale("vi","VN")
    val nf = remember { java.text.NumberFormat.getCurrencyInstance(vi) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                emoji = "📅",
                title = "Lịch còn trống",
                value = availabilityOpen.toString(),
                tint = Color(0xFF93C5FD),
                modifier = Modifier.weight(1f).height(110.dp)
            )
            StatCard(
                emoji = "✨",
                title = "Đã xác nhận",
                value = confirmedCount.toString(),
                tint = Color(0xFF34D399),
                modifier = Modifier.weight(1f).height(110.dp)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                emoji = "💰",
                title = "Đã thu",
                value = nf.format(totalPaid),
                accent = Color(0xFF34D399),
                tint = Color(0xFF34D399),
                modifier = Modifier.weight(1f).height(110.dp)
            )
            StatCard(
                emoji = "⏳",
                title = "Chờ thanh toán",
                value = nf.format(totalPending),
                accent = Color(0xFFFCD34D),
                tint = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f).height(110.dp)
            )
        }
    }
}

@Composable
private fun StatCard(
    emoji: String,
    title: String,
    value: String,
    accent: Color = Color.White,
    tint: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(radius = 24.dp, modifier = modifier) {
        // Chiếm toàn bộ diện tích thẻ và căn GIỮA cả ngang lẫn dọc
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),          // chỉnh cao/thấp tùy Figma (100–120dp)
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // icon đầu thẻ
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji)
                }

                // số liệu
                Text(
                    value,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                // nhãn
                Text(
                    title,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun SegmentedTabs(
    active: MentorTab,
    pendingCount: Int,                 // giữ tham số để không phải đổi nơi gọi
    onChange: (MentorTab) -> Unit
) {
    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(.12f))
                            .border(
                                BorderStroke(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF60A5FA),
                                            Color(0xFFA78BFA),
                                            Color(0xFFF472B6)
                                        )
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    )
                }
            }
        ) {
            MentorTab.values().forEachIndexed { i, tab ->
                val label = when (tab) {
                    MentorTab.Availability -> "📅 Lịch trống"
                    MentorTab.Bookings     -> "📋 Booking"
                    MentorTab.Sessions     -> "💬 Phiên học"
                }
                Tab(
                    selected = i == active.ordinal,
                    onClick = { onChange(tab) },
                    text = {
                        Text(
                            text = label,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (i == active.ordinal) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}


// ======= Booking Pending  =======
@Composable
private fun PendingBookingsTab(bookings: List<Booking>) {
    // Lọc + sắp xếp
    val pending = remember(bookings) {
        bookings.filter { it.status == BookingStatus.PENDING }
            .sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    // Header của tab (title + subtitle) — căn giữa như figma
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
    }

    Spacer(Modifier.height(12.dp))

    if (pending.isEmpty()) {
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏳ Không có booking chờ duyệt", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Tất cả yêu cầu đặt lịch đã được xử lý.", color = Color.White.copy(.7f))
            }
        }
        return
    }

    // Danh sách thẻ booking
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        pending.forEach { b ->
            val extra = MockData.bookingExtras[b.id]
            val topic = extra?.topic ?: "Booking"
            val sessionType = extra?.sessionType ?: "video" // "video" | "in-person"
            val isPaid = extra?.paymentStatus == "paid"
            val menteeNote = extra?.menteeNotes
            val menteeName = MockData.currentMenteeName
            val mentorName = MockData.mentorNameById(b.mentorId)

            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Header: Topic + mentee/mentor + pill "Chờ duyệt"
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "📝 $topic",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "👤 Với $menteeName   •   👨‍🏫 $mentorName",
                                color = Color.White.copy(.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF59E0B).copy(.25f))
                                .border(BorderStroke(1.dp, Color(0xFFF59E0B).copy(.45f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) { Text("⏳ Chờ duyệt", color = Color.White, fontWeight = FontWeight.Medium) }
                    }

                    // 4 ô info: Ngày & giờ + Thời lượng / Giá tư vấn + Hình thức
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("📅 Ngày & giờ", "${b.date} • ${b.startTime}", Modifier.weight(1f))
                        InfoChip("⏱️ Thời lượng", "${durationMinutes(b.startTime, b.endTime)} phút", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val priceText = "${b.price.toInt()} đ"
                        InfoChip("💎 Giá tư vấn", priceText, Modifier.weight(1f))
                        InfoChip(
                            "🎯 Hình thức",
                            if (sessionType == "in-person") "🤝 Trực tiếp" else "💻 Video Call",
                            Modifier.weight(1f)
                        )
                    }

                    // Trạng thái thanh toán (left text + pill bên phải như figma)
                    LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Trạng thái thanh toán", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                            ) { Text(label, color = Color.White, fontWeight = FontWeight.Medium) }
                        }
                    }

                    // Ghi chú từ mentee (nếu có)
                    if (!menteeNote.isNullOrBlank()) {
                        LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("💬 Ghi chú từ mentee:", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "“$menteeNote”",
                                    color = Color.White.copy(.95f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Nút hành động (theo figma)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MMButton(
                            text = "✅ Chấp nhận booking",
                            onClick = { /* accept */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)            // tránh khuất chữ
                        )
                        MMGhostButton(
                            text = "❌ Từ chối",
                            onClick = { /* reject */ },
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

// ======= Sessions (tất cả phiên) =======
// ======= Sessions (tất cả phiên) =======
@Composable
private fun SessionsTab(bookings: List<Booking>) {
    // Header của tab (title + subtitle) — căn giữa như figma
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💬  Tất cả phiên tư vấn",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Quản lý và theo dõi tất cả các phiên tư vấn của bạn",
            color = Color.White.copy(.7f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
    Spacer(Modifier.height(12.dp))

    val all = remember(bookings) {
        bookings.sortedWith(
            compareByDescending<Booking> { it.date }.thenByDescending { it.startTime }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        all.forEach { b ->
            // Lấy thêm dữ liệu mock để hiển thị theo Figma
            val extra = MockData.bookingExtras[b.id]
            val topic = extra?.topic ?: "Phiên tư vấn"
            val sessionType = extra?.sessionType ?: "video"  // "video" | "in-person"
            val isPaid = extra?.paymentStatus == "paid"
            val menteeName = MockData.currentMenteeName
            val mentorName = MockData.mentorNameById(b.mentorId)

            // Map màu + nhãn trạng thái
            val (statusColor, statusLabel) = when (b.status) {
                BookingStatus.CONFIRMED -> Color(0xFF22C55E) to "✅ Đã xác nhận"
                BookingStatus.PENDING   -> Color(0xFFF59E0B) to "⏳ Chờ duyệt"
                BookingStatus.COMPLETED -> Color(0xFF8B5CF6) to "🎉 Hoàn thành"
                BookingStatus.CANCELLED -> Color(0xFFEF4444) to "❌ Đã hủy"
            }

            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ===== Header: chủ đề + tên mentee/mentor + pill trạng thái ở phải
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = (if (sessionType == "in-person") "🤝 " else "💻 ") + topic,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "👤 Với $menteeName   •   👨‍🏫 $mentorName",
                                color = Color.White.copy(.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(.25f))
                                .border(BorderStroke(1.dp, statusColor.copy(.45f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(statusLabel, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }

                    // ===== Hàng 1: Ngày & giờ + Thời lượng
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("📅 Ngày & giờ", "${b.date} • ${b.startTime}", Modifier.weight(1f))
                        InfoChip("⏱️ Thời lượng", "${durationMinutes(b.startTime, b.endTime)} phút", Modifier.weight(1f))
                    }

                    // ===== Hàng 2: Giá tư vấn + Hình thức
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip("💎 Giá tư vấn", "${b.price.toInt()} đ", Modifier.weight(1f))
                        InfoChip(
                            "🎯 Hình thức",
                            if (sessionType == "in-person") "🤝 Trực tiếp" else "💻 Video Call",
                            Modifier.weight(1f)
                        )
                    }

                    // ===== Thanh toán: label bên trái + pill bên phải (giống figma)
                    LiquidGlassCard(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "💳 Thanh toán",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            val (payColor, payLabel) = if (isPaid)
                                Color(0xFF22C55E) to "✅ Đã thanh toán"
                            else
                                Color(0xFFF59E0B) to "⏳ Chờ thanh toán"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(payColor.copy(.25f))
                                    .border(BorderStroke(1.dp, payColor.copy(.45f)), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) { Text(payLabel, color = Color.White, fontWeight = FontWeight.Medium) }
                        }
                    }
                }
            }
        }
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