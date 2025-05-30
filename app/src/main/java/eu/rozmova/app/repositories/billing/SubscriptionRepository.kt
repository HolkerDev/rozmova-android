package eu.rozmova.app.repositories.billing

import android.app.Activity
import android.util.Log
import eu.rozmova.app.domain.billing.BillingResult
import eu.rozmova.app.domain.billing.SubscriptionProduct
import eu.rozmova.app.domain.billing.SubscriptionState
import eu.rozmova.app.domain.billing.SubscriptionStatus
import eu.rozmova.app.services.billing.BillingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository
    @Inject
    constructor(
        private val billingService: BillingService,
    ) {
        fun getSubscriptionState(): Flow<SubscriptionState> =
            flow {
                emit(SubscriptionState.Loading)

                try {
                    val products = billingService.querySubscriptionProducts()
                    Log.i("SubscriptionRepository", "Available products: ${products.size}")
                    if (products.isEmpty()) {
                        emit(SubscriptionState.NotAvailable)
                        return@flow
                    }

                    val product = products.first() // We only have one subscription

                    billingService.getSubscriptionStatus().collect { status ->
                        if (status.isSubscribed) {
                            emit(SubscriptionState.Subscribed(status))
                        } else {
                            emit(SubscriptionState.Available(product))
                        }
                    }
                } catch (e: Exception) {
                    emit(SubscriptionState.Error(e.message ?: "Unknown error"))
                }
            }

        suspend fun getAvailableSubscriptions(): List<SubscriptionProduct> =
            try {
                billingService.querySubscriptionProducts()
            } catch (e: Exception) {
                emptyList()
            }

        suspend fun purchaseSubscription(
            activity: Activity,
            product: SubscriptionProduct,
        ): BillingResult = billingService.launchBillingFlow(activity, product)

        fun getSubscriptionStatus(): Flow<SubscriptionStatus> = billingService.getSubscriptionStatus()

        fun isSubscribed(): Flow<Boolean> =
            billingService.getSubscriptionStatus().combine(
                flow { emit(Unit) },
            ) { status, _ ->
                status.isSubscribed
            }

        suspend fun refreshPurchases() {
            try {
                val purchases = billingService.queryPurchases()
                purchases.forEach { purchase ->
                    if (!purchase.isAcknowledged) {
                        billingService.acknowledgePurchase(purchase)
                    }
                }
            } catch (e: Exception) {
                // Handle error silently or log
            }
        }

        fun startBillingConnection() {
            billingService.startConnection()
        }

        fun endBillingConnection() {
            billingService.endConnection()
        }
    }
