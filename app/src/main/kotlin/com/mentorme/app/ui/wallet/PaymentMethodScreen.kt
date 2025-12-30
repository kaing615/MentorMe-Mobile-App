@file:Suppress("FunctionName")

package com.mentorme.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mentorme.app.ui.components.ui.MMGhostButton
import com.mentorme.app.ui.components.ui.MMPrimaryButton
import com.mentorme.app.ui.theme.LiquidGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    walletViewModel: WalletViewModel,
    methods: List<PaymentMethod>,
    onBack: () -> Unit,
    onChosen: (PaymentMethod) -> Unit,
    onAddNew: () -> Unit,
    onEditSelected: (PaymentMethod) -> Unit
) {
    // selected có thể null khi danh sách rỗng
    var selected by remember(methods) {
        mutableStateOf(methods.firstOrNull { it.isDefault } ?: methods.firstOrNull())
    }

    LaunchedEffect(Unit) {
        walletViewModel.loadPaymentMethods()
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
                        title = { Text("Chọn phương thức thanh toán", fontWeight = FontWeight.Bold, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }
                        },
                        actions = {
                            TextButton(onClick = onAddNew) { Text("Thêm mới", color = Color.White) }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (methods.isEmpty()) {
                        LiquidGlassCard(radius = 16.dp) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Chưa có phương thức nào", style = MaterialTheme.typography.titleMedium)
                                Text("Thêm phương thức để dùng khi thanh toán.", color = Color.White.copy(.8f))
                                MMPrimaryButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
                                    Text("Thêm phương thức")
                                }
                            }
                        }
                        return@Scaffold
                    }

                    methods.forEach { m ->
                        MethodRowSelectable(
                            method = m,
                            selected = selected?.id == m.id,
                            onClick = { selected = m }
                        )
                    }

                    val canAct = selected != null

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        MMPrimaryButton(
                            onClick = {
                                selected?.let {
                                    walletViewModel.setDefaultPaymentMethod(it.id)
                                }
                            },
                            modifier = Modifier.weight(1f).alpha(if (canAct) 1f else 0.5f)
                        ) { Text("Đặt làm mặc định") }

                        MMGhostButton(
                            onClick = { selected?.let(onEditSelected) },
                            modifier = Modifier.weight(1f).alpha(if (canAct) 1f else 0.5f)
                        ) {
                            Icon(Icons.Outlined.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sửa")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodRowSelectable(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (method.provider) {
        PayProvider.MOMO    -> Icons.Outlined.Payments
        PayProvider.ZALOPAY -> Icons.Outlined.CreditCard
        PayProvider.BANK    -> Icons.Outlined.AccountBalance
    }

    LiquidGlassCard(radius = 16.dp) {
        Row(
            Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Color.White) }

            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(method.label, style = MaterialTheme.typography.titleMedium)
                Text(method.detail, color = Color.White.copy(.8f), style = MaterialTheme.typography.bodySmall)
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(.6f)
                )
            )
        }
    }
}
