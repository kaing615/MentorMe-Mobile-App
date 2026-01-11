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
import androidx.compose.material.icons.filled.AttachMoney // ✅ NEW: For "Giá & khoảng đệm" section
import androidx.compose.material.icons.filled.Description // ✅ NEW: For "Mô tả" section
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.core.*
import com.mentorme.app.ui.calendar.core.NewSlotInput
import com.mentorme.app.ui.components.ui.glassOutlinedTextFieldColors
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMButtonSize
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

private fun resolveEndDate(date: LocalDate, start: LocalTime, end: LocalTime): LocalDate =
    if (end.isBefore(start)) date.plusDays(1) else date

private fun durationMinutes(start: LocalTime, end: LocalTime): Int {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute
    val diff = endMinutes - startMinutes
    return if (diff < 0) diff + 24 * 60 else diff
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityTabSection(
    slots: List<AvailabilitySlot>,
    showAdd: Boolean,
    onShowAddChange: (Boolean) -> Unit,
    showEdit: Boolean,
    onShowEditChange: (Boolean) -> Unit,
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

    // ====== EDIT state ======
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
            val endZdt = end.atDate(resolveEndDate(date, start, end)).atZone(zone)
            val sErr = if (startZdt.isBefore(nowPlusSkew)) "Giờ bắt đầu phải trong tương lai" else null
            val eErr = if (endZdt.isBefore(nowPlusSkew)) "Giờ kết thúc phải trong tương lai" else null
            Pair(sErr, eErr)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    // Only show content when dialogs are closed
    if (!showAdd && !showEdit) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Header với gradient background đẹp
            LiquidGlassCard(radius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5).copy(alpha = 0.2f),
                                    Color(0xFF7C3AED).copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Thiết lập lịch trống",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Quản lý thời gian của bạn",
                                    color = Color.White.copy(0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        MMPrimaryButton(
                            onClick = {
                                resetForm()
                                onShowAddChange(true)
                            }
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tạo lịch", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }


        // Statistics Cards với design đẹp hơn
        if (totalCount > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Tổng slot",
                        value = totalCount.toString(),
                        icon = Icons.Default.CalendarToday,
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Còn trống",
                        value = openCount.toString(),
                        icon = Icons.Default.CalendarToday,
                        gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Đã đặt",
                        value = bookedCount.toString(),
                        icon = Icons.Default.CalendarToday,
                        gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tạm dừng",
                        value = pausedCount.toString(),
                        icon = Icons.Default.CalendarToday,
                        gradientColors = listOf(Color(0xFF6B7280), Color(0xFF4B5563)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Empty / List
        if (slots.isEmpty()) {
            LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0x3348A6FF), Color(0x337C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        "Chưa có lịch trống",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Hãy thêm lịch trống để mentee có thể đặt hẹn tư vấn cá nhân với bạn!",
                        color = Color.White.copy(.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                slots.forEach { slot ->
                    LiquidGlassCard(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            val sessionLabel = if (slot.sessionType == "video") "Gọi video" else "Trực tiếp"
                            val sessionIcon = if (slot.sessionType == "video") Icons.Default.Videocam else Icons.Default.LocationOn
                            val sessionGradient = if (slot.sessionType == "video")
                                listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                            else
                                listOf(Color(0xFF22C55E), Color(0xFF10B981))

                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(sessionGradient)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        sessionIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = slot.date,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = Color.White.copy(.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "${slot.startTime} - ${slot.endTime}",
                                            color = Color.White.copy(.8f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (slot.description != null) {
                                        Text(
                                            text = slot.description,
                                            color = Color.White.copy(.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                SlotStatusPill(slot)
                            }

                            // Info Chips với design mới
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ModernInfoChip(
                                        icon = Icons.Default.AccessTime,
                                        label = "Thời lượng",
                                        value = "${slot.duration} phút",
                                        modifier = Modifier.weight(1f)
                                    )
                                    ModernInfoChip(
                                        icon = Icons.Default.AttachMoney,
                                        label = "Giá",
                                        value = if (slot.priceVnd > 0) numberFormat.format(slot.priceVnd) else "Chưa có giá",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ModernInfoChip(
                                        icon = if (slot.sessionType == "video") Icons.Default.Videocam else Icons.Default.LocationOn,
                                        label = "Hình thức",
                                        value = if (slot.sessionType == "video") "Gọi video" else "Trực tiếp",
                                        modifier = Modifier.weight(1f)
                                    )
                                    ModernInfoChip(
                                        icon = Icons.Default.CalendarToday,
                                        label = "Trạng thái",
                                        value = if (slot.isBooked) "Đã đặt" else "Trống",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Action Buttons với design đẹp hơn
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ModernActionButton(
                                    text = "Sửa",
                                    icon = null,
                                    containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                    contentColor = Color(0xFF60A5FA),
                                    borderColor = Color(0xFF3B82F6).copy(alpha = 0.4f),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        // Prefill form from slot
                                        editingSlot = slot
                                        try {
                                            selectedDate = LocalDate.parse(slot.date)
                                            startTime = LocalTime.parse(slot.startTime)
                                            endTime = LocalTime.parse(slot.endTime)
                                        } catch (_: Exception) {
                                            // ignore
                                        }
                                        type = slot.sessionType
                                        desc = TextFieldValue(slot.description ?: "")
                                        priceDigits = if (slot.priceVnd > 0) slot.priceVnd.toString() else ""
                                        startErr = null
                                        endErr = null
                                        onShowEditChange(true)
                                    }
                                )
                                ModernActionButton(
                                    text = if (slot.isActive) "Tạm dừng" else "Kích hoạt",
                                    icon = null,
                                    containerColor = if (slot.isActive)
                                        Color(0xFFFFA500).copy(alpha = 0.2f)
                                    else
                                        Color(0xFF22C55E).copy(alpha = 0.2f),
                                    contentColor = if (slot.isActive) Color(0xFFFFA500) else Color(0xFF22C55E),
                                    borderColor = if (slot.isActive)
                                        Color(0xFFFFA500).copy(alpha = 0.4f)
                                    else
                                        Color(0xFF22C55E).copy(alpha = 0.4f),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onToggle(slot.backendSlotId) }
                                )
                                ModernActionButton(
                                    text = "Xóa",
                                    icon = null,
                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFEF4444),
                                    borderColor = Color(0xFFEF4444).copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(if (!slot.isBooked) Modifier else Modifier.alpha(0.5f)),
                                    onClick = {
                                        if (!slot.isBooked) {
                                            onDelete(slot.backendSlotId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    } // Close if (!showAdd && !showEdit)

    // ===== Dialog: THÊM =====
    if (showAdd) {
        // Delay content rendering to show blank screen immediately
        var showDialogContent by remember { mutableStateOf(false) }
        LaunchedEffect(showAdd) {
            showDialogContent = true
        }
        
        if (showDialogContent) {
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
            onDismiss = { onShowAddChange(false); startErr = null; endErr = null },
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
                
                val duration = durationMinutes(startTime!!, endTime!!)
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
                    onShowAddChange(false)
                }
            }
        )
        }
    }

    // ===== Dialog: SỬA =====
    if (showEdit && editingSlot != null) {
        // Delay content rendering to show blank screen immediately
        var showDialogContent by remember { mutableStateOf(false) }
        LaunchedEffect(showEdit) {
            showDialogContent = true
        }
        
        if (showDialogContent) {
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
            onDismiss = { onShowEditChange(false); editingSlot = null; resetForm(); startErr = null; endErr = null },
            priceDigits = priceDigits,
            onPriceChange = { priceDigits = it.filter(Char::isDigit).take(10) },
            onSubmit = {
                if (selectedDate == null) { Toast.makeText(context, "Vui lòng chọn ngày.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (startTime == null || endTime == null) { Toast.makeText(context, "Vui lòng chọn giờ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                
                val duration = durationMinutes(startTime!!, endTime!!)
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
                onShowEditChange(false)
                editingSlot = null
                resetForm()
            }
        )
        }
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
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.2f))
            .border(BorderStroke(1.5.dp, color.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ===== NEW COMPONENTS =====

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(radius = 18.dp, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.15f) }
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        title,
                        color = Color.White.copy(0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        value,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White.copy(0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    label,
                    color = Color.White.copy(0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp
                )
            }
            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 42.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
        durationMinutes(startTime, endTime)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Full screen with scrim (like GlassOverlay)
        Box(Modifier.fillMaxSize().zIndex(2f)) {
            // Scrim that covers entire screen
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
            )

            // Glass card wrapper (like GlassOverlay Surface)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp)
                    .align(Alignment.Center),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.White.copy(alpha = 0.14f),
                tonalElevation = 12.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
            ) {
                Box(Modifier.fillMaxSize().padding(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Header với icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                DialogSectionHeader(text = "Bước 1: Chọn thời gian", icon = Icons.Default.CalendarToday)

                // Ngày
                FormLabel("Ngày")
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                     OutlinedTextField(
                        value = datePreview,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(0.7f)) },
                        shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
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
                                placeholder = { Text("HH:mm", color = Color.White.copy(0.5f)) },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color.White.copy(0.7f)) },
                                isError = startError != null,
                                supportingText = { if (startError != null) Text(startError, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(16.dp),
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
                                placeholder = { Text("HH:mm", color = Color.White.copy(0.5f)) },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = Color.White.copy(0.7f)) },
                                isError = endError != null,
                                supportingText = { if (endError != null) Text(endError, color = MaterialTheme.colorScheme.error) },
                                shape = RoundedCornerShape(16.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showEndTimePicker = true })
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                DialogSectionHeader(text = "Bước 2: Giá & khoảng đệm", icon = Icons.Default.AttachMoney)

                if (priceDigits != null && onPriceChange != null) {
                    FormLabel("Giá (VND)")
                    OutlinedTextField(
                        value = priceDigits,
                        onValueChange = onPriceChange,
                        placeholder = { Text("Ví dụ: 250000", color = Color.White.copy(0.5f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = glassOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Buffer fields (optional)
                if (bufBeforeDigits != null && bufAfterDigits != null && onBufBeforeChange != null && onBufAfterChange != null) {
                    FormLabel("Khoảng đệm")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Đệm trước (phút)", color = Color.White.copy(0.9f), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bufBeforeDigits,
                                onValueChange = onBufBeforeChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Thời gian đệm trước", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall) }
                        )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Đệm sau (phút)", color = Color.White.copy(0.9f), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bufAfterDigits,
                                onValueChange = onBufAfterChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                colors = glassOutlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Thời gian đệm sau", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall) }
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

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                DialogSectionHeader(text = "Bước 3: Mô tả", icon = Icons.Default.Description)

                // Mô tả
                FormLabel("Mô tả (tùy chọn)")
                OutlinedTextField(
                    value = desc, onValueChange = onDescChange,
                    placeholder = { Text("Ví dụ: React Performance, Career Guidance…", color = Color.White.copy(0.5f)) },
                    shape = RoundedCornerShape(16.dp), minLines = 3,
                    colors = glassOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Actions - stacked like mentor detail sheet
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        )
                    ) { 
                        Text(primaryText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) 
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) { 
                        Text("Hủy", style = MaterialTheme.typography.titleSmall) 
                    }
                }
                }
                }
            }
        }
    }
}

@Composable
private fun DialogSectionHeader(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null // ✅ NEW: Optional icon
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // ✅ NEW: Show icon if provided
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color.White.copy(0.9f),
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
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
    LiquidGlassCard(radius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4F46E5).copy(alpha = 0.15f),
                            Color(0xFF7C3AED).copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "Xem trước lịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreviewRow(icon = Icons.Default.CalendarToday, text = dateLabel)
                    PreviewRow(icon = Icons.Default.AccessTime, text = timeLabel)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnhancedPreviewPill(typeLabel, Color(0xFF4F46E5))
                    EnhancedPreviewPill(durationLabel, Color(0xFF10B981))
                    EnhancedPreviewPill(priceLabel, Color(0xFFF59E0B))
                }
                
                Text(
                    "Tối thiểu 30 phút. Bạn có thể chỉnh sửa sau khi tạo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PreviewRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(0.7f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EnhancedPreviewPill(text: String, accentColor: Color) {
    Surface(
        color = accentColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
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
