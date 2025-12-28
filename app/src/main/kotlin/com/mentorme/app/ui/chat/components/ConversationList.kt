package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.chat.Conversation

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    onOpen: (Conversation) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(conversations) { c ->
            ConversationCard(
                conversation = c,
                onClick = { onOpen(c) }
            )
        }
    }
}
