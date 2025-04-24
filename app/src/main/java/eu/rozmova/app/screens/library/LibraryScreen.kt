package eu.rozmova.app.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.utils.ViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onScenarioClick: (ScenarioModel) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar with Filter Button
        TopAppBar(
            title = { Text(stringResource(R.string.library_title)) },
            actions = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            },
        )

        // Filter Dialog
        if (showFilterDialog) {
            FilterDialog(
                selectedLevel = state.selectedLevel,
                selectedType = state.selectedType,
                showFinishedOnly = state.showFinishedOnly,
                onDismiss = { showFilterDialog = false },
                onApplyFilters = { level, type, finishedOnly ->
                    viewModel.updateFilters(level, type, finishedOnly)
                    showFilterDialog = false
                },
            )
        }

        // Scenarios List
        when (val scenariosState = state.scenarios) {
            is ViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ViewState.Success -> {
                val filteredScenarios =
                    scenariosState.data.filter { scenario ->
                        (state.selectedLevel == null || scenario.languageLevel == state.selectedLevel) &&
                            (state.selectedType == null || scenario.scenarioType == state.selectedType)
                    }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredScenarios) { scenario ->
                        ScenarioCard(
                            scenario = scenario,
                            onClick = { onScenarioClick(scenario) },
                        )
                    }
                }
            }
            is ViewState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Error loading scenarios")
                }
            }
            ViewState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No scenarios available")
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    selectedLevel: String?,
    selectedType: ScenarioType?,
    showFinishedOnly: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (String?, ScenarioType?, Boolean) -> Unit,
) {
    var tempLevel by remember { mutableStateOf(selectedLevel) }
    var tempType by remember { mutableStateOf(selectedType) }
    var tempFinishedOnly by remember { mutableStateOf(showFinishedOnly) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Scenarios") },
        text = {
            Column {
                Text("Language Level", style = MaterialTheme.typography.titleMedium)
                Row {
                    listOf("Easy", "Medium", "Hard").forEach { level ->
                        FilterChip(
                            selected = tempLevel == level,
                            onClick = { tempLevel = if (tempLevel == level) null else level },
                            label = { Text(level) },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Scenario Type", style = MaterialTheme.typography.titleMedium)
                Row {
                    ScenarioType.entries.forEach { type ->
                        FilterChip(
                            selected = tempType == type,
                            onClick = { tempType = if (tempType == type) null else type },
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
                        checked = tempFinishedOnly,
                        onCheckedChange = { tempFinishedOnly = it },
                    )
                    Text("Show finished only")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApplyFilters(tempLevel, tempType, tempFinishedOnly) }) {
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
    scenario: ScenarioModel,
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
                    text = scenario.languageLevel,
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
