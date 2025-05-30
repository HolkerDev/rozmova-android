package eu.rozmova.app.screens.createchat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.Chip
import eu.rozmova.app.components.SimpleToolBar
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.utils.ViewState

typealias ChatId = String

@Composable
fun CreateChatScreen(
    onChatReady: (ChatId, ScenarioType) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val onChatReadyAction = rememberUpdatedState(onChatReady)

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateChatEvent.ChatReady -> onChatReadyAction.value(event.chatId, event.scenarioType)
            }
        }
    }

    val onScenarioSelect: (ScenarioModel) -> Unit = { selectedScenario ->
        viewModel.createChatFromScenario(selectedScenario)
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleToolBar(
            stringResource(R.string.scenarios_page_title),
            modifier = Modifier.padding(bottom = 20.dp),
            onBack = onBack,
        )
        when (val levelGroupsState = state.levelGroups) {
            ViewState.Empty -> TODO()
            is ViewState.Error -> TODO()
            ViewState.Loading -> {}
            is ViewState.Success -> {
                var expandedSections by remember { mutableStateOf(setOf(levelGroupsState.data[0].groupName)) }

                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(levelGroupsState.data) { group ->
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
}

@Composable
private fun ScenarioGroup(
    levelGroup: LevelGroup,
    isExpanded: Boolean,
    onHeaderClick: (String) -> Unit,
    onScenarioSelect: (ScenarioModel) -> Unit,
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
                            onClick = { onScenarioSelect(scenario) },
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
                Chip(label)
            }
        }
    }
}
