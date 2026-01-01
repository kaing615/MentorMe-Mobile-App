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
    placeholder: String = "Type a message…",
    onTypingChanged: ((Boolean) -> Unit)? = null
) {
    var text by remember { mutableStateOf("") }
    var lastTypingState by remember { mutableStateOf(false) }
    var lastTypingEmitTime by remember { mutableStateOf(0L) }
    
    // Emit typing indicator when user types
    LaunchedEffect(text, onTypingChanged) {
        if (onTypingChanged != null) {
            val isTyping = text.isNotEmpty()
            val now = System.currentTimeMillis()
            
            // Only emit if state changed or enough time passed (for throttling "still typing")
            if (isTyping != lastTypingState || (isTyping && now - lastTypingEmitTime > 2000)) {
                android.util.Log.d("ChatComposer", "Emitting typing: $isTyping, text: '$text'")
                onTypingChanged(isTyping)
                lastTypingState = isTyping
                lastTypingEmitTime = now
            }
        }
    }
    
    // Stop typing indicator when sending message
    fun sendAndClear() {
        if (text.isNotBlank() && enabled) {
            onTypingChanged?.invoke(false)
            lastTypingState = false
            onSend(text.trim())
            text = ""
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassStrong(radius = 24.dp, alpha = if (enabled) 0.26f else 0.18f)
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
                onClick = { sendAndClear() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                size = MMButtonSize.Compact,
                enabled = enabled
            )
        }
    }
}
