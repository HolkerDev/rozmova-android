package eu.rozmova.app.components.messagechat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.repositories.ChatsRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class MessageChatState(
    val chat: ChatDto? = null,
    val isLoadingMessage: Boolean = false,
    val isLoadingReview: Boolean = false,
    val review: ReviewDto? = null,
)

sealed class MessageChatEvent {
    data object ScrollToBottom : MessageChatEvent()

    data class ProposeFinish(
        val lastBotMsg: MessageDto,
        val lastUserMsg: MessageDto,
    ) : MessageChatEvent()

    data object Close : MessageChatEvent()
}

@HiltViewModel
class MessageChatViewModel
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel(),
        ContainerHost<MessageChatState, MessageChatEvent> {
        override val container = container<MessageChatState, MessageChatEvent>(MessageChatState())

        fun loadChat(chatId: String) =
            intent {
                chatsRepository.fetchChatById(chatId = chatId).map { chat ->
                    reduce { state.copy(chat = chat) }
                    postSideEffect(MessageChatEvent.ScrollToBottom)
                }
            }

        fun finishChat(chatId: String) =
            intent {
                reduce { state.copy(isLoadingReview = true) }
                chatsRepository.finishChat(chatId = chatId).map { chatUpdate ->
                    reduce {
                        state.copy(
                            chat = chatUpdate.chat,
                            isLoadingReview = false,
                            review = chatUpdate.review,
                        )
                    }
                }
            }

        fun sendMessage(
            chatId: String,
            message: String,
        ) = intent {
            reduce { state.copy(isLoadingMessage = true) }
            chatsRepository
                .sendMessage(
                    chatId = chatId,
                    message = message,
                ).map { chatUpdate ->
                    reduce { state.copy(chat = chatUpdate.chat, isLoadingMessage = false) }
                    if (chatUpdate.shouldFinish) {
                        val messages = chatUpdate.chat.messages
                        postSideEffect(
                            MessageChatEvent.ProposeFinish(
                                lastBotMsg = messages.last(),
                                lastUserMsg = messages[messages.size - 2],
                            ),
                        )
                    }
                }
            postSideEffect(MessageChatEvent.ScrollToBottom)
        }
    }
