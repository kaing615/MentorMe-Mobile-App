package com.mentorme.app.ui.mentors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.home.Mentor
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
// Ở đầu HomeScreen.kt, MentorCard.kt, CalendarScreen.kt
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMButtonSize
import com.mentorme.app.ui.common.compactVnd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MentorCard(
    mentor: Mentor,
    onViewProfile: () -> Unit = {},
    onBookSession: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with avatar and basic info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mentor.name.split(" ").map { it.first() }.take(2).joinToString(""),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = mentor.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = mentor.role,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = mentor.company,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Favorite button
                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        onFavoriteClick()
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Rating and reviews
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .liquidGlass(radius = 12.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${mentor.rating}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "(${mentor.totalReviews} đánh giá)",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.weight(1f))

                // Availability status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (mentor.isAvailable) Color(0xFF22C55E).copy(alpha = 0.2f)
                            else Color(0xFFEF4444).copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (mentor.isAvailable) "Có thể đặt lịch" else "Bận",
                        color = if (mentor.isAvailable) Color(0xFF22C55E) else Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Skills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipMinWidth = 96.dp
                val chipMinHeight = 32.dp

                // Các skill chính
                mentor.skills.take(3).forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .defaultMinSize(minWidth = chipMinWidth, minHeight = chipMinHeight) // 👈 áp dụng ở đây
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skill,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Chip "+x"
                if (mentor.skills.size > 3) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .defaultMinSize(minWidth = chipMinWidth, minHeight = chipMinHeight) // 👈 và cả đây
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${mentor.skills.size - 3}",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Price and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${mentor.hourlyRate.compactVnd()} VNĐ/giờ",   // ⬅️ rút gọn theo k/M
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "Giá tư vấn",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MMButton(
                        text = "Xem hồ sơ",
                        onClick = onViewProfile,
                        size = MMButtonSize.Compact
                    )

                    if (mentor.isAvailable) {
                        MMButton(
                            text = "Đặt lịch",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.VideoCall,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = onBookSession,
                            size = MMButtonSize.Compact
                        )
                    }
                }
            }
        }
    }
}
