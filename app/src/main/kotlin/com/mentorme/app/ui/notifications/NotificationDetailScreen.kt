package com.mentorme.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.core.notifications.NotificationStore
import com.mentorme.app.data.model.NotificationItem
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val notifications by NotificationStore.notifications.collectAsState()
    val item = remember(notifications, notificationId) {
        notifications.firstOrNull { it.id == notificationId }
    }

    LaunchedEffect(notificationId, item?.read) {
        if (item != null && !item.read) {
            viewModel.markRead(notificationId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LiquidBackground(modifier = Modifier.matchParentSize())

        androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Chi tiết thông báo", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
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
                if (item == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidGlassCard(radius = 22.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null)
                                Text(
                                    "Không tìm thấy thông báo",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Thông báo có thể đã bị xóa hoặc chưa được tải về.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                MMGhostButton(onClick = onBack) { Text("Quay lại") }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 84.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NotificationDetailHero(item = item)
                        NotificationDetailBody(item = item)
                        NotificationDetailMeta(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailHero(
    item: NotificationItem
) {
    val typeStyle = notificationTypeStyle(item.type)

    LiquidGlassCard(radius = 24.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${relativeTime(item.timestamp)} • ${formatNotificationTime(item.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                NotificationStatusPill(read = item.read)
            }

            NotificationTypePill(label = typeStyle.label, color = typeStyle.color)
        }
    }
}

@Composable
private fun NotificationDetailBody(
    item: NotificationItem
) {
    LiquidGlassCard(radius = 22.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Nội dung",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                item.body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun NotificationDetailMeta(
    item: NotificationItem
) {
    val typeStyle = notificationTypeStyle(item.type)

    LiquidGlassCard(radius = 22.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Thông tin",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            NotificationMetaRow(label = "Loại", value = typeStyle.label)
            NotificationMetaRow(label = "Thời gian", value = formatNotificationTime(item.timestamp))
            NotificationMetaRow(label = "Mã", value = item.id)
            item.deepLink?.takeIf { it.isNotBlank() }?.let { deepLink ->
                NotificationMetaRow(label = "Liên kết", value = deepLink)
            }
        }
    }
}

@Composable
private fun NotificationMetaRow(
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
