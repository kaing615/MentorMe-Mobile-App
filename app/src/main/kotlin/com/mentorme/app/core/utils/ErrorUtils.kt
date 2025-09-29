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

        // Extract JSON from HTTP error format like "HTTP 400: {...}"
        val jsonStart = trimmed.indexOf("{")
        val actualJson = if (jsonStart > 0) trimmed.substring(jsonStart) else trimmed

        val jsonObj: JSONObject? = try {
            when {
                actualJson.startsWith("{") -> JSONObject(actualJson)
                actualJson.startsWith("[") -> {
                    val arr = JSONArray(actualJson)
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
                // Map validation errors to Vietnamese
                translateValidationError(v)?.let { return it }
                // Don't return raw message directly, process it first
                val processedMessage = processServerMessage(v)
                if (processedMessage.isNotBlank()) return processedMessage
            }
        }

        // 2) Check data.missing array for specific missing fields
        jsonObj.optJSONObject("data")?.let { data ->
            data.optJSONArray("missing")?.let { missingArray ->
                val missingFields = mutableListOf<String>()
                for (i in 0 until missingArray.length()) {
                    val field = missingArray.optString(i)
                    if (field.isNotBlank()) {
                        missingFields.add(field)
                    }
                }
                if (missingFields.isNotEmpty()) {
                    return generateMissingFieldsMessage(missingFields)
                }
            }
        }

        // 3) errors: { field: ["msg", ...] } hoặc errors: ["msg1", ...]
        jsonObj.opt("errors")?.let { e ->
            if (e is JSONArray) {
                firstStringFromJsonArray(e)?.let { msg ->
                    detectAuthCredentialIssue(msg)?.let { return it }
                    return processServerMessage(msg)
                }
            } else if (e is JSONObject) {
                val keys = e.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val node = e.opt(key)
                    if (node is JSONArray) {
                        firstStringFromJsonArray(node)?.let { msg ->
                            detectAuthCredentialIssue(msg)?.let { return it }
                            return processServerMessage(msg)
                        }
                    } else if (node is String && node.isNotBlank()) {
                        detectAuthCredentialIssue(node)?.let { return it }
                        return processServerMessage(node)
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

    /** Translate common validation errors to Vietnamese */
    private fun translateValidationError(message: String): String? {
        val msg = message.lowercase()
        return when {
            msg.contains("missing required fields") -> {
                // Parse specific missing fields from the message
                when {
                    msg.contains("avatar") || msg.contains("avatarurl") ->
                        "Vui lòng chọn ảnh đại diện để tiếp tục."
                    msg.contains("fullname") ->
                        "Vui lòng nhập họ và tên."
                    msg.contains("location") ->
                        "Vui lòng nhập địa điểm."
                    msg.contains("category") ->
                        "Vui lòng nhập lĩnh vực chuyên môn."
                    msg.contains("languages") ->
                        "Vui lòng nhập ngôn ngữ."
                    msg.contains("skills") ->
                        "Vui lòng nhập kỹ năng."
                    msg.contains("jobtitle") ->
                        "Vui lòng nhập chức danh hiện tại."
                    msg.contains("experience") ->
                        "Vui lòng nhập kinh nghiệm."
                    msg.contains("headline") ->
                        "Vui lòng nhập tiêu đề giới thiệu."
                    msg.contains("description") ->
                        "Vui lòng nhập mô tả bản thân."
                    msg.contains("goal") ->
                        "Vui lòng nhập mục tiêu."
                    msg.contains("education") ->
                        "Vui lòng nhập học vấn."
                    else -> "Vui lòng điền đầy đủ thông tin bắt buộc."
                }
            }
            msg.contains("profile already exists") ->
                "Hồ sơ đã tồn tại. Không thể tạo mới."
            msg.contains("user not found") ->
                "Không tìm thấy thông tin người dùng."
            msg.contains("avatar must be an image") ->
                "Ảnh đại diện phải là định dạng hình ảnh hợp lệ."
            msg.contains("invalid url") ->
                "URL không hợp lệ. Vui lòng kiểm tra lại."
            msg.contains("introvideo must be a valid url") ->
                "Link video giới thiệu không hợp lệ. Vui lòng kiểm tra lại hoặc để trống."
            msg.contains("must be a valid url") ->
                "Đường dẫn không hợp lệ. Vui lòng kiểm tra lại."
            else -> null
        }
    }

    /** Generate user-friendly message for missing fields */
    private fun generateMissingFieldsMessage(missingFields: List<String>): String {
        val translatedFields = missingFields.map { field ->
            when (field.lowercase()) {
                "avatar (file hoặc avatarurl)", "avatar", "avatarurl" -> "ảnh đại diện"
                "fullname" -> "họ và tên"
                "location" -> "địa điểm"
                "category" -> "lĩnh vực chuyên môn"
                "languages" -> "ngôn ngữ"
                "skills" -> "kỹ năng"
                "jobtitle" -> "chức danh"
                "experience" -> "kinh nghiệm"
                "headline" -> "tiêu đề giới thiệu"
                "description" -> "mô tả bản thân"
                "goal" -> "mục tiêu"
                "education" -> "học vấn"
                "mentorreason" -> "lý do muốn làm mentor"
                "greatestachievement" -> "thành tựu nổi bật"
                else -> field.lowercase()
            }
        }

        return when {
            translatedFields.size == 1 -> "Vui lòng nhập ${translatedFields[0]}."
            translatedFields.size == 2 -> "Vui lòng nhập ${translatedFields[0]} và ${translatedFields[1]}."
            translatedFields.size > 2 -> {
                val lastField = translatedFields.last()
                val otherFields = translatedFields.dropLast(1).joinToString(", ")
                "Vui lòng nhập $otherFields và $lastField."
            }
            else -> "Vui lòng điền đầy đủ thông tin bắt buộc."
        }
    }

    /** Process and clean up server messages */
    private fun processServerMessage(message: String): String {
        val msg = message.trim()

        // Skip if it contains technical details that shouldn't be shown to users
        if (msg.contains("HTTP ") ||
            msg.contains("Exception") ||
            msg.contains("stacktrace") ||
            msg.contains("internal server") ||
            msg.contains("database") ||
            msg.startsWith("{") ||
            msg.startsWith("[")) {
            return ""
        }

        // Clean up common server message patterns
        return when {
            msg.contains("Missing required fields", ignoreCase = true) -> {
                translateValidationError(msg) ?: "Vui lòng điền đầy đủ thông tin bắt buộc."
            }
            msg.contains("invalid", ignoreCase = true) && msg.contains("format", ignoreCase = true) -> {
                "Định dạng dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
            }
            msg.contains("already exists", ignoreCase = true) -> {
                "Dữ liệu đã tồn tại trong hệ thống."
            }
            msg.contains("not found", ignoreCase = true) -> {
                "Không tìm thấy thông tin yêu cầu."
            }
            msg.contains("access denied", ignoreCase = true) || msg.contains("unauthorized", ignoreCase = true) -> {
                "Bạn không có quyền thực hiện thao tác này."
            }
            msg.contains("too many", ignoreCase = true) -> {
                "Bạn đã thực hiện quá nhiều lần. Vui lòng thử lại sau."
            }
            msg.length > 200 -> {
                // If message is too long, try to extract meaningful part
                val sentences = msg.split(".", "!", "?").filter { it.trim().isNotEmpty() }
                sentences.firstOrNull { it.length in 10..100 }?.trim() ?: "Có lỗi xảy ra. Vui lòng thử lại."
            }
            else -> msg
        }
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
