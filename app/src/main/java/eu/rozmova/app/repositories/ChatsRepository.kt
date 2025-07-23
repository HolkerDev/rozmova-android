package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.backend.ChatClient
import eu.rozmova.app.clients.backend.CreateChatReq
import eu.rozmova.app.clients.backend.GenSignedUrlReq
import eu.rozmova.app.clients.backend.MegaChatClient
import eu.rozmova.app.clients.backend.MessageClient
import eu.rozmova.app.clients.backend.SendAudioReq
import eu.rozmova.app.clients.backend.SendTextReq
import eu.rozmova.app.clients.s3.S3Client
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.ReviewDto
import io.sentry.Sentry
import io.sentry.SentryLevel
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

class UsageLimitReachedException(
    msg: String = "Usage limit reached",
) : Exception(msg)

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
                    Log.i(
                        tag,
                        "Fetching latest chat for userLang: $userLang, scenarioLang: $scenarioLang",
                    )
                    megaChatClient
                        .getLatest(
                            userLang = userLang,
                            scenarioLang = scenarioLang,
                        ).let { res ->
                            if (res.isSuccessful) {
                                res.body()?.chat
                                    ?: throw IllegalStateException("Latest chat fetch failed due to empty body: ${res.message()}")
                            } else if (res.code() == 404) {
                                Log.i(tag, "No latest chat found.")
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
        ): Result<List<ChatDto>> =
            try {
                val response = megaChatClient.listChats(userLang, scenarioLang)
                if (response.isSuccessful.not()) {
                    Log.e(tag, "Chats list fetch failed: ${response.errorBody()?.string()}")
                    throw IllegalStateException("Chats list fetch failed: ${response.errorBody()?.string()}")
                }

                val responseBody =
                    response.body()
                        ?: throw IllegalStateException("Chats list fetch failed due to empty body: ${response.body()}")
                Result.success(responseBody.chats)
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch all chats", e)
                Sentry.captureMessage("Error trying to fetch all chats: ${e.message}", SentryLevel.ERROR)
                Result.failure(InfraErrors.NetworkError("Failed to fetch all chats"))
            }

        suspend fun deleteChat(chatId: String): Either<InfraErrors, List<ChatDto>> =
            Either
                .catch {
                    val response = megaChatClient.delete(chatId)
                    if (response.isSuccessful) {
                        response.body()?.chats
                            ?: throw IllegalStateException(
                                "Chat deletion failed: ${
                                    response.errorBody()?.string()
                                }",
                            )
                    } else {
                        throw IllegalStateException(
                            "Chat deletion failed: ${
                                response.errorBody()?.string()
                            }",
                        )
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error deleting chat", error)
                    InfraErrors.NetworkError("Failed to delete chat")
                }

        suspend fun fetchChatById(chatId: String): Either<InfraErrors, ChatDto> =
            Either
                .catch {
                    megaChatClient.getById(chatId).let { res ->
                        if (res.isSuccessful) {
                            res.body()?.chat
                                ?: throw IllegalStateException("Chat fetch failed: ${res.message()}")
                        } else {
                            throw IllegalStateException("Chat fetch failed: ${res.message()}")
                        }
                    }
                }.mapLeft { e ->
                    Log.e(tag, "Failed to fetch chat by ID", e)
                    InfraErrors.DatabaseError("Failed to fetch chat by ID")
                }

        suspend fun createChat(
            scenarioId: String,
            chatType: ChatType,
        ): Result<String> =
            try {
                val response =
                    megaChatClient.createChat(
                        CreateChatReq(
                            scenarioId = scenarioId,
                            chatType = chatType.name,
                        ),
                    )

                when {
                    response.code() == 429 -> {
                        Log.e(tag, "Usage limit reached for chat creation")
                        Result.failure(UsageLimitReachedException("Usage limit reached for chat creation"))
                    }
                    !response.isSuccessful -> {
                        Log.e(tag, "Chat creation failed: ${response.errorBody()?.string()}, status: ${response.code()}")
                        Result.failure(
                            IllegalStateException("Chat creation failed: ${response.errorBody()?.string()}, status: ${response.code()}"),
                        )
                    }
                    else -> {
                        val chatId =
                            response.body()?.chatId
                                ?: throw IllegalStateException("Body is null in chat creation response: ${response.body()}")
                        Result.success(chatId)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to create chat", e)
                Sentry.captureMessage("Error trying to create chat: ${e.message}", SentryLevel.ERROR)
                Result.failure(InfraErrors.NetworkError("Failed to create chat"))
            }

        suspend fun review(chatId: String): Either<InfraErrors, String> =
            Either
                .catch {
                    val response = megaChatClient.review(chatId)
                    if (!response.isSuccessful) {
                        Log.e(tag, "Chat finish failed: ${response.errorBody()?.string()}")
                        throw IllegalStateException("Chat finish failed: ${response.message()}")
                    }
                    response.body()?.reviewId ?: throw IllegalStateException("Chat finish failed")
                }.mapLeft { error ->
                    Log.e(tag, "Error finishing chat", error)
                    InfraErrors.NetworkError("Failed to finish chat")
                }

        suspend fun getReview(reviewId: String): Either<InfraErrors, ReviewDto> =
            Either
                .catch {
                    Log.i(tag, "Fetching chat review for reviewId: $reviewId")
                    val response = megaChatClient.getReview(reviewId)
                    if (!response.isSuccessful) {
                        throw IllegalStateException(
                            "Chat review fetch failed: ${
                                response.errorBody()?.string()
                            } status: ${response.code()}",
                        )
                    }
                    response.body()?.review
                        ?: throw IllegalStateException("Chat review fetch failed: ${response.errorBody()} status: ${response.code()}")
                }.mapLeft { error ->
                    Log.e(tag, "Error fetching chat review", error)
                    InfraErrors.NetworkError("Failed to fetch chat review")
                }

        suspend fun getReviews(): Either<InfraErrors, List<ReviewDto>> =
            Either
                .catch {
                    Log.i(tag, "Fetching chat reviews")
                    val response = megaChatClient.getReviews()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(
                            "Reviews fetch failed: ${
                                response.errorBody()?.string()
                            } status: ${response.code()}",
                        )
                    }
                    response.body()?.reviews
                        ?: throw IllegalStateException("Chat reviews fetch failed: ${response.errorBody()} status: ${response.code()}")
                }.mapLeft { error ->
                    Log.e(tag, "Error fetching all reviews", error)
                    InfraErrors.NetworkError("Failed to fetch all reviews")
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
                            "Audio message send failed: ${
                                response.errorBody()?.string()
                            } status: ${response.code()}",
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
                    val response =
                        megaChatClient
                            .sendTextMessage(
                                SendTextReq(
                                    chatId = chatId,
                                    content = message,
                                    pronoun = pronounCode,
                                ),
                            )
                    if (!response.isSuccessful) {
                        throw IllegalStateException(
                            "Text message send failed: ${
                                response.errorBody()?.string()
                            } status: ${response.code()}",
                        )
                    }
                    val responseBody =
                        response.body()
                            ?: throw IllegalStateException("Text message send failed: ${response.body()} status: ${response.code()}")
                    ChatUpdate(
                        chat = responseBody.chat,
                        shouldFinish = responseBody.shouldFinish,
                    )
                }.mapLeft { error ->
                    Log.e(tag, "Error sending message", error)
                    InfraErrors.NetworkError("Failed to send text message")
                }
    }
