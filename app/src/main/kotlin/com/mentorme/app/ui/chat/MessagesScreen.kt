package com.mentorme.app.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mentorme.app.data.mock.MockData

// Preview model cho danh sách hội thoại
private data class ConversationPreview(
    val mentorId: String,
    val mentorName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController) {
    // Nguồn dữ liệu: tạo preview từ danh sách mentor (vì chưa có mockBookings)
    val previews = remember {
        val msgs = listOf(
            "Thanks for the great session!",
            "See you next week!",
            "Can we reschedule to 3 PM?",
            "I’ve reviewed your task—good job!",
            "Please check the note I sent."
        )
        val times = listOf("2 hours ago", "yesterday", "Mon 10:30", "Sun 18:45", "Fri 09:00")

        MockData.mockMentors.mapIndexed { idx, m ->
            ConversationPreview(
                mentorId = m.id,
                mentorName = m.fullName,          // KHỚP models mới
                lastMessage = msgs[idx % msgs.size],
                timestamp = times[idx % times.size],
                unreadCount = 0
            )
        }
    }

    // Nếu SAU NÀY có mockBookings (schema mới):
    // val previews = remember {
    //   MockData.mockBookings.map { b ->
    //     val mName = MockData.mockMentors.firstOrNull { it.id == b.mentorId }?.fullName ?: "Unknown"
    //     ConversationPreview(
    //       mentorId = b.mentorId,
    //       mentorName = mName,
    //       lastMessage = "Thanks for the great session!",
    //       timestamp = "${b.date} ${b.startTime}",
    //       unreadCount = 0
    //     )
    //   }
    // }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(previews) { item ->
                ConversationItem(
                    mentorName = item.mentorName,
                    lastMessage = item.lastMessage,
                    timestamp = item.timestamp,
                    unreadCount = item.unreadCount,
                    onClick = {
                        // Navigate to chat detail nếu có route: navController.navigate("chat/${item.mentorId}")
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    mentorName: String,
    lastMessage: String,
    timestamp: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Message, contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(mentorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (unreadCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) {
                        Text(
                            text = unreadCount.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
