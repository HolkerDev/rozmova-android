package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioTypeDto
import retrofit2.Response
import retrofit2.http.GET

interface MegaChatClient {
    @GET("v1/chats")
    suspend fun listChats(): Response<List<ListChatsResp>>
}

data class ListChatsResp(
    val chatId: String,
    val scenario: Scenario,
) {
    data class Scenario(
        val scenarioId: String,
        val difficulty: DifficultyDto,
        val scenarioType: ScenarioTypeDto,
        val title: String,
        val situation: String,
        val labels: List<String>,
    )
}
