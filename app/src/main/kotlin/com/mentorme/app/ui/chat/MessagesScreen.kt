package com.mentorme.app.ui.chat

import android.R.id
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.chat.components.ConversationCard
import com.mentorme.app.ui.chat.components.GlassIconButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.data.model.chat.Conversation

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessagesScreen(
    onOpenConversation: (String) -> Unit,
    onFilterMentors: () -> Unit = {},
    onSearchConversations: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    var query by remember { mutableStateOf("") }
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    val AI_CONVERSATION_ID = "ai_conversation"

    val aiConversation = Conversation(
        id = AI_CONVERSATION_ID,
        peerId = "ai",
        peerName = "MentorMe AI",
        peerAvatar = null,
        peerRole = "AI",
        lastMessage = "Hỏi AI để tìm mentor phù hợp",
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
            base.filter {
                it.peerName.contains(query, ignoreCase = true)
            }
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(                // chừa status bar + lề ngang
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title + subtitle (giống Web)
            Text("Tin nhắn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Trao đổi với mentor của bạn", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            // Search (glass)
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
                    contentDescription = "Filter mentors",
                    onClick = onFilterMentors
                )
            }

            Spacer(Modifier.height(12.dp))

            val filtered = remember(query, conversations) {
                if (query.isBlank()) conversations
                else conversations.filter { it.peerName.contains(query, ignoreCase = true) }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(combinedConversations) { c ->
                    ConversationCard(
                        conversation = c,
                        onClick = {
                            if (c.id == AI_CONVERSATION_ID) {
                                onOpenConversation(AI_CONVERSATION_ID)
                            } else {
                                onOpenConversation(c.id)
                            }
                        }
                    )
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
