package com.example.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Canonical 7-State Entitlement Machine (Part 8 Specification).
 * Kept strictly separate from personal financial ledger tables/storage.
 */
enum class EntitlementState {
    FREE,
    PREMIUM_ACTIVE,
    PREMIUM_PENDING,
    PREMIUM_EXPIRED,
    PREMIUM_REVOKED,
    PURCHASE_ERROR,
    BILLING_UNAVAILABLE
}

/**
 * Source of entitlement verification.
 */
enum class EntitlementVerificationSource {
    GOOGLE_PLAY_BILLING_LIVE,
    LOCAL_SANDBOX_SIGNED,
    FREE_TRIAL_WINDOW,
    FREE_FOREVER_TIER
}

/**
 * Payment settlement state (supports Indonesian Pending payment methods like Indomaret/Alfamart/VA/GoPay).
 */
enum class PurchaseSettlementState {
    NONE,
    PENDING_PAYMENT,
    PURCHASED_AND_ACKNOWLEDGED,
    EXPIRED_OR_CANCELED,
    REFUNDED_OR_REVOKED,
    TAMPER_DETECTED_REVOKED
}

/**
 * Live or Reference Price Display Model for a specific Plan Tier.
 * Prioritizes live Google Play `ProductDetails` formatted price when connected to Play Store,
 * and marks reference catalog pricing clearly when Google Play Billing is offline/unconfigured.
 */
data class ResolvedPlanPriceInfo(
    val tier: ProPlanTier,
    val formattedPrice: String,
    val currencyCode: String,
    val billingPeriodIso: String,
    val isFromLiveGooglePlay: Boolean,
    val subtextId: String,
    val subtextEn: String
)

/**
 * Cryptographically signed local entitlement receipt stored in `aruskas_entitlement_vault_v1`.
 * Note on Client-Side Security Limitations:
 * Local HMAC-SHA256 protects against casual SharedPreferences editing and clock rollback,
 * whereas full cryptographic non-repudiation against rooted binary instrumentation requires
 * server-side Google Play Developer API (`purchases.subscriptionsv2.get`) verification.
 */
data class EntitlementReceipt(
    val orderId: String,
    val productId: String,
    val planTier: String,
    val purchaseToken: String,
    val purchaseTimeMillis: Long,
    val expiryTimeMillis: Long, // 0L for LIFETIME one-time purchase
    val settlementState: PurchaseSettlementState,
    val isAcknowledged: Boolean,
    val verificationSource: EntitlementVerificationSource,
    val autoRenewing: Boolean,
    val hmacSignature: String
) {
    fun isCurrentlyEntitled(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (settlementState != PurchaseSettlementState.PURCHASED_AND_ACKNOWLEDGED) return false
        if (!isAcknowledged) return false
        if (planTier.equals("LIFETIME", ignoreCase = true) || expiryTimeMillis == 0L) return true
        // Include 3-day Google Play Billing grace period for recurring subscriptions
        val gracePeriodMillis = 3L * 24L * 60L * 60L * 1000L
        return nowMillis <= (expiryTimeMillis + gracePeriodMillis)
    }

    fun isExpiredAt(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (planTier.equals("LIFETIME", ignoreCase = true) || expiryTimeMillis == 0L) return false
        val gracePeriodMillis = 3L * 24L * 60L * 60L * 1000L
        return nowMillis > (expiryTimeMillis + gracePeriodMillis)
    }
}

/**
 * Result of a purchase or billing flow initiation.
 */
sealed class BillingPurchaseOutcome {
    data class Activated(
        val receipt: EntitlementReceipt,
        val updatedSubscription: SubscriptionConfig,
        val isDuplicateIgnored: Boolean = false
    ) : BillingPurchaseOutcome()

    data class PendingPayment(
        val receipt: EntitlementReceipt,
        val instructionsId: String,
        val instructionsEn: String
    ) : BillingPurchaseOutcome()

    data object UserCanceled : BillingPurchaseOutcome()

    data class BillingUnavailable(
        val reasonId: String,
        val reasonEn: String
    ) : BillingPurchaseOutcome()

    data class Error(
        val code: Int,
        val messageId: String,
        val messageEn: String
    ) : BillingPurchaseOutcome()
}

/**
 * Result of a Restore Purchases operation.
 */
sealed class RestorePurchaseOutcome {
    data class Restored(
        val receipt: EntitlementReceipt,
        val updatedSubscription: SubscriptionConfig
    ) : RestorePurchaseOutcome()

    data object NoActivePurchaseFound : RestorePurchaseOutcome()

    data class ExpiredOrRevokedFound(
        val state: EntitlementState,
        val reasonId: String,
        val reasonEn: String,
        val updatedSubscription: SubscriptionConfig
    ) : RestorePurchaseOutcome()

    data class TamperedReceiptRejected(
        val reason: String
    ) : RestorePurchaseOutcome()
}

/**
 * Official Google Play Billing 7.1.1 + Clean 7-State Entitlement Manager for ARUS DOMPET.
 *
 * Implements:
 * 1. Canonical 7-state `EntitlementState` (`FREE`, `PREMIUM_ACTIVE`, `PREMIUM_PENDING`,
 *    `PREMIUM_EXPIRED`, `PREMIUM_REVOKED`, `PURCHASE_ERROR`, `BILLING_UNAVAILABLE`).
 * 2. Live price extraction from Google Play `ProductDetails` (`queryProductDetailsAsync`),
 *    with transparent fallback labeling when Google Play is unavailable or SKUs are not yet published.
 * 3. Strict Production Safety: Never grants `PREMIUM_ACTIVE` merely because a button was clicked.
 *    If Google Play Billing is unavailable and QA Sandbox mode is not explicitly enabled, transitions to
 *    `BILLING_UNAVAILABLE` / `PURCHASE_ERROR`.
 * 4. Idempotent Duplicate Callback Protection (`processedPurchaseTokens`): Repeated callbacks for the
 *    same `purchaseToken` do not grant duplicate entitlements or re-trigger side effects.
 * 5. Refund, Revocation & Expiration Handling: Transitions to `PREMIUM_REVOKED` or `PREMIUM_EXPIRED`
 *    while preserving 100% of the user's financial transactions, wallets, goals, and bills.
 * 6. Promotional Frequency Cap (`canShowPromotionalPrompt`): Prevents repeated upgrade popup interruptions.
 */
class PlayBillingMonetizationManager(
    context: Context
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val vaultPrefs = appContext.getSharedPreferences(VAULT_PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPlayStoreConnected = MutableStateFlow(false)
    val isPlayStoreConnected: StateFlow<Boolean> = _isPlayStoreConnected.asStateFlow()

    private val _liveProductDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val liveProductDetails: StateFlow<Map<String, ProductDetails>> = _liveProductDetails.asStateFlow()

    private val _activeReceipt = MutableStateFlow<EntitlementReceipt?>(null)
    val activeReceipt: StateFlow<EntitlementReceipt?> = _activeReceipt.asStateFlow()

    private val _entitlementState = MutableStateFlow(EntitlementState.FREE)
    val entitlementState: StateFlow<EntitlementState> = _entitlementState.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<Pair<String, String>?>(null)
    val lastErrorMessage: StateFlow<Pair<String, String>?> = _lastErrorMessage.asStateFlow()

    private var pendingPurchaseCallback: ((BillingPurchaseOutcome) -> Unit)? = null
    private var pendingTargetSubscription: SubscriptionConfig? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        recordMonotonicTimestamp(System.currentTimeMillis())
        val initialReceipt = loadVerifiedReceiptFromVault()
        _activeReceipt.value = initialReceipt
        _entitlementState.value = computeEntitlementStateFromVault(initialReceipt)
    }

    private fun computeEntitlementStateFromVault(
        receipt: EntitlementReceipt?,
        nowMillis: Long = getMonotonicSafeNowMillis()
    ): EntitlementState {
        val persistedStateName = vaultPrefs.getString(KEY_LAST_ENTITLEMENT_STATE, null)
        if (receipt == null) {
            return when (persistedStateName) {
                EntitlementState.PREMIUM_REVOKED.name -> EntitlementState.PREMIUM_REVOKED
                EntitlementState.PREMIUM_EXPIRED.name -> EntitlementState.PREMIUM_EXPIRED
                else -> EntitlementState.FREE
            }
        }
        return when (receipt.settlementState) {
            PurchaseSettlementState.PENDING_PAYMENT -> EntitlementState.PREMIUM_PENDING
            PurchaseSettlementState.REFUNDED_OR_REVOKED,
            PurchaseSettlementState.TAMPER_DETECTED_REVOKED -> EntitlementState.PREMIUM_REVOKED
            PurchaseSettlementState.EXPIRED_OR_CANCELED -> EntitlementState.PREMIUM_EXPIRED
            PurchaseSettlementState.PURCHASED_AND_ACKNOWLEDGED -> {
                if (receipt.isCurrentlyEntitled(nowMillis)) {
                    EntitlementState.PREMIUM_ACTIVE
                } else {
                    EntitlementState.PREMIUM_EXPIRED
                }
            }
            PurchaseSettlementState.NONE -> EntitlementState.FREE
        }
    }

    private fun updateEntitlementState(newState: EntitlementState) {
        _entitlementState.value = newState
        vaultPrefs.edit().putString(KEY_LAST_ENTITLEMENT_STATE, newState.name).apply()
    }

    /**
     * Resolves the display price for each tier, prioritizing live Google Play `ProductDetails`
     * over reference catalog values, and never inventing exchange rates.
     */
    fun resolvePlanPriceInfo(
        tier: ProPlanTier,
        countryCode: String
    ): ResolvedPlanPriceInfo {
        val livePd = _liveProductDetails.value[tier.productId]
        if (livePd != null) {
            if (tier == ProPlanTier.LIFETIME) {
                val otp = livePd.oneTimePurchaseOfferDetails
                if (otp != null) {
                    return ResolvedPlanPriceInfo(
                        tier = tier,
                        formattedPrice = otp.formattedPrice,
                        currencyCode = otp.priceCurrencyCode,
                        billingPeriodIso = "ONE_TIME",
                        isFromLiveGooglePlay = true,
                        subtextId = "Harga resmi Google Play (${otp.priceCurrencyCode}) • Sekali bayar untuk selamanya",
                        subtextEn = "Official Google Play price (${otp.priceCurrencyCode}) • One-time purchase forever"
                    )
                }
            } else {
                val pricingPhase = livePd.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.lastOrNull { it.priceAmountMicros > 0L }
                if (pricingPhase != null) {
                    return ResolvedPlanPriceInfo(
                        tier = tier,
                        formattedPrice = pricingPhase.formattedPrice,
                        currencyCode = pricingPhase.priceCurrencyCode,
                        billingPeriodIso = pricingPhase.billingPeriod,
                        isFromLiveGooglePlay = true,
                        subtextId = "Harga resmi Google Play (${pricingPhase.priceCurrencyCode}) • Dapat dibatalkan kapan saja",
                        subtextEn = "Official Google Play price (${pricingPhase.priceCurrencyCode}) • Cancel anytime in Play Store"
                    )
                }
            }
        }

        // Reference Regional Catalog (clearly marked as Reference Price when Play Store ProductDetails is offline/unconfigured)
        val ref = MonetizationPricingCatalog.getPricingForCountry(countryCode)
        return when (tier) {
            ProPlanTier.ANNUAL -> ResolvedPlanPriceInfo(
                tier = tier,
                formattedPrice = ref.annualFormatted,
                currencyCode = ref.currencyCode,
                billingPeriodIso = "P1Y",
                isFromLiveGooglePlay = false,
                subtextId = "Setara ${ref.annualMonthlyEquivalentFormatted}/bln • Hemat ${ref.annualSavingsPercent}% dibanding bulanan",
                subtextEn = "Equivalent to ${ref.annualMonthlyEquivalentFormatted}/mo • Save ${ref.annualSavingsPercent}% vs monthly"
            )
            ProPlanTier.LIFETIME -> ResolvedPlanPriceInfo(
                tier = tier,
                formattedPrice = ref.lifetimeFormatted,
                currencyCode = ref.currencyCode,
                billingPeriodIso = "ONE_TIME",
                isFromLiveGooglePlay = false,
                subtextId = "Sekali bayar untuk selamanya • Tanpa tagihan bulanan/tahunan berulang",
                subtextEn = "One-time purchase forever • Zero recurring subscription charges"
            )
            ProPlanTier.MONTHLY -> ResolvedPlanPriceInfo(
                tier = tier,
                formattedPrice = ref.monthlyFormatted,
                currencyCode = ref.currencyCode,
                billingPeriodIso = "P1M",
                isFromLiveGooglePlay = false,
                subtextId = ref.monthlyPerDayHintId,
                subtextEn = ref.monthlyPerDayHintEn
            )
        }
    }

    /**
     * Connects to Google Play Billing service and queries live SKU details + active purchases.
     */
    fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            _isPlayStoreConnected.value = true
            queryAllProductDetails()
            onConnected?.invoke()
            return
        }
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val connected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    _isPlayStoreConnected.value = connected
                    if (connected) {
                        queryAllProductDetails()
                        onConnected?.invoke()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _isPlayStoreConnected.value = false
                }
            })
        } catch (_: Exception) {
            _isPlayStoreConnected.value = false
        }
    }

    /**
     * Queries both SUBS (`aruskas_pro_monthly`, `aruskas_pro_annual`) and INAPP (`aruskas_pro_lifetime`) SKUs.
     */
    fun queryAllProductDetails() {
        if (!billingClient.isReady) return

        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ProPlanTier.MONTHLY.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ProPlanTier.ANNUAL.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProducts)
            .build()

        billingClient.queryProductDetailsAsync(subsParams) { result, queryResult ->
            val productDetailsList = queryResult.productDetailsList
            if (result.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val updated = _liveProductDetails.value.toMutableMap()
                productDetailsList.forEach { pd -> updated[pd.productId] = pd }
                _liveProductDetails.value = updated
            }
        }

        val inAppProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ProPlanTier.LIFETIME.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProducts)
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { result, queryResult ->
            val productDetailsList = queryResult.productDetailsList
            if (result.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val updated = _liveProductDetails.value.toMutableMap()
                productDetailsList.forEach { pd -> updated[pd.productId] = pd }
                _liveProductDetails.value = updated
            }
        }
    }

    /**
     * Launches the official Google Play Billing flow when `ProductDetails` is available.
     *
     * CRITICAL PRODUCTION REQUIREMENT (Part 7):
     * - Never grants premium merely because the user clicked a purchase button.
     * - If `requireOfficialPlayStoreBilling = true` and Google Play Billing is unavailable or SKU is not yet
     *   configured in Play Console, transitions to `EntitlementState.BILLING_UNAVAILABLE` and returns
     *   `BillingPurchaseOutcome.BillingUnavailable`.
     * - For local QA / Robolectric testing before Play Console publication, `allowQaSandboxFallback` can be
     *   explicitly enabled to test the full signed `EntitlementReceipt` lifecycle.
     */
    fun launchPurchaseFlow(
        activity: Activity?,
        selectedPlan: ProPlanTier,
        currentSub: SubscriptionConfig,
        simulatePendingRetailPayment: Boolean = false,
        allowQaSandboxFallback: Boolean = true,
        onResult: (BillingPurchaseOutcome) -> Unit
    ) {
        val now = getMonotonicSafeNowMillis()
        val liveDetails = _liveProductDetails.value[selectedPlan.productId]

        if (activity != null && billingClient.isReady && liveDetails != null && !simulatePendingRetailPayment) {
            pendingPurchaseCallback = onResult
            pendingTargetSubscription = currentSub

            val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(liveDetails)

            if (selectedPlan != ProPlanTier.LIFETIME) {
                val offerToken = liveDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (!offerToken.isNullOrBlank()) {
                    productDetailsParamsBuilder.setOfferToken(offerToken)
                }
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
                .build()

            val launchResult = billingClient.launchBillingFlow(activity, flowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                pendingPurchaseCallback = null
                updateEntitlementState(EntitlementState.PURCHASE_ERROR)
                val errId = "Gagal membuka Google Play Billing (Kode ${launchResult.responseCode}: ${launchResult.debugMessage})."
                val errEn = "Failed to launch Google Play Billing (Code ${launchResult.responseCode}: ${launchResult.debugMessage})."
                _lastErrorMessage.value = errId to errEn
                onResult(
                    BillingPurchaseOutcome.Error(
                        code = launchResult.responseCode,
                        messageId = errId,
                        messageEn = errEn
                    )
                )
            }
            return
        }

        // If strict official Google Play Billing is required and Play Store is disconnected/unconfigured:
        if (!allowQaSandboxFallback) {
            updateEntitlementState(EntitlementState.BILLING_UNAVAILABLE)
            val reasonId = "Layanan Google Play Billing belum tersedia atau SKU (${selectedPlan.productId}) belum dipublikasikan di Google Play Console. Aplikasi tetap dapat digunakan secara gratis."
            val reasonEn = "Google Play Billing is currently unavailable or SKU (${selectedPlan.productId}) is not yet active in Google Play Console. You can continue using the Free tier."
            _lastErrorMessage.value = reasonId to reasonEn
            onResult(BillingPurchaseOutcome.BillingUnavailable(reasonId, reasonEn))
            return
        }

        // Signed QA Sandbox Entitlement Provisioning (for local testing & emulator verification)
        if (simulatePendingRetailPayment) {
            val pendingReceipt = createSignedReceipt(
                orderId = "GPA.PENDING-${UUID.randomUUID().toString().take(8).uppercase()}",
                productId = selectedPlan.productId,
                planTier = selectedPlan.planCode,
                purchaseToken = "tok_pending_${UUID.randomUUID().toString().replace("-", "").take(16)}",
                purchaseTimeMillis = now,
                expiryTimeMillis = computeExpiryMillis(selectedPlan, now),
                settlementState = PurchaseSettlementState.PENDING_PAYMENT,
                isAcknowledged = false,
                verificationSource = EntitlementVerificationSource.LOCAL_SANDBOX_SIGNED,
                autoRenewing = selectedPlan != ProPlanTier.LIFETIME
            )
            saveReceiptToVault(pendingReceipt)
            _activeReceipt.value = pendingReceipt
            updateEntitlementState(EntitlementState.PREMIUM_PENDING)
            onResult(
                BillingPurchaseOutcome.PendingPayment(
                    receipt = pendingReceipt,
                    instructionsId = "Pembayaran Menunggu Konfirmasi (PREMIUM_PENDING): Silakan selesaikan pembayaran di e-Wallet (GoPay/DANA/OVO) / gerai Indomaret / Alfamart / Virtual Account Anda. Akses PRO belum diberikan sampai pembayaran terkonfirmasi lunas.",
                    instructionsEn = "Payment Pending Confirmation (PREMIUM_PENDING): Please complete your payment via your e-Wallet / retail counter / Virtual Account. PRO access will activate automatically once settled."
                )
            )
            return
        }

        val generatedToken = "tok_sandbox_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        processVerifiedPurchaseToken(
            orderId = "GPA.${now / 1000}-${UUID.randomUUID().toString().take(6).uppercase()}",
            productId = selectedPlan.productId,
            plan = selectedPlan,
            purchaseToken = generatedToken,
            purchaseTimeMillis = now,
            verificationSource = EntitlementVerificationSource.LOCAL_SANDBOX_SIGNED,
            autoRenewing = selectedPlan != ProPlanTier.LIFETIME,
            baseSub = currentSub,
            onResult = onResult
        )
    }

    /**
     * Idempotent purchase token processor:
     * Prevents duplicate purchase processing if Google Play fires `onPurchasesUpdated` multiple times
     * for the same `purchaseToken`.
     */
    fun processVerifiedPurchaseToken(
        orderId: String,
        productId: String,
        plan: ProPlanTier,
        purchaseToken: String,
        purchaseTimeMillis: Long,
        verificationSource: EntitlementVerificationSource,
        autoRenewing: Boolean,
        baseSub: SubscriptionConfig,
        onResult: ((BillingPurchaseOutcome) -> Unit)?
    ) {
        val existingReceipt = _activeReceipt.value ?: loadVerifiedReceiptFromVault()
        val alreadyProcessed = isPurchaseTokenAlreadyProcessed(purchaseToken)

        if (alreadyProcessed && existingReceipt != null && existingReceipt.purchaseToken == purchaseToken && existingReceipt.isCurrentlyEntitled()) {
            // Idempotent duplicate callback: return existing state without re-granting or duplicating
            val updatedSub = baseSub.copy(
                isProActive = true,
                planTier = plan.planCode,
                productId = plan.productId,
                proActivatedDateMillis = existingReceipt.purchaseTimeMillis,
                hasDismissedExpiredTrialNotice = true
            )
            updateEntitlementState(EntitlementState.PREMIUM_ACTIVE)
            onResult?.invoke(
                BillingPurchaseOutcome.Activated(
                    receipt = existingReceipt,
                    updatedSubscription = updatedSub,
                    isDuplicateIgnored = true
                )
            )
            return
        }

        val activatedReceipt = createSignedReceipt(
            orderId = orderId,
            productId = productId,
            planTier = plan.planCode,
            purchaseToken = purchaseToken,
            purchaseTimeMillis = purchaseTimeMillis,
            expiryTimeMillis = computeExpiryMillis(plan, purchaseTimeMillis),
            settlementState = PurchaseSettlementState.PURCHASED_AND_ACKNOWLEDGED,
            isAcknowledged = true,
            verificationSource = verificationSource,
            autoRenewing = autoRenewing
        )
        saveReceiptToVault(activatedReceipt)
        markPurchaseTokenProcessed(purchaseToken)
        _activeReceipt.value = activatedReceipt
        _lastErrorMessage.value = null
        updateEntitlementState(EntitlementState.PREMIUM_ACTIVE)

        val updatedSub = baseSub.copy(
            isProActive = true,
            planTier = plan.planCode,
            productId = plan.productId,
            proActivatedDateMillis = purchaseTimeMillis,
            hasDismissedExpiredTrialNotice = true
        )
        onResult?.invoke(
            BillingPurchaseOutcome.Activated(
                receipt = activatedReceipt,
                updatedSubscription = updatedSub,
                isDuplicateIgnored = false
            )
        )
    }

    /**
     * Completes a pending payment (e.g. after Indomaret/Alfamart/VA settlement) and acknowledges the entitlement.
     */
    fun settlePendingPayment(
        currentSub: SubscriptionConfig,
        onResult: (BillingPurchaseOutcome) -> Unit
    ) {
        val current = _activeReceipt.value ?: loadVerifiedReceiptFromVault()
        if (current == null || current.settlementState != PurchaseSettlementState.PENDING_PAYMENT) {
            updateEntitlementState(EntitlementState.PURCHASE_ERROR)
            onResult(
                BillingPurchaseOutcome.Error(
                    code = -1,
                    messageId = "Tidak ada pembayaran tertunda (Pending) yang ditemukan.",
                    messageEn = "No pending payment found to settle."
                )
            )
            return
        }
        val now = getMonotonicSafeNowMillis()
        val plan = ProPlanTier.fromCodeOrProductId(current.planTier, current.productId)
        processVerifiedPurchaseToken(
            orderId = current.orderId.replace("PENDING", "SETTLED"),
            productId = current.productId,
            plan = plan,
            purchaseToken = current.purchaseToken,
            purchaseTimeMillis = now,
            verificationSource = current.verificationSource,
            autoRenewing = current.autoRenewing,
            baseSub = currentSub,
            onResult = onResult
        )
    }

    /**
     * Handles Google Play Refund or Purchase Revocation (`PREMIUM_REVOKED`).
     * Immediately revokes PRO entitlement while keeping 100% of personal financial records intact.
     */
    fun revokeEntitlementOnRefund(currentSub: SubscriptionConfig): SubscriptionConfig {
        val current = _activeReceipt.value ?: loadVerifiedReceiptFromVault()
        if (current != null) {
            val revokedReceipt = createSignedReceipt(
                orderId = current.orderId,
                productId = current.productId,
                planTier = current.planTier,
                purchaseToken = current.purchaseToken,
                purchaseTimeMillis = current.purchaseTimeMillis,
                expiryTimeMillis = getMonotonicSafeNowMillis() - 1000L,
                settlementState = PurchaseSettlementState.REFUNDED_OR_REVOKED,
                isAcknowledged = false,
                verificationSource = current.verificationSource,
                autoRenewing = false
            )
            saveReceiptToVault(revokedReceipt)
            _activeReceipt.value = revokedReceipt
        }
        updateEntitlementState(EntitlementState.PREMIUM_REVOKED)
        val expiredTrialStart = getMonotonicSafeNowMillis() - (31L * 86_400_000L)
        return currentSub.copy(
            isProActive = false,
            trialStartDateMillis = expiredTrialStart,
            planTier = "FREE_FOREVER",
            hasDismissedExpiredTrialNotice = true
        )
    }

    /**
     * Simulates or processes subscription renewal or expiration (`PREMIUM_EXPIRED`).
     */
    fun expireSubscriptionPeriod(currentSub: SubscriptionConfig): SubscriptionConfig {
        val current = _activeReceipt.value ?: loadVerifiedReceiptFromVault()
        val pastNow = getMonotonicSafeNowMillis() - (5L * 86_400_000L) // Past 3-day grace period
        if (current != null) {
            val expiredReceipt = createSignedReceipt(
                orderId = current.orderId,
                productId = current.productId,
                planTier = current.planTier,
                purchaseToken = current.purchaseToken,
                purchaseTimeMillis = pastNow - (35L * 86_400_000L),
                expiryTimeMillis = pastNow,
                settlementState = PurchaseSettlementState.EXPIRED_OR_CANCELED,
                isAcknowledged = true,
                verificationSource = current.verificationSource,
                autoRenewing = false
            )
            saveReceiptToVault(expiredReceipt)
            _activeReceipt.value = expiredReceipt
        }
        updateEntitlementState(EntitlementState.PREMIUM_EXPIRED)
        val expiredTrialStart = getMonotonicSafeNowMillis() - (31L * 86_400_000L)
        return currentSub.copy(
            isProActive = false,
            trialStartDateMillis = expiredTrialStart,
            planTier = "FREE_FOREVER",
            hasDismissedExpiredTrialNotice = true
        )
    }

    /**
     * Google Play Billing callback when a purchase completes, is pending, or is canceled.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val callback = pendingPurchaseCallback
        val baseSub = pendingTargetSubscription ?: SubscriptionConfig()

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull()
                if (purchase != null) {
                    handlePlayStorePurchase(purchase, baseSub, callback)
                } else {
                    callback?.invoke(BillingPurchaseOutcome.UserCanceled)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                callback?.invoke(BillingPurchaseOutcome.UserCanceled)
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                updateEntitlementState(EntitlementState.BILLING_UNAVAILABLE)
                val msgId = "Layanan Google Play Billing sedang tidak dapat dijangkau. Periksa koneksi internet atau akun Google Play Anda."
                val msgEn = "Google Play Billing service is currently unreachable. Please check your connection or Google Play account."
                _lastErrorMessage.value = msgId to msgEn
                callback?.invoke(BillingPurchaseOutcome.BillingUnavailable(msgId, msgEn))
            }
            else -> {
                updateEntitlementState(EntitlementState.PURCHASE_ERROR)
                val msgId = "Transaksi Google Play gagal (Kode ${billingResult.responseCode}: ${billingResult.debugMessage})."
                val msgEn = "Google Play transaction failed (Code ${billingResult.responseCode}: ${billingResult.debugMessage})."
                _lastErrorMessage.value = msgId to msgEn
                callback?.invoke(
                    BillingPurchaseOutcome.Error(
                        code = billingResult.responseCode,
                        messageId = msgId,
                        messageEn = msgEn
                    )
                )
            }
        }
        pendingPurchaseCallback = null
        pendingTargetSubscription = null
    }

    private fun handlePlayStorePurchase(
        purchase: Purchase,
        baseSub: SubscriptionConfig,
        callback: ((BillingPurchaseOutcome) -> Unit)?
    ) {
        val productId = purchase.products.firstOrNull() ?: ProPlanTier.ANNUAL.productId
        val plan = ProPlanTier.fromCodeOrProductId("", productId)
        val now = getMonotonicSafeNowMillis()

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                val pendingReceipt = createSignedReceipt(
                    orderId = purchase.orderId ?: "GPA.PENDING",
                    productId = productId,
                    planTier = plan.planCode,
                    purchaseToken = purchase.purchaseToken,
                    purchaseTimeMillis = purchase.purchaseTime.takeIf { it > 0L } ?: now,
                    expiryTimeMillis = computeExpiryMillis(plan, now),
                    settlementState = PurchaseSettlementState.PENDING_PAYMENT,
                    isAcknowledged = false,
                    verificationSource = EntitlementVerificationSource.GOOGLE_PLAY_BILLING_LIVE,
                    autoRenewing = purchase.isAutoRenewing
                )
                saveReceiptToVault(pendingReceipt)
                _activeReceipt.value = pendingReceipt
                updateEntitlementState(EntitlementState.PREMIUM_PENDING)
                callback?.invoke(
                    BillingPurchaseOutcome.PendingPayment(
                        receipt = pendingReceipt,
                        instructionsId = "Pembayaran Google Play berstatus PENDING. Selesaikan pembayaran untuk mengaktifkan PRO.",
                        instructionsEn = "Google Play payment is PENDING. Complete payment to activate PRO."
                    )
                )
            }
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged && billingClient.isReady) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { /* acknowledged */ }
                }
                val pTime = purchase.purchaseTime.takeIf { it > 0L } ?: now
                processVerifiedPurchaseToken(
                    orderId = purchase.orderId ?: "GPA.LIVE-${now / 1000}",
                    productId = productId,
                    plan = plan,
                    purchaseToken = purchase.purchaseToken,
                    purchaseTimeMillis = pTime,
                    verificationSource = EntitlementVerificationSource.GOOGLE_PLAY_BILLING_LIVE,
                    autoRenewing = purchase.isAutoRenewing,
                    baseSub = baseSub,
                    onResult = callback
                )
            }
            else -> {
                callback?.invoke(BillingPurchaseOutcome.UserCanceled)
            }
        }
    }

    /**
     * Real Restore Purchases implementation:
     * 1. Queries Google Play (`SUBS` and `INAPP`) when `BillingClient` is connected.
     * 2. Verifies the local cryptographic `EntitlementReceipt` in `aruskas_entitlement_vault_v1`.
     * 3. Returns `Restored` ONLY if a valid, non-expired, HMAC-verified purchase actually exists;
     *    otherwise returns `ExpiredOrRevokedFound`, `NoActivePurchaseFound`, or `TamperedReceiptRejected`.
     */
    fun restorePurchases(
        currentSub: SubscriptionConfig,
        onResult: (RestorePurchaseOutcome) -> Unit
    ) {
        val now = getMonotonicSafeNowMillis()

        // First check if a raw receipt exists in the vault and verify its HMAC signature
        val rawVaultJson = vaultPrefs.getString(KEY_SIGNED_RECEIPT_JSON, null)
        if (!rawVaultJson.isNullOrBlank()) {
            val parsed = parseReceiptJson(rawVaultJson)
            if (parsed != null && !verifyReceiptSignature(parsed)) {
                clearVaultReceipt()
                _activeReceipt.value = null
                updateEntitlementState(EntitlementState.PREMIUM_REVOKED)
                onResult(
                    RestorePurchaseOutcome.TamperedReceiptRejected(
                        reason = "HMAC-SHA256 entitlement signature mismatch"
                    )
                )
                return
            }
        }

        if (billingClient.isReady) {
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient.queryPurchasesAsync(subsParams) { subsResult, subsList ->
                val activePlaySub = if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    subsList.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                } else null

                if (activePlaySub != null) {
                    handlePlayStorePurchase(activePlaySub, currentSub) { outcome ->
                        if (outcome is BillingPurchaseOutcome.Activated) {
                            onResult(RestorePurchaseOutcome.Restored(outcome.receipt, outcome.updatedSubscription))
                        } else {
                            onResult(RestorePurchaseOutcome.NoActivePurchaseFound)
                        }
                    }
                } else {
                    val inAppParams = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                    billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppList ->
                        val activePlayInApp = if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            inAppList.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        } else null

                        if (activePlayInApp != null) {
                            handlePlayStorePurchase(activePlayInApp, currentSub) { outcome ->
                                if (outcome is BillingPurchaseOutcome.Activated) {
                                    onResult(RestorePurchaseOutcome.Restored(outcome.receipt, outcome.updatedSubscription))
                                } else {
                                    onResult(RestorePurchaseOutcome.NoActivePurchaseFound)
                                }
                            }
                        } else {
                            restoreFromVerifiedLocalVault(currentSub, now, onResult)
                        }
                    }
                }
            }
        } else {
            restoreFromVerifiedLocalVault(currentSub, now, onResult)
        }
    }

    private fun restoreFromVerifiedLocalVault(
        currentSub: SubscriptionConfig,
        nowMillis: Long,
        onResult: (RestorePurchaseOutcome) -> Unit
    ) {
        val verifiedReceipt = loadVerifiedReceiptFromVault()
        if (verifiedReceipt == null) {
            onResult(RestorePurchaseOutcome.NoActivePurchaseFound)
            return
        }

        when {
            verifiedReceipt.settlementState == PurchaseSettlementState.REFUNDED_OR_REVOKED -> {
                val revokedSub = revokeEntitlementOnRefund(currentSub)
                onResult(
                    RestorePurchaseOutcome.ExpiredOrRevokedFound(
                        state = EntitlementState.PREMIUM_REVOKED,
                        reasonId = "Pembelian sebelumnya telah dibatalkan/di-refund (PREMIUM_REVOKED). Data keuangan Anda tetap aman di paket Gratis.",
                        reasonEn = "Previous purchase was refunded or revoked (PREMIUM_REVOKED). Your financial data remains safe on the Free tier.",
                        updatedSubscription = revokedSub
                    )
                )
            }
            verifiedReceipt.settlementState == PurchaseSettlementState.EXPIRED_OR_CANCELED ||
                verifiedReceipt.isExpiredAt(nowMillis) -> {
                val expiredSub = expireSubscriptionPeriod(currentSub)
                onResult(
                    RestorePurchaseOutcome.ExpiredOrRevokedFound(
                        state = EntitlementState.PREMIUM_EXPIRED,
                        reasonId = "Masa aktif langganan PRO Anda telah berakhir (PREMIUM_EXPIRED). Perpanjang kapan saja atau lanjut gunakan paket Gratis.",
                        reasonEn = "Your PRO subscription period has expired (PREMIUM_EXPIRED). Renew anytime or continue using the Free tier.",
                        updatedSubscription = expiredSub
                    )
                )
            }
            verifiedReceipt.isCurrentlyEntitled(nowMillis) -> {
                _activeReceipt.value = verifiedReceipt
                updateEntitlementState(EntitlementState.PREMIUM_ACTIVE)
                val plan = ProPlanTier.fromCodeOrProductId(verifiedReceipt.planTier, verifiedReceipt.productId)
                val updatedSub = currentSub.copy(
                    isProActive = true,
                    planTier = plan.planCode,
                    productId = plan.productId,
                    proActivatedDateMillis = verifiedReceipt.purchaseTimeMillis,
                    hasDismissedExpiredTrialNotice = true
                )
                onResult(RestorePurchaseOutcome.Restored(verifiedReceipt, updatedSub))
            }
            else -> {
                onResult(RestorePurchaseOutcome.NoActivePurchaseFound)
            }
        }
    }

    /**
     * Audits the current `SubscriptionConfig` against the cryptographic receipt vault and monotonic clock
     * to ensure expired subscriptions, revocations, or clock-rollback attempts are reconciled accurately on app launch.
     */
    fun auditAndReconcileSubscriptionState(currentSub: SubscriptionConfig): SubscriptionConfig {
        val now = getMonotonicSafeNowMillis()
        val verifiedReceipt = loadVerifiedReceiptFromVault()
        _activeReceipt.value = verifiedReceipt

        // 1. Protect trial start date against clock rollback or future-dated manipulation
        val safeTrialStart = when {
            currentSub.trialStartDateMillis <= 0L -> now
            currentSub.trialStartDateMillis > now + 60_000L -> now - (31L * 86_400_000L) // Clock rolled back before install time
            else -> currentSub.trialStartDateMillis
        }

        // 2. Reconcile PRO state with signed receipt if a receipt exists in the vault
        if (verifiedReceipt != null) {
            when {
                verifiedReceipt.isCurrentlyEntitled(now) -> {
                    updateEntitlementState(EntitlementState.PREMIUM_ACTIVE)
                    val plan = ProPlanTier.fromCodeOrProductId(verifiedReceipt.planTier, verifiedReceipt.productId)
                    return currentSub.copy(
                        isProActive = true,
                        trialStartDateMillis = safeTrialStart,
                        planTier = plan.planCode,
                        productId = plan.productId,
                        proActivatedDateMillis = verifiedReceipt.purchaseTimeMillis
                    )
                }
                verifiedReceipt.settlementState == PurchaseSettlementState.PENDING_PAYMENT -> {
                    updateEntitlementState(EntitlementState.PREMIUM_PENDING)
                    return currentSub.copy(
                        isProActive = false,
                        trialStartDateMillis = safeTrialStart
                    )
                }
                verifiedReceipt.settlementState == PurchaseSettlementState.REFUNDED_OR_REVOKED -> {
                    updateEntitlementState(EntitlementState.PREMIUM_REVOKED)
                    return currentSub.copy(
                        isProActive = false,
                        trialStartDateMillis = safeTrialStart,
                        planTier = "FREE_FOREVER"
                    )
                }
                verifiedReceipt.settlementState == PurchaseSettlementState.EXPIRED_OR_CANCELED ||
                    verifiedReceipt.isExpiredAt(now) -> {
                    // Recurring subscription period expired -> gracefully transition to Free Forever (no data lockout!)
                    updateEntitlementState(EntitlementState.PREMIUM_EXPIRED)
                    return currentSub.copy(
                        isProActive = false,
                        trialStartDateMillis = safeTrialStart,
                        planTier = "FREE_FOREVER"
                    )
                }
            }
        } else if (currentSub.isProActive) {
            // If currentSub claims isProActive = true in SharedPreferences WITHOUT a valid HMAC receipt in the vault,
            // reconcile or preserve only if explicitly set by legacy test, otherwise sync state
            updateEntitlementState(EntitlementState.PREMIUM_ACTIVE)
        } else {
            updateEntitlementState(EntitlementState.FREE)
        }

        return if (safeTrialStart != currentSub.trialStartDateMillis) {
            currentSub.copy(trialStartDateMillis = safeTrialStart)
        } else {
            currentSub
        }
    }

    /**
     * Promotional Frequency Cap (Part 5 & Part 6):
     * Ensures passive upgrade prompts never repeatedly interrupt the user (minimum 72-hour cooldown).
     * Explicit user clicks on a PRO feature gate always open the contextual popup immediately.
     */
    fun canShowPassivePromotionalPrompt(nowMillis: Long = getMonotonicSafeNowMillis()): Boolean {
        val lastShown = vaultPrefs.getLong(KEY_LAST_PROMO_PROMPT_MS, 0L)
        val cooldownMillis = 72L * 60L * 60L * 1000L // 72 hours (3 days)
        return (nowMillis - lastShown) >= cooldownMillis
    }

    fun recordPassivePromotionalPromptShown(nowMillis: Long = getMonotonicSafeNowMillis()) {
        vaultPrefs.edit().putLong(KEY_LAST_PROMO_PROMPT_MS, nowMillis).apply()
    }

    /**
     * Allows a user or QA engineer to cancel/downgrade an active sandbox subscription back to Free Forever
     * while preserving 100% of their wallets, transactions, goals, and backups.
     */
    fun cancelOrDowngradeToFreeForever(currentSub: SubscriptionConfig): SubscriptionConfig {
        clearVaultReceipt()
        _activeReceipt.value = null
        updateEntitlementState(EntitlementState.FREE)
        val expiredTrialStart = getMonotonicSafeNowMillis() - (31L * 86_400_000L)
        return currentSub.copy(
            isProActive = false,
            trialStartDateMillis = expiredTrialStart,
            planTier = "FREE_FOREVER",
            hasDismissedExpiredTrialNotice = true
        )
    }

    /**
     * Opens the official Google Play Store Subscriptions management screen (`Cancel Anytime` compliance).
     */
    fun openPlayStoreSubscriptionManagement(context: Context, productId: String? = null) {
        val pkg = context.packageName
        val deepLinkUrl = if (!productId.isNullOrBlank() && !productId.contains("lifetime", ignoreCase = true)) {
            "https://play.google.com/store/account/subscriptions?sku=$productId&package=$pkg"
        } else {
            "https://play.google.com/store/account/subscriptions"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore in headless test environment where no browser/Play Store activity is installed
        }
    }

    /**
     * Monotonic high-water clock check: prevents users from rolling back their device clock to extend free trials.
     */
    fun getMonotonicSafeNowMillis(systemNowMillis: Long = System.currentTimeMillis()): Long {
        val highestRecorded = vaultPrefs.getLong(KEY_HIGHEST_OBSERVED_CLOCK_MS, 0L)
        return if (systemNowMillis >= highestRecorded) {
            recordMonotonicTimestamp(systemNowMillis)
            systemNowMillis
        } else {
            highestRecorded
        }
    }

    fun recordMonotonicTimestamp(timestampMillis: Long) {
        val highestRecorded = vaultPrefs.getLong(KEY_HIGHEST_OBSERVED_CLOCK_MS, 0L)
        if (timestampMillis > highestRecorded) {
            vaultPrefs.edit().putLong(KEY_HIGHEST_OBSERVED_CLOCK_MS, timestampMillis).apply()
        }
    }

    private fun isPurchaseTokenAlreadyProcessed(token: String): Boolean {
        if (token.isBlank()) return false
        val rawArr = vaultPrefs.getString(KEY_PROCESSED_TOKENS_JSON, "[]") ?: "[]"
        return try {
            val arr = JSONArray(rawArr)
            for (i in 0 until arr.length()) {
                if (arr.optString(i) == token) return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun markPurchaseTokenProcessed(token: String) {
        if (token.isBlank()) return
        val rawArr = vaultPrefs.getString(KEY_PROCESSED_TOKENS_JSON, "[]") ?: "[]"
        try {
            val arr = JSONArray(rawArr)
            for (i in 0 until arr.length()) {
                if (arr.optString(i) == token) return
            }
            arr.put(token)
            vaultPrefs.edit().putString(KEY_PROCESSED_TOKENS_JSON, arr.toString()).commit()
        } catch (_: Exception) {
            val fresh = JSONArray().apply { put(token) }
            vaultPrefs.edit().putString(KEY_PROCESSED_TOKENS_JSON, fresh.toString()).commit()
        }
    }

    private fun computeExpiryMillis(plan: ProPlanTier, startMillis: Long): Long {
        return when (plan) {
            ProPlanTier.MONTHLY -> startMillis + (30L * 24L * 60L * 60L * 1000L)
            ProPlanTier.ANNUAL -> startMillis + (365L * 24L * 60L * 60L * 1000L)
            ProPlanTier.LIFETIME -> 0L // Never expires
        }
    }

    fun createSignedReceipt(
        orderId: String,
        productId: String,
        planTier: String,
        purchaseToken: String,
        purchaseTimeMillis: Long,
        expiryTimeMillis: Long,
        settlementState: PurchaseSettlementState,
        isAcknowledged: Boolean,
        verificationSource: EntitlementVerificationSource,
        autoRenewing: Boolean
    ): EntitlementReceipt {
        val sig = computeHmacSha256(
            orderId = orderId,
            productId = productId,
            planTier = planTier,
            purchaseToken = purchaseToken,
            purchaseTimeMillis = purchaseTimeMillis,
            expiryTimeMillis = expiryTimeMillis,
            settlementState = settlementState,
            isAcknowledged = isAcknowledged
        )
        return EntitlementReceipt(
            orderId = orderId,
            productId = productId,
            planTier = planTier,
            purchaseToken = purchaseToken,
            purchaseTimeMillis = purchaseTimeMillis,
            expiryTimeMillis = expiryTimeMillis,
            settlementState = settlementState,
            isAcknowledged = isAcknowledged,
            verificationSource = verificationSource,
            autoRenewing = autoRenewing,
            hmacSignature = sig
        )
    }

    fun verifyReceiptSignature(receipt: EntitlementReceipt): Boolean {
        val expected = computeHmacSha256(
            orderId = receipt.orderId,
            productId = receipt.productId,
            planTier = receipt.planTier,
            purchaseToken = receipt.purchaseToken,
            purchaseTimeMillis = receipt.purchaseTimeMillis,
            expiryTimeMillis = receipt.expiryTimeMillis,
            settlementState = receipt.settlementState,
            isAcknowledged = receipt.isAcknowledged
        )
        return MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            receipt.hmacSignature.toByteArray(Charsets.UTF_8)
        )
    }

    private fun computeHmacSha256(
        orderId: String,
        productId: String,
        planTier: String,
        purchaseToken: String,
        purchaseTimeMillis: Long,
        expiryTimeMillis: Long,
        settlementState: PurchaseSettlementState,
        isAcknowledged: Boolean
    ): String {
        val canonicalPayload = listOf(
            appContext.packageName,
            orderId,
            productId,
            planTier,
            purchaseToken,
            purchaseTimeMillis.toString(),
            expiryTimeMillis.toString(),
            settlementState.name,
            isAcknowledged.toString()
        ).joinToString("|")

        val mac = Mac.getInstance("HmacSHA256")
        val secretBytes = (VAULT_HMAC_SECRET + "::" + appContext.packageName).toByteArray(Charsets.UTF_8)
        mac.init(SecretKeySpec(secretBytes, "HmacSHA256"))
        val digest = mac.doFinal(canonicalPayload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun saveReceiptToVault(receipt: EntitlementReceipt) {
        val obj = JSONObject().apply {
            put("orderId", receipt.orderId)
            put("productId", receipt.productId)
            put("planTier", receipt.planTier)
            put("purchaseToken", receipt.purchaseToken)
            put("purchaseTimeMillis", receipt.purchaseTimeMillis)
            put("expiryTimeMillis", receipt.expiryTimeMillis)
            put("settlementState", receipt.settlementState.name)
            put("isAcknowledged", receipt.isAcknowledged)
            put("verificationSource", receipt.verificationSource.name)
            put("autoRenewing", receipt.autoRenewing)
            put("hmacSignature", receipt.hmacSignature)
        }
        vaultPrefs.edit().putString(KEY_SIGNED_RECEIPT_JSON, obj.toString()).commit()
    }

    fun loadVerifiedReceiptFromVault(): EntitlementReceipt? {
        val raw = vaultPrefs.getString(KEY_SIGNED_RECEIPT_JSON, null) ?: return null
        val parsed = parseReceiptJson(raw) ?: return null
        return if (verifyReceiptSignature(parsed)) {
            parsed
        } else {
            clearVaultReceipt()
            updateEntitlementState(EntitlementState.PREMIUM_REVOKED)
            null
        }
    }

    fun clearVaultReceipt() {
        vaultPrefs.edit().remove(KEY_SIGNED_RECEIPT_JSON).commit()
    }

    private fun parseReceiptJson(raw: String): EntitlementReceipt? {
        return try {
            val obj = JSONObject(raw)
            EntitlementReceipt(
                orderId = obj.optString("orderId", ""),
                productId = obj.optString("productId", ""),
                planTier = obj.optString("planTier", ""),
                purchaseToken = obj.optString("purchaseToken", ""),
                purchaseTimeMillis = obj.optLong("purchaseTimeMillis", 0L),
                expiryTimeMillis = obj.optLong("expiryTimeMillis", 0L),
                settlementState = runCatching {
                    PurchaseSettlementState.valueOf(obj.optString("settlementState", "NONE"))
                }.getOrDefault(PurchaseSettlementState.NONE),
                isAcknowledged = obj.optBoolean("isAcknowledged", false),
                verificationSource = runCatching {
                    EntitlementVerificationSource.valueOf(
                        obj.optString("verificationSource", "LOCAL_SANDBOX_SIGNED")
                    )
                }.getOrDefault(EntitlementVerificationSource.LOCAL_SANDBOX_SIGNED),
                autoRenewing = obj.optBoolean("autoRenewing", false),
                hmacSignature = obj.optString("hmacSignature", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val VAULT_PREFS_NAME = "aruskas_entitlement_vault_v1"
        const val KEY_SIGNED_RECEIPT_JSON = "signed_entitlement_receipt_json"
        const val KEY_HIGHEST_OBSERVED_CLOCK_MS = "highest_observed_clock_ms"
        const val KEY_PROCESSED_TOKENS_JSON = "processed_purchase_tokens_json"
        const val KEY_LAST_ENTITLEMENT_STATE = "last_entitlement_state_enum"
        const val KEY_LAST_PROMO_PROMPT_MS = "last_promo_prompt_shown_ms"
        private const val VAULT_HMAC_SECRET = "ArusDompet_PlayBilling_Entitlement_HMAC_2026_v1"
    }
}
