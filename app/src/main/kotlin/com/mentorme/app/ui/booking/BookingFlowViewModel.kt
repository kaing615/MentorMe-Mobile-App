package com.mentorme.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.model.Booking
import com.mentorme.app.domain.usecase.calendar.CreateBookingUseCase
import com.mentorme.app.domain.usecase.calendar.GetMentorAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.dto.availability.ApiEnvelope

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
        val startLabel: String,
        val endLabel: String
    )

    private val _state: MutableStateFlow<UiState<Map<String, List<TimeSlotUi>>>> =
        MutableStateFlow(UiState.Idle)
    val state: StateFlow<UiState<Map<String, List<TimeSlotUi>>>> = _state

    fun load(mentorId: String) {
        Logx.d("BookingFlowVM") { "load() start mentorId=$mentorId" }
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val result = getMentorAvailability(mentorId)) {
                is AppResult.Success -> {
                    val slots = result.data
                        .filter { it.isAvailable }
                        .groupBy { it.date }
                        .mapValues { (_, list) ->
                            list
                                .sortedBy { it.startTime }
                                .map { s ->
                                    TimeSlotUi(
                                        occurrenceId = s.id ?: "${s.date}_${s.startTime}",  // ✅ Map occurrenceId
                                        date = s.date,
                                        startTime = s.startTime,
                                        endTime = s.endTime,
                                        startLabel = s.startTime,
                                        endLabel = s.endTime
                                    )
                                }
                        }
                    Logx.d("BookingFlowVM") { "load() success mentorId=$mentorId days=${slots.keys.size}" }
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
    ): AppResult<Unit> {
        val result = createBookingUseCase(
            mentorId = mentorId,
            occurrenceId = occurrenceId,
            topic = topic,
            notes = notes
        )
        return when (result) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(result.throwable)
            AppResult.Loading -> AppResult.Loading
        }
    }
}
