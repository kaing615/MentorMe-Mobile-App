package com.mentorme.app.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mentorme.app.ui.theme.LiquidGlassCard
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    email: String,
    verificationId: String,
    onOtpSubmit: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    var otpValue by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(600) } // 10 minutes in seconds
    val keyboardController = LocalSoftwareKeyboardController.current

    // Countdown timer
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Auto-submit when OTP is complete
    LaunchedEffect(otpValue) {
        if (otpValue.length == 6 && !isLoading) {
            keyboardController?.hide()
            onOtpSubmit(otpValue)
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Email Icon
        PulsingEmailIcon()

        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = "Xác minh email",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Description
        Text(
            text = "Chúng tôi đã gửi mã xác minh 6 chữ số đến",
            fontSize = 16.sp,
            color = Color.White.copy(0.8f),
            textAlign = TextAlign.Center
        )

        Text(
            text = email,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // OTP Input Container
        LiquidGlassCard(
            strong = true,
            radius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Nhập mã xác minh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // OTP Input Fields
                OtpInputField(
                    value = otpValue,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            otpValue = it
                        }
                    },
                    isError = error != null && !isLoading
                )

                Spacer(Modifier.height(16.dp))

                // Timer
                if (timeLeft > 0) {
                    Text(
                        text = "Mã hết hạn sau: ${String.format("%02d:%02d", minutes, seconds)}",
                        fontSize = 14.sp,
                        color = Color.White.copy(0.7f)
                    )
                } else {
                    Text(
                        text = "Mã đã hết hạn",
                        fontSize = 14.sp,
                        color = Color.Red.copy(0.8f)
                    )
                }

                // Error message
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Submit button
                BigGlassButton(
                    text = if (isLoading) "Đang xác minh..." else "Xác minh",
                    onClick = {
                        if (otpValue.length == 6) {
                            onOtpSubmit(otpValue)
                        }
                    },
                    icon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Resend OTP
                if (timeLeft <= 0) {
                    SmallGlassPillButton(
                        text = "Gửi lại mã",
                        onClick = {
                            otpValue = ""
                            timeLeft = 600
                            onResendOtp()
                        }
                    )
                } else {
                    TextButton(
                        onClick = { /* Disabled */ },
                        enabled = false
                    ) {
                        Text(
                            "Gửi lại mã",
                            color = Color.White.copy(0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Back to login
        TextButton(onClick = onBackToLogin) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White.copy(0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Quay lại đăng nhập",
                    color = Color.White.copy(0.8f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val char = value.getOrNull(index)?.toString() ?: ""
            val isFilled = char.isNotEmpty()
            val isCurrent = index == value.length

            OtpDigitBox(
                digit = char,
                isFilled = isFilled,
                isCurrent = isCurrent,
                isError = isError
            )
        }
    }

    // Hidden TextField for input handling
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = Modifier
            .size(0.dp)
            .focusRequester(focusRequester)
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFilled: Boolean,
    isCurrent: Boolean,
    isError: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    val borderColor = when {
        isError -> Color.Red.copy(0.6f)
        isFilled -> Color.White.copy(0.6f)
        isCurrent -> Color.White.copy(0.8f)
        else -> Color.White.copy(0.3f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.1f))
            .border(
                2.dp,
                borderColor,
                RoundedCornerShape(12.dp)
            )
            .scale(animatedScale)
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Blinking cursor for current position
        if (isCurrent && digit.isEmpty()) {
            BlinkingCursor()
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(Color.White.copy(alpha))
    )
}

@Composable
private fun PulsingEmailIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "email-pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LiquidGlassCard(
        strong = true,
        radius = 40.dp,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// Success/Failure Popup Components
@Composable
fun OtpVerificationDialog(
    isVisible: Boolean,
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            LiquidGlassCard(
                strong = true,
                radius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Icon
                    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error
                    val iconColor = if (isSuccess) Color.Green else Color.Red

                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Title
                    Text(
                        text = if (isSuccess) "Xác minh thành công!" else "Xác minh thất bại",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    // Message
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color.White.copy(0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Button
                    BigGlassButton(
                        text = if (isSuccess) "Tiếp tục" else "Thử lại",
                        onClick = onDismiss,
                        icon = {
                            Icon(
                                if (isSuccess) Icons.Default.ArrowForward else Icons.Default.Refresh,
                                null,
                                tint = Color.White
                            )
                        }
                    )
                }
            }
        }
    }
}
