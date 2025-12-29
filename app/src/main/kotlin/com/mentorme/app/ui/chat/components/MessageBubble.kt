package com.mentorme.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
            Text(
                text = m.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = if (isMe) TextAlign.End else TextAlign.Start,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
