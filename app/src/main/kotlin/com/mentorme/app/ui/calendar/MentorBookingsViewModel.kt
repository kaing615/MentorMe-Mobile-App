package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.MentorDeclineRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.remote.MentorMeApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MentorBookingsViewModel @Inject constructor(
    private val api: MentorMeApi
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    fun refresh(role: String = "mentor") {
        viewModelScope.launch {
            Logx.d(TAG) { "refresh bookings for role=$role" }
            try {
                val resp = api.getBookings(role = role, page = 1, limit = 50)
                if (resp.isSuccessful) {
                    val list = resp.body()?.data?.bookings.orEmpty()
                    _bookings.value = list
                    Logx.d(TAG) { "refresh success count=${_bookings.value.size}" }
                } else {
                    Logx.e(tag = TAG, message = { "refresh failed HTTP ${resp.code()} ${resp.message()}" })
                }
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "refresh exception: ${t.message}" })
            }
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
