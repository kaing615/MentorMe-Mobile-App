package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.ui.theme.LiquidGlassCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSection(
    onSearch: (String) -> Unit,
    onlineCount: Int = 0,
    avgRating: Double = 0.0
) {
    var query by remember { mutableStateOf("") }
    val skills = listOf(
        "Lập trình",
        "Marketing",
        "Thiết kế",
        "Kinh doanh",
        "Tài chính",
        "Data Science"
    )

    // Gradient vàng gold NEON
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD700), // Bright Gold
            Color(0xFFFFC700), // Electric Yellow
            Color(0xFFFFAA00)  // Amber
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                ),
            radius = 28.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Heading với gradient vàng
                Text(
                    buildAnnotatedString {
                        append("Tìm mentor ")
                        withStyle(
                            style = SpanStyle(
                                brush = goldGradient,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append("phù hợp")
                        }
                        append("\ncho sự nghiệp của bạn")
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                )

                Text(
                    "Kết nối với chuyên gia để phát triển kỹ năng và mục tiêu nghề nghiệp.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 24.sp
                )

                // Search Bar
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    radius = 16.dp
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                "Tìm kiếm mentor theo kỹ năng, lĩnh vực...",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFFFFD700), // Gold cursor
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (query.isNotBlank()) {
                                    onSearch(query)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Skills phổ biến
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.forEach { skill ->
                        Surface(
                            onClick = { onSearch(skill) },
                            modifier = Modifier,
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                skill,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Online count
            LiquidGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    ),
                radius = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF88)) // Neon Green
                    )
                    Text(
                        text = "${formatCompactNumber(onlineCount)} online",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Rating
            LiquidGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    ),
                radius = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Gold star
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (avgRating > 0) "%.1f đánh giá".format(avgRating) else "0 đánh giá",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

