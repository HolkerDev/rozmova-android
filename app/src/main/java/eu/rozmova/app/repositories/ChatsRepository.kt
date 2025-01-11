package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
import eu.rozmova.app.clients.domain.MessageModel
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatWithScenarioModel
import eu.rozmova.app.domain.ScenarioModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import java.io.File
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

        suspend fun fetchChats(): List<ChatWithScenarioModel> {
            val columns =
                Columns.raw(
                    """
                    *,
                    scenario:scenario_id(*)
                """,
                )
            return supabaseClient
                .postgrest
                .from(Tables.CHATS)
                .select(columns)
                .decodeList<ChatWithScenarioModel>()
        }

        suspend fun fetchChatById(chatId: String): ChatWithMessagesDto {
            val columns =
                Columns.raw(
                    """
                *,
                scenario:scenario_id(*)
                messages:messages(*)
            """,
                )
            return supabaseClient
                .postgrest
                .from(Tables.CHATS)
                .select(columns) {
                    filter {
                        ChatWithScenarioModel::id eq chatId
                    }
                }.decodeSingle<ChatWithMessagesDto>()
        }

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

        suspend fun sendMessage(
            chatId: String,
            messageAudioFile: File,
        ) {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: throw IllegalStateException("User is not authenticated")
                val filePath = "$userId/${messageAudioFile.name}"
                supabaseClient.storage.from("audio-messages").upload(filePath, messageAudioFile.readBytes())
                val response = supabaseClient.functions.invoke("send-audio-message", mapOf("chatId" to chatId, "audioPath" to filePath))
                val messages = response.body<List<MessageModel>>()
                Log.i(tag, "All messages: $messages")
            } catch (e: Exception) {
                Log.e(tag, "Failed to send message", e)
                throw e
            }
        }
    }
