package eu.rozmova.app.clients

import eu.rozmova.app.domain.ChatDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatClient {
    @POST("chats")
    suspend fun createChat(
        @Body body: ChatCreateReq,
    ): Response<ChatFetchRes>

    @GET("chats/{chatId}")
    suspend fun fetchChatById(
        @Path("chatId")chatId: String,
    ): Response<ChatDto>
}

data class ChatCreateReq(
    val scenarioId: String,
)

data class ChatFetchRes(
    val chat: ChatDto,
)
