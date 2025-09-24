package com.mentorme.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.mentorme.app.R
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.LiquidGlassLogo
import com.mentorme.app.ui.theme.liquidGlass
import kotlin.math.roundToInt

/* ---------------- Shared Components ---------------- */
@Composable
fun FloatingLogo(size: Dp = 80.dp) {
    val infinite = rememberInfiniteTransition(label = "float-heart")
    val offsetY by infinite.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "y"
    )
    Box(Modifier.size(size).offset(y = offsetY.dp), contentAlignment = Alignment.Center) {
        LiquidGlassLogo(size = size)
    }
}

@Composable
fun GlassFormContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlassCard(
        strong = true,
        modifier = modifier.fillMaxWidth(),
        radius = 28.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(0.85f)) },
        placeholder = { Text(placeholder, color = Color.White.copy(0.5f)) },
        leadingIcon = leading,
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,          // ✅ text khi focus
            unfocusedTextColor = Color.White,        // ✅ text khi blur
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedBorderColor = Color.White.copy(alpha = 0.35f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            cursorColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Nút lớn bọc trong 1 lớp LiquidGlassCard, subText đặt BÊN TRONG glass */
@Composable
fun BigGlassButton(
    text: String,
    subText: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }

    LiquidGlassCard(
        strong = true,
        radius = 32.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp)
        ) {
            Button(
                onClick = { pressed = true; onClick() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (icon != null) {
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
            if (subText != null) {
                Text(
                    subText,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(0.75f)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
    LaunchedEffect(pressed) { if (pressed) { kotlinx.coroutines.delay(300); pressed = false } }
}

/** Nút pill nhỏ chỉ 1 lớp LiquidGlass, bề rộng vừa đủ chữ và canh giữa chuẩn */
@Composable
fun SmallGlassPillButton(
    text: String,
    onClick: () -> Unit,
    width: Dp = 200.dp,   // 👈 ép cùng chiều rộng cho mọi nút
    height: Dp = 48.dp
) {
    LiquidGlassCard(
        strong = true,
        radius = 24.dp,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width, height)   // 👈 ép kích thước cố định
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}



/* ---------------- Role Selector ---------------- */

@Composable
fun RoleSelector(role: UserRole, onRoleChange: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        RoleOptionCard("Mentee", role == UserRole.MENTEE) { onRoleChange(UserRole.MENTEE) }
        RoleOptionCard("Mentor", role == UserRole.MENTOR) { onRoleChange(UserRole.MENTOR) }
    }
}

@Composable
fun RowScope.RoleOptionCard(title: String, selected: Boolean, onClick: () -> Unit) {
    val softGold = Brush.linearGradient(listOf(Color(0xFFFFECB3), Color(0xFFFFD54F), Color(0xFFFFC947))) // dịu mắt
    val idleGlass = Brush.linearGradient(listOf(Color.White.copy(0.10f), Color.White.copy(0.05f)))
    val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, animationSpec = tween(300), label = "")

    LiquidGlassCard(
        strong = true, radius = 18.dp,
        modifier = Modifier
            .weight(1f)
            .height(55.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (selected) softGold else idleGlass),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFF1B1B1B) else Color.White
            )
        }
    }
}


@Composable
fun GlassDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    // Lớp bắt sự kiện toàn màn (không nền), để click ra ngoài thì đóng
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        // Popup LiquidGlass đục mạnh
        Card(
            modifier = Modifier
                .padding(32.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.12f) // nền card base
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            // Dùng Box để chồng thêm lớp “đục” phía trên nền card
            Box {
                // Lớp overlay tăng độ đục, giúp "che" chữ phía dưới
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.28f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(message, color = Color.White.copy(0.9f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}


/* ==== State thống nhất cho popup OTP ==== */
sealed interface OtpDialogState {
    data object None : OtpDialogState
    data class Success(val title: String = "Xác thực thành công", val message: String) : OtpDialogState
    data class Error  (val title: String = "Sai OTP",               val message: String) : OtpDialogState
}

/* ==== Popup LiquidGlass (đục mạnh) + animation success/error ==== */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OtpPopup(
    isVisible: Boolean,
    isSuccess: Boolean,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    scrimAlpha: Float = 0.6f,
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss?.invoke() },
            containerColor = Color.DarkGray.copy(alpha = 0.5f), // ✅ Đồng bộ với wallet
            scrimColor = Color.Black.copy(alpha = 0.75f), // ✅ Đồng bộ với wallet
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = .5f)) // ✅ Đồng bộ với wallet
                )
            }
        ) {
            OtpBottomSheetContent(
                isSuccess = isSuccess,
                title = title,
                message = message,
                confirmText = confirmText,
                onConfirm = onConfirm,
                onDismiss = { onDismiss?.invoke() }
            )
        }
    }
}

@Composable
fun OtpBottomSheetContent(
    isSuccess: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(20.dp) // ✅ Tăng padding như trong wallet
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ Tăng spacing như wallet
    ) {
        // ==== Icon + animation theo trạng thái ====
        if (isSuccess) {
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(320, easing = EaseOutBack), label = "okScale"
            )
            val rotate by animateFloatAsState(
                targetValue = 0f,
                animationSpec = tween(320), label = "okRotate"
            )
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2ECC71),
                modifier = Modifier
                    .size(52.dp)
                    .scale(scale)
                    .rotate(rotate)
            )
        } else {
            val density = LocalDensity.current
            val offsetX = remember { Animatable(0f) }
            // Shake 1 lần khi hiện
            LaunchedEffect(true) {
                offsetX.snapTo(0f)
                offsetX.animateTo(
                    0f,
                    animationSpec = keyframes {
                        durationMillis = 480
                        with(density) {
                            0f at 0
                            12.dp.toPx() at 60
                            -12.dp.toPx() at 120
                            9.dp.toPx() at 180
                            -9.dp.toPx() at 240
                            5.dp.toPx() at 300
                            -5.dp.toPx() at 360
                            0f at 480
                        }
                    }
                )
            }
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFF05151),
                modifier = Modifier
                    .size(48.dp)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            )
        }

        // Title - màu trắng như wallet
        Text(
            title,
            color = Color.White, // ✅ Đổi sang màu trắng như wallet
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, // ✅ Thêm Bold như wallet
            textAlign = TextAlign.Center
        )

        // Message - màu trắng nhạt như wallet
        Text(
            message,
            color = Color.White.copy(.75f), // ✅ Đổi sang màu trắng nhạt như wallet
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        // Actions - 2 nút như wallet
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Primary button
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f), // ✅ Như wallet
                    contentColor = Color.White // ✅ Text trắng
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // ✅ Tăng height như wallet
            ) {
                Text(confirmText)
            }

            // Ghost button để đóng
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White.copy(0.85f)
                ),
                border = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Đóng")
            }
        }
    }
}