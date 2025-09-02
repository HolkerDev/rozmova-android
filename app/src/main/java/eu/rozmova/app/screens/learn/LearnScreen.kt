package eu.rozmova.app.screens.learn

import androidx.compose.foundation.layout.Column
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
import eu.rozmova.app.modules.learn.components.BucketCard
import io.github.jan.supabase.realtime.Column
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
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
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                state.bucketDto?.let { bucket ->
                    BucketCard(
                        progress = bucket.progress,
                        wordList = bucket.activeWords.map { it.word },
                        onPracticeClick = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
