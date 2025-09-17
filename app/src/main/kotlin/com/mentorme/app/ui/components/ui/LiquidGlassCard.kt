package com.mentorme.app.ui.components.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.theme.liquidGlassStrong

/**
 * LiquidGlassCard:
 * - strong = true  -> giống .glass-strong (đậm hơn)
 * - strong = false -> giống .glass (nhẹ)
 * - onClick != null -> Card có ripple/pressed state
 *
 * Dùng để thay Card thường trong Home/MentorCard để có hiệu ứng liquid-glass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassCard(
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
