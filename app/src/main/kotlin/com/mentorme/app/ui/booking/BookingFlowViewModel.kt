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

/**
 * Hilt ViewModel powering mentee booking flow.
 * - load(mentorId): fetch availability, filter, group by date, sort by startTime.
 * - createBooking(...): build scheduledAt ISO-UTC and duration from inputs, call use case and return AppResult.
 * No Android Context / Compose deps here.
 */
@HiltViewModel
class BookingFlowViewModel @Inject constructor(
    private val getMentorAvailability: GetMentorAvailabilityUseCase,
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    // Public UI state: Idle/Loading/Success(Error)
    sealed interface UiState<out T> {
        data object Idle : UiState<Nothing>
        data object Loading : UiState<Nothing>
        data class Success<T>(val data: T) : UiState<T>
        data class Error(val message: String) : UiState<Nothing>
    }

    data class TimeSlotUi(
        val date: String,        // yyyy-MM-dd
        val startTime: String,   // HH:mm
        val endTime: String,     // HH:mm
        val startLabel: String,  // display label (here equals HH:mm)
        val endLabel: String     // display label (here equals HH:mm)
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
     * Create booking and propagate server status. This does not mutate state; caller can handle refresh.
     */
    fun createBooking(
        mentorId: String,
        occurrenceId: String,
        topic: String? = null,
        notes: String? = null
    ): AppResult<Booking> {
        Logx.d("BookingFlowVM") { "createBooking() start mentorId=$mentorId occurrenceId=$occurrenceId" }
        val result = createBookingUseCase(
            mentorId = mentorId,
            occurrenceId = occurrenceId,
            topic = topic,
            notes = notes
        )
        when (result) {
            is AppResult.Success -> Logx.d("BookingFlowVM") { "createBooking() success mentorId=$mentorId occurrenceId=$occurrenceId" }
            is AppResult.Error -> {
                val code = result.throwable.substringAfter("HTTP ", "").substringBefore(":").toIntOrNull()
                Logx.e("BookingFlowVM", { "createBooking() error mentorId=$mentorId httpCode=${code ?: "n/a"}" })
            }
            AppResult.Loading -> Unit
        }
        Logx.d("BookingFlowVM") { "createBooking() done mentorId=$mentorId" }
        return result
    }
}
