package com.mentorme.app.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.time.formatIsoToLocalShort
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit
) {
    val vm = hiltViewModel<BookingDetailViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

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
            BookingDetailContent(booking = s.booking, onBack = onBack)
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
    onBack: () -> Unit
) {
    val mentorName = mentorNameFor(booking.mentorId)
    val timeRange = "${booking.startTime} - ${booking.endTime}"
    val priceLabel = "$${"%.2f".format(booking.price)}"

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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
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

private fun mentorNameFor(mentorId: String): String {
    val mentor = MockData.mockMentors.firstOrNull { it.id == mentorId }
    return mentor?.fullName ?: "Mentor ${mentorId.takeLast(6)}"
}
