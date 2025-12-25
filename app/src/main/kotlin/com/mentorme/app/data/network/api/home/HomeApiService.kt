package com.mentorme.app.data.network.api.home

import com.mentorme.app.data.dto.home.HomeStatsResponse
import com.mentorme.app.data.dto.home.PresencePingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface HomeApiService {

    /**
     * Get home screen statistics
     * Public endpoint - no auth required
     */
    @GET("home/stats")
    suspend fun getHomeStats(): Response<HomeStatsResponse>

    /**
     * Ping user presence (auth required)
     * Sets presence key in Redis with 120s TTL
     */
    @POST("presence/ping")
    suspend fun pingPresence(): Response<PresencePingResponse>
}

