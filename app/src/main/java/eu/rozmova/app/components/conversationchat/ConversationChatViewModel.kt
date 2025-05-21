package eu.rozmova.app.components.conversationchat

import android.app.Application
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.repositories.ChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ChatDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val chat: ChatDto? = null,
    val messages: List<AudioChatMessage>? = null,
    val audioPlayback: AudioState = AudioState(false, null, null),
    val isRecording: Boolean = false,
    val isChatAnalysisSubmitLoading: Boolean = false,
    val chatAnalysis: ChatAnalysis? = null,
    val isAnalysisLoading: Boolean = false,
)

data class ConvoChatState(
    val chat: ChatDto? = null,
    val messages: List<AudioChatMessage> = emptyList(),
    val isAudioRecording: Boolean = false,
    val isMessageLoading: Boolean = false,
)

data class AudioState(
    val isLoading: Boolean,
    val error: String?,
    val currentMessageIdPlaying: String?,
)

data class AudioChatMessage(
    val id: String,
    val isPlaying: Boolean,
    val duration: Int = 0,
    val body: String,
    val author: Author,
)

sealed interface ConvoChatEvents {
    data object ScrollToBottom : ConvoChatEvents

    data object ProposeFinish : ConvoChatEvents

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

        private val _state = MutableStateFlow(ChatDetailState())
        val state = _state.asStateFlow()

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        val onAudioFinished = {
            _state.update { it.copy(messages = it.messages?.map { msg -> msg.copy(isPlaying = false) }) }
        }

        init {
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

//        fun onChatAnalysisSubmit() =
//            viewModelScope.launch {
//                _state.update { it.copy(isChatAnalysisSubmitLoading = true) }
//                chatsRepository.archiveChat(_state.value.chat!!.id).fold(
//                    { error ->
//                        _state.update {
//                            it.copy(
//                                isChatAnalysisSubmitLoading = false,
//                                error = error.message,
//                            )
//                        }
//                    },
//                    { _navigateToChatList.update { true } },
//                )
//            }

        fun onAudioSaved() =
            intent {
                reduce { state.copy(isMessageLoading = true) }
                val chatId = state.chat?.id ?: throw IllegalStateException("Chat ID is null")
                val file = audioFile ?: throw IllegalStateException("Audio file is null")

                chatsRepository
                    .sendAudioMessage(chatId = chatId, file)
                    .map { response ->
                        if (response.shouldFinish) {
                            postSideEffect(ConvoChatEvents.ProposeFinish)
                        }
                        reduce { state.copy(chat = response.chat, isMessageLoading = false) }
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

        fun finishChat(chatId: String) =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
//                chatsRepository.finishChat(chatId).map {
//                    _state.update {
//                        it.copy(chat = it.chat?.copy(chatModel = it.chat.chatModel.copy(status = ChatStatus.FINISHED)))
//                    } // TODO: Refactor this bullshit, I just hate this part of the code, like wtf
//                    _state.update { it.copy(isLoading = false) }
//                } // TODO: Handle error
            }

        fun prepareAnalytics(chatId: String) =
            viewModelScope.launch {
                _state.update { it.copy(isAnalysisLoading = true) }
                chatsRepository
                    .getAnalytics(chatId)
                    .map { chatAnalysis ->
                        _state.update {
                            it.copy(
                                isAnalysisLoading = false,
                                chatAnalysis = chatAnalysis,
                            )
                        }
                    }.mapLeft {
                        Log.e(tag, "Error preparing chat analytics: ${it.message}")
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
                    stopAudio()
                    val messageToPlay =
                        state.chat?.messages?.find { it.id == msgId } ?: throw IllegalStateException("Message not found")
                    val audioUri = buildAudioUri(messageToPlay.audioId, messageToPlay.author == Author.USER)
                    Log.i(tag, "Audio URI: $audioUri")
                    withContext(Dispatchers.Main) {
                        expoPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                        expoPlayer.prepare()
                        expoPlayer.play()
                    }
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Error playing audio", e)
                }
            }

        private suspend fun stopAudio() =
            withContext(Dispatchers.Main) {
                expoPlayer.stop()
            }

        private suspend fun buildAudioUri(
            audioId: String?,
            isUser: Boolean,
        ): Uri {
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
                return TODO()
//                return Uri.parse(chatsRepository.getPublicAudioLink(audioLink))
            }
        }
    }
