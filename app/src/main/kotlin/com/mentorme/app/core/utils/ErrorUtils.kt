package com.mentorme.app.core.utils

/**
 * Utility object for handling and converting error messages to user-friendly format
 */
object ErrorUtils {

    /**
     * Converts technical error messages to user-friendly Vietnamese messages
     * @param errorMessage The original error message from API or system
     * @return User-friendly error message in Vietnamese
     */
    fun getUserFriendlyErrorMessage(errorMessage: String?): String {
        return when {
            errorMessage == null -> "Đã xảy ra lỗi không xác định"

            // OTP specific errors - ưu tiên xử lý trước các lỗi khác
            errorMessage.contains("\"message\":\"Invalid OTP\"", ignoreCase = true) ->
                "Mã OTP không đúng. Vui lòng kiểm tra lại và nhập đúng 6 chữ số."
            errorMessage.contains("\"Invalid OTP\"", ignoreCase = true) ->
                "Mã OTP không đúng. Vui lòng kiểm tra lại và nhập đúng 6 chữ số."
            errorMessage.contains("Invalid OTP", ignoreCase = true) ->
                "Mã OTP không đúng. Vui lòng kiểm tra lại và nhập đúng 6 chữ số."
            errorMessage.contains("\"message\":\"OTP expired", ignoreCase = true) ->
                "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("\"OTP expired", ignoreCase = true) ->
                "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("OTP expired", ignoreCase = true) ->
                "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("\"message\":\"OTP not found", ignoreCase = true) ->
                "Mã OTP không hợp lệ. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("\"OTP not found", ignoreCase = true) ->
                "Mã OTP không hợp lệ. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("OTP not found", ignoreCase = true) ->
                "Mã OTP không hợp lệ. Vui lòng yêu cầu gửi lại mã mới."
            errorMessage.contains("\"message\":\"Invalid OTP format", ignoreCase = true) ->
                "Mã OTP phải là 6 chữ số. Vui lòng nhập lại."
            errorMessage.contains("\"Invalid OTP format", ignoreCase = true) ->
                "Mã OTP phải là 6 chữ số. Vui lòng nhập lại."
            errorMessage.contains("Invalid OTP format", ignoreCase = true) ->
                "Mã OTP phải là 6 chữ số. Vui lòng nhập lại."
            errorMessage.contains("\"message\":\"Too many attempts", ignoreCase = true) ->
                "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã OTP mới."
            errorMessage.contains("\"Too many attempts", ignoreCase = true) ->
                "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã OTP mới."
            errorMessage.contains("Too many attempts", ignoreCase = true) ->
                "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã OTP mới."

            // Network connection errors
            errorMessage.contains("failed to connect", ignoreCase = true) ->
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối internet và thử lại."
            errorMessage.contains("timeout", ignoreCase = true) ->
                "Kết nối bị timeout. Vui lòng thử lại sau."
            errorMessage.contains("network", ignoreCase = true) ->
                "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại."
            errorMessage.contains("connection", ignoreCase = true) ->
                "Lỗi kết nối. Vui lòng thử lại sau."
            errorMessage.contains("Unable to resolve host", ignoreCase = true) ->
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối internet."

            // HTTP status codes
            errorMessage.contains("HTTP", ignoreCase = true) ->
                "Lỗi máy chủ. Vui lòng thử lại sau."
            errorMessage.contains("401") ->
                "Thông tin đăng nhập không đúng"
            errorMessage.contains("403") ->
                "Bạn không có quyền truy cập"
            errorMessage.contains("404") ->
                "Không tìm thấy dịch vụ. Vui lòng thử lại sau."
            errorMessage.contains("500") ->
                "Lỗi máy chủ. Vui lòng thử lại sau."

            // Authentication specific errors
            errorMessage.contains("invalid credentials", ignoreCase = true) ->
                "Thông tin đăng nhập không đúng"
            errorMessage.contains("user not found", ignoreCase = true) ->
                "Tài khoản không tồn tại"
            errorMessage.contains("email already exists", ignoreCase = true) ->
                "Email này đã được sử dụng"
            errorMessage.contains("password", ignoreCase = true) ->
                "Mật khẩu không đúng"

            // Generic cases
            errorMessage.length > 100 ->
                "Đã xảy ra lỗi. Vui lòng thử lại sau."
            errorMessage.isBlank() ->
                "Đã xảy ra lỗi không xác định"

            else -> errorMessage
        }
    }

    /**
     * Checks if an error is a network-related error
     */
    fun isNetworkError(errorMessage: String?): Boolean {
        return errorMessage?.let { message ->
            message.contains("network", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("failed to connect", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true)
        } ?: false
    }

    /**
     * Checks if an error is an authentication-related error
     */
    fun isAuthError(errorMessage: String?): Boolean {
        return errorMessage?.let { message ->
            message.contains("401") ||
            message.contains("403") ||
            message.contains("invalid credentials", ignoreCase = true) ||
            message.contains("unauthorized", ignoreCase = true)
        } ?: false
    }
}
