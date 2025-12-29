package com.mentorme.app.data.repository.chat.impl

import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.Message as ApiMessage
import com.mentorme.app.data.dto.SendMessageRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import com.mentorme.app.data.network.api.chat.ChatApiService
import com.mentorme.app.data.repository.BookingRepository
import com.mentorme.app.data.repository.chat.ChatRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatApiService: ChatApiService,
    private val bookingRepository: BookingRepository,
    private val dataStoreManager: DataStoreManager
) : ChatRepository {

    override suspend fun getConversations(): AppResult<List<Conversation>> = withContext(Dispatchers.IO) {
        try {
            val role = dataStoreManager.getUserRole().first()
            val userId = dataStoreManager.getUserId().first()
            val response = if (role.isNullOrBlank()) {
                bookingRepository.getBookings(page = 1, limit = 50)
            } else {
                bookingRepository.getBookings(role = role, page = 1, limit = 50)
            }

            if (response is AppResult.Success) {
                val bookings = response.data.bookings
                val filtered = bookings.filterNot { booking ->
                    booking.status == BookingStatus.CANCELLED ||
                        booking.status == BookingStatus.FAILED ||
                        booking.status == BookingStatus.DECLINED
                }

                val conversations = filtered.map { booking ->
                    toConversation(booking, userId)
                }.sortedByDescending { it.lastMessageTimeIso }

                AppResult.success(conversations)
            } else if (response is AppResult.Error) {
                AppResult.failure(response.throwable)
            } else {
                AppResult.failure("Failed to load conversations")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to load conversations")
        }
    }

    override suspend fun getMessages(
        conversationId: String,
        limit: Int
    ): AppResult<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val userId = dataStoreManager.getUserId().first()
            val response = chatApiService.getMessages(conversationId, limit, null)
            if (response.isSuccessful) {
                val envelope: ApiEnvelope<List<ApiMessage>>? = response.body()
                if (envelope?.success == true && envelope.data != null) {
                    val mapped = envelope.data.mapNotNull { dto ->
                        dto.toChatMessage(userId)
                    }
                    AppResult.success(mapped)
                } else {
                    AppResult.failure(envelope?.message ?: "Failed to load messages")
                }
            } else {
                AppResult.failure("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to load messages")
        }
    }

    override suspend fun sendMessage(conversationId: String, text: String): AppResult<Message> =
        withContext(Dispatchers.IO) {
            try {
                val userId = dataStoreManager.getUserId().first()
                val request = SendMessageRequest(bookingId = conversationId, content = text)
                val response = chatApiService.sendMessage(request)
                if (response.isSuccessful) {
                    val envelope: ApiEnvelope<ApiMessage>? = response.body()
                    val dto = envelope?.data
                    if (envelope?.success == true && dto != null) {
                        val mapped = dto.toChatMessage(userId)
                        if (mapped != null) {
                            AppResult.success(mapped)
                        } else {
                            AppResult.failure("Failed to parse message")
                        }
                    } else {
                        AppResult.failure(envelope?.message ?: "Failed to send message")
                    }
                } else {
                    AppResult.failure("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Failed to send message")
            }
        }

    override suspend fun getChatRestrictionInfo(conversationId: String): AppResult<ChatRestrictionInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.getChatRestrictionInfo(conversationId)
                if (response.isSuccessful) {
                    val envelope: ApiEnvelope<ChatRestrictionInfo>? = response.body()
                    if (envelope?.success == true && envelope.data != null) {
                        AppResult.success(envelope.data)
                    } else {
                        AppResult.failure(envelope?.message ?: "Failed to get restriction info")
                    }
                } else {
                    AppResult.failure("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Failed to get restriction info")
            }
        }
}

private fun ApiMessage.toChatMessage(currentUserId: String?): Message? {
    val messageId = id.trim()
    val bookingId = bookingId.trim()
    if (messageId.isBlank() || bookingId.isBlank()) return null
    val created = timestamp ?: ""
    return Message(
        id = messageId,
        conversationId = bookingId,
        text = content,
        createdAtIso = created,
        fromCurrentUser = senderId == currentUserId,
        senderName = sender?.fullName,
        senderAvatar = sender?.avatar
    )
}

private fun toConversation(booking: Booking, currentUserId: String?): Conversation {
    val isMentor = booking.mentorId == currentUserId
    val peerSummary = if (isMentor) booking.mentee else booking.mentor
    val peerId = if (isMentor) booking.menteeId else booking.mentorId
    val peerName = peerSummary?.fullName
        ?: (if (isMentor) booking.menteeFullName else booking.mentorFullName)
        ?: "Unknown"
    val peerRole = if (isMentor) "mentee" else "mentor"
    val startIso = booking.startTimeIso ?: booking.createdAt
    val endIso = booking.endTimeIso
    val hasActive = isSessionActive(booking)
    val hasUpcoming = booking.status == BookingStatus.CONFIRMED && !hasActive
    val nextSessionIso = if (hasUpcoming) startIso else null

    return Conversation(
        id = booking.id,
        peerId = peerId,
        peerName = peerName,
        peerAvatar = peerSummary?.avatar,
        peerRole = peerRole,
        isOnline = false,
        lastMessage = "No messages yet",
        lastMessageTimeIso = startIso,
        unreadCount = 0,
        hasActiveSession = hasActive,
        nextSessionDateTimeIso = nextSessionIso,
        nextSessionStartIso = if (hasUpcoming) startIso else null,
        nextSessionEndIso = if (hasUpcoming) endIso else null,
        nextSessionBookingId = if (hasUpcoming) booking.id else null,
        bookingStatus = booking.status,
        myMessageCount = 0 // Will be updated when messages are loaded
    )
}

private fun isSessionActive(booking: Booking): Boolean {
    if (booking.status != BookingStatus.CONFIRMED) return false
    val start = runCatching { Instant.parse(booking.startTimeIso ?: "") }.getOrNull()
    val end = runCatching { Instant.parse(booking.endTimeIso ?: "") }.getOrNull()
    val now = Instant.now()
    if (start == null || end == null) return false
    return !now.isBefore(start) && !now.isAfter(end)
}
