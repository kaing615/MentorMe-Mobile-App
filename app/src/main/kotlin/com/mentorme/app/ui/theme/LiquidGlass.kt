package com.mentorme.app.ui.theme

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mentorme.app.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader

// Constants
const val GLASS_ALPHA = 0.15f
const val GLASS_BORDER = 0.3f
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
val LocalHazeEnabled = staticCompositionLocalOf { false }
private val DefaultGlassBlur = 20.dp

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
    tint: Color = Color.White,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val glassModifier =
        if (strong) modifier.liquidGlassStrong(radius, alpha, borderAlpha, tint)
        else modifier.liquidGlass(radius, alpha, borderAlpha, tint)

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
    tint: Color = Color.White,
    strong: Boolean = false,
    elevation: Dp = if (strong) 20.dp else 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassModifier = if (strong) {
        modifier.liquidGlassStrong(radius, alpha, borderAlpha, tint)
    } else {
        modifier.liquidGlass(radius, alpha, borderAlpha, tint)
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
    borderAlpha: Float = 0.3f,
    tint: Color = Color.White
) = composed {
    val shape = RoundedCornerShape(radius)
    val hazeState = LocalHazeState.current
    val blurEnabled = LocalHazeEnabled.current
    val hazeModifier = if (blurEnabled && hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = Color.Transparent,
                tint = HazeTint(tint.copy(alpha = alpha * 0.5f)),
                blurRadius = 20.dp
            )
        )
    } else {
        Modifier
    }

    this
        .clip(shape)
        .then(hazeModifier)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    tint.copy(alpha = alpha),
                    tint.copy(alpha = alpha * 0.7f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    tint.copy(alpha = borderAlpha),
                    tint.copy(alpha = borderAlpha * 0.5f)
                )
            ),
            shape = shape
        )
}

/** Bản mạnh hơn – tương đương `.glass-strong`. */
fun Modifier.liquidGlassStrong(
    radius: Dp = 32.dp,
    alpha: Float = 0.25f,
    borderAlpha: Float = 0.4f,
    tint: Color = Color.White
) = composed {
    val shape = RoundedCornerShape(radius)
    val hazeState = LocalHazeState.current
    val blurEnabled = LocalHazeEnabled.current
    val hazeModifier = if (blurEnabled && hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = Color.Transparent,
                tint = HazeTint(tint.copy(alpha = alpha * 0.6f)),
                blurRadius = 30.dp
            )
        )
    } else {
        Modifier
    }

    this
        .clip(shape)
        .then(hazeModifier)
        .background(tint.copy(alpha = alpha))
        .border(
            width = 1.5.dp,
            color = tint.copy(alpha = borderAlpha),
            shape = shape
        )
}

@Composable
fun Modifier.realBlur(blurRadius: Dp): Modifier {
    if (blurRadius <= 0.dp) return this
    val radiusPx = with(LocalDensity.current) { blurRadius.toPx() }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = AndroidRenderEffect.createBlurEffect(
                radiusPx,
                radiusPx,
                Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
            compositingStrategy = CompositingStrategy.Offscreen
        }
    } else {
        this.blur(blurRadius)
    }
}

@Composable
fun Modifier.realBlur(blurRadius: Dp): Modifier {
    if (blurRadius <= 0.dp) return this
    val radiusPx = with(LocalDensity.current) { blurRadius.toPx() }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = AndroidRenderEffect.createBlurEffect(
                radiusPx,
                radiusPx,
                Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
            compositingStrategy = CompositingStrategy.Offscreen
        }
    } else {
        this.blur(blurRadius)
    }
}

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
    blob1: Color = Color(0x472C64D6), // rgba(44,100,214,0.28)
    blob2: Color = Color(0x472351C4), // rgba(35,81,196,0.28)
    blob3: Color = Color(0x331F4F98)  // rgba(31,79,152,0.20)
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

@Composable
fun LiquidGlassLogo(
    size: Dp = 96.dp,
    @DrawableRes logoRes: Int = R.drawable.mentormehehe,
    logoScale: Float = 0.68f,     // ← tỉ lệ kích thước logo (so với đường kính vòng)
) {
    val ring = Brush.linearGradient(
        listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
    )
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val sweep by shimmer.animateFloat(
        initialValue = -0.4f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart), label = "sweep"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(width = 2.dp, brush = ring, shape = CircleShape)
            .liquidGlass(radius = size / 2, alpha = 0.22f, borderAlpha = 0.45f)
    ) {
        // highlight bóng phía trên
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.92f)
                .height(size * 0.42f)
                .clip(RoundedCornerShape(bottomStart = size/2, bottomEnd = size/2))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(0.35f), Color.White.copy(0f))
                    )
                )
                .alpha(0.22f)
        )

        // shimmer nhẹ chạy chéo
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { rotationZ = 18f }
                .padding(2.dp)
                .offset(x = (size * sweep))
                .width(size * 0.22f)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(0.20f),
                            Color.Transparent
                        )
                    )
                )
                .alpha(0.20f)
        )

        // logo ở giữa (không tint)
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "MentorMe",
            modifier = Modifier
                .align(Alignment.Center)
                .size(size * 100)
        )
    }
}

