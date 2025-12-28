package com.mentorme.app.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.profile.TxStatus
import com.mentorme.app.ui.profile.TxType
import com.mentorme.app.ui.profile.WalletTx
import com.mentorme.app.ui.profile.formatCurrencyVnd
import com.mentorme.app.ui.profile.mockTx
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.wallet.PayProvider
import com.mentorme.app.ui.wallet.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletTab(
    balance: Long,
    onTopUp: () -> Unit,
    onWithdraw: () -> Unit,
    onChangeMethod: () -> Unit,
    onAddMethod: () -> Unit,
    methods: List<PaymentMethod>
) {
    var filter by remember { mutableStateOf<TxType?>(null) }
    val txAll = remember { mockTx() }
    val tx = remember(filter, txAll) { filter?.let { t -> txAll.filter { it.type == t } } ?: txAll }

    val defaultMethod = methods.firstOrNull { it.isDefault }

    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        Spacer(Modifier.width(8.dp))
                        Text("Nạp tiền")
                    }
                    MMGhostButton(onClick = onWithdraw, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.ArrowDownward, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Rút tiền")
                    }
                }
            }
        }

        if (defaultMethod == null) {
            LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Phương thức thanh toán", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Bạn chưa thêm phương thức nào.Thêm một phương thức để sử dụng nhanh khi thanh toán.",
                        color = Color.White.copy(0.8f)
                    )
                    MMPrimaryButton(onClick = onAddMethod, modifier = Modifier.fillMaxWidth()) {
                        Text("Thêm phương thức")
                    }
                }
            }
        } else {
            val icon = when (defaultMethod.provider) {
                PayProvider.MOMO -> Icons.Outlined.Payments
                PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
                PayProvider.BANK -> Icons.Outlined.AccountBalance
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

        LiquidGlassCard(radius = 22.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleMedium, color = Color.White)
                FlowRowWrap(horizontalGap = 8.dp, verticalGap = 8.dp) {
                    FilterChipPill("Tất cả", selected = filter == null) { filter = null }
                    FilterChipPill("Nạp", selected = filter == TxType.TOP_UP) { filter = TxType.TOP_UP }
                    FilterChipPill("Rút", selected = filter == TxType.WITHDRAW) { filter = TxType.WITHDRAW }
                    FilterChipPill("Thanh toán", selected = filter == TxType.PAYMENT) { filter = TxType.PAYMENT }
                    FilterChipPill("Hoàn tiền", selected = filter == TxType.REFUND) { filter = TxType.REFUND }
                }
            }
        }

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
private fun FilterChipPill(text: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color.White.copy(0.22f) else Color.White.copy(0.08f),
            labelColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = if (selected) 0.5f else 0.2f))
    )
}

@Composable
private fun TransactionRow(tx: WalletTx) {
    val (icon, tint) = when (tx.type) {
        TxType.TOP_UP -> Icons.Outlined.ArrowUpward to Color(0xFF22C55E)
        TxType.WITHDRAW -> Icons.Outlined.ArrowDownward to Color(0xFFEF4444)
        TxType.PAYMENT -> Icons.Outlined.ReceiptLong to Color(0xFF60A5FA)
        TxType.REFUND -> Icons.Outlined.Cached to Color(0xFFF59E0B)
    }
    val amountText = (if (tx.amount > 0) "+ " else "- ") + formatCurrencyVnd(kotlin.math.abs(tx.amount))
    val amountColor = if (tx.amount > 0) Color(0xFF22C55E) else Color(0xFFEF4444)
    val statusColor = when (tx.status) {
        TxStatus.SUCCESS -> Color(0xFF10B981)
        TxStatus.PENDING -> Color(0xFFF59E0B)
        TxStatus.FAILED -> Color(0xFFEF4444)
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
                Text(
                    SimpleDateFormat("yyyy-MM-dd • HH:mm", Locale.getDefault()).format(tx.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.75f)
                )
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
