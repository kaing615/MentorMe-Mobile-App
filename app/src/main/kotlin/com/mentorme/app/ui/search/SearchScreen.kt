package com.mentorme.app.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.mentors.MentorCard
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import kotlin.math.roundToInt
import com.mentorme.app.ui.home.Mentor as HomeMentor

/* ========= Mock data (có thể bỏ nếu bạn lấy từ repo thật) ========= */
object MentorMocks {
    val all = listOf(
        HomeMentor("m1","Nguyễn Văn An","Senior Android Engineer","Google",4.9,156, listOf("Android","Kotlin","Architecture"), 900_000, isAvailable = true),
        HomeMentor("m2","Trần Thị Minh","Product Manager","Meta",4.8,203, listOf("Strategy","Analytics","Leadership"), 1_100_000, isAvailable = true),
        HomeMentor("m3","Lê Hoàng Nam","UX/UI Designer","Apple",4.9,89, listOf("Figma","Design Systems","User Research"), 800_000, isAvailable = false),
        HomeMentor("m4","Phạm Quang Huy","Data Scientist","Grab",4.7,120, listOf("Python","ML","Data Pipeline"), 1_200_000, isAvailable = true),
        HomeMentor("m5","Võ Như Ý","Frontend Engineer","Shopify",4.8,98, listOf("React","TypeScript","System Design"), 950_000, isAvailable = true),
        HomeMentor("m6","Đỗ Trọng Tín","DevOps Engineer","Amazon",4.6,77, listOf("AWS","Kubernetes","CI/CD"), 1_000_000, isAvailable = true),
        HomeMentor("m7","Ngô Bảo Châu","Backend Engineer","Netflix",4.9,145, listOf("Java","Microservices","Kafka"), 1_150_000, isAvailable = true),
        HomeMentor("m8","Lý Thu Trang","Product Designer","Spotify",4.7,64, listOf("UX Writing","Prototyping","Research"), 850_000, isAvailable = true),
    )
}

private enum class SortOption(val label: String) {
    Relevance("Phù hợp"),
    RatingDesc("Đánh giá cao"),
    PriceAsc("Giá thấp → cao"),
    PriceDesc("Giá cao → thấp")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchMentorScreen(
    mentors: List<HomeMentor> = MentorMocks.all,
    onOpenProfile: (String) -> Unit = {},
    onBook: (String) -> Unit = {}
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        // ===== State (saveable) =====
        var query by rememberSaveable { mutableStateOf("") }
        val allSkills = remember(mentors) { mentors.flatMap { it.skills }.distinct().sorted() }

        // Dùng List thay vì Set để save/restore dễ hơn
        var selectedSkills by rememberSaveable { mutableStateOf(listOf<String>()) }

        var minRating by rememberSaveable { mutableFloatStateOf(0f) }

        // Price steps (x50k): tách 2 primitive để saveable ổn định
        var priceStart by rememberSaveable { mutableIntStateOf(0) }   // 0..20
        var priceEnd   by rememberSaveable { mutableIntStateOf(20) }  // 0..20
        val priceRange = priceStart.toFloat()..priceEnd.toFloat()

        // Sort: lưu theo name để saveable
        var sortName by rememberSaveable { mutableStateOf(SortOption.Relevance.name) }
        val sort = remember(sortName) { SortOption.valueOf(sortName) }

        var showFilter by rememberSaveable { mutableStateOf(true) }

        // Nhớ vị trí scroll của list
        val listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

        // ===== Filter + sort =====
        val filtered = remember(query, selectedSkills, minRating, priceStart, priceEnd, mentors) {
            mentors.filter { m ->
                (query.isBlank() || m.name.contains(query, ignoreCase = true)) &&
                        (selectedSkills.isEmpty() || m.skills.any { it in selectedSkills }) &&
                        m.rating >= minRating &&
                        run {
                            val steps = m.hourlyRate / 50_000f
                            steps >= priceStart && steps <= priceEnd
                        }
            }
        }
        val result = remember(filtered, sort) {
            when (sort) {
                SortOption.Relevance -> filtered
                SortOption.RatingDesc -> filtered.sortedByDescending { it.rating }
                SortOption.PriceAsc  -> filtered.sortedBy { it.hourlyRate }
                SortOption.PriceDesc -> filtered.sortedByDescending { it.hourlyRate }
            }
        }

        // ===== Layout =====
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 75.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Tìm mentor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search box
            item {
                MMTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Nhập tên mentor…",
                    leading = { Icon(Icons.Default.Search, null, tint = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(radius = 24.dp)
                )
            }

            // Summary + actions
            item {
                LiquidGlassCard(radius = 18.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${result.size} kết quả",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f)
                        )

                        SortMenu(
                            sort = sort,
                            onSortChange = { sortName = it.name }
                        )

                        Spacer(Modifier.width(8.dp))

                        MMGhostButton(onClick = { showFilter = !showFilter }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (showFilter) "Ẩn lọc" else "Bộ lọc")
                            }
                        }
                    }
                }
            }

            // Filter card (collapsible)
            item {
                AnimatedVisibility(
                    visible = showFilter,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Bộ lọc nhanh", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                SectionCaption(
                                    if (selectedSkills.isEmpty()) "Chưa chọn"
                                    else "${selectedSkills.size} bộ lọc"
                                )
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = {
                                    query = ""
                                    selectedSkills = emptyList()
                                    minRating = 0f
                                    priceStart = 0
                                    priceEnd = 20
                                }) { Text("Đặt lại") }
                            }

                            // Skills – chip “glass”
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allSkills.take(18).forEach { tag ->
                                    val selected = tag in selectedSkills
                                    GlassFilterChip(
                                        text = tag,
                                        selected = selected,
                                        onToggle = {
                                            selectedSkills = if (selected) selectedSkills - tag else selectedSkills + tag
                                        }
                                    )
                                }
                            }

                            // Rating
                            Text("Đánh giá tối thiểu: ${minRating.toInt()}★", style = MaterialTheme.typography.labelLarge)
                            Slider(
                                value = minRating,
                                onValueChange = { minRating = it },
                                valueRange = 0f..5f,
                                steps = 4,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White.copy(0.95f),
                                    inactiveTrackColor = Color.White.copy(0.22f)
                                )
                            )

                            // Price (x50k)
                            val fromVnd = (priceStart * 50_000)
                            val toVnd   = (priceEnd * 50_000)
                            Text("Khoảng giá: ${compactVnd(fromVnd)} – ${compactVnd(toVnd)}", style = MaterialTheme.typography.labelLarge)
                            RangeSlider(
                                value = priceRange,
                                onValueChange = { range ->
                                    priceStart = range.start.roundToInt().coerceIn(0, 20)
                                    priceEnd   = range.endInclusive.roundToInt().coerceIn(priceStart, 20)
                                },
                                valueRange = 0f..20f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White.copy(0.95f),
                                    inactiveTrackColor = Color.White.copy(0.22f)
                                )
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                SectionCaption("0")
                                SectionCaption("1M")
                            }

                            // Apply
                            MMPrimaryButton(
                                onClick = { /* giữ filter – không cần làm gì thêm */ },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Áp dụng") }
                        }
                    }
                }
            }

            // ===== Results =====
            if (result.isEmpty()) {
                item {
                    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy mentor phù hợp")
                        }
                    }
                }
            } else {
                items(result, key = { it.id }) { m ->
                    MentorCard(
                        mentor = m,
                        onViewProfile = { onOpenProfile(m.id) },
                        onBookSession = { onBook(m.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ========= UI helpers ========= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenu(
    sort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        ) {
            Icon(Icons.Default.Sort, null)
            Spacer(Modifier.width(6.dp))
            Text(sort.label)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun compactVnd(amount: Int): String {
    val v = amount
    return when {
        v >= 1_000_000 -> {
            val f = v / 1_000_000f
            val s = if (f % 1f == 0f) f.toInt().toString() else String.format("%.1f", f)
            s.trimEnd('0').trimEnd('.') + "M"
        }
        v >= 1_000 -> {
            val f = v / 1_000f
            val s = if (f % 1f == 0f) f.toInt().toString() else String.format("%.0f", f)
            s + "k"
        }
        else -> v.toString()
    }
}

@Composable
private fun GlassFilterChip(
    text: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (selected) Color.White.copy(.22f) else Color.White.copy(.10f)
    val stroke = if (selected) Color.White.copy(.40f) else Color.White.copy(.18f)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, stroke, shape)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionCaption(text: String) {
    Text(
        text = text,
        color = Color.White.copy(0.8f),
        style = MaterialTheme.typography.labelSmall
    )
}
