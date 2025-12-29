package com.mentorme.app.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatRestrictionInfo(
    @SerializedName("bookingId")
    val bookingId: String,
    @SerializedName("bookingStatus")
    val bookingStatus: String,
    @SerializedName("isConfirmed")
    val isConfirmed: Boolean,
    @SerializedName("sessionPhase")
    val sessionPhase: String, // pre, during, post, outside
    @SerializedName("myMessageCount")
    val myMessageCount: Int,
    @SerializedName("preSessionCount")
    val preSessionCount: Int,
    @SerializedName("postSessionCount")
    val postSessionCount: Int,
    @SerializedName("weeklyMessageCount")
    val weeklyMessageCount: Int,
    @SerializedName("limits")
    val limits: ChatLimits
)

data class ChatLimits(
    @SerializedName("maxFreeMessages")
    val maxFreeMessages: Int,
    @SerializedName("preSessionLimit")
    val preSessionLimit: Int,
    @SerializedName("postSessionLimit")
    val postSessionLimit: Int,
    @SerializedName("weeklyLimit")
    val weeklyLimit: Int,
    @SerializedName("sessionWindowDays")
    val sessionWindowDays: Int
)
