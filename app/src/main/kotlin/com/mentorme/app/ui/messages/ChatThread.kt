package com.mentorme.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.chat.Conversation
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.ui.chat.MessageBubbleGlass
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun ChatThread(
    conversation: Conversation,
    messages: List<Message>,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onJoinSession: () -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(Modifier.fillMaxSize()) {

            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .liquidGlassStrong(radius = 20.dp, alpha = 0.28f)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
                Spacer(Modifier.width(8.dp))
                Text(
                    conversation.peerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                GlassIconButton(icon = Icons.Filled.Info, contentDescription = "Profile", onClick = onOpenProfile)
            }

            // Session banner (mock)
            if (conversation.hasActiveSession) {
                ElevatedCard(
                    Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .liquidGlassStrong(radius = 20.dp, alpha = 0.26f)
                        .background(Color(0xFF22C55E).copy(alpha = 0.18f)),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Upcoming/Active Session",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("With ${conversation.peerName}")
                        Spacer(Modifier.height(8.dp))
                        MMButton(
                            text = "Join now",
                            onClick = onJoinSession,
                            useGlass = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White
                            )
                        )
                    }
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
            ) {
                items(messages) { m ->
                    MessageBubbleGlass(m)
                    Spacer(Modifier.height(8.dp))
                }
            }

            HorizontalDivider()

            // Composer
            ChatComposer(onSend = onSend)
        }
    }
}
