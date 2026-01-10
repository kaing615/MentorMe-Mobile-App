package com.mentorme.app.data.mapper

enum class PayoutUiStatus(
    val label: String
) {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    PROCESSING("Đang xử lý"),
    PAID("Thành công"),
    FAILED("Thất bại"),
    CANCELLED("Đã huỷ");

    companion object {
        fun from(raw: String): PayoutUiStatus =
            values().firstOrNull { it.name == raw } ?: PENDING
    }
}
