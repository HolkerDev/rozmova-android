package eu.rozmova.app.repositories

import android.util.Log
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
        suspend fun fetchScenarios() {
            val response =
                supabaseClient.postgrest
                    .from(Tables.SCENARIOS)
                    .select()
                    .decodeAs<List<ScenarioModel>>()
            Log.i("ScenariosRepository", "Fetched scenarios: $response")
        }
    }
