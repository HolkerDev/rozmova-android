package eu.rozmova.app.modules.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.billing.SubscriptionProduct
import eu.rozmova.app.domain.billing.SubscriptionState
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import eu.rozmova.app.services.billing.BillingEvents
import eu.rozmova.app.services.billing.BillingService
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class SubscriptionUiState(
    val subscriptionState: SubscriptionState = SubscriptionState.Loading,
    val isLoading: Boolean = false,
    val error: Boolean = false,
    val showSuccessMessage: Boolean = false,
)

sealed class SubscriptionSideEffect {
    data class ShowError(
        val message: String,
    ) : SubscriptionSideEffect()

    object ShowSuccess : SubscriptionSideEffect()

    object NavigateBack : SubscriptionSideEffect()
}

@HiltViewModel
class SubscriptionVM
    @Inject
    constructor(
        private val billingService: BillingService,
        private val subscriptionRepository: SubscriptionRepository,
    ) : ViewModel(),
        ContainerHost<SubscriptionUiState, SubscriptionSideEffect> {
        override val container: Container<SubscriptionUiState, SubscriptionSideEffect> =
            container(SubscriptionUiState())

        init {
            fetchSubscriptionState()
            intent {
                billingService.billingState.collect { events ->
                    when (events) {
                        is BillingEvents.PurchaseFound -> {
                            reduce {
                                state.copy(
                                    isLoading = false,
                                    subscriptionState = SubscriptionState.Subscribed,
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        fun purchaseSubscription(
            activity: Activity,
            product: SubscriptionProduct,
        ) = intent {
            reduce { state.copy(isLoading = true, error = false) }
            billingService.purchaseSubscription(activity, product.productId)
        }

        fun fetchSubscriptionState() =
            intent {
                val isSubscribed = subscriptionRepository.getIsSubscribed()
                if (isSubscribed) {
                    reduce { state.copy(subscriptionState = SubscriptionState.Subscribed) }
                } else {
                    val availableSubscription = billingService.getAvailableSubscriptions().first()
                    reduce {
                        state.copy(
                            subscriptionState =
                                SubscriptionState.Available(
                                    availableSubscription,
                                ),
                        )
                    }
                }
            }
    }
