package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import eu.rozmova.app.services.network.ApiService
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
        private val apiService: ApiService,
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
                    apiService.fetchWeeklyScenarios().let { response ->
                        if (response.isSuccessful) {
                            Log.i(
                                "ScenariosRepository",
                                "Successfully fetched weekly scenarios",
                            )
                        } else {
                            Log.e("ScenarioRepository", response.raw().toString())
                            Log.e(
                                "ScenariosRepository",
                                "Failed to fetch weekly scenarios: ${response.errorBody()}",
                            )
                        }
                    }
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
    }
