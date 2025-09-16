package com.mentorme.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),          // --primary gradient → lấy mid color
    onPrimary = Foreground,
    secondary = Secondary,
    onSecondary = SecondaryFg,
    background = Color(0xFF0D47A1),       // nền surface base (nền thật dùng gradient)
    onBackground = Foreground,
    surface = Card,
    onSurface = CardFg,
    surfaceVariant = Muted,
    onSurfaceVariant = MutedFg,
    error = Destructive,
    onError = DestructiveFg,
    outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF34343A),
    secondary = DarkTokens.SurfaceVariant,
    onSecondary = DarkTokens.Foreground,
    background = DarkTokens.Background,
    onBackground = DarkTokens.Foreground,
    surface = DarkTokens.Surface,
    onSurface = DarkTokens.Foreground,
    surfaceVariant = DarkTokens.SurfaceVariant,
    onSurfaceVariant = Color(0xFFBDBDC6),
    error = DarkTokens.Destructive,
    onError = DarkTokens.DestructiveFg,
    outline = DarkTokens.Border
)

@Composable
fun MentorMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(Radius.Sm),
        small      = RoundedCornerShape(Radius.Sm),
        medium     = RoundedCornerShape(Radius.Md),
        large      = RoundedCornerShape(Radius.Lg),
        extraLarge = RoundedCornerShape(Radius.Xl)
    )
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = MentorMeTypography, // tạo file Typography nếu chưa có
        shapes      = shapes,
        content     = content
    )
}
