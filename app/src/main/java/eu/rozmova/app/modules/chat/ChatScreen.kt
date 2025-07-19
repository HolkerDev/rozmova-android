package eu.rozmova.app.modules.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.AudioRecorderButton
import eu.rozmova.app.components.ShouldFinishChatDialog
import eu.rozmova.app.components.conversationchat.ScenarioInfoCard
import eu.rozmova.app.components.conversationchat.toAudioMessage
import eu.rozmova.app.components.messagechat.FinishChat
import eu.rozmova.app.modules.chat.components.ReviewDialog
import eu.rozmova.app.modules.chat.components.RightTranslationBar
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private data class Handlers(
    val finishChat: () -> Unit,
    val onFinishRejected: () -> Unit,
    val toSubscription: () -> Unit,
    val playAudio: (messageId: String) -> Unit,
    val stopAudio: () -> Unit,
    val startRecording: () -> Unit,
    val stopRecording: () -> Unit,
    val back: () -> Unit,
)

@Composable
fun ChatScreen(
    chatId: String,
    toReview: (reviewId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    var finishChatModal: FinishChat? by remember { mutableStateOf(null) }
    val messageListState = rememberLazyListState()

    viewModel.collectSideEffect { event ->
        when (event) {
//            is ConvoChatEvents.ProposeFinish ->
//                finishChat =
//                    FinishChat(
//                        lastBotMsg = event.lastBotMsg,
//                        lastUserMsg = event.lastUserMsg,
//                    )
            ChatEvents.ScrollToBottom ->
                state.chat?.takeIf { it.messages.size > 1 }?.let { chat ->
                    messageListState.animateScrollToItem(
                        chat.messages.size - 1,
                    )
                }

            is ChatEvents.ReviewReady -> toReview(event.reviewId)
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
//                    viewModel.stopAudio()
//                    viewModel.finishChat(chatId)
                },
                onFinishRejected = {
//                    viewModel.stopAudio()
//                    finishChatModal = null
                },
                toSubscription = {
//                    viewModel.stopAudio()
//                    onNavigateToSubscription()
                },
                stopAudio = {},
                playAudio = {},
                back = {},
                startRecording = {},
                stopRecording = {},
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

    finishChatModal?.let { data ->
        ShouldFinishChatDialog(
            lastBotMsg = data.lastBotMsg,
            lastUserMsg = data.lastUserMsg,
            onYesClick = {
                handlers.finishChat()
            },
            onNoClick = {
                handlers.onFinishRejected()
            },
            onDismiss = {
                handlers.onFinishRejected()
            },
        )
    }

    if (state.isReviewLoading) {
        ReviewDialog()
    }

    Scaffold(topBar = {
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
                modifier
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
                ScenarioInfoCard(
                    onPlayMessage = handlers.playAudio,
                    onStopMessage = handlers.stopAudio,
                    navigateToSubscription = handlers.toSubscription,
                    scenario = state.chat.scenario,
                    messages = state.chat.messages.map { it.toAudioMessage() },
                    chatModel = state.chat,
                    words = state.chat.scenario.helperWords,
                    isMessageLoading = state.isMessageLoading,
                    messageListState = listRef,
                    onChatFinish = handlers.finishChat,
                    isSubscribed = true,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AudioRecorderButton(
                onRecordStart = handlers.startRecording,
                onRecordStop = handlers.stopRecording,
                isDisabled = state.isMessageLoading,
                isRecording = state.isAudioRecording,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        if (state.chat == null) {
            return@Scaffold
        }
        RightTranslationBar(
            chatId = state.chat.id,
            show = showHelpModal,
            onClose = { showHelpModal = false },
        )
    }
}
