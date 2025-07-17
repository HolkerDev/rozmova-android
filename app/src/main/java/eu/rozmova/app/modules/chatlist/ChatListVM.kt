package eu.rozmova.app.modules.chatlist

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ChatsListState(
    val chats: List<ChatDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)

@HiltViewModel
class ChatListVM
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
        private val settingsRepository: SettingsRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<ChatsListState, Nothing> {
        override val container = container<ChatsListState, Nothing>(ChatsListState())
        private val tag = this::class.simpleName

        init {
            loadChats()
            intent {
                appStateRepository.refetch.collect {
                    loadChats()
                }
            }
            intent {
                appStateRepository.fetchChats.collect {
                    loadChats()
                }
            }
        }

        fun loadChats() =
            intent {
                reduce { state.copy(isLoading = true) }
                val scenarioLang = settingsRepository.getLearningLangOrDefault()
                val userLang = settingsRepository.getInterfaceLang()
                chatsRepository
                    .fetchAll(userLang, scenarioLang)
                    .map { chats ->
                        reduce { state.copy(isLoading = false, chats = chats) }
                    }.mapLeft { error ->
                        Log.e(tag, "Error loading chats", error)
                        reduce { state.copy(isLoading = false, error = error) }
                    }
            }

        fun refresh() =
            intent {
                reduce { state.copy(isRefreshing = true) }
                try {
                    val scenarioLang = settingsRepository.getLearningLangOrDefault()
                    val userLang = settingsRepository.getInterfaceLang()
                    val chats = chatsRepository.fetchAll(userLang, scenarioLang)

                    chats.map { respChats ->
                        reduce { state.copy(chats = respChats) }
                    }
                } finally {
                    reduce { state.copy(isRefreshing = false) }
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
