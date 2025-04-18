package eu.rozmova.app.screens.learn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import eu.rozmova.app.utils.ViewState

@Composable
fun LearnScreen(
    navigateToChat: (chatId: String, scenarioType: ScenarioType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val todaySelectionState by viewModel.todaySelectionState.collectAsState()
    val weeklyScenariosState by viewModel.weeklyScenarios.collectAsState()
    val navigateToChatAction = rememberUpdatedState(navigateToChat)
    val latestChatState by viewModel.latestChat.collectAsState()

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
            when (val weeklyScenarios = weeklyScenariosState) {
                is ViewState.Success -> {
                    CategorySelection(scenarios = weeklyScenarios.data, onScenarioSelect = onScenarioDtoSelect)
                }
                ViewState.Empty -> CircularProgressIndicator()
                is ViewState.Error -> {
                    // TODO: Handle the error state
                    Text("Error loading scenarios: ${weeklyScenarios.error?.message}")
                }
                ViewState.Loading ->
                    CategorySelection(scenarios = emptyList(), onScenarioSelect = onScenarioDtoSelect)
            }
        }
    }
}
