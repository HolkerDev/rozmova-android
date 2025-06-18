package eu.rozmova.app.services.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import eu.rozmova.app.domain.billing.BillingResult
import eu.rozmova.app.domain.billing.SubscriptionProduct
import eu.rozmova.app.domain.billing.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface BillingService {
    fun startConnection()
    fun endConnection()

    suspend fun querySubscriptionProducts(): List<SubscriptionProduct>
    suspend fun queryPurchases(): List<Purchase>
    suspend fun launchBillingFlow(activity: Activity, product: SubscriptionProduct): BillingResult
    suspend fun acknowledgePurchase(purchase: Purchase): BillingResult
    suspend fun verifyPurchaseWithBackend(purchaseToken: String): BillingResult

    fun getSubscriptionStatus(): Flow<SubscriptionStatus>
    fun isConnected(): Boolean
}
