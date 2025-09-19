package com.mentorme.app.ui.messages

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

@Composable
fun ChatComposer(
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .liquidGlassStrong(radius = 24.dp, alpha = 0.28f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MMTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Type a message…",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MMButton(
                text = "Send",
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                    }
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) }
            )
        }
    }
}
