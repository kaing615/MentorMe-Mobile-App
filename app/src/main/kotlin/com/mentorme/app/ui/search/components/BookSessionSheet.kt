package com.mentorme.app.ui.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.common.glassButtonColors
import com.mentorme.app.ui.common.glassOutlinedTextFieldColors
import com.mentorme.app.ui.home.Mentor as HomeMentor

@Composable
fun BookSessionContent(
    mentor: HomeMentor,
    onClose: () -> Unit,
    onConfirm: (date: String, time: String, durationMins: Int, note: String) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(60) }
    var note by remember { mutableStateOf("") }

    // Use VND pricing from mentor
    val pricePerHourVnd = mentor.hourlyRate.toDouble()
    val subtotal = pricePerHourVnd * duration / 60.0
    val tax = subtotal * 0.10
    val fee = 20_000.0
    val total = subtotal + tax + fee

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "🪄 Đặt lịch tư vấn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            Spacer(Modifier.height(8.dp))
            Text("Với ${mentor.name}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Chọn ngày") },
                singleLine = true,
                colors = glassOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Chọn giờ") },
                singleLine = true,
                colors = glassOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { v -> duration = v.toIntOrNull() ?: 60 },
                label = { Text("Thời lượng (phút)") },
                singleLine = true,
                colors = glassOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú cho mentor (tùy chọn)") },
                colors = glassOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("🧾 Chi tiết thanh toán", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${duration} phút × ${pricePerHourVnd.toLong()}₫/giờ")
                        Text("${subtotal.toLong()}₫")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Thuế (10%)")
                        Text("${tax.toLong()}₫")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phí dịch vụ")
                        Text("${fee.toLong()}₫")
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0x55FFFFFF))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", fontWeight = FontWeight.Bold)
                        Text("${total.toLong()}₫", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(0.35f))
                ) { Text("Hủy") }
                Button(
                    onClick = { onConfirm(date, time, duration, note) },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 46.dp),
                    colors = glassButtonColors()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Xác nhận")
                }
            }
        }
    }
}
