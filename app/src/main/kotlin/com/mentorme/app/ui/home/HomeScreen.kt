package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Ở đầu HomeScreen.kt, MentorCard.kt, CalendarScreen.kt
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.mentors.MentorCard
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass

// ===== Data Models =====
data class Mentor(
    val id: String,
    val name: String,
    val role: String,
    val company: String,
    val rating: Double,
    val totalReviews: Int,
    val skills: List<String>,
    val hourlyRate: Int,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)

data class SuccessStory(
    val title: String,
    val quote: String,
    val author: String,
    val icon: ImageVector,
    val bgGradient: Brush
)

// ===== Sample Data =====
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

val featuredMentors = listOf(
    Mentor(
        id = "1",
        name = "Nguyễn Văn An",
        role = "Senior Software Engineer",
        company = "Google",
        rating = 4.9,
        totalReviews = 156,
        skills = listOf("Android", "Kotlin", "Architecture"),
        hourlyRate = 500,
        isAvailable = true
    ),
    Mentor(
        id = "2",
        name = "Trần Thị Minh",
        role = "Product Manager",
        company = "Meta",
        rating = 4.8,
        totalReviews = 203,
        skills = listOf("Strategy", "Analytics", "Leadership"),
        hourlyRate = 600,
        isAvailable = true
    ),
    Mentor(
        id = "3",
        name = "Lê Hoàng Nam",
        role = "UX/UI Designer",
        company = "Apple",
        rating = 4.9,
        totalReviews = 89,
        skills = listOf("Figma", "Design Systems", "User Research"),
        hourlyRate = 450,
        isAvailable = false
    )
)

val successStories = listOf(
    SuccessStory(
        title = "Từ Junior lên Senior trong 18 tháng",
        quote = "Nhờ mentor, tôi đã nắm vững kiến thức và kỹ năng cần thiết để thăng tiến nhanh chóng",
        author = "Phạm Minh Tuấn",
        icon = Icons.Default.TrendingUp,
        bgGradient = Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))
    ),
    SuccessStory(
        title = "Chuyển ngành thành công",
        quote = "Với sự hướng dẫn tận tình, tôi đã chuyển từ Marketing sang Tech một cách suôn sẻ",
        author = "Nguyễn Thu Hà",
        icon = Icons.Default.WorkspacePremium,
        bgGradient = Brush.linearGradient(listOf(Color(0xFFf093fb), Color(0xFFf5576c)))
    )
)

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onMentorClick: (String) -> Unit = {},
    onViewMentorProfile: (String) -> Unit = {},
    onBookSession: (String) -> Unit = {},
    onNavigateToMentors: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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

        item {
            SectionHeader(
                title = "Mentor nổi bật",
                subtitle = "Kết nối với những mentor hàng đầu"
            )
        }
        items(featuredMentors) { mentor ->
            MentorCard(
                mentor = mentor,
                onViewProfile = { onViewMentorProfile(mentor.id) },
                onBookSession = { onBookSession(mentor.id) }
            )
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MMButton(
                    text = "Xem tất cả mentor",
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    onClick = onNavigateToMentors
                )
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

        // Success Stories
        item {
            SectionHeader(
                title = "Câu chuyện thành công",
                subtitle = "Những thành tựu từ học viên của chúng tôi"
            )
        }
        items(successStories) { story ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                radius = 22.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(story.bgGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(story.icon, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(story.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(story.quote, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        Text("- ${story.author}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
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

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
