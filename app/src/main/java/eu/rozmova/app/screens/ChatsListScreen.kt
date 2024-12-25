package eu.rozmova.app.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.RetrofitClient
import eu.rozmova.app.clients.domain.ChatState
import eu.rozmova.app.components.ChatItem
import eu.rozmova.app.components.SimpleToolBar
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
    data class Success(val chats: List<ChatItem>) : ChatListState
    data class Error(val msg: String) : ChatListState
}

@HiltViewModel
class ChatsListViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<ChatListState>(ChatListState.Empty)
    val state = _state.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() = viewModelScope.launch {
        _state.update { ChatListState.Loading }
        try {
            val result = withContext(Dispatchers.IO) {
                RetrofitClient.chatApi.getChats()
            }
            _state.update {
                ChatListState.Success(result.map { chat ->
                    ChatItem(
                        chat.id, chat.title, listOf("dog"), chat.state == ChatState.CREATED
                    )
                })
            }
        } catch (e: Exception) {
            Log.e("ChatsListViewModel", "Error loading chats", e)
            _state.update { ChatListState.Error(e.message ?: "Unknown error") }
        }
    }
}

@Composable
fun ChatsListScreen(
    onChatSelected: (String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            SimpleToolBar("Chats")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (val viewState = state) {
                    ChatListState.Empty -> Text("No chats")
                    is ChatListState.Error -> Text("Error: ${viewState.msg}")
                    ChatListState.Loading -> CircularProgressIndicator()
                    is ChatListState.Success -> {
                        LazyColumn {
                            items(viewState.chats) { chat ->
                                ChatItem(chat, onChatClick = { onChatSelected(chat.id) })
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {},
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create new chat"
            )
        }
    }
}
