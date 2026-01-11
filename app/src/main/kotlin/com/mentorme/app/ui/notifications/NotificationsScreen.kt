package com.mentorme.app.ui.notifications

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.R
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.navigation.Routes
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import kotlinx.coroutines.launch

private enum class NotificationFilter(@StringRes val labelRes: Int) {
    ALL(R.string.notification_filter_all),
    UNREAD(R.string.notification_filter_unread),
    READ(R.string.notification_filter_read)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {},
    onJoinSession: (String) -> Unit = {},
    showDetailDialog: Boolean = true,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    var filter by remember { mutableStateOf(NotificationFilter.ALL) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val notifications by NotificationStore.notifications.collectAsState()
    val unreadCount = notifications.count { !it.read }
    val filtered = remember(filter, notifications) {
        when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.read }
            NotificationFilter.READ -> notifications.filter { it.read }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LiquidBackground(modifier = Modifier.matchParentSize())

        androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.notifications_title), fontWeight = FontWeight.Bold)
                                if (unreadCount > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3B30))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$unreadCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MMGhostButton(
                        onClick = { viewModel.markAllRead() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = unreadCount > 0
                    ) {
                        Icon(Icons.Outlined.DoneAll, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.notification_mark_all_read))
                    }

                    NotificationFilterBar(
                        selected = filter,
                        unreadCount = unreadCount,
                        onSelect = { filter = it }
                    )

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            NotificationEmptyState(filter = filter)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                val detailRoute = item.deepLink?.takeIf { it.isNotBlank() }
                                    ?: Routes.notificationDetail(item.id)
                                val showDialog = showDetailDialog && item.deepLink.isNullOrBlank()
                                NotificationRow(
                                    item = item,
                                    onOpenDetail = {
                                        if (showDialog) {
                                            selectedNotification = item
                                        } else {
                                            onOpenDetail(detailRoute)
                                        }
                                    },
                                    onJoinSession = onJoinSession,
                                    onMarkRead = { viewModel.markRead(it) },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }

        selectedNotification?.let { item ->
            NotificationDetailDialog(
                item = item,
                onDismiss = { selectedNotification = null }
            )
        }
    }
}

@Composable
private fun NotificationFilterBar(
    selected: NotificationFilter,
    unreadCount: Int,
    onSelect: (NotificationFilter) -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.08f)) // ✅ Frosted Mist: Soft glass pill
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape) // ✅ Gentle border
            .padding(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NotificationFilterOption(
                label = stringResource(NotificationFilter.ALL.labelRes),
                selected = selected == NotificationFilter.ALL,
                onClick = { onSelect(NotificationFilter.ALL) },
                modifier = Modifier.weight(0.9f) // ✅ Nhỏ hơn chút
            )
            NotificationFilterOption(
                label = stringResource(NotificationFilter.UNREAD.labelRes),
                selected = selected == NotificationFilter.UNREAD,
                badgeCount = unreadCount,
                onClick = { onSelect(NotificationFilter.UNREAD) },
                modifier = Modifier.weight(1.2f) // ✅ Rộng hơn để chứa badge
            )
            NotificationFilterOption(
                label = stringResource(NotificationFilter.READ.labelRes),
                selected = selected == NotificationFilter.READ,
                onClick = { onSelect(NotificationFilter.READ) },
                modifier = Modifier.weight(0.9f) // ✅ Nhỏ hơn chút
            )
        }
    }
}

@Composable
private fun NotificationFilterOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val baseModifier = modifier
        .clip(shape)
        .clickable(onClick = onClick)

    val containerModifier = if (selected) {
        baseModifier
            .background(Color.White.copy(alpha = 0.2f)) // ✅ Active: Distinct frosted layer
            .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
    } else {
        baseModifier
            .background(Color.Transparent)
    }

    Box(modifier = containerModifier, contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.coerceAtMost(99).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyState(
    filter: NotificationFilter
) {
    LiquidGlassCard(radius = 22.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
            Text(
                text = when (filter) {
                    NotificationFilter.UNREAD -> stringResource(R.string.notification_empty_unread)
                    NotificationFilter.READ -> stringResource(R.string.notification_empty_read)
                    NotificationFilter.ALL -> stringResource(R.string.notification_empty_all)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.notification_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onOpenDetail: () -> Unit,
    onJoinSession: (String) -> Unit = {},
    onMarkRead: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val typeStyle = notificationTypeStyle(item.type)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Extract booking ID from deepLink
    val bookingId: String? = item.deepLink?.let { link ->
        when {
            link.startsWith("booking_detail/") -> link.removePrefix("booking_detail/")
            link.startsWith("video_call/") -> link.removePrefix("video_call/")
            else -> null
        }
    }
    
    val showJoinButton = bookingId != null && (
        item.type == com.mentorme.app.data.model.NotificationType.BOOKING_REMINDER ||
        item.type == com.mentorme.app.data.model.NotificationType.BOOKING_CONFIRMED
    )
    var joinWindowState by remember(bookingId) {
        mutableStateOf(NotificationsViewModel.JoinWindowState.UNKNOWN)
    }

    LaunchedEffect(bookingId, showJoinButton) {
        if (showJoinButton && bookingId != null) {
            joinWindowState = viewModel.getJoinWindowState(bookingId)
        }
    }
    
    // ✅ Soft & Airy Frosted Mist Style: White tint with gentle alpha
    val cardAlpha = if (!item.read) 0.20f else 0.08f // Unread: Distinct frosted | Read: Very subtle
    val borderAlpha = 0.25f // ✅ Soft white outline
    val cardTint = Color.White // ✅ White tint for Frosted Mist
    val unreadOverlay = if (!item.read) Color(0xFF22C55E).copy(alpha = 0.08f) else Color.Transparent // ✅ Very subtle green tint

    LiquidGlassCard(
        radius = 22.dp,
        alpha = cardAlpha,
        borderAlpha = borderAlpha,
        strong = false,
        tint = cardTint
    ) {
        if (!item.read) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(unreadOverlay)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!item.read) {
                        onMarkRead(item.id)
                    }
                    onOpenDetail()
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(typeStyle.color.copy(alpha = 0.22f))
                    .border(1.dp, typeStyle.color.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeStyle.icon, contentDescription = null, tint = typeStyle.color)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        fontWeight = if (item.read) FontWeight.Medium else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White // ✅ Ensure white text
                    )
                    if (!item.read) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22D3EE)) // Blue dot indicator
                        )
                    }
                }
                Text(
                    item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        NotificationTypePill(label = stringResource(typeStyle.labelRes), color = typeStyle.color)
                        NotificationStatusPill(read = item.read)
                    }
                    Text(
                        text = relativeTime(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
        
        // Join Session button for booking notifications
        if (showJoinButton && bookingId != null) {
            val isTooEarly = joinWindowState == NotificationsViewModel.JoinWindowState.TOO_EARLY
            val buttonLabel = if (isTooEarly) "Chưa tới giờ" else "Join Session"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            ) {
                MMGhostButton(
                    onClick = {
                        scope.launch {
                            val errorMsg = viewModel.validateBookingTime(bookingId)
                            if (errorMsg != null) {
                                android.widget.Toast.makeText(
                                    context,
                                    errorMsg,
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else {
                                onJoinSession(bookingId)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isTooEarly
                ) {
                    Icon(
                        Icons.Default.VideoCall,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(buttonLabel)
                }
            }
        }
        }
    }
}

@Composable
private fun NotificationDetailDialog(
    item: NotificationItem,
    onDismiss: () -> Unit
) {
    val typeStyle = notificationTypeStyle(item.type)

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(radius = 24.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(typeStyle.color.copy(alpha = 0.2f))
                            .border(1.dp, typeStyle.color.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeStyle.icon, contentDescription = null, tint = typeStyle.color)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${relativeTime(item.timestamp)} • ${formatNotificationTime(item.timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_back))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotificationTypePill(
                        label = stringResource(typeStyle.labelRes),
                        color = typeStyle.color
                    )
                    NotificationStatusPill(read = item.read)
                }

                Text(
                    stringResource(R.string.notification_body_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    stringResource(R.string.notification_info_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                NotificationDetailMetaRow(
                    label = stringResource(R.string.notification_label_type),
                    value = stringResource(typeStyle.labelRes)
                )
                NotificationDetailMetaRow(
                    label = stringResource(R.string.notification_label_time),
                    value = formatNotificationTime(item.timestamp)
                )
                NotificationDetailMetaRow(
                    label = stringResource(R.string.notification_label_id),
                    value = item.id
                )
                item.deepLink?.takeIf { it.isNotBlank() }?.let { deepLink ->
                    NotificationDetailMetaRow(
                        label = stringResource(R.string.notification_label_link),
                        value = deepLink
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailMetaRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.width(90.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
