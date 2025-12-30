package com.mentorme.app.data.remote

import com.mentorme.app.data.dto.paymentMethods.AddPaymentMethodRequest
import com.mentorme.app.data.dto.paymentMethods.PaymentMethodDto
import com.mentorme.app.data.dto.paymentMethods.UpdatePaymentMethodRequest
import com.mentorme.app.ui.wallet.PaymentMethod
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PaymentMethodApi {

    // ✅ GET /payment-methods/me
    @GET("payment-methods/me")
    suspend fun getMethods(): ApiResponse<List<PaymentMethodDto>>

    // ✅ POST /payment-methods
    @POST("payment-methods")
    suspend fun addMethod(
        @Body body: AddPaymentMethodRequest
    ): ApiResponse<PaymentMethod>

    // ✅ PUT /payment-methods/:id
    @PUT("payment-methods/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdatePaymentMethodRequest
    ): ApiResponse<PaymentMethod>

    // ✅ DELETE /payment-methods/:id
    @DELETE("payment-methods/{id}")
    suspend fun delete(
        @Path("id") id: String
    ): ApiResponse<Unit>
}
