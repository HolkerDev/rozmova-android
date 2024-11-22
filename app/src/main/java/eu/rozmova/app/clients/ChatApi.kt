package eu.rozmova.app.clients

import eu.rozmova.app.clients.domain.ChatDto
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatApi {
    @GET("chats")
    suspend fun getChats(): List<ChatDto>

    @GET("chats/{chatId}")
    suspend fun getChatById(@Path(value = "chatId") chatId: String): ChatWithMessagesDto
}