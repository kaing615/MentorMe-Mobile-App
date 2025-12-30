package com.mentorme.app.ui.components.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.liquidGlassStrong

/**
 * Banner hiển thị khi có mentee đang đợi mentor join vào phiên học
 * Hiển thị ở trang chủ và trang lịch hẹn của mentor
 */
@Composable
fun ActiveSessionBanner(
    bookingId: String,
    menteeName: String?,
    onJoinSession: (String) -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassStrong(radius = 20.dp, alpha = 0.26f)
            .background(Color(0xFFFF9800).copy(alpha = 0.18f)),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Mentee đang đợi bạn!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!menteeName.isNullOrBlank()) {
                            Text(
                                text = menteeName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Text(
                text = "Mentee đã vào phòng và đang chờ bạn bắt đầu phiên học.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            
            MMButton(
                text = "Vào phiên học ngay",
                onClick = { onJoinSession(bookingId) },
                useGlass = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Compact version - banner nhỏ gọn hơn
 */
@Composable
fun CompactSessionBanner(
    bookingId: String,
    menteeName: String?,
    onJoinSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassStrong(radius = 16.dp, alpha = 0.26f)
            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Mentee đang đợi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (!menteeName.isNullOrBlank()) {
                        Text(
                            text = menteeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
            
            Button(
                onClick = { onJoinSession(bookingId) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Vào ngay", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
