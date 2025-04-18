package eu.rozmova.app.clients

import eu.rozmova.app.domain.ChatDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageClient {
    @POST("messages/text")
    suspend fun sendTextMessage(
        @Body body: SendMessageReq,
    ): Response<SendMessageRes>
}

data class SendMessageReq(
    val chatId: String,
    val content: String,
)

data class SendMessageRes(
    val chat: ChatDto,
    val shouldFinish: Boolean,
)
