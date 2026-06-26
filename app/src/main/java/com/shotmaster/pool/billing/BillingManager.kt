package com.shotmaster.pool.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private var purchasesUpdatedListener: ((List<Purchase>) -> Unit)? = null

    companion object {
        private const val TAG = "BillingManager"
        // REPLACE THESE WITH YOUR ACTUAL PLAY CONSOLE PRODUCT IDs
        const val SKU_WEEKLY = "shot_master_weekly"
        const val SKU_MONTHLY = "shot_master_monthly"
        const val SKU_YEARLY = "shot_master_yearly"
    }

    fun initialize(onReady: () -> Unit = {}) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing client ready")
                    onReady()
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    fun querySubscriptions(skuList: List<String>, onResult: (List<ProductDetails>) -> Unit) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                skuList.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(productDetailsList ?: emptyList())
            } else {
                Log.e(TAG, "Query products failed: ${billingResult.responseCode}")
                onResult(emptyList())
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): Int {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        return billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
    }

    fun queryPurchases(onResult: (List<Purchase>) -> Unit) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(purchases ?: emptyList())
            } else {
                Log.e(TAG, "Query purchases failed: ${billingResult.responseCode}")
                onResult(emptyList())
            }
        }
    }

    fun setPurchasesUpdatedListener(listener: (List<Purchase>) -> Unit) {
        purchasesUpdatedListener = listener
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchasesUpdatedListener?.invoke(purchases)
            for (purchase in purchases) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                Log.i(TAG, "Purchase acknowledged: ${result.responseCode}")
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
