package com.mentorme.app.data.repository.wallet

import android.util.Log
import com.mentorme.app.data.dto.paymentMethods.AddPaymentMethodRequest
import com.mentorme.app.data.dto.paymentMethods.UpdatePaymentMethodRequest
import com.mentorme.app.data.dto.wallet.DebitRequest
import com.mentorme.app.data.dto.wallet.DebitResponse
import com.mentorme.app.data.dto.wallet.TopupRequest
import com.mentorme.app.data.dto.wallet.TopupResponse
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.dto.wallet.WalletTransactionDto
import com.mentorme.app.data.mapper.toDomain
import com.mentorme.app.data.remote.WalletApi
import com.mentorme.app.ui.profile.WalletTx
import com.mentorme.app.data.mapper.toUi
import com.mentorme.app.data.remote.PaymentMethodApi
import com.mentorme.app.ui.wallet.PaymentMethod
import retrofit2.Response
import java.util.UUID

class WalletRepository(
    private val api: WalletApi,
    private val paymentMethodApi: PaymentMethodApi
) {

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

    suspend fun withdraw(
        amount: Long,
        paymentMethodId: String?
    ): DebitResponse {
        val req = DebitRequest(
            amount = amount,
            currency = "VND",
            clientRequestId = UUID.randomUUID().toString(),
            paymentMethodId = paymentMethodId
        )

        val res = api.withdraw(req)

        if (!res.isSuccessful || res.body() == null) {
            throw Exception("WITHDRAW_FAILED_${res.code()}")
        }

        return res.body()!!
    }

    suspend fun getMyWalletWithTransactions(): Pair<WalletDto, List<WalletTransactionDto>> {
        val wallet = api.getMyWallet().data
        val txs = api.getWalletTransactions().data.items
        return wallet to txs
    }

    suspend fun getPaymentMethods(): List<PaymentMethod> {
        return paymentMethodApi.getMethods()
            .data
            .map { it.toDomain() }
    }

    suspend fun addPaymentMethod(req: AddPaymentMethodRequest) =
        paymentMethodApi.addMethod(req).data

    suspend fun updateMethod(id: String, req: UpdatePaymentMethodRequest) =
        paymentMethodApi.update(id, req).data
}
