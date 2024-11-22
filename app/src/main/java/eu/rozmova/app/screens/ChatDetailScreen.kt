package eu.rozmova.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.RetrofitClient
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
import eu.rozmova.app.components.SimpleToolBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatDetailState {
    object Empty : ChatDetailState
    object Loading : ChatDetailState
    data class Success(val chat: ChatWithMessagesDto) : ChatDetailState
    data class Error(val msg: String) : ChatDetailState
}

@HiltViewModel
class ChatDetailsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<ChatDetailState>(ChatDetailState.Empty)
    val state = _state.asStateFlow()

    fun loadChat(chatId: String) = viewModelScope.launch {
        _state.update { ChatDetailState.Loading }
        try {
            val chat = RetrofitClient.chatApi.getChatById(chatId)
            _state.update { ChatDetailState.Success(chat) }
        } catch (e: Exception) {
            _state.update { ChatDetailState.Error(e.message ?: "Unknown error") }
        }
    }
}

@Composable
fun ChatDetailScreen(
    onBackClicked: () -> Unit,
    chatId: String,
    viewModel: ChatDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadChat(chatId)
    }
    val state by viewModel.state.collectAsState()

    Column {
        when (val viewState = state) {
            ChatDetailState.Empty -> SimpleToolBar(title = "Loading...", onBack = onBackClicked)
            ChatDetailState.Loading -> {
                SimpleToolBar(title = "Loading...", onBack = onBackClicked)
                CircularProgressIndicator()
            }

            is ChatDetailState.Success -> {
                SimpleToolBar(title = viewState.chat.title, onBack = onBackClicked)
                Text("Chat detail screen for chat with title ${viewState.chat.title}")
            }

            is ChatDetailState.Error -> Text("Error: ${viewState.msg}")
        }
    }
}