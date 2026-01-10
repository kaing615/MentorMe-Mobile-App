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
import androidx.compose.material.icons.automirrored.outlined.ShowChart
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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
import java.text.SimpleDateFormat
import com.mentorme.app.data.dto.wallet.MentorPayoutDto
import com.mentorme.app.data.dto.wallet.PayoutStatus
import com.mentorme.app.ui.navigation.Routes
import com.mentorme.app.ui.wallet.PayProvider
import com.mentorme.app.ui.wallet.WalletViewModel
import java.util.Locale

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
    methods: List<PaymentMethod> = initialPaymentMethods(),
    navController: NavHostController,
) {
    val TAG = "MentorProfileUI"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val vm: MentorProfileViewModel = hiltViewModel()
    val vmWallet: MentorWalletViewModel = hiltViewModel()
    val walletVm: WalletViewModel = hiltViewModel()
    val methods by walletVm.paymentMethods.collectAsState()
    val payouts by vmWallet.payouts.collectAsState(initial = emptyList())
    val state by vm.state.collectAsState()
    val notificationPreferences by notificationsViewModel.preferences.collectAsState()

    val walletBalance by vm.walletBalance.collectAsState()
    val overallStats by vm.overallStats.collectAsState()
    val weeklyEarnings by vm.weeklyEarnings.collectAsState()
    val yearlyEarnings by vm.yearlyEarnings.collectAsState()
    val currentYear by vm.currentYear.collectAsState()

    val profile = state.profile
    val isLoading = state.loading
    val isEditing = state.editing
    val edited = state.edited

    //  IMPORTANT:
    // - rememberSaveable giữ tab khi rotate/process restore
    // - LaunchedEffect(startTarget) chỉ set khi target thay đổi (vd: từ dashboard sang settings)
    var selectedTab by rememberSaveable { mutableIntStateOf(tabIndexFor(startTarget)) }

    LaunchedEffect(Unit) {
        vmWallet.loadPayouts()
        walletVm.loadPaymentMethods()
    }

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
                            vm = vm,
                            profile = profile,
                            onViewDetail = onViewStatistics,
                            onViewReviews = onViewReviews
                        )

                        2 -> MentorWalletTab(
                            balance = walletBalance,
                            payouts = payouts,
                            methods = methods,
                            onAddMethod = {
                                navController.navigate(Routes.PaymentMethods)
                            },
                            onCreatePayout = { amount ->
                                vmWallet.createPayout(amount)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đã gửi yêu cầu rút tiền")
                                }
                            }
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
            .padding(bottom = 100.dp), //  Extra space: system nav bar + GlassBottomBar (64dp + spacing)
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
                    //  Thông tin cơ bản
                    MentorLabeledField(
                        "Họ và tên",
                        profile.fullName,
                        isEditing,
                        edited.fullName,
                        onValueChange = { onChange(edited.copy(fullName = it)) },
                        leading = { Icon(Icons.Outlined.Person, null) }
                    )

                    MentorLabeledField(
                        "Email",
                        profile.email,
                        isEditing,
                        edited.email,
                        onValueChange = { onChange(edited.copy(email = it)) },
                        leading = { Icon(Icons.Outlined.Email, null) }
                    )

                    MentorLabeledField(
                        "Số điện thoại",
                        profile.phone ?: "Chưa cập nhật",
                        isEditing,
                        edited.phone ?: "",
                        onValueChange = { onChange(edited.copy(phone = it.takeIf { it.isNotBlank() })) },
                        leading = { Icon(Icons.Outlined.Phone, null) }
                    )

                    MentorLabeledField(
                        "Địa điểm",
                        profile.location ?: "Chưa cập nhật",
                        isEditing,
                        edited.location ?: "",
                        onValueChange = { onChange(edited.copy(location = it.takeIf { it.isNotBlank() })) },
                        leading = { Icon(Icons.Outlined.Place, null) }
                    )

                    //  Thông tin nghề nghiệp
                    MentorLabeledField(
                        "Chức danh",
                        profile.jobTitle ?: "Chưa cập nhật",
                        isEditing,
                        edited.jobTitle ?: "",
                        onValueChange = { onChange(edited.copy(jobTitle = it.takeIf { it.isNotBlank() })) },
                        leading = { Icon(Icons.Outlined.Work, null) }
                    )

                    MentorLabeledField(
                        "Chuyên mục",
                        profile.category ?: "Chưa cập nhật",
                        isEditing,
                        edited.category ?: "",
                        onValueChange = { onChange(edited.copy(category = it.takeIf { it.isNotBlank() })) },
                        leading = { Icon(Icons.Outlined.Category, null) }
                    )

                    //  Giá mỗi giờ
                    val hourlyRateText = if (profile.hourlyRate != null && profile.hourlyRate > 0) {
                        "${java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN")).format(profile.hourlyRate)} VNĐ/giờ"
                    } else {
                        "Chưa đặt giá"
                    }
                    val editedHourlyRateText = edited.hourlyRate?.toString() ?: ""

                    MentorLabeledField(
                        "Giá mỗi giờ",
                        hourlyRateText,
                        isEditing,
                        editedHourlyRateText,
                        onValueChange = { raw ->
                            val rate = raw.filter { it.isDigit() }.toIntOrNull()
                            onChange(edited.copy(hourlyRate = rate))
                        },
                        leading = { Icon(Icons.Outlined.AttachMoney, null) }
                    )

                    // ✅ Kinh nghiệm & Học vấn
                    MentorMultilineField(
                        "Kinh nghiệm",
                        profile.experience ?: "Chưa cập nhật",
                        isEditing,
                        edited.experience ?: "",
                        onValueChange = { onChange(edited.copy(experience = it.takeIf { it.isNotBlank() })) }
                    )

                    MentorMultilineField(
                        "Học vấn",
                        profile.education ?: "Chưa cập nhật",
                        isEditing,
                        edited.education ?: "",
                        onValueChange = { onChange(edited.copy(education = it.takeIf { it.isNotBlank() })) }
                    )

                    // ✅ Chuyên môn (Skills)
                    MentorLabeledField(
                        "Chuyên môn",
                        skillsText.ifBlank { "Chưa cập nhật" },
                        isEditing,
                        editedSkillsText,
                        onValueChange = { raw ->
                            val skills = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            onChange(edited.copy(interests = skills))
                        },
                        leading = { Icon(Icons.Outlined.Star, null) }
                    )

                    // ✅ Giới thiệu
                    MentorMultilineField(
                        "Giới thiệu",
                        profile.bio ?: "Chưa cập nhật",
                        isEditing,
                        edited.bio ?: "",
                        onValueChange = { onChange(edited.copy(bio = it.takeIf { it.isNotBlank() })) }
                    )
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
    vm: MentorProfileViewModel,
    profile: UserProfile,
    onViewDetail: () -> Unit,
    onViewReviews: () -> Unit
) {
    val overallStats by vm.overallStats.collectAsState()
    val weeklyEarnings by vm.weeklyEarnings.collectAsState()
    val yearlyEarnings by vm.yearlyEarnings.collectAsState()
    val currentYear by vm.currentYear.collectAsState()

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp), // ✅ Extra space for GlassBottomBar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Rating - from backend
            val ratingText = overallStats?.averageRating?.let {
                if (it > 0) String.format("%.1f⭐", it) else "Chưa có"
            } ?: "..."
            MentorStatCard("Rating", ratingText, Icons.Filled.Star, Color(0xFFFFD700), Modifier.weight(1f)) { onViewReviews() }

            // Total Mentees - from backend (changed from "Học viên" to "Mentee")
            val menteesText = overallStats?.totalMentees?.let {
                if (it > 0) "$it" else "0"
            } ?: "..."
            MentorStatCard("Mentee", menteesText, Icons.Outlined.Groups, Color(0xFF34D399), Modifier.weight(1f)) { onViewDetail() }

            // Total Hours - from backend (changed from "Giờ dạy" to "Giờ tư vấn")
            val hoursText = overallStats?.totalHours?.let {
                if (it > 0) String.format("%.1fh", it) else "0h"
            } ?: "..."
            MentorStatCard("Giờ tư vấn", hoursText, Icons.Outlined.AccessTime, Color(0xFF60A5FA), Modifier.weight(1f)) { onViewDetail() }
        }

        // Weekly Performance Chart (changed from "Hiệu suất tháng này" to "Hiệu suất tuần này")
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.clickable { onViewDetail() }) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.ShowChart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hiệu suất tuần này", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))

                // Bar chart for 7 days (Mon-Sun)
                Column(Modifier.fillMaxWidth()) {
                    // Bar area
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp), // ✅ Fixed height cho bars
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

                        if (weeklyEarnings.isNotEmpty() && weeklyEarnings.size == 7) {
                            val maxEarning = weeklyEarnings.maxOrNull() ?: 1L
                            val maxAmount = if (maxEarning > 0) maxEarning else 700_000L

                            weeklyEarnings.forEachIndexed { index, amount ->
                                val heightFraction = if (maxAmount > 0) {
                                    (amount.toFloat() / maxAmount.toFloat()).coerceIn(0.05f, 1f)
                                } else 0.05f

                                Box(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (amount > 0) Color(0xFF34D399).copy(0.8f)
                                            else Color.White.copy(0.2f)
                                        )
                                )
                            }
                        } else {
                            listOf(0.2f, 0.4f, 0.3f, 0.6f, 0.5f, 0.7f, 0.4f).forEach { h ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .fillMaxHeight(h)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(Color.White.copy(0.3f))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Label area (tách riêng)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Total weekly earnings
                val totalWeeklyEarnings = weeklyEarnings.sum()
                val nf = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                Text(
                    "Tổng thu nhập tuần: ${nf.format(totalWeeklyEarnings)}",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Yearly Performance Chart (12 months)
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.clickable { onViewDetail() }) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.ShowChart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hiệu suất năm $currentYear", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))

                // Bar chart for 12 months (Jan-Dec)
                Column(Modifier.fillMaxWidth()) {
                    // Bar area
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp), // ✅ Fixed height cho bars
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val months = listOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")

                        if (yearlyEarnings.isNotEmpty() && yearlyEarnings.size == 12) {
                            val maxEarning = yearlyEarnings.maxOrNull() ?: 1L
                            val maxAmount = if (maxEarning > 0) maxEarning else 10_000_000L

                            yearlyEarnings.forEach { amount ->
                                val heightFraction = (amount.toFloat() / maxAmount.toFloat()).coerceIn(0.05f, 1f)

                                Box(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 1.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (amount > 0) Color(0xFF60A5FA).copy(0.8f)
                                            else Color.White.copy(0.2f)
                                        )
                                )
                            }
                        } else {
                            listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.7f, 0.5f, 0.8f, 0.6f, 0.7f, 0.5f, 0.6f, 0.4f).forEach { h ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 1.dp)
                                        .fillMaxHeight(h)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(Color.White.copy(0.3f))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Label area (tách riêng)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12").forEach { month ->
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color.White.copy(0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Total yearly earnings
                val totalYearlyEarnings = yearlyEarnings.sum()
                val nfYear = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                Text(
                    "Tổng thu nhập năm: ${nfYear.format(totalYearlyEarnings)}",
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun hasPendingPayout(payouts: List<MentorPayoutDto>): Boolean {
    return payouts.any {
        it.status == PayoutStatus.PENDING || it.status == PayoutStatus.PROCESSING
    }
}

// --- TAB 3: VÍ (Wallet) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MentorWalletTab(
    balance: Long,
    payouts: List<MentorPayoutDto>,
    methods: List<PaymentMethod>,
    onAddMethod: () -> Unit,
    onCreatePayout: (amountMinor: Long) -> Unit
) {
    val defaultMethod = remember(methods) { methods.firstOrNull { it.isDefault } }
    val hasPaymentMethod = methods.isNotEmpty()
    val hasPending = hasPendingPayout(payouts)

    var showWithdrawSheet by remember { mutableStateOf(false) }

    val canWithdraw =
        balance > 0 &&
                hasPaymentMethod &&
                !hasPending

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thu nhập khả dụng", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                Text(
                    text = formatMoneyVnd(balance, withCurrency = true),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                MMGhostButton(
                    enabled = canWithdraw,
                    onClick = {
                        if (methods.isEmpty()) {
                            onAddMethod()
                        } else {
                            showWithdrawSheet = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.ArrowDownward, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Yêu cầu rút tiền")
                }

                when {
                    !hasPaymentMethod -> {
                        Text(
                            "Vui lòng thêm phương thức thanh toán để rút tiền",
                            color = Color(0xFFF59E0B),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onAddMethod) {
                            Text("Thêm phương thức thanh toán")
                        }
                    }
                    hasPending -> {
                        Text(
                            "Bạn đang có yêu cầu rút tiền đang xử lý",
                            color = Color(0xFFF59E0B),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    balance <= 0 -> {
                        Text(
                            "Số dư không đủ để rút",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // --- Phương thức rút tiền (default / đổi) ---
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Phương thức rút tiền", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }

                    // dùng ghost button cho đổi / sửa
                    MMGhostButton(onClick = onAddMethod) {
                        Text(if (defaultMethod == null) "Thêm" else "Đổi / Sửa", color = Color.White)
                    }
                }

                if (defaultMethod == null) {
                    Text(
                        "Chưa có phương thức rút tiền mặc định",
                        color = Color.White.copy(0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    MMPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAddMethod
                    ) {
                        Text("Thêm phương thức thanh toán")
                    }
                } else {
                    DefaultPaymentMethodRow(defaultMethod)
                }
            }
        }

        // --- Lịch sử giao dịch (PHẢI Ở TRONG Column) ---
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleMedium)

                if (payouts.isEmpty()) {
                    Text(
                        "Chưa có yêu cầu rút tiền",
                        color = Color.White.copy(0.7f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    payouts.forEach { payout ->
                        MentorPayoutRow(payout)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    if (showWithdrawSheet) {
        WithdrawBottomSheet(
            balance = balance,
            methods = methods,
            onAddMethod = onAddMethod,
            onConfirm = { amount ->
                onCreatePayout(amount)
            },
            onDismiss = { showWithdrawSheet = false }
        )
    }
}

@Composable
private fun DefaultPaymentMethodRow(method: PaymentMethod) {
    val icon = when (method.provider) {
        PayProvider.MOMO    -> Icons.Outlined.Payments
        PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
        PayProvider.BANK    -> Icons.Outlined.AccountBalance
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                method.label,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                method.detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.7f)
            )
        }

        Text(
            "Mặc định",
            color = Color(0xFF22C55E),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatVndInput(rawDigits: String): String {
    if (rawDigits.isBlank()) return ""
    return try {
        val number = rawDigits.toLong()
        java.text.NumberFormat
            .getNumberInstance(Locale("vi", "VN"))
            .format(number)
    } catch (e: Exception) {
        rawDigits
    }
}

private fun parseVndInputToLong(formatted: String): Long {
    return formatted.replace(".", "").toLongOrNull() ?: 0L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawBottomSheet(
    balance: Long,
    methods: List<PaymentMethod>,
    onAddMethod: () -> Unit,
    onConfirm: (amountMinor: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var selectedIndex by remember {
        mutableStateOf(methods.indexOfFirst { it.isDefault }.coerceAtLeast(0))
    }

    val formattedAmount = formatVndInput(rawAmount)
    val amountLong = parseVndInputToLong(formattedAmount)

    val canSubmit =
        amountLong > 0 &&
                amountLong <= balance &&
                methods.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A), // dark glass
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 42.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(0.3f))
            )

            Text(
                "Yêu cầu rút tiền",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Số dư khả dụng: ${formatMoneyVnd(balance, true)}",
                color = Color.White.copy(0.7f),
                style = MaterialTheme.typography.bodyMedium
            )

            // --- Amount input ---
            Column {
                Text("Số tiền rút", style = MaterialTheme.typography.labelLarge)

                MMTextField(
                    value = formattedAmount,
                    onValueChange = { input ->
                        rawAmount = input.filter { it.isDigit() }
                    },
                    placeholder = "VD: 500.000",
                    modifier = Modifier.fillMaxWidth(),
                    trailing = {
                        Text(
                            "đ",
                            color = Color.White.copy(0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )

                if (amountLong > balance) {
                    Text(
                        "Số tiền vượt quá số dư",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = { rawAmount = balance.toString() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Rút toàn bộ", color = Color(0xFF60A5FA))
                }
            }

            // --- Payment method ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Phương thức nhận tiền", style = MaterialTheme.typography.labelLarge)

                if (methods.isEmpty()) {
                    Text(
                        "Chưa có phương thức thanh toán",
                        color = Color.White.copy(0.7f)
                    )
                    MMPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAddMethod
                    ) {
                        Text("Thêm phương thức")
                    }
                } else {
                    methods.forEachIndexed { index, method ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIndex == index)
                                        Color.White.copy(0.12f)
                                    else Color.White.copy(0.05f)
                                )
                                .clickable { selectedIndex = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index }
                            )

                            Spacer(Modifier.width(8.dp))

                            Column(Modifier.weight(1f)) {
                                Text(method.label, fontWeight = FontWeight.SemiBold)
                                Text(
                                    method.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.7f)
                                )
                            }

                            if (method.isDefault) {
                                Text(
                                    "Mặc định",
                                    color = Color(0xFF22C55E),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    TextButton(onClick = onAddMethod) {
                        Text("Quản lý phương thức")
                    }
                }
            }

            MMPrimaryButton(
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onConfirm(amountLong)
                    onDismiss()
                }
            ) {
                Text("Xác nhận rút tiền")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MentorPayoutRow(payout: MentorPayoutDto) {
    val statusColor = when (payout.status) {
        PayoutStatus.PENDING -> Color(0xFFF59E0B)
        PayoutStatus.PROCESSING -> Color(0xFF60A5FA)
        PayoutStatus.PAID -> Color(0xFF22C55E)
        PayoutStatus.FAILED -> Color(0xFFEF4444)
    }

    LiquidGlassCard(
        radius = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Rút ${formatMoneyVnd(payout.amount, true)}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatDate(payout.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.6f)
                )
            }

            MentorStatusDotChip(
                payout.status.name,
                statusColor
            )
        }
    }
}

private fun formatDate(iso: String): String {
    return try {
        val instant = java.time.Instant.parse(iso)
        val date = java.util.Date.from(instant)
        java.text.SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(date)
    } catch (e: Exception) {
        iso
    }
}

@Composable
private fun MentorStatusDotChip(text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
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
private fun MentorLabeledField(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .9f))
        Spacer(Modifier.height(6.dp))

        if (editing) {
            MMTextField(
                value = editedValue,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                singleLine = true,
                leading = {
                    if (leading != null) {
                        CompositionLocalProvider(LocalContentColor provides Color.White) {
                            leading()
                        }
                    }
                },
                placeholder = value
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .liquidGlass(radius = 16.dp, alpha = 0.18f, borderAlpha = 0.25f)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    leading?.invoke()
                }
                if (leading != null) Spacer(Modifier.size(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MentorMultilineField(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .9f))
        Spacer(Modifier.height(6.dp))
        if (editing) {
            MMTextField(
                value = editedValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                placeholder = value
            )
        } else {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
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
