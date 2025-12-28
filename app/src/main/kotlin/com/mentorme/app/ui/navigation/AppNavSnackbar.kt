package com.mentorme.app.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mentorme.app.data.model.NotificationType
import com.mentorme.app.ui.theme.BookingGlassTint
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.components.ui.BackdropBlur
import com.mentorme.app.ui.notifications.isBookingNotification
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
internal fun GlassSnackbarHost(
    hostState: SnackbarHostState,
    activeType: NotificationType? = null,
    modifier: Modifier = Modifier
) {
    val useGreenGlass = activeType?.let { isBookingNotification(it) } == true ||
        activeType == NotificationType.PAYMENT_SUCCESS
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            GlassNotificationSnackbar(
                data = data,
                useGreenGlass = useGreenGlass,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    )
}

@Composable
internal fun GlassNotificationSnackbar(
    data: SnackbarData,
    useGreenGlass: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var widthPx by remember { mutableStateOf(0f) }
    val dismissThreshold = widthPx * 0.35f
    val glassTint = if (useGreenGlass) BookingGlassTint else Color.White
    val glassAlpha = if (useGreenGlass) 0.36f else 0.18f
    val glassBorderAlpha = if (useGreenGlass) 0.75f else 0.35f
    val frostOverlayAlpha = if (useGreenGlass) 0.2f else 0f
    val blurRadius = if (useGreenGlass) 18.dp else 0.dp
    val iconBg = if (useGreenGlass) glassTint.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.12f)
    val iconBorder = if (useGreenGlass) glassTint.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.35f)
    val shape = RoundedCornerShape(22.dp)

    val dragState = rememberDraggableState { delta ->
        if (widthPx <= 0f) return@rememberDraggableState
        scope.launch {
            offsetX.stop()
            val next = (offsetX.value + delta).coerceIn(-widthPx, widthPx)
            offsetX.snapTo(next)
        }
    }

    val fade = if (widthPx > 0f) {
        val progress = (abs(offsetX.value) / widthPx).coerceIn(0f, 1f)
        1f - 0.35f * progress
    } else {
        1f
    }

    Box(
        modifier = modifier
            .onSizeChanged { widthPx = it.width.toFloat() }
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .alpha(fade)
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (widthPx > 0f && abs(offsetX.value) > dismissThreshold) {
                        scope.launch {
                            val target = if (offsetX.value >= 0f) widthPx else -widthPx
                            offsetX.animateTo(target, animationSpec = tween(150))
                            data.dismiss()
                            offsetX.snapTo(0f)
                        }
                    } else {
                        scope.launch { offsetX.animateTo(0f, animationSpec = tween(150)) }
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            if (blurRadius > 0.dp) {
                BackdropBlur(
                    modifier = Modifier.matchParentSize(),
                    blurRadius = blurRadius,
                    overlayColor = Color.Transparent,
                    cornerRadius = 22.dp
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                glassTint.copy(alpha = glassAlpha),
                                glassTint.copy(alpha = glassAlpha * 0.7f)
                            )
                        )
                    )
                    .border(1.dp, glassTint.copy(alpha = glassBorderAlpha), shape)
            )
            if (frostOverlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(glassTint.copy(alpha = frostOverlayAlpha))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                        .border(1.dp, iconBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val actionLabel = data.visuals.actionLabel
                if (!actionLabel.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .liquidGlass(radius = 12.dp, alpha = glassAlpha, borderAlpha = glassBorderAlpha, tint = glassTint)
                            .clickable { data.performAction() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }
                IconButton(onClick = { data.dismiss() }) {
                    Icon(Icons.Outlined.Close, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
