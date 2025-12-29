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
                is AppResult.Success -> _conversations.value = res.data
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
            when (val res = chatRepository.getMessages(conversationId)) {
                is AppResult.Success -> {
                    _messages.value = res.data
                    val last = res.data.lastOrNull()
                    if (last != null) {
                        updateConversationPreview(conversationId, last, incrementUnread = false)
                    }
                }
                is AppResult.Error -> Unit
                AppResult.Loading -> Unit
            }
            _loading.value = false
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            when (val res = chatRepository.sendMessage(conversationId, text)) {
                is AppResult.Success -> {
                    val msg = res.data
                    _messages.update { it + msg }
                    updateConversationPreview(conversationId, msg, incrementUnread = false)
                }
                is AppResult.Error -> Unit
                AppResult.Loading -> Unit
            }
        }
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
                    else -> Unit
                }
            }
        }
    }

    private fun handleIncomingMessage(payload: ChatSocketPayload) {
        val message = payload.toChatMessage(currentUserId) ?: return
        val bookingId = message.conversationId
        if (bookingId == currentConversationId) {
            _messages.update { it + message }
            updateConversationPreview(bookingId, message, incrementUnread = false)
        } else {
            updateConversationPreview(bookingId, message, incrementUnread = true)
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

    private fun updateConversationSession(bookingId: String?, active: Boolean) {
        val targetId = bookingId?.trim().orEmpty()
        if (targetId.isBlank()) return
        _conversations.update { list ->
            list.map { convo ->
                if (convo.id == targetId) {
                    convo.copy(hasActiveSession = active)
                } else {
                    convo
                }
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
}
