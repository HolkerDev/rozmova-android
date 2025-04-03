package eu.rozmova.app.components.messagechat

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
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Task
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.components.StopChatButton
import eu.rozmova.app.components.WordItem
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.WordModel
import eu.rozmova.app.screens.createchat.ChatId
import eu.rozmova.app.utils.ViewState

@Composable
fun MessageChat(
    chatId: ChatId,
    onChatArchive: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageChatViewModel = hiltViewModel(),
) {
    val messageListState = rememberLazyListState()
    val onChatArchiveState = rememberUpdatedState(onChatArchive)
    val state by viewModel.state.collectAsState()
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MessageChatEvent.ScrollToBottom -> {
                    (state.messages as? ViewState.Success)?.let { messages ->
                        if (messages.data.isEmpty()) {
                            return@let
                        }
                        messageListState.animateScrollToItem(messages.data.size - 1)
                    }
                }

                MessageChatEvent.Close -> {
                    onChatArchiveState.value()
                }
                MessageChatEvent.ProposeFinish -> {
                    showModal = true
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ShouldFinishChatDialog(
            showDialog = showModal,
            onYesClick = {
                showModal = false
                (state.chat as? ViewState.Success)?.let { chatState ->
                    viewModel.finishChat(chatState.data.id)
                }
            },
            onNoClick = {
                showModal = false
            },
            onDismiss = {
                showModal = false
            },
        )

        state.chatAnalysis?.let {
            ChatAnalysisDialog(
                chatAnalysis = it,
                onConfirm = { viewModel.archiveChat(chatId) },
                isLoading = state.isAnalysisLoading,
            )
        }

        when (val chatState = state.chat) {
            is ViewState.Loading -> LoadingComponent(onBackClick)
            ViewState.Empty -> ErrorComponent(onBackClick)
            is ViewState.Error -> ErrorComponent(onBackClick)
            is ViewState.Success -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                ) {
                    ScenarioInfoCard(
                        scenario = chatState.data.scenario,
                        chatStatus = chatState.data.chatModel.status,
                        messages = state.messages,
                        words = chatState.data.words,
                        onBackClick = onBackClick,
                        onChatFinish = { viewModel.finishChat(chatState.data.chatModel.id) },
                        onChatArchive = { viewModel.prepareAnalytics(chatState.data.id) },
                        isMessageLoading = state.isLoadingMessage,
                        messageListState = messageListState,
                        modifier = Modifier.weight(1f),
                    )
                    MessageInput(
                        onSendMessage = { message ->
                            viewModel.sendMessage(chatState.data.id, message)
                        },
                        isDisabled = state.isLoadingMessage || chatState.data.chatModel.status != ChatStatus.IN_PROGRESS,
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioInfoCard(
    scenario: ScenarioModel,
    chatStatus: ChatStatus,
    messages: ViewState<List<ChatMessage>>,
    words: List<WordModel>,
    onBackClick: () -> Unit,
    onChatFinish: () -> Unit,
    onChatArchive: () -> Unit,
    isMessageLoading: Boolean,
    messageListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var showWordsBottomSheet by remember { mutableStateOf(false) }
    var showSituationDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        SimpleToolBar(title = stringResource(R.string.message_chat_title), onBack = onBackClick)
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Row {
                        if (words.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { showWordsBottomSheet = true },
                                colors = ButtonDefaults.filledTonalButtonColors(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CollectionsBookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Situation text with show more button
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { showSituationDialog = true },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scenario.situation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { showInstructionsDialog = true },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Task,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scenario.userInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                if (chatStatus == ChatStatus.FINISHED) {
                    Button(onClick = {
                        if (!isMessageLoading) onChatArchive()
                    }, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        if (isMessageLoading) {
                            Text("Analyzing...")
                        } else {
                            Text("Get analytics")
                        }
                    }
                }
                when (messages) {
                    ViewState.Empty -> {}
                    is ViewState.Error -> {}
                    ViewState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is ViewState.Success -> {
                        MessageList(
                            messages = messages.data,
                            onChatFinish = onChatFinish,
                            messageListState = messageListState,
                            showFinishButton = messages.data.isNotEmpty() && chatStatus == ChatStatus.IN_PROGRESS,
                            isLoadingMessage = isMessageLoading,
                        )
                    }
                }
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
            properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            onDismissRequest = { showSituationDialog = false },
            title = { Text("Situation") },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = scenario.situation,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSituationDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .heightIn(max = screenHeight * 0.8f),
        )
    }

    if (showInstructionsDialog) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            onDismissRequest = { showInstructionsDialog = false },
            title = { Text("Instructions") },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = scenario.userInstruction,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructionsDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .heightIn(max = screenHeight * 0.8f),
        )
    }
}

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    onChatFinish: () -> Unit,
    messageListState: LazyListState,
    isLoadingMessage: Boolean,
    showFinishButton: Boolean,
    modifier: Modifier = Modifier,
) {
    println(messages)
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        state = messageListState,
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
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (showFinishButton) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                StopChatButton(
                    onClick = onChatFinish,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperWordsBottomSheet(
    words: List<WordModel>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
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
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            ) {
                items(words) { word ->
                    WordItem(word = word)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingComponent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        SimpleToolBar(title = stringResource(R.string.loading_progress), onBack = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorComponent(onBackClick: () -> Unit) {
    SimpleToolBar(title = stringResource(R.string.error), onBack = onBackClick)
    Text(text = stringResource(R.string.error_message))
}
