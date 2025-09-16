package com.mentorme.app.ui.components.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun GlassDialog(
    onDismiss: () -> Unit,
    title: String,
    text: @Composable () -> Unit,
    confirm: @Composable () -> Unit,
    dismiss: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { confirm() },
        dismissButton = { dismiss?.invoke() },
        title = { Text(title) },
        text = { text() },
        containerColor = Color.Transparent,
        modifier = Modifier.liquidGlassStrong().padding(8.dp)
    )
}
