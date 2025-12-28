package com.mentorme.app.ui.components.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun GlassTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.10f), shape = outerShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)), shape = outerShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(
                        color = if (selected) Color.White.copy(alpha = 0.28f) else Color.Transparent,
                        shape = shape
                    )
                    .then(
                        if (selected) Modifier.border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)), shape
                        ) else Modifier
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GlassSegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(18.dp)

    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .background(Color.White.copy(alpha = 0.10f), shape = containerShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)), shape = containerShape)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val itemShape = RoundedCornerShape(14.dp)
            val targetBg = if (selected) Color.White.copy(alpha = 0.24f) else Color.Transparent
            val targetBorder = if (selected) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.14f)
            val bg by animateColorAsState(targetValue = targetBg, label = "tabBg")
            val borderColor by animateColorAsState(targetValue = targetBorder, label = "tabBorder")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                    .background(bg, shape = itemShape)
                    .border(BorderStroke(1.dp, borderColor), shape = itemShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
