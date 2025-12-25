package com.mentorme.app.data.repository.home

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.home.HomeStatsData

interface HomeRepository {
    suspend fun getHomeStats(): AppResult<HomeStatsData>
    suspend fun pingPresence(): AppResult<Unit>
}

