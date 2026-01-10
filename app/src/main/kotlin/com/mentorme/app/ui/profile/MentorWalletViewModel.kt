package com.mentorme.app.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.data.dto.wallet.MentorPayoutDto
import com.mentorme.app.data.dto.wallet.PayoutStatus
import com.mentorme.app.data.remote.WalletApi
import com.mentorme.app.data.repository.wallet.WalletRepository
import com.mentorme.app.ui.wallet.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MentorWalletViewModel @Inject constructor(
    private val payoutRepository: WalletRepository,
    private val walletApi: WalletApi
) : ViewModel() {

    private val _payouts = MutableStateFlow<List<MentorPayoutDto>>(emptyList())
    val payouts: StateFlow<List<MentorPayoutDto>> = _payouts

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPayouts() {
        viewModelScope.launch {
            try {
                val res = walletApi.getMyPayouts()
                _payouts.value = res.data?.items ?: emptyList()

                Log.d(
                    "MentorWalletVM",
                    "Loaded payouts = ${_payouts.value.size}"
                )
            } catch (e: Exception) {
                Log.e("MentorWalletVM", "loadPayouts failed", e)
            }
        }
    }

    fun createPayout(amountMinor: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                payoutRepository.createPayout(amountMinor)
                loadPayouts()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun canCreatePayout(
        balance: Long,
        payouts: List<MentorPayoutDto>,
        methods: List<PaymentMethod>
    ): Boolean {
        if (balance <= 0) return false
        if (methods.isEmpty()) return false

        if (
            payouts.any {
                it.status == PayoutStatus.PENDING ||
                        it.status == PayoutStatus.PROCESSING
            }
        ) return false

        return true
    }
}
