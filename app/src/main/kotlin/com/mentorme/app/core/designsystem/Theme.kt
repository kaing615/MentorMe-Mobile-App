package com.mentorme.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary colors for MentorMe brand
private val MentorMePrimary = Color(0xFF2563EB) // Blue-600
private val MentorMeSecondary = Color(0xFF7C3AED) // Purple-600
private val MentorMeAccent = Color(0xFF059669) // Emerald-600

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = MentorMePrimary,
    secondary = MentorMeSecondary,
    tertiary = MentorMeAccent,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFDC2626),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    onError = Color.White
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = MentorMePrimary,
    secondary = MentorMeSecondary,
    tertiary = MentorMeAccent,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    error = Color(0xFFEF4444),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB),
    onError = Color.White
)

@Composable
fun MentorMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MentorMeTypography,
        content = content
    )
}
