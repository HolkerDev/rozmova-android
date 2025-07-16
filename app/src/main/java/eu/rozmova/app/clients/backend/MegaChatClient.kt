package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ReviewDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MegaChatClient {
    @GET("v1/chats")
    suspend fun listChats(
        @Query("userLang") userLang: String,
        @Query("scenarioLang") scenarioLang: String,
    ): Response<ListChatsResp>

    @GET("v1/chats/latest")
    suspend fun getLatest(
        @Query("userLang") userLang: String,
        @Query("scenarioLang") scenarioLang: String,
    ): Response<GetLatestChatResp>

    @POST("v1/chats/messages/audio")
    suspend fun sendAudioMessage(
        @Body body: SendAudioReq,
    ): Response<SendAudioRes>

    @POST("v1/chats/{chatId}/review")
    suspend fun review(
        @Path("chatId") chatId: String,
    ): Response<ReviewChatResp>

    @GET("v1/chats/{reviewId}/review")
    suspend fun getReview(
        @Path("reviewId") reviewId: String,
    ): Response<GetReviewResp>

    @GET("v1/chats/reviews")
    suspend fun getReviews(): Response<GetReviewsResp>

    @POST("v1/chats")
    suspend fun createChat(
        @Body body: CreateChatReq,
    ): Response<CreateChatResp>
}

data class GetReviewsResp(
    val reviews: List<ReviewDto>,
)

data class GetReviewResp(
    val review: ReviewDto,
)

data class ReviewChatResp(
    val reviewId: String,
)

data class GetLatestChatResp(
    val chat: ChatDto,
)

data class CreateChatReq(
    val scenarioId: String,
    val chatType: String,
)

data class CreateChatResp(
    val chatId: String,
)

data class ListChatsResp(
    val chats: List<ChatDto>,
)

data class SendAudioReq(
    val chatId: String,
    val audioId: String,
    val pronoun: String,
)

data class SendAudioRes(
    val chat: ChatDto,
    val shouldFinish: Boolean,
)
