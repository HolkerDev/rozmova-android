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

    @GET("v1/chats/latest")
    suspend fun getLatest(
        @Query("userLang") userLang: String,
        @Query("scenarioLang") scenarioLang: String,
    ): Response<GetLatestChatResp>

    @POST("v1/chats/messages/audio")
    suspend fun sendAudioMessage(
        @Body body: SendAudioReq,
    ): Response<SendAudioRes>

    @POST("v1/chats")
    suspend fun createChat(
        @Body body: CreateChatReq,
    ): Response<CreateChatResp>
}

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
