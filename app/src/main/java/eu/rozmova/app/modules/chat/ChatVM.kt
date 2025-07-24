package eu.rozmova.app.modules.chat

import android.app.Application
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import eu.rozmova.app.state.AppStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ChatState(
    val chat: ChatDto? = null,
    val messages: List<MessageUI> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isReviewLoading: Boolean = false,
    val isAudioRecording: Boolean = false,
    val isMessageLoading: Boolean = false,
    val isSubscribed: Boolean = false,
)

sealed interface ChatEvents {
    object ScrollToBottom : ChatEvents

    data class ReviewReady(
        val reviewId: String,
    ) : ChatEvents

    data class ProposeFinish(
        val lastBotMsg: MessageUI,
        val lastUserMsg: MessageUI,
        val chatType: ChatType,
    ) : ChatEvents
}

data class MessageUI(
    val dto: MessageDto,
    val isPlaying: Boolean = false,
)

@HiltViewModel
class ChatVM
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
        private val appStateRepository: AppStateRepository,
        private val exoPlayer: ExoPlayer,
        private val subscriptionRepository: SubscriptionRepository,
        application: Application,
    ) : AndroidViewModel(application),
        ContainerHost<ChatState, ChatEvents> {
        override val container = container<ChatState, ChatEvents>(ChatState())
        private val tag = "ChatVM"

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        init {
            exoPlayer.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onAudioFinished()
                        }
                    }
                },
            )
            initIsSubscribed()
        }

        private fun initIsSubscribed() =
            intent {
                val isSubscribed =
                    subscriptionRepository
                        .getIsSubscribed()
                reduce { state.copy(isSubscribed = isSubscribed) }
            }

        fun loadChat(chatId: String) =
            intent {
                chatsRepository
                    .fetchChatById(chatId)
                    .map { chat ->
                        reduce {
                            state.copy(
                                chat = chat,
                                messages = chat.messages.map { MessageUI(it, false) },
                            )
                        }
                        Log.i(tag, "Chat type is ${chat.chatType}")
                        postSideEffect(ChatEvents.ScrollToBottom)
                    }.mapLeft { err ->
                        Log.e(tag, "Error loading chat: ${err.message}")
                    }
            }

        fun finishChat(chatId: String) =
            intent {
                reduce { state.copy(isReviewLoading = true) }
                chatsRepository
                    .review(chatId = chatId)
                    .map { reviewId ->
                        appStateRepository.triggerRefetch()
                        postSideEffect(ChatEvents.ReviewReady(reviewId))
                    }.mapLeft { error ->
                        Log.e(tag, "Error finishing chat: ${error.message}")
                        reduce { state.copy(isReviewLoading = false, isError = true) }
                    }
            }

        fun sendMessage(
            chatId: String,
            message: String,
        ) = intent {
            reduce { state.copy(isMessageLoading = true, isError = false) }
            chatsRepository
                .sendMessage(
                    chatId = chatId,
                    message = message,
                ).map { chatUpdate ->
                    val messages = chatUpdate.chat.messages.map { MessageUI(it, false) }
                    reduce {
                        state.copy(
                            chat = chatUpdate.chat,
                            messages = messages,
                            isMessageLoading = false,
                        )
                    }
                    if (chatUpdate.shouldFinish) {
                        postSideEffect(
                            ChatEvents.ProposeFinish(
                                lastBotMsg = messages.last(),
                                lastUserMsg = messages[messages.size - 2],
                                chatType = chatUpdate.chat.chatType,
                            ),
                        )
                    }
                }.mapLeft { error ->
                    Log.e(tag, "Error sending message: ${error.message}")
                    reduce { state.copy(isMessageLoading = false, isError = true) }
                }
            postSideEffect(ChatEvents.ScrollToBottom)
        }

        fun onAudioSaved() =
            intent {
                reduce { state.copy(isMessageLoading = true) }
                val chatId = state.chat?.id ?: throw IllegalStateException("Chat ID is null")
                val file = audioFile ?: throw IllegalStateException("Audio file is null")

                chatsRepository
                    .sendAudioMessage(chatId = chatId, file)
                    .map { response ->
                        val messages = response.chat.messages.map { MessageUI(it, false) }
                        if (response.shouldFinish) {
                            postSideEffect(
                                ChatEvents.ProposeFinish(
                                    lastBotMsg = messages.last(),
                                    lastUserMsg = messages[messages.size - 2],
                                    chatType = response.chat.chatType,
                                ),
                            )
                        }
                        reduce {
                            state.copy(
                                chat = response.chat,
                                isMessageLoading = false,
                                messages = messages,
                            )
                        }
                        postSideEffect(ChatEvents.ScrollToBottom)
                    }.mapLeft { error ->
                        Log.e("ChatDetailsViewModel", "Error sending audio message: ${error.message}")
                        reduce { state.copy(isMessageLoading = false, isError = true) }
                    }
            }

        fun startRecording() =
            intent {
                try {
                    reduce { state.copy(isError = false) }
                    val outputDir =
                        getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    val fileId = UUID.randomUUID()
                    audioFile = File(outputDir, "$fileId.mp4")

                    mediaRecorder =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MediaRecorder(getApplication())
                        } else {
                            MediaRecorder()
                        }

                    mediaRecorder?.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncodingBitRate(128000) // 128 kbps
                        setAudioSamplingRate(44100) // 44.1 kHz
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(audioFile?.absolutePath)
                        prepare()
                        start()
                    }

                    reduce { state.copy(isAudioRecording = true) }
                } catch (e: Exception) {
                    Log.e(tag, "Error starting recording: ${e.message}")
                    reduce { state.copy(isAudioRecording = false) }
                }
            }

        fun onAudioFinished() =
            intent {
                reduce {
                    state.copy(messages = state.messages.map { msg -> msg.copy(isPlaying = false) })
                }
            }

        fun stopRecording() =
            intent {
                try {
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null

                    onAudioSaved()
                } catch (e: Exception) {
                    Log.e(tag, "Error stopping recording: ${e.message}")
                } finally {
                    reduce { state.copy(isAudioRecording = false) }
                }
            }

        override fun onCleared() {
            super.onCleared()
            mediaRecorder?.release()
            mediaRecorder = null
        }

        fun playAudio(msgId: String) =
            intent {
                try {
                    stopPlayer()
                    reduce {
                        state.copy(
                            messages =
                                state.messages.map { msg ->
                                    msg.copy(isPlaying = false)
                                },
                        )
                    }
                    val messageToPlay =
                        state.chat?.messages?.find { it.id == msgId }
                            ?: throw IllegalStateException("Message not found")
                    val audioUri = buildAudioUri(messageToPlay)
                    Log.i(tag, "Audio URI: $audioUri")
                    withContext(Dispatchers.Main) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                    reduce {
                        state.copy(
                            messages =
                                state.messages.map { msg ->
                                    MessageUI(
                                        dto = msg.dto,
                                        isPlaying = msg.dto.id == msgId,
                                    )
                                },
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Error playing audio", e)
                }
            }

        fun stopAudio() =
            intent {
                stopPlayer()
                reduce {
                    state.copy(
                        messages =
                            state.messages.map { msg ->
                                msg.copy(isPlaying = false)
                            },
                    )
                }
            }

        private suspend fun stopPlayer() =
            withContext(Dispatchers.Main) {
                exoPlayer.stop()
            }

        private fun buildAudioUri(messageDto: MessageDto): Uri {
            val audioId = messageDto.audioId
            val audioLink = messageDto.link
            val isUser = messageDto.author == Author.USER
            if (audioId == null || audioId.isEmpty()) {
                throw IllegalArgumentException("Audio ID cannot be empty")
            }
            if (isUser) {
                val outputDir =
                    getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val audioFile = File(outputDir, "$audioId.mp4")
                val audioUri = Uri.fromFile(audioFile)
                return audioUri
            } else {
                return audioLink!!.toUri()
            }
        }
    }
