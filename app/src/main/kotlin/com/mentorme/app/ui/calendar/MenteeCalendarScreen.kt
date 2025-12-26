package com.mentorme.app.ui.calendar

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.ErrorUtils
import com.mentorme.app.data.model.Booking
import com.mentorme.app.domain.usecase.review.SubmitReviewUseCase
import com.mentorme.app.ui.review.ReviewDialog
import kotlinx.coroutines.launch
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MenteeCalendarDeps {
    fun submitReviewUseCase(): SubmitReviewUseCase
}

@Composable
fun MenteeCalendarScreen(
    startTab: CalendarTab = CalendarTab.Upcoming,
    onOpenDetail: (Booking) -> Unit = {}
) {
    val vm = hiltViewModel<MenteeBookingsViewModel>()
    val bookings = vm.bookings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Review dialog state
    var showReviewDialog by remember { mutableStateOf(false) }
    var bookingToReview by remember { mutableStateOf<Booking?>(null) }
    var isSubmittingReview by remember { mutableStateOf(false) }
    var reviewError by remember { mutableStateOf<String?>(null) }

    // Get SubmitReviewUseCase
    val deps = remember(context) {
        EntryPointAccessors.fromApplication(context, MenteeCalendarDeps::class.java)
    }
    val submitReview = remember { deps.submitReviewUseCase() }

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    CalendarScreen(
        startTab = startTab,
        bookings = bookings.value,
        onJoinSession = { Toast.makeText(context, "Join chưa hỗ trợ", Toast.LENGTH_SHORT).show() },
        onRate = { booking ->
            bookingToReview = booking
            reviewError = null
            showReviewDialog = true
        },
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
        },
        onOpen = onOpenDetail
    )

    // Show review dialog
    if (showReviewDialog && bookingToReview != null) {
        val booking = bookingToReview!!
        val mentorName = booking.mentorFullName
            ?: booking.mentor?.fullName
            ?: "Mentor"

        ReviewDialog(
            bookingId = booking.id,
            mentorName = mentorName,
            onDismiss = {
                showReviewDialog = false
                bookingToReview = null
                reviewError = null
            },
            onSubmit = { rating, comment ->
                scope.launch {
                    isSubmittingReview = true
                    reviewError = null

                    when (val result = submitReview(booking.id, rating, comment)) {
                        is AppResult.Success -> {
                            isSubmittingReview = false
                            showReviewDialog = false
                            bookingToReview = null
                            Toast.makeText(context, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show()
                            // Refresh bookings to update reviewId
                            vm.refresh()
                        }
                        is AppResult.Error -> {
                            isSubmittingReview = false
                            reviewError = result.throwable
                        }
                        AppResult.Loading -> {
                            isSubmittingReview = true
                        }
                    }
                }
            },
            isSubmitting = isSubmittingReview,
            errorMessage = reviewError
        )
    }
}
