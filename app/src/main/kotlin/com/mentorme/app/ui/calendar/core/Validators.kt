package com.mentorme.app.ui.calendar.core

/* ---- Date: digits "ddMMyyyy" -> ISO "yyyy-MM-dd" (null nếu không hợp lệ) ---- */
fun validateDateDigitsReturnIso(d: String): String? {
    if (d.length != 8 || d.any { !it.isDigit() }) return null
    val day = d.substring(0, 2).toInt()
    val mon = d.substring(2, 4).toInt()
    val yr  = d.substring(4, 8).toInt()
    if (yr !in 1900..2100 || mon !in 1..12) return null
    val dim = daysInMonth(mon, yr)
    if (day !in 1..dim) return null
    return "%04d-%02d-%02d".format(yr, mon, day)
}

private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
private fun daysInMonth(m: Int, y: Int): Int = when (m) {
    1,3,5,7,8,10,12 -> 31
    4,6,9,11 -> 30
    2 -> if (isLeap(y)) 29 else 28
    else -> 0
}

/* ---- Time: digits "HHmm" -> "HH:mm" (null nếu không hợp lệ) ---- */
fun validateTimeDigitsReturnHHMM(d: String): String? {
    if (d.length != 4 || d.any { !it.isDigit() }) return null
    val h = d.substring(0, 2).toInt()
    val m = d.substring(2, 4).toInt()
    if (h !in 0..23 || m !in 0..59) return null
    return "%02d:%02d".format(h, m)
}

/* ---- Duration: từ 2 chuỗi digits "HHmm" -> phút dương (null nếu âm/không hợp lệ) ---- */
private fun toMinutesFromDigits(d: String): Int {
    val h = d.substring(0, 2).toInt()
    val m = d.substring(2, 4).toInt()
    return h * 60 + m
}
fun durationFromDigits(startD: String, endD: String): Int? {
    if (startD.length != 4 || endD.length != 4) return null
    val diff = toMinutesFromDigits(endD) - toMinutesFromDigits(startD)
    return if (diff > 0) diff else null
}
