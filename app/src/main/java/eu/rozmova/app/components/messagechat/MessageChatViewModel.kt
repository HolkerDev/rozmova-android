package eu.rozmova.app.components.messagechat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val body: String,
    val author: Author,
)

data class MessageChatState(
    val chat: ViewState<ChatDto> = ViewState.Loading,
    val messages: ViewState<List<ChatMessage>> = ViewState.Loading,
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

        private val _events = MutableSharedFlow<MessageChatEvent>()
        val events = _events.asSharedFlow()

        fun loadChat(chatId: String) =
            viewModelScope.launch {
                chatsRepository.fetchChatById(chatId = chatId).map { chat ->
                    _state.update { state ->
                        state.copy(
                            chat = ViewState.Success(chat),
                            messages =
                                ViewState.Success(
                                    chat.messages.sortedBy { msg -> msg.createdAt }.map { message ->
                                        ChatMessage(
                                            id = message.id,
                                            body = message.content,
                                            author = message.author,
                                        )
                                    },
                                ),
                        )
                    }
                    _events.emit(MessageChatEvent.ScrollToBottom)
                }
            }

        fun archiveChat(chatId: String) =
            viewModelScope.launch {
                _state.value = _state.value.copy(isAnalysisLoading = true)
                chatsRepository.archiveChat(chatId).mapLeft { err ->
                    Log.e("MessageChatViewModel", "Error archiving chat", err)
                }
                _state.value = _state.value.copy(isAnalysisLoading = false)
                _events.emit(MessageChatEvent.Close)
            }

        fun prepareAnalytics(chatId: String) =
            viewModelScope.launch {
                _state.update { it.copy(isAnalysisLoading = true) }
                chatsRepository
                    .getAnalytics(chatId)
                    .map { chatAnalysis ->
                        _state.update { state -> state.copy(isAnalysisLoading = false, chatAnalysis = chatAnalysis) }
                    }.mapLeft {
                        Log.e("MessageChatViewModel", "Error preparing chat analytics: ${it.message}")
                    }
            }

        fun sendMessage(
            chatId: String,
            message: String,
        ) = viewModelScope.launch {
            _state.update { _state.value.copy(isLoadingMessage = true) }
            _events.emit(MessageChatEvent.ScrollToBottom)
            chatsRepository
                .sendMessage(
                    chatId = chatId,
                    message = message,
                ).map { response ->
                    if (response.shouldFinishChat) {
                        _events.emit(MessageChatEvent.ProposeFinish)
                    }

                    _state.update { state ->
                        state.copy(
                            messages =
                                ViewState.Success(
                                    response.messages.sortedBy { msg -> msg.createdAt }.map { msg ->
                                        ChatMessage(
                                            id = msg.id,
                                            body = msg.content,
                                            author = msg.author,
                                        )
                                    },
                                ),
                        )
                    }
                }
            _events.emit(MessageChatEvent.ScrollToBottom)
            _state.update { _state.value.copy(isLoadingMessage = false) }
        }

        fun finishChat(chatId: String) =
            viewModelScope.launch {
//                chatsRepository
//                    .finishChat(chatId)
//                    .mapLeft {
//                        Log.e("MessageChatViewModel", "Error finishing chat", it)
//                    }.map {
//                        _state.update {
//                            it.copy(
//                                chat = it.chat.mapSuccess { it.copy(chatModel = it.chatModel.copy(status = ChatStatus.FINISHED)) },
//                            )
//                        }
//                    }
            }
    }
