package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.liquidGlassStrong
import com.mentorme.app.ui.components.ui.MMButtonSize

@Composable
fun ChatComposer(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Type a message…"
) {
    var text by remember { mutableStateOf("") }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassStrong(radius = 24.dp, alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MMTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled
            )
            Spacer(Modifier.width(8.dp))
            MMButton(
                text = "Send",
                onClick = { if (text.isNotBlank() && enabled) { onSend(text.trim()); text = "" } },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                size = MMButtonSize.Compact,
                enabled = enabled
            )
        }
    }
}
