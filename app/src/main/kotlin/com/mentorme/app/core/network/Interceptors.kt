package com.mentorme.app.core.network

import com.mentorme.app.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Lấy token hiện tại (blocking ngắn trên nền tảng OkHttp thread)
        val token = runBlocking { dataStoreManager.getToken().first() }
        android.util.Log.d("AuthInterceptor", "📦 Token in DataStore: $token")

        // Không có token -> đi tiếp như cũ
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Có token -> gắn Authorization header
        val authed = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authed)
    }
}

@Singleton
class ApiKeyInterceptor @Inject constructor(
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // CHỈ log – KHÔNG chỉnh sửa body/header để không làm mất Authorization
        val req = chain.request()

        // Nếu muốn chấp nhận JSON từ server
        val newReq = req.newBuilder()
            .header("Accept", "application/json")
            .build()

        android.util.Log.d("ApiKeyInterceptor", "➡️ ${req.method} ${req.url}")
        android.util.Log.d("ApiKeyInterceptor", "Authorization header: ${req.header("Authorization")}")
        req.body?.contentType()?.let { ct ->
            android.util.Log.d("ApiKeyInterceptor", "Content-Type: $ct")
        }

        val res = chain.proceed(newReq)

        if (!res.isSuccessful) {
            android.util.Log.e("ApiKeyInterceptor", "⬅️ ${res.code} ${res.message}")
            // Đọc body để log rồi bọc lại cho downstream đọc tiếp
            val raw = res.body
            if (raw != null) {
                val str = raw.string()
                android.util.Log.e("ApiKeyInterceptor", "Response Body: $str")
                return res.newBuilder()
                    .body(okhttp3.ResponseBody.create(raw.contentType(), str))
                    .build()
            }
        } else {
            android.util.Log.d("ApiKeyInterceptor", "⬅️ ${res.code} ${res.message}")
        }
        return res
    }
}