package com.mentorme.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.domain.usecase.calendar.CreateBookingUseCase
import com.mentorme.app.domain.usecase.calendar.GetMentorAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.mentorme.app.core.utils.Logx

@HiltViewModel
class BookingFlowViewModel @Inject constructor(
    private val getMentorAvailability: GetMentorAvailabilityUseCase,
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    sealed interface UiState<out T> {
        data object Idle : UiState<Nothing>
        data object Loading : UiState<Nothing>
        data class Success<T>(val data: T) : UiState<T>
        data class Error(val message: String) : UiState<Nothing>
    }

    data class TimeSlotUi(
        val occurrenceId: String,  // ✅ Thêm occurrenceId
        val date: String,
        val startTime: String,
        val endTime: String,
        val priceVnd: Long
    )

    private val _state: MutableStateFlow<UiState<List<TimeSlotUi>>> =
        MutableStateFlow(UiState.Idle)
    val state: StateFlow<UiState<List<TimeSlotUi>>> = _state

    fun load(mentorId: String) {
        Logx.d("BookingFlowVM") { "load() start mentorId=$mentorId" }
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val result = getMentorAvailability(mentorId)) {
                is AppResult.Success -> {
                    val slots = result.data
                        .filter { it.isAvailable }
                        .map { s ->
                            TimeSlotUi(
                                occurrenceId = s.id ?: "${s.date}_${s.startTime}",
                                date = s.date,
                                startTime = s.startTime,
                                endTime = s.endTime,
                                priceVnd = s.priceVnd?.toLong() ?: 0L
                            )
                        }
                        .sortedWith(compareBy({ it.date }, { it.startTime }))
                    Logx.d("BookingFlowVM") { "load() success mentorId=$mentorId slots=${slots.size}" }
                    _state.value = UiState.Success(slots)
                }
                is AppResult.Error -> {
                    Logx.e("BookingFlowVM", { "load() error mentorId=$mentorId msg=${result.throwable}" })
                    _state.value = UiState.Error(result.throwable)
                }
                AppResult.Loading -> Unit
            }
            Logx.d("BookingFlowVM") { "load() done mentorId=$mentorId" }
        }
    }

    /**
     * Create booking using occurrenceId (new PR #14 approach)
     */
    fun createBooking(
        mentorId: String,
        occurrenceId: String,
        topic: String? = null,
        notes: String? = null
    ): AppResult<Booking> {
        val result = createBookingUseCase(
            mentorId = mentorId,
            occurrenceId = occurrenceId,
            topic = topic,
            notes = notes
        )
        return when (result) {
            is AppResult.Success -> {
                val envelope = result.data
                val booking = envelope.data
                if (envelope.success && booking != null) {
                    AppResult.Success(booking)
                } else {
                    AppResult.Error(envelope.message ?: "Failed to create booking")
                }
            }
            is AppResult.Error -> AppResult.Error(result.throwable)
            AppResult.Loading -> AppResult.Loading
        }
    }
}
