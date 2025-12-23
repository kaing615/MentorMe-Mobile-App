package com.mentorme.app.ui.calendar

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.utils.ErrorUtils

@Composable
fun MenteeCalendarScreen() {
    val vm = hiltViewModel<MenteeBookingsViewModel>()
    val bookings = vm.bookings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    CalendarScreen(
        bookings = bookings.value,
        onJoinSession = { Toast.makeText(context, "Join chưa hỗ trợ", Toast.LENGTH_SHORT).show() },
        onRate = { Toast.makeText(context, "Đánh giá chưa hỗ trợ", Toast.LENGTH_SHORT).show() },
        onRebook = { Toast.makeText(context, "Đặt lại chưa hỗ trợ", Toast.LENGTH_SHORT).show() },
        onPay = { booking ->
            vm.simulatePaymentSuccess(booking.id) { ok, err ->
                val msg = if (ok) "Đã gửi webhook thanh toán" else ErrorUtils.getUserFriendlyErrorMessage(err)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        },
        onCancel = { booking ->
            vm.cancelBooking(booking.id, "Mentee cancelled") { ok, err ->
                val msg = if (ok) "Đã hủy booking" else ErrorUtils.getUserFriendlyErrorMessage(err)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    )
}
