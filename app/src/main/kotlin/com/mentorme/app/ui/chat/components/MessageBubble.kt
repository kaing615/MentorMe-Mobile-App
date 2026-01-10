package com.mentorme.app.ui.chat.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.data.model.chat.Message
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.theme.liquidGlassStrong

@Composable
fun MessageBubbleGlass(m: Message) {
    val isMe = m.fromCurrentUser
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        // Avatar for non-current user (left side)
        if (!isMe && m.senderAvatar != null) {
            AsyncImage(
                model = m.senderAvatar,
                contentDescription = m.senderName ?: "Avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
        }
        
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(
                    if (isMe) Modifier.liquidGlassStrong(radius = 20.dp, alpha = 0.32f) else Modifier.liquidGlass(radius = 20.dp)
                )
                .then(
                    if (isMe) Modifier.background(Color(0xFF1D4ED8).copy(alpha = 0.26f)) else Modifier
                )
                .padding(2.dp),
            tonalElevation = 0.dp,
            color = Color.Transparent
        ) {
            MessageContentWithFile(
                content = m.text,
                messageType = m.messageType,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MessageContentWithFile(
    content: String,
    messageType: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    when (messageType) {
        "image" -> {
            // Display image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(content)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clickable {
                        // Open image in browser or gallery
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
                        context.startActivity(intent)
                    }
            )
        }
        "file" -> {
            // Display file attachment
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable {
                        // Open file in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
                        context.startActivity(intent)
                    },
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "File đính kèm",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Nhấn để xem",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        else -> {
            // Regular text message
            Text(content, modifier = modifier)
        }
    }
}
