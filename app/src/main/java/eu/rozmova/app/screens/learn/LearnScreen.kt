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
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.utils.ViewState

@Composable
fun LearnScreen(
    navigateToChat: (chatId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val todaySelectionState by viewModel.todaySelectionState.collectAsState()
    val chatCreationState by viewModel.chatCreationState.collectAsState()
    val scenariosState by viewModel.scenariosState.collectAsState()
    val navigate = rememberUpdatedState(navigateToChat)
    val latestChatState by viewModel.latestChat.collectAsState()

    LaunchedEffect(chatCreationState) {
        when (val state = chatCreationState) {
            is ViewState.Success -> {
                navigate.value(state.data)
                viewModel.resetChatCreationState()
            }
            else -> {}
        }
    }

    val onScenarioSelect = { scenarioModel: ScenarioModel ->
        viewModel.createChatFromScenario(scenarioModel)
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
                onContinueClick = { chatId -> navigateToChat(chatId) },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        item {
            CategorySelection(scenariosState = scenariosState, onScenarioSelect = onScenarioSelect)
        }
    }
}
