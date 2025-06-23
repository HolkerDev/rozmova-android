package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ScenarioTypeDto
import retrofit2.http.Body
import retrofit2.http.POST

interface MegaScenariosClient {
    @POST("v1/scenarios/generate")
    suspend fun generateScenario(
        @Body body: GenerateScenarioReq,
    ): GenerateScenarioResp
}

data class GenerateScenarioReq(
    val description: String,
    val userLang: String,
    val scenarioLang: String,
    val scenarioType: String,
    val difficulty: String,
)

data class GenerateScenarioResp(
    val scenarioType: ScenarioTypeDto,
    val chatId: String,
)
