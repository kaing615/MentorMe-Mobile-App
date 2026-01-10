package com.mentorme.app.ui.profile

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs

private val oneDecimalUS = DecimalFormat("#.#").apply {
    decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
}

fun formatMoneyVnd(amount: Long, withCurrency: Boolean = true): String {
    val formatted = java.text.NumberFormat
        .getNumberInstance(Locale("vi", "VN"))
        .format(amount)

    return if (withCurrency) "$formatted đ" else formatted
}

enum class TxType { TOP_UP, WITHDRAW, PAYMENT, EARN, REFUND }
enum class TxStatus { PENDING, SUCCESS, FAILED }

data class WalletTx(
    val id: String,
    val date: Long,
    val type: TxType,
    val amount: Long,     // + vào ví, - ra ví (VND)
    val note: String,
    val status: TxStatus
)

enum class UserRole { MENTEE, MENTOR }

data class UserHeader(
    val fullName: String,
    val email: String,
    val role: UserRole
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val avatar: String? = null,

    // ✅ NEW: Mentor-specific fields
    val jobTitle: String? = null,       // Chức danh công việc
    val headline: String? = null,       // Tiêu đề ngắn
    val category: String? = null,       // Chuyên mục
    val hourlyRate: Int? = null,        // Giá mỗi giờ (VND)
    val experience: String? = null,     // Kinh nghiệm
    val education: String? = null,      // Học vấn

    val joinDate: Date,
    val totalSessions: Int,
    val totalSpent: Long,
    val interests: List<String>,        // Skills
    val preferredLanguages: List<String>
)

fun formatCurrencyVnd(amount: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return nf.format(amount)
}

fun formatDateVi(date: Date): String {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("vi", "VN"))
    return sdf.format(date)
}

fun mockProfile(user: UserHeader): UserProfile {
    return if (user.role == UserRole.MENTOR) {
        UserProfile(
            id = "1",
            fullName = user.fullName,
            email = user.email,
            phone = "+84 123 456 789",
            location = "Hồ Chí Minh, Việt Nam",
            bio = "Senior Developer với 8+ năm kinh nghiệm trong lĩnh vực Frontend và Backend.Đã mentor cho 100+ mentees và giúp họ phát triển sự nghiệp trong công nghệ.",
            joinDate = GregorianCalendar(2022, Calendar.MARCH, 15).time,
            totalSessions = 156,
            totalSpent = 0L,
            interests = listOf("React", "Node.js", "System Design", "Leadership", "Career Coaching"),
            preferredLanguages = listOf("Tiếng Việt", "English")
        )
    } else {
        UserProfile(
            id = "1",
            fullName = user.fullName,
            email = user.email,
            phone = "+84 123 456 789",
            location = "Hồ Chí Minh, Việt Nam",
            bio = "Tôi là một developer đang học hỏi và phát triển kỹ năng trong lĩnh vực technology.Mong muốn được học hỏi từ những mentor giỏi để có thể phát triển sự nghiệp.",
            joinDate = GregorianCalendar(2023, Calendar.JUNE, 15).time,
            totalSessions = 12,
            totalSpent = 8_500_000L,
            interests = listOf("React", "Node.js", "AI/ML", "Product Management"),
            preferredLanguages = listOf("Tiếng Việt", "English")
        )
    }
}
