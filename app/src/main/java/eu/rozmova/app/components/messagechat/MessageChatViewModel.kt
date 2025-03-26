package eu.rozmova.app.components.messagechat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.components.conversationchat.AudioChatMessage
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatWithMessagesDto
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
    val chat: ViewState<ChatWithMessagesDto> = ViewState.Loading,
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
                chatsRepository.fetchChatById(chatId = chatId).let { chat ->
                    _state.value =
                        _state.value.copy(
                            chat = ViewState.Success(chat),
                            messages =
                                ViewState.Success(
                                    chat.messages.map { message ->
                                        ChatMessage(
                                            id = message.id,
                                            body = message.transcription,
                                            author = message.author,
                                        )
                                    },
                                ),
                        )
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
                    response.messages.map { message ->
                        AudioChatMessage(
                            id = message.id,
                            isPlaying = false,
                            body = message.transcription,
                            link = message.audioReference,
                            author = message.author,
                            duration = message.audioDuration,
                        )
                    }
                }.fold(
                    { error ->
                    },
                    { chatMessages ->
                        _state.update {
                            it.copy(
                                messages =
                                    ViewState.Success(
                                        chatMessages.map { msg ->
                                            ChatMessage(
                                                id = msg.id,
                                                body = msg.body,
                                                author = msg.author,
                                            )
                                        },
                                    ),
                            )
                        }
                    },
                )

            _events.emit(MessageChatEvent.ScrollToBottom)
            _state.update { _state.value.copy(isLoadingMessage = false) }
        }

        fun finisChat(chatId: String) =
            viewModelScope.launch {
                _state.update { it.copy(isLoadingMessage = true) }
                chatsRepository
                    .finishChat(chatId)
                    .mapLeft {
                        Log.e("MessageChatViewModel", "Error finishing chat", it)
                    }.map {
                        val chatState = _state.value.chat
                        if (chatState is ViewState.Success) {
                            _state.value =
                                _state.value.copy(
                                    isLoadingMessage = false,
                                    chat =
                                        chatState.copy(
                                            data =
                                                chatState.data.copy(
                                                    chatModel = chatState.data.chatModel.copy(status = ChatStatus.FINISHED),
                                                ),
                                        ),
                                )
                        }
                    } // TODO: I hate this part of the code even more. FUCK IT
            }
    }
