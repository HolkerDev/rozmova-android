package eu.rozmova.app.modules.review

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.repositories.ChatsRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class ReviewState(
    val review: ReviewDto? = null,
)

@HiltViewModel
class ReviewVM
    @Inject
    constructor(
        private val chatsRepository: ChatsRepository,
    ) : ViewModel(),
        ContainerHost<ReviewState, Unit> {
        override val container = container<ReviewState, Unit>(ReviewState())

        fun fetchReview(reviewId: String) =
            intent {
                chatsRepository.getReview(reviewId).map { review ->
                    reduce { state.copy(review = review) }
                }
            }
    }
