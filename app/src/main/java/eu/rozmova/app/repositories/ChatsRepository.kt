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
import eu.rozmova.app.domain.WordModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import kotlinx.serialization.Serializable
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

    data class AuthError(
        val msg: String,
    ) : InfraErrors(msg)
}

@Serializable
data class ChatResponse(
    val messages: List<MessageModel>,
    val shouldFinishChat: Boolean,
)

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

        suspend fun archiveChat(chatId: String): Either<InfraErrors, Unit> =
            either {
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
                    raise(InfraErrors.DatabaseError("Failed to archive chat"))
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

            val words =
                chatModel.scenario.wordIds?.let { wordIds ->
                    supabaseClient.postgrest
                        .from(Tables.WORDS)
                        .select(Columns.raw("*")) {
                            filter {
                                WordModel::id isIn wordIds
                            }
                        }.decodeList<WordModel>()
                } ?: emptyList()

            val messages =
                supabaseClient.postgrest
                    .from(Tables.MESSAGES)
                    .select(Columns.raw("*")) {
                        filter {
                            MessageModel::chatId eq chatId
                        }
                    }.decodeList<MessageModel>()

            return ChatWithMessagesDto(
                chatModel.id,
                chatModel.scenario,
                chatModel =
                    ChatModel(
                        id = chatId,
                        scenarioId = chatModel.scenario.id,
                        status = chatModel.status,
                        userId = chatModel.userId,
                    ),
                messages,
                words,
            )
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

        suspend fun getAnalytics(chatId: String): Either<InfraErrors, ChatAnalysis> =
            either {
                try {
                    val response = supabaseClient.functions.invoke("finish-chat", mapOf("chatId" to chatId))
                    response.body<ChatAnalysis>()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to prepare chat analytics", e)
                    raise(InfraErrors.DatabaseError("Failed to prepare chat analytics"))
                }
            }

        suspend fun finishChat(chatId: String): Either<InfraErrors, Unit> =
            either {
                try {
                    supabaseClient.postgrest.from("chats").update(
                        {
                            ChatModel::status setTo ChatStatus.FINISHED
                        },
                    ) {
                        filter {
                            ChatModel::id eq chatId
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to finish chat", e)
                    raise(InfraErrors.DatabaseError("Failed to finish chat"))
                }
            }

        suspend fun sendMessage(
            chatId: String,
            messageAudioFile: File,
        ): Either<InfraErrors, ChatResponse> =
            either {
                try {
                    val userId =
                        supabaseClient.auth.currentUserOrNull()?.id ?: raise(InfraErrors.AuthError("User is not authenticated"))
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
                    response.body<ChatResponse>()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to send message", e)
                    raise(InfraErrors.DatabaseError("Failed to send message"))
                }
            }
    }
