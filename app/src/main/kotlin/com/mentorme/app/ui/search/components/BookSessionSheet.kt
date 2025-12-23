package com.mentorme.app.ui.search.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.mentorme.app.domain.usecase.availability.GetPublicCalendarUseCase
import com.mentorme.app.data.dto.availability.slotPriceVndOrNull
import com.mentorme.app.ui.common.glassButtonColors
import com.mentorme.app.ui.common.glassOutlinedTextFieldColors
import com.mentorme.app.ui.home.Mentor as HomeMentor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class SlotChoice(
    val occurrenceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMins: Int,
    val priceVnd: Long
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BookSessionDeps {
    fun getPublicCalendarUseCase(): GetPublicCalendarUseCase
}

@Composable
fun BookSessionContent(
    mentor: HomeMentor,
    onClose: () -> Unit,
    onConfirm: (
        occurrenceId: String,
        date: String,
        startTime: String,
        endTime: String,
        priceVnd: Long,
        note: String
    ) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deps = remember(context) { EntryPointAccessors.fromApplication(context, BookSessionDeps::class.java) }
    val getCalendar = remember { deps.getPublicCalendarUseCase() }

    var slots by remember { mutableStateOf<List<SlotChoice>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedSlot by remember { mutableStateOf<SlotChoice?>(null) }
    var note by remember { mutableStateOf("") }

    val nf = remember { java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }

    LaunchedEffect(mentor.id) {
        loading = true
        val zone = ZoneId.systemDefault()
        val todayStartLocal = java.time.LocalDate.now(zone).atStartOfDay(zone)
        val fromIsoUtc = todayStartLocal.toInstant().toString()
        val toIsoUtc = todayStartLocal.plusDays(30).withHour(23).withMinute(59).withSecond(59).toInstant().toString()

        when (val res = getCalendar(mentor.id, fromIsoUtc, toIsoUtc, includeClosed = true)) {
            is com.mentorme.app.core.utils.AppResult.Success -> {
                val items = res.data
                slots = items.mapNotNull { item ->
                    val s = item.start ?: return@mapNotNull null
                    val e = item.end ?: return@mapNotNull null
                    if (item.status?.lowercase() != "open") return@mapNotNull null
                    val startZ = runCatching { Instant.parse(s).atZone(zone) }.getOrNull() ?: return@mapNotNull null
                    val endZ = runCatching { Instant.parse(e).atZone(zone) }.getOrNull() ?: return@mapNotNull null
                    val duration = java.time.Duration.between(startZ, endZ).toMinutes().toInt()
                    val occurrenceId = item.id?.ifBlank { null } ?: "${s}_${e}"
                    SlotChoice(
                        occurrenceId = occurrenceId,
                        date = dateFmt.format(startZ),
                        startTime = timeFmt.format(startZ),
                        endTime = timeFmt.format(endZ),
                        durationMins = duration,
                        priceVnd = item.slotPriceVndOrNull()?.toLong() ?: 0L
                    )
                }
                loading = false
            }
            is com.mentorme.app.core.utils.AppResult.Error -> {
                slots = emptyList()
                loading = false
            }
            com.mentorme.app.core.utils.AppResult.Loading -> Unit
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Đặt lịch tư vấn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }

            Spacer(Modifier.height(8.dp))
            Text("Với ${mentor.name}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            Text("Chọn khung giờ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            when {
                loading -> Text("Đang tải lịch trống...", color = Color.White.copy(alpha = 0.8f))
                slots.isEmpty() -> Text("Chưa có lịch trống trong 30 ngày tới", color = Color.White.copy(alpha = 0.8f))
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        slots.forEach { slot ->
                            Surface(
                                color = if (selectedSlot?.occurrenceId == slot.occurrenceId) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSlot = slot }
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("${slot.date} • ${slot.startTime} - ${slot.endTime}")
                                        Text(nf.format(slot.priceVnd), color = Color.White.copy(alpha = 0.7f))
                                    }
                                    if (selectedSlot?.occurrenceId == slot.occurrenceId) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú cho mentor (tùy chọn)") },
                colors = glassOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(0.35f))
                ) { Text("Hủy") }
                Button(
                    onClick = {
                        val slot = selectedSlot ?: return@Button
                        onConfirm(
                            slot.occurrenceId,
                            slot.date,
                            slot.startTime,
                            slot.endTime,
                            slot.priceVnd,
                            note
                        )
                    },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 46.dp),
                    enabled = selectedSlot != null,
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
