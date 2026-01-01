package com.mentorme.app.data.dto.home

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /home/stats
 */
data class HomeStatsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: HomeStatsData?
)

data class HomeStatsData(
    @SerializedName("mentorCount")
    val mentorCount: Int,
    @SerializedName("sessionCount")
    val sessionCount: Int,
    @SerializedName("avgRating")
    val avgRating: Double,
    @SerializedName("onlineCount")
    val onlineCount: Int
)

/**
 * Request/Response for POST /presence/ping
 */
data class PresencePingResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: PresencePingData?
)

data class PresencePingData(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("expiresIn")
    val expiresIn: Int
)

/**
 * Request/Response for POST /presence/lookup
 */
data class PresenceLookupRequest(
    @SerializedName("userIds")
    val userIds: List<String>
)

data class PresenceLookupResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: PresenceLookupData?
)

data class PresenceLookupData(
    @SerializedName("onlineUserIds")
    val onlineUserIds: List<String>
)

