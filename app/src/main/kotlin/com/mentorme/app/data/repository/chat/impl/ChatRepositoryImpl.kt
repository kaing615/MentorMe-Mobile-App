package com.mentorme.app.data.repository.chat.impl

import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.Message as ApiMessage
import com.mentorme.app.data.dto.SendMessageRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.home.PresenceLookupRequest
import com.mentorme.app.data.model.Booking
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import com.mentorme.app.data.network.api.chat.ChatApiService
import com.mentorme.app.data.network.api.home.HomeApiService
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
    private val homeApiService: HomeApiService,
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

                // Group bookings by peerId to create one conversation per mentor-mentee pair
                val groupedByPeer = filtered.groupBy { booking ->
                    val isMentor = booking.mentorId == userId
                    if (isMentor) booking.menteeId else booking.mentorId
                }

                val conversations = groupedByPeer.map { (peerId, peerBookings) ->
                    // Use the most recent booking for conversation data
                    val latestBooking = peerBookings.maxByOrNull { 
                        it.startTimeIso ?: it.createdAt 
                    } ?: peerBookings.first()
                    
                    toConversation(latestBooking, userId, peerBookings)
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

    override suspend fun getConversationByPeerId(peerId: String): AppResult<Conversation?> = withContext(Dispatchers.IO) {
        try {
            val role = dataStoreManager.getUserRole().first()
            val userId = dataStoreManager.getUserId().first()

            // Get all bookings to find conversation with this peer
            val response = if (role.isNullOrBlank()) {
                bookingRepository.getBookings(page = 1, limit = 50)
            } else {
                bookingRepository.getBookings(role = role, page = 1, limit = 50)
            }

            if (response is AppResult.Success) {
                val bookings = response.data.bookings

                // Find bookings with this specific peer (not cancelled/failed/declined)
                val peerBookings = bookings.filter { booking ->
                    val isMentor = booking.mentorId == userId
                    val bookingPeerId = if (isMentor) booking.menteeId else booking.mentorId
                    val isValidStatus = booking.status != BookingStatus.CANCELLED &&
                        booking.status != BookingStatus.FAILED &&
                        booking.status != BookingStatus.DECLINED

                    bookingPeerId == peerId && isValidStatus
                }

                if (peerBookings.isEmpty()) {
                    // No conversation exists with this peer
                    return@withContext AppResult.success(null)
                }

                // Use the most recent booking for conversation data
                val latestBooking = peerBookings.maxByOrNull {
                    it.startTimeIso ?: it.createdAt
                } ?: peerBookings.first()

                val conversation = toConversation(latestBooking, userId, peerBookings)
                AppResult.success(conversation)
            } else if (response is AppResult.Error) {
                AppResult.failure(response.throwable)
            } else {
                AppResult.failure("Failed to check conversation")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to check conversation")
        }
    }

    override suspend fun getMessages(
        conversationId: String,
        limit: Int
    ): AppResult<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val userId = dataStoreManager.getUserId().first()
            // conversationId is now peerId, use getMessagesByPeer endpoint
            val response = chatApiService.getMessagesByPeer(conversationId, limit, null)
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

    override suspend fun getOnlinePeerIds(peerIds: List<String>): AppResult<Set<String>> =
        withContext(Dispatchers.IO) {
            try {
                val normalized = peerIds
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(200)

                if (normalized.isEmpty()) {
                    return@withContext AppResult.success(emptySet())
                }

                val response = homeApiService.lookupPresence(PresenceLookupRequest(userIds = normalized))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val online = body.data?.onlineUserIds
                            ?.map { it.trim() }
                            ?.filter { it.isNotBlank() }
                            ?.toSet()
                            ?: emptySet()
                        AppResult.success(online)
                    } else {
                        AppResult.failure("Invalid presence response")
                    }
                } else {
                    AppResult.failure("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                AppResult.failure(e.message ?: "Failed to lookup presence")
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

private fun toConversation(booking: Booking, currentUserId: String?, allBookings: List<Booking> = listOf(booking)): Conversation {
    val isMentor = booking.mentorId == currentUserId
    val peerSummary = if (isMentor) booking.mentee else booking.mentor
    val peerId = if (isMentor) booking.menteeId else booking.mentorId
    val peerName = peerSummary?.fullName
        ?: (if (isMentor) booking.menteeFullName else booking.mentorFullName)
        ?: "Unknown"
    val peerRole = if (isMentor) "mentee" else "mentor"
    
    // Find active or most recent confirmed booking for session info
    val activeBooking = allBookings.firstOrNull { isSessionActive(it) }
    val confirmedBookings = allBookings.filter { it.status == BookingStatus.CONFIRMED }
    val upcomingBooking = confirmedBookings
        .filter { !isSessionActive(it) }
        .minByOrNull { it.startTimeIso ?: "" }
    
    val sessionBooking = activeBooking ?: upcomingBooking
    val startIso = sessionBooking?.startTimeIso ?: booking.startTimeIso ?: booking.createdAt
    val endIso = sessionBooking?.endTimeIso
    val hasActive = activeBooking != null
    val activeBookingId = activeBooking?.id
    val hasUpcoming = upcomingBooking != null && !hasActive
    val nextSessionIso = if (hasUpcoming) upcomingBooking?.startTimeIso else null

    // Use the first booking ID as primary booking for sending messages and restrictions
    val primaryBookingId = allBookings.firstOrNull()?.id ?: booking.id

    return Conversation(
        id = peerId, // Use peerId as conversation ID
        peerId = peerId,
        primaryBookingId = primaryBookingId,
        peerName = peerName,
        peerAvatar = peerSummary?.avatar,
        peerRole = peerRole,
        isOnline = false,
        lastMessage = "No messages yet",
        lastMessageTimeIso = startIso,
        unreadCount = 0,
        hasActiveSession = hasActive,
        activeSessionBookingId = activeBookingId,
        nextSessionDateTimeIso = nextSessionIso,
        nextSessionStartIso = if (hasUpcoming) upcomingBooking?.startTimeIso else null,
        nextSessionEndIso = if (hasUpcoming) upcomingBooking?.endTimeIso else null,
        nextSessionBookingId = if (hasUpcoming) upcomingBooking?.id else null,
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
