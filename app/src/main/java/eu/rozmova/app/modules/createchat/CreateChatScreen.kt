package eu.rozmova.app.modules.createchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.LangDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.UserInstruction
import eu.rozmova.app.modules.createchat.components.SpeakingLimitReached
import eu.rozmova.app.modules.shared.DifficultyLabel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

enum class ChatTypeUI(
    val textLabelId: Int,
    val icon: ImageVector,
) {
    SPEAKING(R.string.chat_type_speaking, Icons.Default.Mic),
    WRITING(
        R.string.chat_type_writing,
        Icons.Default.TextFields,
    ),
}

fun ChatType.toUI(): ChatTypeUI =
    when (this) {
        ChatType.SPEAKING -> ChatTypeUI.SPEAKING
        ChatType.WRITING -> ChatTypeUI.WRITING
    }

fun ChatTypeUI.toModel(): ChatType =
    when (this) {
        ChatTypeUI.SPEAKING -> ChatType.SPEAKING
        ChatTypeUI.WRITING -> ChatType.WRITING
    }

private class Handlers(
    val onChatStart: (ChatTypeUI) -> Unit,
    val toSubscription: () -> Unit,
    val closeLimitReachedModal: () -> Unit,
    val back: () -> Unit,
)

@Composable
fun CreateChatScreen(
    scenarioId: String,
    toSubscription: () -> Unit,
    toChat: (chatId: String) -> Unit,
    back: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateChatVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    var isLimitReachedModalVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.fetchScenario(scenarioId)
    }

    viewModel.collectSideEffect { event ->
        when (event) {
            is CreateChatEvent.ChatCreated -> {
                toChat(event.chatId)
            }

            CreateChatEvent.UsageLimitReached -> {
                isLimitReachedModalVisible = true
            }
        }
    }

    Content(
        isLimitReachedModalVisible = isLimitReachedModalVisible,
        state = state,
        handlers =
            Handlers(
                onChatStart = { chatType ->
                    viewModel.createChat(scenarioId, chatType.toModel())
                },
                toSubscription = toSubscription,
                closeLimitReachedModal = { isLimitReachedModalVisible = false },
                back = back,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    handlers: Handlers,
    isLimitReachedModalVisible: Boolean,
    state: CreateChatState,
    modifier: Modifier = Modifier,
) {
    var selectedChatType by remember { mutableStateOf(ChatTypeUI.WRITING) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_chat_title)) },
                navigationIcon = {
                    IconButton(onClick = handlers.back) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 4.dp),
                ) {
                    if (state.errorChatCreation) {
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                            text = stringResource(R.string.error_generic),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Button(
                        onClick = { handlers.onChatStart(selectedChatType) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.scenario != null && !state.isLoading,
                        shape = MaterialTheme.shapes.medium,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            return@Button
                        }
                        Text(
                            text = stringResource(R.string.start_chat),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (state.errorScenarioLoad) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.error_generic),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                return@Column
            }
            if (state.scenario == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            state.scenario.let { scenario ->
                // Scenario Details Card
                ScenarioDetailsCard(
                    title = scenario.title,
                    situation = scenario.situation,
                    difficulty = scenario.difficulty,
                )

                // Chat Type Selection
                ChatTypeSelection(
                    selectedType = selectedChatType,
                    onTypeSelect = { selectedChatType = it },
                )

                // Add bottom padding to ensure content doesn't get hidden behind bottom bar
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Limit Reached Modal
            if (isLimitReachedModalVisible) {
                SpeakingLimitReached(
                    onUpgradeClick = {
                        handlers.toSubscription()
                        handlers.closeLimitReachedModal()
                    },
                    onDismiss = handlers.closeLimitReachedModal,
                )
            }
        }
    }
}

@Composable
private fun ScenarioDetailsCard(
    title: String,
    situation: String,
    difficulty: DifficultyDto,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f), // Add weight to title
                )
                DifficultyLabel(difficulty = difficulty)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Situation
            Text(
                text = situation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ChatTypeSelection(
    selectedType: ChatTypeUI,
    onTypeSelect: (ChatTypeUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.choose_chat_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(ChatTypeUI.WRITING, ChatTypeUI.SPEAKING).forEach { chatType ->
                ChatTypeOption(
                    chatType = chatType,
                    selected = selectedType == chatType,
                    onSelect = { onTypeSelect(chatType) },
                )
            }
        }
    }
}

@Composable
private fun ChatTypeOption(
    chatType: ChatTypeUI,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            if (selected) {
                CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                    brush =
                        SolidColor(
                            MaterialTheme.colorScheme.primary,
                        ),
                )
            } else {
                CardDefaults.outlinedCardBorder()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors =
                    RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                    ),
            )

            Icon(
                imageVector = chatType.icon,
                contentDescription = null,
                tint =
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(chatType.textLabelId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )

                Text(
                    text =
                        when (chatType) {
                            ChatTypeUI.SPEAKING -> stringResource(R.string.chat_type_speaking_desc)
                            ChatTypeUI.WRITING -> stringResource(R.string.chat_type_writing_desc)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScenarioDetailsScreenPreview() {
    var isLoading by remember { mutableStateOf(false) }
    var isCreationError by remember { mutableStateOf(false) }

    MaterialTheme {
        Content(
            isLimitReachedModalVisible = true,
            state =
                CreateChatState(
                    isLoading = isLoading,
                    errorChatCreation = isCreationError,
                    errorScenarioLoad = true,
                    scenario =
                        ScenarioDto(
                            id = "1",
                            createdAt = "now",
                            userLang = LangDto.EN,
                            scenarioLang = LangDto.DE,
                            difficulty = DifficultyDto.HARD,
                            scenarioType = ScenarioTypeDto.CONVERSATION,
                            title = "Job Interview Scenario",
                            situation =
                                "You are interviewing for a software developer position at a startup." +
                                    "The interviewer asks you to explain a complex technical concept to someone without a technical background. You need to demonstrate both your technical knowledge and communication skills.",
                            labels = listOf(),
                            helperWords = listOf(),
                            userInstructions =
                                listOf(
                                    UserInstruction(
                                        assessment = "Explain the concept clearly and concisely, avoiding jargon.",
                                        task = "Explain a complex technical concept to a non-technical person.",
                                    ),
                                ),
                        ),
                ),
            handlers =
                Handlers(
                    onChatStart = {},
                    toSubscription = {},
                    closeLimitReachedModal = {},
                    back = {},
                ),
        )
    }
}
