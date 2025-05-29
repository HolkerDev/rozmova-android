package eu.rozmova.app.domain.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

data class SubscriptionProduct(
    val productId: String,
    val title: String,
    val description: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val formattedPrice: String,
    val billingPeriod: String,
    val productDetails: ProductDetails
)

data class SubscriptionStatus(
    val isSubscribed: Boolean,
    val productId: String?,
    val purchaseToken: String?,
    val isAcknowledged: Boolean,
    val autoRenewing: Boolean,
    val expiryTimeMillis: Long?
)

sealed class BillingResult {
    object Success : BillingResult()
    data class Error(val message: String, val code: Int) : BillingResult()
    object UserCanceled : BillingResult()
    object NetworkError : BillingResult()
    object ServiceUnavailable : BillingResult()
}

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Available(val product: SubscriptionProduct) : SubscriptionState()
    data class Subscribed(val status: SubscriptionStatus) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
    object NotAvailable : SubscriptionState()
}

enum class SubscriptionPeriod(val value: String) {
    MONTHLY("P1M")
}