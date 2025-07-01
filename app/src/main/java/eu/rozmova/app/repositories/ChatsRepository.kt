package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.backend.ChatClient
import eu.rozmova.app.clients.backend.ChatCreateReq
import eu.rozmova.app.clients.backend.FetchLatestReq
import eu.rozmova.app.clients.backend.FinishChatRes
import eu.rozmova.app.clients.backend.GenSignedUrlReq
import eu.rozmova.app.clients.backend.MegaChatClient
import eu.rozmova.app.clients.backend.MessageClient
import eu.rozmova.app.clients.backend.SendAudioReq
import eu.rozmova.app.clients.backend.SendMessageReq
import eu.rozmova.app.clients.s3.S3Client
import eu.rozmova.app.domain.ChatDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class InfraErrors(
    msg: String,
) : Exception(msg) {
    data class DatabaseError(
        val msg: String,
    ) : InfraErrors(msg)

    data class NetworkError(
        val msg: String,
    ) : InfraErrors(msg)
}

data class ChatUpdate(
    val chat: ChatDto,
    val shouldFinish: Boolean,
)

@Singleton
class ChatsRepository
    @Inject
    constructor(
        private val chatClient: ChatClient,
        private val messageClient: MessageClient,
        private val s3Client: S3Client,
        private val megaChatClient: MegaChatClient,
        private val settingsRepository: SettingsRepository,
    ) {
        private val tag = this::class.simpleName

        suspend fun fetchLatest(
            scenarioLang: String,
            userLang: String,
        ): Either<InfraErrors, ChatDto?> =
            Either
                .catch {
                    chatClient
                        .fetchLatestChat(
                            FetchLatestReq(
                                userLang = userLang,
                                scenarioLang = scenarioLang,
                            ),
                        ).let { res ->
                            if (res.isSuccessful) {
                                res.body()
                            } else if (res.code() == 404) {
                                null
                            } else {
                                throw IllegalStateException("Latest chat failed to fetch: ${res.message()}")
                            }
                        }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to fetch latest chat", e)
                    InfraErrors.DatabaseError("Failed to fetch latest chat")
                }

        suspend fun fetchAll(
            userLang: String,
            scenarioLang: String,
        ): Either<InfraErrors, List<ChatDto>> =
            Either
                .catch {
                    megaChatClient.listChats(userLang, scenarioLang).let { res ->
                        if (res.isSuccessful) {
                            val responseBody =
                                res.body()
                                    ?: throw IllegalStateException("Chats list fetch failed due to empty body: ${res.message()}")
                            responseBody.chats
                        } else {
                            throw IllegalStateException("Chats list fetch failed: ${res.message()}")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to fetch all chats", e)
                    InfraErrors.NetworkError("Failed to fetch all chats")
                }

        suspend fun deleteChat(chatId: String): Either<InfraErrors, List<ChatDto>> =
            Either
                .catch {
                    val response = chatClient.deleteById(chatId)
                    if (response.isSuccessful) {
                        val chats =
                            response.body()
                                ?: throw IllegalStateException("Chat deletion failed: ${response.message()}")
                        chats
                    } else {
                        throw IllegalStateException("Chat deletion failed: ${response.message()}")
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error deleting chat", error)
                    InfraErrors.NetworkError("Failed to delete chat")
                }

        suspend fun fetchChatById(chatId: String): Either<InfraErrors, ChatDto> =
            Either
                .catch {
                    chatClient.fetchChatById(chatId).let { res ->
                        if (res.isSuccessful) {
                            val chat =
                                res.body()
                                    ?: throw IllegalStateException("Chat fetch failed: ${res.message()}")
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
                                res.body()
                                    ?: throw IllegalStateException("Chat creation failed")
                            chat
                        } else {
                            throw IllegalStateException("Chat creation failed")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to create chat", e)
                    InfraErrors.NetworkError("Failed to create chat")
                }

        suspend fun finishChat(chatId: String): Either<InfraErrors, FinishChatRes> =
            Either
                .catch {
                    chatClient.finish(chatId).let { res ->
                        if (res.isSuccessful) {
                            val responseBody =
                                res.body()
                                    ?: throw IllegalStateException("Chat finish failed: ${res.message()}")
                            responseBody
                        } else {
                            throw IllegalStateException("Chat finish failed: ${res.message()}")
                        }
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error finishing chat", error)
                    InfraErrors.NetworkError("Failed to finish chat")
                }

        suspend fun sendAudioMessage(
            chatId: String,
            messageAudioFile: File,
        ): Either<InfraErrors, ChatUpdate> =
            Either
                .catch {
                    val fileId = messageAudioFile.name.substringBefore(".")

                    val signedUrlResponse =
                        messageClient.getSignedUrl(
                            GenSignedUrlReq(
                                fileId = fileId,
                            ),
                        )

                    if (signedUrlResponse.isSuccessful.not()) {
                        throw IllegalStateException("Audio message send failed: ${signedUrlResponse.message()}")
                    }

                    val signedUrl =
                        signedUrlResponse.body()?.url
                            ?: throw IllegalStateException("Audio message send failed: ${signedUrlResponse.message()}")

                    s3Client.uploadFile(signedUrl, messageAudioFile)

                    val pronounCode = settingsRepository.getPronounCodeOrDefault()
                    val response =
                        megaChatClient.sendAudioMessage(
                            SendAudioReq(
                                chatId = chatId,
                                audioId = messageAudioFile.name.substringBefore("."),
                                pronoun = pronounCode,
                            ),
                        )

                    if (response.isSuccessful) {
                        val responseBody =
                            response.body()
                                ?: throw IllegalStateException(
                                    "Audio message send failed: ${response.errorBody()} status: ${response.code()}",
                                )

                        ChatUpdate(
                            chat = responseBody.chat,
                            shouldFinish = responseBody.shouldFinish,
                        )
                    } else {
                        throw IllegalStateException(
                            "Audio message send failed: ${response.errorBody()?.string()} status: ${response.code()}",
                        )
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error sending audio message", error)
                    InfraErrors.NetworkError("Failed to send audio message")
                }

        suspend fun sendMessage(
            chatId: String,
            message: String,
        ): Either<InfraErrors, ChatUpdate> =
            Either
                .catch {
                    val pronounCode = settingsRepository.getPronounCodeOrDefault()
                    messageClient
                        .sendTextMessage(
                            SendMessageReq(
                                chatId = chatId,
                                content = message,
                                pronounCode = pronounCode,
                            ),
                        ).let { res ->
                            if (res.isSuccessful) {
                                val responseBody =
                                    res.body()
                                        ?: throw IllegalStateException("Text message send failed: ${res.body()} status: ${res.code()}")
                                ChatUpdate(
                                    chat = responseBody.chat,
                                    shouldFinish = responseBody.shouldFinish,
                                )
                            } else {
                                throw IllegalStateException("Text message send failed: ${res.errorBody()?.string()} status: ${res.code()}")
                            }
                        }
                }.mapLeft { error ->
                    Log.e(tag, "Error sending message", error)
                    InfraErrors.NetworkError("Failed to send text message")
                }
    }
