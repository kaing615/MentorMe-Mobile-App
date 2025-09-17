// ui/common/MMAtoms.kt  (thêm mới hoặc thay thế block buttons)
package com.mentorme.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.Border
import com.mentorme.app.ui.theme.Foreground
import com.mentorme.app.ui.theme.InputBg
import com.mentorme.app.ui.theme.liquidGlass

@Composable
fun MMPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable (() -> Unit))? = null,
    label: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.liquidGlass(radius = 16.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = InputBg,      // kính mờ đồng bộ Home
            contentColor   = Foreground,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor   = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leading?.invoke()
            if (leading != null) Spacer(Modifier.width(6.dp))
            label()
        }
    }
}

@Composable
fun MMGhostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable (() -> Unit))? = null,
    label: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.liquidGlass(radius = 16.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor   = Foreground,
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leading?.invoke()
            if (leading != null) Spacer(Modifier.width(6.dp))
            label()
        }
    }
}
