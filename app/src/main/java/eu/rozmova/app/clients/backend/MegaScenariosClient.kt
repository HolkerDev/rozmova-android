package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MegaScenariosClient {
    @POST("v1/scenarios/generate")
    suspend fun generateScenario(
        @Body body: GenerateScenarioReq,
    ): Response<GenerateScenarioResp>

    @POST("v1/scenarios/filter")
    suspend fun filter(
        @Body body: FilterScenariosReq,
    ): Response<FilterScenariosResp>

    @GET("v1/scenarios/{scenarioId}")
    suspend fun findById(
        @Path("scenarioId") scenarioId: String,
    ): Response<ScenarioDto>
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

data class FilterScenariosReq(
    val userLang: String,
    val scenarioLang: String,
    val scenarioType: String?,
    val difficulty: String?,
)

data class FilterScenariosResp(
    val scenarios: List<ScenarioDto>,
)
