package eu.rozmova.app.components.conversationchat

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
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.repositories.ChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ConvoChatState(
    val chat: ChatDto? = null,
    val messages: List<AudioChatMessage> = emptyList(),
    val isAudioRecording: Boolean = false,
    val isReviewLoading: Boolean = false,
    val isMessageLoading: Boolean = false,
    val review: ReviewDto? = null,
    val isSubscribed: Boolean = false,
)

data class AudioChatMessage(
    val id: String,
    val isPlaying: Boolean,
    val body: String,
    val author: Author,
)

sealed interface ConvoChatEvents {
    data object ScrollToBottom : ConvoChatEvents

    data class ProposeFinish(
        val lastBotMsg: MessageDto,
        val lastUserMsg: MessageDto,
    ) : ConvoChatEvents

    data object Close : ConvoChatEvents
}

@HiltViewModel
class ChatDetailsViewModel
    @Inject
    constructor(
        private val expoPlayer: ExoPlayer,
        private val chatsRepository: ChatsRepository,
        application: Application,
    ) : AndroidViewModel(application),
        ContainerHost<ConvoChatState, ConvoChatEvents> {
        override val container: Container<ConvoChatState, ConvoChatEvents> = container(ConvoChatState())
        private val tag = this::class.simpleName

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        fun onAudioFinished() =
            intent {
                reduce {
                    state.copy(messages = state.messages.map { msg -> msg.copy(isPlaying = false) })
                }
            }

        init {
            fetchIsSubscribed()
            expoPlayer.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onAudioFinished()
                        }
                    }
                },
            )
        }

        private fun fetchIsSubscribed() =
            intent {
                reduce { state.copy(isSubscribed = true) }
            }

        fun finishChat(chatId: String) =
            intent {
//                reduce { state.copy(isReviewLoading = true) }
//                chatsRepository.review(chatId = chatId).map { chatUpdate ->
//                    reduce {
//                        state.copy(
//                            chat = chatUpdate.chat,
//                            isReviewLoading = false,
//                            review = chatUpdate.review,
//                        )
//                    }
//                }
            }

        fun onAudioSaved() =
            intent {
                reduce { state.copy(isMessageLoading = true) }
                val chatId = state.chat?.id ?: throw IllegalStateException("Chat ID is null")
                val file = audioFile ?: throw IllegalStateException("Audio file is null")

                chatsRepository
                    .sendAudioMessage(chatId = chatId, file)
                    .map { response ->
                        if (response.shouldFinish) {
                            val messages = response.chat.messages
                            postSideEffect(
                                ConvoChatEvents.ProposeFinish(
                                    lastBotMsg = messages.last(),
                                    lastUserMsg = messages[messages.size - 2],
                                ),
                            )
                        }
                        reduce {
                            state.copy(
                                chat = response.chat,
                                isMessageLoading = false,
                                messages = response.chat.messages.map { it.toAudioMessage() },
                            )
                        }
                    }.mapLeft { error ->
                        Log.e("ChatDetailsViewModel", "Error sending audio message: ${error.message}")
                        reduce { state.copy(isMessageLoading = false) }
                    }
            }

        fun startRecording() =
            intent {
                try {
                    val outputDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
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

        fun loadChat(chatId: String) =
            intent {
                chatsRepository
                    .fetchChatById(chatId)
                    .map { chat ->
                        reduce { state.copy(chat = chat, messages = chat.messages.map { it.toAudioMessage() }) }
                        postSideEffect(ConvoChatEvents.ScrollToBottom)
                    }.mapLeft { err ->
                        Log.e(tag, "Error loading chat: ${err.message}")
                    }
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
                        state.chat?.messages?.find { it.id == msgId } ?: throw IllegalStateException("Message not found")
                    val audioUri = buildAudioUri(messageToPlay)
                    Log.i(tag, "Audio URI: $audioUri")
                    withContext(Dispatchers.Main) {
                        expoPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                        expoPlayer.prepare()
                        expoPlayer.play()
                    }
                    reduce {
                        state.copy(
                            messages =
                                state.messages.map { msg ->
                                    AudioChatMessage(
                                        id = msg.id,
                                        isPlaying = msg.id == msgId,
                                        body = msg.body,
                                        author = msg.author,
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
                expoPlayer.stop()
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
