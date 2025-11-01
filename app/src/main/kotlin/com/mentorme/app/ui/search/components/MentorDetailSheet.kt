package com.mentorme.app.ui.search.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextOverflow
import com.mentorme.app.ui.home.Mentor as HomeMentor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MentorDetailContent(
    mentor: HomeMentor,
    onClose: () -> Unit,
    onBookNow: (String) -> Unit,
    onMessage: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✨ Chi tiết mentor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            Spacer(Modifier.height(8.dp))

            // Top summary
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    mentor.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${mentor.role} • ${mentor.company}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⭐ ${"%.1f".format(mentor.rating)}", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(0.14f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.White.copy(0.28f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${mentor.hourlyRate} VNĐ/giờ", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(0.14f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.White.copy(0.28f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Stacked CTAs
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onBookNow(mentor.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Đặt lịch ngay")
                    }
                    OutlinedButton(
                        onClick = { onMessage(mentor.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Nhắn tin")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Edge-to-edge segmented tabs
            SegmentedTabs(
                titles = listOf("Tổng quan", "Kinh nghiệm", "Đánh giá", "Lịch trống"),
                selectedIndex = activeTab,
                onSelect = { activeTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Content
            when (activeTab) {
                0 -> OverviewTab(mentor)
                1 -> ExperienceTab()
                2 -> ReviewsTab()
                3 -> ScheduleTab(onBookNow = { onBookNow(mentor.id) })
            }
        }
    }
}

@Composable
private fun SegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.12f),
        shape = outerShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            titles.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val innerShape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(innerShape)
                        .background(
                            if (selected) Color.White.copy(alpha = 0.28f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(mentor: HomeMentor) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Senior ${mentor.role} với nhiều kinh nghiệm thực chiến. Chia sẻ về lộ trình, kỹ năng và định hướng.",
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )
            }
        }
        if (mentor.skills.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    mentor.skills.forEach {
                        AssistChip(
                            onClick = {},
                            label = { Text(it, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) },
                            modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 36.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(0.14f)
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = Color.White.copy(0.28f),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
        items(listOf("Ngôn ngữ: Tiếng Việt, English", "Hình thức: Online")) { line ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(line, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ExperienceTab() {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            listOf(
                "Senior Software Engineer • 2020 - nay • Tech Company\nPhát triển hệ thống backend phục vụ hàng triệu người dùng.",
                "Software Engineer • 2018 - 2020 • Startup\nXây dựng sản phẩm từ giai đoạn đầu, làm việc trong team nhỏ."
            )
        ) { s ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) { Text(s, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)) }
        }
    }
}

@Composable
private fun ReviewsTab() {
    val reviews = listOf(
        "Mentor tận tâm, giúp tôi rõ career path. Highly recommended! — 15/01/2024",
        "Session bổ ích, kinh nghiệm thực tế, chia sẻ chi tiết. — 10/01/2024",
        "Professional & friendly. Sẽ book thêm. — 05/01/2024",
    )
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reviews) { r ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(r, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
            }
        }
    }
}

@Composable
private fun ScheduleTab(onBookNow: () -> Unit) {
    val slots = listOf(
        "Thứ Bảy, 20 tháng 1, 2024 • 09:00 - 10:00",
        "Thứ Bảy, 20 tháng 1, 2024 • 14:00 - 15:00",
        "Chủ Nhật, 21 tháng 1, 2024 • 10:00 - 11:00",
        "Chủ Nhật, 21 tháng 1, 2024 • 16:00 - 17:00",
    )
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(slots) { s ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s, modifier = Modifier.weight(1f))
                    Button(
                        onClick = onBookNow,
                        modifier = Modifier.fillMaxWidth(0.38f).heightIn(min = 46.dp)
                    ) { Text("Đặt lịch") }
                }
            }
        }
    }
}
