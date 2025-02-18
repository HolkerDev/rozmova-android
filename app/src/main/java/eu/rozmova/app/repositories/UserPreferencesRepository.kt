package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import arrow.core.Option
import eu.rozmova.app.domain.UserPreference
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
    ) {
        suspend fun fetchUserPreferences(): Option<UserPreference> =
            Option.catch {
                supabaseClient.postgrest
                    .from(Tables.USER_PREFERENCES)
                    .select()
                    .decodeSingle<UserPreference>()
            }

        suspend fun updateUserPreferences(userPreference: UserPreference): Either<InfraErrors, Unit> =
            Either
                .catch {
                    supabaseClient.postgrest
                        .from(Tables.USER_PREFERENCES)
                        .upsert(userPreference)
                }.map { }
                .mapLeft {
                    Log.e("UserPreferencesRepository", "Error trying to upsert user preferences", it)
                    InfraErrors.DatabaseError("Error trying to upsert user preferences")
                }
    }
