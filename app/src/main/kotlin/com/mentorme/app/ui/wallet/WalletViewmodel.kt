package com.mentorme.app.ui.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.mentorme.app.data.dto.paymentMethods.AddPaymentMethodRequest
import com.mentorme.app.data.dto.paymentMethods.UpdatePaymentMethodRequest
import com.mentorme.app.data.dto.wallet.TopUpIntentDto
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.repository.wallet.WalletRepository
import com.mentorme.app.data.repository.wallet.WalletRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.data.model.NotificationType
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class WalletUiState {
    object Loading : WalletUiState()
    data class Success(val wallet: WalletDto) : WalletUiState()
    data class Error(val message: String?) : WalletUiState()
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repo: WalletRepository,
    private val repoInterface: WalletRepositoryInterface
) : ViewModel() {

    init {
        RealtimeEventBus.events
            .filterIsInstance<RealtimeEvent.NotificationReceived>()
            .filter { it.notification.type == NotificationType.TOPUP_SUCCESS }
            .onEach {
                loadWallet()
                _topUpEvents.emit(TopUpEvent.Approved)
            }
            .launchIn(viewModelScope)

        // Listen to booking changes to refresh wallet transactions
        RealtimeEventBus.events
            .filterIsInstance<RealtimeEvent.BookingChanged>()
            .onEach {
                Log.d("WalletViewModel", "🔄 Booking changed, refreshing wallet")
                loadWallet()
            }
            .launchIn(viewModelScope)
    }

    private val _uiState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val uiState: StateFlow<WalletUiState> = _uiState

    private val _topUpEvents = MutableSharedFlow<TopUpEvent>(replay = 1)
    val topUpEvents = _topUpEvents

    private val _topUpMethod = MutableStateFlow(TopUpMethod.MoMo)
    val topUpMethod = _topUpMethod.asStateFlow()

    private val _withdrawEvents = MutableSharedFlow<WithdrawEvent>(replay = 1)
    val withdrawEvents = _withdrawEvents

    sealed class TopUpEvent {
        object Submitted : TopUpEvent()
        object Approved : TopUpEvent()
        data class Error(val message: String?) : TopUpEvent()
    }

    sealed class WithdrawEvent {
        object Success : WithdrawEvent()
        data class Error(val message: String) : WithdrawEvent()
    }

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods = _paymentMethods.asStateFlow()

    fun setTopUpMethod(method: TopUpMethod) {
        _topUpMethod.value = method
    }

    fun ensureLoaded() {
        if (_uiState.value is WalletUiState.Loading) {
            loadWallet()
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            try {
                Log.d("WalletViewModel", "🔄 Loading wallet and transactions...")
                val (wallet, txs) = repo.getMyWalletWithTransactions()
                Log.d("WalletViewModel", "✅ Loaded wallet: balance=${wallet.balanceMinor}, transactions=${txs.size}")
                txs.take(5).forEachIndexed { i, tx ->
                    Log.d("WalletViewModel", "  TX[$i]: ${tx.source} ${tx.type} ${tx.amount} - ${tx.description}")
                }

                _uiState.value = WalletUiState.Success(
                    wallet.copy(transactions = txs)
                )
            } catch (e: Exception) {
                Log.e("WalletViewModel", "❌ Error loading wallet: ${e.message}")
                _uiState.value = WalletUiState.Error(e.message)
            }
        }
    }

    private val _topUpIntent = MutableStateFlow<TopUpIntentDto?>(null)
    val topUpIntent = _topUpIntent.asStateFlow()

    fun startTopUp(amountMinor: Long) {
        viewModelScope.launch {
            try {
                val intent = repoInterface.createTopUpIntent(
                    amount = amountMinor,
                    currency = "VND"
                )
                _topUpIntent.value = intent
            } catch (e: Exception) {
                _topUpEvents.emit(TopUpEvent.Error(e.message))
            }
        }
    }

    fun confirmTransferred() {
        viewModelScope.launch {
            try {
                repoInterface.confirmTopUpTransfer(requireNotNull(_topUpIntent.value).id)
                _topUpEvents.emit(TopUpEvent.Submitted)
                _topUpIntent.value = null
            } catch (e: Exception) {
                _topUpEvents.emit(TopUpEvent.Error(e.message))
            }
        }
    }

    fun clearTopUpIntent() {
        _topUpIntent.value = null
    }

    fun loadMyTopUpIntents() = viewModelScope.launch {
        try {
            val list = repoInterface.getMyPendingTopUps()
        } catch (e: Exception) { null }
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
