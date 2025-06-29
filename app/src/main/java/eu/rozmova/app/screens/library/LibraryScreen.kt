package eu.rozmova.app.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.toDifficulty
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

enum class ScenarioType(
    val scenarioTypeDto: ScenarioTypeDto,
    val labelId: Int,
) {
    MESSAGES(ScenarioTypeDto.MESSAGES, R.string.category_message),
    CONVERSATION(ScenarioTypeDto.CONVERSATION, R.string.category_conversation),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navigateToChat: (chatId: String, scenarioType: ScenarioTypeDto) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyDto>(DifficultyDto.EASY) }
    var selectedScenarioType by remember { mutableStateOf<ScenarioTypeDto>(ScenarioTypeDto.MESSAGES) }
    var showFinished by remember { mutableStateOf(true) }

    viewModel.collectSideEffect { event ->
        when (event) {
            is LibraryScreenEvents.ChatCreated -> {
                navigateToChat(event.chatId, event.scenarioType)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.library_title)) },
            actions = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            },
        )

        if (showFilterDialog) {
            FilterDialog(
                onDismiss = {
                    showFilterDialog = false
                    viewModel.fetchScenarios(selectedScenarioType, selectedDifficulty)
                },
                onApplyFilters = { level, type, finishedOnly ->
                    selectedDifficulty = level
                    selectedScenarioType = type
                    showFinished = finishedOnly
                },
                selectedDifficulty = selectedDifficulty,
                selectedType = selectedScenarioType,
                showFinished = showFinished,
            )
        }

        state.scenarios?.let { scenarios ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { scenario -> viewModel.createChat(scenario.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    selectedDifficulty: DifficultyDto,
    selectedType: ScenarioTypeDto,
    showFinished: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (DifficultyDto, ScenarioTypeDto, Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.filter_dialog_lang_levels), style = MaterialTheme.typography.titleMedium)
                Row {
                    DifficultyDto.entries.forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { onApplyFilters(difficulty, selectedType, showFinished) },
                            label = { Text(stringResource(difficulty.toDifficulty().labelId)) },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.filter_dialog_scenario_type), style = MaterialTheme.typography.titleMedium)
                Row {
                    listOf(ScenarioType.MESSAGES, ScenarioType.CONVERSATION).forEach { type ->
                        FilterChip(
                            selected = selectedType == type.scenarioTypeDto,
                            onClick = { onApplyFilters(selectedDifficulty, type.scenarioTypeDto, showFinished) },
                            label = { Text(stringResource(type.labelId)) },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioDto,
    onClick: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPassed = true

    Card(
        onClick = { onClick(scenario) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            if (isPassed) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scenario.situation,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DifficultyLabel(scenario.difficulty)
                ScenarioTypeIcon(scenario.scenarioType)
            }
        }
    }
}

@Composable
private fun DifficultyLabel(difficulty: DifficultyDto) {
    val diff = difficulty.toDifficulty()
    Surface(
        color = diff.color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = stringResource(diff.labelId),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = diff.color,
        )
    }
}

@Composable
private fun ScenarioTypeIcon(type: ScenarioTypeDto) {
    val (icon, desc) =
        when (type) {
            ScenarioTypeDto.MESSAGES -> Icons.Default.Chat to "Messages"
            ScenarioTypeDto.CONVERSATION -> Icons.Default.People to "Conversation"
        }
    Icon(
        imageVector = icon,
        contentDescription = desc,
        tint = MaterialTheme.colorScheme.primary,
    )
}
