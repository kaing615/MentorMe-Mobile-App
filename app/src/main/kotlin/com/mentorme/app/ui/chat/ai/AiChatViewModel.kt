package com.mentorme.app.ui.chat.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.data.repository.ai.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        // Add welcome message
        _messages.value = listOf(
            AiChatMessage.Ai(
                text = "Xin chào! 👋 Tôi là trợ lý AI của MentorMe.\n\nTôi có thể giúp bạn:\n• Tìm mentor phù hợp theo kỹ năng và ngân sách\n• Trả lời các câu hỏi về tính năng app\n• Giải đáp chính sách và quy định\n\nBạn muốn tôi hỗ trợ điều gì? 😊",
                type = AiResponseType.GENERAL,
                suggestions = listOf(
                    "Tìm mentor Java cho người mới",
                    "Làm sao để đăng ký mentor?",
                    "App có những tính năng gì?"
                )
            )
        )
    }

    fun ask(message: String) {
        if (message.isBlank()) return

        // Add user message
        _messages.update {
            it + AiChatMessage.User(message)
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = aiRepository.chatWithAi(message)

            result.fold(
                onSuccess = { response ->
                    when (response.type) {
                        "mentor_recommend" -> {
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = response.answer ?: "Dựa trên yêu cầu của bạn, tôi gợi ý các mentor phù hợp:",
                                    type = AiResponseType.MENTOR_RECOMMEND,
                                    mentors = response.mentors ?: emptyList(),
                                    aiAnalysis = response.ai,
                                    suggestions = response.suggestions ?: emptyList()
                                )
                            }
                        }
                        "app_qa" -> {
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = response.answer ?: "Xin lỗi, tôi không có thông tin về vấn đề này.",
                                    type = AiResponseType.APP_QA,
                                    suggestions = response.suggestions ?: emptyList()
                                )
                            }
                        }
                        "general_response" -> {
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = response.answer ?: "Xin chào! Tôi có thể giúp gì cho bạn?",
                                    type = AiResponseType.GENERAL,
                                    suggestions = response.suggestions ?: emptyList()
                                )
                            }
                        }
                        else -> {
                            // Unknown type, treat as general
                            _messages.update {
                                it + AiChatMessage.Ai(
                                    text = response.answer ?: "Tôi đã nhận được câu hỏi của bạn.",
                                    type = AiResponseType.GENERAL,
                                    suggestions = response.suggestions ?: emptyList()
                                )
                            }
                        }
                    }
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Đã xảy ra lỗi"
                    _messages.update {
                        it + AiChatMessage.Ai(
                            text = "Xin lỗi, tôi gặp sự cố kỹ thuật. Vui lòng thử lại sau! 🙏",
                            type = AiResponseType.GENERAL
                        )
                    }
                }
            )
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}