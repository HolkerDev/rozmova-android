package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ReviewDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatClient {
    @POST("chats")
    suspend fun createChat(
        @Body body: ChatCreateReq,
    ): Response<ChatDto>

    @GET("chats/{chatId}")
    suspend fun fetchChatById(
        @Path("chatId") chatId: String,
    ): Response<ChatDto>

    @GET("chats")
    suspend fun fetchAll(): Response<List<ChatDto>>

    @DELETE("chats/{chatId}")
    suspend fun deleteById(
        @Path("chatId") chatId: String,
    ): Response<List<ChatDto>>

    @POST("chats/{chatId}/finish")
    suspend fun finish(
        @Path("chatId") chatId: String,
    ): Response<FinishChatRes>
}

data class ChatCreateReq(
    val scenarioId: String,
)

data class FinishChatRes(
    val chat: ChatDto,
    val review: ReviewDto,
)
