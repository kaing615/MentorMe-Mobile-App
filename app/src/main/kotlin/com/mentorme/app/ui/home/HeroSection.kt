package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSection(
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val skills = listOf("Lập trình", "Marketing", "Thiết kế", "Kinh doanh", "Tài chính", "Data Science")

    // Gradient vàng gold
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFB347), // Light gold
            Color(0xFFFF8C00)  // Dark orange gold
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        append("Tìm mentor ")
                        withStyle(
                            style = SpanStyle(
                                brush = goldGradient, // Sử dụng gradient vàng gold
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append("phù hợp")
                        }
                        append("\ncho sự nghiệp của bạn")
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Kết nối với chuyên gia để phát triển kỹ năng và mục tiêu nghề nghiệp.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
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
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSearch(query) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Skills phổ biến
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    skills.forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { onSearch(s) }
                        ) {
                            Text(s, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Floating stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .liquidGlass(radius = 16.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("1000+ online", color = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .liquidGlass(radius = 16.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("4.9⭐", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Đánh giá", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
