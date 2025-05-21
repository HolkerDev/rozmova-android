package eu.rozmova.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eu.rozmova.app.R
import eu.rozmova.app.domain.Author
import eu.rozmova.app.domain.MessageDto

@Composable
fun ShouldFinishChatDialog(
    lastBotMsg: MessageDto,
    lastUserMsg: MessageDto,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                MessageItem(lastUserMsg, modifier = Modifier.fillMaxWidth())
                MessageItem(lastBotMsg, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
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
private fun YesNoDialogPreview() {
    ShouldFinishChatDialog(
        lastUserMsg =
            MessageDto(
                id = "1",
                content = "Danke.",
                author = Author.USER,
                audioId = null,
                link = "",
            ),
        lastBotMsg =
            MessageDto(
                id = "2",
                content = "You're welcome.",
                author = Author.BOT,
                audioId = null,
                link = "",
            ),
        onYesClick = {},
        onNoClick = {},
        onDismiss = { },
    )
}
