package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ChatDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MegaChatClient {
    @GET("v1/chats")
    suspend fun listChats(
        @Query("userLang") userLang: String,
        @Query("scenarioLang") scenarioLang: String,
    ): Response<ListChatsResp>
}

data class ListChatsResp(
    val chats: List<ChatDto>,
)
