package com.mentorme.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import androidx.compose.animation.core.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSection(
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val skills = listOf("Lập trình", "Marketing", "Thiết kế", "Kinh doanh", "Tài chính", "Data Science")

    // Gradient chữ vàng
    val goldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFB347), Color(0xFFFF8C00))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ===== Hero glass card =====
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
                                brush = goldGradient,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) { append("phù hợp") }
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

                // Search
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    radius = 16.dp
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Tìm kiếm mentor theo kỹ năng, lĩnh vực...", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Popular skills
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    skills.forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text(s, color = Color.White, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }

        // ===== Slider full-width + overlay badges (floating) =====
        MentorWideSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

/* ---------------- Floating bubble (up-down) ---------------- */
@Composable
private fun FloatingBubble(
    modifier: Modifier = Modifier,
    amplitude: Dp = 10.dp,
    duration: Int = 2200,
    phaseDelay: Int = 0,
    content: @Composable BoxScope.() -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "bubble")
    val offsetY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = amplitude.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing, delayMillis = phaseDelay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleY"
    )
    Box(modifier = modifier.offset(y = offsetY.dp), content = content)
}

/* ---------------- Slider (auto + swipe), full width item ---------------- */

private data class Banner(
    val title: String,
    val subtitle: String,
    val bg: Brush
)

@Composable
private fun MentorWideSlider(modifier: Modifier = Modifier) {
    val banners = remember {
        // Pastel (nhạt) hơn
        listOf(
            Banner(
                "Tư vấn cá nhân",
                "Nhận lời khuyên phù hợp với mục tiêu",
                Brush.linearGradient(listOf(Color(0xFFBFD7FF), Color(0xFFC9B8FF), Color(0xFFBDEBFF)))
            ),
            Banner(
                "Lộ trình nâng cấp",
                "Mentor đồng hành 1-1 theo tuần",
                Brush.linearGradient(listOf(Color(0xFFB7C4FF), Color(0xFFD2BBFF)))
            ),
            Banner(
                "Phỏng vấn giả lập",
                "Practice trước khi apply công việc",
                Brush.linearGradient(listOf(Color(0xFFFFD1B2), Color(0xFFFFB8B4)))
            )
        )
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // Parent (HomeScreen) đã padding 16dp mỗi bên → item = width - 32dp
    val itemWidth = screenWidth - 32.dp

    // Auto scroll
    LaunchedEffect(banners.size) {
        while (true) {
            delay(3500)
            val next = (listState.firstVisibleItemIndex + 1) % banners.size
            scope.launch { listState.animateScrollToItem(next) }
        }
    }

    Box(modifier = modifier) {
        val fling: FlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        LazyRow(
            state = listState,
            flingBehavior = fling,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) {
            itemsIndexed(banners) { _, b ->
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        // Nền pastel + nội dung
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.1f)) // Sử dụng background đơn giản thay vì brush.copy()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.CenterStart),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(b.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(b.subtitle, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // --- Overlay badges (FloatingBubble) ---
        // Online (góc trái trên) – bồng bềnh
        FloatingBubble(amplitude = 10.dp, duration = 2400, phaseDelay = 0,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 8.dp)
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
        }

        // Rating (góc phải dưới) – bồng bềnh & đè lên card
        FloatingBubble(amplitude = 12.dp, duration = 2600, phaseDelay = 400,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .liquidGlass(radius = 16.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("4.9⭐", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Đánh giá", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}
