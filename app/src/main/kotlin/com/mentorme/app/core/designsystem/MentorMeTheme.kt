package com.mentorme.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material3 ColorScheme dựa trên các token đã map từ CSS (DesignTokens.kt).
 * Nền gradient “liquid” sẽ vẽ ở layer UI (Scaffold root), còn ColorScheme
 * dùng cho component Material3.
 */
private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = MMColors.Foreground,
    secondary = MMColors.Secondary,
    onSecondary = MMColors.SecondaryFg,
    background = Color(0xFF0D47A1), // base; nền thật sẽ là gradient ở UI
    onBackground = MMColors.Foreground,
    surface = MMColors.Card,
    onSurface = MMColors.CardFg,
    surfaceVariant = MMColors.Muted,
    onSurfaceVariant = MMColors.MutedFg,
    error = MMColors.Destructive,
    onError = MMColors.DestructiveFg,
    outline = MMColors.Border,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF34343A),
    secondary = MMColors.Dark.Muted,
    onSecondary = MMColors.Foreground,
    background = MMColors.Dark.Background,
    onBackground = MMColors.Dark.Foreground,
    surface = MMColors.Dark.Surface,
    onSurface = MMColors.Dark.Foreground,
    surfaceVariant = MMColors.Dark.SurfaceVariant,
    onSurfaceVariant = Color(0xFFBDBDC6),
    error = MMColors.Dark.Destructive,
    onError = MMColors.Dark.DestructiveFg,
    outline = MMColors.Dark.Border,
)

@Composable
fun MentorMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MentorMeTypography, // dùng bộ chữ đã map h1–h4, p, label, button, input
        shapes = Shapes(
            extraSmall = RoundedCornerShape(MMShapes.RadiusSm),
            small      = RoundedCornerShape(MMShapes.RadiusSm),
            medium     = RoundedCornerShape(MMShapes.RadiusMd),
            large      = RoundedCornerShape(MMShapes.RadiusLg),
            extraLarge = RoundedCornerShape(MMShapes.RadiusLg),
        ),
        content = content
    )
}
