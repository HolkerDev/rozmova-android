package eu.rozmova.app.screens.chats

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.ChatItem
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toScenarioType

@Composable
fun ChatsListScreen(
    onChatSelect: (String, ScenarioType) -> Unit,
    onChatCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp, top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                shape = MaterialTheme.shapes.medium,
            ) {
                when (val viewState = state) {
                    ChatListState.Empty ->
                        Text(
                            text = stringResource(R.string.chats_screen_no_chats),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    is ChatListState.Error ->
                        Text(
                            text = stringResource(R.string.error_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    ChatListState.Loading ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    is ChatListState.Success -> {
                        LazyColumn(
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            items(viewState.chats) { chat ->
                                ChatItem(chat, onChatClick = {
                                    onChatSelect(
                                        chat.id,
                                        chat.scenario.scenarioType.toScenarioType(),
                                    )
                                }, onChatDelete = {
                                    viewModel.deleteChat(chat.id)
                                })
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
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.create_chat_content_description),
            )
        }
    }
}
