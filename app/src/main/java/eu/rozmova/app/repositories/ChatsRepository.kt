package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ScenarioModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
    ) {
        private val tag = this::class.simpleName

        suspend fun fetchChats(): List<ChatModel> =
            supabaseClient.postgrest
                .from(Tables.CHATS)
                .select()
                .decodeAs<List<ChatModel>>()

        suspend fun createChatFromScenario(scenario: ScenarioModel): String {
            try {
                val chatId = UUID.randomUUID().toString()
                supabaseClient.postgrest.from(Tables.CHATS).insert(
                    ChatModel(
                        id = chatId,
                        scenarioId = scenario.id,
                        status = ChatStatus.IN_PROGRESS,
                        userId = supabaseClient.auth.currentUserOrNull()!!.id,
                    ),
                )

                return chatId
            } catch (e: Exception) {
                Log.e(tag, "Failed to create chat", e)
                throw e
            }
        }
    }
