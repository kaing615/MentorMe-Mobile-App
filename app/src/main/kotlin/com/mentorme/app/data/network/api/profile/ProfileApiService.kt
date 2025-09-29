package com.mentorme.app.data.network.api.profile

import com.mentorme.app.data.dto.profile.ProfileCreateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ProfileApiService {

    @Multipart
    @POST("profile/required")
    suspend fun createRequiredProfileMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<ProfileCreateResponse>
}
