package com.mentorme.app.data.network.api.session

import com.mentorme.app.data.dto.availability.ApiEnvelope
import com.mentorme.app.data.dto.session.SessionJoinResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApiService {
    @POST("sessions/{bookingId}/join-token")
    suspend fun createJoinToken(
        @Path("bookingId") bookingId: String
    ): Response<ApiEnvelope<SessionJoinResponse>>
}
