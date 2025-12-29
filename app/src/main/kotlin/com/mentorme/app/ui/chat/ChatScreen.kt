package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.mentorme.app.ui.theme.liquidGlassStrong
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.chat.components.ChatComposer
import com.mentorme.app.ui.chat.components.GlassIconButton
import com.mentorme.app.ui.chat.components.MessageBubbleGlass
import com.mentorme.app.ui.chat.components.ProfileSheet

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onJoinSession: () -> Unit
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val conversation = remember(conversations, conversationId) {
        conversations.find { it.id == conversationId }
    }
    var showProfile by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        val listState = rememberLazyListState()
        var composerHeightPx by remember { mutableStateOf(0) }
        val density = LocalDensity.current

        // phần IME lấn thêm so với nav bar (px)
        val imeBottomPx = WindowInsets.ime.getBottom(density)
        val navBottomPx = WindowInsets.navigationBars.getBottom(density)
        val extraImePx = (imeBottomPx - navBottomPx).coerceAtLeast(0)
        val composerOverlayDp = with(density) { (composerHeightPx - extraImePx).coerceAtLeast(0).toDp() }

        Column(modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
        ) {
            // Header glass
            Row(
                modifier = Modifier
                    .windowInsetsPadding(                // chừa status bar + lề ngang
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
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
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = composerOverlayDp + 12.dp
                )
            ) {
                items(messages) { m ->
                    MessageBubbleGlass(m)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Composer (glass + imePadding)
            ChatComposer(
                onSend = { text ->
                    viewModel.sendMessage(conversationId, text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .onGloballyPositioned { composerHeightPx = it.size.height }
            )

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }
            LaunchedEffect(conversationId) {
                viewModel.openConversation(conversationId)
            }
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
