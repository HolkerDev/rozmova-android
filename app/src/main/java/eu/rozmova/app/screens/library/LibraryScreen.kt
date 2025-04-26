package eu.rozmova.app.screens.library

import android.util.Log
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyDto>(DifficultyDto.EASY) }
    var selectedScenarioType by remember { mutableStateOf<ScenarioTypeDto>(ScenarioTypeDto.MESSAGES) }
    var showFinished by remember { mutableStateOf(true) }

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
                onDismiss = { showFilterDialog = false },
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
            Log.i("LibraryScreen", "Scenarios LOADED: $scenarios")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { },
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
        title = { Text("Filter Scenarios") },
        text = {
            Column {
                Text("Language Level", style = MaterialTheme.typography.titleMedium)
                Row {
                    DifficultyDto.entries.forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { onApplyFilters(difficulty, selectedType, showFinished) },
                            label = { Text(difficulty.name) },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Scenario Type", style = MaterialTheme.typography.titleMedium)
                Row {
                    listOf(ScenarioTypeDto.MESSAGES, ScenarioTypeDto.CONVERSATION).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onApplyFilters(selectedDifficulty, type, showFinished) },
                            label = { Text(type.name) },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = showFinished,
                        onCheckedChange = { onApplyFilters(selectedDifficulty, selectedType, !showFinished) },
                    )
                    Text("Include finished scenarios", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scenario.situation,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = scenario.difficulty.name,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = scenario.scenarioType.name,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
