package eu.rozmova.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.modules.chat.MessageUI
import eu.rozmova.app.modules.convochat.components.shouldfinishdialog.ShouldFinishAudioEvents
import eu.rozmova.app.modules.convochat.components.shouldfinishdialog.ShouldFinishAudioVM
import eu.rozmova.app.modules.convochat.components.shouldfinishdialog.ShouldFinishUiState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private data class Handlers(
    val onYesClick: () -> Unit,
    val onDismiss: () -> Unit,
    val toSubscription: () -> Unit,
    val playUsrMsg: () -> Unit,
    val playBotMsg: () -> Unit,
    val stopPlay: () -> Unit,
)

@Composable
fun ShouldFinishDialog(
    lastBotMsg: MessageUI,
    lastUserMsg: MessageUI,
    chatType: ChatType,
    onYesClick: () -> Unit,
    onDismiss: () -> Unit,
    toSubscription: () -> Unit,
    viewModel: ShouldFinishAudioVM = hiltViewModel(),
) {
    var botMsg by remember { mutableStateOf(lastBotMsg) }
    var userMsg by remember { mutableStateOf(lastUserMsg) }
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            ShouldFinishAudioEvents.StopAll -> {
                botMsg = botMsg.copy(isPlaying = false)
                userMsg = userMsg.copy(isPlaying = false)
            }
        }
    }

    Content(
        state,
        userMsg,
        botMsg,
        chatType,
        Handlers(
            onYesClick = onYesClick,
            onDismiss = {
                viewModel.stopAudio()
                onDismiss()
            },
            toSubscription = toSubscription,
            playUsrMsg = {
                viewModel.stopAudio()
                botMsg = botMsg.copy(isPlaying = false)
                userMsg = userMsg.copy(isPlaying = true)
                viewModel.playAudio(userMsg.dto)
            },
            playBotMsg = {
                viewModel.stopAudio()
                userMsg = userMsg.copy(isPlaying = false)
                botMsg = botMsg.copy(isPlaying = true)
                viewModel.playAudio(botMsg.dto)
            },
            stopPlay = {
                viewModel.stopAudio()
                userMsg = userMsg.copy(isPlaying = false)
                botMsg = botMsg.copy(isPlaying = false)
            },
        ),
    )
}

@Composable
private fun Content(
    state: ShouldFinishUiState,
    lastUserMsg: MessageUI,
    lastBotMsg: MessageUI,
    chatType: ChatType,
    handlers: Handlers,
) {
    AlertDialog(
        onDismissRequest = handlers.onDismiss,
        title = {
            Text(
                text = stringResource(R.string.should_finish_chat_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (chatType == ChatType.SPEAKING) {
                    AudioMessageItem(
                        lastUserMsg,
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
                        onPlayMessage = { handlers.playUsrMsg() },
                        onStopMessage = { handlers.stopPlay() },
                        isSubscribed = state.isSubscribed,
                        navigateToSubscription = handlers.toSubscription,
                    )
                    AudioMessageItem(
                        lastBotMsg,
                        modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
                        onPlayMessage = {
                            handlers.playBotMsg()
                        },
                        onStopMessage = {
                            handlers.stopPlay()
                        },
                        navigateToSubscription = handlers.toSubscription,
                        isSubscribed = state.isSubscribed,
                    )
                    return@Column
                }
                MessageItem(lastUserMsg, modifier = Modifier.fillMaxWidth())
                MessageItem(lastBotMsg, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    handlers.onYesClick()
                },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.should_finish_chat_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    handlers.onDismiss()
                },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.should_finish_chat_dismiss))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview
@Composable
private fun YesNoDialogPreview() {
    Content(
        lastUserMsg =
            MessageUI(
                MessageDto(
                    id = "1",
                    content = "Danke.",
                    author = Author.USER,
                    audioId = null,
                    link = "",
                ),
                false,
            ),
        lastBotMsg =
            MessageUI(
                MessageDto(
                    id = "2",
                    content = "You're welcome.",
                    author = Author.BOT,
                    audioId = null,
                    link = "",
                ),
                false,
            ),
        chatType = ChatType.SPEAKING,
        state =
            ShouldFinishUiState(
                isSubscribed = true,
            ),
        handlers =
            Handlers(
                onYesClick = {},
                onDismiss = {},
                toSubscription = {},
                playUsrMsg = {},
                playBotMsg = {},
                stopPlay = {},
            ),
    )
}
