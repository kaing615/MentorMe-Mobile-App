package com.mentorme.app.data.network.api.chat

import com.mentorme.app.data.dto.Message
import com.mentorme.app.data.dto.SendMessageRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {
    @GET("messages/{bookingId}")
    suspend fun getMessages(
        @Path("bookingId") bookingId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<ApiEnvelope<List<Message>>>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ApiEnvelope<Message>>
    
    @GET("messages/{bookingId}/restriction-info")
    suspend fun getChatRestrictionInfo(
        @Path("bookingId") bookingId: String
    ): Response<ApiEnvelope<ChatRestrictionInfo>>
}
