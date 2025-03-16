package eu.rozmova.app.screens.chatdetails

import android.R.attr.maxLines
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.rozmova.app.R
import eu.rozmova.app.components.AudioMessageItem
import eu.rozmova.app.components.ChatAnalysisDialog
import eu.rozmova.app.components.ShouldFinishChatDialog
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.components.StopChatButton
import eu.rozmova.app.components.WordItem
import eu.rozmova.app.domain.ChatModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.WordModel

@Composable
fun ChatDetailScreen(
    onBackClick: () -> Unit,
    onChatArchive: () -> Unit,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    val onChatArchiveState = rememberUpdatedState(onChatArchive)
    val state by viewModel.state.collectAsState()
    val chatState by viewModel.state.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val shouldScrollToBottom by viewModel.shouldScrollToBottom.collectAsStateWithLifecycle()
    val shouldProposeToFinishChat by viewModel.shouldProposeToFinishChat.collectAsState()
    val chatArchived by viewModel.navigateToChatList.collectAsStateWithLifecycle()
    val messageListState = rememberLazyListState()
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(shouldScrollToBottom) {
        if (shouldScrollToBottom && chatState.messages != null) {
            messageListState.animateScrollToItem(chatState.messages!!.size - 1)
            viewModel.onScrollToBottom()
        }
    }

    LaunchedEffect(shouldProposeToFinishChat) {
        if (shouldProposeToFinishChat) {
            showModal = true
        }
    }

    LaunchedEffect(chatArchived) {
        if (chatArchived) {
            onChatArchiveState.value()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ShouldFinishChatDialog(
            showDialog = showModal,
            onYesClick = {
                showModal = false
                viewModel.finishChat(chatState.chat!!.id)
                viewModel.resetProposal()
            },
            onNoClick = {
                showModal = false
                viewModel.resetProposal()
            },
            onDismiss = {
                showModal = false
                viewModel.resetProposal()
            },
        )

        state.chatAnalysis?.let {
            ChatAnalysisDialog(
                chatAnalysis = it,
                onConfirm = { viewModel.onChatAnalysisSubmit() },
                isLoading = chatState.isChatAnalysisSubmitLoading,
            )
        }

        if (state.isLoading && state.chat == null) {
            LoadingComponent(onBackClick)
        } else if (!state.error.isNullOrBlank()) {
            ErrorComponent(onBackClick)
        } else {
            chatState.chat?.let { chat ->
                ScenarioInfoCard(
                    onBackClick = onBackClick,
                    onRecordStart = { viewModel.startRecording() },
                    onRecordStop = { viewModel.stopRecording() },
                    onPlayMessage = { messageId -> viewModel.playAudio(messageId) },
                    onStopMessage = { viewModel.stopAudio() },
                    isRecording = isRecording,
                    scenario = chat.scenario,
                    messages = chatState.messages ?: emptyList(),
                    chatModel = chat.chatModel,
                    words = chat.words,
                    isMessageLoading = state.isLoading,
                    messageListState = messageListState,
                    onChatFinish = { viewModel.finishChat(chat.id) },
                    onChatArchive = { viewModel.prepareAnalytics(chat.id) },
                )
            } ?: LoadingComponent(onBackClick)
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
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    onChatFinish: () -> Unit,
    onChatArchive: () -> Unit,
    isMessageLoading: Boolean,
    isRecording: Boolean,
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
                AudioMessageList(
                    messages = messages,
                    onPlayMessage,
                    onStopMessage,
                    onChatFinish,
                    messageListState,
                    isMessageLoading,
                    chatModel.status == ChatStatus.IN_PROGRESS,
                    Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                AudioRecorderButton(
                    onRecordStart = onRecordStart,
                    onRecordStop = onRecordStop,
                    isDisabled = isMessageLoading,
                    shouldAnalyse = chatModel.status == ChatStatus.FINISHED,
                    onChatAnalyticsRequest = onChatArchive,
                    isRecording = isRecording,
                )
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
    messages: List<ChatMessage>,
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
                text = "helper_words",
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
