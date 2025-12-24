package com.mentorme.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentorme.app.data.model.BookingStatus
import com.mentorme.app.ui.calendar.components.SegmentedTabs
import com.mentorme.app.ui.calendar.components.StatCard
import com.mentorme.app.ui.calendar.tabs.AvailabilityTabSection
import com.mentorme.app.ui.calendar.tabs.PendingBookingsTab
import com.mentorme.app.ui.calendar.tabs.SessionsTab
import com.mentorme.app.core.utils.Logx
import kotlinx.coroutines.launch

// Hilt entry point to access DataStoreManager
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface MentorCalendarDeps {
    fun dataStoreManager(): com.mentorme.app.core.datastore.DataStoreManager
}

@Composable
fun MentorCalendarScreen(
    onViewSession: (String) -> Unit = {},
    onCreateSession: () -> Unit = {},
    onUpdateAvailability: () -> Unit = {},
    onCancelSession: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // --- Tabs: 0 = Availability, 1 = Bookings, 2 = Sessions ---
    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("Lịch trống", "Booking", "Phiên học")

    // --- Dialog states for availability (lifted from AvailabilityTabSection) ---
    var showAdd by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    // --- ViewModel for Availability ---
    val vm =
        androidx.hilt.navigation.compose.hiltViewModel<com.mentorme.app.ui.calendar.MentorCalendarViewModel>()
    val bookingsVm =
        androidx.hilt.navigation.compose.hiltViewModel<com.mentorme.app.ui.calendar.MentorBookingsViewModel>()

    // Observe availability + bookings state
    val slotsState = vm.slots.collectAsState()
    val mentorBookings = bookingsVm.bookings.collectAsStateWithLifecycle()

    // Resolve mentorId: always from DataStore (no nav arg)
    val context = androidx.compose.ui.platform.LocalContext.current
    val deps = remember(context) {
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            context,
            MentorCalendarDeps::class.java
        )
    }
    // Thay đổi: thu ID an toàn với initial = ""
    val userIdFlow = remember { deps.dataStoreManager().getUserId() }
    val mentorId: String = userIdFlow.collectAsState(initial = "").value ?: ""

    // Compute default window [today 00:00 local .. +30 days 23:59:59] as ISO-UTC
    val zone = java.time.ZoneId.systemDefault()
    val todayStart = remember { java.time.LocalDate.now().atStartOfDay(zone) }
    val fromIsoUtc = remember { todayStart.toInstant().toString() }
    val toIsoUtc = remember {
        todayStart.plusDays(30).withHour(23).withMinute(59).withSecond(59)
            .toInstant().toString()
    }

    // Coroutine scope + toast helper
    val scope = rememberCoroutineScope()
    fun toast(msg: String) = android.widget.Toast.makeText(
        context,
        msg,
        android.widget.Toast.LENGTH_LONG
    ).show()

    // Initial load for availability only
    LaunchedEffect(mentorId) {
        // Chỉ load khi đã có mentorId; không toast ở giai đoạn init
        if (mentorId.isBlank()) return@LaunchedEffect
        Logx.d("MentorCalendarScreen") { "Loading window for mentorId=$mentorId, from=$fromIsoUtc, to=$toIsoUtc" }
        try {
            vm.loadWindow(mentorId, fromIsoUtc, toIsoUtc)
            bookingsVm.refresh()
        } catch (t: Throwable) {
            val msg =
                com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                    t.message
                )
            android.widget.Toast.makeText(
                context,
                msg,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // ---- Summary metrics based on mentor bookings ----
    val bookings = mentorBookings.value
    val availabilityOpen =
        remember(slotsState.value) { slotsState.value.count { it.isActive && !it.isBooked } }
    val confirmedCount =
        remember(bookings) { bookings.count { it.status == BookingStatus.CONFIRMED } }

    val totalPaid = remember(bookings) {
        bookings
            .filter { it.status == BookingStatus.COMPLETED }
            .sumOf { it.price.toInt() }
    }
    val totalPending = remember(bookings) {
        bookings
            .filter { it.status == BookingStatus.PAYMENT_PENDING }
            .sumOf { it.price.toInt() }
    }

    // Insets để né status bar & bottom nav
    val topInset =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dashboardHeight = 88.dp
    val bottomPadding = bottomInset + dashboardHeight

    // Helpers
    fun toIsoUtc(
        dateIso: String,
        hhmm: String,
        zoneId: java.time.ZoneId
    ): String {
        val localDate = java.time.LocalDate.parse(dateIso)
        val localTime = java.time.LocalTime.parse(hhmm)
        val zdt = localDate.atTime(localTime).atZone(zoneId)
        return zdt.toInstant().toString()
    }

    fun nowMinusSkew(): java.time.Instant =
        java.time.Instant.now().minusSeconds(30)

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Main content (hidden when dialog open)
        if (!showAdd && !showEdit) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Quản lý lịch Mentor",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Quản lý lịch trống và các buổi hẹn với mentee",
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))

                // Stats
                StatsOverview(
                    availabilityOpen = availabilityOpen,
                    confirmedCount = confirmedCount,
                    totalPaid = totalPaid,
                    totalPending = totalPending
                )
                Spacer(Modifier.height(10.dp))

                // Tabs
                SegmentedTabs(
                    activeIndex = activeTab,
                    labels = tabLabels,
                    onChange = { index ->
                        if (index == 0) {
                            // Chỉ load nếu mentorId đã sẵn sàng; KHÔNG toast khi rỗng
                            if (mentorId.isNotBlank()) {
                                try {
                                    Logx.d("MentorCalendarScreen") { "Loading window for mentorId=$mentorId, from=$fromIsoUtc, to=$toIsoUtc" }
                                    vm.loadWindow(
                                        mentorId,
                                        fromIsoUtc,
                                        toIsoUtc
                                    )
                                } catch (t: Throwable) {
                                    val msg =
                                        com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                                            t.message
                                        )
                                    android.widget.Toast.makeText(
                                        context,
                                        msg,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        if (index != 0) {
                            bookingsVm.refresh()
                        }
                        activeTab = index
                    }
                )
                Spacer(Modifier.height(12.dp))

                // (Tuỳ chọn) Thông báo thân thiện khi chưa có id
                if (activeTab == 0 && mentorId.isBlank()) {
                    Text(
                        text = "Đang khởi tạo phiên đăng nhập…",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(6.dp))
                }

                // Display content based on activeTab
                when (activeTab) {
                    0 -> AvailabilityTabSection(
                        slots = slotsState.value,
                        showAdd = showAdd,
                        onShowAddChange = { showAdd = it },
                        showEdit = showEdit,
                        onShowEditChange = { showEdit = it },
                        onAdd = { newSlot ->
                            if (mentorId.isBlank()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập lại",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@AvailabilityTabSection false
                            } else {
                                // Client guard: block past times with 30s skew
                                val startUtc = toIsoUtc(
                                    newSlot.date,
                                    newSlot.startTime,
                                    zone
                                )
                                val endUtc = toIsoUtc(
                                    newSlot.date,
                                    newSlot.endTime,
                                    zone
                                )
                                val startInstant = runCatching {
                                    java.time.Instant.parse(
                                        startUtc
                                    )
                                }.getOrNull()
                                val endInstant = runCatching {
                                    java.time.Instant.parse(
                                        endUtc
                                    )
                                }.getOrNull()
                                val nowSkew = nowMinusSkew()
                                if (startInstant == null || endInstant == null) {
                                    toast("Giờ không hợp lệ."); return@AvailabilityTabSection false
                                }
                                if (startInstant.isBefore(nowSkew)) {
                                    toast("Giờ bắt đầu phải ở tương lai"); return@AvailabilityTabSection false
                                }
                                if (endInstant.isBefore(nowSkew)) {
                                    toast("Giờ kết thúc phải ở tương lai"); return@AvailabilityTabSection false
                                }
                                if (!endInstant.isAfter(startInstant)) {
                                    toast("Giờ kết thúc phải sau giờ bắt đầu"); return@AvailabilityTabSection false
                                }

                                val res = vm.addSlot(
                                    mentorId = mentorId,
                                    dateIso = newSlot.date,
                                    startHHmm = newSlot.startTime,
                                    endHHmm = newSlot.endTime,
                                    sessionType = newSlot.sessionType,
                                    description = newSlot.description,
                                    priceVnd = newSlot.priceVnd,
                                    bufferBeforeMin = newSlot.bufferBeforeMin,
                                    bufferAfterMin = newSlot.bufferAfterMin
                                )
                                when (res) {
                                    is com.mentorme.app.core.utils.AppResult.Success -> {
                                        val pr = res.data
                                        if (pr.skippedConflict > 0) toast("Một số lịch bị bỏ qua vì trùng/buffer.")
                                        else toast("Đã xuất bản lịch")
                                        true
                                    }

                                    is com.mentorme.app.core.utils.AppResult.Error -> {
                                        val raw = res.throwable
                                        val code = run {
                                            val after = raw.substringAfter("HTTP ", "")
                                            after.take(3).toIntOrNull() ?: Regex("""\b([1-5]\d{2})\b""").find(
                                                raw
                                            )?.groupValues?.getOrNull(1)?.toIntOrNull()
                                        }
                                        when (code) {
                                            401 -> toast("Bạn cần đăng nhập để thực hiện thao tác này.")
                                            400 -> toast("Dữ liệu thời gian không hợp lệ")
                                            409 -> toast("Khung giờ bị trùng (đã tính buffer). Vui lòng chọn khung khác.")
                                            422 -> toast("Khung giờ bị trùng/đã tồn tại")
                                            else -> toast(
                                                com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                                                    raw
                                                )
                                            )
                                        }
                                        false
                                    }

                                    com.mentorme.app.core.utils.AppResult.Loading -> false
                                }
                            }
                        },
                        onUpdate = { updated ->
                            if (mentorId.isBlank()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập lại",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@AvailabilityTabSection
                            }
                            val normalizedType =
                                if (updated.sessionType.equals("in-person", true)) "in-person" else "video"
                            val desc = (updated.description ?: "").trim()
                            val composedTitle = buildString {
                                append("[type=$normalizedType] ")
                                if (desc.isNotBlank()) append(desc) else append(
                                    if (normalizedType == "in-person") "Phiên Trực tiếp" else "Phiên Video Call"
                                )
                            }
                            val startUtc = toIsoUtc(
                                updated.date,
                                updated.startTime,
                                zone
                            )
                            val endUtc = toIsoUtc(updated.date, updated.endTime, zone)
                            // Client guard: block past times with 30s skew
                            val startInstant = runCatching {
                                java.time.Instant.parse(
                                    startUtc
                                )
                            }.getOrNull()
                            val endInstant = runCatching {
                                java.time.Instant.parse(
                                    endUtc
                                )
                            }.getOrNull()
                            val nowSkew = nowMinusSkew()
                            if (startInstant == null || endInstant == null) {
                                toast("Giờ không hợp lệ."); return@AvailabilityTabSection
                            }
                            if (startInstant.isBefore(nowSkew)) {
                                toast("Giờ bắt đầu phải ở tương lai"); return@AvailabilityTabSection
                            }
                            if (endInstant.isBefore(nowSkew)) {
                                toast("Giờ kết thúc phải ở tương lai"); return@AvailabilityTabSection
                            }
                            if (!endInstant.isAfter(startInstant)) {
                                toast("Giờ kết thúc phải sau giờ bắt đầu"); return@AvailabilityTabSection
                            }

                            val patch =
                                com.mentorme.app.data.dto.availability.UpdateSlotRequest(
                                    title = composedTitle,
                                    description = desc,
                                    start = startUtc,
                                    end = endUtc,
                                    priceVnd = updated.priceVnd.toDouble()
                                )
                            val slotId = updated.backendSlotId
                            if (slotId.isBlank()) {
                                toast("Không xác định được Slot ID trên máy chủ.")
                            } else {
                                scope.launch {
                                    vm.updateSlotMeta(slotId, patch) { res ->
                                        when (res) {
                                            is com.mentorme.app.core.utils.AppResult.Success -> toast(
                                                "Đã cập nhật lịch trống!"
                                            )

                                            is com.mentorme.app.core.utils.AppResult.Error -> toast(
                                                com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                                                    res.throwable
                                                )
                                            )

                                            com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                        }
                                    }
                                }
                            }
                        },
                        onToggle = { id ->
                            if (mentorId.isBlank()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập lại",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@AvailabilityTabSection
                            }
                            val slot =
                                slotsState.value.firstOrNull { it.backendSlotId == id }
                            val action = if (slot?.isActive == true) "pause" else "resume"
                            scope.launch {
                                vm.updateSlotMeta(
                                    id,
                                    com.mentorme.app.data.dto.availability.UpdateSlotRequest(
                                        action = action
                                    )
                                ) { res ->
                                    when (res) {
                                        is com.mentorme.app.core.utils.AppResult.Success -> toast(
                                            if (action == "pause") "Đã tạm dừng lịch" else "Đã kích hoạt lịch"
                                        )

                                        is com.mentorme.app.core.utils.AppResult.Error -> toast(
                                            com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                                                res.throwable
                                            )
                                        )

                                        com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                    }
                                }
                            }
                        },
                        onDelete = { id ->
                            if (mentorId.isBlank()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập lại",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@AvailabilityTabSection
                            }
                            scope.launch {
                                vm.deleteSlot(id) { res ->
                                    when (res) {
                                        is com.mentorme.app.core.utils.AppResult.Success -> toast(
                                            "Đã xóa lịch trống"
                                        )

                                        is com.mentorme.app.core.utils.AppResult.Error -> {
                                            val raw = res.throwable
                                            if (raw.contains("409")) toast("Slot có booking trong tương lai, không thể xóa.")
                                            else toast(
                                                com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(
                                                    raw
                                                )
                                            )
                                        }

                                        com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                    }
                                }
                            }
                        }
                    )

                    1 -> PendingBookingsTab(
                        bookings = bookings,
                        onAccept = { bookingId ->
                            bookingsVm.accept(bookingId) { ok ->
                                toast(if (ok) "Đã chấp nhận booking" else "Không thể chấp nhận booking")
                                if (ok) bookingsVm.refresh()
                            }
                        },
                        onReject = { bookingId ->
                            bookingsVm.decline(
                                bookingId,
                                "Mentor từ chối"
                            ) { ok ->
                                toast(if (ok) "Đã từ chối booking" else "Không thể từ chối booking")
                                if (ok) bookingsVm.refresh()
                            }
                        }
                    )

                    2 -> SessionsTab(bookings = bookings)
                }

                // chừa chỗ đáy để né dashboard
                Spacer(Modifier.height(bottomPadding))
            }
        }

        // Layer 2: Always render AvailabilityTabSection when tab 0 AND dialog is open (for dialog rendering)
        if (activeTab == 0 && (showAdd || showEdit)) {
            AvailabilityTabSection(
                slots = slotsState.value,
                showAdd = showAdd,
                onShowAddChange = { showAdd = it },
                showEdit = showEdit,
                onShowEditChange = { showEdit = it },
                onAdd = { newSlot ->
                    if (mentorId.isBlank()) {
                        android.widget.Toast.makeText(
                            context,
                            "Vui lòng đăng nhập lại",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@AvailabilityTabSection false
                    } else {
                        val startUtc = toIsoUtc(newSlot.date, newSlot.startTime, zone)
                        val endUtc = toIsoUtc(newSlot.date, newSlot.endTime, zone)
                        val startInstant = runCatching { java.time.Instant.parse(startUtc) }.getOrNull()
                        val endInstant = runCatching { java.time.Instant.parse(endUtc) }.getOrNull()
                        val nowSkew = nowMinusSkew()
                        if (startInstant == null || endInstant == null) {
                            toast("Giờ không hợp lệ."); return@AvailabilityTabSection false
                        }
                        if (startInstant.isBefore(nowSkew)) {
                            toast("Giờ bắt đầu phải ở tương lai"); return@AvailabilityTabSection false
                        }
                        if (endInstant.isBefore(nowSkew)) {
                            toast("Giờ kết thúc phải ở tương lai"); return@AvailabilityTabSection false
                        }
                        if (!endInstant.isAfter(startInstant)) {
                            toast("Giờ kết thúc phải sau giờ bắt đầu"); return@AvailabilityTabSection false
                        }

                        val res = vm.addSlot(
                            mentorId = mentorId,
                            dateIso = newSlot.date,
                            startHHmm = newSlot.startTime,
                            endHHmm = newSlot.endTime,
                            sessionType = newSlot.sessionType,
                            description = newSlot.description,
                            priceVnd = newSlot.priceVnd,
                            bufferBeforeMin = newSlot.bufferBeforeMin,
                            bufferAfterMin = newSlot.bufferAfterMin
                        )
                        when (res) {
                            is com.mentorme.app.core.utils.AppResult.Success -> {
                                val pr = res.data
                                if (pr.skippedConflict > 0) toast("Một số lịch bị bỏ qua vì trùng/buffer.")
                                else toast("Đã xuất bản lịch")
                                true
                            }
                            is com.mentorme.app.core.utils.AppResult.Error -> {
                                val raw = res.throwable
                                val code = run {
                                    val after = raw.substringAfter("HTTP ", "")
                                    after.take(3).toIntOrNull() ?: Regex("""\b([1-5]\d{2})\b""").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
                                }
                                when (code) {
                                    401 -> toast("Bạn cần đăng nhập để thực hiện thao tác này.")
                                    400 -> toast("Dữ liệu thời gian không hợp lệ")
                                    409 -> toast("Khung giờ bị trùng (đã tính buffer). Vui lòng chọn khung khác.")
                                    422 -> toast("Khung giờ bị trùng/đã tồn tại")
                                    else -> toast(com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(raw))
                                }
                                false
                            }
                            com.mentorme.app.core.utils.AppResult.Loading -> false
                        }
                    }
                },
                onUpdate = { updated ->
                    if (mentorId.isBlank()) {
                        android.widget.Toast.makeText(context, "Vui lòng đăng nhập lại", android.widget.Toast.LENGTH_LONG).show()
                        return@AvailabilityTabSection
                    }
                    val normalizedType = if (updated.sessionType.equals("in-person", true)) "in-person" else "video"
                    val desc = (updated.description ?: "").trim()
                    val composedTitle = buildString {
                        append("[type=$normalizedType] ")
                        if (desc.isNotBlank()) append(desc) else append(if (normalizedType == "in-person") "Phiên Trực tiếp" else "Phiên Video Call")
                    }
                    val startUtc = toIsoUtc(updated.date, updated.startTime, zone)
                    val endUtc = toIsoUtc(updated.date, updated.endTime, zone)
                    val startInstant = runCatching { java.time.Instant.parse(startUtc) }.getOrNull()
                    val endInstant = runCatching { java.time.Instant.parse(endUtc) }.getOrNull()
                    val nowSkew = nowMinusSkew()
                    if (startInstant == null || endInstant == null) {
                        toast("Giờ không hợp lệ."); return@AvailabilityTabSection
                    }
                    if (startInstant.isBefore(nowSkew)) {
                        toast("Giờ bắt đầu phải ở tương lai"); return@AvailabilityTabSection
                    }
                    if (endInstant.isBefore(nowSkew)) {
                        toast("Giờ kết thúc phải ở tương lai"); return@AvailabilityTabSection
                    }
                    if (!endInstant.isAfter(startInstant)) {
                        toast("Giờ kết thúc phải sau giờ bắt đầu"); return@AvailabilityTabSection
                    }

                    val patch = com.mentorme.app.data.dto.availability.UpdateSlotRequest(
                        title = composedTitle,
                        description = desc,
                        start = startUtc,
                        end = endUtc,
                        priceVnd = updated.priceVnd.toDouble()
                    )
                    val slotId = updated.backendSlotId
                    if (slotId.isBlank()) {
                        toast("Không xác định được Slot ID trên máy chủ.")
                    } else {
                        scope.launch {
                            vm.updateSlotMeta(slotId, patch) { res ->
                                when (res) {
                                    is com.mentorme.app.core.utils.AppResult.Success -> toast("Đã cập nhật lịch trống!")
                                    is com.mentorme.app.core.utils.AppResult.Error -> toast(com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(res.throwable))
                                    com.mentorme.app.core.utils.AppResult.Loading -> Unit
                                }
                            }
                        }
                    }
                },
                onToggle = { id ->
                    if (mentorId.isBlank()) {
                        android.widget.Toast.makeText(context, "Vui lòng đăng nhập lại", android.widget.Toast.LENGTH_LONG).show()
                        return@AvailabilityTabSection
                    }
                    val slot = slotsState.value.firstOrNull { it.backendSlotId == id }
                    val action = if (slot?.isActive == true) "pause" else "resume"
                    scope.launch {
                        vm.updateSlotMeta(id, com.mentorme.app.data.dto.availability.UpdateSlotRequest(action = action)) { res ->
                            when (res) {
                                is com.mentorme.app.core.utils.AppResult.Success -> toast(if (action == "pause") "Đã tạm dừng lịch" else "Đã kích hoạt lịch")
                                is com.mentorme.app.core.utils.AppResult.Error -> toast(com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(res.throwable))
                                com.mentorme.app.core.utils.AppResult.Loading -> Unit
                            }
                        }
                    }
                },
                onDelete = { id ->
                    if (mentorId.isBlank()) {
                        android.widget.Toast.makeText(context, "Vui lòng đăng nhập lại", android.widget.Toast.LENGTH_LONG).show()
                        return@AvailabilityTabSection
                    }
                    scope.launch {
                        vm.deleteSlot(id) { res ->
                            when (res) {
                                is com.mentorme.app.core.utils.AppResult.Success -> toast("Đã xóa lịch trống")
                                is com.mentorme.app.core.utils.AppResult.Error -> {
                                    val raw = res.throwable
                                    if (raw.contains("409")) toast("Slot có booking trong tương lai, không thể xóa.")
                                    else toast(com.mentorme.app.core.utils.ErrorUtils.getUserFriendlyErrorMessage(raw))
                                }
                                com.mentorme.app.core.utils.AppResult.Loading -> Unit
                            }
                        }
                    }
                }
            )
        }
    }
}

/* -------------------- Local UI pieces -------------------- */

@Composable
private fun StatsOverview(
    availabilityOpen: Int,
    confirmedCount: Int,
    totalPaid: Int,
    totalPending: Int,
) {
    val vi = java.util.Locale("vi", "VN")
    val nf = remember { java.text.NumberFormat.getCurrencyInstance(vi) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD)
                    )
                },
                title = "Lịch còn trống",
                value = availabilityOpen.toString(),
                tint = Color(0xFF93C5FD),
                modifier = Modifier.weight(1f).height(110.dp)
            )
            StatCard(
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399)
                    )
                },
                title = "Đã xác nhận",
                value = confirmedCount.toString(),
                tint = Color(0xFF34D399),
                modifier = Modifier.weight(1f).height(110.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF34D399)
                    )
                },
                title = "Đã thu",
                value = nf.format(totalPaid),
                tint = Color(0xFF34D399),
                modifier = Modifier.weight(1f).height(110.dp)
            )
            StatCard(
                icon = {
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B)
                    )
                },
                title = "Chờ thanh toán",
                value = nf.format(totalPending),
                tint = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f).height(110.dp)
            )
        }
    }
}


