package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.repository.chat.impl.ChatRepositoryImpl
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.liquidGlass

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessagesScreen(
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = remember { ChatRepositoryImpl() }
    var query by remember { mutableStateOf("") }
    val conversations by remember { mutableStateOf(repo.getConversations()) }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = Modifier
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
            MMTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Tìm kiếm cuộc trò chuyện…",
                leading = { Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(radius = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            // List
            val filtered = remember(query, conversations) {
                if (query.isBlank()) conversations
                else conversations.filter { it.peerName.contains(query, ignoreCase = true) }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 45.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filtered) { c ->
                    ConversationCard(conversation = c, onClick = { onOpenConversation(c.id) })
                }

            }
        }
    }
}
