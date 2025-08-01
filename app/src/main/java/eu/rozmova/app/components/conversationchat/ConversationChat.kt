package eu.rozmova.app.components.conversationchat

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationChat(
    onBackClick: () -> Unit,
    toReview: (reviewId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailsViewModel = hiltViewModel(),
) {
}
//    val messageListState = rememberLazyListState()
//    var finishChat: FinishChat? by remember { mutableStateOf(null) }
//    var showHelpModal by remember { mutableStateOf(false) }
//    val state by viewModel.collectAsState()
//
//    LaunchedEffect(chatId) {
//        viewModel.loadChat(chatId)
//    }
//
//    viewModel.collectSideEffect { event ->
//        when (event) {
//            ConvoChatEvents.Close -> TODO()
//            is ConvoChatEvents.ProposeFinish ->
//                finishChat =
//                    FinishChat(
//                        lastBotMsg = event.lastBotMsg,
//                        lastUserMsg = event.lastUserMsg,
//                    )
//            ConvoChatEvents.ScrollToBottom ->
//                state.chat?.takeIf { it.messages.size > 1 }?.let { chat ->
//                    messageListState.animateScrollToItem(
//                        chat.messages.size - 1,
//                    )
//                }
//
//            is ConvoChatEvents.ToReview -> toReview(event.reviewId)
//        }
//    }
//
//    finishChat?.let { data ->
//        ShouldFinishAudioDialog(
//            lastBotMsg = data.lastBotMsg,
//            lastUserMsg = data.lastUserMsg,
//            onYesClick = {
//                finishChat = null
//                viewModel.stopAudio()
//                viewModel.finishChat(chatId)
//            },
//            onNoClick = {
//                viewModel.stopAudio()
//                finishChat = null
//            },
//            onDismiss = {
//                viewModel.stopAudio()
//                finishChat = null
//            },
//            navigateToSubscription = onNavigateToSubscription,
//        )
//    }
//
//    if (state.isReviewLoading) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = {
//                Text(
//                    stringResource(R.string.analysing_conversation),
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
//                )
//            },
//            text = {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = MaterialTheme.colorScheme.primary,
//                        strokeWidth = 2.dp,
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = stringResource(R.string.analyse_wait),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    )
//                }
//            },
//            confirmButton = {},
//            containerColor = MaterialTheme.colorScheme.surface,
//            shape = RoundedCornerShape(16.dp),
//        )
//    }
//
//    Box(modifier = modifier.fillMaxSize()) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            state.chat?.let { chat ->
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                ) {
//                    TopAppBar(
//                        title = { Text(text = stringResource(R.string.chat_details_title)) },
//                        navigationIcon = {
//                            IconButton(onClick = onBackClick) {
//                                Icon(
//                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                                    contentDescription = "Back",
//                                )
//                            }
//                        },
//                        actions = {
//                            IconButton(onClick = { showHelpModal = true }) {
//                                Icon(
//                                    imageVector = Icons.Rounded.Translate,
//                                    contentDescription = "How do I say that?",
//                                    tint = MaterialTheme.colorScheme.onSurface,
//                                )
//                            }
//                        },
//                    )
//                    Box(
//                        modifier =
//                            Modifier
//                                .weight(1f)
//                                .fillMaxWidth(),
//                    ) {
//                        ScenarioInfoCard(
//                            onPlayMessage = { messageId -> viewModel.playAudio(messageId) },
//                            onStopMessage = { viewModel.stopAudio() },
//                            navigateToSubscription = onNavigateToSubscription,
//                            scenario = chat.scenario,
//                            messages = state.messages,
//                            chatModel = chat,
//                            words = chat.scenario.helperWords,
//                            isMessageLoading = state.isMessageLoading,
//                            messageListState = messageListState,
//                            onChatFinish = { viewModel.finishChat(chat.id) },
//                            isSubscribed = state.isSubscribed,
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                    AudioRecorderButton(
//                        onRecordStart = { viewModel.startRecording() },
//                        onRecordStop = { viewModel.stopRecording() },
//                        isDisabled = state.isMessageLoading,
//                        isRecording = state.isAudioRecording,
//                        modifier = Modifier.padding(bottom = 16.dp),
//                    )
//                }
//            } ?: LoadingComponent(onBackClick = { onBackClick() }, modifier = Modifier.fillMaxSize())
//        }
//
//        // How do I say that? side modal
//        AnimatedVisibility(
//            visible = showHelpModal,
//            enter =
//                slideInHorizontally(
//                    initialOffsetX = { it },
//                    animationSpec = tween(300),
//                ),
//            exit =
//                slideOutHorizontally(
//                    targetOffsetX = { it },
//                    animationSpec = tween(300),
//                ),
//        ) {
//            TranslationProposalModal(
//                chatId = chatId,
//                onDismiss = { showHelpModal = false },
//            )
//        }
//    }
// }
//
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun ScenarioInfoCard(
//    scenario: ScenarioDto,
//    messages: List<AudioChatMessage>,
//    words: List<WordDto>,
//    chatModel: ChatDto,
//    onPlayMessage: (messageId: String) -> Unit,
//    onStopMessage: () -> Unit,
//    onChatFinish: () -> Unit,
//    navigateToSubscription: () -> Unit,
//    isMessageLoading: Boolean,
//    messageListState: LazyListState,
//    isSubscribed: Boolean,
//    modifier: Modifier = Modifier,
// ) {
//    var showWordsBottomSheet by remember { mutableStateOf(false) }
//    var showSituationDialog by remember { mutableStateOf(false) }
//    var showInstructionsDialog by remember { mutableStateOf(false) }
//
//    Column(modifier = modifier) {
//        Card(
//            modifier =
//                Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 12.dp, vertical = 8.dp)
//                    .fillMaxHeight(),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors =
//                CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                ),
//        ) {
//            Column(
//                modifier =
//                    Modifier
//                        .padding(12.dp)
//                        .fillMaxWidth(),
//            ) {
//                Row(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    Text(
//                        text = scenario.title,
//                        style = MaterialTheme.typography.titleSmall,
//                        color = MaterialTheme.colorScheme.primary,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier.weight(1f),
//                    ) // Title
//
//                    if (words.isNotEmpty()) {
//                        FilledTonalButton(
//                            onClick = { showWordsBottomSheet = true },
//                            colors = ButtonDefaults.filledTonalButtonColors(),
//                            modifier = Modifier.padding(start = 8.dp),
//                            shape = MaterialTheme.shapes.medium,
//                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
//                        ) {
//                            Icon(
//                                imageVector = Icons.Rounded.CollectionsBookmark,
//                                contentDescription = stringResource(R.string.helper_words),
//                                modifier = Modifier.size(16.dp),
//                            )
//                        }
//                    }
//                }
//
//                // Compact information cards in a Row
//                Row(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 4.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    SituationButton(
//                        modifier =
//                            Modifier
//                                .weight(1f)
//                                .clickable { showSituationDialog = true },
//                    )
//                    InstructionsButton(
//                        modifier =
//                            Modifier
//                                .weight(1f)
//                                .clickable { showInstructionsDialog = true },
//                    )
//                }
//
//                HorizontalDivider(
//                    thickness = 1.dp,
//                    color = MaterialTheme.colorScheme.outlineVariant,
//                    modifier = Modifier.padding(vertical = 8.dp),
//                )
//
//                AudioMessageList(
//                    messages = messages,
//                    onPlayMessage = onPlayMessage,
//                    onStopMessage = onStopMessage,
//                    onChatFinish = onChatFinish,
//                    messageListState = messageListState,
//                    isLoadingMessage = isMessageLoading,
//                    isSubscribed = isSubscribed,
//                    navigateToSubscription = navigateToSubscription,
//                    showFinishButton = messages.isNotEmpty() && chatModel.status == ChatStatus.IN_PROGRESS,
//                    modifier = Modifier.weight(1f),
//                )
//            }
//        }
//    }
//
//    if (showWordsBottomSheet) {
//        HelperWords(
//            words = words,
//            onDismiss = { showWordsBottomSheet = false },
//            navigateToSubscription = {
//                showWordsBottomSheet = false
//                navigateToSubscription()
//            },
//            isSubscribed = isSubscribed,
//        )
//    }
//
//    if (showSituationDialog) {
//        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
//        AlertDialog(
//            properties =
//                DialogProperties(
//                    dismissOnClickOutside = true,
//                    dismissOnBackPress = true,
//                    usePlatformDefaultWidth = false,
//                ),
//            onDismissRequest = { showSituationDialog = false },
//            title = {
//                Text(
//                    stringResource(R.string.situation),
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
//                )
//            },
//            text = {
//                Box(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .verticalScroll(rememberScrollState()),
//                ) {
//                    Text(
//                        text = scenario.situation,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    )
//                }
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = { showSituationDialog = false },
//                ) {
//                    Text(stringResource(R.string.close_content_description))
//                }
//            },
//            containerColor = MaterialTheme.colorScheme.surface,
//            modifier =
//                Modifier
//                    .fillMaxWidth(0.95f)
//                    .wrapContentHeight()
//                    .heightIn(max = screenHeight * 0.8f),
//            shape = RoundedCornerShape(16.dp),
//        )
//    }
//
//    if (showInstructionsDialog) {
//        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
//        AlertDialog(
//            properties =
//                DialogProperties(
//                    dismissOnClickOutside = true,
//                    dismissOnBackPress = true,
//                    usePlatformDefaultWidth = false,
//                ),
//            onDismissRequest = { showInstructionsDialog = false },
//            title = {
//                Text(
//                    stringResource(R.string.instructions),
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
//                )
//            },
//            text = {
//                Box(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .verticalScroll(rememberScrollState()),
//                ) {
//                    Text(
//                        text = scenario.userInstructions.joinToString("\n\n") { it.task },
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    )
//                }
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = { showInstructionsDialog = false },
//                ) {
//                    Text(stringResource(R.string.close_content_description))
//                }
//            },
//            containerColor = MaterialTheme.colorScheme.surface,
//            modifier =
//                Modifier
//                    .fillMaxWidth(0.95f)
//                    .wrapContentHeight()
//                    .heightIn(max = screenHeight * 0.8f),
//            shape = RoundedCornerShape(16.dp),
//        )
//    }
// }
//
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// private fun LoadingComponent(
//    onBackClick: () -> Unit,
//    modifier: Modifier = Modifier,
// ) {
//    Column(
//        modifier = modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//    ) {
//        CircularProgressIndicator(
//            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(48.dp),
//        )
//    }
// }
