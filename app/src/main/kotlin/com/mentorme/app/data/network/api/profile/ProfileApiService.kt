package com.mentorme.app.data.network.api.profile

import com.mentorme.app.data.dto.common.ApiResponse
import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import com.mentorme.app.data.dto.profile.ProfileDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @Multipart
    @POST("profile/required")
    suspend fun createRequiredProfileMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<ProfileCreateResponse>

    @GET("profile/me")
    suspend fun getMe(): Response<ApiResponse<MePayload>>

    @Multipart
    @PATCH("profile")
    suspend fun updateProfile(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<ApiResponse<ProfileDto>>
}
