package eu.rozmova.app.components.conversationchat

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import eu.rozmova.app.components.AudioRecorderButton
import eu.rozmova.app.components.InstructionsButton
import eu.rozmova.app.components.SituationButton
import eu.rozmova.app.components.WordItem
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.MessageDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.WordDto
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationChat(
    onBackClick: () -> Unit,
    onChatArchive: () -> Unit,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    val chatState by viewModel.state.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val messageListState = rememberLazyListState()
    var showFinishModal by remember { mutableStateOf(false) }

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            ConvoChatEvents.Close -> TODO()
            ConvoChatEvents.ProposeFinish -> TODO()
            ConvoChatEvents.ScrollToBottom ->
                state.chat?.takeIf { it.messages.size > 1 }?.let { chat ->
                    messageListState.animateScrollToItem(
                        chat.messages.size - 1,
                    )
                }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
//        ShouldFinishChatDialog(
//            showDialog = showModal,
//            onYesClick = {
//                showModal = false
//                viewModel.finishChat(chatState.chat!!.id)
//                viewModel.resetProposal()
//            },
//            onNoClick = {
//                showModal = false
//                viewModel.resetProposal()
//            },
//            onDismiss = {
//                showModal = false
//                viewModel.resetProposal()
//            },
//        )

//        state.chatAnalysis?.let {
//            ChatAnalysisDialog(
//                review = it,
//                onConfirm = { viewModel.onChatAnalysisSubmit() },
//                isLoading = chatState.isChatAnalysisSubmitLoading,
//            )
//        }

        state.chat?.let { chat ->
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.chat_details_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    ScenarioInfoCard(
                        onPlayMessage = { messageId -> viewModel.playAudio(messageId) },
                        onStopMessage = { viewModel.stopAudio() },
                        scenario = chat.scenario,
                        messages = chat.messages,
                        chatModel = chat,
                        words = chat.scenario.helperWords,
                        isMessageLoading = false,
                        messageListState = messageListState,
                        onChatFinish = { viewModel.finishChat(chat.id) },
                        isAnalysisLoading = chatState.isAnalysisLoading,
                        onChatArchive = { viewModel.prepareAnalytics(chat.id) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AudioRecorderButton(
                    onRecordStart = { viewModel.startRecording() },
                    onRecordStop = { viewModel.stopRecording() },
                    isDisabled = false,
                    onChatAnalyticsRequest = onChatArchive,
                    isRecording = isRecording,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        } ?: LoadingComponent(onBackClick = { onBackClick() }, modifier = Modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioInfoCard(
    scenario: ScenarioDto,
    messages: List<MessageDto>,
    words: List<WordDto>,
    chatModel: ChatDto,
    onPlayMessage: (messageId: String) -> Unit,
    onStopMessage: () -> Unit,
    onChatFinish: () -> Unit,
    onChatArchive: () -> Unit,
    isMessageLoading: Boolean,
    isAnalysisLoading: Boolean,
    messageListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var showWordsBottomSheet by remember { mutableStateOf(false) }
    var showSituationDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
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
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    ) // Title

                    if (words.isNotEmpty()) {
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

                // Always show the analytics button for finished chats
                if (chatModel.status == ChatStatus.FINISHED) {
                    Button(
                        onClick = { onChatArchive() },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        enabled = !isAnalysisLoading,
                    ) {
                        if (isAnalysisLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Text("Get analytics", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                AudioMessageList(
                    messages = messages,
                    onPlayMessage = onPlayMessage,
                    onStopMessage = onStopMessage,
                    onChatFinish = onChatFinish,
                    messageListState = messageListState,
                    isLoadingMessage = isMessageLoading,
                    showFinishButton = messages.isNotEmpty() && chatModel.status == ChatStatus.IN_PROGRESS,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showWordsBottomSheet) {
        HelperWordsBottomSheet(
            words = words,
            onDismiss = { showWordsBottomSheet = false },
        )
    }

    if (showSituationDialog) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        AlertDialog(
            properties =
                DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = false,
                ),
            onDismissRequest = { showSituationDialog = false },
            title = {
                Text(
                    "Situation",
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
                        text = scenario.situation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSituationDialog = false },
                ) {
                    Text("Close")
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
            properties =
                DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = false,
                ),
            onDismissRequest = { showInstructionsDialog = false },
            title = {
                Text(
                    "Instructions",
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
                        text = scenario.userInstructions.joinToString("\n"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInstructionsDialog = false },
                ) {
                    Text("Close")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperWordsBottomSheet(
    words: List<WordDto>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.helper_words),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(words) { word ->
                    WordItem(word = word)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingComponent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.loading_progress)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading conversation...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorComponent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.error)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onBackClick,
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Text("Go Back", style = MaterialTheme.typography.labelLarge)
        }
    }
}
