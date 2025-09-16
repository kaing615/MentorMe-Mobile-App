package com.mentorme.app.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset

/**
 * Map các biến trong globals.css -> token Kotlin
 * (màu, gradient, radius, weight…)
 */
object MMColors {
    // Light (blue themed)
    val Foreground = Color(0xF2FFFFFF)       // rgba(255,255,255,0.95)
    val Card       = Color(0x1AFFFFFF)       // 0.1
    val CardFg     = Color(0xE6FFFFFF)       // 0.9
    val Popover    = Color(0xF2FFFFFF)       // 0.95
    val PopoverFg  = Color(0xE61E3A8A.toInt()) // approx rgba(30,58,138,0.9)

    val Secondary  = Color(0x33FFFFFF)       // 0.2
    val SecondaryFg= Color(0xCCFFFFFF)       // 0.8
    val Muted      = Color(0x1AFFFFFF)       // 0.1
    val MutedFg    = Color(0xB3FFFFFF)       // 0.7
    val Accent     = Color(0x26FFFFFF)       // 0.15
    val AccentFg   = Color(0xE6FFFFFF)       // 0.9
    val Destructive= Color(0xFFEF4444)
    val DestructiveFg= Color(0xFFFFFFFF)
    val Border     = Color(0x33FFFFFF)       // 0.2
    val InputBg    = Color(0x26FFFFFF)       // 0.15
    val SwitchBg   = Color(0x4DFFFFFF)       // 0.3
    val Ring       = Color(0x803B82F6)       // rgba(59,130,246,0.5)

    // Liquid glass
    val GlassBg        = Color(0x1AFFFFFF)   // 0.1
    val GlassBgHover   = Color(0x33FFFFFF)   // 0.2
    val GlassBorder    = Color(0x33FFFFFF)   // 0.2
    val GlassBackdrop  = Color(0x40FFFFFF)   // 0.25

    // Sidebar (nếu cần)
    val Sidebar        = Color(0x1AFFFFFF)
    val SidebarFg      = Color(0xE6FFFFFF)

    // Dark scheme OKLCH không có trực tiếp trong Compose,
    // dùng xấp xỉ theo ý đồ sáng/tối:
    object Dark {
        val Background   = Color(0xFF101012) // oklch(0.145 0 0) ~ rất tối
        val Foreground   = Color(0xFFFFFFFF)
        val Surface      = Color(0xFF141418)
        val SurfaceVariant = Color(0xFF1D1D22)
        val Border       = Color(0xFF33333A)
        val Muted        = Color(0xFF2F2F35)
        val Ring         = Color(0xFF6F6F78)
        val Destructive  = Color(0xFFE74C3C)
        val DestructiveFg= Color(0xFFFFF8F7)
    }
}

object MMGradients {
    val Primary = Brush.linearGradient(
        colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val Secondary = Brush.linearGradient(
        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
    )
    val Tertiary = Brush.linearGradient(
        listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4))
    )
    val Accent = Brush.linearGradient(
        listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
    )
    val Warm = Brush.linearGradient(
        listOf(Color(0xFFF59E0B), Color(0xFFEAB308))
    )
    val Cool = Brush.linearGradient(
        listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
    )
}

// Radius
object MMShapes {
    // CSS: --radius: 1.5rem ≈ 24dp
    val RadiusLg = 24.dp
    val RadiusMd = 20.dp   // radius - 2px ~ 22dp (xấp xỉ)
    val RadiusSm = 16.dp

    val CardShape = RoundedCornerShape(RadiusLg)
    val ButtonShape = RoundedCornerShape(18.dp)
}

// Typography tokens
object MMTypoTokens {
    const val FontWeightMedium = 500
    const val FontWeightNormal = 400

    val TextBase = 16.sp
    val TextLg   = 18.sp
    val TextXl   = 20.sp
    val Text2Xl  = 24.sp
}
