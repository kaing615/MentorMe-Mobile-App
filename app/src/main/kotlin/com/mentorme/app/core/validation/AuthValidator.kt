package com.mentorme.app.core.validation


import android.util.Patterns

// ---------------- Data ----------------

data class SignUpData(
    val userName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class SignInData(
    val email: String,
    val password: String
)

data class OtpData(
    val verificationId: String,
    val otp: String
)

/** Dùng để giữ cấu trúc path giống code cũ (".email", ".password" ...) */
data class ValidationError(val dataPath: String, val message: String)

object AuthValidator {

    // Regex & ràng buộc
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    private val STRONG_PASS_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,128}$")
    private val OTP_REGEX = Regex("^\\d{6}$")

    // ---------------- Core validators (trả list lỗi chi tiết) ----------------

    private fun validate(signUp: SignUpData): List<ValidationError> {
        val errs = mutableListOf<ValidationError>()

        // userName
        if (signUp.userName.length < 3)
            errs += ValidationError(".userName", "Tên người dùng phải có ít nhất 3 ký tự")
        if (signUp.userName.length > 30)
            errs += ValidationError(".userName", "Tên người dùng không được quá 30 ký tự")
        if (!USERNAME_REGEX.matches(signUp.userName))
            errs += ValidationError(".userName", "Tên người dùng chỉ được chứa chữ cái, số và dấu gạch dưới")

        // email
        if (!Patterns.EMAIL_ADDRESS.matcher(signUp.email).matches())
            errs += ValidationError(".email", "Email không hợp lệ")

        // password
        if (!STRONG_PASS_REGEX.matches(signUp.password))
            errs += ValidationError(".password", "Mật khẩu phải có ít nhất 8 ký tự, chứa 1 chữ thường, 1 chữ hoa và 1 số")

        // confirm
        if (signUp.password != signUp.confirmPassword)
            errs += ValidationError(".confirmPassword", "Mật khẩu xác nhận không khớp")

        return errs
    }

    private fun validate(signIn: SignInData): List<ValidationError> {
        val errs = mutableListOf<ValidationError>()
        if (!Patterns.EMAIL_ADDRESS.matcher(signIn.email).matches())
            errs += ValidationError(".email", "Email không hợp lệ")
        if (signIn.password.isBlank())
            errs += ValidationError(".password", "Mật khẩu không được để trống")
        return errs
    }

    private fun validate(otp: OtpData): List<ValidationError> {
        val errs = mutableListOf<ValidationError>()
        if (otp.verificationId.isBlank())
            errs += ValidationError(".verificationId", "Mã xác thực không hợp lệ")
        if (!OTP_REGEX.matches(otp.otp))
            errs += ValidationError(".otp", "Mã OTP phải là 6 chữ số")
        return errs
    }

    // ---------------- API tương thích code cũ ----------------

    /** Trả về null nếu hợp lệ, hoặc message lỗi đầu tiên nếu không */
    fun validateSignUpData(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        val result = validate(SignUpData(userName, email, password, confirmPassword))
        return result.firstOrNull()?.message
    }

    fun validateSignInData(email: String, password: String): String? {
        val result = validate(SignInData(email, password))
        return result.firstOrNull()?.message
    }

    fun validateOtpData(verificationId: String, otp: String): String? {
        val result = validate(OtpData(verificationId, otp))
        return result.firstOrNull()?.message
    }

    // ---------------- Validators theo-field cho UI realtime ----------------

    fun validateUserName(userName: String): String? =
        validate(SignUpData(userName, "tmp@tmp.com", "TempPass1A", "TempPass1A"))
            .firstOrNull { it.dataPath == ".userName" }?.message

    fun validateEmail(email: String): String? =
        validate(SignInData(email, "x"))
            .firstOrNull { it.dataPath == ".email" }?.message

    fun validatePassword(password: String): String? =
        validate(SignUpData("tmp", "tmp@tmp.com", password, password))
            .firstOrNull { it.dataPath == ".password" }?.message

    fun validatePasswordConfirmation(password: String, confirmPassword: String): String? =
        validate(SignUpData("tmp", "tmp@tmp.com", password, confirmPassword))
            .firstOrNull { it.dataPath == ".confirmPassword" }?.message

    fun validateOtp(otp: String): String? =
        validate(OtpData("tmp", otp))
            .firstOrNull { it.dataPath == ".otp" }?.message

    fun validateVerificationId(verificationId: String): String? =
        validate(OtpData(verificationId, "123456"))
            .firstOrNull { it.dataPath == ".verificationId" }?.message

    // ---------------- Lấy tất cả lỗi ----------------

    fun getAllSignUpErrors(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): List<String> =
        validate(SignUpData(userName, email, password, confirmPassword)).map { it.message }

    fun getAllSignInErrors(email: String, password: String): List<String> =
        validate(SignInData(email, password)).map { it.message }

    fun getAllOtpErrors(verificationId: String, otp: String): List<String> =
        validate(OtpData(verificationId, otp)).map { it.message }
}
