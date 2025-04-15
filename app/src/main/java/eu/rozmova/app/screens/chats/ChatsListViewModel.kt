package eu.rozmova.app.screens.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsListViewModel
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel() {
        private val tag = this::class.simpleName

        private val _state = MutableStateFlow<ViewState<List<ChatDto>>>(ViewState.Loading)
        val state = _state.asStateFlow()

        init {
            loadChats()
        }

        fun loadChats() =
            viewModelScope.launch {
                _state.update { ViewState.Loading }
                chatsRepository
                    .fetchAll()
                    .map { chats ->
                        _state.update { ViewState.Success(chats) }
                    }.mapLeft { error ->
                        Log.e(tag, "Error loading chats", error)
                        _state.update { ViewState.Error(error) }
                    }
            }

        fun deleteChat(chatId: String) =
            viewModelScope.launch {
                _state.update { ViewState.Loading }
                chatsRepository
                    .deleteChat(chatId)
                    .map { chats ->
                        _state.update {
                            ViewState.Success(chats)
                        }
                    }.mapLeft {
                        Log.e(tag, "Error while deleting chat", it)
                    }
            }
    }
