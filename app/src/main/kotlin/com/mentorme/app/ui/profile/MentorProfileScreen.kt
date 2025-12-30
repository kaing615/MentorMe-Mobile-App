package com.mentorme.app.ui.profile

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mentorme.app.data.model.NotificationPreferences
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.notifications.NotificationsViewModel
import com.mentorme.app.ui.profile.components.SettingSwitchRow
import com.mentorme.app.ui.theme.LiquidBackground
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.initialPaymentMethods
import kotlinx.coroutines.launch
import java.util.*

private fun mockTxLocal(): List<WalletTx> = listOf(
    WalletTx(
        id = "t1",
        date = GregorianCalendar(2025, 8, 21, 10, 15).timeInMillis,
        type = TxType.TOP_UP,
        amount = 5_000_000,
        note = "Thu nhập tháng 8",
        status = TxStatus.SUCCESS
    ),
    WalletTx(
        id = "t2",
        date = GregorianCalendar(2025, 8, 20, 14, 0).timeInMillis,
        type = TxType.WITHDRAW,
        amount = -2_000_000,
        note = "Rút về VCB",
        status = TxStatus.SUCCESS
    ),
    WalletTx(
        id = "t3",
        date = GregorianCalendar(2025, 8, 19, 9, 30).timeInMillis,
        type = TxType.PAYMENT,
        amount = 500_000,
        note = "Booking #123",
        status = TxStatus.SUCCESS
    ),
)

// ✅ helper map target -> tab index
private fun tabIndexFor(target: String): Int {
    return when (target.lowercase(Locale.ROOT)) {
        "profile", "hoso", "ho-so" -> 0
        "dashboard" -> 1
        "wallet", "vi" -> 2
        "settings", "caidat", "cai-dat" -> 3
        else -> 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorProfileScreen(
    // ✅ NEW: nhận “điểm vào” để mở đúng tab
    startTarget: String = "profile",
    notificationsViewModel: NotificationsViewModel,

    onEditProfile: () -> Unit = {},
    onViewEarnings: () -> Unit = {},
    onViewReviews: () -> Unit = {},
    onUpdateAvailability: () -> Unit = {},
    onManageServices: () -> Unit = {},
    onViewStatistics: () -> Unit = {},
    onSettings: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    onOpenTopUp: () -> Unit = {},
    onOpenWithdraw: () -> Unit = {},
    onOpenChangeMethod: () -> Unit = {},
    onAddMethod: () -> Unit = {},
    methods: List<PaymentMethod> = initialPaymentMethods()
) {
    val TAG = "MentorProfileUI"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val vm: MentorProfileViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val notificationPreferences by notificationsViewModel.preferences.collectAsState()

    val profile = state.profile
    val isLoading = state.loading
    val isEditing = state.editing
    val edited = state.edited

    // ✅ IMPORTANT:
    // - rememberSaveable giữ tab khi rotate/process restore
    // - LaunchedEffect(startTarget) chỉ set khi target thay đổi (vd: từ dashboard sang settings)
    var selectedTab by rememberSaveable { mutableIntStateOf(tabIndexFor(startTarget)) }
    LaunchedEffect(startTarget) {
        Log.d(TAG, "Enter MentorProfileScreen -> startTarget=$startTarget -> selectedTab=${tabIndexFor(startTarget)}")
        selectedTab = tabIndexFor(startTarget)
    }

    val tabs = listOf("Hồ sơ", "Thống kê", "Ví", "Cài đặt")

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Hồ sơ Mentor",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Verified",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    "Quản lý thương hiệu cá nhân",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White
                        )
                    )
                }
            ) { padding ->
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    return@Scaffold
                }

                state.error?.let { errorMsg ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lỗi: $errorMsg", color = Color.White)
                            Spacer(Modifier.height(12.dp))
                            MMPrimaryButton(onClick = { vm.refresh() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                    return@Scaffold
                }

                if (profile == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không có dữ liệu profile", color = Color.White)
                    }
                    return@Scaffold
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 22.dp
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            indicator = { positions ->
                                if (positions.isNotEmpty()) {
                                    val pos = positions[selectedTab.coerceIn(0, positions.lastIndex)]
                                    Box(
                                        Modifier
                                            .tabIndicatorOffset(pos)
                                            .fillMaxHeight()
                                            .padding(6.dp)
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
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            tabs.forEachIndexed { i, label ->
                                Tab(
                                    selected = selectedTab == i,
                                    onClick = {
                                        Log.d(TAG, "Tab click: index=$i label=$label")
                                        selectedTab = i
                                    },
                                    selectedContentColor = Color.White,
                                    unselectedContentColor = Color.White.copy(alpha = .6f),
                                    text = {
                                        Text(
                                            label,
                                            fontWeight = if (selectedTab == i) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    when (selectedTab) {
                        0 -> MentorInfoTab(
                            profile = profile,
                            isEditing = isEditing,
                            edited = edited ?: profile,
                            onEditToggle = { vm.toggleEdit() },
                            onChange = { updated -> vm.updateEdited { updated } },
                            onSave = {
                                vm.save { success, msg ->
                                    scope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar("Đã cập nhật hồ sơ")
                                        } else {
                                            snackbarHostState.showSnackbar(msg ?: "Cập nhật thất bại")
                                        }
                                    }
                                }
                            },
                            onCancel = { vm.toggleEdit() }
                        )


                        1 -> MentorStatsTab(
                            profile = profile,
                            onViewDetail = onViewStatistics,
                            onViewReviews = onViewReviews
                        )

                        2 -> MentorWalletTab(
                            balance = 15_600_000L,
                            onWithdraw = onOpenWithdraw,
                            methods = methods,
                            onAddMethod = onAddMethod
                        )

                        3 -> MentorSettingsTab(
                            onOpenSettings = onSettings,
                            onOpenNotifications = onOpenNotifications,
                            notificationPreferences = notificationPreferences,
                            onUpdateNotificationPreferences = { notificationsViewModel.updatePreferences(it) },
                            onUpdateAvailability = onUpdateAvailability,
                            onManageServices = onManageServices,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 1: THÔNG TIN (Info) ---
@Composable
private fun MentorInfoTab(
    profile: UserProfile,
    isEditing: Boolean,
    edited: UserProfile,
    onEditToggle: () -> Unit,
    onChange: (UserProfile) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val skillsText = profile.interests.joinToString(", ")
    val editedSkillsText = edited.interests.joinToString(", ")
    val headline = skillsText.ifBlank { profile.preferredLanguages.joinToString(", ") }
        .ifBlank { "Mentor" }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp), // ✅ Extra space: system nav bar + GlassBottomBar (64dp + spacing)
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thông tin chuyên gia", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onEditToggle,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = .5f))
                    ) {
                        Icon(if (isEditing) Icons.Outlined.Close else Icons.Outlined.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditing) "Hủy" else "Sửa")
                    }
                }

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    MentorAvatarPicker(
                        avatarUrl = if (isEditing) edited.avatar else profile.avatar,
                        initial = profile.fullName.take(1).firstOrNull() ?: 'M',
                        size = 110.dp,
                        enabled = isEditing,
                        onPick = { onChange(edited.copy(avatar = it)) }
                    )

                    if (!isEditing) {
                        Spacer(Modifier.height(12.dp))
                        Text(profile.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(headline, color = Color(0xFF60A5FA))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MentorLabeledField("Họ và tên", profile.fullName, isEditing, edited.fullName) { onChange(edited.copy(fullName = it)) }
                    MentorLabeledField("Chuyên môn", skillsText.ifBlank { "Chưa cập nhật" }, isEditing, editedSkillsText) { raw ->
                        val skills = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onChange(edited.copy(interests = skills))
                    }
                    MentorLabeledField("Email", profile.email, isEditing, edited.email) { onChange(edited.copy(email = it)) }
                    MentorMultilineField("Giới thiệu", profile.bio ?: "", isEditing, edited.bio ?: "") { onChange(edited.copy(bio = it)) }
                }

                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MMPrimaryButton(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Lưu") }
                        MMGhostButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Hủy") }
                    }
                }
            }
        }
    }
}

// --- TAB 2: THỐNG KÊ (Stats) ---
@Composable
private fun MentorStatsTab(
    profile: UserProfile,
    onViewDetail: () -> Unit,
    onViewReviews: () -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp), // ✅ Extra space for GlassBottomBar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MentorStatCard("Rating", "4.9⭐", Icons.Filled.Star, Color(0xFFFFD700), Modifier.weight(1f)) { onViewReviews() }
            MentorStatCard("Học viên", "50+", Icons.Outlined.Groups, Color(0xFF34D399), Modifier.weight(1f)) { onViewDetail() }
            MentorStatCard("Giờ dạy", "400h", Icons.Outlined.AccessTime, Color(0xFF60A5FA), Modifier.weight(1f)) { onViewDetail() }
        }

        LiquidGlassCard(radius = 22.dp, modifier = Modifier.clickable { onViewDetail() }) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ShowChart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hiệu suất tháng này", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().height(80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.5f).forEach { h ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(h)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(0.5f))
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Tổng thu nhập: 15.6M ₫", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB 3: VÍ (Wallet) ---
@Composable
private fun MentorWalletTab(
    balance: Long,
    onWithdraw: () -> Unit,
    methods: List<com.mentorme.app.ui.wallet.PaymentMethod>,
    onAddMethod: () -> Unit
) {
    val txs = remember { mockTxLocal() }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp), // ✅ Extra space for GlassBottomBar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp)) {
                Row { Icon(Icons.Outlined.AccountBalanceWallet, null); Spacer(Modifier.width(8.dp)); Text("Thu nhập khả dụng") }
                Spacer(Modifier.height(8.dp))
                Text(formatMoneyShortVnd(balance, true), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                Spacer(Modifier.height(16.dp))
                MMGhostButton(onClick = onWithdraw, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.ArrowDownward, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rút tiền về tài khoản")
                }
            }
        }

        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                txs.forEach { tx -> MentorTransactionRow(tx) }
            }
        }
    }
}

@Composable
private fun MentorTransactionRow(tx: WalletTx) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (tx.amount > 0) Color(0xFF22C55E) else Color(0xFFEF4444)
        Icon(
            if (tx.amount > 0) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.note, fontWeight = FontWeight.Medium)
            Text("Hôm nay", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
        }
        Text(
            (if (tx.amount > 0) "+" else "") + formatMoneyShortVnd(tx.amount),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- TAB 4: CÀI ĐẶT (Settings) ---
@Composable
private fun MentorSettingsTab(
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    notificationPreferences: NotificationPreferences,
    onUpdateNotificationPreferences: (NotificationPreferences) -> Unit,
    onUpdateAvailability: () -> Unit,
    onManageServices: () -> Unit,
    onLogout: () -> Unit
) {
    val allPushEnabled = notificationPreferences.pushBooking &&
        notificationPreferences.pushPayment &&
        notificationPreferences.pushMessage &&
        notificationPreferences.pushSystem

    val TAG = "MentorProfileUI"
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp), // ✅ Extra space for GlassBottomBar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("Quản lý dịch vụ", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                MentorSettingItem(Icons.Outlined.Schedule, "Lịch làm việc", "Quản lý khung giờ rảnh", onClick = {
                    Log.d(TAG, "Click SettingItem: Lịch làm việc")
                    onUpdateAvailability()
                })
                MentorSettingItem(Icons.Outlined.DesignServices, "Gói Mentoring", "Cài đặt giá và dịch vụ", onClick = {
                    Log.d(TAG, "Click SettingItem: Gói Mentoring")
                    onManageServices()
                })
            }
        }

        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thông báo", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                SettingSwitchRow(
                    "Thông báo push",
                    "Nhận thông báo trên thiết bị",
                    checked = allPushEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateNotificationPreferences(
                            notificationPreferences.copy(
                                pushBooking = enabled,
                                pushPayment = enabled,
                                pushMessage = enabled,
                                pushSystem = enabled
                            )
                        )
                    }
                )
                Text(
                    "Tùy chọn thông báo push",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                SettingSwitchRow(
                    "Booking",
                    "Xác nhận, nhắc lịch, hủy",
                    checked = notificationPreferences.pushBooking,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushBooking = it))
                    }
                )
                SettingSwitchRow(
                    "Thanh toán",
                    "Thành công hoặc thất bại",
                    checked = notificationPreferences.pushPayment,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushPayment = it))
                    }
                )
                SettingSwitchRow(
                    "Tin nhắn",
                    "Thông báo chat mới",
                    checked = notificationPreferences.pushMessage,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushMessage = it))
                    }
                )
                SettingSwitchRow(
                    "Hệ thống",
                    "Cập nhật chung",
                    checked = notificationPreferences.pushSystem,
                    onCheckedChange = {
                        onUpdateNotificationPreferences(notificationPreferences.copy(pushSystem = it))
                    }
                )
                MentorSettingItem(
                    Icons.Outlined.Notifications,
                    "Xem thông báo",
                    "Danh sách thông báo",
                    onClick = {
                        Log.d(TAG, "Click SettingItem: Thông báo")
                        onOpenNotifications()
                    }
                )
            }
        }

        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("Hệ thống", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                MentorSettingItem(Icons.Outlined.Settings, "Cài đặt chung", "Bảo mật, thông báo", onClick = {
                    Log.d(TAG, "Click SettingItem: Cài đặt chung")
                    onOpenSettings()
                })
                MentorSettingItem(Icons.AutoMirrored.Outlined.Logout, "Đăng xuất", null, isDestructive = true, onClick = {
                    Log.d(TAG, "Click SettingItem: Đăng xuất")
                    onLogout()
                })
            }
        }
    }
}

@Composable
private fun MentorAvatarPicker(avatarUrl: String?, initial: Char, size: Dp, enabled: Boolean, onPick: (String) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let(onPick)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(0.06f))
            .border(
                BorderStroke(3.dp, Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFFA78BFA)))),
                CircleShape
            )
            .liquidGlass(radius = size / 2, alpha = 0.22f)
            .clickable(enabled = enabled) { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().clip(CircleShape)
            )
        } else {
            Text(initial.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
        }

        if (enabled) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                Icon(Icons.Outlined.Edit, null, Modifier.size(12.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun MentorLabeledField(label: String, value: String, editing: Boolean, editedValue: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.7f))
        Spacer(Modifier.height(4.dp))
        if (editing) {
            MMTextField(value = editedValue, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth())
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(0.1f))
        }
    }
}

@Composable
private fun MentorMultilineField(label: String, value: String, editing: Boolean, editedValue: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.7f))
        Spacer(Modifier.height(4.dp))
        if (editing) {
            MMTextField(value = editedValue, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = false)
        } else {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f))
        }
    }
}

@Composable
private fun MentorStatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    LiquidGlassCard(radius = 18.dp, modifier = modifier.height(100.dp).clickable(onClick = onClick)) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
        }
    }
}

@Composable
private fun MentorSettingItem(icon: ImageVector, title: String, subtitle: String?, isDestructive: Boolean = false, onClick: () -> Unit) {
    MMGhostButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = if (isDestructive) Color(0xFFEF4444) else Color.White)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (isDestructive) Color(0xFFEF4444) else Color.White)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            }
            if (!isDestructive) Icon(Icons.Outlined.ChevronRight, null, tint = Color.White.copy(0.3f))
        }
    }
}
