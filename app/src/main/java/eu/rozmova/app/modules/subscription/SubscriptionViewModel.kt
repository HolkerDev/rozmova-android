package eu.rozmova.app.modules.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.billing.BillingResult
import eu.rozmova.app.domain.billing.SubscriptionProduct
import eu.rozmova.app.domain.billing.SubscriptionState
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class SubscriptionUiState(
    val subscriptionState: SubscriptionState = SubscriptionState.Loading,
    val isLoading: Boolean = false,
    val error: String? = null,
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
class SubscriptionViewModel
    @Inject
    constructor(
        private val subscriptionRepository: SubscriptionRepository,
    ) : ViewModel(),
        ContainerHost<SubscriptionUiState, SubscriptionSideEffect> {
        override val container: Container<SubscriptionUiState, SubscriptionSideEffect> =
            container(SubscriptionUiState())

        init {
            observeSubscriptionState()
            startBillingConnection()
        }

        private fun observeSubscriptionState() {
            subscriptionRepository
                .getSubscriptionState()
                .onEach { subscriptionState ->
                    intent {
                        reduce {
                            state.copy(
                                subscriptionState = subscriptionState,
                                isLoading = false,
                            )
                        }
                    }
                }.launchIn(viewModelScope)
        }

        private fun startBillingConnection() =
            intent {
                subscriptionRepository.startBillingConnection()
            }

        fun purchaseSubscription(
            activity: Activity,
            product: SubscriptionProduct,
        ) = intent {
            reduce { state.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                when (val result = subscriptionRepository.purchaseSubscription(activity, product)) {
                    is BillingResult.Success -> {
                        reduce {
                            state.copy(
                                isLoading = false,
                                showSuccessMessage = true,
                            )
                        }
                        // The subscription state will be updated automatically through the observer
                    }
                    is BillingResult.Error -> {
                        reduce {
                            state.copy(
                                isLoading = false,
                                error = result.message,
                            )
                        }
                    }
                    is BillingResult.UserCanceled -> {
                        reduce { state.copy(isLoading = false) }
                    }
                    is BillingResult.NetworkError -> {
                        reduce {
                            state.copy(
                                isLoading = false,
                                error = "Network error. Please check your connection.",
                            )
                        }
                    }
                    is BillingResult.ServiceUnavailable -> {
                        reduce {
                            state.copy(
                                isLoading = false,
                                error = "Google Play services unavailable. Please try again later.",
                            )
                        }
                    }
                }
            }
        }

        fun refreshSubscriptions() =
            intent {
                reduce { state.copy(isLoading = true, error = null) }

                viewModelScope.launch {
                    subscriptionRepository.refreshPurchases()
                    reduce { state.copy(isLoading = false) }
                }
            }

        fun clearError() =
            intent {
                reduce { state.copy(error = null) }
            }

        fun clearSuccessMessage() =
            intent {
                reduce { state.copy(showSuccessMessage = false) }
            }

        override fun onCleared() {
            super.onCleared()
        }
    }
