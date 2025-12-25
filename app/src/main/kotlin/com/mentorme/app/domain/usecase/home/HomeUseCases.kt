package com.mentorme.app.domain.usecase.home

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.dto.home.HomeStatsData
import com.mentorme.app.data.repository.home.HomeRepository
import javax.inject.Inject

class GetHomeStatsUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): AppResult<HomeStatsData> {
        return homeRepository.getHomeStats()
    }
}

class PingPresenceUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return homeRepository.pingPresence()
    }
}

