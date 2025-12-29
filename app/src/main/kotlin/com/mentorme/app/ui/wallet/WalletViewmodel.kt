package com.mentorme.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.repository.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WalletUiState {
    object Loading : WalletUiState()
    data class Success(val wallet: WalletDto) : WalletUiState()
    data class Error(val message: String?) : WalletUiState()
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repo: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val uiState: StateFlow<WalletUiState> = _uiState

    private val _topUpEvents = MutableSharedFlow<TopUpEvent>()
    val topUpEvents = _topUpEvents

    sealed class TopUpEvent {
        object Success : TopUpEvent()
        data class Error(val message: String?) : TopUpEvent()
    }

    fun ensureLoaded() {
        if (_uiState.value is WalletUiState.Loading) {
            loadWallet()
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            try {
                val (wallet, txs) = repo.getMyWalletWithTransactions()

                _uiState.value = WalletUiState.Success(
                    wallet.copy(transactions = txs)
                )
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message)
            }
        }
    }

    fun topUp(amountMinor: Long) {
        viewModelScope.launch {
            try {
                // 1) Gọi API nạp
                repo.mockTopup(
                    amount = amountMinor,
                    currency = "VND",
                    clientRequestId = UUID.randomUUID().toString()
                )

                // 2) Lấy lại wallet mới từ server
                val wallet = repo.getMyWallet()

                // 3) Cập nhật ui state => Compose sẽ recompose
                _uiState.value = wallet?.let { WalletUiState.Success(it) }
                    ?: WalletUiState.Error("Không lấy được ví sau khi nạp")

                // 4) Emit event để UI show snackbar / navigation
                _topUpEvents.emit(TopUpEvent.Success)
            } catch (e: Exception) {
                _topUpEvents.emit(TopUpEvent.Error(e.message))
            }
        }
    }

}
