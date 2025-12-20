package com.mentorme.app.ui.calendar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun SegmentedTabs(
    activeIndex: Int,
    labels: List<String>,
    onChange: (Int) -> Unit
) {
    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = activeIndex,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { positions ->
                if (positions.isNotEmpty()) {
                    val pos = positions[activeIndex]
                    Box(
                        Modifier
                            .tabIndicatorOffset(pos)
                            .fillMaxHeight()
                            .padding(6.dp)
                            .background(Color.White.copy(.12f), RoundedCornerShape(16.dp))
                            .border(
                                BorderStroke(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF60A5FA),
                                            Color(0xFFA78BFA),
                                            Color(0xFFF472B6)
                                        )
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                    )
                }
            }
        ) {
            labels.forEachIndexed { i, label ->
                Tab(
                    selected = i == activeIndex,
                    onClick = { onChange(i) },
                    text = {
                        Text(
                            text = label,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (i == activeIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}
