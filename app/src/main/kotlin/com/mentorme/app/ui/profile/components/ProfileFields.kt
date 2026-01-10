package com.mentorme.app.ui.profile.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.liquidGlass

enum class TextFieldType { Text, Email }

@Composable
fun LabeledField(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit,
    leading: (@Composable () -> Unit)? = null,
    type: TextFieldType = TextFieldType.Text
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .9f))
        Spacer(Modifier.height(6.dp))

        if (editing) {
            MMTextField(
                value = editedValue,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                singleLine = true,
                leading = {
                    if (leading != null) {
                        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides Color.White) {
                            leading()
                        }
                    }
                },
                placeholder = value
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .liquidGlass(radius = 16.dp, alpha = 0.18f, borderAlpha = 0.25f)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides Color.White) {
                    leading?.invoke()
                }
                if (leading != null) Spacer(Modifier.size(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LabeledMultiline(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        if (editing) {
            MMTextField(
                value = editedValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                placeholder = value
            )
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AvatarPicker(
    avatarUrl: String?,
    initial: Char,
    size: Dp,
    enabled: Boolean = true,
    onPick: (String) -> Unit
) {
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.toString()?.let(onPick)
    }

    val ring = Brush.linearGradient(
        listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(2.dp, ring), CircleShape)
            .liquidGlass(radius = size / 2, alpha = 0.22f, borderAlpha = 0.45f)
            .clickable(enabled = enabled) { pickImageLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        // Always show initial as fallback background
        Text(
            initial.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        // Load avatar on top if URL exists
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier.matchParentSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        if (enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(Modifier.size(4.dp))
                    Text("Đổi ảnh", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}
