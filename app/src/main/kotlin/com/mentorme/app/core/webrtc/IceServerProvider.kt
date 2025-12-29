package com.mentorme.app.core.webrtc

import com.mentorme.app.BuildConfig
import org.webrtc.PeerConnection

object IceServerProvider {
    fun defaultIceServers(): List<PeerConnection.IceServer> {
        val servers = mutableListOf<PeerConnection.IceServer>()

        val stunUrl = BuildConfig.WEBRTC_STUN_URL.trim()
        if (stunUrl.isNotEmpty()) {
            servers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer())
        }

        val turnUrl = BuildConfig.WEBRTC_TURN_URL.trim()
        if (turnUrl.isNotEmpty()) {
            val builder = PeerConnection.IceServer.builder(turnUrl)
            val username = BuildConfig.WEBRTC_TURN_USERNAME.trim()
            val password = BuildConfig.WEBRTC_TURN_PASSWORD.trim()
            if (username.isNotEmpty() || password.isNotEmpty()) {
                builder.setUsername(username).setPassword(password)
            }
            servers.add(builder.createIceServer())
        }

        return servers
    }
}
