package com.mentorme.app.ui.components.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.realBlur

@Composable
fun BackdropBlur(
    modifier: Modifier = Modifier,
    blurRadius: Dp,
    overlayColor: Color = Color.Transparent,
    cornerRadius: Dp = 0.dp
) {
    // Use native Compose blur instead of BlurView to avoid layout measurement issues
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ - use RenderEffect blur (hardware accelerated)
        modifier.realBlur(blurRadius)
    } else {
        // Android 11 and below - use software blur
        modifier.blur(blurRadius)
    }

    androidx.compose.foundation.layout.Box(
        modifier = blurModifier
            .then(
                if (cornerRadius > 0.dp) {
                    Modifier.clip(RoundedCornerShape(cornerRadius))
                } else {
                    Modifier
                }
            )
            .background(overlayColor)
    )
}
