package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.chat.components.ConversationCard
import com.mentorme.app.ui.chat.components.GlassIconButton
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.data.model.chat.Conversation

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MentorMessagesScreen(
    onOpenConversation: (String) -> Unit = {},
    onFilterStudents: () -> Unit = {},
    onSearchConversations: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    var query by remember { mutableStateOf("") }
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val AI_CONVERSATION_ID = "mentor_ai_conversation"
    val aiConversation = Conversation(
        id = AI_CONVERSATION_ID,
        peerId = "ai",
        peerName = "MentorMe AI",
        peerAvatar = null,
        peerRole = "AI",
        lastMessage = "H?i AI v? h? tr? mentor",
        lastMessageTimeIso = "",
        unreadCount = 0,
        isOnline = true,
        bookingStatus = BookingStatus.CONFIRMED
    )

    val combinedConversations = remember(conversations, query) {
        val base = listOf(aiConversation) + conversations
        if (query.isBlank()) {
            base
        } else {
            base.filter { it.peerName.contains(query, ignoreCase = true) }
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Tin nhắn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Trao đổi với học viên của bạn", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MMTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        onSearchConversations(it)
                    },
                    placeholder = "Tìm kiếm cuộc trò chuyện…",
                    leading = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlass(radius = 24.dp)
                )
                Spacer(Modifier.width(10.dp))
                GlassIconButton(
                    icon = Icons.Default.FilterList,
                    contentDescription = "Filter students",
                    onClick = onFilterStudents
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(combinedConversations) { c ->
                    ConversationCard(conversation = c, onClick = { onOpenConversation(c.id) })
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshConversations()
    }
    
    // Refresh conversations when screen becomes visible to catch any missed realtime events
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Initial refresh
            viewModel.refreshConversations()
            
            // Then poll periodically while screen is visible
            while (true) {
                kotlinx.coroutines.delay(10000) // Poll every 10 seconds
                viewModel.refreshConversations()
            }
        }
    }
}

