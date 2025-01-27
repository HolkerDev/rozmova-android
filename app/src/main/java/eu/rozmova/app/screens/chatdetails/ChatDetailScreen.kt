package eu.rozmova.app.screens.chatdetails

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.utils.formatDuration

@Composable
fun ChatDetailScreen(
    onBackClick: () -> Unit,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }
    val state by viewModel.state.collectAsState()
    val chatState by viewModel.state.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val shouldScrollToBottom by viewModel.shouldScrollToBottom.collectAsStateWithLifecycle()
    val messageListState = rememberLazyListState()

    LaunchedEffect(shouldScrollToBottom) {
        if (shouldScrollToBottom && chatState.messages != null) {
            messageListState.animateScrollToItem(chatState.messages!!.size - 1)
            viewModel.onScrollToBottom()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                AudioMessageList(messages = messages, onPlayMessage, onStopMessage, messageListState, isMessageLoading, Modifier.weight(1f))
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
        }
    }
}

@Composable
fun AudioMessageItem(
    message: ChatMessage,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onIconClick = {
        if (message.isPlaying) {
            onStopMessage()
        } else {
            onPlayMessage(message.id)
        }
    }

    val isUserMessage = message.author == Author.USER
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (isUserMessage) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                ),
            shape =
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUserMessage) 16.dp else 4.dp,
                    bottomEnd = if (isUserMessage) 4.dp else 16.dp,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onIconClick) {
                    Icon(
                        imageVector =
                            if (message.isPlaying) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                        contentDescription = null,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (message.isPlaying) {
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(4.dp),
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(4.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatDuration(message.duration),
                    style = MaterialTheme.typography.labelMedium,
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
