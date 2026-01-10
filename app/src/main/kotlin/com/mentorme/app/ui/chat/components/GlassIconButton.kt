package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    tint: Color = Color.White,
    enabled: Boolean = true // Thêm parameter này
) {
    Box(
        modifier = Modifier
            .size(size)
            .liquidGlassStrong(radius = size / 2, alpha = if (enabled) 0.26f else 0.15f),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(size),
            enabled = enabled // Sử dụng enabled
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (enabled) tint else tint.copy(alpha = 0.5f)
            )
        }
    }
}
