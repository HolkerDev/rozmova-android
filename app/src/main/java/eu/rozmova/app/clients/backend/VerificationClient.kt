package eu.rozmova.app.clients.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationClient {
    @POST("v1/verification/subscription")
    suspend fun verifyToken(
        @Body body: VerifySubscriptionReq,
    ): Response<VerifySubscriptionResp>

    @POST("v1/verification/vip")
    suspend fun verifyVIP(): Response<VerifyVIPResp>
}

data class VerifySubscriptionReq(
    val purchaseToken: String,
)

data class VerifySubscriptionResp(
    val isSubscribed: Boolean,
)

data class VerifyVIPResp(
    val isVIP: Boolean,
)
