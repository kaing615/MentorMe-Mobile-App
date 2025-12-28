package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.ui.theme.GradientSecondary
import com.mentorme.app.ui.theme.gradientBackground
import com.mentorme.app.ui.theme.liquidGlass

@Composable
fun ConversationCard(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(radius = 22.dp)
            .clickable(onClick = onClick),
        tonalElevation = 0.dp,
        color = Color.Transparent
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar text fallback
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .gradientBackground(GradientSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(conversation.peerName.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold)
                // Online dot
                if (conversation.isOnline) {
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFF11E37C))
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(conversation.peerName, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    if (conversation.unreadCount > 0) {
                        UnreadPill(conversation.unreadCount)
                    }
                }
                Text(conversation.peerRole, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(conversation.lastMessage, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.width(10.dp))
            Text(
                text = shortTime(conversation.lastMessageTimeIso),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun UnreadPill(count: Int) {
    Box(
        Modifier
            .padding(start = 4.dp)
            .clip(CircleShape)
            .gradientBackground(GradientSecondary)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", fontWeight = FontWeight.SemiBold)
    }
}

private fun shortTime(iso: String): String =
    iso.take(10) // mock: yyyy-MM-dd → đủ để demo giống "14 thg 1"
