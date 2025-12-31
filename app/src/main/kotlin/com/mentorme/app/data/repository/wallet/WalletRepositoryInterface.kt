package com.mentorme.app.data.repository.wallet

import com.mentorme.app.data.dto.wallet.TopUpIntentDto

interface WalletRepositoryInterface {

    suspend fun createTopUpIntent(
        amount: Long,
        currency: String
    ): TopUpIntentDto

    suspend fun confirmTopUpTransfer(intentId: String)

    suspend fun getMyPendingTopUps(): List<TopUpIntentDto>
}
