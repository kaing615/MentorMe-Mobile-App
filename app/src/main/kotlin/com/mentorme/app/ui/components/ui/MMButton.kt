package com.mentorme.app.ui.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.GradientSecondary   // --gradient-secondary
import com.mentorme.app.ui.theme.gradientBackground  // gradient modifier
import com.mentorme.app.ui.theme.liquidGlass
import androidx.compose.ui.graphics.Shape

/**
 * Nút primary: nền gradient (secondary), chữ màu trắng (theo --primary-foreground ~ #FFF/0.95).
 * Map từ CSS:
 *  - background: var(--gradient-secondary)
 *  - color: var(--primary-foreground)
 */

// Thêm enum kích thước
enum class MMButtonSize { Compact, Medium, Large }

@Composable
fun MMPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(shape)
            .background(Color.Transparent, RoundedCornerShape(16.dp))
            .gradientBackground(GradientSecondary)
            .defaultMinSize(minHeight = 40.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), // dọc 10dp
        content = content
    )
}

@Composable
fun MMGhostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 40.dp), // ✅
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        content = content
    )
}

/**
 * Standard MMButton component with text and optional leading icon
 */
@Composable
fun MMButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    useGlass: Boolean = true,
    size: MMButtonSize = MMButtonSize.Medium,            // ✅ thêm size
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White
    )
) {
    val (minH, padH, padV) = when (size) {
        MMButtonSize.Compact -> Triple(36.dp, 12.dp, 8.dp)  // phù hợp nút thấp
        MMButtonSize.Medium  -> Triple(40.dp, 16.dp, 10.dp)
        MMButtonSize.Large   -> Triple(48.dp, 18.dp, 12.dp)
    }

    val appliedModifier =
        (if (useGlass) modifier.liquidGlass(radius = 16.dp) else modifier)
            .defaultMinSize(minHeight = minH)               // ✅ không cắt chữ

    Button(
        onClick = onClick,
        modifier = appliedModifier,
        colors = colors,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = padH, vertical = padV)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingIcon?.invoke()
            Text(text = text, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}
