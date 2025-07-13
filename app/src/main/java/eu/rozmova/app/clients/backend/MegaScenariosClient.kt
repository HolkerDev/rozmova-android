package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.ScenarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MegaScenariosClient {
    @POST("v2/scenarios/generate")
    suspend fun generateScenarioV2(
        @Body body: GenerateScenarioV2Req,
    ): Response<String>

    @POST("v1/scenarios/filter")
    suspend fun filter(
        @Body body: FilterScenariosReq,
    ): Response<FilterScenariosResp>

    @GET("v1/scenarios/{scenarioId}")
    suspend fun findById(
        @Path("scenarioId") scenarioId: String,
    ): Response<ScenarioDto>
}

data class GenerateScenarioV2Req(
    val description: String,
    val userLang: String,
    val scenarioLang: String,
    val chatType: String,
    val difficulty: String,
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
