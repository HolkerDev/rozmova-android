package eu.rozmova.app.screens.chatdetails

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.clients.domain.Author
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.domain.ScenarioModel

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
    val audioState by viewModel.state.collectAsState()
    val chatState by viewModel.state.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
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
                    onStopMessage = { },
                    isRecording = isRecording,
                    scenario = chat.scenario,
                    messages =
                        chatState.messages?.map {
                            Log.i("ChatDetailScreen", "ChatMessage: $it")
                            AudioMessage(
                                id = it.id,
                                isFromUser = it.author == Author.USER,
                                duration = "0",
                                isPlaying = it.isPlaying,
                                progress = 0f,
                            )
                        } ?: emptyList(),
                )
            } ?: LoadingComponent(onBackClick)
        }
    }
}

@Composable
fun ScenarioInfoCard(
    scenario: ScenarioModel,
    messages: List<AudioMessage>,
    onBackClick: () -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
//    val messages =
//        listOf(
//            AudioMessage(
//                id = "1",
//                isFromUser = true,
//                duration = "1:23",
//                isPlaying = false,
//                progress = 0.5f,
//            ),
//            AudioMessage(
//                id = "2",
//                isFromUser = false,
//                duration = "2:34",
//                isPlaying = true,
//                progress = 0.7f,
//            ),
//            AudioMessage(
//                id = "3",
//                isFromUser = true,
//                duration = "3:45",
//                isPlaying = false,
//                progress = 0.3f,
//            ),
//            AudioMessage(
//                id = "4",
//                isFromUser = false,
//                duration = "4:56",
//                isPlaying = false,
//                progress = 0.1f,
//            ),
//        )
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
                AudioMessageList(messages = messages, onPlayMessage, onStopMessage, Modifier.weight(1f))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                AudioRecorderButton(
                    onRecordStart = onRecordStart,
                    onRecordStop = onRecordStop,
                    isRecording = isRecording,
                )
            }
        }
    }
}

@Composable
fun AudioMessageList(
    messages: List<AudioMessage>,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(messages) { message ->
            AudioMessageItem(
                message = message,
                onPlayMessage = onPlayMessage,
                onStopMessage = onStopMessage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun AudioMessageItem(
    message: AudioMessage,
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

    val isUserMessage = message.isFromUser
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

                LinearProgressIndicator(
                    progress = { message.progress },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(4.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message.duration,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

data class AudioMessage(
    val id: String,
    val isFromUser: Boolean,
    val duration: String,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
)

@Composable
fun AudioRecordButton(
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1.7f,
        targetValue = 1.9f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(850),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "",
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.9f,
        targetValue = 2.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800), // Different duration
                repeatMode = RepeatMode.Reverse,
            ),
        label = "",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isRecording) {
            // First animation
            Box(
                Modifier
                    .size(48.dp)
                    .scale(scale1)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        CircleShape,
                    ),
            )
            // Second animation
            Box(
                Modifier
                    .size(48.dp)
                    .scale(scale2)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        CircleShape,
                    ),
            )
        }

        FloatingActionButton(
            onClick = {
                onRecordClick()
            },
            containerColor =
                if (isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
            )
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
