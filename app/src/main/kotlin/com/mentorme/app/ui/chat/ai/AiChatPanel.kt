package com.mentorme.app.ui.chat.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mentorme.app.data.dto.ai.MentorWithExplanation
import com.mentorme.app.ui.components.ui.GlassOverlay
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.home.Mentor
import com.mentorme.app.ui.search.components.MentorDetailSheet
import com.mentorme.app.ui.theme.liquidGlassStrong@Composable
fun AiChatPanel(
    onMentorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    // ✅ State for GlassOverlay pattern (same as SearchScreen)
    var showDetail by rememberSaveable { mutableStateOf(false) }
    var selectedMentorId by remember { mutableStateOf<String?>(null) }

    val blurOn = showDetail
    val blurRadius = if (blurOn) 8.dp else 0.dp

    // ✅ Notify parent when overlay state changes (like SearchScreen)
    androidx.compose.runtime.LaunchedEffect(blurOn) {
        // Parent callback removed to keep this component self-contained
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // LAYER A: Main content (blur when modal shown)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
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

                                msg.mentors.forEach { mentor ->
                                    MentorSuggestCard(
                                        mentor = mentor,
                                        onClick = {
                                            // ✅ Store mentorId and show detail sheet
                                            // MentorDetailSheet will fetch full info from backend
                                            selectedMentorId = mentor.mentorId
                                            showDetail = true
                                        }
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }

                                // Show suggestions if available
                                if (msg.suggestions.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        msg.suggestions.take(2).forEach { suggestion ->
                                            SuggestionChip(
                                                onClick = {
                                                    input = suggestion
                                                    viewModel.ask(suggestion)
                                                    input = ""
                                                },
                                                label = { Text(suggestion) }
                                            )
                                        }
                                    }
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

        // LAYER B: GlassOverlay with MentorDetailSheet (same as SearchScreen)
        GlassOverlay(
            visible = blurOn,
            onDismiss = { showDetail = false; selectedMentorId = null },
            formModifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            selectedMentorId?.let { mentorId ->
                // ✅ Create temporary Mentor object - MentorDetailSheet will fetch full data
                val tempMentor = Mentor(
                    id = mentorId,
                    name = "Loading...",
                    role = "",
                    company = "",
                    rating = 0.0,
                    totalReviews = 0,
                    skills = emptyList(),
                    hourlyRate = 0,
                    imageUrl = "",
                    isAvailable = false
                )

                MentorDetailSheet(
                    mentorId = mentorId,
                    mentor = tempMentor,
                    onClose = {
                        showDetail = false
                        selectedMentorId = null
                    },
                    onBookNow = { _ ->
                        showDetail = false
                        selectedMentorId = null
                        // ✅ Trigger parent callback to navigate to booking
                        onMentorClick(mentorId)
                    },
                    onMessage = { mid ->
                        showDetail = false
                        selectedMentorId = null
                        // ✅ Trigger parent callback to navigate to chat
                        onMentorClick(mid)
                    }
                )
            }
        }
    }
}

@Composable
fun MentorSuggestCard(
    mentor: MentorWithExplanation,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = mentor.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mentor.fullName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )

                if (mentor.headline != null) {
                    Text(
                        mentor.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (mentor.rating?.average != null) {
                        Text(
                            "⭐ ${String.format("%.1f", mentor.rating.average)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFBBF24)
                        )
                    }

                    Text(
                        "${mentor.hourlyRateVnd.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}đ/h",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // AI Explanation
        if (mentor.explanation != null) {
            Spacer(Modifier.height(8.dp))
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "✨",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        mentor.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight.times(1.4f)
                    )
                }
            }
        }
    }
}

