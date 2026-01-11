package com.mentorme.app.ui.components.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.theme.LiquidGlassCard

@Composable
fun OngoingSessionDialog(
    title: String,
    description: String,
    timeLabel: String,
    onJoin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = modifier.fillMaxWidth(),
            radius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = Color(0xFF22C55E)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (timeLabel.isNotBlank()) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MMGhostButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Để sau")
                    }
                    MMButton(
                        text = "Vào phiên tư vấn",
                        onClick = onJoin,
                        modifier = Modifier.weight(1f),
                        useGlass = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            contentColor = Color.White
                        )
                    )
                }
            }
        }
    }
}
