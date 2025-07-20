package eu.rozmova.app.modules.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ChatState(
    val chat: ChatDto? = null,
    val messages: List<MessageUI> = emptyList(),
    val isLoading: Boolean = false,
    val isReviewLoading: Boolean = false,
    val isAudioRecording: Boolean = false,
    val isMessageLoading: Boolean = false,
)

sealed interface ChatEvents {
    object ScrollToBottom : ChatEvents

    data class ReviewReady(
        val reviewId: String,
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
    ) : ViewModel(),
        ContainerHost<ChatState, ChatEvents> {
        override val container = container<ChatState, ChatEvents>(ChatState())
        private val tag = "ChatVM"

        fun loadChat(chatId: String) =
            intent {
                chatsRepository
                    .fetchChatById(chatId)
                    .map { chat ->
                        reduce { state.copy(chat = chat, messages = chat.messages.map { MessageUI(it, false) }) }
                        postSideEffect(ChatEvents.ScrollToBottom)
                    }.mapLeft { err ->
                        Log.e(tag, "Error loading chat: ${err.message}")
                    }
            }

        fun finishChat(chatId: String) =
            intent {
                reduce { state.copy(isReviewLoading = true) }
                chatsRepository.review(chatId = chatId).map { reviewId ->
                    appStateRepository.triggerRefetch()
                    postSideEffect(ChatEvents.ReviewReady(reviewId))
                }
            }
    }
