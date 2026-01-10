package com.mentorme.app.ui.search

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch // ✅ For coroutine scope
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.mentorme.app.domain.usecase.SearchMentorsUseCase
import kotlin.math.roundToInt

// FlowRow còn ở ExperimentalLayoutApi (Compose version hiện tại)
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.mentors.MentorCard
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.home.Mentor as HomeMentor

import com.mentorme.app.data.mock.SearchMockData
import com.mentorme.app.ui.search.components.MentorDetailSheet
import com.mentorme.app.ui.search.components.BookSessionContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import com.mentorme.app.ui.components.ui.GlassOverlay
import androidx.compose.runtime.LaunchedEffect

private enum class SortOption(val label: String) {
    Relevance("Phù hợp"),
    RatingDesc("Đánh giá cao"),
    PriceAsc("Giá thấp → cao"),
    PriceDesc("Giá cao → thấp")
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SearchDeps {
    fun searchMentorsUseCase(): SearchMentorsUseCase
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchMentorScreen(
    mentors: List<HomeMentor> = SearchMockData.mentors,
    initialExpertise: String? = null, // ✅ NEW: Pre-filter by expertise from category
    onOpenProfile: (String) -> Unit = {},
    onBook: (String) -> Unit = {},
    onMessage: (String) -> Unit = {}, //  NEW: Callback to open chat with mentorId
    onBookSlot: (
        mentor: HomeMentor,
        occurrenceId: String,
        date: String,
        startTime: String,
        endTime: String,
        priceVnd: Long,
        note: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onOverlayOpened: () -> Unit = {},
    onOverlayClosed: () -> Unit = {}
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val deps = remember(context) { EntryPointAccessors.fromApplication(context, SearchDeps::class.java) }
        val searchUC = remember { deps.searchMentorsUseCase() }

        // ✅ Coroutine scope for load more
        val scope = rememberCoroutineScope()

        // Backing state for remote mentors
        var remoteMentors by remember { mutableStateOf<List<HomeMentor>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        // ✅ Pagination state
        var currentPage by remember { mutableIntStateOf(1) }
        var hasMore by remember { mutableStateOf(true) }
        var loadingMore by remember { mutableStateOf(false) }
        var isRefreshing by remember { mutableStateOf(false) }

        // ===== State (saveable) =====
        var query by rememberSaveable { mutableStateOf(initialExpertise ?: "") }
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

        // ===== Modal states =====
        var showDetail by rememberSaveable { mutableStateOf(false) }
        var showBooking by rememberSaveable { mutableStateOf(false) }
        var selectedMentor by remember { mutableStateOf<HomeMentor?>(null) }

        val blurOn = showDetail || showBooking
        val blurRadius = if (blurOn) 28.dp else 0.dp
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomBarPadding = 64.dp + 20.dp + bottomInset

        LaunchedEffect(blurOn) {
            if (blurOn) onOverlayOpened() else onOverlayClosed()
        }

        // ✅ Auto-refresh when screen is visible (sync latest data)
        LaunchedEffect(Unit) {
            // Initial load happens via filters LaunchedEffect
            // This just marks that screen is active
        }

        // ✅ Refresh when returning from detail/booking sheets
        LaunchedEffect(showDetail, showBooking) {
            if (!showDetail && !showBooking && remoteMentors.isNotEmpty()) {
                // Optional: Refresh data when closing sheets
                // Uncomment if needed:
                // delay(300)
                // refresh by re-triggering search
            }
        }

        // ✅ Trigger network search when filters change - RESET to page 1
        LaunchedEffect(query, selectedSkills, minRating, priceStart, priceEnd, sortName) {
            loading = true
            error = null
            currentPage = 1 // ✅ Reset to page 1
            remoteMentors = emptyList() // ✅ Clear old results
            hasMore = true

            val priceMin = (priceStart * 50_000).takeIf { it > 0 }
            val priceMax = (priceEnd * 50_000).takeIf { it > 0 }
            val res = searchUC(
                q = query.takeIf { it.isNotBlank() },
                skills = selectedSkills,
                minRating = minRating.takeIf { it > 0f },
                priceMin = priceMin,
                priceMax = priceMax,
                sort = sortName,
                page = 1,
                limit = 100 // ✅ Tăng từ 20 → 100 để hiển thị nhiều mentors hơn
            )
            when (res) {
                is com.mentorme.app.core.utils.AppResult.Success -> {
                    remoteMentors = res.data
                    hasMore = res.data.size >= 100 // ✅ Update check
                    loading = false
                    // ✅ Debug log
                    Log.d("SearchScreen", "✅ Loaded ${res.data.size} mentors (page=$currentPage, hasMore=$hasMore)")
                    Log.d("SearchScreen", "First mentor: ${res.data.firstOrNull()?.name}, Last: ${res.data.lastOrNull()?.name}")
                }
                is com.mentorme.app.core.utils.AppResult.Error -> {
                    error = res.throwable
                    loading = false
                    Log.e("SearchScreen", "❌ Load failed: ${res.throwable}")
                }
                com.mentorme.app.core.utils.AppResult.Loading -> {
                    loading = true
                    Log.d("SearchScreen", "⏳ Loading mentors...")
                }
            }
        }

        // ✅ Load more function
        suspend fun loadMore() {
            if (loadingMore || !hasMore) return

            loadingMore = true
            val nextPage = currentPage + 1
            val priceMin = (priceStart * 50_000).takeIf { it > 0 }
            val priceMax = (priceEnd * 50_000).takeIf { it > 0 }
            val res = searchUC(
                q = query.takeIf { it.isNotBlank() },
                skills = selectedSkills,
                minRating = minRating.takeIf { it > 0f },
                priceMin = priceMin,
                priceMax = priceMax,
                sort = sortName,
                page = nextPage,
                limit = 100 // ✅ Tăng từ 20 → 100
            )
            when (res) {
                is com.mentorme.app.core.utils.AppResult.Success -> {
                    remoteMentors = remoteMentors + res.data // ✅ Append new results
                    currentPage = nextPage
                    hasMore = res.data.size >= 100 // ✅ Update check
                    loadingMore = false
                }
                is com.mentorme.app.core.utils.AppResult.Error -> {
                    loadingMore = false
                }
                com.mentorme.app.core.utils.AppResult.Loading -> { }
            }
        }

        // ✅ Manual refresh function
        suspend fun refresh() {
            if (isRefreshing) return

            isRefreshing = true
            currentPage = 1
            hasMore = true

            val priceMin = (priceStart * 50_000).takeIf { it > 0 }
            val priceMax = (priceEnd * 50_000).takeIf { it > 0 }
            val res = searchUC(
                q = query.takeIf { it.isNotBlank() },
                skills = selectedSkills,
                minRating = minRating.takeIf { it > 0f },
                priceMin = priceMin,
                priceMax = priceMax,
                sort = sortName,
                page = 1,
                limit = 100
            )
            when (res) {
                is com.mentorme.app.core.utils.AppResult.Success -> {
                    remoteMentors = res.data
                    hasMore = res.data.size >= 100
                    isRefreshing = false
                }
                is com.mentorme.app.core.utils.AppResult.Error -> {
                    isRefreshing = false
                }
                com.mentorme.app.core.utils.AppResult.Loading -> { }
            }
        }

        // Use remote mentors if available, else fallback
        val listForUi = remoteMentors

        // Log to verify calendar ID flows through Ui layer
        LaunchedEffect(listForUi) {
            listForUi.forEach { m ->
                Log.d("Search", "ui mentor id=${m.id}")
            }
        }

        // Derive chips from current list
        val allSkills = remember(listForUi) { listForUi.flatMap { it.skills }.distinct().sorted() }

        // ✅ REMOVED: Client-side filtering duplicates backend logic
        // Backend already filters by minRating, priceMin, priceMax, skills
        // Only keep local name search for instant feedback
        val filtered = remember(query, listForUi) {
            if (query.isBlank()) {
                listForUi
            } else {
                listForUi.filter { m ->
                    m.name.contains(query, ignoreCase = true) ||
                    m.role.contains(query, ignoreCase = true) ||
                    m.skills.any { it.contains(query, ignoreCase = true) }
                }
            }
        }

        // ✅ REMOVED: Client-side sorting duplicates backend logic
        // Backend already sorts by hasAvailability + rating/price
        val result = filtered

        // ===== Layout with two layers =====
        Box(Modifier.fillMaxSize()) {
            // LAYER A: Search content (blur when modal shown)
            Box(
                Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
            ) {
                // Original Search content
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                    )
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = bottomBarPadding)
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
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
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
                                Column(
                                    Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Bộ lọc nhanh", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.width(8.dp))
                                        SectionCaption(
                                            if (selectedSkills.isEmpty()) "Chưa chọn"
                                            else "${selectedSkills.size} bộ lọc"
                                        )
                                        Spacer(Modifier.weight(1f))
                                        TextButton(
                                            onClick = {
                                                query = ""
                                                selectedSkills = emptyList()
                                                minRating = 0f
                                                priceStart = 0
                                                priceEnd = 20
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color(0xFFFFD700) // ✅ Gold/Yellow - nổi bật với background xanh
                                            )
                                        ) {
                                            Text(
                                                "Đặt lại",
                                                fontWeight = FontWeight.SemiBold // ✅ Thêm bold để nổi bật
                                            )
                                        }
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
                                    Text(if (loading) "Đang tải..." else error ?: "Không tìm thấy mentor phù hợp")
                                }
                            }
                        }
                    } else {
                        items(result, key = { it.id }) { m ->
                            MentorCard(
                                mentor = m,
                                onViewProfile = {
                                    Log.d("Search", "clicked mentor card id=${m.id}")
                                    selectedMentor = m
                                    showDetail = true
                                },
                                onBookSession = {
                                    Log.d("Search", "clicked mentor card id=${m.id}")
                                    selectedMentor = m
                                    showBooking = true
                                }
                            )
                        }

                        // ✅ Load More button/indicator
                        if (hasMore) {
                            item {
                                LiquidGlassCard(
                                    radius = 22.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !loadingMore) {
                                            scope.launch { loadMore() } // ✅ Use scope
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (loadingMore) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                "Tải thêm mentor...",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (result.isNotEmpty()) {
                            // ✅ End of list indicator
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Đã hiển thị tất cả ${result.size} mentor",
                                        color = Color.White.copy(0.6f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            // LAYER B: Glass overlay
            GlassOverlay(
                visible = blurOn,
                onDismiss = { showDetail = false; showBooking = false },
                formModifier = Modifier.fillMaxSize().padding(4.dp) // ✅ Giảm padding 12→4dp để sheet rộng hơn
            ) {
                selectedMentor?.let { mentor ->
                    when {
                        showDetail -> {
                            MentorDetailSheet(
                                mentorId = mentor.id,
                                mentor = mentor,
                                onClose = { showDetail = false },
                                onBookNow = { _ ->
                                    showDetail = false
                                    showBooking = true
                                },
                                onMessage = { mentorId ->
                                    //  Close sheet first
                                    showDetail = false
                                    //  Trigger parent callback
                                    onMessage(mentorId)
                                }
                            )
                        }
                        showBooking -> {
                            BookSessionContent(
                                mentor = mentor,
                                onClose = { showBooking = false },
                                onConfirm = { occurrenceId, date, startTime, endTime, priceVnd, note ->
                                    showBooking = false
                                    onBookSlot(
                                        mentor,
                                        occurrenceId,
                                        date,
                                        startTime,
                                        endTime,
                                        priceVnd,
                                        note
                                    )
                                }
                            )
                        }
                    }
                }
            }
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
private fun SectionCaption(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.7f))
}

private fun Modifier.noIndicationClickable(onClick: () -> Unit) = composed {
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
}

@Composable
private fun GlassFilterChip(
    text: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    AssistChip(
        onClick = onToggle,
        label = { Text(text, color = Color.White, maxLines = 1) },
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = Color.White.copy(0.35f),
            borderWidth = 1.dp
        ),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color.White.copy(0.12f) else Color.Transparent,
            labelColor = Color.White,
            leadingIconContentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
