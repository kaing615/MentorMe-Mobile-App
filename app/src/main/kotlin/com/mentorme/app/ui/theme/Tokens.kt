package com.mentorme.app.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Base foreground/background (không dùng linear-gradient cho ColorScheme)
val Foreground = Color(0xF2FFFFFF) // rgba(255,255,255,0.95)
val Card       = Color(0x1AFFFFFF) // 0.10
val CardFg     = Color(0xE6FFFFFF) // 0.90
val Popover    = Color(0xF2FFFFFF) // 0.95
val PopoverFg  = Color(0xE61E3A8A) // rgba(30,58,138,0.9)

val Secondary  = Color(0x33FFFFFF) // 0.2
val SecondaryFg= Color(0xCCFFFFFF) // 0.8
val Muted      = Color(0x1AFFFFFF) // 0.1
val MutedFg    = Color(0xB3FFFFFF) // 0.7
val Accent     = Color(0x26FFFFFF) // 0.15
val AccentFg   = Color(0xE6FFFFFF) // 0.9
val Destructive= Color(0xFFEF4444)
val DestructiveFg= Color(0xFFFFFFFF)
val Border     = Color(0x33FFFFFF) // 0.2
val InputBg    = Color(0x26FFFFFF) // 0.15
val SwitchBg   = Color(0x4DFFFFFF) // 0.3
val Ring       = Color(0x803B82F6) // rgba(59,130,246,0.5)
val ActiveNavGreen = Color(0xFF22C55E) // green for active nav item

// ===== Gradients (map thẳng từ CSS)
// --gradient-primary / secondary / tertiary / accent / warm / cool
val GradientPrimary   = listOf(Color(0xFF0E2C56), Color(0xFF2F6BC4))
val GradientSecondary = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
val GradientTertiary  = listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4))
val GradientAccent    = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
val GradientWarm      = listOf(Color(0xFFF59E0B), Color(0xFFEAB308))
val GradientCool      = listOf(Color(0xFF6366F1), Color(0xFF3B82F6))

// ===== Liquid glass tokens
val GlassBg       = Color(0x1AFFFFFF)  // --glass-bg
val GlassBgHover  = Color(0x33FFFFFF)  // --glass-bg-hover
val GlassBorder   = Color(0x33FFFFFF)  // --glass-border
val GlassShadow   = 0.37f              // dùng cho elevation/alpha
val GlassBackdrop = Color(0x40FFFFFF)  // --glass-backdrop ~ 0.25 (thêm chút sáng)

// ===== Dark scheme map từ .dark {...}
object DarkTokens {
    val Background     = Color(0xFF252525) // oklch map gần đúng
    val Foreground     = Color(0xFFFFFFFF)
    val Surface        = Color(0xFF252525)
    val SurfaceVariant = Color(0xFF444444)
    val Border         = Color(0xFF444444)
    val Destructive    = Color(0xFFBE3A2F)
    val DestructiveFg  = Color(0xFFA73C2A)
}

// ===== Radius từ --radius: 1.5rem (~24px)
object Radius {
    val Sm = 20
    val Md = 22
    val Lg = 24
    val Xl = 28
}
