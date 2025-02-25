package eu.rozmova.app.screens.learn

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.TodaysScenarioSelection
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.utils.ViewState

@Composable
fun LearnScreen(
    onChatCreate: (scenarioId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val todaySelectionState by viewModel.todaySelectionState.collectAsState()
    val chatCreationState by viewModel.chatCreationState.collectAsState()

    val latestOnChatCreate = rememberUpdatedState(onChatCreate)

    LaunchedEffect(chatCreationState) {
        when (val state = chatCreationState) {
            is ViewState.Success -> {
                latestOnChatCreate.value(state.data)
                viewModel.resetChatCreationState()
            }
            else -> {}
        }
    }

    val onScenarioSelect = { scenarioModel: ScenarioModel ->
        viewModel.createChatFromScenario(scenarioModel)
    }

    TodaysScenarioSelection(
        onScenarioClick = onScenarioSelect,
        state = todaySelectionState,
        modifier = modifier.padding(8.dp),
    )
}
