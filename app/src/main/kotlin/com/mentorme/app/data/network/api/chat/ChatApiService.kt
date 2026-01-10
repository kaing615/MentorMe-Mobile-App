package com.mentorme.app.data.network.api.chat

import com.mentorme.app.data.dto.Message
import com.mentorme.app.data.dto.SendMessageRequest
import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.model.chat.ChatRestrictionInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class FileUploadResponse(
    val url: String,
    val publicId: String,
    val fileType: String, // "image" or "file"
    val format: String,
    val size: Long,
    val originalName: String
)

interface ChatApiService {
    @GET("messages/{bookingId}")
    suspend fun getMessages(
        @Path("bookingId") bookingId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<ApiEnvelope<List<Message>>>

    @GET("messages/peer/{peerId}")
    suspend fun getMessagesByPeer(
        @Path("peerId") peerId: String,
        @Query("limit") limit: Int = 200,
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
    
    @Multipart
    @POST("messages/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("bookingId") bookingId: RequestBody
    ): Response<ApiEnvelope<FileUploadResponse>>
}
