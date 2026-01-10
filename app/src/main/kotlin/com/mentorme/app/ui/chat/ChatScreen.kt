package com.mentorme.app.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.mentorme.app.ui.theme.liquidGlassStrong
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.core.utils.DateTimeUtils
import com.mentorme.app.ui.chat.ai.AiChatPanel
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
    onJoinSession: (String) -> Unit,
    onOpenBookingDetail: (String) -> Unit = {},
    onOpenProfile: (mentorId: String) -> Unit
) {
    val viewModel = hiltViewModel<ChatViewModel>()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val peerTypingStatus by viewModel.peerTypingStatus.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val conversation = remember(conversations, conversationId) {
        conversations.find { it.id == conversationId }
    }
    val joinBookingId = conversation?.activeSessionBookingId
        ?: conversation?.primaryBookingId
        ?: conversation?.nextSessionBookingId
    var showProfile by remember { mutableStateOf(false) }

    // Typing indicator from real-time events
    val isTyping = remember(peerTypingStatus, conversation) {
        conversation?.peerId?.let { peerTypingStatus[it] } ?: false
    }

    // Scroll to bottom button
    var showScrollToBottom by remember { mutableStateOf(false) }

    // Chat restriction logic
    val isMenteeChattingWithMentor = conversation?.peerRole == "mentor"
    val isBookingConfirmed =
        conversation?.bookingStatus == com.mentorme.app.data.model.BookingStatus.CONFIRMED
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

    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        val listState = rememberLazyListState()
        var composerHeightPx by remember { mutableStateOf(0) }
        val density = LocalDensity.current

        // phần IME lấn thêm so với nav bar (px)
        val imeBottomPx = WindowInsets.ime.getBottom(density)
        val navBottomPx = WindowInsets.navigationBars.getBottom(density)
        val extraImePx = (imeBottomPx - navBottomPx).coerceAtLeast(0)
        val composerOverlayDp = with(density) {
            (composerHeightPx - extraImePx).coerceAtLeast(0).toDp()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
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
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        onClick = onBack
                    )
                    Spacer(Modifier.width(4.dp))

                    // Avatar with online badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { showProfile = true },
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar container with shadow, border and gradient
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .then(
                                    if (conversation?.isOnline == true) {
                                        Modifier.shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            ambientColor = Color(0xFF6366F1).copy(
                                                alpha = 0.5f
                                            ),
                                            spotColor = Color(0xFF6366F1).copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF8B5CF6)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!conversation?.peerAvatar.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(conversation?.peerAvatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(
                                        android.R.drawable.ic_menu_gallery
                                    ),
                                    error = painterResource(
                                        android.R.drawable.ic_menu_gallery
                                    ),
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = conversation?.peerName?.firstOrNull()
                                        ?.uppercase() ?: "?",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Online status badge with pulse animation
                        if (conversation?.isOnline == true) {
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.5f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        1000,
                                        easing = LinearEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFF1F2937), CircleShape)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0xFF22C55E).copy(alpha = alpha),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Column(
                        Modifier
                            .weight(1f)
                            .clickable { showProfile = true }
                    ) {
                        Text(
                            conversation?.peerName ?: "Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        // Role badge
                        if (conversation?.peerRole != null) {
                            Text(
                                text = when (conversation.peerRole.lowercase()) {
                                    "mentor" -> "Mentor"
                                    "mentee" -> "Mentee"
                                    else -> conversation.peerRole
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFBBF24), // Amber/Gold color for role
                                maxLines = 1
                            )
                        }

                        // Status text with typing indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isTyping) {
                                // Typing indicator with animated dots
                                Text(
                                    "đang nhập",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF22C55E),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                                Spacer(Modifier.width(2.dp))

                                // Animated dots
                                val infiniteTransition =
                                    rememberInfiniteTransition(label = "typing")
                                val dotCount by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 3f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(
                                            1000,
                                            easing = LinearEasing
                                        ),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "dots"
                                )

                                Text(
                                    ".".repeat((dotCount.toInt() % 3) + 1)
                                        .padEnd(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    when {
                                        conversation?.isOnline == true -> "Đang hoạt động"
                                        conversation?.lastSeenAt != null -> {
                                            val lastSeen = try {
                                                DateTimeUtils.formatLastSeen(
                                                    conversation.lastSeenAt
                                                )
                                            } catch (e: Exception) {
                                                "Không hoạt động"
                                            }
                                            lastSeen
                                        }

                                        else -> "Không hoạt động"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (conversation?.isOnline == true) Color(
                                        0xFF22C55E
                                    ) else Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    GlassIconButton(
                        icon = Icons.Filled.MoreVert,
                        contentDescription = null,
                        onClick = { showProfile = true })
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
                                Text(
                                    "Phiên tư vấn đang diễn ra",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Cùng ${conversation.peerName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            MMButton(
                                text = "Tham gia",
                                onClick = { joinBookingId?.let(onJoinSession) },
                                useGlass = false,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A), // deep green
                                    contentColor = Color.White
                                ),
                                enabled = joinBookingId != null
                            )
                        }
                    }
                }

                // Banner phiên học sắp tới - chỉ hiển thị khi không có phiên active
                // Ưu tiên hiển thị active session nếu có, vì nút join cần active session
                if (conversation?.nextSessionStartIso != null && conversation.hasActiveSession == false) {
                    val formattedDateTime =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            DateTimeUtils.formatIsoToReadable(conversation.nextSessionStartIso)
                        } else {
                            conversation.nextSessionStartIso ?: ""
                        }

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
                                Text(
                                    "Phiên học sắp tới",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Với ${conversation.peerName} - $formattedDateTime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            MMButton(
                                text = "Xem chi tiết",
                                onClick = {
                                    // Ưu tiên activeSessionBookingId nếu có, vì có thể session đã bắt đầu
                                    val bookingIdToOpen =
                                        conversation.activeSessionBookingId
                                            ?: conversation.nextSessionBookingId
                                    bookingIdToOpen?.let { bookingId ->
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

                    // Scroll to Bottom FAB
                    val coroutineScope = rememberCoroutineScope()
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.lastIndex)
                                    }
                                }
                            },
                            containerColor = Color(0xFF6366F1).copy(alpha = 0.9f),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to bottom",
                                modifier = Modifier.size(24.dp)
                            )
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
                    onSendFile = { uri, fileName ->
                        if (canSendMessage) {
                            viewModel.sendFileMessage(conversationId, uri, fileName)
                        }
                    },
                    enabled = canSendMessage,
                    placeholder = if (!canSendMessage) disabledPlaceholder else "Nhập tin nhắn...",
                    onTypingChanged = { isTyping ->
                        conversation?.peerId?.let { peerId ->
                            viewModel.emitTypingStatus(peerId, isTyping)
                        }
                    },
                    isUploading = isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .onGloballyPositioned {
                            composerHeightPx = it.size.height
                        }
                )

                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                }

                // Track scroll position for "scroll to bottom" button
                LaunchedEffect(listState.firstVisibleItemIndex, messages.size) {
                    val lastItemIndex =
                        if (messages.isNotEmpty()) messages.lastIndex else 0
                    // Show button if not near bottom (more than 2 items away)
                    showScrollToBottom = messages.isNotEmpty() &&
                            listState.firstVisibleItemIndex < lastItemIndex - 2
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

        val context = androidx.compose.ui.platform.LocalContext.current

        // Show error toast
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                android.widget.Toast.makeText(
                    context,
                    error,
                    android.widget.Toast.LENGTH_LONG
                ).show()
                // Clear error after showing
                viewModel.clearError()
            }
        }
    }
}
