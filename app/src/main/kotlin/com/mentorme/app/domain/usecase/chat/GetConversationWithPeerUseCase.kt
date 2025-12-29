package com.mentorme.app.domain.usecase.chat

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.repository.chat.ChatRepository
import javax.inject.Inject

/**
 * Use case to get or find existing conversation with a specific peer (mentor/mentee).
 * Returns conversationId if conversation exists, null if not.
 */
class GetConversationWithPeerUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(peerId: String): AppResult<String?> {
        if (peerId.isBlank()) {
            return AppResult.failure("Peer ID cannot be blank")
        }

        return when (val result = chatRepository.getConversationByPeerId(peerId)) {
            is AppResult.Success -> {
                // Conversation model has id field which is the conversationId
                AppResult.success(result.data?.id)
            }
            is AppResult.Error -> AppResult.failure(result.throwable)
            AppResult.Loading -> AppResult.failure("Loading")
        }
    }
}

