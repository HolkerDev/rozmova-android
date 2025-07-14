package eu.rozmova.app.modules.review

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
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.MistakeDto
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.domain.TaskCompletionDto
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun ReviewScreen(
    reviewId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.fetchReview(reviewId)
    }

    Content(state, onClose = { onClose() }, modifier)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Content(
    state: ReviewState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(R.string.chat_analysis_title)) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_content_description),
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )
    }) { paddingValues ->
        // Loading state
        if (state.review == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Surface(
            modifier =
                modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
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
                                    CompletionStatusChip(state.review.taskCompletion.isCompleted)
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                RatingBar(state.review.taskCompletion.rating)
                                Spacer(modifier = Modifier.height(16.dp))
                                RequirementsSection(
                                    state.review.taskCompletion.metInstructions,
                                    state.review.taskCompletion.missedInstructions,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mistakes Section
                        if (state.review.taskCompletion.mistakes
                                .isNotEmpty()
                        ) {
                            Text(
                                text = stringResource(R.string.chat_analysis_mistakes),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            MistakesSection(state.review.taskCompletion.mistakes)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Topics to Review Section
                        if (state.review.topicsToReview.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.chat_analysis_topics_to_refresh),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            TopicsToReviewList(state.review.topicsToReview)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Topics to Review Section
                        if (state.review.wordsToLearn.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.words_to_learn),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            WordsToLearn(state.review.wordsToLearn)
                        }
                    }
                }
                Button(
                    onClick = onClose,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Composable
private fun TopicsToReviewList(topics: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        topics.forEach { topic ->
            TopicItem(topic)
        }
    }
}

@Composable
private fun WordsToLearn(words: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        words.forEach { topic ->
            WordItem(topic)
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
        repeat(3) { index ->
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
private fun RequirementsSection(
    metInstructions: List<String>,
    missedInstructions: List<String>,
) {
    Column {
        metInstructions.takeIf { it.isNotEmpty() }?.let { met ->
            Text(
                text = stringResource(R.string.met_requirements),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            met.forEach { requirement ->
                RequirementItem(
                    requirement = requirement,
                    isMet = true,
                )
            }
        }

        missedInstructions.takeIf { it.isNotEmpty() }?.let { missed ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.missed_requirements),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            missed.forEach { requirement ->
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
private fun WordItem(word: String) {
    val context = LocalContext.current
    val clipboardManager =
        remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val copiedToClipboardMsg = stringResource(R.string.copied_to_clipboard)

    val onCopyClick = {
        val clip = ClipData.newPlainText("word", word)
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
                imageVector = Icons.Rounded.Translate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = word,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copy $word",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun TopicItem(topic: String) {
    val context = LocalContext.current
    val clipboardManager =
        remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val copiedToClipboardMsg = stringResource(R.string.copied_to_clipboard)

    val onCopyClick = {
        val clip = ClipData.newPlainText("topic", topic)
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
                text = topic,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copy $topic",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun parseAsteriskText(text: String): AnnotatedString =
    buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            val asteriskIndex = text.indexOf('*', currentIndex)
            if (asteriskIndex == -1) {
                append(text.substring(currentIndex))
                break
            }

            // Add text before asterisk
            append(text.substring(currentIndex, asteriskIndex))

            // Find closing asterisk
            val closingAsteriskIndex = text.indexOf('*', asteriskIndex + 1)
            if (closingAsteriskIndex == -1) {
                append(text.substring(asteriskIndex))
                break
            }

            // Add bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(asteriskIndex + 1, closingAsteriskIndex))
            }

            currentIndex = closingAsteriskIndex + 1
        }
    }

@Composable
private fun MistakesSection(mistakes: List<MistakeDto>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        mistakes.forEach { mistake ->
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
                    // Wrong version with strikethrough
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseAsteriskText(mistake.wrong),
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                ),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Correct version with arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseAsteriskText(mistake.correct),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChatAnalysisDialogPreview() {
    Content(
        onClose = {},
        state =
            ReviewState(
                ReviewDto(
                    taskCompletion =
                        TaskCompletionDto(
                            isCompleted = true,
                            metInstructions = listOf("you were good"),
                            missedInstructions = listOf("you were bad"),
                            mistakes =
                                listOf(
                                    MistakeDto(
                                        wrong = "I *has* a dog",
                                        correct = "I *have* a dog",
                                    ),
                                    MistakeDto(
                                        wrong = "I *has* a dog",
                                        correct = "I *have* a dog",
                                    ),
                                ),
                            rating = 2,
                        ),
                    topicsToReview =
                        listOf(
                            "Akkusativ",
                        ),
                    wordsToLearn =
                        listOf(
                            "der Junge",
                        ),
                ),
            ),
    )
}
