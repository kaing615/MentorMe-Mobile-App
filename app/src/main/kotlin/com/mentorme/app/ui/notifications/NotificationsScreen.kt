package com.mentorme.app.ui.notifications

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mentorme.app.core.notifications.NotificationHelper
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.data.model.NotificationType
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showUnreadOnly by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasPostPermission(context)) }
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
    val filtered = remember(showUnreadOnly, notifications) {
        if (showUnreadOnly) notifications.filter { !it.read } else notifications
    }
    val unreadCount = notifications.count { !it.read }

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
                    LiquidGlassCard(radius = 22.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Thông báo đẩy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (hasPermission) "Đã bật quyền thông báo."
                                else "Ứng dụng cần quyền để hiển thị thông báo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MMPrimaryButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission
                                ) {
                                    Text("Bật quyền")
                                }
                                MMGhostButton(onClick = {
                                    val demo = NotificationItem(
                                        title = "Thông báo thử",
                                        body = "Đây là thông báo local để kiểm tra.",
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
                                }) {
                                    Text("Gửi test")
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !showUnreadOnly,
                            onClick = { showUnreadOnly = false },
                            label = { Text("Tất cả") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.2f),
                                selectedLabelColor = Color.White,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.liquidGlass(radius = 16.dp)
                        )
                        FilterChip(
                            selected = showUnreadOnly,
                            onClick = { showUnreadOnly = true },
                            label = { Text("Chưa đọc") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.2f),
                                selectedLabelColor = Color.White,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.liquidGlass(radius = 16.dp)
                        )
                    }

                    if (filtered.isEmpty()) {
                        LiquidGlassCard(radius = 22.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    "Chưa có thông báo",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Thông báo mới sẽ xuất hiện tại đây.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                NotificationRow(
                                    item = item,
                                    onClick = { viewModel.markRead(item.id) }
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
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val (icon, color) = typeStyle(item.type)
    val cardAlpha = if (item.read) 0.12f else 0.2f

    LiquidGlassCard(radius = 20.dp, alpha = cardAlpha, borderAlpha = 0.35f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        fontWeight = if (item.read) FontWeight.Medium else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.read) {
                        Spacer(Modifier.width(8.dp))
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
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))
            Text(
                text = relativeTime(item.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

private fun typeStyle(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.BOOKING_CONFIRMED -> Icons.Outlined.CheckCircle to Color(0xFF34D399)
        NotificationType.BOOKING_REMINDER -> Icons.Outlined.AccessTime to Color(0xFFFBBF24)
        NotificationType.BOOKING_CANCELLED -> Icons.Outlined.EventBusy to Color(0xFFF87171)
        NotificationType.BOOKING_PENDING -> Icons.Outlined.AccessTime to Color(0xFFFBBF24)
        NotificationType.BOOKING_DECLINED -> Icons.Outlined.EventBusy to Color(0xFFF87171)
        NotificationType.BOOKING_FAILED -> Icons.Outlined.EventBusy to Color(0xFFF87171)
        NotificationType.PAYMENT_SUCCESS -> Icons.Outlined.CheckCircle to Color(0xFF34D399)
        NotificationType.PAYMENT_FAILED -> Icons.Outlined.EventBusy to Color(0xFFF87171)
        NotificationType.MESSAGE -> Icons.Outlined.Message to Color(0xFF60A5FA)
        NotificationType.SYSTEM -> Icons.Outlined.Notifications to Color(0xFFA78BFA)
    }
}

private fun relativeTime(timestamp: Long): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val minutes = diffMs / 60000L
    val hours = diffMs / 3600000L
    val days = diffMs / 86400000L
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "vừa xong"
    }
}
