package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.dto.MentorDeclineRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.ui.home.WaitingSession
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

    private val _pendingConfirmations = MutableStateFlow<List<Booking>>(emptyList())
    val pendingConfirmations: StateFlow<List<Booking>> = _pendingConfirmations.asStateFlow()
    private val seenPendingIds = mutableSetOf<String>()

    private val _waitingSession = MutableStateFlow<WaitingSession?>(null)
    val waitingSession: StateFlow<WaitingSession?> = _waitingSession.asStateFlow()

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
                    syncPendingPrompts(_bookings.value)
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
                    is RealtimeEvent.SessionReady -> {
                        updateBooking(event.payload.bookingId ?: return@collect)
                        // Clear waiting session when ready
                        if (_waitingSession.value?.bookingId == event.payload.bookingId) {
                            _waitingSession.value = null
                        }
                    }
                    is RealtimeEvent.SessionAdmitted -> updateBooking(event.payload.bookingId ?: return@collect)
                    is RealtimeEvent.SessionParticipantJoined -> {
                        val bookingId = event.payload.bookingId ?: return@collect
                        val updated = updateBooking(bookingId)
                        // Show waiting session banner when mentee joins
                        if (event.payload.role == "mentee" && updated?.status == BookingStatus.CONFIRMED) {
                            _waitingSession.value = WaitingSession(
                                bookingId = bookingId,
                                menteeUserId = event.payload.userId,
                                menteeName = null
                            )
                        }
                    }
                    is RealtimeEvent.SessionEnded -> {
                        updateBooking(event.payload.bookingId ?: return@collect)
                        // Clear waiting session when ended
                        if (_waitingSession.value?.bookingId == event.payload.bookingId) {
                            _waitingSession.value = null
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private suspend fun updateBooking(bookingId: String): Booking? {
        try {
            val resp = api.getBookingById(bookingId)
            if (!resp.isSuccessful) {
                refresh()
                return null
            }
            val booking = resp.body()?.data ?: return null
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
            if (normalized.status == BookingStatus.PENDING_MENTOR) {
                upsertPendingPrompt(normalized)
            } else {
                removePendingPrompt(normalized.id)
            }
            return normalized
        } catch (t: Throwable) {
            Logx.e(tag = TAG, message = { "update booking failed: ${t.message}" })
            refresh()
            return null
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

    fun dismissWaitingSession() {
        _waitingSession.value = null
    }

    fun dismissPendingPrompt(bookingId: String) {
        seenPendingIds.add(bookingId)
        removePendingPrompt(bookingId)
    }

    private fun syncPendingPrompts(bookings: List<Booking>) {
        val pending = bookings.filter { it.status == BookingStatus.PENDING_MENTOR }
        val pendingIds = pending.map { it.id }.toSet()
        _pendingConfirmations.update { current -> current.filter { it.id in pendingIds } }
        pending.sortedWith(compareBy({ it.date }, { it.startTime })).forEach { booking ->
            upsertPendingPrompt(booking)
        }
    }

    private fun upsertPendingPrompt(booking: Booking) {
        if (booking.status != BookingStatus.PENDING_MENTOR) return
        _pendingConfirmations.update { current ->
            val idx = current.indexOfFirst { it.id == booking.id }
            if (idx >= 0) {
                val mutable = current.toMutableList()
                mutable[idx] = booking
                mutable
            } else if (seenPendingIds.add(booking.id)) {
                current + booking
            } else {
                current
            }
        }
    }

    private fun removePendingPrompt(bookingId: String) {
        _pendingConfirmations.update { current ->
            current.filterNot { it.id == bookingId }
        }
    }

    private companion object { const val TAG = "MentorBookingsVM" }
}
