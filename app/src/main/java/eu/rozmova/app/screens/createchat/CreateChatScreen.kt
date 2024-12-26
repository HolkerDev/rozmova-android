package eu.rozmova.app.screens.createchat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.domain.ScenarioModel

@Composable
fun CreateChatScreen(
    onScenarioReady: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val onScenarioSelect = {
        // TODO: start a chat
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleToolBar("Select scenario", modifier = Modifier.padding(bottom = 20.dp), onBack = onBack)
        when (val uiState = state) {
            CreateChatState.Loading -> CircularProgressIndicator()
            is CreateChatState.Success -> {
                // Keep track of expanded sections
                var expandedSections by remember { mutableStateOf(setOf(uiState.levelGroups[0].groupName)) }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.levelGroups) { group ->
                        ScenarioGroup(
                            levelGroup = group,
                            isExpanded = expandedSections.contains(group.groupName),
                            onHeaderClick = { level ->
                                expandedSections =
                                    if (expandedSections.contains(level)) {
                                        expandedSections - level
                                    } else {
                                        expandedSections + level
                                    }
                            },
                            onScenarioSelect = onScenarioSelect,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioGroup(
    levelGroup: LevelGroup,
    isExpanded: Boolean,
    onHeaderClick: (String) -> Unit,
    onScenarioSelect: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column {
            // Header
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onHeaderClick(levelGroup.groupName) }
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = levelGroup.groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    imageVector =
                        if (isExpanded) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowRight
                        },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                )
            }

            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    levelGroup.scenarios.forEach { scenario ->
                        ScenarioItem(
                            scenario = scenario,
                            onClick = { onScenarioSelect() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioItem(
    scenario: ScenarioModel,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Text(
            text = scenario.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            scenario.labels.forEach { label ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(label) },
                )
            }
        }
    }
}
