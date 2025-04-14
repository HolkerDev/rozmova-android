package eu.rozmova.app.clients

import eu.rozmova.app.domain.ChatDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatClient {
    @POST("chats")
    suspend fun createChat(
        @Body body: ChatCreateReq,
    ): Response<ChatCreateRes>
}

data class ChatCreateReq(
    val scenarioId: String,
)

data class ChatCreateRes(
    val chat: ChatDto,
)
