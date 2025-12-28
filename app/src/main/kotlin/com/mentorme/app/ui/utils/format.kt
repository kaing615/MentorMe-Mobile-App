package com.mentorme.app.ui.utils

/**
 * Format VND gọn: 1_500_000 -> 1.5M, 900_000 -> 900k.
 * Nếu dữ liệu đang để "đơn vị nghìn" (vd 500 nghĩa là 500k) thì tự nhân 1_000.
 */
fun Int.compactVnd(): String {
    val vnd = if (this < 10_000) this * 1_000 else this  // hỗ trợ cả dữ liệu kiểu 500 (=500k)
    return when {
        vnd >= 1_000_000 -> {
            val v = vnd / 1_000_000f
            val s = if (v % 1f == 0f) v.toInt().toString() else String.format("%.1f", v)
            s.trimEnd('0').trimEnd('.') + "M"
        }
        vnd >= 1_000 -> {
            val v = vnd / 1_000f
            val s = if (v % 1f == 0f) v.toInt().toString() else String.format("%.0f", v)
            s + "k"
        }
        else -> vnd.toString()
    }
}
