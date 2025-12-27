package com.mentorme.app.ui.notifications

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
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
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationType
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

private enum class NotificationFilter(@StringRes val labelRes: Int) {
    ALL(R.string.notification_filter_all),
    UNREAD(R.string.notification_filter_unread),
    READ(R.string.notification_filter_read)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {},
    showDetailDialog: Boolean = true,
    viewModel: NotificationsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(NotificationFilter.ALL) }
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasPostPermission(context)) }
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        hasPermission = NotificationHelper.hasPostPermission(context)
        viewModel.refresh()
    }

    val notifications by NotificationStore.notifications.collectAsState()
    LaunchedEffect(notifications) {
        if (notifications.isEmpty()) {
            NotificationStore.seed(MockData.mockNotifications)
        }
    }
    val unreadCount = notifications.count { !it.read }
    val readCount = notifications.count { it.read }
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
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.markAllRead() },
                                enabled = unreadCount > 0
                            ) {
                                Icon(Icons.Outlined.DoneAll, contentDescription = stringResource(R.string.notification_mark_all_read_action))
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                        .padding(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NotificationSummaryCard(
                        unreadCount = unreadCount,
                        readCount = readCount,
                        totalCount = notifications.size,
                        onMarkAllRead = { viewModel.markAllRead() },
                        markAllEnabled = unreadCount > 0
                    )

                    NotificationPermissionCard(
                        hasPermission = hasPermission,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onSendTest = {
                            val demo = NotificationItem(
                                title = context.getString(R.string.notification_test_title),
                                body = context.getString(R.string.notification_test_body),
                                type = NotificationType.SYSTEM,
                                timestamp = System.currentTimeMillis(),
                                read = false
                            )
                            NotificationHelper.showNotification(
                                context,
                                demo.title,
                                demo.body,
                                demo.type
                            )
                            NotificationStore.add(demo)
                        }
                    )


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
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                NotificationRow(
                                    item = item,
                                    onOpenDetail = {
                                        if (showDetailDialog) {
                                            selectedNotification = item
                                        } else {
                                            onOpenDetail(item.id)
                                        }
                                    },
                                    onMarkRead = { viewModel.markRead(it) }
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
private fun NotificationSummaryCard(
    unreadCount: Int,
    readCount: Int,
    totalCount: Int,
    onMarkAllRead: () -> Unit,
    markAllEnabled: Boolean
) {
    LiquidGlassCard(radius = 24.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (unreadCount > 0) stringResource(R.string.notification_summary_unread, unreadCount) else stringResource(R.string.notification_summary_none),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.notification_summary_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationStatChip(label = stringResource(R.string.notification_stat_total), count = totalCount, modifier = Modifier.weight(1f))
                NotificationStatChip(label = stringResource(R.string.notification_stat_unread), count = unreadCount, modifier = Modifier.weight(1f))
                NotificationStatChip(label = stringResource(R.string.notification_stat_read), count = readCount, modifier = Modifier.weight(1f))
            }

            MMGhostButton(
                onClick = onMarkAllRead,
                modifier = Modifier.fillMaxWidth(),
                enabled = markAllEnabled
            ) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.notification_mark_all_read))
            }
        }
    }
}

@Composable
private fun NotificationStatChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .liquidGlass(radius = 14.dp, alpha = 0.16f, borderAlpha = 0.3f)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("$count", fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun NotificationPermissionCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSendTest: () -> Unit
) {
    val statusText = if (hasPermission) stringResource(R.string.notification_permission_enabled) else stringResource(R.string.notification_permission_required)
    val statusColor = if (hasPermission) Color(0xFF34D399) else Color(0xFFFBBF24)

    LiquidGlassCard(radius = 22.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = statusColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.notification_permission_title), fontWeight = FontWeight.SemiBold)
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                PermissionStatusPill(granted = hasPermission)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MMPrimaryButton(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.notification_permission_enable_action))
                    }
                    MMGhostButton(
                        onClick = onSendTest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.notification_send_test))
                    }
                }
            } else {
                MMGhostButton(
                    onClick = onSendTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.notification_send_test))
                }
            }
        }
    }
}

private fun PermissionStatusPill(
    granted: Boolean
) {
    val label = if (granted) stringResource(R.string.notification_permission_granted) else stringResource(R.string.notification_permission_not_granted)
    val accent = if (granted) Color(0xFF34D399) else Color(0xFFFBBF24)
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.5f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
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
            .liquidGlass(radius = 18.dp, alpha = 0.14f, borderAlpha = 0.28f)
            .padding(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationFilterOption(
                label = stringResource(NotificationFilter.ALL.labelRes),
                selected = selected == NotificationFilter.ALL,
                onClick = { onSelect(NotificationFilter.ALL) },
                modifier = Modifier.weight(1f)
            )
            NotificationFilterOption(
                label = stringResource(NotificationFilter.UNREAD.labelRes),
                selected = selected == NotificationFilter.UNREAD,
                badgeCount = unreadCount,
                onClick = { onSelect(NotificationFilter.UNREAD) },
                modifier = Modifier.weight(1f)
            )
            NotificationFilterOption(
                label = stringResource(NotificationFilter.READ.labelRes),
                selected = selected == NotificationFilter.READ,
                onClick = { onSelect(NotificationFilter.READ) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NotificationFilterOption(
    label: String,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val baseModifier = modifier
        .clip(shape)
        .clickable(onClick = onClick)

    val containerModifier = baseModifier.liquidGlass(
        radius = 14.dp,
        alpha = if (selected) 0.22f else 0.1f,
        borderAlpha = if (selected) 0.45f else 0.25f
    )

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
                Badge { Text(badgeCount.coerceAtMost(99).toString()) }
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
    onMarkRead: (String) -> Unit
) {
    val typeStyle = notificationTypeStyle(item.type)
    val cardAlpha = if (item.read) 0.12f else 0.22f
    val borderAlpha = if (item.read) 0.25f else 0.4f

    LiquidGlassCard(radius = 22.dp, alpha = cardAlpha, borderAlpha = borderAlpha) {
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
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.read) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22D3EE))
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
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    stringResource(R.string.notification_info_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
