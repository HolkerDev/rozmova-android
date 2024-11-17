package eu.rozmova.app.clients

import retrofit2.http.GET

interface ChatApi {
    @GET("chats")
    suspend fun getChats(): List<ChatDto>
}