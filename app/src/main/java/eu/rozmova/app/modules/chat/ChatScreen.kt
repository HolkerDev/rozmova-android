package eu.rozmova.app.modules.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.AudioRecorderButton
import eu.rozmova.app.components.InstructionsButton
import eu.rozmova.app.components.MessageInput
import eu.rozmova.app.components.ShouldFinishDialog
import eu.rozmova.app.components.SituationButton
import eu.rozmova.app.components.conversationchat.AudioMessageList
import eu.rozmova.app.components.messagechat.FinishChat
import eu.rozmova.app.components.messagechat.MessageList
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.modules.chat.components.ReviewDialog
import eu.rozmova.app.modules.chat.components.RightTranslationBar
import eu.rozmova.app.modules.chat.components.SituationDialog
import eu.rozmova.app.modules.chat.components.UserInstructionsDialog
import eu.rozmova.app.modules.shared.HelperWords
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private data class Handlers(
    val finishChat: () -> Unit,
    val onFinishRejected: () -> Unit,
    val toSubscription: () -> Unit,
    val playAudio: (messageId: String) -> Unit,
    val stopAudio: () -> Unit,
    val sendMessage: (content: String) -> Unit,
    val startRecording: () -> Unit,
    val stopRecording: () -> Unit,
    val back: () -> Unit,
)

@Composable
fun ChatScreen(
    chatId: String,
    toReview: (reviewId: String) -> Unit,
    toSubscription: () -> Unit,
    back: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    var finishChatModal: FinishChat? by remember { mutableStateOf(null) }
    val messageListState = rememberLazyListState()

    viewModel.collectSideEffect { event ->
        when (event) {
            ChatEvents.ScrollToBottom ->
                state.chat?.takeIf { it.messages.size > 1 }?.let { chat ->
                    messageListState.animateScrollToItem(
                        chat.messages.size - 1,
                    )
                }

            is ChatEvents.ReviewReady -> toReview(event.reviewId)
            is ChatEvents.ProposeFinish ->
                FinishChat(
                    lastBotMsg = event.lastBotMsg,
                    lastUserMsg = event.lastUserMsg,
                    chatType = event.chatType,
                )
        }
    }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    Content(
        state,
        finishChatModal,
        listRef = messageListState,
        handlers =
            Handlers(
                finishChat = {
                    viewModel.stopAudio()
                    viewModel.finishChat(chatId)
                },
                onFinishRejected = {
                    viewModel.stopAudio()
                    finishChatModal = null
                },
                toSubscription = {
                    viewModel.stopAudio()
                    toSubscription()
                },
                stopAudio = { viewModel.stopAudio() },
                playAudio = { msgId: String -> viewModel.playAudio(msgId) },
                back = back,
                startRecording = {
                    viewModel.startRecording()
                },
                stopRecording = {
                    viewModel.stopRecording()
                },
                sendMessage = { msg -> viewModel.sendMessage(chatId, msg) },
            ),
        modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: ChatState,
    finishChatModal: FinishChat?,
    listRef: LazyListState,
    handlers: Handlers,
    modifier: Modifier = Modifier,
) {
    var showHelpModal by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var showSituationDialog by remember { mutableStateOf(false) }
    var showWordsBottomSheet by remember { mutableStateOf(false) }

    finishChatModal?.let { data ->
        ShouldFinishDialog(
            lastBotMsg = data.lastBotMsg,
            lastUserMsg = data.lastUserMsg,
            onYesClick = {
                handlers.finishChat()
            },
            onDismiss = {
                handlers.onFinishRejected()
            },
            chatType = data.chatType,
            toSubscription = handlers.toSubscription,
        )
    }

    if (state.isReviewLoading) {
        ReviewDialog()
    }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = { Text(text = stringResource(R.string.chat_details_title)) },
            navigationIcon = {
                IconButton(onClick = handlers.back) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            actions = {
                IconButton(onClick = { showHelpModal = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = "How do I say that?",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            if (state.chat == null) {
                CircularProgressIndicator()
                return@Column
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                Column {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxHeight(),
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
                                    text = state.chat.scenario.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                ) // Title

                                if (state.chat.scenario.helperWords
                                        .isNotEmpty()
                                ) {
                                    FilledTonalButton(
                                        onClick = { showWordsBottomSheet = true },
                                        colors = ButtonDefaults.filledTonalButtonColors(),
                                        modifier = Modifier.padding(start = 8.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        contentPadding =
                                            PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 6.dp,
                                            ),
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
                                SituationButton(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clickable { showSituationDialog = true },
                                )
                                InstructionsButton(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clickable { showInstructionsDialog = true },
                                )
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )

                            if (state.chat.chatType == ChatType.WRITING) {
                                MessageList(
                                    messages = state.messages,
                                    onChatFinish = handlers.finishChat,
                                    messageListState = listRef,
                                    isLoadingMessage = state.isMessageLoading,
                                    showFinishButton = state.messages.isNotEmpty() && state.chat.status == ChatStatus.IN_PROGRESS,
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                AudioMessageList(
                                    messages = state.messages,
                                    onPlayMessage = handlers.playAudio,
                                    onStopMessage = handlers.stopAudio,
                                    onChatFinish = handlers.finishChat,
                                    messageListState = listRef,
                                    isLoadingMessage = state.isMessageLoading,
                                    isSubscribed = true,
                                    navigateToSubscription = handlers.toSubscription,
                                    showFinishButton = state.messages.isNotEmpty() && state.chat.status == ChatStatus.IN_PROGRESS,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
            if (state.isError) {
                Text(
                    text = stringResource(R.string.error_generic),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).align(Alignment.CenterHorizontally),
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (state.chat.chatType == ChatType.WRITING) {
                MessageInput(
                    onSendMessage = handlers.sendMessage,
                    isDisabled = state.isMessageLoading,
                )
            } else {
                AudioRecorderButton(
                    onRecordStart = handlers.startRecording,
                    onRecordStop = handlers.stopRecording,
                    isDisabled = state.isMessageLoading,
                    isRecording = state.isAudioRecording,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }

        if (state.chat == null) {
            return@Scaffold
        }
        RightTranslationBar(
            chatId = state.chat.id,
            show = showHelpModal,
            onClose = { showHelpModal = false },
        )

        if (showSituationDialog) {
            SituationDialog(
                state.chat.scenario.situation,
                onClose = { showSituationDialog = false },
            )
        }

        if (showInstructionsDialog) {
            UserInstructionsDialog(
                state.chat.scenario.userInstructions,
                onClose = { showInstructionsDialog = false },
            )
        }

        if (showWordsBottomSheet) {
            HelperWords(
                words = state.chat.scenario.helperWords,
                onDismiss = { showWordsBottomSheet = false },
                navigateToSubscription = {
                    showWordsBottomSheet = false
                    handlers.toSubscription()
                },
                isSubscribed = true,
            )
        }
    }
}
