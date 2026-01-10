package com.mentorme.app.ui.chat.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.liquidGlassStrong
import com.mentorme.app.ui.components.ui.MMButtonSize
import android.net.Uri

@Composable
fun ChatComposer(
    onSend: (String) -> Unit,
    onSendFile: ((Uri, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Type a message…",
    onTypingChanged: ((Boolean) -> Unit)? = null,
    isUploading: Boolean = false
) {
    var text by remember { mutableStateOf("") }
    var lastTypingState by remember { mutableStateOf(false) }
    var lastTypingEmitTime by remember { mutableStateOf(0L) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            onSendFile?.invoke(it, fileName)
        }
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "file_${System.currentTimeMillis()}"
            onSendFile?.invoke(it, fileName)
        }
    }
    
    // Emit typing indicator when user types
    LaunchedEffect(text, onTypingChanged) {
        if (onTypingChanged != null) {
            val isTyping = text.isNotEmpty()
            val now = System.currentTimeMillis()
            
            if (isTyping != lastTypingState || (isTyping && now - lastTypingEmitTime > 2000)) {
                onTypingChanged(isTyping)
                lastTypingState = isTyping
                lastTypingEmitTime = now
            }
        }
    }
    
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
            // Image button
            if (onSendFile != null) {
                GlassIconButton(
                    icon = Icons.Default.Image,
                    contentDescription = "Gửi ảnh",
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = enabled && !isUploading
                )
                Spacer(Modifier.width(4.dp))
                
                // File button
                GlassIconButton(
                    icon = Icons.Default.AttachFile,
                    contentDescription = "Gửi file",
                    onClick = { filePickerLauncher.launch("*/*") },
                    enabled = enabled && !isUploading
                )
                Spacer(Modifier.width(8.dp))
            }
            
            MMTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = if (isUploading) "Đang tải file..." else placeholder,
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled && !isUploading
            )
            Spacer(Modifier.width(8.dp))
            MMButton(
                text = "Send",
                onClick = { sendAndClear() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                size = MMButtonSize.Compact,
                enabled = enabled && !isUploading && text.isNotBlank()
            )
        }
    }
}
