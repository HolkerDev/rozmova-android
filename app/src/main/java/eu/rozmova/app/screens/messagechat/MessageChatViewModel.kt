package eu.rozmova.app.screens.messagechat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatWithMessagesDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageChatState(
    val chat: ViewState<ChatWithMessagesDto> = ViewState.Loading,
    val isLoadingMessage: Boolean = false,
    val chatAnalysis: ChatAnalysis? = null,
    val isAnalysisLoading: Boolean = false,
)

sealed class MessageChatEvent {
    data object ScrollToBottom : MessageChatEvent()

    data object ProposeFinish : MessageChatEvent()

    data object Close : MessageChatEvent()
}

@HiltViewModel
class MessageChatViewModel
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel() {
        private val _state =
            MutableStateFlow(MessageChatState())

        val state = _state.asStateFlow()

        private val _events = MutableSharedFlow<MessageChatEvent?>()
        val events = _events.asSharedFlow()

        fun loadChat(chatId: String) =
            viewModelScope.launch {
                chatsRepository.fetchChatById(chatId = chatId).let { chat ->
                    _state.value = _state.value.copy(chat = ViewState.Success(chat))
                    _events.emit(MessageChatEvent.ScrollToBottom)
                }
            }

        fun finishChat(chatId: String) =
            viewModelScope.launch {
                chatsRepository.finishChat(chatId).mapLeft {
                    Log.e("MessageChatViewModel", "Error finishing chat", it)
                }
            }

        fun archiveChat(chatId: String) =
            viewModelScope.launch {
                _state.value = _state.value.copy(isAnalysisLoading = true)
                chatsRepository.archiveChat(chatId).mapLeft {
                    Log.e("MessageChatViewModel", "Error archiving chat", it)
                }
                _state.value = _state.value.copy(isAnalysisLoading = false)
            }
    }
