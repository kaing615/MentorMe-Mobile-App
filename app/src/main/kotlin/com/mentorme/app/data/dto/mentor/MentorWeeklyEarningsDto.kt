package com.mentorme.app.data.dto.mentor

import com.google.gson.annotations.SerializedName

data class MentorWeeklyEarningsDto(
    @SerializedName("weeklyEarnings") val weeklyEarnings: List<Long>
)

