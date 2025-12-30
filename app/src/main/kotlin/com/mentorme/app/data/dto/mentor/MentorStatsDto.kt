package com.mentorme.app.data.dto.mentor

import com.google.gson.annotations.SerializedName

data class MentorStatsDto(
    @SerializedName("earnings") val earnings: Long,
    @SerializedName("menteeCount") val menteeCount: Int,
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("totalHours") val totalHours: Double
)

