package com.mentorme.app.ui.chat.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.data.repository.ai.AiChatHistoryManager
import com.mentorme.app.data.repository.ai.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val historyManager: AiChatHistoryManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = historyManager.loadMessages()
            if (history.isEmpty()) {
                // Add welcome message
                _messages.value = listOf(
                    AiChatMessage.Ai(
                        text = "Xin chào! 👋 Tôi là trợ lý AI của MentorMe.\n\nTôi có thể giúp bạn tìm mentor phù hợp!",
                        type = "text",
                        suggestions = listOf(
                            "Tìm mentor Java cho người mới",
                            "App có những tính năng gì?"
                        )
                    )
                )
            } else {
                _messages.value = history
            }
        }
    }

    fun ask(message: String) {
        if (message.isBlank()) return

        _messages.update { it + AiChatMessage.User(message) }
        saveHistory()

        viewModelScope.launch {
            _loading.value = true

            val res = aiRepository.askAi(message)

            res.fold(
                onSuccess = { response ->
                    val aiMsg = when (response.type) {
                        "app_qa" -> AiChatMessage.Ai(
                            text = response.answer ?: "Em không hiểu câu hỏi này",
                            type = "app_qa",
                            suggestions = response.suggestions ?: emptyList()
                        )
                        "mentor_recommend" -> {
                            val mentors = response.mentors ?: emptyList()
                            AiChatMessage.Ai(
                                text = if (mentors.isNotEmpty()) {
                                    "Dựa trên yêu cầu của bạn, em gợi ý ${mentors.size} mentor phù hợp:"
                                } else {
                                    "Em chưa tìm thấy mentor phù hợp"
                                },
                                mentors = mentors,
                                type = "mentor_recommend",
                                suggestions = response.suggestions ?: emptyList()
                            )
                        }
                        else -> AiChatMessage.Ai(
                            text = response.answer ?: "Em không hiểu",
                            type = "text",
                            suggestions = response.suggestions ?: emptyList()
                        )
                    }
                    _messages.update { it + aiMsg }
                    saveHistory()
                },
                onFailure = { error ->
                    _messages.update {
                        it + AiChatMessage.Ai(
                            text = "Xin lỗi, em gặp lỗi: ${error.message}",
                            type = "text"
                        )
                    }
                    saveHistory()
                }
            )
            _loading.value = false
        }
    }

    private fun saveHistory() {
        viewModelScope.launch {
            historyManager.saveMessages(_messages.value)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyManager.clearHistory()
            _messages.value = emptyList()
            loadHistory() // Reload welcome message
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            historyManager.saveMessages(_messages.value)
        }
    }
}