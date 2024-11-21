package eu.rozmova.app.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.ChatState
import eu.rozmova.app.clients.RetrofitClient
import eu.rozmova.app.components.ChatItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatsListViewModel @Inject constructor() : ViewModel() {
    private val _chats = MutableStateFlow(listOf<ChatItem>())
    val chats = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error = _error.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.chatApi.getChats()
                }
                _chats.value = result.map {
                    ChatItem(
                        it.id, it.title, listOf("dog"), it.state == ChatState.CREATED
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatsListViewModel", "Error loading chats", e)
                _error.value = e.message ?: "Unknown error"
            }
            _isLoading.value = false
        }
    }
}


@Composable
fun ChatsListScreen(
    onChatSelected: (String) -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (error != null) {
            Text("Error: $error")
        } else if (isLoading) {
            CircularProgressIndicator()
        } else {
            chats.forEach { chat ->
                ChatItem(chat, onChatClick = { onChatSelected(chat.id) })
            }
        }
    }
}
