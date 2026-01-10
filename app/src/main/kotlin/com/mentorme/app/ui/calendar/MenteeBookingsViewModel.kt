package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.dto.PaymentWebhookRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MenteeBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val api: MentorMeApi
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _paymentEvents = MutableSharedFlow<PaymentEvent>()
    val paymentEvents = _paymentEvents.asSharedFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
        observeRealtimeEvents()
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            Logx.d(TAG) { "refresh bookings (mentee)" }
            when (val res = bookingRepository.getBookings(role = "mentee", page = 1, limit = 100)) {
                is AppResult.Success -> _bookings.value = res.data.bookings
                is AppResult.Error -> Logx.e(TAG, { "refresh failed: ${res.throwable}" })
                AppResult.Loading -> Unit
            }
            _loading.value = false
        }
    }

    private fun observeRealtimeEvents() {
        viewModelScope.launch {
            RealtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.BookingChanged -> updateBooking(event.bookingId)
                    is RealtimeEvent.SessionReady -> updateBooking(event.payload.bookingId ?: return@collect)
                    is RealtimeEvent.SessionAdmitted -> updateBooking(event.payload.bookingId ?: return@collect)
                    is RealtimeEvent.SessionParticipantJoined -> updateBooking(event.payload.bookingId ?: return@collect)
                    is RealtimeEvent.SessionEnded -> updateBooking(event.payload.bookingId ?: return@collect)
                    else -> Unit
                }
            }
        }
    }

    private suspend fun updateBooking(bookingId: String) {
        when (val res = bookingRepository.getBookingById(bookingId)) {
            is AppResult.Success -> {
                val updated = res.data
                _bookings.update { current ->
                    val idx = current.indexOfFirst { it.id == updated.id }
                    if (idx >= 0) {
                        val mutable = current.toMutableList()
                        mutable[idx] = updated
                        mutable
                    } else {
                        listOf(updated) + current
                    }
                }
            }
            is AppResult.Error -> refresh()
            AppResult.Loading -> Unit
        }
    }

    fun cancelBooking(bookingId: String, reason: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = bookingRepository.cancelBooking(bookingId, reason)
            when (res) {
                is AppResult.Success -> {
                    refresh()
                    onResult(true, null)
                }
                is AppResult.Error -> onResult(false, res.throwable)
                AppResult.Loading -> Unit
            }
        }
    }

    fun simulatePaymentSuccess(bookingId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.simulatePaymentWebhook(
                    PaymentWebhookRequest(event = "payment.success", bookingId = bookingId)
                )
                if (resp.isSuccessful) {
                    refresh()
                    onResult(true, null)
                } else {
                    onResult(false, resp.errorBody()?.string())
                }
            } catch (t: Throwable) {
                onResult(false, t.message)
            }
        }
    }

    fun payBooking(bookingId: String) {
        viewModelScope.launch {
            _paymentEvents.emit(PaymentEvent.Loading)

            when (val res = bookingRepository.payBooking(bookingId)) {
                is AppResult.Success -> {
                    refresh()
                    _paymentEvents.emit(PaymentEvent.Success)
                }
                is AppResult.Error -> {
                    val raw = res.throwable ?: "Thanh toán thất bại"
                    if (raw.contains("INSUFFICIENT_BALANCE", true)) {
                        _paymentEvents.emit(PaymentEvent.InsufficientBalance)
                    } else {
                        _paymentEvents.emit(PaymentEvent.Failed(raw))
                    }
                }
                AppResult.Loading -> Unit
            }
        }
    }

    sealed class PaymentEvent {
        object Success : PaymentEvent()
        object InsufficientBalance : PaymentEvent()
        object Loading : PaymentEvent()
        data class Failed(val message: String) : PaymentEvent()
    }

    private companion object {
        const val TAG = "MenteeBookingsVM"
    }
}
