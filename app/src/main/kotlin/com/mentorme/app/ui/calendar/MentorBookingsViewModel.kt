package com.mentorme.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.UpdateBookingRequest
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

    fun refresh(mentorId: String) {
        viewModelScope.launch {
            Logx.d(TAG) { "refresh bookings for mentor=$mentorId" }
            try {
                val resp = api.getBookings(page = 1, limit = 50)
                if (resp.isSuccessful) {
                    val list = resp.body()?.data?.bookings.orEmpty()
                    _bookings.value = list.filter { it.mentorId == mentorId }
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
                val resp = api.updateBooking(bookingId, UpdateBookingRequest(status = "accepted"))
                onResult(resp.isSuccessful)
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "accept exception: ${t.message}" })
                onResult(false)
            }
        }
    }

    fun decline(bookingId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            Logx.d(TAG) { "decline bookingId=$bookingId" }
            try {
                val resp = api.updateBooking(bookingId, UpdateBookingRequest(status = "declined", notes = "declined"))
                onResult(resp.isSuccessful)
            } catch (t: Throwable) {
                Logx.e(tag = TAG, message = { "decline exception: ${t.message}" })
                onResult(false)
            }
        }
    }

    private companion object { const val TAG = "MentorBookingsVM" }
}
