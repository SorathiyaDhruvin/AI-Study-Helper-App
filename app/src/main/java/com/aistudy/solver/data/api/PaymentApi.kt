package com.aistudy.solver.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class OrderRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String = "INR"
)

data class OrderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String
)

data class VerificationRequest(
    @SerializedName("razorpay_order_id") val orderId: String,
    @SerializedName("razorpay_payment_id") val paymentId: String,
    @SerializedName("razorpay_signature") val signature: String
)

data class VerificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

interface PaymentApi {
    @POST("create-order")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @POST("verify-payment")
    suspend fun verifyPayment(@Body request: VerificationRequest): Response<VerificationResponse>
}
