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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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
    onJoinSession: () -> Unit,
    onOpenBookingDetail: (String) -> Unit = {}
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val conversation = remember(conversations, conversationId) {
        conversations.find { it.id == conversationId }
    }
    var showProfile by remember { mutableStateOf(false) }

    // Chat restriction logic
    val isMenteeChattingWithMentor = conversation?.peerRole == "mentor"
    val isBookingConfirmed = conversation?.bookingStatus == com.mentorme.app.data.model.BookingStatus.CONFIRMED
    val sessionPhase = conversation?.sessionPhase ?: "outside"
    val myMessageCount = conversation?.myMessageCount ?: 0
    val preSessionCount = conversation?.preSessionCount ?: 0
    val postSessionCount = conversation?.postSessionCount ?: 0
    val weeklyMessageCount = conversation?.weeklyMessageCount ?: 0
    
    val maxFreeMessages = 10
    val preSessionLimit = 5
    val postSessionLimit = 3
    val weeklyLimit = 2
    
    val canSendMessage = when {
        !isMenteeChattingWithMentor -> true  // Mentor always can send
        !isBookingConfirmed -> myMessageCount < maxFreeMessages  // Not confirmed: 10 free messages
        sessionPhase == "during" -> false  // During session: chat disabled
        sessionPhase == "pre" -> preSessionCount < preSessionLimit  // Pre-session: 5 messages
        sessionPhase == "post" -> postSessionCount < postSessionLimit  // Post-session: 3 messages
        else -> weeklyMessageCount < weeklyLimit  // Outside window: 2 messages/week
    }
    
    val showWarning = when {
        !isMenteeChattingWithMentor -> false
        !isBookingConfirmed -> myMessageCount in (maxFreeMessages - 3) until maxFreeMessages
        sessionPhase == "pre" -> preSessionCount in (preSessionLimit - 2) until preSessionLimit
        sessionPhase == "post" -> postSessionCount in (postSessionLimit - 1) until postSessionLimit
        sessionPhase == "outside" -> weeklyMessageCount in (weeklyLimit - 1) until weeklyLimit
        else -> false
    }
    
    val warningMessage = when {
        !isMenteeChattingWithMentor -> ""
        !isBookingConfirmed -> "Còn ${maxFreeMessages - myMessageCount} tin nhắn miễn phí. Đặt lịch tư vấn để tiếp tục!"
        sessionPhase == "pre" -> "Còn ${preSessionLimit - preSessionCount}/${preSessionLimit} tin nhắn chuẩn bị. Dành câu hỏi chính cho phiên tư vấn!"
        sessionPhase == "post" -> "Còn ${postSessionLimit - postSessionCount}/${postSessionLimit} tin nhắn follow-up."
        sessionPhase == "outside" -> "Còn ${weeklyLimit - weeklyMessageCount}/${weeklyLimit} tin nhắn tuần này."
        else -> ""
    }
    
    val disabledPlaceholder = when {
        !isMenteeChattingWithMentor -> ""
        !isBookingConfirmed -> "Đã hết ${maxFreeMessages} tin nhắn miễn phí. Vui lòng đặt lịch tư vấn."
        sessionPhase == "during" -> "Phiên tư vấn đang diễn ra. Vui lòng trao đổi trực tiếp với mentor."
        sessionPhase == "pre" -> "Đã hết ${preSessionLimit} tin nhắn chuẩn bị. Dành câu hỏi cho phiên tư vấn nhé!"
        sessionPhase == "post" -> "Đã hết ${postSessionLimit} tin nhắn follow-up. Đặt phiên mới nếu cần tư vấn thêm."
        sessionPhase == "outside" -> "Đã hết ${weeklyLimit} tin nhắn/tuần. Hỏi nhiều qua chat sẽ mất giá trị phiên tư vấn."
        else -> ""
    }

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
            
            // Banner phiên học sắp tới
            if (conversation?.nextSessionStartIso != null && conversation.hasActiveSession == false) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .liquidGlassStrong(radius = 20.dp, alpha = 0.26f)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.18f)),
                    tonalElevation = 0.dp,
                    color = Color.Transparent
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Phiên học sắp tới", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Với ${conversation.peerName} - ${conversation.nextSessionDateTimeIso.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        MMButton(
                            text = "Xem chi tiết",
                            onClick = { 
                                conversation.nextSessionBookingId?.let { bookingId ->
                                    onOpenBookingDetail(bookingId)
                                }
                            },
                            useGlass = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            )
                        )
                    }
                }
            }

            // Warning banner for message limit
            if (showWarning && warningMessage.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .liquidGlassStrong(radius = 20.dp, alpha = 0.26f)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.18f)),
                    tonalElevation = 0.dp,
                    color = Color.Transparent
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ $warningMessage",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Messages list with background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .liquidGlassStrong(radius = 24.dp, alpha = 0.22f)
                    .background(Color(0xFF000000).copy(alpha = 0.15f))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    reverseLayout = false,
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = composerOverlayDp + 12.dp
                    )
                ) {
                    items(messages) { m ->
                        MessageBubbleGlass(m)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // Composer (glass + imePadding)
            ChatComposer(
                onSend = { text ->
                    if (canSendMessage) {
                        viewModel.sendMessage(conversationId, text)
                    }
                },
                enabled = canSendMessage,
                placeholder = if (!canSendMessage) disabledPlaceholder else "Nhập tin nhắn...",
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
            
            // Refresh messages when screen becomes visible to catch any missed realtime events
            LaunchedEffect(conversationId, lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    // Initial load
                    viewModel.loadMessages(conversationId)
                    
                    // Then poll periodically while screen is visible
                    while (true) {
                        kotlinx.coroutines.delay(5000) // Poll every 5 seconds
                        viewModel.loadMessages(conversationId)
                    }
                }
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