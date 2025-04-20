package eu.rozmova.app.screens.learn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.CategorySelection
import eu.rozmova.app.components.QuickResumeCard
import eu.rozmova.app.components.TodaysScenarioSelection
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toScenarioType
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun LearnScreen(
    navigateToChat: (chatId: String, scenarioType: ScenarioType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val todaySelectionState by viewModel.todaySelectionState.collectAsState()
    val navigateToChatAction = rememberUpdatedState(navigateToChat)
    val latestChatState by viewModel.latestChat.collectAsState()
    val state by viewModel.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LearnEvent.ChatCreated -> {
                    navigateToChatAction.value(event.chatId, event.scenarioType.toScenarioType())
                }
            }
        }
    }

    val onScenarioSelect = { scenarioModel: ScenarioModel ->
        viewModel.createChatFromScenario(scenarioModel.id)
    }

    val onScenarioDtoSelect = { scenarioDto: ScenarioDto ->
        viewModel.createChatFromScenario(scenarioId = scenarioDto.id)
    }

    // Wrap the content in a scrollable Column
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            TodaysScenarioSelection(
                onScenarioClick = onScenarioSelect,
                state = todaySelectionState,
                modifier = Modifier.padding(8.dp),
            )
        }

        item {
            QuickResumeCard(
                chat = latestChatState,
                onContinueClick = { chatId, scenarioType ->
                    navigateToChat(
                        chatId,
                        scenarioType,
                    )
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        item {
            CategorySelection(
                scenarios = state.weeklyScenarios ?: emptyList(),
                isLoading = state.weeklyScenariosLoading,
                onScenarioSelect = onScenarioDtoSelect,
            )
        }
    }
}
