package eu.rozmova.app.repositories

import eu.rozmova.app.domain.ScenarioModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScenariosRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
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
    }
