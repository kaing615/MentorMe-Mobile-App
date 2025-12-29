package com.mentorme.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.repository.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class TopUpUiState {
    object Idle : TopUpUiState()
    object Loading : TopUpUiState()
    data class Success(val wallet: WalletDto) : TopUpUiState()
    data class Error(val message: String) : TopUpUiState()
}

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val repo: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopUpUiState>(TopUpUiState.Idle)
    val uiState: StateFlow<TopUpUiState> = _uiState

    fun submitTopup(amount: Long) {
        viewModelScope.launch {
            try {
                repo.mockTopup(
                    amount = amount,
                    currency = "VND",
                    clientRequestId = UUID.randomUUID().toString()
                )

                val wallet = repo.getMyWallet()

                if (wallet != null) {
                    _uiState.value = TopUpUiState.Success(wallet)
                } else {
                    _uiState.value = TopUpUiState.Error("Không lấy được ví")
                }
            } catch (e: Exception) {
                _uiState.value = TopUpUiState.Error(e.message ?: "Lỗi nạp tiền")
            }
        }
    }

    fun clear() {
        _uiState.value = TopUpUiState.Idle
    }
}

