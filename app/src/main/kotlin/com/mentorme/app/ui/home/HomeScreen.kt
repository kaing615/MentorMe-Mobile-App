package com.mentorme.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.LiquidGlassCard
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

// ===== demo data =====
private data class QuickStat(val title: String, val subtitle: String, val icon: @Composable () -> Unit)
private val quickStats = listOf(
    QuickStat("500+", "Mentor chất lượng") { Icon(Icons.Filled.Star, null, tint = Color.White) },
    QuickStat("10k+", "Buổi tư vấn") { Icon(Icons.Filled.CalendarMonth, null, tint = Color.White) },
    QuickStat("98%", "Đánh giá 5 sao") { Icon(Icons.Filled.ThumbUp, null, tint = Color.White) },
    QuickStat("< 2h", "Phản hồi nhanh") { Icon(Icons.Filled.Timelapse, null, tint = Color.White) },
)

private data class Category(val name: String, val emoji: String)
private val categories = listOf(
    Category("Technology", "💻"), Category("Business", "📈"),
    Category("Design", "🎨"),     Category("Marketing", "📱"),
    Category("Finance", "💰"),    Category("Career", "🚀"),
)

private data class MentorUi(val name: String, val role: String, val rating: Double)
private val topMentors = listOf(
    MentorUi("Nguyễn An", "Software Engineer", 4.9),
    MentorUi("Trần Minh", "Product Manager", 4.8),
    MentorUi("Lê Lan",    "UX Designer", 4.9),
    MentorUi("Hoàng Quân","Data Scientist", 4.7)
)
// ======================

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onMentorClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),                 // lề ngang
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp)
    ) {
        item { HeroSection(onSearch = onSearch) }

        item { SectionTitle("Thống kê nhanh") }
        items(quickStats.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { stat ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        radius = 22.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .liquidGlass(radius = 12.dp),
                                contentAlignment = Alignment.Center
                            ) { stat.icon() }

                            Column {
                                Text(
                                    stat.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    stat.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item { SectionTitle("Khám phá theo lĩnh vực") }
        items(categories.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { cat ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        radius = 22.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.emoji, fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item { SectionTitle("Mentor được đánh giá cao") }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(topMentors) { m ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .width(240.dp)
                            .height(140.dp),
                        radius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    m.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    m.role,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .liquidGlass(radius = 14.dp)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text("⭐ ${m.rating}", color = Color.White) }

                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .liquidGlass(radius = 14.dp)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text("Đặt lịch", color = Color.White) }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}
