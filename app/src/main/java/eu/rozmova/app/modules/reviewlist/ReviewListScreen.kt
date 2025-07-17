package eu.rozmova.app.modules.reviewlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.modules.reviewlist.components.ReviewItem
import eu.rozmova.app.utils.MockData.mockReviewDto
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun ReviewListScreen(
    back: () -> Unit,
    toReview: (reviewId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewListVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    Content(
        state = state,
        modifier = modifier,
        back = back,
        refresh = {
            viewModel.refresh()
        },
        toReview = toReview,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    back: () -> Unit,
    toReview: (reviewId: String) -> Unit,
    refresh: () -> Unit,
    state: ReviewListState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.review_list_title)) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.close_content_description),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { refresh() },
            modifier =
                Modifier.fillMaxSize().padding(
                    PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                        bottom = 4.dp,
                    ),
                ),
        ) {
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    when {
                        state.isLoading -> {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        state.reviews.isNotEmpty() -> {
                            items(state.reviews, key = { it.id }) { review ->
                                ReviewItem(
                                    review = review,
                                    onReviewClick = { reviewId ->
                                        toReview(reviewId)
                                    },
                                )
                            }
                        }

                        else -> {
                            item {
                                Text(
                                    text = stringResource(R.string.reviews_screen_no_reviews),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun ReviewListScreenPreview() {
    Content(
        state =
            ReviewListState(
                reviews =
                    listOf(
                        mockReviewDto(),
                        mockReviewDto(isCompleted = false),
                    ),
            ),
        back = {},
        refresh = {},
        toReview = {},
    )
}
