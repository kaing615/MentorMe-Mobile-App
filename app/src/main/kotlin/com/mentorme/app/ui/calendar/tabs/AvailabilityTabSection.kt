package com.mentorme.app.ui.calendar.tabs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.core.*
import com.mentorme.app.ui.calendar.core.NewSlotInput
import com.mentorme.app.ui.common.glassOutlinedTextFieldColors
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMButtonSize
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityTabSection(
    slots: List<AvailabilitySlot>,
    onAdd: (NewSlotInput) -> Boolean,
    onUpdate: (AvailabilitySlot) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("vi","VN")) }
    val totalCount = slots.size
    val activeCount = slots.count { it.isActive }
    val bookedCount = slots.count { it.isBooked }
    val openCount = slots.count { it.isActive && !it.isBooked }
    val pausedCount = totalCount - activeCount

    // Persist last used buffer minutes across opens
    var lastBufBefore by rememberSaveable { mutableStateOf("0") }
    var lastBufAfter by rememberSaveable { mutableStateOf("0") }

    // Form state
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var type by remember { mutableStateOf("video") }
    var desc by remember { mutableStateOf(TextFieldValue("")) }
    var priceDigits by remember { mutableStateOf("") }
    var startErr by remember { mutableStateOf<String?>(null) }
    var endErr by remember { mutableStateOf<String?>(null) }

    // Dialog flags
    var showAdd by remember { mutableStateOf(false) }

    // ====== EDIT state ======
    var showEdit by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<AvailabilitySlot?>(null) }

    fun resetForm() {
        selectedDate = null; startTime = null; endTime = null; type = "video"; desc = TextFieldValue(""); priceDigits = ""
        startErr = null; endErr = null
    }

    // Helper: check future (+30s) errors
    fun futureErrors(date: LocalDate, start: LocalTime, end: LocalTime): Pair<String?, String?> {
        return try {
            val zone = ZoneId.systemDefault()
            val nowPlusSkew = java.time.ZonedDateTime.now(zone).plusSeconds(30)
            val startZdt = start.atDate(date).atZone(zone)
            val endZdt = end.atDate(date).atZone(zone)
            val sErr = if (startZdt.isBefore(nowPlusSkew)) "Giờ bắt đầu phải trong tương lai" else null
            val eErr = if (endZdt.isBefore(nowPlusSkew)) "Giờ kết thúc phải trong tương lai" else null
            Pair(sErr, eErr)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3348A6FF)).padding(6.dp),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.CalendarToday, null, tint = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Thiết lập lịch trống",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.weight(1f))
            MMPrimaryButton(onClick = {
                resetForm()
                showAdd = true
            }) {
                Icon(Icons.Default.Add, null, tint = Color.White)
                Spacer(Modifier.width(6.dp)); Text("Tạo lịch", color = Color.White)
            }
        }

        if (totalCount > 0) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoChip("Tổng slot", totalCount.toString(), Modifier.weight(1f), center = true)
                InfoChip("Còn trống", openCount.toString(), Modifier.weight(1f), center = true)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoChip("Đã đặt", bookedCount.toString(), Modifier.weight(1f), center = true)
                InfoChip("Tạm dừng", pausedCount.toString(), Modifier.weight(1f), center = true)
            }
        }

        // Empty / List
        if (slots.isEmpty()) {
            LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                            .background(Color(0x3348A6FF)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(.7f)) }
                    Spacer(Modifier.height(8.dp))
                    Text("Chưa có lịch trống", color = Color.White)
                    Text(
                        "Hãy thêm lịch trống để mentee có thể đặt hẹn tư vấn cá nhân với bạn!",
                        color = Color.White.copy(.7f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                slots.forEach { slot ->
                    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            val sessionLabel = if (slot.sessionType == "video") "Gọi video" else "Trực tiếp"
                            val sessionIcon = if (slot.sessionType == "video") Icons.Default.Videocam else Icons.Default.LocationOn

                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (slot.sessionType == "video") Color(0x332467F1) else Color(0x3322C55E)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(sessionIcon, null, tint = Color.White, modifier = Modifier.size(16.dp)) }

                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = slot.date,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        "${slot.startTime} - ${slot.endTime}",
                                        color = Color.White.copy(.8f)
                                    )
                                    Text(
                                        text = slot.description ?: "Buổi $sessionLabel",
                                        color = Color.White.copy(.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                SlotStatusPill(slot)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip("Thời lượng", "${slot.duration} phút", Modifier.weight(1f))
                                    InfoChip(
                                        "Giá tư vấn",
                                        if (slot.priceVnd > 0) numberFormat.format(slot.priceVnd) else "Chưa có giá",
                                        Modifier.weight(1f)
                                    )
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip(
                                        "Hình thức",
                                        if (slot.sessionType=="video") "Gọi video" else "Trực tiếp",
                                        Modifier.weight(1f)
                                    )
                                    InfoChip(
                                        "Trạng thái",
                                        if (slot.isBooked) "Đã đặt" else "Trống",
                                        Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MMButton(
                                    text = "Sửa",
                                    size = MMButtonSize.Compact,
                                    onClick = {
                                        // Prefill form from slot
                                        editingSlot = slot
                                        try {
                                            selectedDate = LocalDate.parse(slot.date)
                                            startTime = LocalTime.parse(slot.startTime)
                                            endTime = LocalTime.parse(slot.endTime)
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                        type = slot.sessionType
                                        desc = TextFieldValue(slot.description ?: "")
                                        priceDigits = if (slot.priceVnd > 0) slot.priceVnd.toString() else ""
                                        startErr = null; endErr = null
                                        showEdit = true
                                    }
                                )
                                MMButton(
                                    text = if (slot.isActive) "Tạm dừng" else "Kích hoạt",
                                    onClick = { onToggle(slot.backendSlotId) },
                                    size = MMButtonSize.Compact
                                )
                                MMButton(
                                    text = "Xóa",
                                    onClick = {
                                        if (!slot.isBooked) {
                                            onDelete(slot.backendSlotId)
                                        }
                                    },
                                    size = MMButtonSize.Compact,
                                    modifier = if (!slot.isBooked) Modifier else Modifier.alpha(0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ===== Dialog: THÊM =====
    if (showAdd) {
        // Buffer minutes state (digits only, clamp 0..120), seeded with last used
        var bufBeforeDigits by rememberSaveable(showAdd) { mutableStateOf(lastBufBefore) }
        var bufAfterDigits  by rememberSaveable(showAdd) { mutableStateOf(lastBufAfter) }

        AvailabilityDialog(
            title = "Thêm lịch trống mới",
            primaryText = "Thêm lịch",
            selectedDate = selectedDate,
            startTime = startTime,
            endTime = endTime,
            startError = startErr,
            endError = endErr,
            type = type,
            desc = desc,
            onDateSelected = { selectedDate = it },
            onStartTimeSelected = { startTime = it; startErr = null },
            onEndTimeSelected = { endTime = it; endErr = null },
            onTypeChange = { type = it },
            onDescChange = { desc = it },
            onDismiss = { showAdd = false; startErr = null; endErr = null },
            priceDigits = priceDigits,
            onPriceChange = { priceDigits = it.filter(Char::isDigit).take(10) },
            // Inject buffer fields into dialog content
            bufBeforeDigits = bufBeforeDigits,
            bufAfterDigits = bufAfterDigits,
            onBufBeforeChange = { bufBeforeDigits = it.filter(Char::isDigit).take(3) },
            onBufAfterChange  = { bufAfterDigits  = it.filter(Char::isDigit).take(3) },
            onSubmit = {
                if (selectedDate == null) { Toast.makeText(context, "Vui lòng chọn ngày.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (startTime == null || endTime == null) { Toast.makeText(context, "Vui lòng chọn giờ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                
                val duration = java.time.Duration.between(startTime, endTime).toMinutes()
                if (duration < 30) { Toast.makeText(context, "Tối thiểu 30 phút.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                
                // Future-only validation (+30s)
                val (sErr, eErr) = futureErrors(selectedDate!!, startTime!!, endTime!!)
                startErr = sErr; endErr = eErr
                if (sErr != null || eErr != null) {
                    Toast.makeText(context, "Vui lòng chọn thời gian ở tương lai.", Toast.LENGTH_SHORT).show()
                    return@AvailabilityDialog
                }

                fun clamp(v: String): Int = v.filter(Char::isDigit).take(3).toIntOrNull()?.coerceIn(0,120) ?: 0
                val bufBefore = clamp(bufBeforeDigits)
                val bufAfter  = clamp(bufAfterDigits)
                val priceVnd = priceDigits.filter(Char::isDigit).toLongOrNull()

                val newSlot = NewSlotInput(
                    date = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    startTime = startTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = endTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                    duration = duration.toInt(),
                    priceVnd = priceVnd,
                    description = desc.text.ifBlank { null },
                    sessionType = type,
                    bufferBeforeMin = bufBefore,
                    bufferAfterMin = bufAfter
                )
                val added = onAdd(newSlot)
                if (added) {
                    // Persist last used buffer values
                    lastBufBefore = bufBefore.toString()
                    lastBufAfter  = bufAfter.toString()
                    // Close dialog; parent shows toast on result
                    resetForm()
                    showAdd = false
                }
            }
        )
    }

    // ===== Dialog: SỬA =====
    if (showEdit && editingSlot != null) {
        AvailabilityDialog(
            title = "Chỉnh sửa lịch trống",
            primaryText = "Cập nhật",
            selectedDate = selectedDate,
            startTime = startTime,
            endTime = endTime,
            startError = startErr,
            endError = endErr,
            type = type,
            desc = desc,
            onDateSelected = { selectedDate = it },
            onStartTimeSelected = { startTime = it; startErr = null },
            onEndTimeSelected = { endTime = it; endErr = null },
            onTypeChange = { type = it },
            onDescChange = { desc = it },
            onDismiss = { showEdit = false; editingSlot = null; resetForm(); startErr = null; endErr = null },
            priceDigits = priceDigits,
            onPriceChange = { priceDigits = it.filter(Char::isDigit).take(10) },
            onSubmit = {
                if (selectedDate == null) { Toast.makeText(context, "Vui lòng chọn ngày.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (startTime == null || endTime == null) { Toast.makeText(context, "Vui lòng chọn giờ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                
                val duration = java.time.Duration.between(startTime, endTime).toMinutes()
                if (duration < 30) { Toast.makeText(context, "Tối thiểu 30 phút.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                
                // Future-only validation (+30s)
                val (sErr, eErr) = futureErrors(selectedDate!!, startTime!!, endTime!!)
                startErr = sErr; endErr = eErr
                if (sErr != null || eErr != null) {
                    Toast.makeText(context, "Vui lòng chọn thời gian ở tương lai.", Toast.LENGTH_SHORT).show()
                    return@AvailabilityDialog
                }

                val parsedPrice = priceDigits.filter(Char::isDigit).toLongOrNull()
                val base = editingSlot!!
                val updated = base.copy(
                    date = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    startTime = startTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = endTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                    duration = duration.toInt(),
                    priceVnd = parsedPrice ?: base.priceVnd,
                    description = desc.text.ifBlank { null },
                    sessionType = type,
                    // preserve backend ids for mutation
                    backendSlotId = base.backendSlotId,
                    backendOccurrenceId = base.backendOccurrenceId
                    // giữ nguyên id, isActive, isBooked
                )
                onUpdate(updated)
                // toast is handled by parent after API result
                showEdit = false
                editingSlot = null
                resetForm()
            }
        )
    }
}

@Composable
private fun SlotStatusPill(slot: AvailabilitySlot) {
    val (label, color) = when {
        !slot.isActive -> "Tạm dừng" to Color(0xFF6B7280)
        slot.isBooked -> "Đã đặt" to Color(0xFFEF4444)
        else -> "Còn trống" to Color(0xFF22C55E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.18f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.45f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * Dialog dùng chung cho Thêm/Sửa để tránh lặp code
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilityDialog(
    title: String,
    primaryText: String,
    selectedDate: LocalDate?,
    startTime: LocalTime?,
    endTime: LocalTime?,
    startError: String?,
    endError: String?,
    type: String,
    desc: TextFieldValue,
    onDateSelected: (LocalDate) -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onTypeChange: (String) -> Unit,
    onDescChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    // Optional price field
    priceDigits: String? = null,
    onPriceChange: ((String) -> Unit)? = null,
    // Optional buffer fields: if provided, dialog renders buffer controls
    bufBeforeDigits: String? = null,
    bufAfterDigits: String? = null,
    onBufBeforeChange: ((String) -> Unit)? = null,
    onBufAfterChange: ((String) -> Unit)? = null,
    onSubmit: () -> Unit
) {
    var typeMenu by remember { mutableStateOf(false) }
    val numberFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")) }
    
    val durationPreview = if (startTime != null && endTime != null) {
        java.time.Duration.between(startTime, endTime).toMinutes()
    } else null
    
    val pricePreview = priceDigits?.filter(Char::isDigit)?.toLongOrNull()
    val datePreview = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/MM/yyyy"
    val timePreview = if (startTime != null && endTime != null) {
        "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    } else "--:-- - --:--"
    
    val typeLabel = if (type == "video") "Gọi video" else "Trực tiếp"

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    // Time Picker State
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val startTimePickerState = rememberTimePickerState(
        initialHour = startTime?.hour ?: 9,
        initialMinute = startTime?.minute ?: 0,
        is24Hour = true
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = endTime?.hour ?: 10,
        initialMinute = endTime?.minute ?: 0,
        is24Hour = true
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onStartTimeSelected(LocalTime.of(startTimePickerState.hour, startTimePickerState.minute))
                    showStartTimePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Hủy") }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onEndTimeSelected(LocalTime.of(endTimePickerState.hour, endTimePickerState.minute))
                    showEndTimePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Hủy") }
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {}, dismissButton = {}, title = null,
        containerColor = Color(0xF2334155),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                DialogSectionHeader("Bước 1: Chọn thời gian")

                // Ngày
                FormLabel("Ngày")
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                     OutlinedTextField(
                        value = datePreview,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.White) },
                        shape = RoundedCornerShape(14.dp),
                        colors = glassOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                     )
                     Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }

                // Loại phiên
                FormLabel("Loại phiên")
                ExposedDropdownMenuBox(expanded = typeMenu, onExpandedChange = { typeMenu = it }) {
                    OutlinedTextField(
                        value = typeLabel,
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = typeMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassOutlinedTextFieldColors()
                    )
                    DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        DropdownMenuItem(text = { Text("Gọi video") }, onClick = { onTypeChange("video"); typeMenu = false })
                        DropdownMenuItem(text = { Text("Trực tiếp") }, onClick = { onTypeChange("in-person"); typeMenu = false })
                    }
                }

                // Giờ
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        FormLabel("Giờ bắt đầu")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                onValueChange = {},
                                placeholder = { Text("HH:mm") },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color.White) },
                                isError = startError != null,
                                supportingText = { if (startError != null) Text(startError, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(14.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showStartTimePicker = true })
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        FormLabel("Giờ kết thúc")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                onValueChange = {},
                                placeholder = { Text("HH:mm") },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color.White) },
                                isError = endError != null,
                                supportingText = { if (endError != null) Text(endError, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(14.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showEndTimePicker = true })
                        }
                    }
                }

                DialogSectionHeader("Bước 2: Giá & khoảng đệm")

                if (priceDigits != null && onPriceChange != null) {
                    FormLabel("Giá (VND)")
                    OutlinedTextField(
                        value = priceDigits,
                        onValueChange = onPriceChange,
                        placeholder = { Text("Ví dụ: 250000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Buffer fields (optional)
                if (bufBeforeDigits != null && bufAfterDigits != null && onBufBeforeChange != null && onBufAfterChange != null) {
                    FormLabel("Khoảng đệm")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Đệm trước (phút)", color = Color.White)
                            OutlinedTextField(
                                value = bufBeforeDigits,
                                onValueChange = onBufBeforeChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Khoảng đệm trước tính bằng phút", color = Color.White.copy(alpha = 0.7f)) }
                        )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Đệm sau (phút)", color = Color.White)
                            OutlinedTextField(
                                value = bufAfterDigits,
                                onValueChange = onBufAfterChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Khoảng đệm sau tính bằng phút", color = Color.White.copy(alpha = 0.7f)) }
                        )
                        }
                    }
                }

                AvailabilityPreviewCard(
                    dateLabel = datePreview,
                    timeLabel = timePreview,
                    durationLabel = durationPreview?.let { "${it} phút" } ?: "—",
                    priceLabel = pricePreview?.let { numberFormat.format(it) } ?: "Chưa có giá",
                    typeLabel = typeLabel
                )

                DialogSectionHeader("Bước 3: Mô tả")

                // Mô tả
                FormLabel("Mô tả (tùy chọn)")
                OutlinedTextField(
                    value = desc, onValueChange = onDescChange,
                    placeholder = { Text("Ví dụ: React Performance, Career Guidance…") },
                    shape = RoundedCornerShape(14.dp), minLines = 3,
                    colors = glassOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) { Text("Hủy") }

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        )
                    ) { Text(primaryText) }
                }
            }
        }
    )
}

@Composable
private fun DialogSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AvailabilityPreviewCard(
    dateLabel: String,
    timeLabel: String,
    durationLabel: String,
    priceLabel: String,
    typeLabel: String
) {
    LiquidGlassCard(radius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Xem trước lịch",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                "$dateLabel • $timeLabel",
                color = Color.White.copy(alpha = 0.85f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewPill(typeLabel)
                PreviewPill(durationLabel)
                PreviewPill(priceLabel)
            }
            Text(
                "Tối thiểu 30 phút. Bạn có thể chỉnh sửa sau khi tạo.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun PreviewPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Chọn giờ",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(shape = MaterialTheme.shapes.extraLarge, color = containerColor),
            color = containerColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
