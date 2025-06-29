package eu.rozmova.app.screens.learn

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.QuickResumeCard
import eu.rozmova.app.components.RecentlyAdded
import eu.rozmova.app.components.TodaysScenarioSelection
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toScenarioType
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    navigateToChat: (chatId: String, scenarioType: ScenarioType) -> Unit,
    startOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenViewModel = hiltViewModel(),
) {
    val navigateToChatAction = rememberUpdatedState(navigateToChat)
    val state by viewModel.collectAsState()

    Log.i("LearnScreen", "LearnScreen composed - ViewModel hashCode: ${viewModel.hashCode()}")

    viewModel.collectSideEffect { event ->
        when (event) {
            is LearnEvent.ChatCreated -> {
                navigateToChatAction.value(event.chatId, event.scenarioType.toScenarioType())
            }

            LearnEvent.StartOnboarding -> {
                Log.i("LearnScreen", "Starting onboarding")
                startOnboarding()
            }
        }
    }

    fun onScenarioSelect(scenario: ScenarioDto) = viewModel.createChatFromScenario(scenario.id)

    fun onScenarioDtoSelect(scenario: ScenarioDto) = viewModel.createChatFromScenario(scenarioId = scenario.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practice_title)) },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                state.recommendedScenarios?.let { recommendedScenarios ->
                    item {
                        TodaysScenarioSelection(
                            onScenarioClick = { scenario ->
                                onScenarioSelect(scenario)
                            },
                            state = recommendedScenarios,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                state.latestChat?.let { latestChat ->
                    item {
                        QuickResumeCard(
                            chat = latestChat,
                            onContinueClick = { chatId, scenarioType ->
                                navigateToChat(
                                    chatId,
                                    scenarioType,
                                )
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                item {
                    RecentlyAdded(
                        scenarios = state.weeklyScenarios ?: emptyList(),
                        isLoading = state.weeklyScenariosLoading,
                        onScenarioSelect = { scenario ->
                            onScenarioDtoSelect(scenario)
                        },
                    )
                }
            }
        }
    }
}
