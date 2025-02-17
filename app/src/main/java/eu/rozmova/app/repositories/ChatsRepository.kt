package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import arrow.core.raise.either
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatWithMessagesDto
import eu.rozmova.app.domain.ChatWithScenarioModel
import eu.rozmova.app.domain.MessageModel
import eu.rozmova.app.domain.ScenarioModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

sealed class InfraErrors(
    msg: String,
) : Exception(msg) {
    data class DatabaseError(
        val msg: String,
    ) : InfraErrors(msg)
}

@Singleton
class ChatsRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
    ) {
        private val tag = this::class.simpleName

        suspend fun fetchChats(): Either<InfraErrors, List<ChatWithScenarioModel>> =
            either {
                try {
                    val columns =
                        Columns.raw(
                            """
                                    *,
                                    scenario:scenario_id(*)
                                  """,
                        )
                    supabaseClient
                        .postgrest
                        .from(Tables.CHATS)
                        .select(columns)
                        .decodeList<ChatWithScenarioModel>()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to fetch chats", e)
                    raise(InfraErrors.DatabaseError("Failed to fetch chats"))
                }
            }

        suspend fun archiveChat(chatId: String) {
            try {
                supabaseClient.from(Tables.CHATS).update(
                    {
                        ChatModel::status setTo ChatStatus.ARCHIVED
                    },
                ) {
                    filter {
                        ChatModel::id eq chatId
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to archive chat", e)
                throw e
            }
        }

        suspend fun fetchChatById(chatId: String): ChatWithMessagesDto {
            val columns =
                Columns.raw(
                    """
                *,
                scenario:scenario_id(*)
            """,
                )
            val chatModel =
                supabaseClient
                    .postgrest
                    .from(Tables.CHATS)
                    .select(columns) {
                        filter {
                            ChatWithScenarioModel::id eq chatId
                        }
                    }.decodeSingle<ChatWithScenarioModel>()

            val messages =
                supabaseClient.postgrest
                    .from(Tables.MESSAGES)
                    .select(Columns.raw("*")) {
                        filter {
                            MessageModel::chatId eq chatId
                        }
                    }.decodeList<MessageModel>()

            return ChatWithMessagesDto(chatModel.id, chatModel.scenario, messages)
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

        suspend fun getPublicAudioLink(audioPath: String): String {
            val userId =
                supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User is not authenticated")
            return supabaseClient.storage
                .from("audio-messages")
                .createSignedUrl("$userId/$audioPath", 60.seconds)
        }

        suspend fun finishChat(chatId: String): ChatAnalysis {
            try {
                val response = supabaseClient.functions.invoke("finish-chat", mapOf("chatId" to chatId))
                return response.body<ChatAnalysis>()
            } catch (e: Exception) {
                Log.e(tag, "Failed to finish chat", e)
                throw e
            }
        }

        suspend fun sendMessage(
            chatId: String,
            messageAudioFile: File,
        ): List<MessageModel> {
            try {
                val userId =
                    supabaseClient.auth.currentUserOrNull()?.id ?: throw IllegalStateException(
                        "User is not authenticated",
                    )
                val filePath = "$userId/${messageAudioFile.name}"
                supabaseClient.storage
                    .from("audio-messages")
                    .upload(filePath, messageAudioFile.readBytes())
                val response =
                    supabaseClient.functions.invoke(
                        "send-audio-message",
                        mapOf("chatId" to chatId, "audioPath" to filePath),
                    )
                Log.i(tag, "Message sent: ${response.body<String>()}")
                val messages = response.body<List<MessageModel>>()
                return messages
            } catch (e: Exception) {
                Log.e(tag, "Failed to send message", e)
                throw e
            }
        }
    }
