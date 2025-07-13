package eu.rozmova.app.modules.generatechat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.modules.createchat.ChatTypeUI
import eu.rozmova.app.modules.createchat.toModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

typealias ChatId = String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateChatScreen(
    onChatReady: (ChatId, ChatType) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenerateChatVM = hiltViewModel(),
) {
    var description by remember { mutableStateOf("") }
    var selectedChatType by remember { mutableStateOf(ChatTypeUI.WRITING) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { events ->
        when (events) {
            is GenerateChatEvents.ChatCreated -> {
                onChatReady(events.chatId, events.chatType)
            }
        }
    }

    fun generateScenario() {
        viewModel.generateScenario(selectedDifficulty.toDto(), selectedChatType.toModel(), description)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.generate_chat_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Description Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                        placeholder = {
                            Text(stringResource(R.string.describe_scenario))
                        },
                        maxLines = 6,
                        enabled = !state.isLoading,
                    )
                }
            }

            // Scenario Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.scenario_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        ChatTypeUI.entries.forEach { chatType ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedChatType == chatType,
                                            onClick = { selectedChatType = chatType },
                                            role = Role.RadioButton,
                                            enabled = !state.isLoading,
                                        ).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedChatType == chatType,
                                    onClick = null,
                                    enabled = !state.isLoading,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(chatType.textLabelId),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = stringResource(chatType.textLabelId),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Difficulty Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.difficulty),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Difficulty.entries.forEach { difficulty ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedDifficulty == difficulty,
                                            onClick = { selectedDifficulty = difficulty },
                                            role = Role.RadioButton,
                                            enabled = !state.isLoading,
                                        ).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedDifficulty == difficulty,
                                    onClick = null,
                                    enabled = !state.isLoading,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(difficulty.displayName),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = stringResource(difficulty.description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    generateScenario()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !state.isLoading && description.isNotBlank(),
            ) {
                if (state.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(stringResource(R.string.generate_scenario_loading))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.generate_scenario),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private enum class Difficulty(
    val displayName: Int,
    val description: Int,
) {
    EASY(R.string.level_easy, R.string.level_easy_description),
    MEDIUM(R.string.level_medium, R.string.level_medium_description),
    HARD(R.string.level_hard, R.string.level_hard_description),
    ;

    fun toDto(): DifficultyDto =
        when (this) {
            EASY -> DifficultyDto.EASY
            MEDIUM -> DifficultyDto.MEDIUM
            HARD -> DifficultyDto.HARD
        }
}

private enum class ScenarioType(
    val displayName: Int,
    val description: Int,
) {
    MESSAGES(R.string.category_message, R.string.scenario_type_messages_description),
    CONVERSATION(R.string.category_conversation, R.string.scenario_type_conversation_description),
    ;

    fun toDto(): ScenarioTypeDto =
        when (this) {
            MESSAGES -> ScenarioTypeDto.MESSAGES
            CONVERSATION -> ScenarioTypeDto.CONVERSATION
        }
}
