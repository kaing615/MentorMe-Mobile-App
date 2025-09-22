package com.mentorme.app.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mentorme.app.core.validation.AuthValidator

/**
 * Demo Compose screen showing how to use Konform validation in real-time
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonformValidationDemo() {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Real-time validation errors
    var userNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Overall validation
    val allErrors = remember(userName, email, password, confirmPassword) {
        AuthValidator.getAllSignUpErrors(userName, email, password, confirmPassword)
    }

    val isFormValid = allErrors.isEmpty() &&
                     userName.isNotBlank() &&
                     email.isNotBlank() &&
                     password.isNotBlank() &&
                     confirmPassword.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Konform Validation Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        // User Name Field với real-time validation
        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                userNameError = AuthValidator.validateUserName(it)
            },
            label = { Text("Tên người dùng") },
            isError = userNameError != null,
            supportingText = {
                userNameError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Email Field với real-time validation
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = AuthValidator.validateEmail(it)
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = {
                emailError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Password Field với real-time validation
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = AuthValidator.validatePassword(it)
                // Re-validate confirm password nếu đã nhập
                if (confirmPassword.isNotBlank()) {
                    confirmPasswordError = AuthValidator.validatePasswordConfirmation(it, confirmPassword)
                }
            },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = {
                passwordError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Confirm Password Field với real-time validation
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = AuthValidator.validatePasswordConfirmation(password, it)
            },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Submit Button
        Button(
            onClick = {
                // Thực hiện đăng ký khi form valid
                println("Đăng ký với: userName=$userName, email=$email")
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Đăng ký")
        }

        // Display all errors for debugging
        if (allErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lỗi validation:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    allErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Status indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isFormValid)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFormValid) "✅ Form hợp lệ" else "❌ Form chưa hợp lệ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFormValid)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Demo cho OTP Validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpValidationDemo() {
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }

    var otpError by remember { mutableStateOf<String?>(null) }
    var verificationIdError by remember { mutableStateOf<String?>(null) }

    val isOtpValid = AuthValidator.validateOtpData(verificationId, otp) == null &&
                    verificationId.isNotBlank() && otp.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "OTP Validation Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = verificationId,
            onValueChange = {
                verificationId = it
                verificationIdError = AuthValidator.validateVerificationId(it)
            },
            label = { Text("Verification ID") },
            isError = verificationIdError != null,
            supportingText = {
                verificationIdError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = otp,
            onValueChange = {
                otp = it
                otpError = AuthValidator.validateOtp(it)
            },
            label = { Text("Mã OTP (6 chữ số)") },
            isError = otpError != null,
            supportingText = {
                otpError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                println("Xác thực OTP: $otp")
            },
            enabled = isOtpValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Xác thực OTP")
        }
    }
}
