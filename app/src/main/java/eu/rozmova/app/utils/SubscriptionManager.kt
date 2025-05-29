package eu.rozmova.app.utils

import eu.rozmova.app.repositories.billing.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    
    fun isUserSubscribed(): Flow<Boolean> {
        return subscriptionRepository.getSubscriptionStatus()
            .map { it.isSubscribed }
    }
    
    suspend fun hasActiveSubscription(): Boolean {
        return try {
            val purchases = subscriptionRepository.getSubscriptionStatus()
            // This would need to be collected, but for synchronous check we can use a different approach
            false // Placeholder - in real implementation you'd check current status
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