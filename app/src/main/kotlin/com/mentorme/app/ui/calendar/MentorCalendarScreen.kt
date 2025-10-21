package com.mentorme.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.calendar.components.SegmentedTabs
import com.mentorme.app.ui.calendar.components.StatCard
import com.mentorme.app.ui.calendar.core.AvailabilitySlot
import com.mentorme.app.ui.calendar.core.durationMinutes
import com.mentorme.app.ui.calendar.tabs.AvailabilityTabSection
import com.mentorme.app.ui.calendar.tabs.PendingBookingsTab
import com.mentorme.app.ui.calendar.tabs.SessionsTab

@Composable
fun MentorCalendarScreen(
    onViewSession: (String) -> Unit = {},
    onCreateSession: () -> Unit = {},
    onUpdateAvailability: () -> Unit = {},
    onCancelSession: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // --- Tabs: 0 = Availability, 1 = Bookings, 2 = Sessions ---
    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("📅 Lịch trống", "📋 Booking", "💬 Phiên học")

    // --- bookings state (mock) ---
    var bookings by remember { mutableStateOf(MockData.mockBookings) }

    // --- slots state (mock demo) ---
    var slots by remember {
        mutableStateOf(
            listOf(
                AvailabilitySlot("1","2024-01-15","09:00","10:00",60,"React/NextJS Consultation", true,"video", true),
                AvailabilitySlot("2","2024-01-16","14:00","15:30",90,"System Design & Architecture", true,"in-person", false),
                AvailabilitySlot("3","2024-01-17","10:30","11:30",60,"Career Guidance", true,"video", false),
            )
        )
    }

    // ---- Tính số liệu tổng quan (memo theo state) ----
    val availabilityOpen = remember(slots) { slots.count { it.isActive && !it.isBooked } }
    val confirmedCount = remember(bookings) { bookings.count { it.status == BookingStatus.CONFIRMED } }

    val totalPaid = remember(bookings) {
        bookings
            .filter { it.status == BookingStatus.COMPLETED }
            .filter { MockData.bookingExtras[it.id]?.paymentStatus == "paid" }
            .sumOf { it.price.toInt() }
    }
    val totalPending = remember(bookings) {
        bookings
            .filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED }
            .filter { MockData.bookingExtras[it.id]?.paymentStatus != "paid" }
            .sumOf { it.price.toInt() }
    }

    // Insets để né status bar & bottom nav
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dashboardHeight = 88.dp
    val bottomPadding = bottomInset + dashboardHeight

    Column(
        modifier = modifier
            .fillMaxSize()
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
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))

        // Stats
        StatsOverview(
            availabilityOpen = availabilityOpen,
            confirmedCount = confirmedCount,
            totalPaid = totalPaid,
            totalPending = totalPending
        )
        Spacer(Modifier.height(10.dp))

        // Tabs
        SegmentedTabs(
            activeIndex = activeTab,
            labels = tabLabels,
            onChange = { activeTab = it }
        )
        Spacer(Modifier.height(12.dp))

        // Nội dung từng tab
        when (activeTab) {
            0 -> AvailabilityTabSection(
                slots = slots,
                onAdd = { newSlot -> slots = slots + newSlot },
                onUpdate = { updated ->                      // 👈 NEW
                    slots = slots.map { if (it.id == updated.id) updated else it }
                },
                onToggle = { id ->
                    slots = slots.map { if (it.id == id) it.copy(isActive = !it.isActive) else it }
                },
                onDelete = { id -> slots = slots.filterNot { it.id == id } }

            )
            1 -> PendingBookingsTab(
                bookings = bookings,
                onAccept = { id -> bookings = bookings.map { if (it.id == id) it.copy(status = BookingStatus.CONFIRMED) else it } },
                onReject = { id -> bookings = bookings.map { if (it.id == id) it.copy(status = BookingStatus.CANCELLED) else it } }
            )
            2 -> SessionsTab(bookings = bookings)
        }

        // chừa chỗ đáy để né dashboard
        Spacer(Modifier.height(bottomPadding))
    }
}

/* -------------------- Local UI pieces -------------------- */

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
                tint = Color(0xFF34D399),
                modifier = Modifier.weight(1f).height(110.dp)
            )
            StatCard(
                emoji = "⏳",
                title = "Chờ thanh toán",
                value = nf.format(totalPending),
                tint = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f).height(110.dp)
            )
        }
    }
}
