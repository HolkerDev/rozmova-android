package eu.rozmova.app.screens.chatdetails
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
import eu.rozmova.app.domain.ChatWithMessagesDto
import eu.rozmova.app.repositories.ChatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ChatDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val chat: ChatWithMessagesDto? = null,
    val messages: List<ChatMessage>? = null,
    val audioPlayback: AudioState = AudioState(false, null, null),
    val isRecording: Boolean = false,
)

data class AudioState(
    val isLoading: Boolean,
    val error: String?,
    val currentMessageIdPlaying: String?,
)

data class ChatMessage(
    val id: String,
    val isPlaying: Boolean,
    val duration: Int = 0,
    val body: String,
    val link: String,
    val author: Author,
)

@HiltViewModel
class ChatDetailsViewModel
    @Inject
    constructor(
        private val expoPlayer: ExoPlayer,
        private val chatsRepository: ChatsRepository,
        application: Application,
    ) : AndroidViewModel(application) {
        private val tag = this::class.simpleName

        private val _state = MutableStateFlow(ChatDetailState())
        val state = _state.asStateFlow()
        private val _shouldScrollToBottom = MutableStateFlow(false)
        val shouldScrollToBottom = _shouldScrollToBottom.asStateFlow()

        fun scrollToBottom() {
            if (_shouldScrollToBottom.value) return
            if (_state.value.messages.isNullOrEmpty()) return
            _shouldScrollToBottom.update { true }
        }

        fun onScrollToBottom() {
            _shouldScrollToBottom.update { false }
        }

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        private val _isRecording = MutableStateFlow(false)
        val isRecording = _isRecording.asStateFlow()

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

        val onAudioSaved = {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                scrollToBottom()
                val allMessages =
                    chatsRepository.sendMessage(
                        chatId = _state.value.chat!!.id,
                        audioFile!!,
                    )
                _state.update {
                    it.copy(
                        isLoading = false,
                        messages =
                            allMessages.map { message ->
                                ChatMessage(
                                    id = message.id,
                                    isPlaying = false,
                                    body = message.transcription,
                                    link = message.audioReference,
                                    author = message.author,
                                    duration = message.audioDuration,
                                )
                            },
                    )
                }
                scrollToBottom()
            }
        }

        fun startRecording() {
            try {
                val outputDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                audioFile = File(outputDir, "recording_${System.currentTimeMillis()}.mp4")

                mediaRecorder =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(getApplication())
                    } else {
                        MediaRecorder()
                    }

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                }

                _isRecording.value = true
            } catch (e: Exception) {
                Log.e(tag, "Error starting recording: ${e.message}")
                _isRecording.value = false
            }
        }

        fun stopRecording() {
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
                _isRecording.value = false
            }
        }

        fun finishChat() {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    val analysis = chatsRepository.finishChat(_state.value.chat!!.id)
                    Log.i(tag, "Chat finished: $analysis")
                    _state.update { it.copy(isLoading = false) }
                } catch (e: Exception) {
                    Log.e(tag, "Error finishing chat: ${e.message}")
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            mediaRecorder?.release()
            mediaRecorder = null
        }

        fun loadChat(chatId: String) =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    val chat = chatsRepository.fetchChatById(chatId)
                    _state.update {
                        it.copy(
                            chat = chat,
                            isLoading = false,
                            messages =
                                chat.messages.map { message ->
                                    ChatMessage(
                                        id = message.id,
                                        isPlaying = false,
                                        body = message.transcription,
                                        link = message.audioReference,
                                        author = message.author,
                                        duration = message.audioDuration,
                                    )
                                },
                        )
                    }
                    scrollToBottom()
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Error loading chat", e)
                }
            }

        fun playAudio(messageId: String) =
            viewModelScope.launch {
                try {
                    stopAudio()
                    val message =
                        _state.value.messages?.first { it.id == messageId }
                            ?: throw IllegalStateException("Message not found")
                    val audioUri = buildAudioUri(message.link, message.author == Author.USER)
                    expoPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                    expoPlayer.prepare()
                    expoPlayer.play()
                    _state.update { it.copy(messages = it.messages?.map { msg -> msg.copy(isPlaying = msg.id == messageId) }) }
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Error playing audio", e)
                }
            }

        fun stopAudio() {
            expoPlayer.stop()
            _state.update { it.copy(messages = it.messages?.map { msg -> msg.copy(isPlaying = false) }) }
        }

        private suspend fun buildAudioUri(
            audioLink: String,
            isUser: Boolean,
        ): Uri {
            if (isUser) {
                val audioName = audioLink.split("/").last()
                val outputDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val audioFile = File(outputDir, audioName)
                val audioUri = Uri.fromFile(audioFile)
                return audioUri
            } else {
                return Uri.parse(chatsRepository.getPublicAudioLink(audioLink))
            }
        }
    }
