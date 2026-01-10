package com.mentorme.app.ui.chat.components

import android.content.Context
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
                .widthIn(max = 320.dp)
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
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}

@Composable
fun AiMessageBubble(
    text: String,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI Bot Avatar (only for bot messages)
        if (!isMine) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(
                    if (isMine) 
                        Modifier.liquidGlassStrong(radius = 20.dp, alpha = 0.32f) 
                    else 
                        Modifier.liquidGlass(radius = 20.dp)
                )
                .then(
                    if (isMine) 
                        Modifier.background(Color(0xFF1D4ED8).copy(alpha = 0.26f)) 
                    else 
                        Modifier
                ),
            tonalElevation = 0.dp,
            color = Color.Transparent
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
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
    
    // Auto-detect file type from URL if messageType is not set correctly
    val isImageUrl = content.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|bmp)([?#].*)?$", RegexOption.IGNORE_CASE)) ||
        (content.contains("cloudinary.com") && content.contains("/image/"))
    val isFileUrl = content.startsWith("http") && (
        content.contains("cloudinary.com/") || 
        content.matches(Regex(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|zip|rar)([?#].*)?$", RegexOption.IGNORE_CASE))
    )
    
    // Debug logging
    android.util.Log.d("MessageBubble", "Content: $content")
    android.util.Log.d("MessageBubble", "MessageType: $messageType, isImageUrl: $isImageUrl, isFileUrl: $isFileUrl")
    
    when {
        messageType == "image" || (isImageUrl && !isFileUrl) -> {
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
        messageType == "file" || isFileUrl -> {
            // Display file attachment
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            android.util.Log.d("MessageBubble", "Download clicked - URL: $content")
                            
                            // Extract clean filename and extension
                            val urlPath = content.substringBefore('?')
                            val fileName = urlPath
                                .substringAfterLast('/')
                                .ifEmpty { "file_${System.currentTimeMillis()}" }
                            
                            android.util.Log.d("MessageBubble", "Extracted fileName: $fileName")
                            
                            // Ensure file has extension
                            val finalFileName = if (!fileName.contains('.')) {
                                when {
                                    content.contains("pdf", ignoreCase = true) -> "$fileName.pdf"
                                    content.contains("doc", ignoreCase = true) -> "$fileName.docx"
                                    content.contains("xls", ignoreCase = true) -> "$fileName.xlsx"
                                    else -> fileName
                                }
                            } else fileName
                            
                            android.util.Log.d("MessageBubble", "Final fileName: $finalFileName")
                            
                            // Detect MIME type
                            val mimeType = when {
                                finalFileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                                finalFileName.endsWith(".doc", ignoreCase = true) -> "application/msword"
                                finalFileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                finalFileName.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
                                finalFileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                finalFileName.endsWith(".ppt", ignoreCase = true) -> "application/vnd.ms-powerpoint"
                                finalFileName.endsWith(".pptx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                finalFileName.endsWith(".zip", ignoreCase = true) -> "application/zip"
                                finalFileName.endsWith(".rar", ignoreCase = true) -> "application/x-rar-compressed"
                                else -> "*/*"
                            }
                            
                            android.util.Log.d("MessageBubble", "MIME type: $mimeType")
                            
                            // Download file using DownloadManager
                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                            val request = android.app.DownloadManager.Request(Uri.parse(content)).apply {
                                setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, finalFileName)
                                setTitle("Đang tải $finalFileName")
                                setDescription("Tải file từ chat")
                                setMimeType(mimeType)
                                // Allow download over metered network
                                setAllowedOverMetered(true)
                                setAllowedOverRoaming(true)
                                // Add headers if needed for Cloudinary
                                addRequestHeader("User-Agent", "MentorMe-Mobile-App")
                            }
                            val downloadId = downloadManager.enqueue(request)
                            android.util.Log.d("MessageBubble", "Download enqueued with ID: $downloadId")
                            android.widget.Toast.makeText(context, "Đang tải xuống: $finalFileName", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("MessageBubble", "Download failed", e)
                            android.widget.Toast.makeText(context, "Không thể tải file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = Color(0xFF2D3748).copy(alpha = 0.8f), // Dark blue-gray background
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B).copy(alpha = 0.5f),
                                    Color(0xFF334155).copy(alpha = 0.3f)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show icon based on file extension
                        val fileName = content.substringAfterLast('/').substringBefore('?')
                        val fileIcon = when {
                            fileName.endsWith(".pdf", ignoreCase = true) -> "📄"
                            fileName.endsWith(".doc", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true) -> "📝"
                            fileName.endsWith(".xls", ignoreCase = true) || fileName.endsWith(".xlsx", ignoreCase = true) -> "📊"
                            fileName.endsWith(".ppt", ignoreCase = true) || fileName.endsWith(".pptx", ignoreCase = true) -> "📊"
                            fileName.endsWith(".zip", ignoreCase = true) || fileName.endsWith(".rar", ignoreCase = true) -> "📦"
                            else -> null
                        }
                        
                        // Icon with background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFF4A5568).copy(alpha = 0.6f),
                                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fileIcon != null) {
                                Text(
                                    text = fileIcon,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            } else {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color(0xFFA5B4FC)
                                )
                            }
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Nhấn để tải về",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    
                    // Download icon with background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF3B82F6).copy(alpha = 0.2f),
                                androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF60A5FA)
                        )
                    }
                }
            }
        }
        else -> {
            // Regular text message
            Text(
                text = content,
                modifier = modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}
