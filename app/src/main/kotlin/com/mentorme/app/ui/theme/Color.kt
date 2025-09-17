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

val GradientGold = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFFFB347), // Light gold
    Color(0xFFFF8C00)  // Dark orange gold
)

// Base theme colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// App specific colors
val MentorMeBlue = Color(0xFF667eea)
val MentorMePurple = Color(0xFF764ba2)
val MentorMePink = Color(0xFFf093fb)
val MentorMeRed = Color(0xFFf5576c)

// Status colors
val SuccessGreen = Color(0xFF22C55E)
val ErrorRed = Color(0xFFEF4444)
val WarningYellow = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF3B82F6)
