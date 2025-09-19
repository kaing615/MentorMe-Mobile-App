package com.mentorme.app.ui.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    Box(
        modifier = Modifier
            .size(size)
            .liquidGlassStrong(radius = size / 2, alpha = 0.26f),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(size)) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}
