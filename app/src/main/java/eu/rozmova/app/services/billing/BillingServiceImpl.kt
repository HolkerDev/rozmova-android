package eu.rozmova.app.services.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.rozmova.app.clients.backend.VerificationClient
import eu.rozmova.app.clients.backend.VerifySubscriptionReq
import eu.rozmova.app.domain.billing.BillingResult
import eu.rozmova.app.domain.billing.SubscriptionProduct
import eu.rozmova.app.domain.billing.SubscriptionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingServiceImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val verificationClient: VerificationClient,
    ) : BillingService,
        PurchasesUpdatedListener {
        private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        companion object {
            const val PREMIUM_SUBSCRIPTION_ID = "rozmova_premium_monthly"
        }

        private val billingClient =
            BillingClient
                .newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()

        private val _subscriptionStatus =
            MutableStateFlow(
                SubscriptionStatus(
                    isSubscribed = false,
                    productId = null,
                    purchaseToken = null,
                    isAcknowledged = false,
                    autoRenewing = false,
                    expiryTimeMillis = null,
                    isVerifiedWithBackend = false,
                    verificationPending = false,
                ),
            )

        override fun startConnection() {
            if (!billingClient.isReady) {
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: com.android.billingclient.api.BillingResult) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                // Connection established
                                refreshPurchases()
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            // Service disconnected
                        }
                    },
                )
            }
        }

        override fun endConnection() {
            billingClient.endConnection()
        }

        override suspend fun querySubscriptionProducts(): List<SubscriptionProduct> =
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
                        continuation.resume(products)
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }

        override suspend fun queryPurchases(): List<Purchase> =
            suspendCancellableCoroutine { continuation ->
                val params =
                    QueryPurchasesParams
                        .newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(purchases)
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }

        override suspend fun launchBillingFlow(
            activity: Activity,
            product: SubscriptionProduct,
        ): BillingResult {
            return suspendCancellableCoroutine { continuation ->
                val subscriptionOfferDetails = product.productDetails.subscriptionOfferDetails?.firstOrNull()
                if (subscriptionOfferDetails == null) {
                    continuation.resume(BillingResult.Error("No subscription offer found", -1))
                    return@suspendCancellableCoroutine
                }

                val productDetailsParamsList =
                    listOf(
                        BillingFlowParams.ProductDetailsParams
                            .newBuilder()
                            .setProductDetails(product.productDetails)
                            .setOfferToken(subscriptionOfferDetails.offerToken)
                            .build(),
                    )

                val billingFlowParams =
                    BillingFlowParams
                        .newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        // Flow launched successfully, result will come in onPurchasesUpdated
                        continuation.resume(BillingResult.Success)
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        continuation.resume(BillingResult.UserCanceled)
                    }
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                        continuation.resume(BillingResult.ServiceUnavailable)
                    }
                    BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                        continuation.resume(BillingResult.NetworkError)
                    }
                    else -> {
                        continuation.resume(BillingResult.Error(billingResult.debugMessage, billingResult.responseCode))
                    }
                }
            }
        }

        override suspend fun acknowledgePurchase(purchase: Purchase): BillingResult {
            return suspendCancellableCoroutine { continuation ->
                if (purchase.isAcknowledged) {
                    continuation.resume(BillingResult.Success)
                    return@suspendCancellableCoroutine
                }

                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams
                        .newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(BillingResult.Success)
                    } else {
                        continuation.resume(BillingResult.Error(billingResult.debugMessage, billingResult.responseCode))
                    }
                }
            }
        }

        override fun getSubscriptionStatus(): Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

        override fun isConnected(): Boolean = billingClient.isReady

        override suspend fun verifyPurchaseWithBackend(purchaseToken: String): BillingResult {
            return try {
                Log.d("BillingServiceImpl", "Verifying purchase with backend: $purchaseToken")

                // Update status to show verification is pending
                _subscriptionStatus.value = _subscriptionStatus.value.copy(verificationPending = true)

                val response = verificationClient.fetchTranslationProposal(
                    VerifySubscriptionReq(purchaseToken = purchaseToken)
                )

                if (response.isSuccessful) {
                    val verificationResult = response.body()
                    if (verificationResult?.isSubscribed == true) {
                        Log.d("BillingServiceImpl", "Backend verification successful")
                        // Update subscription status with verification success
                        _subscriptionStatus.value = _subscriptionStatus.value.copy(
                            isVerifiedWithBackend = true,
                            verificationPending = false
                        )
                        BillingResult.VerificationSuccess
                    } else {
                        Log.w("BillingServiceImpl", "Backend verification failed - subscription not valid")
                        _subscriptionStatus.value = _subscriptionStatus.value.copy(
                            isSubscribed = false,
                            isVerifiedWithBackend = false,
                            verificationPending = false
                        )
                        BillingResult.VerificationFailed
                    }
                } else {
                    Log.e("BillingServiceImpl", "Backend verification request failed: ${response.code()}")
                    _subscriptionStatus.value = _subscriptionStatus.value.copy(verificationPending = false)
                    BillingResult.Error("Verification failed", response.code())
                }
            } catch (e: Exception) {
                Log.e("BillingServiceImpl", "Backend verification error", e)
                _subscriptionStatus.value = _subscriptionStatus.value.copy(verificationPending = false)
                BillingResult.Error(e.message ?: "Unknown verification error", -1)
            }
        }

        override fun onPurchasesUpdated(
            billingResult: com.android.billingclient.api.BillingResult,
            purchases: MutableList<Purchase>?,
        ) {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.let { processPurchases(it) }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    // User canceled the purchase
                }
                else -> {
                    // Handle other error cases
                }
            }
        }

        private fun processPurchases(purchases: List<Purchase>) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.products.contains(PREMIUM_SUBSCRIPTION_ID)) {
                        updateSubscriptionStatus(purchase)

                        // Acknowledge the purchase if not already acknowledged
                        if (!purchase.isAcknowledged) {
                            // Launch coroutine to acknowledge purchase
                            serviceScope.launch {
                                acknowledgePurchase(purchase)
                            }
                        }

                        // Verify purchase with backend
                        serviceScope.launch {
                            verifyPurchaseWithBackend(purchase.purchaseToken)
                        }
                    }
                }
            }
        }

        private fun updateSubscriptionStatus(purchase: Purchase) {
            _subscriptionStatus.value =
                SubscriptionStatus(
                    isSubscribed = true,
                    productId = PREMIUM_SUBSCRIPTION_ID,
                    purchaseToken = purchase.purchaseToken,
                    isAcknowledged = purchase.isAcknowledged,
                    autoRenewing = purchase.isAutoRenewing,
                    expiryTimeMillis = null, // For subscriptions, you might need to implement server-side validation
                    isVerifiedWithBackend = false, // Will be updated after backend verification
                    verificationPending = false,
                )
        }

        private fun refreshPurchases() {
            serviceScope.launch {
                val purchases = queryPurchases()
                if (purchases.isNotEmpty()) {
                    processPurchases(purchases)
                } else {
                    _subscriptionStatus.value =
                        SubscriptionStatus(
                            isSubscribed = false,
                            productId = null,
                            purchaseToken = null,
                            isAcknowledged = false,
                            autoRenewing = false,
                            expiryTimeMillis = null,
                        )
                }
            }
        }
    }
