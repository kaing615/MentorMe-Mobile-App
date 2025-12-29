package com.mentorme.app.data.repository.wallet

import android.util.Log
import com.mentorme.app.data.dto.wallet.TopupRequest
import com.mentorme.app.data.dto.wallet.TopupResponse
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.dto.wallet.WalletTransactionDto
import com.mentorme.app.data.remote.WalletApi
import com.mentorme.app.ui.profile.WalletTx
import com.mentorme.app.data.mapper.toUi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(private val api: WalletApi) {

    suspend fun mockTopup(amount: Long, currency: String = "VND", clientRequestId: String): TopupResponse {
        val req =
            TopupRequest(amount = amount, currency = currency, clientRequestId = clientRequestId)
        return api.mockTopup(req)
    }
    suspend fun getMyWallet(): WalletDto? {
        val res = api.getMyWallet()
        Log.d("WalletRepo", "balanceMinor = ${res.data.balanceMinor}")
        return res.data
    }

    suspend fun getMyWalletWithTransactions(): Pair<WalletDto, List<WalletTransactionDto>> {
        val wallet = api.getMyWallet().data
        val txs = api.getWalletTransactions().data.items
        return wallet to txs
    }
}
