package eu.rozmova.app.modules.convochat.components.shouldfinishdialog

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
import eu.rozmova.app.components.AudioMessageItem
import eu.rozmova.app.components.conversationchat.toAudioMessage
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.MessageDto
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ShouldFinishAudioDialog(
    lastBotMsg: MessageDto,
    lastUserMsg: MessageDto,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    onDismiss: () -> Unit,
    navigateToSubscription: () -> Unit,
    viewModel: ShouldFinishAudioVM = hiltViewModel(),
) {
    var botMsg by remember { mutableStateOf(lastBotMsg.toAudioMessage()) }
    var userMsg by remember { mutableStateOf(lastUserMsg.toAudioMessage()) }
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            ShouldFinishAudioEvents.StopAll -> {
                botMsg = botMsg.copy(isPlaying = false)
                userMsg = userMsg.copy(isPlaying = false)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.should_finish_chat_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AudioMessageItem(
                    userMsg,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
                    onPlayMessage = {
                        userMsg = userMsg.copy(isPlaying = true)
                        viewModel.playAudio(lastUserMsg)
                    },
                    onStopMessage = {
                        userMsg = userMsg.copy(isPlaying = false)
                        viewModel.stopAudio()
                    },
                    isSubscribed = false,
                    navigateToSubscription = navigateToSubscription,
                )
                AudioMessageItem(
                    botMsg,
                    modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
                    onPlayMessage = {
                        botMsg = botMsg.copy(isPlaying = true)
                        viewModel.playAudio(lastBotMsg)
                    },
                    onStopMessage = {
                        botMsg = botMsg.copy(isPlaying = false)
                        viewModel.stopAudio()
                    },
                    navigateToSubscription = navigateToSubscription,
                    isSubscribed = state.isSubscribed,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.stopAudio()
                    onYesClick()
                    onDismiss()
                },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.should_finish_chat_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.stopAudio()
                    onNoClick()
                    onDismiss()
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
private fun Preview() {
    ShouldFinishAudioDialog(
        lastBotMsg =
            MessageDto(
                id = "",
                content = "test1",
                author = Author.BOT,
                audioId = "123",
                link = "",
            ),
        lastUserMsg =
            MessageDto(
                id = "",
                content = "test2",
                author = Author.USER,
                audioId = "123",
                link = "",
            ),
        onYesClick = {},
        onNoClick = {},
        onDismiss = {},
        navigateToSubscription = {},
    )
}
