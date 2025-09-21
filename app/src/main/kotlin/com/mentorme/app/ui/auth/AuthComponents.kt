package com.mentorme.app.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.mentorme.app.data.model.UserRole
import com.mentorme.app.ui.theme.LiquidGlassCard
import com.mentorme.app.ui.theme.liquidGlass
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset

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
    LiquidGlassCard(
        strong = true,
        modifier = Modifier
            .size(size)
            .offset(y = offsetY.dp),
        radius = size / 2
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Favorite, null, tint = Color.White, modifier = Modifier.size(size / 2))
        }
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
