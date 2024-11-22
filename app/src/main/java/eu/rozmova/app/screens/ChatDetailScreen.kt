package eu.rozmova.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadChat(chatId)
    }
    val state by viewModel.state.collectAsState()

    Column {
        when (val viewState = state) {
            ChatDetailState.Empty -> LoadingComponent(onBackClicked)
            ChatDetailState.Loading -> LoadingComponent(onBackClicked)

            is ChatDetailState.Success -> {
                ChatDetails(
                    chatWithMessages = viewState.chat,
                    onBackClicked = onBackClicked,
                    modifier = modifier,
                )
            }

            is ChatDetailState.Error -> ErrorComponent(viewState.msg, onBackClicked)
        }
    }
}

@Composable
private fun ChatDetails(
    chatWithMessages: ChatWithMessagesDto, onBackClicked: () -> Unit, modifier: Modifier = Modifier
) {
    SimpleToolBar(title = chatWithMessages.title, onBack = onBackClicked)
    TaskDetailComponent(modifier, chatWithMessages.description, chatWithMessages.userInstructions)
    // Messages List
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(chatWithMessages.messages) { message ->
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskDetailComponent(modifier: Modifier, description: String, userInstruction: String) {
    Column(modifier = modifier) {
        Text(
            text = "Task Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Language Level: A2",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userInstruction,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingComponent(onBackClicked: () -> Unit) {
    SimpleToolBar("Loading...", onBackClicked)
    CircularProgressIndicator()
}

@Composable
private fun ErrorComponent(errorMessage: String, onBackClicked: () -> Unit) {
    SimpleToolBar("Error", onBack = onBackClicked)
    Text("Error: $errorMessage")
}