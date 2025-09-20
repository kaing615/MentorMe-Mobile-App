package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
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
) {
    val repo = remember { ChatRepositoryImpl() }
    var query by remember { mutableStateOf("") }
    val conversations by remember { mutableStateOf(repo.getConversations()) }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            Modifier
                .fillMaxSize()
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
                leading = { Icon(Icons.Default.Search, contentDescription = null) },
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
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { c ->
                    ConversationCard(conversation = c, onClick = { onOpenConversation(c.id) })
                }

            }
        }
    }
}
