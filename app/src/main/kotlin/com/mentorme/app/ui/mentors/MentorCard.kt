package com.mentorme.app.ui.mentors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mentorme.app.ui.components.ui.LiquidGlassCard

data class MentorUi(
    val id: String,
    val fullName: String,
    val avatar: String?,
    val verified: Boolean,
    val hourlyRate: Int,
    val rating: Double,
    val totalReviews: Int,
    val bio: String,
    val skills: List<String>,
    val experience: String,
    val expertise: List<String>
)

@Composable
fun MentorCard(
    mentor: MentorUi,
    onViewProfile: (String) -> Unit,
    onBookSession: (String) -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            // Profile header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    if (mentor.avatar != null) {
                        AsyncImage(
                            model = mentor.avatar,
                            contentDescription = mentor.fullName,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                mentor.fullName.split(" ").map { it[0] }.joinToString(""),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    // online indicator
                    Box(
                        Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                mentor.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (mentor.verified) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("✓ Đã xác minh") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFF10B981),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                        Text(
                            "${mentor.hourlyRate}$/h",
                            modifier = Modifier
                                .background(
                                    Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            Modifier
                                .background(Color.White.copy(0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            repeat(5) { i ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < mentor.rating.toInt()) Color(0xFFFFD54F) else Color.White.copy(0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            "${mentor.rating} (${mentor.totalReviews})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Bio
            Text(
                mentor.bio,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.7f),
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            // Skills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mentor.skills.take(2).forEach {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
                if (mentor.skills.size > 2) {
                    AssistChip(onClick = {}, label = { Text("+${mentor.skills.size - 2}") })
                }
            }

            Spacer(Modifier.height(8.dp))

            // Experience & language
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(onClick = {}, label = { Text(mentor.experience) })
                AssistChip(
                    onClick = {},
                    label = { Text("Online") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(0.1f),
                        labelColor = Color(0xFF4ADE80)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Footer buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewProfile(mentor.id) },
                    modifier = Modifier.weight(1f)
                ) { Text("Xem chi tiết") }
                Button(
                    onClick = { onBookSession(mentor.id) },
                    modifier = Modifier.weight(1f)
                ) { Text("Đặt lịch") }
            }
        }
    }
}
