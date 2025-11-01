package com.mentorme.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun GlassOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    formModifier: Modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(12.dp),
    content: @Composable () -> Unit
) {
    if (!visible) return

    // Root covers system bars (no safe insets)
    Box(Modifier.fillMaxSize().zIndex(2f)) {
        // Scrim that covers entire screen with strong blur
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
                .blur(28.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        )

        // Bright liquid-glass sheet, near-edges with small margin; full height
        Surface(
            modifier = formModifier
                .align(Alignment.Center)
                .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            color = Color.White.copy(alpha = 0.14f),
            tonalElevation = 12.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }
    }
}
