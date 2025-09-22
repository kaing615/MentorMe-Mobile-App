@file:Suppress("FunctionName")
package com.mentorme.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPaymentMethodScreen(
    method: PaymentMethod,
    onBack: () -> Unit,
    onSaved: (PaymentMethod) -> Unit
) {
    var label by remember { mutableStateOf(if (method.provider == PayProvider.BANK) method.label else "") }
    var phone by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accNumber by remember { mutableStateOf("") }

    val icon = when (method.provider) {
        PayProvider.MOMO    -> Icons.Outlined.Payments
        PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
        PayProvider.BANK    -> Icons.Outlined.AccountBalance
    }

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(Modifier.matchParentSize().zIndex(-1f))

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("Sửa phương thức", fontWeight = FontWeight.Bold, color = Color.White) },
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Loại (chỉ hiện, không đổi)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(.06f))) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                when (method.provider) {
                                    PayProvider.MOMO -> "MoMo"
                                    PayProvider.ZALOPAY -> "ZaloPay"
                                    PayProvider.BANK -> "Ngân hàng"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Trường theo loại
                    when (method.provider) {
                        PayProvider.MOMO, PayProvider.ZALOPAY -> {
                            MMTextField(
                                value = phone, onValueChange = { phone = it.filter(Char::isDigit).take(11) },
                                singleLine = true, placeholder = "SĐT (hiện tại: ${method.detail})"
                            )
                        }
                        PayProvider.BANK -> {
                            MMTextField(
                                value = label, onValueChange = { label = it },
                                singleLine = true, placeholder = "Tên ngân hàng (hiện tại: ${method.label})"
                            )
                            MMTextField(
                                value = accNumber, onValueChange = { accNumber = it.filter(Char::isDigit).take(20) },
                                singleLine = true, placeholder = "Số tài khoản (hiện tại: ${method.detail})"
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    MMPrimaryButton(
                        onClick = {
                            val updated = when (method.provider) {
                                PayProvider.MOMO ->
                                    method.copy(detail = if (phone.isNotBlank()) maskPhone(phone) else method.detail)
                                PayProvider.ZALOPAY ->
                                    method.copy(detail = if (phone.isNotBlank()) maskPhone(phone) else method.detail)
                                PayProvider.BANK ->
                                    method.copy(
                                        label = label.ifBlank { method.label },
                                        detail = if (accNumber.isNotBlank()) maskAccount(accNumber) else method.detail
                                    )
                            }
                            onSaved(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Lưu thay đổi") }

                    MMGhostButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Huỷ") }
                }
            }
        }
    }
}
