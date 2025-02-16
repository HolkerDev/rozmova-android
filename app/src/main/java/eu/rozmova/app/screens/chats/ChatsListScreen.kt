package eu.rozmova.app.screens.chats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.ChatItem
import eu.rozmova.app.components.SimpleToolBar

@Composable
fun ChatsListScreen(
    onChatSelect: (String) -> Unit,
    onChatCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleToolBar("Chats")
            Card(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp, top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                when (val viewState = state) {
                    ChatListState.Empty -> Text("No chats")
                    is ChatListState.Error -> Text("Error: ${viewState.msg}")
                    ChatListState.Loading -> CircularProgressIndicator()
                    is ChatListState.Success -> {
                        LazyColumn {
                            items(viewState.chats) { chat ->
                                ChatItem(chat, onChatClick = { onChatSelect(chat.id) })
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onChatCreateClick,
            modifier =
                Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create new chat",
            )
        }
    }
}
