package com.mentorme.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.realtime.RealtimeEvent
import com.mentorme.app.core.realtime.RealtimeEventBus
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.mapper.ChatSocketPayload
import com.mentorme.app.data.mapper.toChatMessage
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.data.repository.chat.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentConversationId: String? = null
    private var currentUserId: String? = null

    init {
        observeUser()
        refreshConversations()
        observeRealtime()
    }

    fun refreshConversations() {
        viewModelScope.launch {
            _loading.value = true
            when (val res = chatRepository.getConversations()) {
                is AppResult.Success -> {
                    _conversations.value = res.data
                    
                    // Load last message preview for each conversation
                    res.data.forEach { conversation ->
                        launch {
                            when (val msgRes = chatRepository.getMessages(conversation.id, limit = 1)) {
                                is AppResult.Success -> {
                                    val lastMsg = msgRes.data.lastOrNull()
                                    if (lastMsg != null) {
                                        updateConversationPreview(conversation.id, lastMsg, incrementUnread = false)
                                    }
                                }
                                is AppResult.Error -> Unit
                                AppResult.Loading -> Unit
                            }
                        }
                    }
                }
                is AppResult.Error -> Unit
                AppResult.Loading -> Unit
            }
            _loading.value = false
        }
    }

    fun openConversation(conversationId: String) {
        currentConversationId = conversationId
        markConversationRead(conversationId)
        loadMessages(conversationId)
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _loading.value = true
            
            // Load messages
            when (val res = chatRepository.getMessages(conversationId)) {
                is AppResult.Success -> {
                    val deduped = dedupeMessages(res.data)
                    _messages.value = deduped
                    
                    val last = deduped.lastOrNull()
                    if (last != null) {
                        updateConversationPreview(conversationId, last, incrementUnread = false)
                    }
                }
                is AppResult.Error -> {
                    _errorMessage.value = res.throwable
                }
                AppResult.Loading -> Unit
            }
            
            // Load restriction info separately
            val conversation = _conversations.value.find { it.id == conversationId }
            val bookingId = conversation?.primaryBookingId
            
            if (bookingId != null) {
                when (val restrictionRes = chatRepository.getChatRestrictionInfo(bookingId)) {
                is AppResult.Success -> {
                    val info = restrictionRes.data
                    _conversations.update { list ->
                        list.map { convo ->
                            if (convo.id == conversationId) {
                                convo.copy(
                                    myMessageCount = info.myMessageCount,
                                    sessionPhase = info.sessionPhase,
                                    preSessionCount = info.preSessionCount,
                                    postSessionCount = info.postSessionCount,
                                    weeklyMessageCount = info.weeklyMessageCount
                                )
                            } else {
                                convo
                            }
                        }
                    }
                }
                is AppResult.Error -> {
                    // Silently fail - not critical
                }
                AppResult.Loading -> Unit
            }
            }
            
            _loading.value = false
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _errorMessage.value = null
            
            // Get primaryBookingId from conversation
            val conversation = _conversations.value.find { it.id == conversationId }
            val bookingId = conversation?.primaryBookingId
            
            if (bookingId == null) {
                _errorMessage.value = "Cannot send message: Booking not found"
                return@launch
            }
            
            when (val res = chatRepository.sendMessage(bookingId, text)) {
                is AppResult.Success -> {
                    val msg = res.data
                    val isNew = addOrUpdateMessage(msg)
                    
                    // Update message count if message is from current user
                    if (isNew && msg.fromCurrentUser) {
                        _conversations.update { list ->
                            list.map { convo ->
                                if (convo.id == conversationId) {
                                    convo.copy(myMessageCount = convo.myMessageCount + 1)
                                } else {
                                    convo
                                }
                            }
                        }
                    }
                    
                    if (isNew) {
                        updateConversationPreview(conversationId, msg, incrementUnread = false)
                    }
                }
                is AppResult.Error -> {
                    _errorMessage.value = res.throwable
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun observeUser() {
        viewModelScope.launch {
            dataStoreManager.getUserId().collectLatest { id ->
                currentUserId = id
            }
        }
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            RealtimeEventBus.events.collect { event ->
                when (event) {
                    is RealtimeEvent.ChatMessageReceived -> handleIncomingMessage(event.payload)
                    is RealtimeEvent.SessionReady -> updateConversationSession(event.payload.bookingId, true)
                    is RealtimeEvent.SessionAdmitted -> updateConversationSession(event.payload.bookingId, true)
                    is RealtimeEvent.SessionWaiting -> updateConversationSession(event.payload.bookingId, true)
                    is RealtimeEvent.SessionParticipantJoined -> updateConversationSession(event.payload.bookingId, true)
                    is RealtimeEvent.SessionEnded -> updateConversationSession(event.payload.bookingId, false)
                    is RealtimeEvent.BookingChanged -> refreshConversations()
                    is RealtimeEvent.UserOnlineStatusChanged -> updateUserOnlineStatus(event.userId, event.isOnline)
                    else -> Unit
                }
            }
        }
    }

    private fun handleIncomingMessage(payload: ChatSocketPayload) {
        val message = payload.toChatMessage(currentUserId) ?: return
        val conversationId = message.conversationId
        if (conversationId == currentConversationId) {
            val isNew = addOrUpdateMessage(message)
            if (isNew) {
                if (message.fromCurrentUser) {
                    _conversations.update { list ->
                        list.map { convo ->
                            if (convo.id == conversationId) {
                                convo.copy(myMessageCount = convo.myMessageCount + 1)
                            } else {
                                convo
                            }
                        }
                    }
                }
                updateConversationPreview(conversationId, message, incrementUnread = false)
            }
        } else {
            updateConversationPreview(conversationId, message, incrementUnread = true)
        }
    }

    private fun updateConversationPreview(
        conversationId: String,
        message: Message,
        incrementUnread: Boolean
    ) {
        _conversations.update { list ->
            list.map { convo ->
                if (convo.id == conversationId) {
                    val newUnread = if (incrementUnread) convo.unreadCount + 1 else 0
                    convo.copy(
                        lastMessage = message.text,
                        lastMessageTimeIso = message.createdAtIso,
                        unreadCount = newUnread
                    )
                } else {
                    convo
                }
            }
        }
    }

    private fun addOrUpdateMessage(message: Message): Boolean {
        var isNew = false
        _messages.update { list ->
            val index = findMessageIndex(list, message)
            if (index == -1) {
                isNew = true
                list + message
            } else {
                val updated = list.toMutableList()
                updated[index] = message
                updated
            }
        }
        return isNew
    }

    private fun findMessageIndex(list: List<Message>, message: Message): Int {
        val messageId = message.id.trim()
        if (messageId.isNotEmpty()) {
            val byId = list.indexOfFirst { it.id == messageId }
            if (byId != -1) return byId
        }
        val fallbackKey = messageDedupeKey(message)
        return list.indexOfFirst { messageDedupeKey(it) == fallbackKey }
    }

    private fun dedupeMessages(messages: List<Message>): List<Message> {
        val seen = HashSet<String>(messages.size)
        val deduped = ArrayList<Message>(messages.size)
        for (message in messages) {
            val key = messageDedupeKey(message)
            if (seen.add(key)) {
                deduped.add(message)
            }
        }
        return deduped
    }

    private fun messageDedupeKey(message: Message): String {
        val conversationId = message.conversationId.trim()
        val timestamp = message.createdAtIso.trim()
        val text = message.text.trim()
        return "$conversationId|${message.fromCurrentUser}|$timestamp|$text"
    }

    private fun updateConversationSession(bookingId: String?, active: Boolean) {
        val targetId = bookingId?.trim().orEmpty()
        if (targetId.isBlank()) return
        _conversations.update { list ->
            list.map { convo ->
                val matches = targetId == convo.activeSessionBookingId ||
                    targetId == convo.primaryBookingId ||
                    targetId == convo.nextSessionBookingId
                if (!matches) return@map convo

                val nextActiveId = when {
                    active -> targetId
                    targetId == convo.activeSessionBookingId -> null
                    else -> convo.activeSessionBookingId
                }
                val nextHasActive = if (active) {
                    true
                } else if (targetId == convo.activeSessionBookingId) {
                    false
                } else {
                    convo.hasActiveSession
                }
                convo.copy(
                    hasActiveSession = nextHasActive,
                    activeSessionBookingId = nextActiveId
                )
            }
        }
    }

    private fun markConversationRead(conversationId: String) {
        _conversations.update { list ->
            list.map { convo ->
                if (convo.id == conversationId) convo.copy(unreadCount = 0) else convo
            }
        }
    }
    
    private fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        _conversations.update { list ->
            list.map { convo ->
                if (convo.peerId == userId) {
                    convo.copy(isOnline = isOnline)
                } else {
                    convo
                }
            }
        }
    }
}
