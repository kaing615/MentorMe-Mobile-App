package com.mentorme.app.core.network

import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthFailureInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val hadAuthHeader = !request.header("Authorization").isNullOrBlank()
        if (hadAuthHeader && response.code == 401) {
            runBlocking {
                dataStoreManager.clearToken()
                dataStoreManager.clearUserInfo()
            }
            SessionManager.clear()
        }

        return response
    }
}
