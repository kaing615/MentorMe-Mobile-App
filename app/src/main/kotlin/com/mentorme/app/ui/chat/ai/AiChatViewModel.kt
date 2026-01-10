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

    fun ask(message: String) {
        if (message.isBlank()) return

        _messages.update {
            it + AiChatMessage.User(message)
        }

        viewModelScope.launch {
            _loading.value = true

            val res = aiRepository.recommendMentor(message)

            res.fold(
                onSuccess = { mentors ->
                    _messages.update {
                        it + AiChatMessage.Ai(
                            text = "Dựa trên yêu cầu của anh, em gợi ý các mentor phù hợp",
                            mentors = mentors
                        )
                    }
                },
                onFailure = {
                    _messages.update {
                        it + AiChatMessage.Ai(
                            text = "Em chưa tìm được mentor phù hợp TvT",
                            mentors = emptyList()
                        )
                    }
                }
            )
            _loading.value = false
        }
    }
}