package eu.rozmova.app.screens.messagechat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.AudioMessageItem
import eu.rozmova.app.components.ChatAnalysisDialog
import eu.rozmova.app.components.ShouldFinishChatDialog
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.components.StopChatButton
import eu.rozmova.app.components.WordItem
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.WordModel
import eu.rozmova.app.screens.chatdetails.AudioChatMessage
import eu.rozmova.app.screens.createchat.ChatId
import eu.rozmova.app.utils.ViewState

data class ChatMessage(
    val id: String,
    val body: String,
    val author: Author,
)

@Composable
fun MessageChatScreen(
    chatId: ChatId,
    onChatArchive: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageChatViewModel = hiltViewModel(),
) {
    val messageListState = rememberLazyListState()
    val onChatArchiveState = rememberUpdatedState(onChatArchive)
    val state by viewModel.state.collectAsState()
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MessageChatEvent.ScrollToBottom -> {
                    (state.chat as? ViewState.Success)?.let { successState ->
                        messageListState.animateScrollToItem(successState.data.messages.size - 1)
                    }
                }

                MessageChatEvent.Close -> {
                    onChatArchiveState.value()
                }
                MessageChatEvent.ProposeFinish -> {
                    showModal = true
                }
                null -> {}
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ShouldFinishChatDialog(
            showDialog = showModal,
            onYesClick = {
                showModal = false
                (state.chat as? ViewState.Success)?.let { chatState ->
                    viewModel.finishChat(chatState.data.id)
                }
            },
            onNoClick = {
                showModal = false
            },
            onDismiss = {
                showModal = false
            },
        )

        state.chatAnalysis?.let {
            ChatAnalysisDialog(
                chatAnalysis = it,
                onConfirm = { viewModel.archiveChat(chatId) },
                isLoading = state.isAnalysisLoading,
            )
        }

        when (val chatState = state.chat) {
            is ViewState.Loading -> LoadingComponent(onBackClick)
            ViewState.Empty -> ErrorComponent(onBackClick)
            is ViewState.Error -> ErrorComponent(onBackClick)
            is ViewState.Success -> {
                ScenarioInfoCard(
                    scenario = chatState.data.scenario,
                    messages =
                        chatState.data.messages.map { message ->
                            ChatMessage(
                                id = message.id,
                                author = message.author,
                                body = message.transcription,
                            )
                        },
                    words = chatState.data.words,
                    chatModel = chatState.data.chatModel,
                    onBackClick = onBackClick,
                    onChatFinish = { },
                    onChatArchive = { },
                    isMessageLoading = state.isLoadingMessage,
                    messageListState = messageListState,
                )
            }
        }
    }
}

@Composable
fun ScenarioInfoCard(
    scenario: ScenarioModel,
    messages: List<ChatMessage>,
    words: List<WordModel>,
    chatModel: ChatModel,
    onBackClick: () -> Unit,
    onChatFinish: () -> Unit,
    onChatArchive: () -> Unit,
    isMessageLoading: Boolean,
    messageListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var showWordsBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleToolBar(title = stringResource(R.string.chat_details_title), onBack = onBackClick)
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 50.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (words.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { showWordsBottomSheet = true },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CollectionsBookmark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scenario.situation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Task,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scenario.userInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showWordsBottomSheet) {
        HelperWordsBottomSheet(
            words = words,
            onDismiss = { showWordsBottomSheet = false },
        )
    }
}

@Composable
fun AudioMessageList(
    messages: List<AudioChatMessage>,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    onChatFinish: () -> Unit,
    messageListState: LazyListState,
    isLoadingMessage: Boolean,
    showFinishButton: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        state = messageListState,
    ) {
        items(messages) { message ->
            AudioMessageItem(
                message = message,
                onPlayMessage = onPlayMessage,
                onStopMessage = onStopMessage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (isLoadingMessage) {
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (messages.size > 1 && showFinishButton) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                StopChatButton(
                    onClick = onChatFinish,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperWordsBottomSheet(
    words: List<WordModel>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.helper_words),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            ) {
                items(words) { word ->
                    WordItem(word = word)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingComponent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        SimpleToolBar(title = stringResource(R.string.loading_progress), onBack = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorComponent(onBackClick: () -> Unit) {
    SimpleToolBar(title = stringResource(R.string.error), onBack = onBackClick)
    Text(text = stringResource(R.string.error_message))
}
