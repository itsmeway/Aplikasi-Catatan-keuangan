package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLanguage
import com.example.data.AppLocalization
import com.example.data.EntitlementReceipt
import com.example.data.EntitlementState
import com.example.data.MonetizationPricingCatalog
import com.example.data.ProFeatureTrigger
import com.example.data.ProPlanTier
import com.example.data.PurchaseSettlementState
import com.example.data.ResolvedPlanPriceInfo
import com.example.data.SubscriptionConfig
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Complete Ethical Freemium + Hybrid 3-Tier PRO Purchasing Experience for ARUS DOMPET (ArusKas PRO).
 *
 * Implements all 13 Required Screens & States (Part 5 Specification):
 * - A. Premium Introduction Screen
 * - B. Premium Comparison Screen (Free Forever vs. PRO)
 * - C. Monthly and Annual Pricing Cards (Live Google Play ProductDetails or labeled regional reference)
 * - D. One-Time Lifetime Purchase Option
 * - E. Feature-Specific Contextual Upgrade Popup (`ProFeatureTrigger`)
 * - F. Pre-Purchase Confirmation State (`showConfirmCheckoutModal`)
 * - G. Successful Purchase Screen (`showPurchaseSuccessBanner`)
 * - H. Purchase Pending State (`EntitlementState.PREMIUM_PENDING`)
 * - I. Purchase Failure / Billing Unavailable State (`PURCHASE_ERROR` / `BILLING_UNAVAILABLE`)
 * - J. Restore Purchases Functionality
 * - K. Subscription Management & Cancellation Information
 * - L. Premium Account Status (`EntitlementState`)
 * - M. Expiration or Renewal Information
 */
@Composable
fun ArusKasProPaywallDialog(
    subscription: SubscriptionConfig,
    language: AppLanguage,
    countryCode: String = WorldCountryCurrencyRegistry.activeCountryCode,
    trigger: ProFeatureTrigger = ProFeatureTrigger.GENERAL_UPGRADE,
    activeReceipt: EntitlementReceipt? = null,
    entitlementState: EntitlementState = if (subscription.isProActive) EntitlementState.PREMIUM_ACTIVE else EntitlementState.FREE,
    isPlayStoreConnected: Boolean = false,
    resolvedPrices: Map<ProPlanTier, ResolvedPlanPriceInfo> = emptyMap(),
    lastErrorMessage: Pair<String, String>? = null,
    onDismiss: () -> Unit,
    onActivateFreeTrial: () -> Unit,
    onSubscribePro: () -> Unit,
    onSubscribeProWithPlan: (ProPlanTier) -> Unit = { onSubscribePro() },
    onSimulatePendingRetailPayment: ((ProPlanTier) -> Unit)? = null,
    onSettlePendingPayment: (() -> Unit)? = null,
    onSimulateRefundRevocation: (() -> Unit)? = null,
    onSimulateSubscriptionExpiration: (() -> Unit)? = null,
    onDowngradeToFreeForever: (() -> Unit)? = null,
    onRestorePurchase: () -> Unit
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN
    val isPro = subscription.isProActive
    val isTrial = subscription.isTrialActive
    val isFreeTierAfterTrial = !isPro && !isTrial
    val remainingDays = subscription.remainingTrialDays
    var showBillingInspector by remember { mutableStateOf(false) }
    var showComparisonTable by remember { mutableStateOf(false) }
    var showConfirmCheckoutModal by remember { mutableStateOf(false) }
    var justCompletedPurchase by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }

    val fallbackCatalog = remember(countryCode) {
        MonetizationPricingCatalog.getPricingForCountry(countryCode)
    }

    val annualPriceStr = resolvedPrices[ProPlanTier.ANNUAL]?.formattedPrice ?: fallbackCatalog.annualFormatted
    val lifetimePriceStr = resolvedPrices[ProPlanTier.LIFETIME]?.formattedPrice ?: fallbackCatalog.lifetimeFormatted
    val monthlyPriceStr = resolvedPrices[ProPlanTier.MONTHLY]?.formattedPrice ?: fallbackCatalog.monthlyFormatted
    val isLivePlayPricing = resolvedPrices.values.any { it.isFromLiveGooglePlay }

    var selectedPlan by remember(subscription.planTier, subscription.productId) {
        val initial = if (subscription.isProActive) {
            subscription.activePlanTierEnum
        } else {
            ProPlanTier.ANNUAL
        }
        mutableStateOf(initial)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 14.dp)
                .testTag("aruskas_pro_paywall_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Status Badge & Always-Visible Close Button (Never hidden or disguised)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isPro) EmeraldPrimary.copy(alpha = 0.16f) else GoldAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isPro) EmeraldPrimary else GoldAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPro) Icons.Default.Verified else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isPro) EmeraldPrimary else GoldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = when {
                                    subscription.isLifetimePro -> AppLocalization.get("pro_lifetime_chip", language)
                                    isPro -> AppLocalization.get("pro_active_chip", language)
                                    isTrial -> String.format(AppLocalization.get("pro_trial_chip", language), remainingDays)
                                    else -> AppLocalization.get("pro_badge", language)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = if (isPro) EmeraldPrimary else GoldAccent,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("paywall_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppLocalization.get("btn_close", language),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SCREEN G: SUCCESSFUL PURCHASE CONFIRMATION BANNER (When PRO is active)
                if (isPro || justCompletedPurchase) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.2.dp, EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_success_status_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (subscription.isLifetimePro) {
                                    if (isId) "✅ ArusKas PRO Lifetime Aktif Selamanya" else "✅ ArusKas PRO Lifetime Permanently Active"
                                } else {
                                    if (isId) "✅ Langganan ArusKas PRO Aktif" else "✅ ArusKas PRO Subscription Active"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = if (isId) {
                                    "Seluruh fitur PRO telah terbuka: 12 karakter asisten offline (10.000 kalimat lokal), diagnostik mendalam 5-bagian, dompet & target tanpa batas, siklus kalender gajian, serta laporan PDF A4 resmi."
                                } else {
                                    "All PRO features are unlocked: all 12 offline assistant personas (10,000 local phrases), 5-part deep diagnostics, unlimited wallets & goals, custom payday cycles, and executive A4 PDF reports."
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, lineHeight = 13.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // SCREEN I: PURCHASE FAILURE / BILLING UNAVAILABLE / REVOKED / EXPIRED BANNER
                if (entitlementState == EntitlementState.PURCHASE_ERROR ||
                    entitlementState == EntitlementState.BILLING_UNAVAILABLE ||
                    entitlementState == EntitlementState.PREMIUM_REVOKED ||
                    entitlementState == EntitlementState.PREMIUM_EXPIRED
                ) {
                    val statusBannerTitle = when (entitlementState) {
                        EntitlementState.PURCHASE_ERROR -> if (isId) "Pembayaran Belum Berhasil (PURCHASE_ERROR)" else "Purchase Could Not Complete (PURCHASE_ERROR)"
                        EntitlementState.BILLING_UNAVAILABLE -> if (isId) "Google Play Billing Belum Tersedia (BILLING_UNAVAILABLE)" else "Google Play Billing Unavailable (BILLING_UNAVAILABLE)"
                        EntitlementState.PREMIUM_REVOKED -> if (isId) "Lisensi PRO Dibatalkan / Refund (PREMIUM_REVOKED)" else "PRO License Revoked / Refunded (PREMIUM_REVOKED)"
                        else -> if (isId) "Masa Langganan PRO Berakhir (PREMIUM_EXPIRED)" else "PRO Subscription Period Ended (PREMIUM_EXPIRED)"
                    }
                    val statusBannerBody = lastErrorMessage?.let { if (isId) it.first else it.second }
                        ?: if (isId) {
                            "Jangan khawatir, seluruh catatan transaksi, saldo dompet, dan cadangan JSON Anda 100% aman di paket Gratis Selamanya."
                        } else {
                            "Rest assured, all your transaction records, wallet balances, and JSON backups remain 100% safe on the Free Forever tier."
                        }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CoralExpense.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralExpense.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_error_or_expired_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = CoralExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = statusBannerTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CoralExpense
                                )
                            }
                            Text(
                                text = statusBannerBody,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // SCREEN E: CONTEXTUAL UPGRADE BANNER (When triggered by a specific PRO feature)
                if (trigger != ProFeatureTrigger.GENERAL_UPGRADE || isFreeTierAfterTrial) {
                    val effectiveTrigger = if (trigger == ProFeatureTrigger.GENERAL_UPGRADE && isFreeTierAfterTrial) {
                        ProFeatureTrigger.TRIAL_ENDED_NOTICE
                    } else {
                        trigger
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BcaRoyalBlue.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paywall_contextual_trigger_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = BcaRoyalBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = effectiveTrigger.title(language),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BcaRoyalBlue
                                )
                            }
                            Text(
                                text = effectiveTrigger.subtitle(language),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // SCREEN A: PREMIUM INTRODUCTION HEADER (ArusKas PRO Identity)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1B1602),
                        border = BorderStroke(1.5.dp, GoldAccent),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppLocalization.get("paywall_title", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppLocalization.get("paywall_subtitle", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ethical Data Safety Guarantee Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldPrimary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("trial_highlight_badge")
                ) {
                    Text(
                        text = if (isId) {
                            "🛡️ DATA TRANSAKSI, SALDO & BACKUP JSON 100% GRATIS SELAMANYA"
                        } else {
                            "🛡️ CORE LEDGER, BALANCES & JSON BACKUP 100% FREE FOREVER"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Source Indicator (Live Google Play ProductDetails vs Regional Reference Catalog)
                Text(
                    text = if (isLivePlayPricing) {
                        if (isId) "✓ Harga Resmi Terkoneksi Google Play Billing" else "✓ Live Price Retrieved from Google Play Billing"
                    } else {
                        if (isId) "Info Harga Regional (${fallbackCatalog.currencyCode}) • Harga final mengikuti Google Play saat checkout"
                        else "Regional Reference Pricing (${fallbackCatalog.currencyCode}) • Final price confirmed by Google Play at checkout"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SCREENS C & D: 3-TIER HYBRID PLAN SELECTOR (ANNUAL, LIFETIME, MONTHLY)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. ANNUAL PLAN (MOST POPULAR / SAVE 45-50%)
                    PlanOptionCard(
                        tier = ProPlanTier.ANNUAL,
                        isSelected = selectedPlan == ProPlanTier.ANNUAL,
                        badgeText = if (isId) "PALING HEMAT • DISKON ${fallbackCatalog.annualSavingsPercent}%" else "MOST POPULAR • SAVE ${fallbackCatalog.annualSavingsPercent}%",
                        badgeColor = GoldAccent,
                        title = if (isId) "Tahunan (12 Bulan)" else "Annual (12 Months)",
                        priceHeadline = "$annualPriceStr / ${if (isId) "Tahun" else "Year"}",
                        subtext = resolvedPrices[ProPlanTier.ANNUAL]?.let { if (isId) it.subtextId else it.subtextEn }
                            ?: if (isId) {
                                "Setara ${fallbackCatalog.annualMonthlyEquivalentFormatted}/bulan • Hemat ${fallbackCatalog.annualSavingsPercent}% dibanding bulanan"
                            } else {
                                "Just ${fallbackCatalog.annualMonthlyEquivalentFormatted}/month • Save ${fallbackCatalog.annualSavingsPercent}% vs monthly"
                            },
                        onSelect = { selectedPlan = ProPlanTier.ANNUAL }
                    )

                    // 2. LIFETIME PLAN (ONE-TIME PURCHASE / OWN FOREVER)
                    PlanOptionCard(
                        tier = ProPlanTier.LIFETIME,
                        isSelected = selectedPlan == ProPlanTier.LIFETIME,
                        badgeText = if (isId) "SEKALI BAYAR SELAMANYA 👑" else "ONE-TIME FOREVER 👑",
                        badgeColor = EmeraldPrimary,
                        title = if (isId) "Lisensi Seumur Hidup (Lifetime)" else "Lifetime License (One-Time)",
                        priceHeadline = lifetimePriceStr,
                        subtext = resolvedPrices[ProPlanTier.LIFETIME]?.let { if (isId) it.subtextId else it.subtextEn }
                            ?: if (isId) {
                                "Bayar 1x untuk selamanya • Tanpa tagihan bulanan atau tahunan lagi"
                            } else {
                                "Pay once, own ArusKas PRO forever • Zero recurring subscription fees"
                            },
                        onSelect = { selectedPlan = ProPlanTier.LIFETIME }
                    )

                    // 3. MONTHLY PLAN (FLEXIBLE)
                    PlanOptionCard(
                        tier = ProPlanTier.MONTHLY,
                        isSelected = selectedPlan == ProPlanTier.MONTHLY,
                        badgeText = if (isId) "FLEKSIBEL" else "FLEXIBLE",
                        badgeColor = SkyBlue,
                        title = if (isId) "Bulanan (1 Bulan)" else "Monthly (1 Month)",
                        priceHeadline = "$monthlyPriceStr / ${if (isId) "Bulan" else "Month"}",
                        subtext = resolvedPrices[ProPlanTier.MONTHLY]?.let { if (isId) it.subtextId else it.subtextEn }
                            ?: if (isId) fallbackCatalog.monthlyPerDayHintId else fallbackCatalog.monthlyPerDayHintEn,
                        onSelect = { selectedPlan = ProPlanTier.MONTHLY }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Core PRO Benefits Summary Card + SCREEN B: EXPANDABLE COMPARISON TABLE
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = if (isId) "Apa yang Langsung Anda Dapatkan di ArusKas PRO:" else "What You Receive Immediately with ArusKas PRO:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        ProFeatureRow(
                            if (isId) "12 Karakter Asisten Offline Deterministik (10.000 Kalimat Lokal: Si Roaster 🔥, Si Sarkas 😏, Si Detektif 🕵️, dll) & Acak Kalimat Tanpa Batas"
                            else "All 12 Deterministic Offline Assistant Personas (10,000 Local Phrases: Roaster 🔥, Sarcastic 😏, Detective 🕵️, etc.) & Unlimited Daily Insight Refreshes"
                        )
                        ProFeatureRow(
                            if (isId) "Diagnostik Mendalam 5-Bagian: Fakta Angka Nyata, Pola 30 Hari & Rekomendasi Strategis"
                            else "5-Part Deep Financial Diagnostic: Factual Breakdown, 30-Day Habit Patterns & Action Plan"
                        )
                        ProFeatureRow(
                            if (isId) "Dompet/Rekening Tanpa Batas (6+ Akun), Kategori Kustom Tanpa Batas & Siklus Kalender Gajian"
                            else "Unlimited Wallets (6+ Accounts), Unlimited Custom Categories & Custom Payday Calendar Cycles"
                        )
                        ProFeatureRow(
                            if (isId) "Celengan Target Impian (3+) & Ceklist Tagihan Wajib (5+) Tanpa Batas"
                            else "Unlimited Simultaneous Savings Goals (3+) & Recurring Mandatory Bills (5+)"
                        )
                        ProFeatureRow(
                            if (isId) "Ekspor Laporan Tabel PDF Resmi A4, Smart Merge Restore & Tema Eksklusif"
                            else "Executive Styled A4 PDF Reports, Smart Duplicate-Safe Merge Restore & Premium Themes"
                        )

                        // SCREEN B: Expandable Free vs PRO Full Comparison Table
                        Surface(
                            onClick = { showComparisonTable = !showComparisonTable },
                            shape = RoundedCornerShape(8.dp),
                            color = BcaRoyalBlue.copy(alpha = 0.10f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .testTag("toggle_free_vs_pro_comparison_button")
                        ) {
                            Text(
                                text = if (showComparisonTable) {
                                    if (isId) "▲ Sembunyikan Tabel Perbandingan Gratis vs PRO" else "▲ Hide Free Forever vs PRO Comparison"
                                } else {
                                    if (isId) "▼ Lihat Tabel Perbandingan Gratis Selamanya vs PRO" else "▼ Compare Free Forever vs PRO Features"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BcaRoyalBlue,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                            )
                        }

                        if (showComparisonTable) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            FreeVsProComparisonRow(
                                feature = if (isId) "Catat Pemasukan, Pengeluaran & Riwayat Excel" else "Income/Expense Logging & Excel History",
                                freeVal = if (isId) "Gratis Tanpa Batas" else "Unlimited Free",
                                proVal = if (isId) "Gratis Tanpa Batas" else "Unlimited Free"
                            )
                            FreeVsProComparisonRow(
                                feature = if (isId) "Backup JSON SHA-256 & Ekspor Excel (.CSV)" else "SHA-256 JSON Backup & CSV Export",
                                freeVal = if (isId) "100% Gratis" else "100% Free",
                                proVal = if (isId) "+ Smart Merge & PDF A4" else "+ Smart Merge & A4 PDF"
                            )
                            FreeVsProComparisonRow(
                                feature = if (isId) "Jumlah Rekening Bank & E-Wallet" else "Active Bank & E-Wallet Accounts",
                                freeVal = if (isId) "Hingga 5 Dompet" else "Up to 5 Wallets",
                                proVal = if (isId) "Tanpa Batas (Unlimited)" else "Unlimited"
                            )
                            FreeVsProComparisonRow(
                                feature = if (isId) "Kategori Pengeluaran & Periode Limit" else "Categories & Budget Cycles",
                                freeVal = if (isId) "6 Utama + 1 Kustom" else "6 Core + 1 Custom",
                                proVal = if (isId) "Tanpa Batas + Siklus Gajian" else "Unlimited + Payday Dates"
                            )
                            FreeVsProComparisonRow(
                                feature = if (isId) "Target Tabungan & Tagihan Wajib" else "Savings Goals & Mandatory Bills",
                                freeVal = if (isId) "2 Target & 4 Tagihan" else "2 Goals & 4 Bills",
                                proVal = if (isId) "Tanpa Batas (Unlimited)" else "Unlimited"
                            )
                            FreeVsProComparisonRow(
                                feature = if (isId) "Mesin Asisten Keuangan Offline (10.000 Kalimat)" else "Offline Smart Assistant Engine (10,000 Phrases)",
                                freeVal = if (isId) "3 Karakter + 3x Acak/Hari" else "3 Personas + 3x/Day",
                                proVal = if (isId) "12 Karakter + Analisis 5-Bagian" else "All 12 Personas + Deep Analysis"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Google Play Billing Policy Disclosure (Clear & Readable >= 10.sp)
                val policyText = when (selectedPlan) {
                    ProPlanTier.LIFETIME -> if (isId) {
                        "Pembelian Sekali Bayar ($lifetimePriceStr) untuk akses ArusKas PRO selamanya. Tidak ada perpanjangan otomatis atau tagihan berulang. Pembelian diproses resmi melalui Google Play dan terhubung ke akun Google Play Anda."
                    } else {
                        "One-time payment ($lifetimePriceStr) for permanent ArusKas PRO access. Never auto-renews and has zero recurring charges. Processed officially via Google Play and linked to your Google Play account."
                    }
                    ProPlanTier.ANNUAL -> if (isId) {
                        "Langganan Tahunan ($annualPriceStr/tahun) diperpanjang otomatis setiap 12 bulan melalui Google Play kecuali dibatalkan minimal 24 jam sebelum periode berakhir di menu Langganan Google Play Store."
                    } else {
                        "Annual subscription ($annualPriceStr/year) auto-renews every 12 months via Google Play unless canceled at least 24 hours before renewal in Google Play Store Subscriptions."
                    }
                    ProPlanTier.MONTHLY -> if (isId) {
                        "Langganan Bulanan ($monthlyPriceStr/bulan) diperpanjang otomatis setiap bulan melalui Google Play. Anda dapat membatalkan kapan saja tanpa penalti melalui menu Langganan Google Play Store."
                    } else {
                        "Monthly subscription ($monthlyPriceStr/month) auto-renews monthly via Google Play. Cancel anytime with zero penalty in Google Play Store Subscriptions."
                    }
                }
                Text(
                    text = policyText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Primary CTA Action Area
                val ctaLabel = when (selectedPlan) {
                    ProPlanTier.ANNUAL -> if (isId) {
                        "Aktifkan PRO Tahunan ($annualPriceStr/thn)"
                    } else {
                        "Unlock Annual PRO ($annualPriceStr/yr)"
                    }
                    ProPlanTier.LIFETIME -> if (isId) {
                        "Beli PRO Selamanya ($lifetimePriceStr Sekali Bayar)"
                    } else {
                        "Buy Lifetime PRO ($lifetimePriceStr Once)"
                    }
                    ProPlanTier.MONTHLY -> if (isId) {
                        "Langganan PRO Bulanan ($monthlyPriceStr/bln)"
                    } else {
                        "Subscribe Monthly PRO ($monthlyPriceStr/mo)"
                    }
                }

                if (isPro) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.18f),
                        border = BorderStroke(1.2.dp, EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (subscription.isLifetimePro) {
                                    if (isId) "👑 Lisensi ArusKas PRO Selamanya Aktif" else "👑 ArusKas PRO Lifetime Active"
                                } else {
                                    AppLocalization.get("pro_active_chip", language)
                                },
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (!subscription.isLifetimePro && selectedPlan == ProPlanTier.LIFETIME) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onSubscribeProWithPlan(ProPlanTier.LIFETIME) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color(0xFF241800)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("upgrade_to_lifetime_button")
                        ) {
                            Text(
                                text = ctaLabel,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            justCompletedPurchase = true
                            onSubscribeProWithPlan(selectedPlan)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color(0xFF241800)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("subscribe_pro_button")
                    ) {
                        Text(
                            text = ctaLabel,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // SCREEN F: Pre-Checkout Summary & Terms Confirmation Modal Trigger
                    TextButton(
                        onClick = { showConfirmCheckoutModal = true },
                        modifier = Modifier.testTag("open_purchase_confirmation_details_button")
                    ) {
                        Text(
                            text = if (isId) "Lihat Rincian Konfirmasi Pembelian & Syarat Perpanjangan" else "Review Purchase Confirmation & Renewal Terms",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            color = BcaRoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (subscription.trialStartDateMillis <= 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onActivateFreeTrial,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_free_trial_button")
                        ) {
                            Text(
                                text = AppLocalization.get("btn_start_free_trial", language),
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Explicit Ethical Free Forever Continuation Button (Minimum 48.dp touch target)
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_free_forever_button")
                    ) {
                        Text(
                            text = if (isTrial) {
                                if (isId) "Lanjut Uji Coba Gratis ($remainingDays Hari Tersisa)" else "Continue Free Trial ($remainingDays Days Left)"
                            } else {
                                if (isId) "Lanjut Pakai Versi Gratis Selamanya" else "Continue with Free Forever Tier"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // SCREEN H: PENDING RETAIL / E-WALLET PAYMENT BANNER (GoPay, DANA, OVO, Indomaret, Alfamart, VA)
                if (activeReceipt?.settlementState == PurchaseSettlementState.PENDING_PAYMENT ||
                    entitlementState == EntitlementState.PREMIUM_PENDING
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GoldAccent.copy(alpha = 0.14f),
                        border = BorderStroke(1.2.dp, GoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_payment_settlement_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isId) {
                                    "⏳ Status: PREMIUM_PENDING (${activeReceipt?.orderId ?: "Pending"})"
                                } else {
                                    "⏳ Status: PREMIUM_PENDING (${activeReceipt?.orderId ?: "Pending"})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                            Text(
                                text = if (isId) {
                                    "SKU: ${activeReceipt?.productId ?: selectedPlan.productId} • Pembayaran Anda sedang menunggu pelunasan di Google Play (e-Wallet / Indomaret / Alfamart / Virtual Account). Fitur PRO belum diaktifkan sebelum konfirmasi lunas diterima."
                                } else {
                                    "SKU: ${activeReceipt?.productId ?: selectedPlan.productId} • Your payment is awaiting settlement in Google Play. PRO features will activate automatically once payment is confirmed."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onSettlePendingPayment != null) {
                                Button(
                                    onClick = onSettlePendingPayment,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settle_pending_payment_button")
                                ) {
                                    Text(
                                        text = if (isId) "Konfirmasi Lunas & Aktifkan PRO Sekarang" else "Confirm Settlement & Activate PRO Now",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // SCREENS J & K: Restore Purchases & Manage in Google Play
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onRestorePurchase,
                        modifier = Modifier.testTag("restore_purchase_button")
                    ) {
                        Text(
                            text = AppLocalization.get("btn_restore_purchase", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = {
                            val playUrl = if (!subscription.productId.isBlank() && !subscription.isLifetimePro) {
                                "https://play.google.com/store/account/subscriptions?sku=${subscription.productId}&package=${context.packageName}"
                            } else {
                                "https://play.google.com/store/account/subscriptions"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Ignore if browser is unavailable
                            }
                        },
                        modifier = Modifier.testTag("manage_play_store_subscriptions")
                    ) {
                        Text(
                            text = AppLocalization.get("btn_manage_subscription", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // SCREENS L & M: Expandable Account Status, Renewal/Expiration Info & QA Controls
                Surface(
                    onClick = { showBillingInspector = !showBillingInspector },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_billing_inspector_button")
                ) {
                    Text(
                        text = if (showBillingInspector) {
                            if (isId) "▲ Sembunyikan Status Akun PRO, Pembaruan & Uji Billing" else "▲ Hide Account Status, Renewal & Billing Controls"
                        } else {
                            if (isId) "▼ Status Akun (${entitlementState.name}), Masa Aktif & Uji Billing" else "▼ Account Status (${entitlementState.name}), Renewal & Billing Controls"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }

                if (showBillingInspector) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.8.dp, BcaCobaltBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("billing_receipt_inspector_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isId) "Status Lisensi Akun & Informasi Perpanjangan" else "Premium Account Status & Renewal Information",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BcaRoyalBlue
                            )
                            Text(
                                text = "• Entitlement State: ${entitlementState.name} | Tier: ${subscription.statusTier}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Google Play Billing 7.1.1: " + if (isPlayStoreConnected) {
                                    "Connected to Play Store"
                                } else {
                                    "Offline / Pre-Play-Console Signed Vault"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Active SKU: ${subscription.productId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (activeReceipt != null) {
                                Text(
                                    text = "• Order ID: ${activeReceipt.orderId} (${activeReceipt.settlementState.name})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                val expLabel = if (activeReceipt.expiryTimeMillis == 0L) {
                                    if (isId) "Aktif Selamanya (Sekali Bayar / Tanpa Kedaluwarsa)" else "Permanent Lifetime (Never Expires)"
                                } else {
                                    val formattedExp = dateFmt.format(Date(activeReceipt.expiryTimeMillis))
                                    if (activeReceipt.autoRenewing) {
                                        if (isId) "Perpanjangan otomatis pada $formattedExp" else "Auto-renews on $formattedExp"
                                    } else {
                                        if (isId) "Berakhir pada $formattedExp" else "Expires on $formattedExp"
                                    }
                                }
                                Text(
                                    text = "• Pembaruan/Masa Aktif: $expLabel",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• HMAC-SHA256 Signature: ${activeReceipt.hmacSignature.take(16)}...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!isPro && onSimulatePendingRetailPayment != null) {
                                    OutlinedButton(
                                        onClick = { onSimulatePendingRetailPayment(selectedPlan) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("simulate_pending_payment_button")
                                    ) {
                                        Text(
                                            text = if (isId) "Uji Bayar Pending (VA/Retail)" else "Test Pending Payment",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                if ((isPro || isTrial) && onDowngradeToFreeForever != null) {
                                    OutlinedButton(
                                        onClick = onDowngradeToFreeForever,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.8.dp, CoralExpense.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("downgrade_to_free_forever_button")
                                    ) {
                                        Text(
                                            text = if (isId) "Uji Paket Gratis Selamanya (Tanpa Hapus Data)" else "Switch to Free Forever Tier (Safe)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            color = CoralExpense,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            if (isPro && (onSimulateRefundRevocation != null || onSimulateSubscriptionExpiration != null)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (onSimulateSubscriptionExpiration != null) {
                                        OutlinedButton(
                                            onClick = onSimulateSubscriptionExpiration,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("simulate_subscription_expired_button")
                                        ) {
                                            Text(
                                                text = if (isId) "Uji Kedaluwarsa (EXPIRED)" else "Test Expiration",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    if (onSimulateRefundRevocation != null) {
                                        OutlinedButton(
                                            onClick = onSimulateRefundRevocation,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("simulate_refund_revoked_button")
                                        ) {
                                            Text(
                                                text = if (isId) "Uji Refund (REVOKED)" else "Test Refund/Revoke",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // SCREEN F: PURCHASE CONFIRMATION DIALOG (Pre-checkout transparent summary)
    if (showConfirmCheckoutModal) {
        val selectedPriceDisplay = when (selectedPlan) {
            ProPlanTier.ANNUAL -> "$annualPriceStr / ${if (isId) "Tahun" else "Year"}"
            ProPlanTier.LIFETIME -> "$lifetimePriceStr (${if (isId) "Sekali Bayar" else "One-Time"})"
            ProPlanTier.MONTHLY -> "$monthlyPriceStr / ${if (isId) "Bulan" else "Month"}"
        }
        val selectedPlanTitle = when (selectedPlan) {
            ProPlanTier.ANNUAL -> if (isId) "Tahunan (12 Bulan)" else "Annual (12 Months)"
            ProPlanTier.LIFETIME -> if (isId) "Lisensi Seumur Hidup (Lifetime)" else "Lifetime License (One-Time)"
            ProPlanTier.MONTHLY -> if (isId) "Bulanan (1 Bulan)" else "Monthly (1 Month)"
        }
        val confirmPolicyText = when (selectedPlan) {
            ProPlanTier.LIFETIME -> if (isId) {
                "Pembelian Sekali Bayar ($lifetimePriceStr) untuk akses ArusKas PRO selamanya. Tidak ada perpanjangan otomatis atau tagihan berulang."
            } else {
                "One-time payment ($lifetimePriceStr) for permanent ArusKas PRO access. Never auto-renews and has zero recurring charges."
            }
            ProPlanTier.ANNUAL -> if (isId) {
                "Langganan Tahunan ($annualPriceStr/tahun) diperpanjang otomatis setiap 12 bulan melalui Google Play kecuali dibatalkan minimal 24 jam sebelum periode berakhir."
            } else {
                "Annual subscription ($annualPriceStr/year) auto-renews every 12 months via Google Play unless canceled at least 24 hours before renewal."
            }
            ProPlanTier.MONTHLY -> if (isId) {
                "Langganan Bulanan ($monthlyPriceStr/bulan) diperpanjang otomatis setiap bulan melalui Google Play. Batalkan kapan saja di Google Play Store."
            } else {
                "Monthly subscription ($monthlyPriceStr/month) auto-renews monthly via Google Play. Cancel anytime in Google Play Store."
            }
        }
        AlertDialog(
            onDismissRequest = { showConfirmCheckoutModal = false },
            title = {
                Text(
                    text = if (isId) "Konfirmasi Pembelian ArusKas PRO" else "Confirm ArusKas PRO Purchase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isId) "Paket Dipilih: $selectedPlanTitle" else "Selected Plan: $selectedPlanTitle",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isId) "Harga & Siklus: $selectedPriceDisplay (SKU: ${selectedPlan.productId})" else "Price & Billing Cycle: $selectedPriceDisplay (SKU: ${selectedPlan.productId})",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = confirmPolicyText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCheckoutModal = false
                        justCompletedPurchase = true
                        onSubscribeProWithPlan(selectedPlan)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF241800)),
                    modifier = Modifier.testTag("confirm_checkout_modal_proceed_button")
                ) {
                    Text(
                        text = if (isId) "Lanjutkan ke Google Play" else "Continue to Google Play",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCheckoutModal = false }) {
                    Text(if (isId) "Kembali" else "Back")
                }
            }
        )
    }
}

@Composable
private fun PlanOptionCard(
    tier: ProPlanTier,
    isSelected: Boolean,
    badgeText: String,
    badgeColor: Color,
    title: String,
    priceHeadline: String,
    subtext: String,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) badgeColor.copy(alpha = 0.13f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_option_${tier.planCode}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = badgeColor),
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.22f),
                            border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = priceHeadline,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FreeVsProComparisonRow(
    feature: String,
    freeVal: String,
    proVal: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Free: $freeVal",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "PRO: $proVal",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
            )
        }
    }
}
