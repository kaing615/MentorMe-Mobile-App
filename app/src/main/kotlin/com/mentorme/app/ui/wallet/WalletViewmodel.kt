package com.mentorme.app.ui.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.mentorme.app.data.dto.paymentMethods.AddPaymentMethodRequest
import com.mentorme.app.data.dto.paymentMethods.UpdatePaymentMethodRequest
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.repository.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update

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

    private val _topUpEvents = MutableSharedFlow<TopUpEvent>(replay = 1)
    val topUpEvents = _topUpEvents

    private val _withdrawEvents = MutableSharedFlow<WithdrawEvent>(replay = 1)
    val withdrawEvents = _withdrawEvents

    sealed class TopUpEvent {
        object Success : TopUpEvent()
        data class Error(val message: String?) : TopUpEvent()
    }

    sealed class WithdrawEvent {
        object Success : WithdrawEvent()
        data class Error(val message: String) : WithdrawEvent()
    }

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods = _paymentMethods.asStateFlow()

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

                val (wallet, txs) = repo.getMyWalletWithTransactions()
                _uiState.value = WalletUiState.Success(wallet.copy(transactions = txs))

                // 4) Emit event để UI show snackbar / navigation
                _topUpEvents.emit(TopUpEvent.Success)
            } catch (e: Exception) {
                _topUpEvents.emit(TopUpEvent.Error(e.message))
            }
        }
    }

    fun withdraw(amountMinor: Long, paymentMethodId: String?) {
        viewModelScope.launch {
            try {
                repo.withdraw(
                    amount = amountMinor,
                    paymentMethodId = paymentMethodId
                )

                val (wallet, txs) = repo.getMyWalletWithTransactions()
                _uiState.value = WalletUiState.Success(wallet.copy(transactions = txs))

                _withdrawEvents.emit(WithdrawEvent.Success)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("MIN_WITHDRAW_50K") == true -> "Số tiền rút tối thiểu là 50.000đ"
                    e.message?.contains("INSUFFICIENT_BALANCE") == true -> "Số dư không đủ"
                    e.message?.contains("PAYMENT_METHOD_REQUIRED") == true -> "Vui lòng chọn phương thức rút tiền"
                    e.message?.contains("WALLET_LOCKED") == true -> "Ví đang bị khóa"
                    else -> "Không thể rút tiền, vui lòng thử lại"
                }
                _withdrawEvents.emit(WithdrawEvent.Error(msg))
            }
        }
    }

    fun loadPaymentMethods() = viewModelScope.launch {
        val list = repo.getPaymentMethods()
        list.forEach {
            Log.d("PM_UI", "id=${it.id}, default=${it.isDefault}, provider=${it.provider}")
        }
        _paymentMethods.value = list
    }

    fun addPaymentMethod(req: AddPaymentMethodRequest) = viewModelScope.launch {
        runCatching {
            repo.addPaymentMethod(req)
        }.onSuccess { created ->
            // backend trả object hoàn chỉnh (id, isDefault, label, detail...)
            _paymentMethods.update { list ->
                if (list.isEmpty()) listOf(created) else list + created
            }
        }.onFailure {
            // optional: emit snackbar / log
        }
    }

    fun updatePaymentMethod(
        methodId: String,
        req: UpdatePaymentMethodRequest
    ) = viewModelScope.launch {

        runCatching {
            repo.updateMethod(methodId, req)
        }.onSuccess {
            loadPaymentMethods()
        }.onFailure {
            loadPaymentMethods()
        }
    }


    fun setDefaultPaymentMethod(id: String) = viewModelScope.launch {
        runCatching {
            repo.updateMethod(id, UpdatePaymentMethodRequest(isDefault = true))
        }.onSuccess {
            loadPaymentMethods()
        }.onFailure {
            loadPaymentMethods()
        }
    }
}
