package eu.rozmova.app.modules.reviewlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.utils.MockData.mockReviewDto
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ReviewListState(
    val reviews: List<ReviewDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class ReviewListVM
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<ReviewListState, Unit> {
        override val container: Container<ReviewListState, Unit> = container(ReviewListState())

        init {
            fetchReviews()
        }

        fun fetchReviews() =
            intent {
                reduce { state.copy(isLoading = true) }
                reduce { state.copy(isLoading = false, reviews = listOf(mockReviewDto())) }
            }

        fun refresh() =
            intent {
                reduce { state.copy(isRefreshing = true) }
                // TODO: Implement actual API call to refresh reviews
                // For now, we'll just clear the refreshing state
                reduce { state.copy(isRefreshing = false) }
            }
    }
