package eu.rozmova.app.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.rozmova.app.R
import eu.rozmova.app.domain.ChatAnalysis
import eu.rozmova.app.domain.Requirements
import eu.rozmova.app.domain.TaskCompletion
import eu.rozmova.app.domain.TopicToReview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAnalysisDialog(
    chatAnalysis: ChatAnalysis,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Header
                TopAppBar(
                    title = { Text(text = stringResource(R.string.chat_analysis_title)) },
                    navigationIcon = {
                        IconButton(onClick = onConfirm) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_content_description))
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp),
                    ) {
                        // Task Completion Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(R.string.chat_analysis_task_completion),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    CompletionStatusChip(chatAnalysis.taskCompletion.isCompleted)
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                RatingBar(chatAnalysis.taskCompletion.rating)
                                Spacer(modifier = Modifier.height(16.dp))
                                RequirementsSection(chatAnalysis.taskCompletion.requirements)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Topics to Review Section
                        if (chatAnalysis.topicsToReview.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.chat_analysis_topics_to_refresh),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            TopicsToReviewList(chatAnalysis.topicsToReview)
                        }
                    }
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isLoading) {
                        Text(text = stringResource(R.string.loading_progress))
                    } else {
                        Text(text = stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicsToReviewList(topics: List<TopicToReview>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        topics.forEach { topic ->
            TopicItem(topic)
        }
    }
}

@Composable
private fun CompletionStatusChip(isCompleted: Boolean) {
    Surface(
        color =
            if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector =
                    if (isCompleted) {
                        Icons.Rounded.CheckCircle
                    } else {
                        Icons.Rounded.Cancel
                    },
                contentDescription = null,
                tint =
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isCompleted) stringResource(R.string.completed) else stringResource(R.string.incompleted),
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }
    }
}

@Composable
private fun RatingBar(rating: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(5) { index ->
            Icon(
                imageVector =
                    if (index < rating) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.Star
                    },
                contentDescription = null,
                tint =
                    if (index < rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun RequirementsSection(requirements: Requirements) {
    Column {
        if (requirements.met.isNotEmpty()) {
            Text(
                text = stringResource(R.string.met_requirements),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            requirements.met.forEach { requirement ->
                RequirementItem(
                    requirement = requirement,
                    isMet = true,
                )
            }
        }

        if (requirements.missed.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.missed_requirements),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            requirements.missed.forEach { requirement ->
                RequirementItem(
                    requirement = requirement,
                    isMet = false,
                )
            }
        }
    }
}

@Composable
private fun RequirementItem(
    requirement: String,
    isMet: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector =
                if (isMet) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.Cancel
                },
            contentDescription = null,
            tint =
                if (isMet) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = requirement,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TopicItem(topic: TopicToReview) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val copiedToClipboardMsg = stringResource(R.string.copied_to_clipboard)

    val onCopyClick = {
        val clip = ClipData.newPlainText("topic", topic.topic)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clipboardManager.setPrimaryClip(clip)
        } else {
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, copiedToClipboardMsg, Toast.LENGTH_SHORT).show()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onCopyClick() }
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = Icons.Rounded.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = topic.topic,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copy ${topic.topic}",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ChatAnalysisDialogPreview() {
    var isLoading by remember { mutableStateOf(false) }

    ChatAnalysisDialog(
        chatAnalysis =
            ChatAnalysis(
                taskCompletion =
                    TaskCompletion(
                        isCompleted = false,
                        rating = 4,
                        requirements =
                            Requirements(
                                met = listOf("Requirement 1", "Requirement 2"),
                                missed = listOf("Requirement 3"),
                            ),
                    ),
                topicsToReview =
                    listOf(
                        TopicToReview("Very long topic name, that is longer and longer 1"),
                        TopicToReview("Topic 2"),
                        TopicToReview("Topic 3"),
                    ),
            ),
        onConfirm = { isLoading = !isLoading },
        isLoading = isLoading,
    )
}
