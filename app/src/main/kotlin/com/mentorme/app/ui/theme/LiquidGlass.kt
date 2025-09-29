package com.mentorme.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Constants
const val GLASS_ALPHA = 0.15f
const val GLASS_BORDER = 0.3f

/* =========================
 * Tokens map từ globals.css
 * ========================= */

/**
 * Liquid Glass Card Component
 */
// --- LiquidGlassCard: tách LỚP NỀN (có blur) và LỚP CONTENT (không blur) ---
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = 16.dp,
    alpha: Float = GLASS_ALPHA,
    borderAlpha: Float = GLASS_BORDER,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val glassModifier =
        if (strong) modifier.liquidGlassStrong(radius, alpha, borderAlpha)
        else modifier.liquidGlass(radius, alpha, borderAlpha)

    Box(modifier = glassModifier, content = content)
}



/**
 * Card-based LiquidGlassCard - Material3 Card with liquid glass effect
 * Use this for clickable cards or when you need ColumnScope
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassCardColumn(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    alpha: Float = 0.15f,
    borderAlpha: Float = 0.3f,
    strong: Boolean = false,
    elevation: Dp = if (strong) 20.dp else 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassModifier = if (strong) {
        modifier.liquidGlassStrong(radius, alpha, borderAlpha)
    } else {
        modifier.liquidGlass(radius, alpha, borderAlpha)
    }

    val colors = CardDefaults.cardColors(
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = glassModifier,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = glassModifier,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

/**
 * Liquid Glass modifier effect
 */
fun Modifier.liquidGlass(
    radius: Dp = 16.dp,
    alpha: Float = 0.15f,
    borderAlpha: Float = 0.3f
) = this
    .clip(RoundedCornerShape(radius))
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 0.7f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.5f)
            )
        ),
        shape = RoundedCornerShape(radius)
    )

/** Bản mạnh hơn – tương đương `.glass-strong`. */
fun Modifier.liquidGlassStrong(
    radius: Dp = 32.dp,
    alpha: Float = 0.25f,
    borderAlpha: Float = 0.4f
) = this
    .clip(RoundedCornerShape(radius))
    .background(Color.White.copy(alpha = alpha))
    .border(
        width = 1.5.dp,
        color = Color.White.copy(alpha = borderAlpha),
        shape = RoundedCornerShape(radius)
    )

/* =======================================================
 * Liquid background: mô phỏng body::before + keyframes
 * ======================================================= */

/**
 * Nền động "liquid motion" giống `body::before`:
 *  - Lớp 1: gradient nền chính (GradientPrimary)
 *  - Lớp 2: 3 radial-gradient bán trong suốt di chuyển liên tục.
 *
 * Web tham chiếu: `body` + `@keyframes liquidMotion`.
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    baseGradient: List<Color> = GradientPrimary,
    blob1: Color = Color(0x4D3B82F6), // rgba(59,130,246,0.3)
    blob2: Color = Color(0x4D1D4ED8), // rgba(29,78,216,0.3)
    blob3: Color = Color(0x3310A5E9)  // rgba(14,165,233,0.2)
) {
    // Animation như keyframes (0%→33%→66%→100%)
    val t by rememberInfiniteTransition(label = "liquid").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 20000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "t"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.linearGradient(baseGradient))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Tính biến thiên đơn giản theo t
            // dịch chuyển nhẹ + xoay nhẹ giống css.
            val dx = (t - 0.5f) * 40f      // ~ [-20, 20] px
            val dy = (t - 0.5f) * 20f
            val rot = t * 360f

            rotate(degrees = rot, pivot = center) {
                translate(left = dx, top = dy) {
                    // blob 1: ở ~20% 80%
                    drawCircle(
                        color = blob1,
                        radius = maxOf(w, h) * 0.35f,
                        center = Offset(w * 0.20f, h * 0.80f)
                    )
                    // blob 2: ở ~80% 20%
                    drawCircle(
                        color = blob2,
                        radius = maxOf(w, h) * 0.35f,
                        center = Offset(w * 0.80f, h * 0.20f)
                    )
                    // blob 3: ở ~40% 40%
                    drawCircle(
                        color = blob3,
                        radius = maxOf(w, h) * 0.28f,
                        center = Offset(w * 0.40f, h * 0.40f)
                    )
                }
            }
        }
    }
}
