package com.mentorme.app.data.dto

import com.google.gson.annotations.SerializedName

data class MenteeStatsDto(
    @SerializedName("totalSessions") val totalSessions: Int,
    @SerializedName("totalSpent") val totalSpent: Long
)

