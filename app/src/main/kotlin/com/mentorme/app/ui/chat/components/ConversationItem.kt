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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
            // Avatar with online status indicator
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar container with shadow when online
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .then(
                            if (conversation.isOnline) {
                                Modifier.shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFF22C55E).copy(alpha = 0.5f),
                                    spotColor = Color(0xFF22C55E).copy(alpha = 0.5f)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (conversation.peerAvatar != null) {
                        AsyncImage(
                            model = conversation.peerAvatar,
                            contentDescription = conversation.peerName,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .matchParentSize()
                                .gradientBackground(GradientSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.peerId == "ai" || conversation.peerName == "MentorMe AI") {
                                    "🤖"
                                } else {
                                    conversation.peerName.firstOrNull()?.uppercase() ?: "?"
                                },
                                style = if (conversation.peerId == "ai") {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Online status badge
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF1F2937), CircleShape) // Dark background
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF22C55E), CircleShape) // Green indicator
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.peerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    if (conversation.unreadCount > 0) {
                        UnreadPill(conversation.unreadCount)
                    }
                }
                Text(
                    text = when (conversation.peerRole.lowercase()) {
                        "mentor" -> "Mentor"
                        "mentee" -> "Mentee"
                        else -> conversation.peerRole
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFBBF24) // Amber/Gold color for role - nổi bật trên nền glass
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = conversation.lastMessage,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
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
