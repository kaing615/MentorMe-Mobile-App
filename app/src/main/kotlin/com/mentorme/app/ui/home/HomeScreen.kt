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
            skills = listOf("React", "Node.js", "AWS"), // Sửa từ expertise thành skills
            experience = "5+ years",
            rating = 4.9,
            hourlyRate = 50,
            totalReviews = 127, // Thêm totalReviews
            bio = "Senior Full-stack Developer at Google. Specialized in building scalable web applications.",
            expertise = listOf("React", "Node.js", "AWS") // Giữ lại expertise
        ),
        MentorUi(
            id = "2",
            fullName = "Bob Wilson",
            avatar = null,
            verified = true,
            skills = listOf("UI/UX", "Figma", "Design Systems"), // Sửa từ expertise thành skills
            experience = "7+ years",
            rating = 4.8,
            hourlyRate = 45,
            totalReviews = 89, // Thêm totalReviews
            bio = "Lead Product Designer with experience at top tech companies.",
            expertise = listOf("UI/UX", "Figma", "Design Systems") // Giữ lại expertise
        )
    )

    // Nội dung có thể cuộn, không có nền riêng - sẽ hiển thị liquid background phía sau
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) } // Spacer đầu để tránh header

        // Hero Section
        item {
            LiquidGlassCard {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Tìm mentor phù hợp\ncho sự nghiệp của bạn",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Kết nối với các chuyên gia hàng đầu để phát triển kỹ năng và thúc đẩy sự nghiệp",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(20.dp))

                    MMTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Tìm kiếm mentor theo kỹ năng...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    MMPrimaryButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tìm kiếm ngay") // Sử dụng content thay vì text parameter
                    }
                }
            }
        }

        // Quick Stats
        item {
            LiquidGlassCard {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Thống kê nổi bật",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))

                    quickStats.chunked(2).forEach { rowStats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowStats.forEach { (label, value, icon) ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            value,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        if (rowStats != quickStats.chunked(2).last()) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Categories
        item {
            LiquidGlassCard {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Lĩnh vực phổ biến",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))

                    categories.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (key, name, emoji) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { /* TODO: navigate to category */ }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        emoji,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        if (row != categories.chunked(3).last()) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Featured Mentors
        item {
            Text(
                "Mentor nổi bật",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(mentors) { mentor ->
            MentorCard(
                mentor = mentor,
                onViewProfile = { mentorId -> /* TODO: navigate to profile */ },
                onBookSession = { mentorId -> /* TODO: navigate to booking */ }
            )
        }

        item { Spacer(Modifier.height(16.dp)) } // Spacer cuối để tránh bottom nav
    }
}
