package com.mentorme.app.ui.calendar.tabs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.calendar.components.InfoChip
import com.mentorme.app.ui.calendar.core.*
import com.mentorme.app.ui.calendar.core.NewSlotInput
import com.mentorme.app.ui.components.ui.MMButton
import com.mentorme.app.ui.components.ui.MMButtonSize
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityTabSection(
    slots: List<AvailabilitySlot>,
    onAdd: (NewSlotInput) -> Unit,
    onUpdate: (AvailabilitySlot) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("vi","VN")) }
    val HOURLY = 100_000

    // Persist last used buffer minutes across opens
    var lastBufBefore by rememberSaveable { mutableStateOf("0") }
    var lastBufAfter by rememberSaveable { mutableStateOf("0") }

    // Form state dùng chung cho Add/Edit (dạng digits để caret ổn định)
    var dateDigits by remember { mutableStateOf("") }
    var startDigits by remember { mutableStateOf("") }
    var endDigits by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("video") }
    var desc by remember { mutableStateOf(TextFieldValue("")) }
    var startErr by remember { mutableStateOf<String?>(null) }
    var endErr by remember { mutableStateOf<String?>(null) }

    // Dialog flags
    var showAdd by remember { mutableStateOf(false) }

    // ====== EDIT state ======
    var showEdit by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<AvailabilitySlot?>(null) }

    // Helpers chuyển đổi giữa định dạng lưu & digits hiển thị
    fun isoToDigits(iso: String): String { // "yyyy-MM-dd" -> "ddMMyyyy"
        val y = iso.substring(0,4); val m = iso.substring(5,7); val d = iso.substring(8,10)
        return d + m + y
    }
    fun hhmmToDigits(hhmm: String) = hhmm.replace(":", "") // "09:30" -> "0930"

    fun resetForm() {
        dateDigits = ""; startDigits = ""; endDigits = ""; type = "video"; desc = TextFieldValue("")
        startErr = null; endErr = null
    }

    // Helper: check future (+30s) errors
    fun futureErrors(dateIso: String, startHHMM: String, endHHMM: String): Pair<String?, String?> {
        return try {
            val zone = java.time.ZoneId.systemDefault()
            val selectedDate = java.time.LocalDate.parse(dateIso)
            val nowPlusSkew = java.time.ZonedDateTime.now(zone).plusSeconds(30)
            val startZdt = java.time.LocalTime.parse(startHHMM).atDate(selectedDate).atZone(zone)
            val endZdt = java.time.LocalTime.parse(endHHMM).atDate(selectedDate).atZone(zone)
            val sErr = if (startZdt.isBefore(nowPlusSkew)) "Giờ bắt đầu phải ở tương lai" else null
            val eErr = if (endZdt.isBefore(nowPlusSkew)) "Giờ kết thúc phải ở tương lai" else null
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
                Text("Lịch trống của bạn", color = Color.White)
            }
            Spacer(Modifier.weight(1f))
            MMPrimaryButton(onClick = {
                resetForm()
                showAdd = true
            }) {
                Icon(Icons.Default.Add, null, tint = Color.White)
                Spacer(Modifier.width(6.dp)); Text("Thêm lịch", color = Color.White)
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

                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (slot.sessionType == "video") Color(0x332467F1) else Color(0x3322C55E)),
                                    contentAlignment = Alignment.Center
                                ) { Text(if (slot.sessionType == "video") "💻" else "🤝") }

                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = slot.description ?: "Phiên ${if (slot.sessionType=="video") "Video Call" else "Trực tiếp"}",
                                        color = Color.White
                                    )
                                    Text("${slot.date}  •  ${slot.startTime} - ${slot.endTime}",
                                        color = Color.White.copy(.7f))
                                }

                                val badgeBg = when {
                                    !slot.isActive -> Color(0xFF6B7280)
                                    slot.isBooked  -> Color(0xFFEF4444)
                                    else           -> Color(0xFF22C55E)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badgeBg.copy(.25f))
                                        .border(BorderStroke(1.dp, badgeBg.copy(.45f)), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        when {
                                            !slot.isActive -> "Tạm dừng"
                                            slot.isBooked  -> "Đã đặt"
                                            else           -> "Còn trống"
                                        },
                                        color = Color.White
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip("Thời lượng", "${slot.duration} phút", Modifier.weight(1f))
                                    InfoChip(
                                        "Giá tư vấn",
                                        numberFormat.format((HOURLY * slot.duration) / 60),
                                        Modifier.weight(1f)
                                    )
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    InfoChip(
                                        "Hình thức",
                                        if (slot.sessionType=="video") "Video Call" else "Trực tiếp",
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
                                        dateDigits = isoToDigits(slot.date)
                                        startDigits = hhmmToDigits(slot.startTime)
                                        endDigits = hhmmToDigits(slot.endTime)
                                        type = slot.sessionType
                                        desc = TextFieldValue(slot.description ?: "")
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
            dateDigits = dateDigits,
            startDigits = startDigits,
            endDigits = endDigits,
            type = type,
            desc = desc,
            startError = startErr,
            endError = endErr,
            onDateChange = { dateDigits = it.filter(Char::isDigit).take(8) },
            onStartChange = { startDigits = it.filter(Char::isDigit).take(4); startErr = null },
            onEndChange = { endDigits = it.filter(Char::isDigit).take(4); endErr = null },
            onTypeChange = { type = it },
            onDescChange = { desc = it },
            onDismiss = { showAdd = false; startErr = null; endErr = null },
            // Inject buffer fields into dialog content
            bufBeforeDigits = bufBeforeDigits,
            bufAfterDigits = bufAfterDigits,
            onBufBeforeChange = { bufBeforeDigits = it.filter(Char::isDigit).take(3) },
            onBufAfterChange  = { bufAfterDigits  = it.filter(Char::isDigit).take(3) },
            onSubmit = {
                val dateIso   = validateDateDigitsReturnIso(dateDigits)
                val startHHMM = validateTimeDigitsReturnHHMM(startDigits)
                val endHHMM   = validateTimeDigitsReturnHHMM(endDigits)
                val duration  = durationFromDigits(startDigits, endDigits)

                if (dateIso == null) { Toast.makeText(context, "Ngày không hợp lệ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (startHHMM == null || endHHMM == null) { Toast.makeText(context, "Giờ không hợp lệ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (duration == null || duration < 30) { Toast.makeText(context, "Tối thiểu 30 phút.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                // Future-only validation (+30s)
                val (sErr, eErr) = futureErrors(dateIso, startHHMM, endHHMM)
                startErr = sErr; endErr = eErr
                if (sErr != null || eErr != null) {
                    Toast.makeText(context, "Vui lòng chọn thời gian ở tương lai.", Toast.LENGTH_SHORT).show()
                    return@AvailabilityDialog
                }

                fun clamp(v: String): Int = v.filter(Char::isDigit).take(3).toIntOrNull()?.coerceIn(0,120) ?: 0
                val bufBefore = clamp(bufBeforeDigits)
                val bufAfter  = clamp(bufAfterDigits)

                val newSlot = NewSlotInput(
                    date = dateIso,
                    startTime = startHHMM,
                    endTime = endHHMM,
                    duration = duration,
                    description = desc.text.ifBlank { null },
                    sessionType = type,
                    bufferBeforeMin = bufBefore,
                    bufferAfterMin = bufAfter
                )
                onAdd(newSlot)
                // Persist last used buffer values
                lastBufBefore = bufBefore.toString()
                lastBufAfter  = bufAfter.toString()
                // Close dialog; parent shows toast on result
                resetForm()
                showAdd = false
            }
        )
    }

    // ===== Dialog: SỬA =====
    if (showEdit && editingSlot != null) {
        AvailabilityDialog(
            title = "Chỉnh sửa lịch trống",
            primaryText = "Cập nhật",
            dateDigits = dateDigits,
            startDigits = startDigits,
            endDigits = endDigits,
            type = type,
            desc = desc,
            startError = startErr,
            endError = endErr,
            onDateChange = { dateDigits = it.filter(Char::isDigit).take(8) },
            onStartChange = { startDigits = it.filter(Char::isDigit).take(4); startErr = null },
            onEndChange = { endDigits = it.filter(Char::isDigit).take(4); endErr = null },
            onTypeChange = { type = it },
            onDescChange = { desc = it },
            onDismiss = { showEdit = false; editingSlot = null; resetForm(); startErr = null; endErr = null },
            onSubmit = {
                val dateIso   = validateDateDigitsReturnIso(dateDigits)
                val startHHMM = validateTimeDigitsReturnHHMM(startDigits)
                val endHHMM   = validateTimeDigitsReturnHHMM(endDigits)
                val duration  = durationFromDigits(startDigits, endDigits)

                if (dateIso == null) { Toast.makeText(context, "Ngày không hợp lệ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (startHHMM == null || endHHMM == null) { Toast.makeText(context, "Giờ không hợp lệ.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                if (duration == null || duration < 30) { Toast.makeText(context, "Tối thiểu 30 phút.", Toast.LENGTH_SHORT).show(); return@AvailabilityDialog }
                // Future-only validation (+30s)
                val (sErr, eErr) = futureErrors(dateIso, startHHMM, endHHMM)
                startErr = sErr; endErr = eErr
                if (sErr != null || eErr != null) {
                    Toast.makeText(context, "Vui lòng chọn thời gian ở tương lai.", Toast.LENGTH_SHORT).show()
                    return@AvailabilityDialog
                }

                val base = editingSlot!!
                val updated = base.copy(
                    date = dateIso,
                    startTime = startHHMM,
                    endTime = endHHMM,
                    duration = duration,
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

/**
 * Dialog dùng chung cho Thêm/Sửa để tránh lặp code
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilityDialog(
    title: String,
    primaryText: String,
    dateDigits: String,
    startDigits: String,
    endDigits: String,
    startError: String?,
    endError: String?,
    type: String,
    desc: TextFieldValue,
    onDateChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onDescChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    // Optional buffer fields: if provided, dialog renders buffer controls
    bufBeforeDigits: String? = null,
    bufAfterDigits: String? = null,
    onBufBeforeChange: ((String) -> Unit)? = null,
    onBufAfterChange: ((String) -> Unit)? = null,
    onSubmit: () -> Unit
) {
    var typeMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {}, dismissButton = {}, title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title)

                // Ngày
                FormLabel("📅  Ngày")
                OutlinedTextField(
                    value = dateDigits,
                    onValueChange = onDateChange,
                    placeholder = { Text("dd/MM/yyyy") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateMaskTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Loại phiên
                FormLabel("🎯  Loại phiên")
                ExposedDropdownMenuBox(expanded = typeMenu, onExpandedChange = { typeMenu = it }) {
                    OutlinedTextField(
                        value = if (type == "video") "💻 Video Call" else "🤝 Trực tiếp",
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = typeMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        DropdownMenuItem(text = { Text("💻 Video Call") }, onClick = { onTypeChange("video"); typeMenu = false })
                        DropdownMenuItem(text = { Text("🤝 Trực tiếp") }, onClick = { onTypeChange("in-person"); typeMenu = false })
                    }
                }

                // Giờ
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        FormLabel("🕐  Giờ bắt đầu")
                        OutlinedTextField(
                            value = startDigits,
                            onValueChange = onStartChange,
                            placeholder = { Text("HH:mm") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = TimeMaskTransformation(),
                            isError = startError != null,
                            supportingText = { if (startError != null) Text(startError, color = MaterialTheme.colorScheme.error) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        FormLabel("🕐  Giờ kết thúc")
                        OutlinedTextField(
                            value = endDigits,
                            onValueChange = onEndChange,
                            placeholder = { Text("HH:mm") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = TimeMaskTransformation(),
                            isError = endError != null,
                            supportingText = { if (endError != null) Text(endError, color = MaterialTheme.colorScheme.error) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Buffer fields (optional)
                if (bufBeforeDigits != null && bufAfterDigits != null && onBufBeforeChange != null && onBufAfterChange != null) {
                    FormLabel("🧱 Khoảng đệm (Buffer)")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Buffer trước (phút)", color = Color.White)
                            OutlinedTextField(
                                value = bufBeforeDigits,
                                onValueChange = onBufBeforeChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Khoảng đệm trước tính bằng phút") }
                        )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Buffer sau (phút)", color = Color.White)
                            OutlinedTextField(
                                value = bufAfterDigits,
                                onValueChange = onBufAfterChange,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("Khoảng đệm sau tính bằng phút") }
                        )
                        }
                    }
                }

                // Giá tham chiếu
                LiquidGlassCard(radius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("💎 Giá niêm yết", color = Color(0xFF0F172A))
                        Text("${NumberFormat.getCurrencyInstance(java.util.Locale("vi","VN")).format(100_000)}/giờ",
                            color = Color(0xFF059669))
                        Text("ℹ️ Giá tự động tính theo thời lượng phiên", color = Color(0xFF64748B))
                    }
                }

                // Mô tả
                FormLabel("📝  Mô tả (tùy chọn)")
                OutlinedTextField(
                    value = desc, onValueChange = onDescChange,
                    placeholder = { Text("Ví dụ: React Performance, Career Guidance…") },
                    shape = RoundedCornerShape(14.dp), minLines = 3,
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
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("❌ Hủy") }

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
private fun FormLabel(text: String) {
    Text(text = text, color = Color(0xFF475569), modifier = Modifier.fillMaxWidth())
}
