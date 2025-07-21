package eu.rozmova.app.modules.chatlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.modules.chatlist.components.ChatItem
import org.orbitmvi.orbit.compose.collectAsState

private data class Handlers(
    val refresh: () -> Unit,
    val deleteChat: (chatId: String) -> Unit,
    val selectChat: (chatId: String) -> Unit,
)

@Composable
fun ChatsListScreen(
    onChatSelect: (chatId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    Content(
        state,
        Handlers(
            refresh = viewModel::refresh,
            deleteChat = viewModel::deleteChat,
            selectChat = { chatId ->
                onChatSelect(chatId)
            },
        ),
        modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Content(
    state: ChatsListState,
    handlers: Handlers,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chats_screen_title)) },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = handlers.refresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                            end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                            bottom = 4.dp,
                        ),
                    ),
        ) {
            Column {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    if (state.isLoading) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        return@LazyColumn
                    }

                    if (state.isError) {
                        item {
                            Text(
                                text = stringResource(R.string.error_generic),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                            )
                        }
                        return@LazyColumn
                    }

                    when {
                        state.chats.isNotEmpty() -> {
                            items(state.chats) { chat ->
                                ChatItem(
                                    chat = chat,
                                    onChatClick = handlers.selectChat,
                                    onChatDelete = handlers.deleteChat,
                                )
                            }
                        }

                        else -> {
                            item {
                                Text(
                                    text = stringResource(R.string.chats_screen_no_chats),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun PreviewChatsListScreen() {
    Content(
        modifier = Modifier.fillMaxSize(),
        state = ChatsListState(isError = true),
        handlers =
            Handlers(
                refresh = {},
                deleteChat = {},
                selectChat = {},
            ),
    )
}
