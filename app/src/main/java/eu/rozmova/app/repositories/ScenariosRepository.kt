package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.backend.FilterScenariosReq
import eu.rozmova.app.clients.backend.GenerateScenarioReq
import eu.rozmova.app.clients.backend.MegaScenariosClient
import eu.rozmova.app.clients.backend.RecommendedScenariosRequest
import eu.rozmova.app.clients.backend.ScenarioClient
import eu.rozmova.app.clients.backend.WeeklyScenariosBody
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.TodayScenarioSelection
import javax.inject.Inject
import javax.inject.Singleton

data class ChatIdWithScenarioType(
    val chatId: String,
    val scenarioType: ScenarioTypeDto,
)

@Singleton
class ScenariosRepository
    @Inject
    constructor(
        private val scenarioClient: ScenarioClient,
        private val megaScenariosClient: MegaScenariosClient,
    ) {
        suspend fun getAllWithFilter(
            userLang: String,
            scenarioLang: String,
            scenarioType: ScenarioTypeDto?,
            difficulty: DifficultyDto?,
        ): Either<InfraErrors, List<ScenarioDto>> =
            Either
                .catch {
                    Log.i(
                        "ScenariosRepository",
                        "Fetching scenarios with type: $scenarioType, difficulty: $difficulty",
                    )
                    val scenarios =
                        megaScenariosClient.filter(
                            FilterScenariosReq(
                                userLang = userLang,
                                scenarioLang = scenarioLang,
                                scenarioType = scenarioType?.name,
                                difficulty = difficulty?.name,
                            ),
                        )
                    if (scenarios.isSuccessful) {
                        scenarios.body()?.scenarios ?: emptyList()
                    } else {
                        throw InfraErrors.NetworkError(
                            "Error trying to fetch scenarios: ${scenarios.errorBody()}",
                        )
                    }
                }.mapLeft { error ->
                    Log.e("ScenariosRepository", "Error trying to fetch scenarios", error)
                    InfraErrors.NetworkError("Error trying to fetch scenarios: $error")
                }

        suspend fun generateScenario(
            userLang: String,
            scenarioLang: String,
            scenarioType: ScenarioTypeDto,
            difficulty: DifficultyDto,
            description: String,
        ): Either<InfraErrors, ChatIdWithScenarioType> =
            Either
                .catch {
                    Log.i(
                        "ScenariosRepository",
                        "Generating scenario with type: $scenarioType, difficulty: $difficulty",
                    )
                    val response =
                        megaScenariosClient.generateScenario(
                            GenerateScenarioReq(
                                description = description,
                                userLang = userLang,
                                scenarioLang = scenarioLang,
                                scenarioType = scenarioType.name,
                                difficulty = difficulty.name,
                            ),
                        )
                    if (!response.isSuccessful) {
                        throw InfraErrors.NetworkError(
                            "Error trying to generate scenario: ${response.errorBody()}",
                        )
                    }
                    val responseBody =
                        response.body()
                            ?: throw IllegalStateException("Response body is null")
                    ChatIdWithScenarioType(
                        chatId = responseBody.chatId,
                        scenarioType = responseBody.scenarioType,
                    )
                }.mapLeft { error ->
                    Log.e("ScenariosRepository", "Error trying to generate scenario", error)
                    InfraErrors.NetworkError("Error trying to generate scenario: $error")
                }

        suspend fun getTodaySelection(
            userLang: String,
            scenarioLang: String,
        ): Either<InfraErrors, TodayScenarioSelection> =
            Either
                .catch {
                    Log.i("ScenariosRepository", "Fetching today's scenario selection")
                    val response =
                        scenarioClient.fetchRecommendedScenarios(
                            RecommendedScenariosRequest(
                                userLang = userLang,
                                scenarioLang = scenarioLang,
                            ),
                        )
                    if (response.isSuccessful) {
                        response.body()?.let { resp ->
                            TodayScenarioSelection(
                                easyScenario = resp.easy,
                                mediumScenario = resp.medium,
                                hardScenario = resp.hard,
                            )
                        } ?: throw IllegalStateException("Response body is null")
                    } else {
                        throw InfraErrors.NetworkError(
                            "Error trying to fetch today selection: $response",
                        )
                    }
                }.mapLeft {
                    Log.e("ScenariosRepository", "Error trying to fetch today selection", it)
                    InfraErrors.DatabaseError("Error trying to fetch today selection")
                }

        suspend fun getScenarioById(scenarioId: String): Either<InfraErrors, ScenarioDto> =
            Either
                .catch {
                    Log.i("ScenariosRepository", "Fetching scenario by ID")
                    val response = megaScenariosClient.findById(scenarioId)
                    if (!response.isSuccessful) {
                        throw InfraErrors.NetworkError(
                            "Error trying to fetch scenario by ID: ${response.errorBody()}",
                        )
                    }
                    response.body()?.let { scenario ->
                        Log.i("ScenariosRepository", "Scenario fetched successfully: $scenario")
                        scenario
                    } ?: throw IllegalStateException("Response body is null")
                }.mapLeft {
                    Log.e("ScenariosRepository", "Error trying to fetch scenario by ID", it)
                    InfraErrors.DatabaseError("Error trying to fetch scenario by ID: $it")
                }

        suspend fun weeklyScenarios(
            userLang: String,
            scenarioLang: String,
        ): Either<InfraErrors, List<ScenarioDto>> =
            Either
                .catch {
                    Log.i("ScenariosRepository", "Fetching weekly scenarios")
                    val response =
                        scenarioClient.fetchWeeklyScenarios(
                            WeeklyScenariosBody(
                                userLang = userLang,
                                scenarioLang = scenarioLang,
                            ),
                        )
                    Log.i("ScenariosRepository", "Weekly scenarios response: $response")

                    if (response.isSuccessful) {
                        response.body()?.scenarios ?: emptyList()
                    } else {
                        throw InfraErrors.NetworkError(
                            "Error trying to fetch weekly scenarios: ${response.errorBody()}",
                        )
                    }
                }.mapLeft {
                    Log.e("ScenariosRepository", "Error trying to fetch weekly scenarios", it)
                    when (it) {
                        is InfraErrors -> it
                        else -> InfraErrors.NetworkError("Error trying to fetch weekly scenarios: $it")
                    }
                }
    }
