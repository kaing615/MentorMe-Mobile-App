package com.mentorme.app.data.dto.mentor

import com.google.gson.annotations.SerializedName

data class MentorOverallStatsDto(
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("totalMentees") val totalMentees: Int,
    @SerializedName("totalHours") val totalHours: Double
)

