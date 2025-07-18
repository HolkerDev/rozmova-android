package eu.rozmova.app.modules.allscenarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.modules.allscenarios.components.ScenarioCard
import org.orbitmvi.orbit.compose.collectAsState

enum class ScenarioType(
    val scenarioTypeDto: ScenarioTypeDto?,
    val labelId: Int,
) {
    MESSAGES(ScenarioTypeDto.MESSAGES, R.string.category_message),
    CONVERSATION(ScenarioTypeDto.CONVERSATION, R.string.category_conversation),
    ALL(null, R.string.type_all),
}

enum class Difficulty(
    val difficultyDto: DifficultyDto?,
    val labelId: Int,
) {
    EASY(DifficultyDto.EASY, R.string.level_easy),
    MEDIUM(DifficultyDto.MEDIUM, R.string.level_medium),
    HARD(DifficultyDto.HARD, R.string.level_hard),
    ALL(null, R.string.type_all),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllScenariosScreen(
    back: () -> Unit,
    toChatCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllScenariosVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyDto?>(null) }
    var selectedScenarioType by remember { mutableStateOf<ScenarioTypeDto?>(null) }
    var showFinished by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.library_title)) },
            navigationIcon = {
                IconButton(onClick = back) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            },
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
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { scenario -> toChatCreate(scenario.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterDialog(
    selectedDifficulty: DifficultyDto?,
    selectedType: ScenarioTypeDto?,
    showFinished: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (DifficultyDto?, ScenarioTypeDto?, Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Difficulty Level Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.filter_dialog_lang_levels),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Difficulty.entries.forEach { difficulty ->
                            FilterChip(
                                selected = selectedDifficulty == difficulty.difficultyDto,
                                onClick = {
                                    onApplyFilters(difficulty.difficultyDto, selectedType, showFinished)
                                },
                                label = {
                                    Text(
                                        text = stringResource(difficulty.labelId),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                            )
                        }
                    }
                }

                // Scenario Type Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.filter_dialog_scenario_type),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(ScenarioType.MESSAGES, ScenarioType.CONVERSATION, ScenarioType.ALL).forEach { type ->
                            FilterChip(
                                selected = selectedType == type.scenarioTypeDto,
                                onClick = {
                                    onApplyFilters(selectedDifficulty, type.scenarioTypeDto, showFinished)
                                },
                                label = {
                                    Text(
                                        text = stringResource(type.labelId),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                            )
                        }
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
