package com.mentorme.app.core.network

import com.mentorme.app.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Response
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking {
            dataStoreManager.getToken().first()
        }

        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Create a new request builder
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        // If the request has a body, we need to ensure the Content-Type is exactly "application/json"
        originalRequest.body?.let { body ->
            val mediaType = "application/json".toMediaType()
            val buffer = Buffer()
            body.writeTo(buffer)
            val requestBody = buffer.readByteArray()
            val newBody = requestBody.toRequestBody(mediaType)
            requestBuilder.method(originalRequest.method, newBody)

            // Log the request details for debugging
            android.util.Log.d("ApiKeyInterceptor", "Request URL: ${originalRequest.url}")
            android.util.Log.d("ApiKeyInterceptor", "Request Method: ${originalRequest.method}")
            android.util.Log.d("ApiKeyInterceptor", "Request Body: ${String(requestBody)}")
            android.util.Log.d("ApiKeyInterceptor", "Content-Type: application/json")
        }

        val newRequest = requestBuilder.build()
        val response = chain.proceed(newRequest)

        // Log response details for debugging
        if (!response.isSuccessful) {
            android.util.Log.e("ApiKeyInterceptor", "Response Code: ${response.code}")
            android.util.Log.e("ApiKeyInterceptor", "Response Message: ${response.message}")
            response.body?.let { responseBody ->
                val responseString = responseBody.string()
                android.util.Log.e("ApiKeyInterceptor", "Response Body: $responseString")
                // Create a new response body since we consumed the original
                val newResponseBody = responseString.toResponseBody(responseBody.contentType())
                return response.newBuilder().body(newResponseBody).build()
            }
        }

        return response
    }
}
