package com.mentorme.app.ui.booking

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.ui.booking.BookingPaymentViewModel.UiEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingPaymentViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun payBooking(bookingId: String) {
        viewModelScope.launch {
            when (val result = bookingRepository.payBooking(bookingId)) {

                is AppResult.Success -> {
                    _uiEvent.emit(UiEvent.PaySuccess)
                }

                is AppResult.Error -> {
                    val raw = result.throwable ?: "Thanh toán không thành công"

                    if (raw.contains("INSUFFICIENT_BALANCE", ignoreCase = true)) {
                        _uiEvent.emit(UiEvent.InsufficientBalance)
                    } else {
                        _uiEvent.emit(UiEvent.PayFailed(raw))
                    }
                }

                is AppResult.Loading -> {
                    _uiEvent.emit(UiEvent.Loading)
                }
            }
        }
    }

    sealed class UiEvent {
        object PaySuccess : UiEvent()
        object InsufficientBalance : UiEvent()
        object Loading : UiEvent()
        data class PayFailed(val message: String) : UiEvent()
    }
}
