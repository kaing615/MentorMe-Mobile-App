package com.mentorme.app.ui.home

/**
 * Format number to compact string with + suffix
 * Examples:
 * 1234 -> "1.2k+"
 * 10000 -> "10k+"
 * 523 -> "500+"
 * 95 -> "95+"
 */
fun formatCompactNumber(num: Int): String {
    return when {
        num >= 10_000 -> "${num / 1000}k+"
        num >= 1_000 -> "%.1fk+".format(num / 1000.0).replace(".0k", "k")
        num >= 100 -> "${(num / 100) * 100}+"
        else -> "$num+"
    }
}

