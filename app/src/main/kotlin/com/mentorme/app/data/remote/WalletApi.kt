package com.mentorme.app.data.remote

import com.mentorme.app.data.dto.wallet.TopupRequest
import com.mentorme.app.data.dto.wallet.TopupResponse
import com.mentorme.app.data.dto.wallet.DebitRequest
import com.mentorme.app.data.dto.wallet.DebitResponse
import com.mentorme.app.data.dto.wallet.WalletDto
import com.mentorme.app.data.dto.wallet.WalletTransactionListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApi {

    @POST("wallet/topups/mock")
    suspend fun mockTopup(
        @Body req: TopupRequest
    ): TopupResponse

    @POST("wallet/debits/mock")
    suspend fun mockDebit(
        @Body req: DebitRequest
    ): Response<DebitResponse>

    @GET("wallet/me")
    suspend fun getMyWallet(): ApiResponse<WalletDto>

    @GET("wallet/transactions")
    suspend fun getWalletTransactions(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
        @Query("type") type: String? = null,
        @Query("source") source: String? = null
    ): ApiResponse<WalletTransactionListResponse>
}
