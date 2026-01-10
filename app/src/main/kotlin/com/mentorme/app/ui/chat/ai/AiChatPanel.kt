package com.mentorme.app.ui.chat.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mentorme.app.data.dto.mentors.MentorCardDto
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun AiChatPanel(
    onMentorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxWidth()
            .liquidGlassStrong(radius = 24.dp, alpha = 0.25f)
            .padding(12.dp)
    ) {
        Text(
            "🤖 Trợ lý AI – tìm mentor",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 220.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                when (msg) {
                    is AiChatMessage.User -> {
                        Text("🧑‍💻 ${msg.text}")
                    }

                    is AiChatMessage.Ai -> {
                        Text("🤖 ${msg.text}")

                        Spacer(Modifier.height(6.dp))

                        // Show mentor cards if available
                        if (msg.mentors.isNotEmpty()) {
                            msg.mentors.forEach { mentor ->
                                MentorSuggestCard(
                                    mentor = mentor,
                                    onClick = { onMentorClick(mentor.id) }
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Show suggestions if available
                        if (msg.suggestions.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            AiSuggestionChips(
                                suggestions = msg.suggestions,
                                onSuggestionClick = { suggestion ->
                                    input = suggestion
                                    viewModel.ask(suggestion)
                                    input = ""
                                }
                            )
                        }
                    }
                }
            }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))

        Row {
            MMTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = "Nhập nhu cầu (VD: Java backend, DevOps...)",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MMButton(
                text = "Hỏi AI",
                enabled = input.isNotBlank(),
                onClick = {
                    viewModel.ask(input)
                    input = ""
                }
            )
        }
    }
}

@Composable
fun MentorSuggestCard(
    mentor: MentorCardDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = mentor.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                mentor.name ?: "Mentor",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                mentor.skills?.joinToString(", ") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

