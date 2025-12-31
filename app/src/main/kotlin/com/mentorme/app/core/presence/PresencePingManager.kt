package com.mentorme.app.core.presence

import com.mentorme.app.core.appstate.AppForegroundTracker
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.domain.usecase.home.PingPresenceUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class PresencePingManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val pingPresenceUseCase: PingPresenceUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var watchJob: Job? = null
    private var pingJob: Job? = null

    fun start() {
        if (watchJob != null) return
        watchJob = scope.launch {
            combine(
                AppForegroundTracker.isForeground,
                dataStoreManager.getToken()
            ) { isForeground, token ->
                isForeground && !token.isNullOrBlank()
            }.distinctUntilChanged().collect { shouldPing ->
                if (shouldPing) {
                    startPingLoop()
                } else {
                    stopPingLoop()
                }
            }
        }
    }

    fun stop() {
        watchJob?.cancel()
        watchJob = null
        stopPingLoop()
    }

    private fun startPingLoop() {
        if (pingJob?.isActive == true) return
        pingJob = scope.launch {
            pingPresenceUseCase()
            while (isActive) {
                delay(PING_INTERVAL_MS)
                pingPresenceUseCase()
            }
        }
    }

    private fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    companion object {
        private const val PING_INTERVAL_MS = 90_000L
    }
}
