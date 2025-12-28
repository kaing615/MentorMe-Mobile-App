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

/** 1_000 -> 1K, 1_000_000 -> 1M. Giữ 1 chữ số thập phân, bỏ .0 nếu không cần. */
fun formatMoneyShortVnd(amount: Long, withCurrency: Boolean = false): String {
    val absAmount = abs(amount)
    val sign = if (amount < 0) "-" else ""
    val text = when {
        absAmount >= 1_000_000 -> sign + oneDecimalUS.format(absAmount / 1_000_000.0) + "M"
        absAmount >= 1_000 -> sign + oneDecimalUS.format(absAmount / 1_000.0) + "K"
        else -> sign + NumberFormat.getNumberInstance(Locale("vi", "VN")).format(absAmount)
    }
    return if (withCurrency) "$text ₫" else text
}

enum class TxType { TOP_UP, WITHDRAW, PAYMENT, REFUND }
enum class TxStatus { PENDING, SUCCESS, FAILED }

data class WalletTx(
    val id: String,
    val date: Date,
    val type: TxType,
    val amount: Long,     // + vào ví, - ra ví (VND)
    val note: String,
    val status: TxStatus
)

fun mockTx(): List<WalletTx> = listOf(
    WalletTx("t1", GregorianCalendar(2025, 8, 21, 10, 15).time, TxType.TOP_UP, +300_000, "Nạp MoMo", TxStatus.SUCCESS),
    WalletTx("t2", GregorianCalendar(2025, 8, 20, 14, 0).time, TxType.PAYMENT, -850_000, "Thanh toán buổi 60’", TxStatus.SUCCESS),
    WalletTx("t3", GregorianCalendar(2025, 8, 19, 9, 30).time, TxType.WITHDRAW, -500_000, "Rút về ngân hàng", TxStatus.PENDING),
    WalletTx("t4", GregorianCalendar(2025, 8, 18, 16, 45).time, TxType.REFUND, +120_000, "Hoàn tiền phí", TxStatus.SUCCESS),
    WalletTx("t5", GregorianCalendar(2025, 8, 17, 11, 5).time, TxType.PAYMENT, -450_000, "Thanh toán buổi 30’", TxStatus.FAILED)
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
    val avatar: String? = null, // if you add Coil later
    val joinDate: Date,
    val totalSessions: Int,
    val totalSpent: Long,
    val interests: List<String>,
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
