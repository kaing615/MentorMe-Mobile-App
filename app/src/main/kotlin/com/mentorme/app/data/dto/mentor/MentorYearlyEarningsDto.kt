package com.mentorme.app.data.dto.mentor

import com.google.gson.annotations.SerializedName

data class MentorYearlyEarningsDto(
    @SerializedName("yearlyEarnings") val yearlyEarnings: List<Long>,
    @SerializedName("year") val year: Int
)

