package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.domain.ChatModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
    ) {
        suspend fun fetchChats() {
            val response =
                supabaseClient.postgrest
                    .from(Tables.CHATS)
                    .select()
                    .decodeAs<List<ChatModel>>()
            Log.i("ChatsRepository", "Fetched chats: $response")
        }
    }
