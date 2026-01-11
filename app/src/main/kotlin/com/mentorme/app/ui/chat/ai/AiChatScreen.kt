package com.mentorme.app.ui.chat.ai

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mentorme.app.ui.chat.components.ChatComposer
import com.mentorme.app.ui.chat.components.GlassIconButton
import com.mentorme.app.ui.components.ui.GlassOverlay
import com.mentorme.app.ui.home.Mentor
import com.mentorme.app.ui.search.components.MentorDetailSheet
import com.mentorme.app.ui.theme.liquidGlassStrong
import com.mentorme.app.data.repository.ai.AiChatMode
import com.mentorme.app.data.dto.ai.MentorWithExplanation

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AiChatScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    mode: AiChatMode = AiChatMode.MENTEE
) {
    val viewModel = hiltViewModel<AiChatViewModel>()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    // ✅ State for GlassOverlay pattern (same as AiChatPanel)
    var showDetail by rememberSaveable { mutableStateOf(false) }
    var selectedMentor by remember { mutableStateOf<MentorWithExplanation?>(null) }

    val blurOn = showDetail
    val blurRadius = if (blurOn) 8.dp else 0.dp

    val listState = rememberLazyListState()
    val subtitle = remember(mode) {
        when (mode) {
            AiChatMode.MENTEE -> "Trợ lý tìm mentor"
            AiChatMode.MENTOR -> "Trợ lý cho mentor"
        }
    }
    val placeholder = remember(mode) {
        when (mode) {
            AiChatMode.MENTEE -> "Hỏi AI về mentor bạn cần..."
            AiChatMode.MENTOR -> "Hỏi AI về lịch rảnh, booking, payout..."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // LAYER A: Main content (blur when modal shown)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .liquidGlassStrong(radius = 24.dp, alpha = 0.28f)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    onClick = onBack
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        "MentorMe AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.Delete, "Xóa lịch sử")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .liquidGlassStrong(radius = 24.dp, alpha = 0.22f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        when (msg) {
                            is AiChatMessage.User -> {
                                AiMessageBubble(
                                    text = msg.text,
                                    isMine = true
                                )
                            }

                            is AiChatMessage.Ai -> {
                                AiMessageBubble(  // ✅ Changed from MessageBubbleGlass
                                    text = msg.text,
                                    isMine = false
                                )

                                Spacer(Modifier.height(6.dp))

                                if (mode == AiChatMode.MENTEE) {
                                    msg.mentors.forEach { mentor ->
                                        MentorSuggestCard(
                                            mentor = mentor,
                                            onClick = {
                                                // ✅ Store full mentor object and show detail sheet
                                                selectedMentor = mentor
                                                showDetail = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }
            }

            ChatComposer(
                onSend = { text ->
                    viewModel.ask(text)
                },
                enabled = true,
                placeholder = placeholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(12.dp)
            )
        }
    }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    } // End blur Box

        // LAYER B: GlassOverlay with MentorDetailSheet (same as AiChatPanel)
        GlassOverlay(
            visible = blurOn,
            onDismiss = {
                showDetail = false
                selectedMentor = null
            },
            formModifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            selectedMentor?.let { mentor ->
                // ✅ Create Mentor object from MentorWithExplanation data
                val tempMentor = Mentor(
                    id = mentor.mentorId,
                    name = mentor.fullName,
                    role = mentor.headline ?: "",
                    company = "",
                    rating = mentor.rating?.average ?: 0.0,
                    totalReviews = mentor.rating?.count ?: 0,
                    skills = emptyList(),
                    hourlyRate = mentor.hourlyRateVnd,
                    imageUrl = mentor.avatarUrl ?: "",
                    isAvailable = false
                )

                MentorDetailSheet(
                    mentorId = mentor.mentorId,
                    mentor = tempMentor,
                    onClose = {
                        showDetail = false
                        selectedMentor = null
                    },
                    onBookNow = { _ ->
                        showDetail = false
                        selectedMentor = null
                        // ✅ Trigger parent callback to navigate to booking
                        onOpenProfile(mentor.mentorId)
                    },
                    onMessage = { mid ->
                        showDetail = false
                        selectedMentor = null
                        // ✅ Trigger parent callback to navigate to chat
                        onOpenProfile(mid)
                    }
                )
            }
        }
    } // End outer Box
}

@Composable
fun ChatHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .liquidGlassStrong(radius = 24.dp, alpha = 0.28f)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            onClick = onBack
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AiMessageBubble(
    text: String,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // ✅ AI Bot Avatar (only for bot messages)
        if (!isMine) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isMine)
                Color(0xFF6366F1).copy(alpha = 0.85f)
            else
                Color.White.copy(alpha = 0.12f)
        ) {
            Text(
                text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



