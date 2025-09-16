package com.mentorme.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


fun Modifier.gradientBackground(colors: List<Color>) = this
    .background(
        Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset.Infinite
        )
    )

object MMColors {
    val Foreground = Color(0xFFFFFFFF)
    val Card = Color.White.copy(alpha = 0.1f)
    val CardFg = Color.White.copy(alpha = 0.9f)
    val Border = Color.White.copy(alpha = 0.2f)
    val Muted = Color.White.copy(alpha = 0.1f)
    val MutedFg = Color.White.copy(alpha = 0.7f)
    val Accent = Color.White.copy(alpha = 0.15f)
    val AccentFg = Color.White.copy(alpha = 0.9f)
    val Destructive = Color(0xFFEF4444)
    val DestructiveFg = Color.White
}
