package com.mentorme.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(val booking: Booking) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load(bookingId: String) {
        if (bookingId.isBlank()) {
            _state.value = UiState.Error("Booking id is required")
            return
        }

        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val res = bookingRepository.getBookingById(bookingId)) {
                is AppResult.Success -> _state.value = UiState.Success(res.data)
                is AppResult.Error -> _state.value = UiState.Error(res.throwable)
                AppResult.Loading -> Unit
            }
        }
    }
}
