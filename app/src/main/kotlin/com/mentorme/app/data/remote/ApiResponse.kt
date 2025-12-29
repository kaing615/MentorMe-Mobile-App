package com.mentorme.app.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T
)

