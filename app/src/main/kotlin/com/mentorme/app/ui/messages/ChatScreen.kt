package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mentorme.app.ui.theme.liquidGlassStrong
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.repository.chat.impl.ChatRepositoryImpl
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.messages.ChatComposer
import com.mentorme.app.ui.messages.GlassIconButton

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onJoinSession: () -> Unit
) {
    val repo = remember { ChatRepositoryImpl() }
    val conversation = remember { repo.getConversations().find { it.id == conversationId } }
    var messages by remember { mutableStateOf(repo.getMessages(conversationId)) }
    var showProfile by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(Modifier.fillMaxSize()) {
            // Header glass
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .liquidGlassStrong(radius = 24.dp, alpha = 0.28f)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, onClick = onBack)
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f)) {
                    Text(conversation?.peerName ?: "Chat", fontWeight = FontWeight.SemiBold)
                    Text(if (conversation?.isOnline == true) "Online" else "Offline", style = MaterialTheme.typography.labelSmall)
                }
                GlassIconButton(icon = Icons.Filled.MoreVert, contentDescription = null, onClick = { showProfile = true })
            }

            // (Optional) banner phiên tư vấn (glassy + green tint)
            if (conversation?.hasActiveSession == true) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .liquidGlassStrong(radius = 20.dp, alpha = 0.26f)
                        .background(Color(0xFF22C55E).copy(alpha = 0.18f)),
                    tonalElevation = 0.dp,
                    color = Color.Transparent
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Phiên tư vấn đang diễn ra", fontWeight = FontWeight.SemiBold)
                            Text("Cùng ${conversation.peerName}", style = MaterialTheme.typography.bodySmall)
                        }
                        MMButton(
                            text = "Tham gia",
                            onClick = onJoinSession,
                            useGlass = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A), // deep green
                                contentColor = Color.White
                            )
                        )
                    }
                }
            }

            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
            ) {
                items(messages) { m ->
                    MessageBubbleGlass(m)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Composer (glass + imePadding)
            ChatComposer(
                onSend = { text ->
                    val msg = repo.sendMessage(conversationId, text)
                    messages = messages + msg
                }
            )
        }
    }

    if (showProfile && conversation != null) {
        ProfileSheet(
            name = conversation.peerName,
            role = conversation.peerRole,
            onClose = { showProfile = false }
        )
    }
}
