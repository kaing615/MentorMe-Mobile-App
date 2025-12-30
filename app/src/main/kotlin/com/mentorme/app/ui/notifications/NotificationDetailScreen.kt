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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.VideoCall
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mentorme.app.R
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
    onJoinSession: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val notifications by NotificationStore.notifications.collectAsState()
    val item = remember(notifications, notificationId) {
        notifications.firstOrNull { it.id == notificationId }
    }

    LaunchedEffect(notificationId) {
        viewModel.ensureNotificationAvailable(notificationId)
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
                        title = { Text(stringResource(R.string.notification_detail_title), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                        LiquidGlassCard(
                            radius = 22.dp,
                            alpha = 0.12f, // ✅ Standard glass for readability
                            borderAlpha = 0.25f // ✅ Soft border
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null)
                                Text(
                                    stringResource(R.string.notification_missing_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.notification_missing_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                MMGhostButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
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
                        NotificationDetailActions(item = item, onJoinSession = onJoinSession)
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

    LiquidGlassCard(
        radius = 24.dp,
        alpha = 0.12f, // ✅ Standard glass for readability
        borderAlpha = 0.25f // ✅ Soft border
    ) {
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
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // ✅ Full white for titles
                    )
                    Text(
                        "${relativeTime(item.timestamp)} • ${formatNotificationTime(item.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                NotificationStatusPill(read = item.read)
            }

            NotificationTypePill(label = stringResource(typeStyle.labelRes), color = typeStyle.color)
        }
    }
}

@Composable
private fun NotificationDetailBody(
    item: NotificationItem
) {
    LiquidGlassCard(
        radius = 22.dp,
        alpha = 0.12f, // ✅ Standard glass for readability
        borderAlpha = 0.25f // ✅ Soft border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.notification_body_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f) // ✅ Label: 70% opacity
            )
            Text(
                item.body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White // ✅ Body: Full white for readability
            )
        }
    }
}

@Composable
private fun NotificationDetailMeta(
    item: NotificationItem
) {
    val typeStyle = notificationTypeStyle(item.type)

    LiquidGlassCard(
        radius = 22.dp,
        alpha = 0.12f, // ✅ Standard glass for readability
        borderAlpha = 0.25f // ✅ Soft border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.notification_info_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f) // ✅ Label: 70% opacity
            )
            NotificationMetaRow(label = stringResource(R.string.notification_label_type), value = stringResource(typeStyle.labelRes))
            NotificationMetaRow(label = stringResource(R.string.notification_label_time), value = formatNotificationTime(item.timestamp))
            NotificationMetaRow(label = stringResource(R.string.notification_label_id), value = item.id)
            item.deepLink?.takeIf { it.isNotBlank() }?.let { deepLink ->
                NotificationMetaRow(label = stringResource(R.string.notification_label_link), value = deepLink)
            }
        }
    }
}

@Composable
private fun NotificationDetailActions(
    item: NotificationItem,
    onJoinSession: (String) -> Unit
) {
    // Extract booking ID from deepLink if it's a session-related notification
    val bookingId = item.deepLink?.let { link ->
        when {
            link.startsWith("booking_detail/") -> link.removePrefix("booking_detail/")
            link.startsWith("video_call/") -> link.removePrefix("video_call/")
            else -> null
        }
    }

    if (bookingId != null && (
        item.type == com.mentorme.app.data.model.NotificationType.BOOKING_REMINDER ||
        item.type == com.mentorme.app.data.model.NotificationType.BOOKING_CONFIRMED
    )) {
        LiquidGlassCard(
            radius = 22.dp,
            alpha = 0.12f, // ✅ Standard glass for readability
            borderAlpha = 0.25f // ✅ Soft border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f) // ✅ Label: 70% opacity
                )
                MMGhostButton(
                    onClick = { onJoinSession(bookingId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null)
                        Text("Join Session")
                    }
                }
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
