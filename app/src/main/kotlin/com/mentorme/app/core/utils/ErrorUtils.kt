package com.mentorme.app.core.utils

import org.json.JSONArray
import org.json.JSONObject

/**
 * Chuyển lỗi kỹ thuật thành thông điệp thân thiện (tiếng Việt).
 * Thứ tự ưu tiên:
 * 1) JSON message (message/error/detail/errors[field])
 * 2) Nhận diện sai email/mật khẩu (kể cả HTTP 400/422)
 * 3) Lỗi mạng (timeout/ECONNREFUSED/DNS)
 * 4) Mã HTTP phổ biến
 * 5) Câu súc tích từ raw
 * 6) Thông điệp chung
 */
object ErrorUtils {

    private const val GENERIC = "Lỗi máy chủ. Vui lòng thử lại sau."

    fun getUserFriendlyErrorMessage(raw: String?): String {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return GENERIC

        // 1) Ưu tiên JSON body
        parseJsonMessage(text)?.let { if (it.isNotBlank()) return it }

        // 2) Sai email/mật khẩu (bắt sớm, kể cả khi status = 400/422)
        detectAuthCredentialIssue(text)?.let { return it }

        // 3) OTP – fallback chuỗi thường
        otpMessage(text)?.let { return it }

        // 4) Lỗi mạng
        detectNetworkIssue(text)?.let { return it }

        // 5) Mã HTTP trong chuỗi
        when (extractStatusCode(text)) {
            400, 422 -> {
                // Nếu có dấu hiệu form auth → vẫn coi như sai credentials
                detectAuthCredentialIssue(text)?.let { return it }
                pickMeaningfulSentence(text)?.let { return it }
                return "Dữ liệu chưa hợp lệ. Vui lòng kiểm tra lại."
            }
            401 -> return "Email hoặc mật khẩu không đúng."
            403 -> return "Bạn không có quyền thực hiện thao tác này."
            404 -> return "Không tìm thấy tài nguyên."
            409 -> return "Dữ liệu xung đột. Vui lòng thử lại."
            429 -> return "Bạn thao tác quá nhanh. Vui lòng thử lại sau."
            in 500..599 -> return GENERIC
        }

        // 6) Cụm từ phổ biến khác
        if (text.contains("invalid email or password", true)) {
            return "Email hoặc mật khẩu không đúng."
        }

        // 7) Cố lấy 1 câu có nghĩa từ raw
        pickMeaningfulSentence(text)?.let { return it }

        return GENERIC
    }

    // ========= Helpers =========

    /** Parse message từ JSON: "message" | "error" | "detail" | "errors[field][0]" */
    private fun parseJsonMessage(raw: String): String? {
        val trimmed = raw.trim()
        val jsonObj: JSONObject? = try {
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed)
                trimmed.startsWith("[") -> {
                    val arr = JSONArray(trimmed)
                    return firstStringFromJsonArray(arr)
                }
                else -> null
            }
        } catch (_: Exception) { null }

        jsonObj ?: return null

        // 1) Lấy trực tiếp các field phổ biến
        arrayOf("message", "error", "detail").forEach { k ->
            val v = jsonObj.opt(k)
            if (v is String && v.isNotBlank()) {
                // Map nhanh các thông điệp auth phổ biến
                detectAuthCredentialIssue(v)?.let { return it }
                return v
            }
        }

        // 2) errors: { field: ["msg", ...] } hoặc errors: ["msg1", ...]
        jsonObj.opt("errors")?.let { e ->
            if (e is JSONArray) {
                firstStringFromJsonArray(e)?.let { msg ->
                    detectAuthCredentialIssue(msg)?.let { return it }
                    return msg
                }
            } else if (e is JSONObject) {
                val keys = e.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val node = e.opt(key)
                    if (node is JSONArray) {
                        firstStringFromJsonArray(node)?.let { msg ->
                            detectAuthCredentialIssue(msg)?.let { return it }
                            return msg
                        }
                    } else if (node is String && node.isNotBlank()) {
                        detectAuthCredentialIssue(node)?.let { return it }
                        return node
                    }
                }
            }
        }

        return null
    }

    private fun firstStringFromJsonArray(arr: JSONArray): String? {
        for (i in 0 until arr.length()) {
            val v = arr.opt(i)
            when (v) {
                is String -> if (v.isNotBlank()) return v
                is JSONObject -> {
                    v.optString("message")?.takeIf { it.isNotBlank() }?.let { return it }
                    v.optString("error")?.takeIf { it.isNotBlank() }?.let { return it }
                    v.optString("detail")?.takeIf { it.isNotBlank() }?.let { return it }
                }
            }
        }
        return null
    }

    /** Nhận diện sai email/mật khẩu từ chuỗi (kể cả 400/422) */
    private fun detectAuthCredentialIssue(raw: String): String? {
        val s = raw.lowercase()

        // Mẫu thường gặp
        val patterns = listOf(
            "invalid email or password",
            "invalid credentials",
            "credentials invalid",
            "incorrect password",
            "wrong password",
            "wrong email or password",
            "email or password is incorrect",
            "authentication failed",
            "auth failed"
        )
        if (patterns.any { it in s }) {
            return "Email hoặc mật khẩu không đúng."
        }

        // Regex linh hoạt: (invalid|incorrect|wrong) ... (password|credential)
        val re1 = Regex("(invalid|incorrect|wrong).{0,50}(password|credential)s?", RegexOption.IGNORE_CASE)
        if (re1.containsMatchIn(raw)) return "Email hoặc mật khẩu không đúng."

        // Email không tồn tại
        val re2 = Regex("(email).{0,40}(not found|does not exist|unregistered)", RegexOption.IGNORE_CASE)
        if (re2.containsMatchIn(raw)) return "Tài khoản không tồn tại."

        // Field-level từ errors: "password": ["Sai mật khẩu"]
        if (s.contains("\"password\"") && (s.contains("invalid") || s.contains("incorrect") || s.contains("wrong") || s.contains("sai"))) {
            return "Email hoặc mật khẩu không đúng."
        }

        return null
    }

    /** Nhận diện OTP từ chuỗi thường */
    private fun otpMessage(t: String): String? {
        val s = t.lowercase()
        return when {
            "invalid otp format" in s -> "Mã OTP phải là 6 chữ số. Vui lòng nhập lại."
            "invalid otp" in s -> "Mã OTP không đúng. Vui lòng kiểm tra lại và nhập đúng 6 chữ số."
            "otp expired" in s -> "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới."
            "otp not found" in s -> "Mã OTP không hợp lệ. Vui lòng yêu cầu gửi lại mã mới."
            "too many attempts" in s -> "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu gửi lại mã OTP mới."
            else -> null
        }
    }

    /** Nhận diện lỗi mạng thường gặp */
    private fun detectNetworkIssue(raw: String): String? {
        val t = raw.lowercase()
        return when {
            "unknownhost" in t || "no address associated" in t || "unable to resolve host" in t ->
                "Không có kết nối mạng hoặc máy chủ không xác định."
            "timeout" in t || "sockettimeout" in t ->
                "Kết nối bị quá thời gian. Vui lòng thử lại."
            "econnrefused" in t || "connection refused" in t || "failed to connect" in t ->
                "Không thể kết nối tới máy chủ."
            else -> null
        }
    }

    /** Rút mã HTTP nếu xuất hiện trong chuỗi raw */
    private fun extractStatusCode(raw: String): Int? {
        val re = Regex("""\b(?:HTTP\s*)?([1-5]\d{2})\b""", RegexOption.IGNORE_CASE)
        val m = re.find(raw) ?: return null
        return m.groupValues.getOrNull(1)?.toIntOrNull()
    }

    /** Chọn 1 câu gọn gàng để hiển thị (bỏ stacktrace) */
    private fun pickMeaningfulSentence(raw: String): String? {
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("at ") && !it.contains("Exception") }

        return lines.firstOrNull { it.length in 8..160 }
    }

    // ==== Helpers cũ nếu nơi khác còn dùng ====

    fun isNetworkError(errorMessage: String?): Boolean {
        val m = errorMessage?.lowercase() ?: return false
        return arrayOf("network", "connection", "timeout", "failed to connect", "unable to resolve host")
            .any { it in m }
    }

    fun isAuthError(errorMessage: String?): Boolean {
        val m = errorMessage ?: return false
        return m.contains("401") ||
                m.contains("403") ||
                m.contains("invalid credentials", true) ||
                m.contains("unauthorized", true)
    }
}
