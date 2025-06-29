package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ScenarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ScenarioClient {
    @POST("scenarios/weekly")
    suspend fun fetchWeeklyScenarios(
        @Body body: WeeklyScenariosBody,
    ): Response<WeeklyScenariosResponse>

    @POST("scenarios/recommended")
    suspend fun fetchRecommendedScenarios(
        @Body body: RecommendedScenariosRequest,
    ): Response<RecommendedScenariosResponse>
}

data class WeeklyScenariosResponse(
    val scenarios: List<ScenarioDto>,
)

data class WeeklyScenariosBody(
    val userLang: String,
    val scenarioLang: String,
)

data class RecommendedScenariosRequest(
    val userLang: String,
    val scenarioLang: String,
)

data class RecommendedScenariosResponse(
    val easy: ScenarioDto,
    val medium: ScenarioDto,
    val hard: ScenarioDto,
)
