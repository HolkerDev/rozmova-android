package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import arrow.core.raise.either
import eu.rozmova.app.clients.ChatClient
import eu.rozmova.app.clients.ChatCreateReq
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatWithScenarioModel
import eu.rozmova.app.domain.MessageDto
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

    data class NetworkError(
        val msg: String,
    ) : InfraErrors(msg)
}

@Serializable
data class ChatResponse(
    val messages: List<MessageDto>,
    val shouldFinishChat: Boolean,
)

@Singleton
class ChatsRepository
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
        private val chatClient: ChatClient,
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

        suspend fun fetchAll(): Either<InfraErrors, List<ChatDto>> =
            Either
                .catch {
                    chatClient.fetchAll().let { res ->
                        if (res.isSuccessful) {
                            val chats =
                                res.body()
                                    ?: throw IllegalStateException("Chats list fetch failed due to empty body: ${res.message()}")
                            Log.i(tag, "Chat fetched: $chats")
                            chats
                        } else {
                            throw IllegalStateException("Chats list fetch failed: ${res.message()}")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to fetch all chats", e)
                    InfraErrors.DatabaseError("Failed to fetch all chats")
                }

        suspend fun deleteChat(chatId: String): Either<InfraErrors, List<ChatDto>> =
            Either
                .catch {
                    val response = chatClient.deleteById(chatId)
                    if (response.isSuccessful) {
                        val chats =
                            response.body()
                                ?: throw IllegalStateException("Chat deletion failed: ${response.message()}")
                        Log.i(tag, "Chat deleted: $chats")
                        chats
                    } else {
                        throw IllegalStateException("Chat deletion failed: ${response.message()}")
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error deleting chat", error)
                    InfraErrors.NetworkError("Failed to delete chat")
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

        suspend fun fetchChatById(chatId: String): Either<InfraErrors, ChatDto> =
            Either
                .catch {
                    chatClient.fetchChatById(chatId).let { res ->
                        if (res.isSuccessful) {
                            val chat =
                                res.body()
                                    ?: throw IllegalStateException("Chat fetch failed: ${res.message()}")
                            Log.i(tag, "Chat fetched: $chat")
                            chat
                        } else {
                            throw IllegalStateException("Chat fetch failed: ${res.message()}")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to fetch chat by ID", e)
                    InfraErrors.DatabaseError("Failed to fetch chat by ID")
                }

        suspend fun createChatFromScenario(scenarioId: String): Either<InfraErrors, ChatDto> =
            Either
                .catch {
                    chatClient.createChat(ChatCreateReq(scenarioId = scenarioId)).let { res ->
                        if (res.isSuccessful) {
                            val chat =
                                res.body()?.chat
                                    ?: throw IllegalStateException("Chat creation failed")
                            Log.i(tag, "Chat created: $chat")
                            chat
                        } else {
                            throw IllegalStateException("Chat creation failed")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to create chat", e)
                    InfraErrors.NetworkError("Failed to create chat")
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

        suspend fun sendAudioMessage(
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

        suspend fun sendMessage(
            chatId: String,
            message: String,
        ): Either<InfraErrors, ChatResponse> =
            either {
                try {
                    val response = supabaseClient.functions.invoke("send-message", mapOf("chatId" to chatId, "message" to message))
                    response.body<ChatResponse>()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to send message", e)
                    raise(InfraErrors.DatabaseError("Failed to send message"))
                }
            }
    }
