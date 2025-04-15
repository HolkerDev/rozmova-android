package eu.rozmova.app.screens.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.repositories.ChatsRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ChatsListState(
    val chats: List<ChatDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)

@HiltViewModel
class ChatsListViewModel
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel(),
        ContainerHost<ChatsListState, Nothing> {
        override val container = container<ChatsListState, Nothing>(ChatsListState())
        private val tag = this::class.simpleName

        init {
            loadChats()
        }

        fun loadChats() =
            intent {
                reduce { state.copy(isLoading = true) }
                chatsRepository
                    .fetchAll()
                    .map { chats ->
                        reduce { state.copy(isLoading = false, chats = chats) }
                    }.mapLeft { error ->
                        Log.e(tag, "Error loading chats", error)
                        reduce { state.copy(isLoading = false, error = error) }
                    }
            }

        fun deleteChat(chatId: String) =
            intent {
                reduce { state.copy(isLoading = true) }
                chatsRepository
                    .deleteChat(chatId)
                    .map { chats ->
                        reduce { state.copy(chats = chats, isLoading = false) }
                    }.mapLeft { error ->
                        Log.e(tag, "Error while deleting chat", error)
                        reduce { state.copy(isLoading = false, error = error) }
                    }
            }
    }
