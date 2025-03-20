package eu.rozmova.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.domain.Author
import eu.rozmova.app.screens.chatdetails.AudioChatMessage
import eu.rozmova.app.utils.formatDuration

@Composable
fun AudioMessageItem(
    message: AudioChatMessage,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTranscription by remember { mutableStateOf(false) }
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
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Row(
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

                    // Transcription toggle button
                    IconButton(
                        onClick = { showTranscription = !showTranscription },
                    ) {
                        Icon(
                            imageVector =
                                if (showTranscription) {
                                    Icons.Rounded.ExpandLess
                                } else {
                                    Icons.Rounded.ExpandMore
                                },
                            contentDescription =
                                if (showTranscription) {
                                    "Hide transcription"
                                } else {
                                    "Show transcription"
                                },
                        )
                    }
                }

                // Animated transcription text
                AnimatedVisibility(
                    visible = showTranscription,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Text(
                        text = message.body,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AudioMessageItemPreview() {
    AudioMessageItem(
        message =
            AudioChatMessage(
                id = "1",
                author = Author.USER,
                body = "Hello, how are you?",
                duration = 10000,
                isPlaying = false,
                link = "randomLink",
            ),
        onPlayMessage = {},
        onStopMessage = {},
    )
}
