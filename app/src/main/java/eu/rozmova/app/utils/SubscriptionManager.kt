package eu.rozmova.app.utils

import eu.rozmova.app.repositories.billing.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {

    fun isUserSubscribed(): Flow<Boolean> {
        return subscriptionRepository.getSubscriptionStatus()
            .map { it.isSubscribed && it.isVerifiedWithBackend }
    }

    suspend fun hasActiveSubscription(): Boolean {
        return try {
            subscriptionRepository.isSubscribed().first()
        } catch (e: Exception) {
            false
        }
    }

    fun initializeBilling() {
        subscriptionRepository.startBillingConnection()
    }

    fun cleanupBilling() {
        subscriptionRepository.endBillingConnection()
    }

    suspend fun refreshSubscriptionStatus() {
        subscriptionRepository.refreshPurchases()
    }
}
