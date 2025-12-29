package com.mentorme.app.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.time.formatIsoToLocalShort
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.calendar.MentorBookingsViewModel
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.session.SessionViewModel
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    onJoinSession: (String) -> Unit = {}
) {
    val vm = hiltViewModel<BookingDetailViewModel>()
    val mentorBookingsVm = hiltViewModel<MentorBookingsViewModel>()
    val sessionVm = hiltViewModel<SessionViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val sessionState by sessionVm.session.collectAsStateWithLifecycle()
    val isMentor = sessionState.role.equals("mentor", ignoreCase = true)
    val context = LocalContext.current
    var actionBusy by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        vm.load(bookingId)
    }

    when (val s = state) {
        BookingDetailViewModel.UiState.Idle,
        BookingDetailViewModel.UiState.Loading -> {
            LoadingState(onBack = onBack)
        }
        is BookingDetailViewModel.UiState.Error -> {
            ErrorState(
                message = s.message,
                onBack = onBack,
                onRetry = { vm.load(bookingId) }
            )
        }
        is BookingDetailViewModel.UiState.Success -> {
            BookingDetailContent(
                booking = s.booking,
                onBack = onBack,
                onJoinSession = onJoinSession,
                showActions = isMentor,
                actionBusy = actionBusy,
                onAccept = {
                    actionBusy = true
                    mentorBookingsVm.accept(s.booking.id) { ok ->
                        actionBusy = false
                        if (ok) {
                            android.widget.Toast.makeText(context, "Booking confirmed", android.widget.Toast.LENGTH_SHORT).show()
                            vm.load(bookingId)
                        } else {
                            android.widget.Toast.makeText(context, "Failed to confirm booking", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDecline = {
                    actionBusy = true
                    mentorBookingsVm.decline(s.booking.id, "Mentor declined") { ok ->
                        actionBusy = false
                        if (ok) {
                            android.widget.Toast.makeText(context, "Booking declined", android.widget.Toast.LENGTH_SHORT).show()
                            vm.load(bookingId)
                        } else {
                            android.widget.Toast.makeText(context, "Failed to decline booking", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderRow(title = "Booking Details", onBack = onBack, status = null)
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text("Loading booking...", color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderRow(title = "Booking Details", onBack = onBack, status = null)
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Failed to load booking", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(message, color = Color.White.copy(alpha = 0.85f))
                MMPrimaryButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
private fun BookingDetailContent(
    booking: Booking,
    onBack: () -> Unit,
    onJoinSession: (String) -> Unit = {},
    showActions: Boolean,
    actionBusy: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val mentorName = mentorDisplayName(booking)
    val timeRange = "${booking.startTime} - ${booking.endTime}"
    val priceLabel = "$${"%.2f".format(booking.price)}"
    val canRespond = showActions && (booking.status == BookingStatus.PENDING_MENTOR || booking.status == BookingStatus.PAYMENT_PENDING)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPadding = 84.dp + bottomInset

    val sessionItems = buildList {
        add("Mentor" to mentorName)
        add("Date" to booking.date)
        add("Time" to timeRange)
        add("Price" to priceLabel)
        add("Booking Id" to booking.id)
        booking.topic?.takeIf { it.isNotBlank() }?.let { add("Topic" to it) }
        booking.notes?.takeIf { it.isNotBlank() }?.let { add("Notes" to it) }
        booking.meetingLink?.takeIf { it.isNotBlank() }?.let { add("Meeting Link" to it) }
        booking.location?.takeIf { it.isNotBlank() }?.let { add("Location" to it) }
    }

    val policyItems = buildList {
        formatIsoToLocalShort(booking.expiresAt)?.let { add("Payment Expires" to it) }
        formatIsoToLocalShort(booking.mentorResponseDeadline)?.let { add("Mentor Deadline" to it) }
        formatIsoToLocalShort(booking.reminder24hSentAt)?.let { add("Reminder 24h" to it) }
        formatIsoToLocalShort(booking.reminder1hSentAt)?.let { add("Reminder 1h" to it) }
        if (booking.lateCancel == true) {
            val minutes = booking.lateCancelMinutes?.toString() ?: "unknown"
            add("Late Cancel" to "Yes ($minutes minutes)")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = bottomPadding)
    ) {
        item {
            HeaderRow(title = "Booking Details", onBack = onBack, status = booking.status)
        }

        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sessionItems.forEach { (label, value) ->
                        DetailRow(label = label, value = value)
                    }
                }
            }
        }

        if (policyItems.isNotEmpty()) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Policy", color = Color.White, fontWeight = FontWeight.SemiBold)
                        policyItems.forEach { (label, value) ->
                            DetailRow(label = label, value = value)
                        }
                    }
                }
            }
        }

        // Join Session button for confirmed bookings
        if (booking.status == BookingStatus.CONFIRMED) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Session Actions", color = Color.White, fontWeight = FontWeight.SemiBold)
                        MMPrimaryButton(
                            onClick = { onJoinSession(booking.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) { 
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(androidx.compose.material.icons.Icons.Default.VideoCall, contentDescription = null)
                                Text("Join Session")
                            }
                        }
                    }
                }
            }
        }

        if (canRespond) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Booking response", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MMGhostButton(
                                onClick = onDecline,
                                modifier = Modifier.weight(1f),
                                enabled = !actionBusy
                            ) { Text("Decline") }
                            MMPrimaryButton(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f),
                                enabled = !actionBusy
                            ) { Text("Accept") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    onBack: () -> Unit,
    status: BookingStatus?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        status?.let { StatusPill(it) }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
private fun StatusPill(status: BookingStatus) {
    val (label, color) = when (status) {
        BookingStatus.PAYMENT_PENDING -> "Payment Pending" to Color(0xFFF59E0B)
        BookingStatus.PENDING_MENTOR -> "Pending Mentor" to Color(0xFFF59E0B)
        BookingStatus.CONFIRMED -> "Confirmed" to Color(0xFF10B981)
        BookingStatus.COMPLETED -> "Completed" to Color(0xFF8B5CF6)
        BookingStatus.NO_SHOW_MENTOR -> "No-show mentor" to Color(0xFFF97316)
        BookingStatus.NO_SHOW_MENTEE -> "No-show mentee" to Color(0xFFF97316)
        BookingStatus.NO_SHOW_BOTH -> "No-show both" to Color(0xFFF97316)
        BookingStatus.CANCELLED -> "Cancelled" to Color(0xFFEF4444)
        BookingStatus.DECLINED -> "Declined" to Color(0xFFEF4444)
        BookingStatus.FAILED -> "Failed" to Color(0xFFEF4444)
    }

    LiquidGlassCard(radius = 14.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("*", color = color, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun mentorDisplayName(booking: Booking): String {
    val fallback = MockData.mockMentors.firstOrNull { it.id == booking.mentorId }?.fullName
    val displayName = listOf(
        booking.mentorFullName,
        booking.mentor?.fullName,
        fallback
    ).firstOrNull { !it.isNullOrBlank() }?.trim()

    return displayName ?: "Mentor ${booking.mentorId.takeLast(6)}"
}
