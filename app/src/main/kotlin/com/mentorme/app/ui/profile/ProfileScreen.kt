// ProfileScreen.kt
@file:Suppress("FunctionName")

package com.mentorme.app.ui.profile

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SupportAgent
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.wallet.PaymentMethod
import com.mentorme.app.ui.wallet.PayProvider
import com.mentorme.app.ui.wallet.initialPaymentMethods
import com.mentorme.app.ui.wallet.mockPaymentMethods
import androidx.compose.material.icons.outlined.Logout

private val oneDecimalUS = DecimalFormat("#.#").apply {
    decimalFormatSymbols = DecimalFormatSymbols(Locale.US)  // dùng dấu chấm
}

/** 1_000 -> 1K, 1_000_000 -> 1M. Giữ 1 chữ số thập phân, bỏ .0 nếu không cần. */
fun formatMoneyShortVnd(amount: Long, withCurrency: Boolean = false): String {
    val abs = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    val text = when {
        abs >= 1_000_000 -> sign + oneDecimalUS.format(abs / 1_000_000.0) + "M"
        abs >= 1_000     -> sign + oneDecimalUS.format(abs / 1_000.0)     + "K"
        else -> sign + NumberFormat.getNumberInstance(Locale("vi", "VN")).format(abs)
    }
    return if (withCurrency) "$text ₫" else text
}

enum class TxType { TOP_UP, WITHDRAW, PAYMENT, REFUND }
enum class TxStatus { PENDING, SUCCESS, FAILED }

data class WalletTx(
    val id: String,
    val date: Date,
    val type: TxType,
    val amount: Long,     // + vào ví, - ra ví (VND)
    val note: String,
    val status: TxStatus
)

private fun mockTx(): List<WalletTx> = listOf(
    WalletTx("t1", GregorianCalendar(2025, 8, 21, 10, 15).time, TxType.TOP_UP,   +300_000, "Nạp MoMo",           TxStatus.SUCCESS),
    WalletTx("t2", GregorianCalendar(2025, 8, 20, 14, 0 ).time, TxType.PAYMENT,  -850_000, "Thanh toán buổi 60’", TxStatus.SUCCESS),
    WalletTx("t3", GregorianCalendar(2025, 8, 19, 9,  30).time, TxType.WITHDRAW, -500_000, "Rút về ngân hàng",    TxStatus.PENDING),
    WalletTx("t4", GregorianCalendar(2025, 8, 18, 16, 45).time, TxType.REFUND,   +120_000, "Hoàn tiền phí",       TxStatus.SUCCESS),
    WalletTx("t5", GregorianCalendar(2025, 8, 17, 11, 5 ).time, TxType.PAYMENT,  -450_000, "Thanh toán buổi 30’", TxStatus.FAILED)
)


/* =======================
   Models & Helpers
   ======================= */

private val typo: Typography
    @Composable get() = MaterialTheme.typography

enum class UserRole { MENTEE, MENTOR }

data class UserHeader(
    val fullName: String,
    val email: String,
    val role: UserRole
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val avatar: String? = null, // if you add Coil later
    val joinDate: Date,
    val totalSessions: Int,
    val totalSpent: Long,
    val interests: List<String>,
    val preferredLanguages: List<String>
)

fun formatCurrencyVnd(amount: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return nf.format(amount)
}

fun formatDateVi(date: Date): String {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("vi", "VN"))
    return sdf.format(date)
}

fun mockProfile(user: UserHeader): UserProfile {
    return if (user.role == UserRole.MENTOR) {
        UserProfile(
            id = "1",
            fullName = user.fullName,
            email = user.email,
            phone = "+84 123 456 789",
            location = "Hồ Chí Minh, Việt Nam",
            bio = "Senior Developer với 8+ năm kinh nghiệm trong lĩnh vực Frontend và Backend. Đã mentor cho 100+ mentees và giúp họ phát triển sự nghiệp trong công nghệ.",
            joinDate = GregorianCalendar(2022, Calendar.MARCH, 15).time,
            totalSessions = 156,
            totalSpent = 0L,
            interests = listOf("React", "Node.js", "System Design", "Leadership", "Career Coaching"),
            preferredLanguages = listOf("Tiếng Việt", "English")
        )
    } else {
        UserProfile(
            id = "1",
            fullName = user.fullName,
            email = user.email,
            phone = "+84 123 456 789",
            location = "Hồ Chí Minh, Việt Nam",
            bio = "Tôi là một developer đang học hỏi và phát triển kỹ năng trong lĩnh vực technology. Mong muốn được học hỏi từ những mentor giỏi để có thể phát triển sự nghiệp.",
            joinDate = GregorianCalendar(2023, Calendar.JUNE, 15).time,
            totalSessions = 12,
            totalSpent = 8_500_000L,
            interests = listOf("React", "Node.js", "AI/ML", "Product Management"),
            preferredLanguages = listOf("Tiếng Việt", "English")
        )
    }
}

/* =======================
   Screen
   ======================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserHeader,
    onOpenSettings: (() -> Unit)? = null,
    onOpenTopUp: () -> Unit = {},
    onOpenWithdraw: () -> Unit = {},
    onOpenChangeMethod: () -> Unit = {},
    onAddMethod: () -> Unit = {},
    methods: List<PaymentMethod> = initialPaymentMethods(),
    onLogout: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf(mockProfile(user)) }
    var isEditing by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf(profile) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Profile 1=Stats 2=Settings

    Box(Modifier.fillMaxSize()) {
        // ✅ NỀN LIQUID giống ChatScreen
        com.mentorme.app.ui.theme.LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )

        // ✅ Toàn bộ nội dung dùng màu trắng mặc định
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Thông tin cá nhân",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Quản lý thông tin và cài đặt tài khoản",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 92.dp) // chừa chỗ cho BottomBar
                ) {
                    // ---------- Tabs ----------
                    com.mentorme.app.ui.theme.LiquidGlassCard(
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
                                            .padding(6.dp)                             // tạo khoảng trống để viền không đè text
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
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
                            listOf("Profile", "Ví", "Thống kê", "Cài đặt").forEachIndexed { i, label ->
                                Tab(
                                    selected = selectedTab == i,
                                    onClick = { selectedTab = i },
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

                    Spacer(Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> ProfileTab(
                            profile = profile,
                            isEditing = isEditing,
                            edited = edited,
                            onEditToggle = { isEditing = !isEditing; if (!isEditing) edited = profile },
                            onChange = { edited = it },
                            onSave = {
                                profile = edited
                                isEditing = false
                                scope.launch { snackbarHostState.showSnackbar("Cập nhật profile thành công!") }
                            },
                            onCancel = {
                                edited = profile
                                isEditing = false
                            }
                        )
                        1 -> WalletTab(                         // 👈 NEW
                            balance = 8500000L,
                            onTopUp = onOpenTopUp,
                            onWithdraw = onOpenWithdraw,
                            onChangeMethod = onOpenChangeMethod,
                            onAddMethod = onAddMethod,
                            methods = methods
                        )
                        2 -> StatsTab(userRole = user.role, profile = profile)
                        3 -> SettingsTab(
                            onOpenSettings = onOpenSettings,
                            onOpenCSBM = null,
                            onOpenDKSD = null,
                            onOpenLHHT = null,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

/* =======================
   Tabs
   ======================= */

@Composable
private fun ProfileTab(
    profile: UserProfile,
    isEditing: Boolean,
    edited: UserProfile,
    onEditToggle: () -> Unit,
    onChange: (UserProfile) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            radius = 22.dp
        ) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thông tin cơ bản", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onEditToggle,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = .5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Outlined.Close else Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Hủy" else "Chỉnh sửa",
                            style = MaterialTheme.typography.labelLarge    // chiều cao chữ hợp với 36dp
                        )
                    }
                }

                // Avatar + header
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarCircle(initial = profile.fullName.firstOrNull()?.uppercaseChar() ?: 'U', size = 96.dp)
                    AnimatedVisibility(visible = !isEditing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                profile.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Thành viên từ ${formatDateVi(profile.joinDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Fields
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField(
                        label = "Họ và tên",
                        leading = { Icon(Icons.Outlined.Person, null) },
                        value = profile.fullName,
                        editing = isEditing,
                        editedValue = edited.fullName,
                        onValueChange = { onChange(edited.copy(fullName = it)) }
                    )
                    LabeledField(
                        label = "Email",
                        leading = { Icon(Icons.Outlined.Email, null) },
                        value = profile.email,
                        editing = isEditing,
                        editedValue = edited.email,
                        onValueChange = { onChange(edited.copy(email = it)) },
                        type = TextFieldType.Email
                    )
                    LabeledField(
                        label = "Số điện thoại",
                        leading = { Icon(Icons.Outlined.Phone, null) },
                        value = profile.phone ?: "Chưa cập nhật",
                        editing = isEditing,
                        editedValue = edited.phone.orEmpty(),
                        onValueChange = { onChange(edited.copy(phone = it)) }
                    )
                    LabeledField(
                        label = "Địa chỉ",
                        leading = { Icon(Icons.Outlined.Place, null) },
                        value = profile.location ?: "Chưa cập nhật",
                        editing = isEditing,
                        editedValue = edited.location.orEmpty(),
                        onValueChange = { onChange(edited.copy(location = it)) }
                    )
                    LabeledMultiline(
                        label = "Giới thiệu bản thân",
                        value = profile.bio ?: "Chưa có mô tả",
                        editing = isEditing,
                        editedValue = edited.bio.orEmpty(),
                        onValueChange = { onChange(edited.copy(bio = it)) }
                    )
                }

                // Interests
                Column {
                    Text("Lĩnh vực quan tâm", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    FlowRowWrap(horizontalGap = 8.dp, verticalGap = 8.dp) {
                        profile.interests.forEach {
                            AssistChip(
                                onClick = {},
                                label = { Text(it) },
                                leadingIcon = { Icon(Icons.Outlined.Star, null) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White,
                                    leadingIconContentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isEditing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MMPrimaryButton(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Lưu thay đổi") }
                        MMGhostButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Hủy") }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(text: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color.White.copy(0.22f) else Color.White.copy(0.08f),
            labelColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (selected) 0.5f else 0.2f))
    )
}

@Composable
private fun TransactionRow(tx: WalletTx) {
    val (icon, tint) = when (tx.type) {
        TxType.TOP_UP   -> Icons.Outlined.ArrowUpward   to Color(0xFF22C55E)
        TxType.WITHDRAW -> Icons.Outlined.ArrowDownward to Color(0xFFEF4444)
        TxType.PAYMENT  -> Icons.Outlined.ReceiptLong   to Color(0xFF60A5FA)
        TxType.REFUND   -> Icons.Outlined.Cached        to Color(0xFFF59E0B)
    }
    val amountText = (if (tx.amount > 0) "+ " else "- ") + formatCurrencyVnd(kotlin.math.abs(tx.amount))
    val amountColor = if (tx.amount > 0) Color(0xFF22C55E) else Color(0xFFEF4444)
    val statusColor = when (tx.status) {
        TxStatus.SUCCESS -> Color(0xFF10B981)
        TxStatus.PENDING -> Color(0xFFF59E0B)
        TxStatus.FAILED  -> Color(0xFFEF4444)
    }

    LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint)
            }

            Column(Modifier.weight(1f)) {
                Text(tx.note, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(SimpleDateFormat("yyyy-MM-dd • HH:mm", Locale.getDefault()).format(tx.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.75f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(amountText, fontWeight = FontWeight.Bold, color = amountColor)
                StatusDotChip(tx.status.name.lowercase().replaceFirstChar { it.titlecase() }, statusColor)
            }
        }
    }
}

@Composable
private fun StatusDotChip(text: String, color: Color) {
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

@Composable
private fun WalletTab(
    balance: Long,
    onTopUp: () -> Unit,
    onWithdraw: () -> Unit,
    onChangeMethod: () -> Unit,
    onAddMethod: () -> Unit,
    methods: List<PaymentMethod>
) {
    var filter by remember { mutableStateOf<TxType?>(null) } // null = Tất cả
    val txAll = remember { mockTx() }
    val tx = remember(filter, txAll) { filter?.let { t -> txAll.filter { it.type == t } } ?: txAll }

    val defaultMethod = methods.firstOrNull { it.isDefault }

    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---- Số dư + action ----
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Số dư ví", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Text(
                    text = formatCurrencyVnd(balance),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MMPrimaryButton(onClick = onTopUp, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.ArrowUpward, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp)); Text("Nạp tiền")
                    }
                    MMGhostButton(onClick = onWithdraw, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.ArrowDownward, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp)); Text("Rút tiền")
                    }
                }
            }
        }

        // ---- Phương thức thanh toán ----
        if (defaultMethod == null) {
            // EMPTY STATE
            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Phương thức thanh toán", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Bạn chưa thêm phương thức nào. Thêm một phương thức để sử dụng nhanh khi thanh toán.",
                        color = Color.White.copy(0.8f)
                    )
                    MMPrimaryButton(onClick = onAddMethod, modifier = Modifier.fillMaxWidth()) {
                        Text("Thêm phương thức")
                    }
                }
            }
        } else {
            // ĐÃ CÓ — show thẻ mặc định + nút Thay đổi
            val icon = when (defaultMethod.provider) {
                PayProvider.MOMO    -> Icons.Outlined.Payments
                PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
                PayProvider.BANK    -> Icons.Outlined.AccountBalance
            }
            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(icon, null, tint = Color.White) }

                    Column(Modifier.weight(1f)) {
                        Text("Thẻ mặc định", fontWeight = FontWeight.SemiBold)
                        Text("${defaultMethod.label} • ${defaultMethod.detail}", color = Color.White.copy(.85f))
                    }
                    MMGhostButton(onClick = onChangeMethod) { Text("Thay đổi") }
                }
            }
        }

        // ---- Filter giao dịch ----
        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleMedium, color = Color.White)
                FlowRowWrap(horizontalGap = 8.dp, verticalGap = 8.dp) {
                    FilterChipPill("Tất cả", selected = filter == null) { filter = null }
                    FilterChipPill("Nạp",     selected = filter == TxType.TOP_UP)   { filter = TxType.TOP_UP }
                    FilterChipPill("Rút",     selected = filter == TxType.WITHDRAW){ filter = TxType.WITHDRAW }
                    FilterChipPill("Thanh toán", selected = filter == TxType.PAYMENT){ filter = TxType.PAYMENT }
                    FilterChipPill("Hoàn tiền",  selected = filter == TxType.REFUND) { filter = TxType.REFUND }
                }
            }
        }

        // ---- Danh sách giao dịch ----
        tx.forEach { item ->
            TransactionRow(item)
        }

        if (tx.isEmpty()) {
            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Không có giao dịch", color = Color.White.copy(0.8f))
                }
            }
        }
    }
}

@Composable
private fun StatsTab(userRole: UserRole, profile: UserProfile) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = if (userRole == UserRole.MENTOR) "Phiên đã dạy" else "Buổi tư vấn",
                value = profile.totalSessions.toString(),
                icon = Icons.Outlined.MenuBook,
                modifier = Modifier.weight(1f)
            )
            val moneyText = if (userRole == UserRole.MENTOR) {
                formatMoneyShortVnd(45_600_000L, withCurrency = true)
            } else {
                formatMoneyShortVnd(profile.totalSpent, withCurrency = true)
            }

            StatCard(
                title = if (userRole == UserRole.MENTOR) "Thu nhập" else "Chi tiêu",
                value = moneyText,
                icon = Icons.Outlined.CreditCard,
                modifier = Modifier.weight(1f)
            )
        }

        LiquidGlassCard(radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thành tựu", style = MaterialTheme.typography.titleMedium)
                }

                if (userRole == UserRole.MENTOR) {
                    AchievementItem(
                        iconBg = Color(0x33FFD54F),
                        icon = Icons.Outlined.Star,
                        title = "Mentor xuất sắc",
                        subtitle = "Rating 4.9⭐ từ 100+ mentees"
                    )
                    AchievementItem(
                        iconBg = Color(0x334CAF50),
                        icon = Icons.Outlined.MenuBook,
                        title = "Mentor chuyên nghiệp",
                        subtitle = "150+ phiên tư vấn thành công"
                    )
                    AchievementItem(
                        iconBg = Color(0x338E24AA),
                        icon = Icons.Outlined.EmojiEvents,
                        title = "Top Mentor",
                        subtitle = "Top 5% mentor được yêu thích nhất"
                    )
                } else {
                    AchievementItem(
                        iconBg = Color(0x33FFD54F),
                        icon = Icons.Outlined.Star,
                        title = "Học viên tích cực",
                        subtitle = "Hoàn thành 10+ buổi tư vấn"
                    )
                    AchievementItem(
                        iconBg = Color(0x333F51B5),
                        icon = Icons.Outlined.MenuBook,
                        title = "Người học chuyên cần",
                        subtitle = "3 tháng liên tiếp có session"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    onOpenSettings: (() -> Unit)?,
    onOpenCSBM: (() -> Unit)? = null,   // Chính sách bảo mật
    onOpenDKSD: (() -> Unit)? = null,   // Điều khoản sử dụng
    onOpenLHHT: (() -> Unit)? = null,   // Liên hệ hỗ trợ
    onLogout: (() -> Unit)? = null
) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // --- Card: Thông báo ---
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thông báo", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                SettingSwitchRow("Email thông báo","Nhận email về booking và tin nhắn", true)
                SettingSwitchRow("Thông báo push","Nhận thông báo trên thiết bị", true)
                SettingSwitchRow("Tin nhắn marketing","Nhận thông tin khuyến mãi", false)
            }
        }

        // --- Card: Bảo mật ---
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Security, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Bảo mật", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                MMPrimaryButton (onClick = { onOpenSettings?.invoke() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Settings, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Cài đặt nâng cao")
                }
            }
        }

        // --- Card: Khác ---
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Khác", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                SettingLinkItem(Icons.Outlined.PrivacyTip, "Chính sách bảo mật") {
                    onOpenCSBM?.invoke()
                }
                SettingLinkItem(Icons.AutoMirrored.Outlined.Article, "Điều khoản sử dụng") {
                    onOpenDKSD?.invoke()
                }
                SettingLinkItem(Icons.Outlined.SupportAgent, "Liên hệ hỗ trợ") {
                    onOpenLHHT?.invoke()
                }
            }
        }

        // --- Card: Đăng xuất ---
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tài khoản",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                MMPrimaryButton (
                    onClick = { onLogout?.invoke() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất")
                }
            }
        }
    }
}


/* =======================
   Reusable UI pieces
   ======================= */

@Composable
private fun StatCard(
    title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier.height(120.dp), radius = 22.dp) {
        Row(
            Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = .8f)
                )
            }
        }
    }
}

@Composable
private fun AchievementItem(
    iconBg: Color,
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val grad = androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(iconBg.copy(alpha = .9f), Color.White.copy(alpha = .12f))
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(grad),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White) }

        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .75f))
        }
    }
}

enum class TextFieldType { Text, Email }

@Composable
private fun LabeledField(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit,
    leading: (@Composable () -> Unit)? = null,
    type: TextFieldType = TextFieldType.Text
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .9f))
        Spacer(Modifier.height(6.dp))

        if (editing) {
            // --- EDIT MODE: icon trắng + text cùng hàng, chiều cao 56dp ---
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
            // --- READ-ONLY MODE như cũ, icon trắng & căn giữa dọc ---
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
                if (leading != null) Spacer(Modifier.width(8.dp))
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
private fun LabeledMultiline(
    label: String,
    value: String,
    editing: Boolean,
    editedValue: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
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
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String, subtitle: String, defaultChecked: Boolean
) {
    var checked by remember { mutableStateOf(defaultChecked) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .7f))
        }
        Switch(
            checked = checked, onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.White.copy(alpha = .35f),
                uncheckedThumbColor = Color.White.copy(alpha = .7f),
                uncheckedTrackColor = Color.White.copy(alpha = .2f)
            )
        )
    }
}

/** Simple avatar with initial. Replace with Coil AsyncImage if you want. */
@Composable
private fun AvatarCircle(initial: Char, size: Dp) {
    val ring = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(2.dp, ring), CircleShape)
            .liquidGlass(radius = size / 2, alpha = 0.22f, borderAlpha = 0.45f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowWrap(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        content = { content() }
    )
}

/* =======================
   Previews
   ======================= */

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun Preview_Mentee() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        ProfileScreen(
            user = UserHeader(fullName = "Nguyễn Văn A", email = "a@example.com", role = UserRole.MENTEE)
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_Mentor_Dark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ProfileScreen(
            user = UserHeader(fullName = "Trần Thị Mentor", email = "mentor@example.com", role = UserRole.MENTOR)
        )
    }
}

@Composable
private fun SettingLinkItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    MMGhostButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(label, color = Color.White, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}


