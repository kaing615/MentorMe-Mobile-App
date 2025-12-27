package com.mentorme.app.ui.notifications

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationPreferences
import com.mentorme.app.data.model.NotificationType
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

private enum class NotificationFilter(val label: String) {
    ALL("Tất cả"),
    UNREAD("Chưa đọc"),
    READ("Đã đọc")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(NotificationFilter.ALL) }
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasPostPermission(context)) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        hasPermission = NotificationHelper.hasPostPermission(context)
        viewModel.refresh()
        viewModel.refreshPreferences()
    }

    val notifications by NotificationStore.notifications.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
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
                                Text("Thông báo", fontWeight = FontWeight.Bold)
                                if (unreadCount > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.markAllRead() },
                                enabled = unreadCount > 0
                            ) {
                                Icon(Icons.Outlined.DoneAll, contentDescription = "Mark all read")
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
                                title = "Thông báo thử",
                                body = "Đây là thông báo local để kiểm tra giao diện.",
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

                    NotificationPreferencesCard(
                        preferences = preferences,
                        onUpdate = { viewModel.updatePreferences(it) }
                    )

                    NotificationFilterBar(
                        selected = filter,
                        unreadCount = unreadCount,
                        onSelect = { filter = it }
                    )

                    if (filtered.isEmpty()) {
                        NotificationEmptyState(filter = filter)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                NotificationRow(
                                    item = item,
                                    onOpenDetail = { onOpenDetail(item.id) },
                                    onMarkRead = { viewModel.markRead(it) }
                                )
                            }
                        }
                    }
                }
            }
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
                        text = if (unreadCount > 0) "Bạn có $unreadCount thông báo mới" else "Không có thông báo mới",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Theo dõi lịch hẹn, tin nhắn và cập nhật hệ thống.",
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
                NotificationStatChip(label = "Tổng", count = totalCount, modifier = Modifier.weight(1f))
                NotificationStatChip(label = "Chưa đọc", count = unreadCount, modifier = Modifier.weight(1f))
                NotificationStatChip(label = "Đã đọc", count = readCount, modifier = Modifier.weight(1f))
            }

            MMGhostButton(
                onClick = onMarkAllRead,
                modifier = Modifier.fillMaxWidth(),
                enabled = markAllEnabled
            ) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Đánh dấu tất cả đã đọc")
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
    val statusText = if (hasPermission) "Đã bật quyền thông báo." else "Ứng dụng cần quyền để hiển thị thông báo."
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
                    Text("Quyền thông báo", fontWeight = FontWeight.SemiBold)
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
                        Text("Bật quyền")
                    }
                    MMGhostButton(
                        onClick = onSendTest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Gửi test")
                    }
                }
            } else {
                MMGhostButton(
                    onClick = onSendTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gửi test")
                }
            }
        }
    }
}

@Composable
private fun NotificationPreferencesCard(
    preferences: NotificationPreferences,
    onUpdate: (NotificationPreferences) -> Unit
) {
    LiquidGlassCard(radius = 24.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Push preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            NotificationPreferenceRow(
                title = "Booking updates",
                subtitle = "Confirmations, reminders, cancellations",
                checked = preferences.pushBooking,
                onCheckedChange = { onUpdate(preferences.copy(pushBooking = it)) }
            )
            NotificationPreferenceRow(
                title = "Payments",
                subtitle = "Payment success or failure",
                checked = preferences.pushPayment,
                onCheckedChange = { onUpdate(preferences.copy(pushPayment = it)) }
            )
            NotificationPreferenceRow(
                title = "Messages",
                subtitle = "New chat messages",
                checked = preferences.pushMessage,
                onCheckedChange = { onUpdate(preferences.copy(pushMessage = it)) }
            )
            NotificationPreferenceRow(
                title = "System",
                subtitle = "General updates",
                checked = preferences.pushSystem,
                onCheckedChange = { onUpdate(preferences.copy(pushSystem = it)) }
            )
        }
    }
}

@Composable
private fun NotificationPreferenceRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.White.copy(alpha = 0.35f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun PermissionStatusPill(
    granted: Boolean
) {
    val label = if (granted) "Đã bật" else "Chưa bật"
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
                label = NotificationFilter.ALL.label,
                selected = selected == NotificationFilter.ALL,
                onClick = { onSelect(NotificationFilter.ALL) },
                modifier = Modifier.weight(1f)
            )
            NotificationFilterOption(
                label = NotificationFilter.UNREAD.label,
                selected = selected == NotificationFilter.UNREAD,
                badgeCount = unreadCount,
                onClick = { onSelect(NotificationFilter.UNREAD) },
                modifier = Modifier.weight(1f)
            )
            NotificationFilterOption(
                label = NotificationFilter.READ.label,
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
                    NotificationFilter.UNREAD -> "Không có thông báo chưa đọc"
                    NotificationFilter.READ -> "Chưa có thông báo đã đọc"
                    NotificationFilter.ALL -> "Chưa có thông báo"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Thông báo mới sẽ xuất hiện tại đây.",
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
                        NotificationTypePill(label = typeStyle.label, color = typeStyle.color)
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
