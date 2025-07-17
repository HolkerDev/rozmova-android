package eu.rozmova.app.modules.reviewlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.repositories.ChatsRepository
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
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel(),
        ContainerHost<ReviewListState, Unit> {
        override val container: Container<ReviewListState, Unit> = container(ReviewListState())

        init {
            fetchReviews()
        }

        fun fetchReviews() =
            intent {
                reduce { state.copy(isLoading = true) }
                chatsRepository
                    .getReviews()
                    .map { reviews ->
                        reduce {
                            state.copy(reviews = reviews, isLoading = false)
                        }
                    }.mapLeft { error ->
                        reduce { state.copy(isLoading = false) }
                    }
            }

        fun refresh() =
            intent {
                reduce { state.copy(isRefreshing = true) }
                chatsRepository
                    .getReviews()
                    .map { reviews ->
                        reduce {
                            state.copy(reviews = reviews, isRefreshing = false)
                        }
                    }.mapLeft { error ->
                        reduce { state.copy(isRefreshing = false) }
                    }
            }
    }
