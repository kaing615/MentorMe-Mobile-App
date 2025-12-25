package com.mentorme.app.data.repository.home

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.home.HomeStatsData
import com.mentorme.app.data.network.api.home.HomeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val homeApiService: HomeApiService
) : HomeRepository {

    override suspend fun getHomeStats(): AppResult<HomeStatsData> = withContext(Dispatchers.IO) {
        try {
            val response = homeApiService.getHomeStats()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    AppResult.success(body.data)
                } else {
                    AppResult.failure("Invalid response from server")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                AppResult.failure("HTTP ${response.code()}: ${errorBody ?: response.message()}")
            }
        } catch (e: Exception) {
            AppResult.failure(e.message ?: "Failed to get home stats")
        }
    }

    override suspend fun pingPresence(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = homeApiService.pingPresence()
            if (response.isSuccessful) {
                AppResult.success(Unit)
            } else {
                // Non-critical, just log
                android.util.Log.w("HomeRepo", "Presence ping failed: ${response.code()}")
                AppResult.success(Unit) // Don't fail the app
            }
        } catch (e: Exception) {
            android.util.Log.w("HomeRepo", "Presence ping error: ${e.message}")
            AppResult.success(Unit) // Non-blocking
        }
    }
}

