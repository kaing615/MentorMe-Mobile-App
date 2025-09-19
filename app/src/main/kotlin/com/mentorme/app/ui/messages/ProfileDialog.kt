package com.mentorme.app.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.GlassDialog

@Composable
fun ProfileSheet(
    name: String,
    role: String,
    onClose: () -> Unit
) {
    GlassDialog(
        onDismiss = onClose,
        title = name,
        text = {
            Column {
                Text(role)
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                Text("Bio / Skills / Languages … (mock)")
            }
        },
        confirm = {
            OutlinedButton(onClick = onClose) { Text("Đóng") }
        }
    )
}
