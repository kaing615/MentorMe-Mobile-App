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
        date: String,       // yyyy-MM-dd
        startTime: String,  // HH:mm
        endTime: String,    // HH:mm
        topic: String,
        notes: String?
    ): AppResult<Booking> {
        val scheduledAtIsoUtc = DateIsoUtils.toIsoUtc(date, startTime)
        val durationMinutes = DateIsoUtils.durationMinutes(startTime, endTime)
        Logx.d("BookingFlowVM") { "createBooking() start mentorId=$mentorId date=$date start=$startTime end=$endTime duration=$durationMinutes" }
        val result = createBookingUseCase(
            mentorId = mentorId,
            scheduledAtIsoUtc = scheduledAtIsoUtc,
            durationMinutes = durationMinutes,
            topic = topic,
            notes = notes
        )
        when (result) {
            is AppResult.Success -> Logx.d("BookingFlowVM") { "createBooking() success mentorId=$mentorId date=$date start=$startTime" }
            is AppResult.Error -> {
                val code = result.throwable.substringAfter("HTTP ", "").substringBefore(":").toIntOrNull()
                Logx.e("BookingFlowVM", { "createBooking() error mentorId=$mentorId httpCode=${code ?: "n/a"}" })
            }
            AppResult.Loading -> Unit
        }
        Logx.d("BookingFlowVM") { "createBooking() done mentorId=$mentorId" }
        return result
    }

    /** Internal, JVM-only date helpers (no Android deps). */
    private object DateIsoUtils {
        fun toIsoUtc(date: String, startTime: String): String {
            // date: yyyy-MM-dd, time: HH:mm
            val dt = java.time.LocalDate.parse(date)
                .atTime(java.time.LocalTime.parse(startTime))
                .atOffset(java.time.ZoneOffset.UTC)
            return dt.toInstant().toString() // ISO-8601 UTC like 2025-10-22T13:45:00Z
        }

        fun durationMinutes(startTime: String, endTime: String): Int {
            val start = java.time.LocalTime.parse(startTime)
            val end = java.time.LocalTime.parse(endTime)
            var mins = java.time.Duration.between(start, end).toMinutes().toInt()
            if (mins <= 0) mins += 24 * 60 // cross-midnight fallback
            return mins
        }
    }
}
