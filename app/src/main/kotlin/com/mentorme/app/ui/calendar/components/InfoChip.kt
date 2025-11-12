package com.mentorme.app.ui.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun InfoChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    center: Boolean = false
) {
    LiquidGlassCard(radius = 16.dp, modifier = modifier.height(68.dp)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                title,
                color = Color.White.copy(.85f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = if (center) TextAlign.Center else TextAlign.Start
            )
            Text(
                value,
                color = Color.White
            )
        }
    }
}
