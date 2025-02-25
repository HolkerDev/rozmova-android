package eu.rozmova.app.screens.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.TodaysScenarioSelection

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val todaySelectionState by viewModel.todaySelectionState.collectAsState()
    TodaysScenarioSelection(
        onScenarioClick = {},
        modifier = modifier,
        state = todaySelectionState,
    )
}
