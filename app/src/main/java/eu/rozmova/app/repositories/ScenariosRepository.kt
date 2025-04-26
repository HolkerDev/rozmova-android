package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.ScenarioClient
import eu.rozmova.app.clients.ScenariosRequest
import eu.rozmova.app.clients.WeeklyScenariosBody
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

typealias LanguageCode = String

@Singleton
class ScenariosRepository
@Inject
constructor(
    private val supabaseClient: SupabaseClient,
    private val scenarioClient: ScenarioClient,
) {
    suspend fun getAll(
        learningLanguage: String,
        interfaceLanguage: String,
    ): List<ScenarioModel> =
        supabaseClient.postgrest
            .from(Tables.SCENARIOS)
            .select {
                filter {
                    and {
                        ScenarioModel::targetLanguage eq learningLanguage
                        ScenarioModel::userLanguage eq interfaceLanguage
                    }
                }
            }.decodeAs<List<ScenarioModel>>()

    suspend fun getAllWithFilter(): Either<InfraErrors, List<ScenarioDto>> = Either.catch {
        val scenarios = scenarioClient.fetchScenarios(
            ScenariosRequest(
                userLang = "en",
                scenarioLang = "de",
                scenarioType = ScenarioTypeDto.MESSAGES.name,
                difficulty = DifficultyDto.EASY.name
            )
        )
        Log.i("ScenariosRepository", "Scenarios: $scenarios")
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

    suspend fun getTodaySelection(
        learningLanguage: LanguageCode,
        interfaceLanguage: LanguageCode,
    ): Either<InfraErrors, TodayScenarioSelectionModel> =
        Either
            .catch {
                Log.i(
                    "ScenariosRepository",
                    "Fetching today selection for $learningLanguage and $interfaceLanguage",
                )
                supabaseClient.postgrest
                    .from(Tables.TODAY_SCENARIO_SELECTION)
                    .select(
                        Columns.raw(
                            """
                                    id, 
                                    created_at,
                                    user_language, 
                                    scenario_language,
                                    easy_scenario:easy_scenario_id(*), 
                                    medium_scenario:medium_scenario_id(*), 
                                    hard_scenario:hard_scenario_id(*)
                                """,
                        ),
                    ) {
                        filter {
                            and {
                                TodayScenarioSelectionModel::scenarioLanguage eq learningLanguage
                                TodayScenarioSelectionModel::userLanguage eq interfaceLanguage
                            }
                        }
                    }.decodeSingle<TodayScenarioSelectionModel>()
            }.mapLeft {
                Log.e("ScenariosRepository", "Error trying to fetch today selection", it)
                InfraErrors.DatabaseError("Error trying to fetch today selection")
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
