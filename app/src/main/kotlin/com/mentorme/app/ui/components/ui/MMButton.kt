package com.mentorme.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.GradientSecondary   // --gradient-secondary
import com.mentorme.app.ui.theme.gradientBackground  // gradient modifier

/**
 * Nút primary: nền gradient (secondary), chữ màu trắng (theo --primary-foreground ~ #FFF/0.95).
 * Map từ CSS:
 *  - background: var(--gradient-secondary)
 *  - color: var(--primary-foreground)
 */
@Composable
fun MMPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .background(Color.Transparent, RoundedCornerShape(16.dp))
            .gradientBackground(GradientSecondary),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White // --primary-foreground ~ rgba(255,255,255,0.95)
        ),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}
