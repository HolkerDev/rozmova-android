package eu.rozmova.app.clients

import eu.rozmova.app.domain.ScenarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ScenarioClient {
    @POST("scenarios/weekly")
    suspend fun fetchWeeklyScenarios(
        @Body body: WeeklyScenariosBody,
    ): Response<WeeklyScenariosResponse>
}

data class WeeklyScenariosResponse(
    val scenarios: List<ScenarioDto>,
)

data class WeeklyScenariosBody(
    val userLang: String,
    val scenarioLang: String,
)
