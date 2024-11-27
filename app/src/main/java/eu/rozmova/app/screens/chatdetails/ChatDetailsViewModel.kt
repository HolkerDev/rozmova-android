package eu.rozmova.app.screens.chatdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.RetrofitClient
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatDetailState {
    object Empty : ChatDetailState
    object Loading : ChatDetailState
    data class Success(
        val chat: ChatWithMessagesDto, val audioState: AudioState, val messages: List<ChatMessage>
    ) : ChatDetailState

    data class Error(val msg: String) : ChatDetailState
}

data class AudioState(
    val isLoading: Boolean, val error: String?, val currentMessageIdPlaying: String?
)

data class ChatMessage(val id: String, val isPlaying: Boolean, val body: String, val link: String)

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val expoPlayer: ExoPlayer,
) : ViewModel() {
    private val _state = MutableStateFlow<ChatDetailState>(ChatDetailState.Empty)
    val state = _state.asStateFlow()

    fun loadChat(chatId: String) = viewModelScope.launch {
        _state.update { ChatDetailState.Loading }
        try {
            val chat = RetrofitClient.chatApi.getChatById(chatId)
            _state.update {
                ChatDetailState.Success(chat, audioState = AudioState(
                    isLoading = false, error = null, currentMessageIdPlaying = null
                ), messages = chat.messages.map { message ->
                    ChatMessage(
                        id = message.id,
                        isPlaying = false,
                        body = message.body,
                        link = message.link
                    )
                })
            }
        } catch (e: Exception) {
            Log.e("ChatDetailsViewModel", "Error loading chat", e)
            _state.update { ChatDetailState.Error(e.message ?: "Unknown error") }
        }
    }

    fun playAudio(messageId: String, audioUrl: String) = viewModelScope.launch {
        try {
            Log.i("ChatDetailsViewModel", "Playing audio $audioUrl")
            updateAudioState { it.copy(isLoading = true, error = null) }

            // If something playing, stop it
            state.value.let { currentState ->
                if (currentState is ChatDetailState.Success) {
                    currentState.audioState.currentMessageIdPlaying?.let {
                        stopAudio(it)
                    }
                }
            }

            // Play audio
            expoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
            expoPlayer.prepare()
            expoPlayer.play()
            updateAudioState {
                it.copy(
                    isLoading = false, error = null, currentMessageIdPlaying = messageId
                )
            }

            updateMessages {
                it.map { message ->
                    if (message.id == messageId) {
                        message.copy(isPlaying = true)
                    } else {
                        message
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatDetailsViewModel", "Error playing audio", e)
            updateAudioState { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun stopAudio(messageId: String) {
        expoPlayer.stop()
        updateAudioState {
            it.copy(
                isLoading = false, error = null, currentMessageIdPlaying = null
            )
        }
        updateMessages {
            it.map { message ->
                if (message.id == messageId) {
                    message.copy(isPlaying = false)
                } else {
                    message
                }
            }
        }
    }

    private fun updateAudioState(update: (AudioState) -> AudioState) {
        _state.update { state ->
            if (state is ChatDetailState.Success) {
                state.copy(audioState = update(state.audioState))
            } else {
                state
            }
        }
    }

    private fun updateMessages(update: (List<ChatMessage>) -> List<ChatMessage>) {
        _state.update { state ->
            if (state is ChatDetailState.Success) {
                state.copy(messages = update(state.messages))
            } else {
                state
            }
        }
    }
}
