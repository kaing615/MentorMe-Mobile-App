@file:Suppress("FunctionName")

package com.mentorme.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mentorme.app.ui.components.ui.MMTextField
import com.mentorme.app.ui.theme.LiquidGlassCard
import java.util.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodScreen(
    onBack: () -> Unit,
    onSaved: (PaymentMethod) -> Unit
) {
    var provider by remember { mutableStateOf(PayProvider.MOMO) }

    // fields
    var phone by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accNumber by remember { mutableStateOf("") }
    var accName by remember { mutableStateOf("") }

    val isValid = when (provider) {
        PayProvider.MOMO, PayProvider.ZALOPAY ->
            phone.filter { it.isDigit() }.length in 9..11
        PayProvider.BANK ->
            bankName.isNotBlank() && accNumber.filter { it.isDigit() }.length >= 8 && accName.isNotBlank()
    }

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(
            modifier = Modifier.matchParentSize().zIndex(-1f)
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("Thêm phương thức", fontWeight = MaterialTheme.typography.titleLarge.fontWeight, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Chọn loại
                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Chọn loại", style = MaterialTheme.typography.titleMedium)
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProviderChip(
                                    text = "MoMo",
                                    selected = provider == PayProvider.MOMO,
                                    icon = Icons.Outlined.Payments
                                ) { provider = PayProvider.MOMO }
                                ProviderChip(
                                    text = "ZaloPay",
                                    selected = provider == PayProvider.ZALOPAY,
                                    icon = Icons.Outlined.CreditCard
                                ) { provider = PayProvider.ZALOPAY }
                                ProviderChip(
                                    text = "Ngân hàng",
                                    selected = provider == PayProvider.BANK,
                                    icon = Icons.Outlined.AccountBalance
                                ) { provider = PayProvider.BANK }
                            }
                        }
                    }

                    // Nhập thông tin theo loại
                    LiquidGlassCard(radius = 22.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (provider) {
                                PayProvider.MOMO, PayProvider.ZALOPAY -> {
                                    Text(
                                        "Số điện thoại liên kết",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    MMTextField(
                                        value = phone,
                                        onValueChange = { new -> phone = new.filter { it.isDigit() }.take(11) },
                                        singleLine = true,
                                        placeholder = "VD: 0901234567"
                                    )
                                    Text(
                                        "Dùng số điện thoại đã đăng ký ví ${if (provider == PayProvider.MOMO) "MoMo" else "ZaloPay"}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(.75f)
                                    )
                                }
                                PayProvider.BANK -> {
                                    Text("Thông tin tài khoản", style = MaterialTheme.typography.titleMedium)
                                    MMTextField(
                                        value = bankName,
                                        onValueChange = { bankName = it },
                                        singleLine = true,
                                        placeholder = "Tên ngân hàng (VD: Vietcombank)"
                                    )
                                    MMTextField(
                                        value = accNumber,
                                        onValueChange = { accNumber = it.filter { c -> c.isDigit() }.take(20) },
                                        singleLine = true,
                                        placeholder = "Số tài khoản"
                                    )
                                    MMTextField(
                                        value = accName,
                                        onValueChange = { accName = it },
                                        singleLine = true,
                                        placeholder = "Chủ tài khoản (VIẾT HOA không dấu càng tốt)"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    MMPrimaryButton(
                        onClick = {
                            if (!isValid) return@MMPrimaryButton
                            val id = UUID.randomUUID().toString()
                            val method = when (provider) {
                                PayProvider.MOMO -> PaymentMethod(
                                    id = id,
                                    provider = provider,
                                    label = "MoMo",
                                    detail = maskPhone(phone),
                                    isDefault = false
                                )
                                PayProvider.ZALOPAY -> PaymentMethod(
                                    id = id,
                                    provider = provider,
                                    label = "ZaloPay",
                                    detail = maskPhone(phone),
                                    isDefault = false
                                )
                                PayProvider.BANK -> PaymentMethod(
                                    id = id,
                                    provider = provider,
                                    label = bankName.ifBlank { "Ngân hàng" },
                                    detail = maskAccount(accNumber),
                                    isDefault = false
                                )
                            }
                            onSaved(method)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isValid) 1f else 0.5f),
                    ) { Text("Lưu phương thức") }

                    MMGhostButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Huỷ") }
                }
            }
        }
    }
}

@Composable
private fun ProviderChip(
    text: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(Color.White.copy(alpha = if (selected) 0.22f else 0.10f))
            .then(if (selected) Modifier.border(1.dp, Color.White.copy(0.5f), shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White) }

        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

fun maskPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return if (digits.length >= 3) "${digits.take(3)}•••${digits.takeLast(3)}" else digits
}
fun maskAccount(raw: String): String {
    val d = raw.filter { it.isDigit() }
    return if (d.length >= 4) "•••• ${d.takeLast(4)}" else d
}
