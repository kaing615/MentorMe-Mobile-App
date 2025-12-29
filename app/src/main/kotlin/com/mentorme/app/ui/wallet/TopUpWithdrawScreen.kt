@file:Suppress("FunctionName")
package com.mentorme.app.ui.wallet

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.composed
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

private fun formatCurrencyVnd(amount: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return nf.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ví MentorMe", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(.7f))
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, null, tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/* ---------------------------------
   Màn hình NẠP TIỀN (UI-only)
   GIỮ NGUYÊN UI như yêu cầu
---------------------------------- */
@Composable
fun TopUpScreen(
    balance: Long,
    onBack: () -> Unit,
    onSubmit: (amount: Long, method: TopUpMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(TopUpMethod.MoMo) }
    var showConfirm by remember { mutableStateOf(false) }
    val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { WalletTopBar("Nạp tiền", onBack) }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.AttachMoney, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Nhập số tiền", style = MaterialTheme.typography.titleMedium)
                            }
                            MMTextField(
                                value = amountText,
                                onValueChange = { new ->
                                    // chỉ giữ số
                                    amountText = new.filter { it.isDigit() }.take(12)
                                },
                                singleLine = true,
                                placeholder = "VD: 200000",
                            )
                            if (amount > 0) {
                                Text(
                                    "Xem trước: ${formatCurrencyVnd(amount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(.85f)
                                )
                            }
                        }
                    }

                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Payments, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Phương thức nạp", style = MaterialTheme.typography.titleMedium)
                            }
                            MethodRowTopUp(
                                selected = method,
                                onSelected = { method = it }
                            )
                            Text(
                                when (method) {
                                    TopUpMethod.MoMo     -> "Ví MoMo: xử lý gần như tức thời."
                                    TopUpMethod.ZaloPay  -> "ZaloPay: xử lý nhanh trong vài phút."
                                    TopUpMethod.Bank     -> "Chuyển khoản ngân hàng: 5–10 phút tuỳ thời điểm."
                                },
                                color = Color.White.copy(.75f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val canSubmit = amount >= 10_000
                    MMPrimaryButton(
                        onClick = { if (canSubmit) showConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (canSubmit) 1f else 0.5f)
                    ) { Text("Xác nhận nạp") }
                    MMGhostButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) { Text("Huỷ") }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        if (showConfirm) {
            ConfirmSheet(
                title = "Xác nhận nạp",
                lines = listOf(
                    "Số tiền" to formatCurrencyVnd(max(0, amount)),
                    "Phương thức" to method.label
                ),
                confirmText = "Nạp ngay",
                onDismiss = { showConfirm = false },
                onConfirm = {
                    showConfirm = false
                    onSubmit(max(0, amount), method)
                }
            )
        }
    }
}

enum class TopUpMethod(val label: String) { MoMo("MoMo"), ZaloPay("ZaloPay"), Bank("Ngân hàng") }

@Composable
private fun MethodRowTopUp(selected: TopUpMethod, onSelected: (TopUpMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MethodItem(
            title = "MoMo", subtitle = "Khuyến nghị – nhanh nhất",
            selected = selected == TopUpMethod.MoMo, icon = Icons.Outlined.Payments
        ) { onSelected(TopUpMethod.MoMo) }

        MethodItem(
            title = "ZaloPay", subtitle = "Nhanh, phí thấp",
            selected = selected == TopUpMethod.ZaloPay, icon = Icons.Outlined.CreditCard
        ) { onSelected(TopUpMethod.ZaloPay) }

        MethodItem(
            title = "Chuyển khoản ngân hàng", subtitle = "5–10 phút",
            selected = selected == TopUpMethod.Bank, icon = Icons.Outlined.AccountBalance
        ) { onSelected(TopUpMethod.Bank) }
    }
}

/* ---------------------------------
   Wrapper: TopUpScreen sử dụng Shared WalletViewModel
   (không thay đổi UI, chỉ kết nối ViewModel và show snackbar khi số dư tăng)
---------------------------------- */

@Composable
fun TopUpScreen(
    onBack: () -> Unit,
    walletViewModel: WalletViewModel
) {
    val uiState by walletViewModel.uiState.collectAsState()
    val balance = (uiState as? WalletUiState.Success)?.wallet?.balanceMinor ?: 0L

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect top-up events
    LaunchedEffect(walletViewModel) {
        walletViewModel.topUpEvents.collect { ev ->
            when (ev) {
                is WalletViewModel.TopUpEvent.Success -> {
                    snackbarHostState.showSnackbar("Nạp tiền thành công")
                    // tùy ux: nav.popBackStack() từ caller; hoặc gửi event navigation
                }
                is WalletViewModel.TopUpEvent.Error -> {
                    snackbarHostState.showSnackbar("Lỗi: ${ev.message ?: "Không xác định"}")
                }
            }
        }
    }

    // Reuse existing UI (không đổi)
    TopUpScreen( // existing UI function defined earlier
        balance = balance,
        onBack = onBack,
        onSubmit = { amount, _method ->
            walletViewModel.topUp(amount)
        }
    )

    Box(Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

/* ---------------------------------
   Màn hình RÚT TIỀN (giữ nguyên như trước)
---------------------------------- */
@Composable
fun WithdrawScreen(
    balance: Long,
    bankInfo: BankInfo,
    onBack: () -> Unit,
    onSubmit: (amount: Long, bank: BankInfo) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val canWithdraw = amount >= 50_000 && amount <= balance

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
        )
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { WalletTopBar("Rút tiền", onBack) }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.AttachMoney, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Nhập số tiền rút", style = MaterialTheme.typography.titleMedium)
                            }
                            MMTextField(
                                value = amountText,
                                onValueChange = { new -> amountText = new.filter { it.isDigit() }.take(12) },
                                singleLine = true,
                                placeholder = "VD: 300000",
                            )
                            val warn = when {
                                amountText.isBlank() -> "Tối thiểu 50.000₫"
                                amount < 50_000      -> "Số tiền tối thiểu 50.000₫"
                                amount > balance     -> "Vượt quá số dư khả dụng"
                                else -> null
                            }
                            if (warn != null) Text(warn, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD54F))
                            if (amount > 0) Text("Xem trước: ${formatCurrencyVnd(amount)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(.85f))
                        }
                    }

                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Tài khoản nhận", style = MaterialTheme.typography.titleMedium)
                            BankBox(bankInfo)
                            Text(
                                "Lưu ý: rút tiền xử lý trong giờ làm việc (9:00–17:00, T2–T6).",
                                color = Color.White.copy(.75f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    MMPrimaryButton(
                        onClick = { if (canWithdraw) showConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (canWithdraw) 1f else 0.5f)
                    ) { Text("Xác nhận rút") }

                    MMGhostButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Huỷ") }
                }
            }
        }

        if (showConfirm) {
            ConfirmSheet(
                title = "Xác nhận rút",
                lines = listOf(
                    "Số tiền" to formatCurrencyVnd(amount),
                    "Ngân hàng" to bankInfo.bankName,
                    "Số tài khoản" to bankInfo.accountNumber,
                    "Chủ tài khoản" to bankInfo.accountName
                ),
                confirmText = "Rút ngay",
                onDismiss = { showConfirm = false },
                onConfirm = {
                    showConfirm = false
                    onSubmit(max(0, amount), bankInfo)
                }
            )
        }
    }
}

data class BankInfo(
    val bankName: String,
    val accountNumber: String,
    val accountName: String
)

@Composable
private fun BankBox(info: BankInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .liquidGlass(radius = 16.dp, alpha = 0.18f, borderAlpha = 0.25f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bg = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFFA78BFA)))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.AccountBalance, null, tint = Color.White) }

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(info.bankName, fontWeight = FontWeight.SemiBold)
            Text("STK: ${info.accountNumber}", color = Color.White.copy(.85f))
            Text("Chủ TK: ${info.accountName}", color = Color.White.copy(.75f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* --------- Reusables --------- */

@Composable
private fun MethodItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val borderAlpha = if (selected) 0.45f else 0.25f
    val bgAlpha = if (selected) 0.22f else 0.10f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = bgAlpha))
            .border(1.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(14.dp))
            .padding(12.dp)
            .then(Modifier)
            .noRippleClickable(onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White) }

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(.75f))
        }
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(
            selectedColor = Color.White, unselectedColor = Color.White.copy(.6f)
        ))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmSheet(
    title: String,
    lines: List<Pair<String, String>>,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.DarkGray.copy(alpha = 0.5f),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = .5f))
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

            // Info lines
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lines.forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k, color = Color.White.copy(.75f))
                        Text(v, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }
            }

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MMPrimaryButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                    Text(confirmText)
                }
                MMGhostButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Đóng")
                }
            }
        }
    }
}

/* Clickable without ripple – giữ phong cách Glass */
@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}
