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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
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
                    )
                    if (words.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { showWordsBottomSheet = true },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier.padding(start = 8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CollectionsBookmark,
                                contentDescription = stringResource(R.string.helper_words),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.helper_words),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Compact information cards in a Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Situation card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Situation",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }

                    // Instructions card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Instructions",
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
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (chatStatus == ChatStatus.FINISHED) {
                    Button(
                        onClick = {
                            if (!isMessageLoading) onChatArchive()
                        }, 
                        shape = MaterialTheme.shapes.small, 
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        if (isMessageLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Text("Get analytics", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                when (messages) {
                    ViewState.Empty -> {}
                    is ViewState.Error -> {}
                    ViewState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
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
            title = { 
                Text(
                    "Situation", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSituationDialog = false }
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
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showInstructionsDialog) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false),
            onDismissRequest = { showInstructionsDialog = false },
            title = { 
                Text(
                    "Instructions", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                        text = scenario.userInstruction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInstructionsDialog = false }
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
            shape = RoundedCornerShape(16.dp)
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
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        state = messageListState,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Typing...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperWordsBottomSheet(
    words: List<WordModel>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(words) { word ->
                    WordItem(word = word)
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
    Column(
        modifier = modifier.fillMaxSize(), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SimpleToolBar(title = stringResource(R.string.loading_progress), onBack = onBackClick)
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading conversation...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorComponent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SimpleToolBar(title = stringResource(R.string.error), onBack = onBackClick)
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onBackClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Go Back", style = MaterialTheme.typography.labelLarge)
        }
    }
}
