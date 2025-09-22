package com.mentorme.app.ui.auth

import android.util.Log
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
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.launch

@Composable
fun OtpVerificationScreen(
    email: String,
    verificationId: String,
    onOtpSubmit: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    showVerificationDialog: Boolean = false, // Thêm parameter để biết khi nào popup hiện
    modifier: Modifier = Modifier
) {
    var otpValue by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(600) } // 10 minutes in seconds for OTP expiry
    var resendTimeLeft by remember { mutableIntStateOf(0) } // Countdown for resend button (5 minutes = 300 seconds)
    var isResending by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Countdown timer for OTP expiry
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Countdown timer for resend button
    LaunchedEffect(resendTimeLeft) {
        if (resendTimeLeft > 0) {
            delay(1000)
            resendTimeLeft--
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
    val resendMinutes = resendTimeLeft / 60
    val resendSeconds = resendTimeLeft % 60

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
                    isError = error != null && !isLoading && !showVerificationDialog // Ẩn error state khi popup hiện
                )

                Spacer(Modifier.height(16.dp))

                // Timer for OTP expiry
                if (timeLeft > 0) {
                    Text(
                        text = "Mã hết hạn sau: ${String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)}",
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

                // Resend OTP section - Ẩn khi popup hiện
                if (!showVerificationDialog) {
                    if (resendTimeLeft > 0) {
                        // Show countdown timer for resend
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gửi lại mã sau:",
                                fontSize = 14.sp,
                                color = Color.White.copy(0.6f)
                            )
                            Text(
                                text = "${String.format(java.util.Locale.getDefault(), "%02d:%02d", resendMinutes, resendSeconds)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(0.8f)
                            )
                        }
                    } else {
                        // Show resend button when countdown is finished
                        SmallGlassPillButton(
                            text = if (isResending) "Đang gửi..." else "Gửi lại mã",
                            onClick = {
                                if (!isResending) {
                                    isResending = true
                                    otpValue = ""
                                    resendTimeLeft = 300 // 5 minutes countdown
                                    timeLeft = 600 // Reset OTP expiry timer
                                    onResendOtp()
                                    // Reset isResending after a short delay to simulate API call
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        delay(2000) // 2 seconds delay to simulate API call
                                        isResending = false
                                    }
                                }
                            }
                        )

                        if (!isResending) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Chưa nhận được mã?",
                                fontSize = 12.sp,
                                color = Color.White.copy(0.6f)
                            )
                        }
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
    Box(modifier = Modifier.fillMaxWidth()) {
        // OTP digit boxes - ensure all 6 boxes are visible
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, // Changed from spacedBy to SpaceEvenly
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
                    isError = isError,
                    onClick = { /* Handle click if needed */ },
                    modifier = Modifier.weight(1f) // Add weight to ensure equal distribution
                )
            }
        }

        // Invisible TextField that covers the entire row for input
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                // Transparent decoration box
                Box(modifier = Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Match the height of digit boxes
        )
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFilled: Boolean,
    isCurrent: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.1f))
            .border(
                2.dp,
                borderColor,
                RoundedCornerShape(12.dp)
            )
            .scale(animatedScale)
            .clickable(onClick = onClick) // Handle click
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
    onDismiss: () -> Unit,
    onLoginRedirect: (() -> Unit)? = null // Thêm callback riêng cho nút "Đăng nhập ngay"
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
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Animated Icon
                    if (isSuccess) {
                        AnimatedSuccessIcon()
                    } else {
                        AnimatedErrorIcon()
                    }

                    Spacer(Modifier.height(20.dp))

                    // Title với màu sắc phù hợp
                    Text(
                        text = if (isSuccess) "Đăng ký thành công!" else "Xác minh thất bại",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSuccess) Color.Green.copy(0.9f) else Color.Red.copy(0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    // Message với nội dung cải thiện
                    val displayMessage = if (isSuccess) {
                        "Tài khoản của bạn đã được tạo thành công!\nVui lòng đăng nhập để tiếp tục sử dụng dịch vụ."
                    } else {
                        if (message.contains("OTP") || message.contains("code") || message.contains("mã")) {
                            "Mã OTP không đúng hoặc đã hết hạn.\nVui lòng kiểm tra lại email và nhập mã OTP mới."
                        } else {
                            "Không thể xác minh tài khoản.\nVui lòng thử lại sau hoặc liên hệ hỗ trợ."
                        }
                    }

                    Text(
                        text = displayMessage,
                        fontSize = 16.sp,
                        color = Color.White.copy(0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    // Button với logic khác nhau cho success và failure
                    BigGlassButton(
                        text = if (isSuccess) "Đăng nhập ngay" else "Nhập lại OTP",
                        onClick = {
                            if (isSuccess && onLoginRedirect != null) {
                                // Gọi callback đặc biệt để chuyển về màn hình đăng nhập
                                onLoginRedirect()
                            } else {
                                // Trường hợp thất bại, chỉ đóng dialog để quay lại màn hình nhập OTP
                                onDismiss()
                            }
                        },
                        icon = {
                            Icon(
                                if (isSuccess) Icons.Default.Login else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )

                    // Bỏ nút "Gửi lại mã OTP" trong popup thất bai - user sẽ sử dụng nút gửi lại ở màn hình OTP
                }
            }
        }
    }
}

@Composable
private fun AnimatedSuccessIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Green.copy(0.3f),
                            Color.Green.copy(0.1f)
                        )
                    )
                )
        )

        // Checkmark icon
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.Green,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
private fun AnimatedErrorIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "error")
    val shake by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .offset(x = shake.dp)
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Red.copy(0.3f),
                            Color.Red.copy(0.1f)
                        )
                    )
                )
        )

        // Error icon
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(50.dp)
        )
    }
}
