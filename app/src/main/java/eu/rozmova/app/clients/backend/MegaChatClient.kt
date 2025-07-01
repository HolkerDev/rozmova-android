package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ChatDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MegaChatClient {
    @GET("v1/chats")
    suspend fun listChats(
        @Query("userLang") userLang: String,
        @Query("scenarioLang") scenarioLang: String,
    ): Response<ListChatsResp>

    @POST("v1/chats/messages/audio")
    suspend fun sendAudioMessage(
        @Body body: SendAudioReq,
    ): Response<SendAudioRes>
}

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
