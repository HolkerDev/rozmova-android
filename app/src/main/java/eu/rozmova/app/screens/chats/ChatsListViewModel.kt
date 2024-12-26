package eu.rozmova.app.screens.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.RetrofitClient
import eu.rozmova.app.clients.domain.ChatState
import eu.rozmova.app.components.ChatItem
import eu.rozmova.app.repositories.ChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ChatListState {
    data object Empty : ChatListState

    data object Loading : ChatListState

    data class Success(
        val chats: List<ChatItem>,
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
        private val _state = MutableStateFlow<ChatListState>(ChatListState.Empty)
        val state = _state.asStateFlow()

        init {
            loadChats()
            viewModelScope.launch {
                chatsRepository.fetchChats()
            }
        }

        private fun loadChats() =
            viewModelScope.launch {
                _state.update { ChatListState.Loading }
                try {
                    val result =
                        withContext(Dispatchers.IO) {
                            RetrofitClient.chatApi.getChats()
                        }
                    _state.update {
                        ChatListState.Success(
                            result.map { chat ->
                                ChatItem(
                                    chat.id,
                                    chat.title,
                                    listOf("dog"),
                                    chat.state == ChatState.CREATED,
                                )
                            },
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ChatsListViewModel", "Error loading chats", e)
                    _state.update { ChatListState.Error(e.message ?: "Unknown error") }
                }
            }
    }
