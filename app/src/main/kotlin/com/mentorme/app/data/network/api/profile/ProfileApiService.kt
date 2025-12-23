package com.mentorme.app.data.network.api.profile

import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import com.mentorme.app.data.dto.profile.MePayload
import com.mentorme.app.domain.usecase.profile.UpdateProfileParams
import retrofit2.http.*

interface ProfileApiService {

    @Multipart
    @POST("profile/required")
    suspend fun createRequiredProfileMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<ProfileCreateResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiEnvelope<MePayload>>

    @PUT("profile/me")
    suspend fun updateProfile(
        @Body params: UpdateProfileParams
    ): Response<ApiEnvelope<Unit>>

    @Multipart
    @PUT("profile/me")
    suspend fun updateProfileMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<ApiEnvelope<Unit>>
}
