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
    private val historyManager: AiChatHistoryManager  // Inject
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        // Load history khi khởi tạo
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = historyManager.loadMessages()
            _messages.value = history
        }
    }

    fun ask(message: String) {
        if (message.isBlank()) return

        _messages.update {
            it + AiChatMessage.User(message)
        }
        
        // Save after adding user message
        saveHistory()

        viewModelScope.launch {
            _loading.value = true

            val res = aiRepository.askAi(message)

            res.fold(
                onSuccess = { response ->
                    when (response.type) {
                        "app_qa" -> {
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = response.answer ?: "Em không hiểu câu hỏi này",
                                    type = "app_qa"
                                )
                            }
                        }
                        "mentor_recommend" -> {
                            val mentors = response.mentors ?: emptyList()
                            val text = if (mentors.isNotEmpty()) {
                                "Dựa trên yêu cầu của anh, em gợi ý ${mentors.size} mentor phù hợp:"
                            } else {
                                "Em chưa tìm thấy mentor phù hợp với yêu cầu này"
                            }
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = text,
                                    mentors = mentors,
                                    type = "mentor_recommend"
                                )
                            }
                        }
                        else -> {
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = "Em không hiểu câu hỏi này",
                                    type = "text"
                                )
                            }
                        }
                    }
                    
                    // Save after AI response
                    saveHistory()
                },
                onFailure = { error ->
                    _messages.update {
                        it + AiChatMessage.Ai(
                            text = "Xin lỗi, em gặp lỗi khi xử lý: ${error.message}",
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
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Save trước khi ViewModel bị destroy
        viewModelScope.launch {
            historyManager.saveMessages(_messages.value)
        }
    }
}