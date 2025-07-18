package eu.rozmova.app.screens.learn

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.components.QuickResumeCard
import eu.rozmova.app.components.RecentlyAdded
import eu.rozmova.app.components.TodaysScenarioSelection
import eu.rozmova.app.domain.ChatType
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    toCreateChat: (scenarioId: String) -> Unit,
    toChat: (chatId: String, chatType: ChatType) -> Unit,
    startOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearnScreenVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            LearnEvent.StartOnboarding -> {
                startOnboarding()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practice_title)) },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(contentPadding = paddingValues) {
                state.recommendedScenarios?.let { recommendedScenarios ->
                    item {
                        TodaysScenarioSelection(
                            onScenarioClick = { scenario ->
                                toCreateChat(scenario.id)
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
                            onContinueClick = { chatId, chatType ->
                                toChat(chatId, chatType)
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
                            toCreateChat(scenario.id)
                        },
                    )
                }
            }
        }
    }
}
