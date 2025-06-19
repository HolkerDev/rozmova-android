package eu.rozmova.app.domain.billing

import com.android.billingclient.api.ProductDetails

data class SubscriptionProduct(
    val productId: String,
    val title: String,
    val description: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val formattedPrice: String,
    val billingPeriod: String,
    val productDetails: ProductDetails,
)

sealed class SubscriptionState {
    object Loading : SubscriptionState()

    data class Available(
        val product: SubscriptionProduct,
    ) : SubscriptionState()

    data object Subscribed : SubscriptionState()

    data class Error(
        val message: String,
    ) : SubscriptionState()

    object NotAvailable : SubscriptionState()
}
