package eu.rozmova.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.rozmova.app.domain.Author
import eu.rozmova.app.screens.chatdetails.ChatMessage
import eu.rozmova.app.utils.formatDuration

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
