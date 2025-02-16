package eu.rozmova.app.screens.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatWithScenarioModel
import eu.rozmova.app.repositories.ChatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatListState {
    data object Empty : ChatListState

    data object Loading : ChatListState

    data class Success(
        val chats: List<ChatWithScenarioModel>,
    ) : ChatListState

    data class Error(
        val msg: String,
    ) : ChatListState
}

@HiltViewModel
class ChatsListViewModel
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel() {
        private val tag = this::class.simpleName

        private val _state = MutableStateFlow<ChatListState>(ChatListState.Empty)
        val state = _state.asStateFlow()

        init {
            loadChats()
        }

        fun loadChats() =
            viewModelScope.launch {
                _state.update { ChatListState.Loading }
                try {
                    val chats = chatsRepository.fetchChats()
                    _state.update {
                        ChatListState.Success(chats)
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error loading chats", e)
                    _state.update { ChatListState.Error(e.message ?: "Unknown error") }
                }
            }
    }
