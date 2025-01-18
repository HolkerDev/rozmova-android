package eu.rozmova.app.screens.chatdetails
import android.app.Application
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.domain.Author
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
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

data class ChatState(
    val chat: ChatWithMessagesDto,
    val messages: List<ChatMessage>,
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

        private val _state = MutableStateFlow<ChatDetailState>(ChatDetailState())
        val state = _state.asStateFlow()

        private val _audioState = MutableStateFlow<AudioState>(AudioState(false, null, null))
        val audioState = _audioState.asStateFlow()

        private var mediaRecorder: MediaRecorder? = null
        private var audioFile: File? = null

        private val _isRecording = MutableStateFlow(false)
        val isRecording = _isRecording.asStateFlow()

        val onAudioSaved = {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
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
                                )
                            },
                    )
                }
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
                                    )
                                },
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Error loading chat", e)
                }
            }

//        fun playAudio(
//            messageId: String,
//            audioUrl: String,
//        ) = viewModelScope.launch {
//            try {
//                Log.i("ChatDetailsViewModel", "Playing audio $audioUrl")
//                updateAudioState { it.copy(isLoading = true, error = null) }
//
//                // Play audio
//                expoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
//                expoPlayer.prepare()
//                expoPlayer.play()
//                updateAudioState {
//                    it.copy(
//                        isLoading = false,
//                        error = null,
//                        currentMessageIdPlaying = messageId,
//                    )
//                }
//
//                updateMessages {
//                    it.map { message ->
//                        if (message.id == messageId) {
//                            message.copy(isPlaying = true)
//                        } else {
//                            message
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ChatDetailsViewModel", "Error playing audio", e)
//                updateAudioState { it.copy(isLoading = false, error = e.message) }
//            }
//        }
//
//        fun stopAudio(messageId: String) {
//            expoPlayer.stop()
//            updateAudioState {
//                it.copy(
//                    isLoading = false,
//                    error = null,
//                    currentMessageIdPlaying = null,
//                )
//            }
//            updateMessages {
//                it.map { message ->
//                    if (message.id == messageId) {
//                        message.copy(isPlaying = false)
//                    } else {
//                        message
//                    }
//                }
//            }
//        }

        private fun updateAudioState(update: (AudioState) -> AudioState) {
//            _state.update { state ->
//                if (state is ChatDetailState.Loaded) {
//                    state.copy(audioState = update(state.audioState))
//                } else {
//                    state
//                }
//            }
        }
    }
