package com.mentorme.app.core.network

object NetworkConstants {
    // Thay đổi IP này thành IP máy tính chạy backend của bạn
    // Nếu chạy trên emulator, dùng 10.0.2.2
    // Nếu chạy trên device thật, dùng IP thật của máy tính
    const val BASE_URL = "http://172.72.74.44:4000/api/v1/"

    // Headers
    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val BEARER = "Bearer"

    // Timeout
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
