package eu.rozmova.app.modules.chatlist.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.modules.createchat.ChatTypeUI
import eu.rozmova.app.modules.createchat.toUI

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: ChatDto,
    onChatClick: (String) -> Unit,
    onChatDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_chat)) },
            text = { Text(stringResource(R.string.delete_chat_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChatDelete(chat.id)
                        showDeleteDialog = false
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .scale(scale)
                .clip(MaterialTheme.shapes.medium)
                .combinedClickable(
                    onClick = { onChatClick(chat.id) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isPressed = true
                        showDeleteDialog = true
                    },
                    onLongClickLabel = "Delete chat",
                ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Status indicator and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    ChatTypeIconWithBackground(chatType = chat.chatType.toUI())
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = chat.scenario.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview of the situation
            Text(
                text = chat.scenario.situation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ChatTypeIconWithBackground(
    chatType: ChatTypeUI,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when (chatType) {
            ChatTypeUI.SPEAKING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ChatTypeUI.WRITING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        }

    val iconTint =
        when (chatType) {
            ChatTypeUI.WRITING -> MaterialTheme.colorScheme.primary
            ChatTypeUI.SPEAKING -> MaterialTheme.colorScheme.tertiary
        }

    val icon =
        when (chatType) {
            ChatTypeUI.SPEAKING -> Icons.Default.RecordVoiceOver
            ChatTypeUI.WRITING -> Icons.AutoMirrored.Filled.Chat
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(30.dp)
                .clip(MaterialTheme.shapes.small)
                .background(backgroundColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Chat status: $chatType",
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
    }
}
