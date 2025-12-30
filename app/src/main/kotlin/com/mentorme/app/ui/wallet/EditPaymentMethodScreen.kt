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
import com.mentorme.app.data.dto.paymentMethods.UpdatePaymentMethodRequest
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.components.ui.MMTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPaymentMethodScreen(
    method: PaymentMethod,
    onBack: () -> Unit,
    onSaved: (UpdatePaymentMethodRequest) -> Unit
) {
    var phone by remember {
        mutableStateOf(
            if (method.provider != PayProvider.BANK) method.detail else ""
        )
    }

    var bankName by remember {
        mutableStateOf(
            if (method.provider == PayProvider.BANK) method.label else ""
        )
    }

    var accountNumber by remember {
        mutableStateOf(
            if (method.provider == PayProvider.BANK) method.detail else ""
        )
    }

    val canSave = when (method.provider) {
        PayProvider.BANK ->
            bankName.isNotBlank() || accountNumber.isNotBlank()
        else ->
            phone.isNotBlank()
    }

    val icon = when (method.provider) {
        PayProvider.MOMO    -> Icons.Outlined.Payments
        PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
        PayProvider.BANK    -> Icons.Outlined.AccountBalance
    }

    Box(Modifier.fillMaxSize()) {
        com.mentorme.app.ui.theme.LiquidBackground(
            Modifier.matchParentSize().zIndex(-1f)
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("Sửa phương thức", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.ArrowBack, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { padding ->

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.06f)
                        )
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                    when (method.provider) {
                        PayProvider.MOMO, PayProvider.ZALOPAY -> {
                            MMTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it.filter(Char::isDigit).take(11)
                                },
                                singleLine = true,
                                placeholder = "Số điện thoại"
                            )
                        }

                        PayProvider.BANK -> {
                            MMTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                singleLine = true,
                                placeholder = "Tên ngân hàng"
                            )
                            MMTextField(
                                value = accountNumber,
                                onValueChange = {
                                    accountNumber = it.filter(Char::isDigit).take(20)
                                },
                                singleLine = true,
                                placeholder = "Số tài khoản"
                            )
                        }
                    }

                    MMPrimaryButton(
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val req = when (method.provider) {
                                PayProvider.MOMO, PayProvider.ZALOPAY ->
                                    UpdatePaymentMethodRequest(
                                        accountNumber = phone
                                    )

                                PayProvider.BANK ->
                                    UpdatePaymentMethodRequest(
                                        accountName = bankName,
                                        accountNumber = accountNumber
                                    )
                            }

                            onSaved(req)
                        }
                    ) {
                        Text("Lưu thay đổi")
                    }

                    MMGhostButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Huỷ")
                    }
                }
            }
        }
    }
}
