package com.bitcraftapps.stax.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "BillingRepository"
private const val PRODUCT_MONTHLY = "stax_premium_monthly"
private const val PRODUCT_ANNUAL = "stax_premium_annual"

// Grace buffer added to purchaseTime as a local expiry cache.
// This does not replace server-side validation; it bounds the stale-entitlement window
// to at most one subscription period if Google Play revokes a purchase silently.
private const val MONTHLY_EXPIRY_BUFFER_MS = 31L * 24 * 60 * 60 * 1000   // 31 days
private const val ANNUAL_EXPIRY_BUFFER_MS  = 366L * 24 * 60 * 60 * 1000  // 366 days

val LocalBillingRepository = compositionLocalOf<BillingRepository> {
    error("No BillingRepository provided")
}

class BillingRepository(
    private val context: Context,
    private val entitlementManager: EntitlementManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private var retryDelayMs = 1000L

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase flow")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryExistingPurchases()
            }
            else -> {
                Log.e(TAG, "Purchase update error: ${billingResult.debugMessage}")
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    retryDelayMs = 1000L
                    Log.d(TAG, "Billing client connected")
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected, retrying in ${retryDelayMs}ms")
                scope.launch {
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2L).coerceAtMost(30_000L)
                    startConnection()
                }
            }
        })
    }

    fun queryProducts() {
        if (!billingClient.isReady) return
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ANNUAL)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, queryResult: QueryProductDetailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val detailsList = queryResult.productDetailsList
                _products.value = detailsList
                Log.d(TAG, "Loaded ${detailsList.size} products")
            } else {
                Log.e(TAG, "Product query failed: ${result.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String,
        oldPurchaseToken: String? = null,
        replacementMode: Int? = null
    ) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
        if (oldPurchaseToken != null && replacementMode != null) {
            builder.setSubscriptionUpdateParams(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(oldPurchaseToken)
                    .setSubscriptionReplacementMode(replacementMode)
                    .build()
            )
        }
        val billingFlowParams = builder.build()
        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${result.debugMessage}")
        }
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                } else {
                    Log.e(TAG, "Acknowledge failed: ${result.debugMessage}")
                }
            }
        }

        // Check if any pricing phase has zero cost (free trial)
        val isInTrial = purchase.products.any { productId ->
            _products.value
                .firstOrNull { it.productId == productId }
                ?.subscriptionOfferDetails
                ?.any { offer ->
                    offer.pricingPhases.pricingPhaseList
                        .any { phase -> phase.priceAmountMicros == 0L }
                } == true
        }

        val isAnnual = purchase.products.any { it == PRODUCT_ANNUAL }
        val buffer = if (isAnnual) ANNUAL_EXPIRY_BUFFER_MS else MONTHLY_EXPIRY_BUFFER_MS
        val expiryMs = purchase.purchaseTime + buffer
        entitlementManager.setPremium(isInTrial = isInTrial, expiryMs = expiryMs)
    }

    fun queryExistingPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchaseList ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "Query purchases failed: ${result.debugMessage}")
                return@queryPurchasesAsync
            }

            val purchasedList = purchaseList.filter {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            val hasPending = purchaseList.any {
                it.purchaseState == Purchase.PurchaseState.PENDING
            }

            if (purchasedList.isNotEmpty()) {
                purchasedList.forEach { handlePurchase(it) }
            } else if (hasPending) {
                Log.d(TAG, "Purchase pending")
            } else {
                val current = entitlementManager.subscriptionState.value
                if (current !is SubscriptionState.Free) {
                    entitlementManager.setExpired(null)
                }
            }
        }
    }

    fun restorePurchases() {
        queryExistingPurchases()
        Log.d(TAG, "Restore purchases initiated")
    }

    fun endConnection() {
        billingClient.endConnection()
        Log.d(TAG, "Billing client connection ended")
    }
}
