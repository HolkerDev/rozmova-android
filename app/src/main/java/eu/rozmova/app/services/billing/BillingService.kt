package eu.rozmova.app.services.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.rozmova.app.domain.billing.SubscriptionProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BillingEvents {
    data class PurchaseFound(
        val purchase: Purchase,
    ) : BillingEvents

    data object NoPurchaseFound : BillingEvents

    data object Loading : BillingEvents
}

@Singleton
class BillingService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val tag = this::class.java.simpleName
        private var isInitialized: Boolean = false

        private val _billingState = MutableStateFlow<BillingEvents>(BillingEvents.Loading)
        val billingState = _billingState.asStateFlow()

        companion object {
            const val PREMIUM_SUBSCRIPTION_ID = "rozmova_premium_monthly"
        }

        private lateinit var billingClient: BillingClient

        fun initialize() {
            if (isInitialized) {
                Log.w(tag, "BillingService is already initialized")
                return
            }

            val pendingPurchasesParams =
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()

            billingClient =
                BillingClient
                    .newBuilder(context)
                    .enablePendingPurchases(pendingPurchasesParams)
                    .setListener { billingResult, purchases ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            purchases?.first()?.let { _billingState.value = BillingEvents.PurchaseFound(it) }
                        }
                    }.build()

            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                            Log.e(tag, "Billing setup failed: ${billingResult.debugMessage}")
                            return
                        }
                        queryExistingPurchases(
                            onPurchaseFound = {
                                _billingState.value = BillingEvents.PurchaseFound(it)
                            },
                            noPurchaseNotFound = {
                                Log.i(tag, "No existing subscription purchases found")
                                _billingState.value = BillingEvents.NoPurchaseFound
                            },
                        )
                    }

                    override fun onBillingServiceDisconnected() {}
                },
            )
            isInitialized = true
        }

        private fun queryExistingPurchases(
            onPurchaseFound: (Purchase) -> Unit,
            noPurchaseNotFound: () -> Unit,
        ) {
            val query =
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()

            billingClient.queryPurchasesAsync(query) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(tag, "Found ${purchasesList.size} existing subscription purchases")
                    val purchaseToAcknowledge = findPurchase(purchasesList)
                    if (purchaseToAcknowledge != null) {
                        onPurchaseFound(purchaseToAcknowledge)
                    } else {
                        Log.i(tag, "No existing subscription purchases found")
                        noPurchaseNotFound()
                    }
                } else {
                    Log.e(tag, "Failed to query existing purchases: ${billingResult.debugMessage}")
                }
            }
        }

        fun acknowledgePurchase(purchase: Purchase) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
                Log.w(tag, "Purchase is not in a valid state for acknowledgment")
                return
            }

            val acknowledgeParams =
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

            billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(tag, "Purchase acknowledged successfully")
                } else {
                    Log.e(tag, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                }
            }
        }

        private fun findPurchase(purchases: List<Purchase>): Purchase? =
            purchases.firstOrNull { purchase -> purchase.purchaseState == Purchase.PurchaseState.PURCHASED }

        suspend fun getAvailableSubscriptions(): List<SubscriptionProduct> =
            suspendCancellableCoroutine { continuation ->
                val productList =
                    listOf(
                        QueryProductDetailsParams.Product
                            .newBuilder()
                            .setProductId(PREMIUM_SUBSCRIPTION_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    )

                val params =
                    QueryProductDetailsParams
                        .newBuilder()
                        .setProductList(productList)
                        .build()

                billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val products =
                            productDetailsList.mapNotNull { productDetails ->
                                val subscriptionOfferDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                                val pricingPhase = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

                                if (subscriptionOfferDetails != null && pricingPhase != null) {
                                    SubscriptionProduct(
                                        productId = productDetails.productId,
                                        title = productDetails.title,
                                        description = productDetails.description,
                                        priceAmountMicros = pricingPhase.priceAmountMicros,
                                        priceCurrencyCode = pricingPhase.priceCurrencyCode,
                                        formattedPrice = pricingPhase.formattedPrice,
                                        billingPeriod = pricingPhase.billingPeriod,
                                        productDetails = productDetails,
                                    )
                                } else {
                                    null
                                }
                            }
                        continuation.resume(products) {}
                    } else {
                        continuation.resume(emptyList()) {}
                    }
                }
            }

        suspend fun purchaseSubscription(
            activity: Activity,
            productId: String,
        ): Boolean =
            suspendCancellableCoroutine { continuation ->
                val productList =
                    listOf(
                        QueryProductDetailsParams.Product
                            .newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    )

                val params =
                    QueryProductDetailsParams
                        .newBuilder()
                        .setProductList(productList)
                        .build()

                billingClient?.queryProductDetailsAsync(params) { result, productDetailsList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK &&
                        productDetailsList.isNotEmpty()
                    ) {
                        val productDetails = productDetailsList[0]
                        val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken

                        val productDetailsParamsList =
                            listOf(
                                BillingFlowParams.ProductDetailsParams
                                    .newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken ?: "")
                                    .build(),
                            )

                        val billingFlowParams =
                            BillingFlowParams
                                .newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()

                        val launchResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
                        continuation.resume(
                            launchResult?.responseCode == BillingClient.BillingResponseCode.OK,
                        ) { cause, _, _ -> }
                    } else {
                        continuation.resume(false) { cause, _, _ -> }
                    }
                }
            }
    }
