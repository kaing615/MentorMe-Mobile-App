package com.mentorme.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.LiquidGlassCard
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.mentors.MentorCard
import com.mentorme.app.ui.mentors.MentorUi
import com.mentorme.app.ui.theme.GradientPrimary
import com.mentorme.app.ui.theme.gradientBackground

@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }

    val quickStats = listOf(
        Triple("Mentor chất lượng", "500+", Icons.Filled.Star),
        Triple("Buổi tư vấn", "10k+", Icons.Filled.CalendarMonth),
        Triple("Đánh giá 5 sao", "98%", Icons.Filled.ThumbUp),
        Triple("Phản hồi nhanh", "<2h", Icons.Filled.FlashOn),
    )

    val categories = listOf(
        Triple("tech", "Technology", "💻"),
        Triple("business", "Business", "📊"),
        Triple("design", "Design", "🎨"),
        Triple("marketing", "Marketing", "📱"),
        Triple("finance", "Finance", "💰"),
        Triple("career", "Career", "🚀"),
    )

    val mentors = listOf(
        MentorUi(
            id = "1",
            fullName = "Alice Nguyen",
            avatar = null,
            verified = true,
            hourlyRate = 35,
            rating = 4.9,
            totalReviews = 180,
            bio = "Mobile engineer 6+ năm, chuyên Kotlin/Compose, tối ưu performance & clean architecture.",
            skills = listOf("Kotlin", "Compose", "Clean Arch"),
            experience = "6+ năm"
        ),
        MentorUi(
            id = "2",
            fullName = "Bao Tran",
            avatar = null,
            verified = true,
            hourlyRate = 30,
            rating = 4.8,
            totalReviews = 150,
            bio = "Backend (Node.js + Postgres). Thiết kế API, tối ưu query, triển khai CI/CD.",
            skills = listOf("Node", "Postgres", "CI/CD"),
            experience = "5+ năm"
        ),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground(GradientPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Search
        item {
            LiquidGlassCard(strong = true) {
                Text("Chào mừng tới MentorMe 👋", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))

                MMTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Nhập từ khóa tìm kiếm...",
                    leading = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White) }
                )

                Spacer(Modifier.height(12.dp))

                MMPrimaryButton(
                    onClick = { /* TODO: xử lý search */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Search, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Tìm kiếm", color = Color.White)
                }
            }
        }

        // Quick stats
        item {
            Text("Thống kê nhanh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(quickStats.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value, icon) ->
                    LiquidGlassCard(modifier = Modifier.weight(1f)) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = Color(0xFF93C5FD))
                            Spacer(Modifier.height(4.dp))
                            Text(value, fontWeight = FontWeight.Bold)
                            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                        }
                    }
                }
            }
        }

        // Categories
        item {
            Text("Khám phá theo lĩnh vực", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(categories.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (id, name, emoji) ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* TODO: filter by id */ }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Text(name, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Top rated mentors
        item {
            Text("Mentor được đánh giá cao", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(mentors) { m ->
            MentorCard(
                mentor = m,
                onViewProfile = { /* TODO */ },
                onBookSession = { /* TODO */ }
            )
        }

        // Featured mentors
        item {
            Text("Mentor nổi bật", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(mentors) { m ->
            MentorCard(
                mentor = m,
                onViewProfile = { /* TODO */ },
                onBookSession = { /* TODO */ }
            )
        }
    }
}
