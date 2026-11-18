package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLanguage
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.CategoryLimitConfig
import com.example.data.EmergencyFundConfig
import com.example.data.MandatoryBillItem
import com.example.data.ProFeatureTrigger
import com.example.data.SavingsGoal
import com.example.data.SubscriptionConfig
import com.example.data.TransactionRecord
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.example.data.offlineinsight.CommunicationStylePreference
import com.example.data.offlineinsight.InsightCategory
import com.example.data.offlineinsight.InsightPersona
import com.example.data.offlineinsight.OfflineInsightDatabaseHelper
import com.example.data.offlineinsight.OnboardingEngine
import com.example.data.offlineinsight.ResolvedFinancialInsight
import com.example.data.offlineinsight.ResponseEngine
import com.example.data.offlineinsight.UserCommunicationPreferences
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue

/**
 * KARTU ASISTEN KEUANGAN PINTAR OFFLINE (LOCAL RULE-BASED FINANCIAL INSIGHT ENGINE)
 *
 * Posisi Wajib di Dashboard:
 * 1. Kotak Total Saldo (`TotalSaldoDirectWalletsCard`)
 * 2. DI SINI (`SmartOfflineInsightCard`)
 * 3. Kotak Categories / Spend Limit (`UpgradedCategoryGroupSection`)
 *
 * Bekerja 100% secara lokal & offline menggunakan SQLite 10.000 kalimat unik dan 4 mesin analisis.
 */
@Composable
fun SmartOfflineInsightCard(
    wallets: List<Wallet>,
    transactions: List<TransactionRecord>,
    goals: List<SavingsGoal>,
    categoryLimits: Map<String, CategoryLimitConfig>,
    mandatoryBills: List<MandatoryBillItem>,
    emergencyFund: EmergencyFundConfig,
    userProfile: UserFinancialProfile,
    language: AppLanguage,
    isDarkMode: Boolean,
    subscription: SubscriptionConfig = SubscriptionConfig(),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    onRecordInsightRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN

    val dbHelper = remember(context) { OfflineInsightDatabaseHelper.getInstance(context) }
    val onboardingEngine = remember(dbHelper) { OnboardingEngine(dbHelper) }
    val responseEngine = remember(dbHelper) { ResponseEngine(dbHelper, onboardingEngine) }

    var commPrefs by remember(userProfile, subscription.hasProAccess) {
        val loaded = onboardingEngine.loadPreferences()
        val synced = onboardingEngine.syncFromUserFinancialProfile(userProfile, loaded)
        // If user is on Free Forever tier and previously selected a PRO-only manual persona, gracefully fallback to SI_BESTIE
        val safePersona = if (ArusKasFeatureGateEvaluator.isPersonaUnlocked(synced.manualPersona.name, subscription)) {
            synced.manualPersona
        } else {
            InsightPersona.SI_BESTIE
        }
        mutableStateOf(synced.copy(manualPersona = safePersona))
    }

    var variationOffset by remember { mutableIntStateOf(0) }
    var showFullAnalysisDialog by remember { mutableStateOf(false) }
    var showPersonaSettingsDialog by remember { mutableStateOf(false) }

    // Otomatis menghitung ulang ketika ada perubahan pada saldo, transaksi, tabungan, limit, tagihan, atau preferensi
    val currentInsight: ResolvedFinancialInsight = remember(
        wallets,
        transactions,
        goals,
        categoryLimits,
        mandatoryBills,
        emergencyFund,
        userProfile,
        commPrefs,
        variationOffset
    ) {
        responseEngine.generateInsight(
            wallets = wallets,
            transactions = transactions,
            goals = goals,
            categoryLimits = categoryLimits,
            mandatoryBills = mandatoryBills,
            emergencyFund = emergencyFund,
            userProfile = userProfile,
            preferencesOverride = commPrefs,
            variationOffset = variationOffset,
            recordUsageInDb = true
        )
    }

    val categoryAccent = remember(currentInsight.category, currentInsight.priorityLevel) {
        when {
            currentInsight.priorityLevel <= 2 || currentInsight.category == InsightCategory.PERINGATAN -> CoralExpense
            currentInsight.category == InsightCategory.APRESIASI -> EmeraldPrimary
            currentInsight.category == InsightCategory.TABUNGAN -> GoldAccent
            currentInsight.category == InsightCategory.TAGIHAN -> Color(0xFFD97706)
            currentInsight.category == InsightCategory.KEBIASAAN -> BcaCobaltBlue
            else -> BcaRoyalBlue
        }
    }

    val categoryIcon = remember(currentInsight.category) {
        when (currentInsight.category) {
            InsightCategory.PERINGATAN -> Icons.Default.WarningAmber
            InsightCategory.APRESIASI -> Icons.Default.CheckCircle
            InsightCategory.TABUNGAN -> Icons.Default.Savings
            InsightCategory.KEBIASAAN -> Icons.Default.Psychology
            InsightCategory.STRATEGI -> Icons.Default.Lightbulb
            else -> Icons.Default.Analytics
        }
    }

    val cardBgBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF10233E).copy(alpha = 0.96f),
                Color(0xFF0B182B).copy(alpha = 0.96f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF3F8FF)
            )
        )
    }

    val primaryTextColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F2942)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)

    if (showFullAnalysisDialog) {
        FullOfflineAnalysisDialog(
            insight = currentInsight,
            commPrefs = commPrefs,
            language = language,
            onOpenPersonaSettings = {
                showFullAnalysisDialog = false
                showPersonaSettingsDialog = true
            },
            onNextInsight = { variationOffset++ },
            onDismiss = { showFullAnalysisDialog = false }
        )
    }

    if (showPersonaSettingsDialog) {
        InsightPersonaAndPreferencesDialog(
            initialPrefs = commPrefs,
            language = language,
            subscription = subscription,
            onOpenPaywallWithTrigger = { trigger ->
                showPersonaSettingsDialog = false
                onOpenPaywallWithTrigger(trigger)
            },
            onSavePreferences = { updatedPrefs ->
                onboardingEngine.savePreferences(updatedPrefs)
                commPrefs = updatedPrefs
                variationOffset++
                showPersonaSettingsDialog = false
            },
            onDismiss = { showPersonaSettingsDialog = false }
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, categoryAccent.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_offline_insight_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgBrush)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BARIS ATAS: Indikator Jenis Insight + Chip Karakter Komunikasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = categoryAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, categoryAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("insight_category_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = currentInsight.category.displayName(language),
                            tint = categoryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = currentInsight.badge(language),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = { showPersonaSettingsDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("insight_persona_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${currentInsight.persona.badgeEmoji} ${currentInsight.persona.displayName(language)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) SkyBlue else BcaRoyalBlue
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = if (isId) "Ganti Gaya Bahasa" else "Communication Style",
                            tint = if (isDarkMode) SkyBlue else BcaRoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ISI TENGAH: Satu Kalimat Analisis yang Jelas & Mudah Dibaca
            AnimatedContent(
                targetState = currentInsight.message(language),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "OfflineInsightMessageTransition"
            ) { messageText ->
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.5.sp,
                        lineHeight = 23.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = primaryTextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (ArusKasFeatureGateEvaluator.canOpenDeepInsightAnalysis(subscription)) {
                                showFullAnalysisDialog = true
                            } else {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.DEEP_OFFLINE_ANALYSIS)
                            }
                        }
                        .testTag("offline_insight_main_sentence")
                )
            }

            // BARIS BAWAH: Tombol Penjelasan Lengkap & Ganti Saran yang Jelas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        if (ArusKasFeatureGateEvaluator.canOpenDeepInsightAnalysis(subscription)) {
                            showFullAnalysisDialog = true
                        } else {
                            onOpenPaywallWithTrigger(ProFeatureTrigger.DEEP_OFFLINE_ANALYSIS)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else BcaRoyalBlue.copy(alpha = 0.08f),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("open_full_insight_analysis_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isDarkMode) SkyBlue else BcaRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (subscription.hasProAccess) {
                                if (isId) "Lihat Rincian" else "Full Details"
                            } else {
                                if (isId) "Rincian (PRO)" else "Details (PRO)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) SkyBlue else BcaRoyalBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val remainingFreeRefreshes = subscription.remainingFreeInsightRefreshesToday()
                Surface(
                    onClick = {
                        if (ArusKasFeatureGateEvaluator.canRefreshInsightToday(subscription)) {
                            variationOffset++
                            onRecordInsightRefresh()
                        } else {
                            onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_INSIGHT_REFRESH)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = categoryAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, categoryAccent.copy(alpha = 0.38f)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("next_offline_insight_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isId) "Ganti Saran" else "Next Tip",
                            tint = categoryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (subscription.hasProAccess) {
                                if (isId) "Ganti Saran" else "Next Tip"
                            } else {
                                if (isId) "Saran Lain ($remainingFreeRefreshes/3)" else "Next Tip ($remainingFreeRefreshes/3)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = categoryAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Penjelasan & Analisis Lengkap (Fakta Angka, Indikasi Pola 30 Hari, dan Saran Tindakan Praktis).
 */
@Composable
fun FullOfflineAnalysisDialog(
    insight: ResolvedFinancialInsight,
    commPrefs: UserCommunicationPreferences,
    language: AppLanguage,
    onOpenPersonaSettings: () -> Unit,
    onNextInsight: () -> Unit,
    onDismiss: () -> Unit
) {
    val isId = language == AppLanguage.INDONESIAN

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .testTag("full_offline_analysis_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isId) "Analisis Asisten Keuangan Offline" else "Offline Financial Assistant Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BcaRoyalBlue
                        )
                        Text(
                            text = "${insight.persona.badgeEmoji} ${insight.persona.displayName(language)} • ${insight.badge(language)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // 1. Insight Utama
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = insight.message(language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isId) {
                                "Saran Tindakan: ${insight.action(language)}"
                            } else {
                                "Recommended Action: ${insight.action(language)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                // 2. Fakta Angka Aktual (Factual Observations)
                Text(
                    text = if (isId) "📊 Fakta Data Tercatat" else "📊 Recorded Financial Facts",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                insight.facts(language).forEach { fact ->
                    Text(
                        text = "• $fact",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3. Indikasi & Analisis Kebiasaan 30 Hari
                Text(
                    text = if (isId) "🔍 Indikasi & Pola Kebiasaan 30 Hari" else "🔍 30-Day Habit Indications",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val indications = insight.indications(language)
                if (indications.isEmpty()) {
                    Text(
                        text = if (isId) {
                            "• Pola transaksi berjalan stabil tanpa anomali signifikan."
                        } else {
                            "• Transaction patterns are steady with no significant anomalies."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    indications.forEach { ind ->
                        Text(
                            text = "• $ind",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 4. Tombol Aksi Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenPersonaSettings,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isId) "Ubah Gaya Bicara" else "Change Persona",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onNextInsight,
                        colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isId) "Ganti Kalimat" else "Next Insight",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pengaturan Personalisasi Komunikasi & 12 Karakter Asisten Keuangan Offline.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightPersonaAndPreferencesDialog(
    initialPrefs: UserCommunicationPreferences,
    language: AppLanguage,
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    onSavePreferences: (UserCommunicationPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    val isId = language == AppLanguage.INDONESIAN
    var isManualMode by remember { mutableStateOf(initialPrefs.isManualPersonaMode) }
    var selectedPersona by remember { mutableStateOf(initialPrefs.manualPersona) }
    var selectedStyle by remember { mutableStateOf(initialPrefs.stylePreference) }
    var allowRoast by remember { mutableStateOf(initialPrefs.allowRoastingAndSarcasm) }
    var strictness by remember { mutableStateOf(initialPrefs.strictnessLevel) }
    var sentenceLength by remember { mutableStateOf(initialPrefs.sentenceLength) }
    var focusPref by remember { mutableStateOf(initialPrefs.focusPreference) }
    var perceivedState by remember { mutableStateOf(initialPrefs.selfPerceivedCondition) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("insight_persona_preferences_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isId) "Gaya Komunikasi & 12 Karakter" else "Communication Style & 12 Personas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BcaRoyalBlue
                        )
                        Text(
                            text = if (isId) {
                                "Sesuaikan kepribadian teman keuangan offline Anda (10.000 kalimat lokal)"
                            } else {
                                "Customize your offline financial companion (10,000 local phrases)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Mode Pemilihan: Otomatis vs Manual 12 Persona
                Text(
                    text = if (isId) "1. Mode Karakter Komunikasi" else "1. Persona Selection Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isManualMode,
                        onClick = { isManualMode = false },
                        label = {
                            Text(if (isId) "✨ Otomatis Sesuai Gaya" else "✨ Auto by Style")
                        },
                        modifier = Modifier.testTag("persona_mode_auto_chip")
                    )
                    FilterChip(
                        selected = isManualMode,
                        onClick = { isManualMode = true },
                        label = {
                            Text(if (isId) "🎭 Pilih Manual (12 Karakter)" else "🎭 Manual (12 Personas)")
                        },
                        modifier = Modifier.testTag("persona_mode_manual_chip")
                    )
                }

                if (isManualMode) {
                    Text(
                        text = if (subscription.hasProAccess) {
                            if (isId) "Pilih 1 dari 12 Karakter (Semua Terbuka):" else "Choose 1 of 12 Personas (All Unlocked):"
                        } else {
                            if (isId) "Pilih Karakter (3 Gratis Selamanya • 9 Karakter PRO):" else "Choose Persona (3 Free Forever • 9 PRO Personas):"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InsightPersona.entries.forEach { persona ->
                            val isUnlocked = ArusKasFeatureGateEvaluator.isPersonaUnlocked(persona.name, subscription)
                            FilterChip(
                                selected = selectedPersona == persona,
                                onClick = {
                                    if (isUnlocked) {
                                        selectedPersona = persona
                                    } else {
                                        onOpenPaywallWithTrigger(ProFeatureTrigger.ALL_12_PERSONAS)
                                    }
                                },
                                label = {
                                    val proTag = if (isUnlocked) "" else " 🔒 PRO"
                                    Text(
                                        text = "${persona.badgeEmoji} ${persona.displayName(language)}$proTag",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("select_persona_${persona.name}")
                            )
                        }
                    }
                    Text(
                        text = selectedPersona.description(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = BcaRoyalBlue,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = if (isId) "Pilih Gaya Komunikasi Utama:" else "Select Primary Communication Style:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CommunicationStylePreference.entries.forEach { stylePref ->
                            FilterChip(
                                selected = selectedStyle == stylePref,
                                onClick = { selectedStyle = stylePref },
                                label = {
                                    Text(
                                        text = if (isId) stylePref.labelId else stylePref.labelEn,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("select_comm_style_${stylePref.name}")
                            )
                        }
                    }
                }

                // Toggle Roasting & Sarkasme
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isId) "Izinkan Humor, Roasting & Sarkasme" else "Allow Humor, Roasting & Sarcasm",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isId) {
                                "Matikan jika Anda hanya ingin bahasa serius, sopan, atau empatik"
                            } else {
                                "Turn off if you prefer strictly serious, polite, or empathetic tone"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = allowRoast,
                        onCheckedChange = { allowRoast = it },
                        modifier = Modifier.testTag("toggle_allow_roasting_switch")
                    )
                }

                // Tingkat Ketegasan
                Text(
                    text = if (isId) "2. Tingkat Ketegasan" else "2. Strictness Level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "GENTLE" to (if (isId) "Lembut" else "Gentle"),
                        "BALANCED" to (if (isId) "Seimbang" else "Balanced"),
                        "STRICT" to (if (isId) "Tegas" else "Strict")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = strictness == key,
                            onClick = { strictness = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Panjang Kalimat
                Text(
                    text = if (isId) "3. Panjang Kalimat Insight" else "3. Sentence Length",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "SHORT" to (if (isId) "Singkat & Padat" else "Short & Concise"),
                        "DETAILED" to (if (isId) "Penjelasan Lengkap" else "Detailed"),
                        "ANY" to (if (isId) "Otomatis Bervariasi" else "Auto Varied")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = sentenceLength == key,
                            onClick = { sentenceLength = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Fokus Penyampaian: Motivasi vs Solusi Langsung
                Text(
                    text = if (isId) "4. Fokus Penyampaian" else "4. Insight Focus",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "MOTIVATION" to (if (isId) "Motivasi" else "Motivation"),
                        "SOLUTION" to (if (isId) "Solusi Langsung" else "Direct Solution"),
                        "BALANCED" to (if (isId) "Seimbang" else "Balanced")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = focusPref == key,
                            onClick = { focusPref = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Kondisi Keuangan Menurut Pengguna (Opsional)
                Text(
                    text = if (isId) "5. Kondisi Keuangan Menurut Anda (Opsional)" else "5. Self-Perceived Financial State (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "STABLE" to (if (isId) "Stabil" else "Stable"),
                        "ENOUGH_HARD_TO_SAVE" to (if (isId) "Cukup, sulit menabung" else "Hard to save"),
                        "OFTEN_RUNS_OUT" to (if (isId) "Sering kehabisan uang" else "Often runs out"),
                        "IRREGULAR_INCOME" to (if (isId) "Pemasukan tidak tetap" else "Irregular income"),
                        "MANY_BILLS_DEBT" to (if (isId) "Banyak cicilan/tagihan" else "Many bills/debt"),
                        "RECOVERING" to (if (isId) "Sedang memperbaiki" else "Recovering")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = perceivedState == key,
                            onClick = { perceivedState = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isId) "Batal" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSavePreferences(
                                initialPrefs.copy(
                                    isManualPersonaMode = isManualMode,
                                    manualPersona = selectedPersona,
                                    stylePreference = selectedStyle,
                                    allowRoastingAndSarcasm = allowRoast,
                                    strictnessLevel = strictness,
                                    sentenceLength = sentenceLength,
                                    focusPreference = focusPref,
                                    selfPerceivedCondition = perceivedState
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue),
                        modifier = Modifier.testTag("save_persona_preferences_button")
                    ) {
                        Text(
                            text = if (isId) "Simpan Preferensi" else "Save Preferences",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
