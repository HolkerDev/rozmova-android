package eu.rozmova.app.components.messagechat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.ChatAnalysisDialog
import eu.rozmova.app.components.MessageInput
import eu.rozmova.app.components.MessageItem
import eu.rozmova.app.components.ShouldFinishChatDialog
import eu.rozmova.app.components.StopChatButton
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.modules.generatechat.ChatId
import eu.rozmova.app.modules.shared.HelperWords
import eu.rozmova.app.modules.shared.translationproposal.TranslationProposalModal
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

data class FinishChat(
    val lastBotMsg: MessageDto,
    val lastUserMsg: MessageDto,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageChat(
    chatId: ChatId,
    onReviewAccept: () -> Unit,
    onBackClick: () -> Unit,
    toReview: (reviewId: String) -> Unit,
    navigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageChatViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    val messageListState = rememberLazyListState()
    var finishChat: FinishChat? by remember { mutableStateOf(null) }
    var contextualTranslatorShow by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    suspend fun scrollToBottom() {
        state.chat?.messages?.takeIf { it.isNotEmpty() }?.let { messages ->
            messageListState.animateScrollToItem(messages.size - 1)
        }
    }

    viewModel.collectSideEffect { event ->
        when (event) {
            MessageChatEvent.Close -> {}
            is MessageChatEvent.ProposeFinish ->
                finishChat =
                    FinishChat(
                        lastBotMsg = event.lastBotMsg,
                        lastUserMsg = event.lastUserMsg,
                    )
            MessageChatEvent.ScrollToBottom -> scrollToBottom()
            is MessageChatEvent.ShowReview -> toReview(event.reviewId)
        }
    }

    fun onMessageSend(content: String) {
        viewModel.sendMessage(chatId, content)
    }

    finishChat?.let { data ->
        ShouldFinishChatDialog(
            lastBotMsg = data.lastBotMsg,
            lastUserMsg = data.lastUserMsg,
            onYesClick = {
                finishChat = null
                viewModel.finishChat(chatId)
            },
            onNoClick = {
                finishChat = null
            },
            onDismiss = {
                finishChat = null
            },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main content
        Column(modifier = Modifier.fillMaxSize()) {
            state.review?.let {
                ChatAnalysisDialog(
                    review = it,
                    onConfirm = { onReviewAccept() },
                    isLoading = false,
                )
            }

            if (state.isLoadingReview) {
                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text(
                            stringResource(R.string.analysing_conversation),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.analyse_wait),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    confirmButton = { },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                )
            }

            state.chat?.let { chat ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                ) {
                    ScenarioInfoCard(
                        chat = chat,
                        onBackClick = onBackClick,
                        onChatFinish = { viewModel.finishChat(chat.id) },
                        isMessageLoading = state.isLoadingMessage,
                        messageListState = messageListState,
                        isSubscribed = state.isSubscribed,
                        navigateToSubscription = navigateToSubscription,
                        onTranslateClick = { contextualTranslatorShow = true },
                        modifier = Modifier.weight(1f),
                    )
                    MessageInput(
                        onSendMessage = ::onMessageSend,
                        isDisabled = state.isLoadingMessage || chat.status != ChatStatus.IN_PROGRESS,
                    )
                }
            }
        }

        // Translation modal overlay - moved outside the main Column
        AnimatedVisibility(
            visible = contextualTranslatorShow,
            enter =
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300),
                ),
            exit =
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300),
                ),
        ) {
            TranslationProposalModal(
                chatId = chatId,
                onDismiss = { contextualTranslatorShow = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioInfoCard(
    chat: ChatDto,
    onBackClick: () -> Unit,
    onChatFinish: () -> Unit,
    isMessageLoading: Boolean,
    messageListState: LazyListState,
    isSubscribed: Boolean,
    navigateToSubscription: () -> Unit,
    onTranslateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showWordsBottomSheet by remember { mutableStateOf(false) }
    var showSituationDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(R.string.message_chat_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            actions = {
                IconButton(onClick = onTranslateClick) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = "How do I say that?",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = chat.scenario.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (chat.scenario.helperWords.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { showWordsBottomSheet = true },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier.padding(start = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CollectionsBookmark,
                                contentDescription = stringResource(R.string.helper_words),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // Compact information cards in a Row
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Situation card
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable { showSituationDialog = true },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.situation),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }

                    // Instructions card
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable { showInstructionsDialog = true },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Task,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.instructions),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                MessageList(
                    messages = chat.messages,
                    onChatFinish = onChatFinish,
                    messageListState = messageListState,
                    showFinishButton = chat.messages.isNotEmpty() && chat.status == ChatStatus.IN_PROGRESS,
                    isLoadingMessage = isMessageLoading,
                )
            }
        }
    }

    if (showWordsBottomSheet) {
        HelperWords(
            words = chat.scenario.helperWords,
            onDismiss = { showWordsBottomSheet = false },
            isSubscribed = isSubscribed,
            navigateToSubscription = {
                showWordsBottomSheet = false
                navigateToSubscription()
            },
        )
    }

    if (showSituationDialog) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            onDismissRequest = { showSituationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.situation),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = chat.scenario.situation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSituationDialog = false },
                ) {
                    Text(stringResource(R.string.close_content_description))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier =
                Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .heightIn(max = screenHeight * 0.8f),
            shape = RoundedCornerShape(16.dp),
        )
    }

    if (showInstructionsDialog) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            onDismissRequest = { showInstructionsDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.instructions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text =
                            chat.scenario.userInstructions.joinToString("\n\n") { it.task },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInstructionsDialog = false },
                ) {
                    Text(stringResource(R.string.close_content_description))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier =
                Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .heightIn(max = screenHeight * 0.8f),
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
fun MessageList(
    messages: List<MessageDto>,
    onChatFinish: () -> Unit,
    messageListState: LazyListState,
    isLoadingMessage: Boolean,
    showFinishButton: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        state = messageListState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(messages) { message ->
            MessageItem(
                message = message,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (isLoadingMessage) {
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.answering),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        } else if (showFinishButton) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StopChatButton(
                    onClick = onChatFinish,
                )
            }
        }
    }
}
