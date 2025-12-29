package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.dto.MentorDeclineRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class MentorBookingsViewModel @Inject constructor(
    private val api: MentorMeApi
) : ViewModel() {

    private fun normalizeBookingLocalTime(booking: Booking): Booking {
        val startIso = booking.startTimeIso
        val endIso = booking.endTimeIso
        if (startIso.isNullOrBlank() || endIso.isNullOrBlank()) return booking

        val zone = ZoneId.systemDefault()
        val start = runCatching { Instant.parse(startIso).atZone(zone) }.getOrNull() ?: return booking
        val end = runCatching { Instant.parse(endIso).atZone(zone) }.getOrNull() ?: return booking
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

        return booking.copy(
            date = start.toLocalDate().toString(),
            startTime = start.format(timeFmt),
            endTime = end.format(timeFmt)
        )
    }

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    init {
        observeRealtimeEvents()
    }

    fun refresh(role: String = "mentor") {
        viewModelScope.launch {
            Logx.d(TAG) { "refresh bookings for role=$role" }
            try {
                val resp = api.getBookings(role = role, page = 1, limit = 50)
                if (resp.isSuccessful) {
                    val list = resp.body()?.data?.bookings.orEmpty()
                    _bookings.value = list.map(::normalizeBookingLocalTime)
                    Logx.d(TAG) { "refresh success count=${_bookings.value.size}" }
                } else {
                    Logx.e(tag = TAG, message = { "refresh failed HTTP ${resp.code()} ${resp.message()}" })
                }
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "refresh exception: ${t.message}" })
            }
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
        try {
            val resp = api.getBookingById(bookingId)
            if (!resp.isSuccessful) {
                refresh()
                return
            }
            val booking = resp.body()?.data ?: return
            val normalized = normalizeBookingLocalTime(booking)
            _bookings.update { current ->
                val idx = current.indexOfFirst { it.id == normalized.id }
                if (idx >= 0) {
                    val mutable = current.toMutableList()
                    mutable[idx] = normalized
                    mutable
                } else {
                    listOf(normalized) + current
                }
            }
        } catch (t: Throwable) {
            Logx.e(tag = TAG, message = { "update booking failed: ${t.message}" })
            refresh()
        }
    }

    fun accept(bookingId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            Logx.d(TAG) { "accept bookingId=$bookingId" }
            try {
                val resp = api.mentorConfirmBooking(bookingId)
                onResult(resp.isSuccessful)
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "accept exception: ${t.message}" })
                onResult(false)
            }
        }
    }

    fun decline(bookingId: String, reason: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            Logx.d(TAG) { "decline bookingId=$bookingId" }
            try {
                val resp = api.mentorDeclineBooking(bookingId, MentorDeclineRequest(reason))
                onResult(resp.isSuccessful)
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "decline exception: ${t.message}" })
                onResult(false)
            }
        }
    }

    private companion object { const val TAG = "MentorBookingsVM" }
}
