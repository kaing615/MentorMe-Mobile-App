package com.mentorme.app.ui.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.Booking
import com.mentorme.app.ui.components.ui.GlassDialog
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import java.text.NumberFormat
import java.util.Locale

/* ---- Helper function ---- */
private fun formatVnd(amount: Double): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return nf.format(amount)
}

@Composable
fun PendingBookingPromptDialog(
    booking: Booking,
    actionBusy: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    val menteeName = menteeDisplayName(booking)
    val timeLabel = "${booking.date} ${booking.startTime}-${booking.endTime}"
    val priceLabel = if (booking.price > 0) {
        formatVnd(booking.price)
    } else {
        "TBD"
    }

    GlassDialog(
        onDismiss = onDismiss,
        title = "Yêu cầu tư vấn mới",
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Bạn có một lịch hẹn mới đang chờ xác nhận. Mentee đã hoàn tất thanh toán và đang chờ phản hồi từ bạn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Mentee",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    menteeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Thời gian",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Thu nhập dự kiến",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    priceLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
            }
        },
        confirm = {
            MMPrimaryButton(
                onClick = onAccept,
                enabled = !actionBusy
            ) {
                Text("Chấp nhận", fontWeight = FontWeight.SemiBold)
            }
        },
        dismiss = {
            MMGhostButton(
                onClick = onDecline,
                enabled = !actionBusy
            ) {
                Text("Từ chối", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private fun menteeDisplayName(booking: Booking): String {
    val menteeId = booking.menteeId.trim()
    val displayName = sequenceOf(
        booking.menteeFullName,
        booking.mentee?.profile?.fullName,
        booking.mentee?.fullName,
        booking.mentee?.user?.fullName,
        booking.mentee?.user?.userName
    )
        .mapNotNull { it?.trim() }
        .firstOrNull { it.isNotEmpty() && it != menteeId }

    return displayName ?: if (menteeId.length > 6) "...${menteeId.takeLast(6)}" else menteeId
}
