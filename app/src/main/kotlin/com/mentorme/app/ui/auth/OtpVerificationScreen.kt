package com.mentorme.app.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ✅ Khớp chữ ký đang được gọi từ AuthScreens.kt
@Composable
fun OtpVerificationScreen(
    email: String,
    onOtpSubmit: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBackToLogin: () -> Unit,
    onVerificationSuccess: () -> Unit,
    authState: AuthState
) {
    var otp by remember { mutableStateOf("") }

    val isLoading = authState.isLoading
    val isVerifying = authState.isOtpVerifying
    val verifySuccess = authState.verificationSuccess
    val errorText = authState.otpError

    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorText) {
        if (!errorText.isNullOrBlank()) {
            showSuccessDialog = false
            showErrorDialog = true
        }
    }
    LaunchedEffect(verifySuccess) {
        if (verifySuccess) {
            showErrorDialog = false
            showSuccessDialog = true
        }
    }
    LaunchedEffect(otp) {
        if (otp.length == 6 && !isLoading && !isVerifying) onOtpSubmit(otp)
    }

    // ✅ BỌC TOÀN BỘ MÀN HÌNH BẰNG BOX ĐỂ OVERLAY
    Box(modifier = Modifier.fillMaxSize()) {

        // ===== NỘI DUNG CHÍNH =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            FloatingLogo(size = 80.dp)

            Text(
                "Xác thực OTP",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color.White
            )
            Text(
                "Nhập mã gồm 6 chữ số được gửi tới:\n$email",
                color = Color.White.copy(0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            GlassFormContainer(modifier = Modifier.fillMaxWidth()) {
                SquareOtpInputField(
                    value = otp,
                    onValueChange = { otp = it },
                    isError = false,
                    isSuccess = verifySuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                )

                BigGlassButton(
                    text = when {
                        isVerifying -> "Đang xác minh..."
                        isLoading -> "Đang xử lý..."
                        otp.length < 6 -> "Nhập đủ 6 số"
                        else -> "Xác nhận"
                    },
                    subText = if (otp.length < 6) "${otp.length}/6 số" else null,
                    icon = null,
                    onClick = { if (otp.length == 6 && !isLoading && !isVerifying) onOtpSubmit(otp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onResendOtp,
                    enabled = !isLoading && !isVerifying,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                    border = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Gửi lại mã")
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp)
            ) {
                Text("Quay lại đăng nhập?", color = Color.White.copy(0.85f))
                SmallGlassPillButton(
                    text = "Đăng nhập",
                    onClick = onBackToLogin
                )
            }
        }


    }

    // Auto-close sau 20s
    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            kotlinx.coroutines.delay(20_000L)
            if (showSuccessDialog) {
                showSuccessDialog = false
                onVerificationSuccess()
            }
        }
    }
}

    /* =========================================================
 *  Thành phần OTP – hiển thị 6 ô & caret nháy
 * ========================================================= */
@Composable
private fun SquareOtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    isSuccess: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // Input ẩn: nhận focus & bàn phím, không vẽ text
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                val digits = newValue.filter { it.isDigit() }.take(6)
                onValueChange(digits)
                if (digits.length == 6) keyboard?.hide()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .focusRequester(focusRequester)
                .alpha(0f)
                .background(Color.Transparent),
            decorationBox = { /* không vẽ innerTextField */ }
        )

        // Lớp hiển thị 6 ô
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { focusRequester.requestFocus() },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val char = value.getOrNull(index)?.toString() ?: ""
                val isFocused = (value.length == index) || (value.isEmpty() && index == 0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                ) {
                    OtpBox(
                        value = char,
                        isFocused = isFocused,
                        // Không bôi đỏ theo error nữa
                        isError = false,
                        isSuccess = isSuccess && value.length == 6,
                        onClick = { focusRequester.requestFocus() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun OtpBox(
    value: String,
    isFocused: Boolean,
    isError: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSuccess -> Color(0xFF22C55E)
        isFocused -> Color.White.copy(0.9f)
        value.isNotEmpty() -> Color.White.copy(0.6f)
        isError -> MaterialTheme.colorScheme.error
        else -> Color.White.copy(0.35f)
    }

    val backgroundColor = when {
        isSuccess -> Color(0xFF22C55E).copy(alpha = 0.1f)
        isFocused -> Color.White.copy(0.12f)
        value.isNotEmpty() -> Color.White.copy(0.08f)
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else -> Color.White.copy(0.05f)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White
            )
        } else if (isFocused) {
            val alpha by animateFloatAsState(
                targetValue = if (System.currentTimeMillis() % 1000 < 500) 1f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "caret_blink"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(Color.White.copy(alpha), RoundedCornerShape(1.5.dp))
            )
        }
    }
}
