package eu.rozmova.app.screens.chatdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import eu.rozmova.app.clients.domain.ChatWithMessagesDto
import eu.rozmova.app.clients.domain.Owner
import eu.rozmova.app.components.SimpleToolBar

@Composable
fun ChatDetailScreen(
    onBackClick: () -> Unit,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadChat(chatId)
    }
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        when (val viewState = state) {
            ChatDetailState.Empty -> LoadingComponent(onBackClick)
            ChatDetailState.Loading -> LoadingComponent(onBackClick)

            is ChatDetailState.Success -> {
                ChatDetails(
                    chatWithMessages = viewState.chat,
                    onBackClick = onBackClick,
                    messages = viewState.messages,
                    viewModel = viewModel,
                )
            }

            is ChatDetailState.Error -> ErrorComponent(viewState.msg, onBackClick)
        }
    }
}

@Composable
private fun ChatDetails(
    chatWithMessages: ChatWithMessagesDto,
    messages: List<ChatMessage>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
    Column(modifier = modifier.fillMaxSize()) {
        SimpleToolBar(title = chatWithMessages.title, onBack = onBackClick)
        TaskDetailComponent(
            chatWithMessages.description,
            chatWithMessages.userInstructions,
        )
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            items(messages) { message ->
                ChatMessageComponent(
                    chatMessage = message,
                    onStopClick = { viewModel.stopAudio(message.id) },
                    onPlayClick = { viewModel.playAudio(message.id, message.link) },
                )
            }
        }
        SpeechRecognitionComponent()
    }
}

@Composable
private fun ChatMessageComponent(
    chatMessage: ChatMessage,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUser = chatMessage.owner == Owner.USER
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            ),
    ) {
        Column(
            modifier = Modifier.Companion.padding(16.dp),
            horizontalAlignment =
                if (isUser) {
                    androidx.compose.ui.Alignment.End
                } else {
                    androidx.compose.ui.Alignment.Start
                },
        ) {
            Text(chatMessage.body)
            if (chatMessage.link.isNotEmpty()) {
                IconButton(onClick = {
                    if (chatMessage.isPlaying) {
                        onStopClick()
                    } else {
                        onPlayClick()
                    }
                }) {
                    Icon(
                        imageVector =
                            if (chatMessage.isPlaying) {
                                Icons.Default.Stop
                            } else {
                                Icons.Default.PlayArrow
                            },
                        contentDescription = "Play/Stop",
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailComponent(
    description: String,
    userInstruction: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Task Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Companion.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))
        Text(
            text = "Language Level: A2",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Companion.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.Companion.height(4.dp))
        Text(
            text = userInstruction,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingComponent(onBackClick: () -> Unit) {
    SimpleToolBar("Loading...", onBack = onBackClick)
    CircularProgressIndicator()
}

@Composable
private fun ErrorComponent(
    errorMessage: String,
    onBackClick: () -> Unit,
) {
    SimpleToolBar("Error", onBack = onBackClick)
    Text("Error: $errorMessage")
}
