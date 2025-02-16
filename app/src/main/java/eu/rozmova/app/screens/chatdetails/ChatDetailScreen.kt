package eu.rozmova.app.screens.chatdetails

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.rozmova.app.components.AudioMessageItem
import eu.rozmova.app.components.ChatAnalysisDialog
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.components.StopChatButton
import eu.rozmova.app.domain.ScenarioModel

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
    val chatArchived by viewModel.navigateToChatList.collectAsStateWithLifecycle()
    val messageListState = rememberLazyListState()

    LaunchedEffect(shouldScrollToBottom) {
        if (shouldScrollToBottom && chatState.messages != null) {
            messageListState.animateScrollToItem(chatState.messages!!.size - 1)
            viewModel.onScrollToBottom()
        }
    }

    LaunchedEffect(chatArchived) {
        if (chatArchived) {
            onChatArchiveState.value()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
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
            ErrorComponent(state.error!!, onBackClick)
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
                    isMessageLoading = state.isLoading,
                    messageListState = messageListState,
                    onChatFinish = { viewModel.finishChat() },
                )
            } ?: LoadingComponent(onBackClick)
        }
    }
}

@Composable
fun ScenarioInfoCard(
    scenario: ScenarioModel,
    messages: List<ChatMessage>,
    onBackClick: () -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    onChatFinish: () -> Unit,
    isMessageLoading: Boolean,
    isRecording: Boolean,
    messageListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SimpleToolBar(title = "Speaking practice", onBack = onBackClick)
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 50.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
                    Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                AudioRecorderButton(
                    onRecordStart = onRecordStart,
                    onRecordStop = onRecordStop,
                    isDisabled = isMessageLoading,
                    isRecording = isRecording,
                )
            }
        }
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
        } else if (messages.size > 1) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                StopChatButton(
                    onClick = onChatFinish,
                )
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
        SimpleToolBar("Loading...", onBack = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorComponent(
    errorMessage: String,
    onBackClick: () -> Unit,
) {
    SimpleToolBar("Error", onBack = onBackClick)
    Text("Error: $errorMessage")
}
