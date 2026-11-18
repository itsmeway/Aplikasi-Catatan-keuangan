package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VisibilityOff
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaElectricCyan
import com.example.ui.theme.BcaFrostWhite
import com.example.ui.theme.BcaIceTint
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.BcaTextNavy
import com.example.ui.theme.BcaTextSlate
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AppLocalization
import com.example.data.AppSettings
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.CurrencyFormatOption
import com.example.data.ProFeatureTrigger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsBottomSheet(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit = {},
    onGenerateBackupJson: () -> String = { "" },
    onRestoreValidatedBackup: (com.example.data.ParsedArusDompetBackup, Boolean) -> Unit = { _, _ -> },
    onResetAllFinancialData: () -> Unit = {},
    onOpenUserGuide: () -> Unit = {},
    onOpenFinancialProfileWizard: () -> Unit = {},
    onOpenWalletsSheet: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = { onOpenPaywall() },
    onChangeCountryAndCurrency: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentSettings by remember(settings) { mutableStateOf(settings) }
    val lang = currentSettings.language
    val isId = lang == AppLanguage.INDONESIAN
    var showOfflinePersonaDialog by remember { mutableStateOf(false) }
    var showPinConfigDialog by remember { mutableStateOf(false) }
    var showDataStorageDialog by remember { mutableStateOf(false) }
    var showHelpAndFeedbackDialog by remember { mutableStateOf(false) }
    var helpDialogInitialTab by remember { mutableStateOf(0) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var selectedRegionTab by remember { mutableStateOf("ALL") }
    val appContext = androidx.compose.ui.platform.LocalContext.current

    val activeCountryProfile = remember(currentSettings.countryCode) {
        com.example.data.WorldCountryCurrencyRegistry.byCountryCode(currentSettings.countryCode)
    }

    if (showPinConfigDialog) {
        PinConfigurationDialog(
            language = lang,
            isCurrentlyEnabled = currentSettings.isAppLockEnabled,
            onPinSavedAndEnabled = {
                val updated = currentSettings.copy(isAppLockEnabled = true)
                currentSettings = updated
                onUpdateSettings(updated)
                showPinConfigDialog = false
                onShowMessage(
                    if (isId) "PIN Keamanan (SHA-256 + Salt) berhasil disimpan & diaktifkan!"
                    else "Security PIN (SHA-256 + Salt) saved & enabled!"
                )
            },
            onPinDisabled = {
                val updated = currentSettings.copy(isAppLockEnabled = false)
                currentSettings = updated
                onUpdateSettings(updated)
                showPinConfigDialog = false
                onShowMessage(
                    if (isId) "Kunci PIN aplikasi berhasil dinonaktifkan."
                    else "App PIN lock has been disabled."
                )
            },
            onDismiss = { showPinConfigDialog = false }
        )
    }

    if (showDataStorageDialog) {
        DataStorageAndBackupDialog(
            language = lang,
            subscription = currentSettings.subscription,
            onOpenPaywallWithTrigger = { trigger ->
                showDataStorageDialog = false
                onOpenPaywallWithTrigger(trigger)
            },
            onGenerateBackupJson = onGenerateBackupJson,
            onRestoreValidatedBackup = onRestoreValidatedBackup,
            onExportPdfReport = onExportPdf,
            onExportCsvReport = onExportCsv,
            onResetAllFinancialData = onResetAllFinancialData,
            onShowMessage = onShowMessage,
            onDismiss = { showDataStorageDialog = false }
        )
    }

    if (showHelpAndFeedbackDialog) {
        HelpAndFeedbackHubDialog(
            language = lang,
            initialTab = helpDialogInitialTab,
            onOpenUserGuide = onOpenUserGuide,
            onShowMessage = onShowMessage,
            onDismiss = { showHelpAndFeedbackDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        ComprehensivePrivacyPolicyDialog(
            language = lang,
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    if (showOfflinePersonaDialog) {
        val dbHelper = remember(appContext) { com.example.data.offlineinsight.OfflineInsightDatabaseHelper.getInstance(appContext) }
        val onboardingEngine = remember(dbHelper) { com.example.data.offlineinsight.OnboardingEngine(dbHelper) }
        val currentPrefs = remember { onboardingEngine.loadPreferences() }
        InsightPersonaAndPreferencesDialog(
            initialPrefs = currentPrefs,
            language = lang,
            subscription = currentSettings.subscription,
            onOpenPaywallWithTrigger = { trigger ->
                showOfflinePersonaDialog = false
                onOpenPaywallWithTrigger(trigger)
            },
            onSavePreferences = { updatedPrefs ->
                onboardingEngine.savePreferences(updatedPrefs)
                showOfflinePersonaDialog = false
                onShowMessage(
                    if (isId) "Gaya komunikasi asisten offline berhasil disimpan!"
                    else "Offline assistant communication preferences saved!"
                )
            },
            onDismiss = { showOfflinePersonaDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppLocalization.get("settings_title", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AppLocalization.get("settings_subtitle", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppLocalization.get("btn_close", lang)
                    )
                }
            }

            // PRO SUBSCRIPTION STATUS CARD
            val sub = currentSettings.subscription
            val isPro = sub.isProActive
            val isTrial = sub.isTrialActive
            val remainingDays = sub.remainingTrialDays

            val subBadgeText = when {
                sub.isLifetimePro -> AppLocalization.get("pro_lifetime_chip", lang)
                isPro -> AppLocalization.get("pro_active_chip", lang)
                isTrial -> String.format(AppLocalization.get("pro_trial_chip", lang), remainingDays)
                else -> AppLocalization.get("pro_free_forever_chip", lang)
            }
            val subBadgeColor = if (isPro || isTrial) GoldAccent else EmeraldPrimary

            Card(
                onClick = { onOpenPaywallWithTrigger(ProFeatureTrigger.GENERAL_UPGRADE) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF141916)
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.3.dp, GoldAccent.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_pro_subscription_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = AppLocalization.get("sec_subscription_title", lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = subBadgeColor.copy(alpha = 0.22f),
                                    border = BorderStroke(0.8.dp, subBadgeColor.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = subBadgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = subBadgeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = AppLocalization.get("sec_subscription_desc", lang),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = Color(0xFFD6E2EE)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Subscription",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Quick simulation controls for testing trial expiry & reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val expiredSub = sub.copy(
                            isProActive = false,
                            planTier = "FREE",
                            hasDismissedExpiredTrialNotice = false,
                            trialStartDateMillis = System.currentTimeMillis() - 31L * 24 * 60 * 60 * 1000L
                        )
                        val updated = currentSettings.copy(subscription = expiredSub)
                        currentSettings = updated
                        onUpdateSettings(updated)
                        onShowMessage(
                            if (isId) "Simulasi: Masa uji coba selesai. Beralih ke Versi Gratis Selamanya (Data tetap 100% aman)!"
                            else "Simulated: Free trial ended. Switched to Free Forever Tier (All data 100% safe)!"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                ) {
                    Text(
                        if (isId) "🧪 Versi Gratis" else "🧪 Free Tier",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF8A80)
                    )
                }
                OutlinedButton(
                    onClick = {
                        val resetSub = sub.copy(
                            isProActive = false,
                            planTier = "TRIAL",
                            hasDismissedExpiredTrialNotice = false,
                            trialStartDateMillis = System.currentTimeMillis()
                        )
                        val updated = currentSettings.copy(subscription = resetSub)
                        currentSettings = updated
                        onUpdateSettings(updated)
                        onShowMessage(
                            if (isId) "Masa uji coba berhasil direset ke 30 hari penuh!"
                            else "Free trial reset to full 30 days!"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                ) {
                    Text(
                        if (isId) "🔄 Reset 30 Hari" else "🔄 Reset 30 Days",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }

            // SECTION 1: LANGUAGE OPTIONS (INTERNATIONAL ENGLISH DEFAULT, INDONESIAN OPTIONAL)
            SettingsSectionCard(
                title = AppLocalization.get("sec_language", lang),
                description = AppLocalization.get("sec_language_desc", lang),
                icon = Icons.Default.Language,
                accentColor = EmeraldPrimary
            ) {
                AppLanguage.values().forEach { languageOption ->
                    val isSelected = currentSettings.language == languageOption
                    Surface(
                        onClick = {
                            com.example.data.WorldCountryCurrencyRegistry.activeLanguage = languageOption
                            val updated = currentSettings.copy(language = languageOption)
                            currentSettings = updated
                            onUpdateSettings(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lang_option_${languageOption.code}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = languageOption.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = languageOption.regionLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    com.example.data.WorldCountryCurrencyRegistry.activeLanguage = languageOption
                                    val updated = currentSettings.copy(language = languageOption)
                                    currentSettings = updated
                                    onUpdateSettings(updated)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = EmeraldPrimary
                                )
                            )
                        }
                    }
                }
            }

            // SECTION 1.5: GLOBAL COUNTRY & CURRENCY EXCHANGE RATE SETTINGS
            SettingsSectionCard(
                title = if (isId) "Negara & Kurs Mata Uang Dunia" else "Country & Currency Exchange Rate",
                description = if (isId) {
                    "Pilih negara (Asia, Amerika, Eropa, Timteng). Seluruh format mata uang, kurs, daftar Bank & E-Wallet otomatis menyesuaikan negara pilihan:"
                } else {
                    "Select your country across Americas, Asia-Pacific, Europe & Middle East. Currency symbols, exchange rates, Banks & Digital Wallets adapt automatically:"
                },
                icon = Icons.Default.Language,
                accentColor = SkyBlue
            ) {
                // Active Country & Exchange Rate Summary Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SkyBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeCountryProfile.flagEmoji} ${activeCountryProfile.displayCountryName(lang)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = SkyBlue
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "${activeCountryProfile.currencyCode} (${activeCountryProfile.currencySymbol})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        val sampleFormatted = com.example.data.WorldCountryCurrencyRegistry.formatCurrency(
                            activeCountryProfile.defaultMonthlyIncome,
                            activeCountryProfile.countryCode
                        )
                        val banksPreview = activeCountryProfile.starterWallets.take(4).joinToString(", ") { it.name }
                        Text(
                            text = if (isId) {
                                "Kurs Referensi: 1 USD = ${activeCountryProfile.ratePerUsd} ${activeCountryProfile.currencyCode} • Contoh Format: $sampleFormatted"
                            } else {
                                "Reference Rate: 1 USD = ${activeCountryProfile.ratePerUsd} ${activeCountryProfile.currencyCode} • Format Preview: $sampleFormatted"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isId) "Bank & Dompet Negara: $banksPreview" else "Country Banks & Wallets: $banksPreview",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Region Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to (if (isId) "Semua" else "All"),
                        "Americas" to (if (isId) "Amerika" else "Americas"),
                        "Asia-Pacific" to "Asia-Pacific",
                        "Europe & Middle East" to (if (isId) "Eropa" else "Europe")
                    ).forEach { (regKey, regLabel) ->
                        val isRegSel = selectedRegionTab == regKey
                        Surface(
                            onClick = { selectedRegionTab = regKey },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isRegSel) SkyBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isRegSel) 1.3.dp else 0.8.dp,
                                color = if (isRegSel) SkyBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = regLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = if (isRegSel) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isRegSel) SkyBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                val displayedCountries = remember(selectedRegionTab) {
                    if (selectedRegionTab == "ALL") {
                        com.example.data.WorldCountryCurrencyRegistry.allCountries
                    } else {
                        com.example.data.WorldCountryCurrencyRegistry.allCountries.filter { it.regionEn == selectedRegionTab }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    displayedCountries.forEach { country ->
                        val isSelectedCountry = currentSettings.countryCode.equals(country.countryCode, ignoreCase = true)
                        Surface(
                            onClick = {
                                val updated = currentSettings.copy(
                                    countryCode = country.countryCode,
                                    currencyCode = country.currencyCode
                                )
                                currentSettings = updated
                                onChangeCountryAndCurrency(country.countryCode)
                            },
                            shape = RoundedCornerShape(11.dp),
                            color = if (isSelectedCountry) SkyBlue.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                width = if (isSelectedCountry) 1.4.dp else 0.8.dp,
                                color = if (isSelectedCountry) SkyBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_country_option_${country.countryCode}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = country.flagEmoji, fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = "${country.displayCountryName(lang)} • ${country.currencyCode} (${country.currencySymbol})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelectedCountry) SkyBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = country.starterWallets.take(3).joinToString(" • ") { it.name },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = isSelectedCountry,
                                    onClick = {
                                        val updated = currentSettings.copy(
                                            countryCode = country.countryCode,
                                            currencyCode = country.currencyCode
                                        )
                                        currentSettings = updated
                                        onChangeCountryAndCurrency(country.countryCode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1.8: HOME SCREEN QUICK TRANSACTION WIDGET
            SettingsSectionCard(
                title = if (isId) "Widget Transaksi di Beranda HP" else "Home Screen Transaction Widget",
                description = if (isId) {
                    "Masukkan nominal uang keluar atau masuk langsung dari layar beranda HP tanpa perlu masuk ke aplikasi."
                } else {
                    "Enter expense or income amounts directly from your phone home screen without opening the app."
                },
                icon = Icons.Default.AddToHomeScreen,
                accentColor = EmeraldPrimary
            ) {
                Button(
                    onClick = {
                        val widgetManager = android.appwidget.AppWidgetManager.getInstance(appContext)
                        val provider = android.content.ComponentName(
                            appContext,
                            com.example.widget.ArusKasQuickEntryWidgetReceiver::class.java
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                            widgetManager.isRequestPinAppWidgetSupported
                        ) {
                            val requested = widgetManager.requestPinAppWidget(provider, null, null)
                            if (requested) {
                                onShowMessage(
                                    if (isId) {
                                        "Konfirmasi pemasangan widget muncul di beranda HP Anda."
                                    } else {
                                        "Widget pin confirmation requested on your home screen."
                                    }
                                )
                            } else {
                                onShowMessage(
                                    if (isId) {
                                        "Tekan & tahan area kosong di beranda HP -> pilih Widget -> ArusKas."
                                    } else {
                                        "Touch & hold an empty space on your home screen -> Widgets -> ArusKas."
                                    }
                                )
                            }
                        } else {
                            onShowMessage(
                                if (isId) {
                                    "Tekan & tahan area kosong di beranda HP -> pilih Widget -> Catat Transaksi Cepat."
                                } else {
                                    "Touch & hold an empty space on your home screen -> Widgets -> Quick Transaction."
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("settings_pin_home_widget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddToHomeScreen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isId) "Pasang Widget ke Beranda HP" else "Add Widget to Home Screen",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.5.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // SECTION 2: DISPLAY & CURRENCY FORMAT
            SettingsSectionCard(
                title = AppLocalization.get("sec_appearance", lang),
                description = AppLocalization.get("sec_appearance_desc", lang),
                icon = Icons.Default.Palette,
                accentColor = GoldAccent
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isId) "Format Tampilan Nominal (${activeCountryProfile.currencyCode})" else "Currency Display Format (${activeCountryProfile.currencyCode})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isFull = currentSettings.currencyFormat == CurrencyFormatOption.FULL_RUPIAH
                        val sampleAmt = activeCountryProfile.defaultMonthlyIncome
                        Surface(
                            onClick = {
                                val updated = currentSettings.copy(currencyFormat = CurrencyFormatOption.FULL_RUPIAH)
                                currentSettings = updated
                                onUpdateSettings(updated)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFull) GoldAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isFull) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isId) "Lengkap" else "Full Precision",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFull) GoldAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = com.example.data.WorldCountryCurrencyRegistry.formatCurrency(sampleAmt, activeCountryProfile.countryCode),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isCompact = currentSettings.currencyFormat == CurrencyFormatOption.COMPACT
                        Surface(
                            onClick = {
                                val updated = currentSettings.copy(currencyFormat = CurrencyFormatOption.COMPACT)
                                currentSettings = updated
                                onUpdateSettings(updated)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCompact) GoldAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isCompact) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isId) "Ringkas" else "Compact",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompact) GoldAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = com.example.data.WorldCountryCurrencyRegistry.formatCompactCurrency(sampleAmt, activeCountryProfile.countryCode, lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Hide Balance Toggle
                SettingsToggleRow(
                    icon = Icons.Default.VisibilityOff,
                    title = AppLocalization.get("opt_hide_balance", lang),
                    description = AppLocalization.get("opt_hide_balance_desc", lang),
                    checked = currentSettings.hideNominalOnDashboard,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(hideNominalOnDashboard = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Dashboard Assistant Visibility Toggle
                SettingsToggleRow(
                    icon = Icons.Default.AutoAwesome,
                    title = if (isId) "Tampilkan Asisten Keuangan Offline di Beranda" else "Show Smart Offline Assistant on Dashboard",
                    description = if (isId) "Tampilkan kartu analisis & kalimat asisten tepat di bawah Total Saldo" else "Display the 10,000-phrase offline insight card below Total Balance",
                    checked = currentSettings.showInsightCardOnDashboard,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(showInsightCardOnDashboard = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Date & Time Format Preferences
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isId) "Format Tanggal & Waktu Transaksi" else "Transaction Date & Time Format",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "dd MMM yyyy" to "11 Nov 2026",
                            "dd/MM/yyyy" to "11/11/2026",
                            "yyyy-MM-dd" to "2026-11-11"
                        ).forEach { (pattern, sample) ->
                            val isSel = currentSettings.dateFormatPattern == pattern
                            Surface(
                                onClick = {
                                    val updated = currentSettings.copy(dateFormatPattern = pattern)
                                    currentSettings = updated
                                    onUpdateSettings(updated)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) GoldAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) GoldAccent else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("date_format_$pattern")
                            ) {
                                Text(
                                    text = sample,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) GoldAccent else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2.5: ACCESSIBILITY & DISPLAY (FONTS & NUMBERS)
            SettingsSectionCard(
                title = AppLocalization.get("sec_accessibility", lang),
                description = AppLocalization.get("sec_accessibility_desc", lang),
                icon = Icons.Default.Palette,
                accentColor = BcaRoyalBlue
            ) {
                // Font & Number Size Scaling
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppLocalization.get("opt_font_size", lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(1.0f, AppLocalization.get("opt_font_normal", lang), "100%"),
                            Triple(1.15f, AppLocalization.get("opt_font_large", lang), "115%"),
                            Triple(1.30f, AppLocalization.get("opt_font_xlarge", lang), "130%")
                        ).forEach { (scaleVal, scaleLabel, scalePct) ->
                            val isSel = kotlin.math.abs(currentSettings.fontScaleFactor - scaleVal) < 0.05f
                            Surface(
                                onClick = {
                                    val updated = currentSettings.copy(fontScaleFactor = scaleVal)
                                    currentSettings = updated
                                    onUpdateSettings(updated)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) BcaRoyalBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("font_scale_${scalePct}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = scalePct,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = scaleLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Bold Numbers Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Payments,
                    title = AppLocalization.get("opt_bold_numbers", lang),
                    description = AppLocalization.get("opt_bold_numbers_desc", lang),
                    checked = currentSettings.boldNumbers,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(boldNumbers = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // High Contrast Mode Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Security,
                    title = AppLocalization.get("opt_high_contrast", lang),
                    description = AppLocalization.get("opt_high_contrast_desc", lang),
                    checked = currentSettings.highContrastMode,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(highContrastMode = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Accent Theme Colors (ROYAL_BLUE is Free Forever; EMERALD, COBALT, VIOLET are PRO)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppLocalization.get("opt_theme_accent", lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("ROYAL_BLUE", "BCA Blue", Color(0xFF0066AE)),
                            Triple("EMERALD", "Emerald", Color(0xFF10B981)),
                            Triple("COBALT", "Navy Cobalt", Color(0xFF003882)),
                            Triple("VIOLET", "Amethyst", Color(0xFF7C3AED))
                        ).forEach { (thKey, thName, thColor) ->
                            val isSel = currentSettings.accentTheme == thKey
                            val isThemeUnlocked = ArusKasFeatureGateEvaluator.isAccentThemeUnlocked(thKey, currentSettings.subscription)
                            Surface(
                                onClick = {
                                    if (isThemeUnlocked) {
                                        val updated = currentSettings.copy(accentTheme = thKey)
                                        currentSettings = updated
                                        onUpdateSettings(updated)
                                    } else {
                                        onOpenPaywallWithTrigger(ProFeatureTrigger.PREMIUM_THEMES)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) thColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) thColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_accent_$thKey")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(thColor)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val displayThName = if (isThemeUnlocked) thName else "$thName • PRO"
                                    Text(
                                        text = displayThName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) thColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: SECURITY & PRIVACY (PIN SHA-256 + SALT, NOTIFICATION PRIVACY & PRIVACY POLICY)
            SettingsSectionCard(
                title = AppLocalization.get("sec_security", lang),
                description = AppLocalization.get("sec_security_desc", lang),
                icon = Icons.Default.Security,
                accentColor = SkyBlue
            ) {
                val hasPinSaved = remember(currentSettings.isAppLockEnabled, showPinConfigDialog) {
                    com.example.data.PinSecurityManager.hasPinConfigured(appContext)
                }
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = AppLocalization.get("opt_app_lock", lang),
                    description = if (hasPinSaved && currentSettings.isAppLockEnabled) {
                        if (isId) "PIN Aktif (Dienkripsi SHA-256 + Cryptographic Salt)" else "PIN Active (Hashed with SHA-256 + Salt)"
                    } else {
                        AppLocalization.get("opt_app_lock_desc", lang)
                    },
                    checked = currentSettings.isAppLockEnabled && hasPinSaved,
                    onCheckedChange = { checked ->
                        if (checked && !hasPinSaved) {
                            showPinConfigDialog = true
                        } else if (!checked && hasPinSaved) {
                            showPinConfigDialog = true
                        } else {
                            val updated = currentSettings.copy(isAppLockEnabled = checked)
                            currentSettings = updated
                            onUpdateSettings(updated)
                        }
                    }
                )

                OutlinedButton(
                    onClick = { showPinConfigDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_pin_configuration_dialog_button")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasPinSaved) {
                            if (isId) "Ubah / Kelola PIN Keamanan (SHA-256)" else "Change / Manage Security PIN (SHA-256)"
                        } else {
                            if (isId) "Buat PIN Keamanan Aplikasi (4–6 Angka)" else "Set Up App Security PIN (4–6 Digits)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlue
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                val canStealthNotif = ArusKasFeatureGateEvaluator.canUseNotificationStealthMasking(currentSettings.subscription)
                SettingsToggleRow(
                    icon = Icons.Default.VisibilityOff,
                    title = if (canStealthNotif) {
                        if (isId) "Sembunyikan Nominal di Notifikasi" else "Hide Amounts in Notifications"
                    } else {
                        if (isId) "Sembunyikan Nominal di Notifikasi • PRO" else "Hide Amounts in Notifications • PRO"
                    },
                    description = if (isId) "Sensor angka uang pada pengingat aktivitas menjadi '••••••'" else "Mask currency amounts in reminder banners with '••••••'",
                    checked = currentSettings.hideAmountInNotifications && canStealthNotif,
                    onCheckedChange = { checked ->
                        if (canStealthNotif) {
                            val updated = currentSettings.copy(hideAmountInNotifications = checked)
                            currentSettings = updated
                            onUpdateSettings(updated)
                        } else {
                            onOpenPaywallWithTrigger(ProFeatureTrigger.PRIVACY_STEALTH)
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Surface(
                    onClick = { showPrivacyPolicyDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_privacy_policy_dialog_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ ${AppLocalization.get("opt_privacy_policy", lang)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isId) {
                                    "Baca rincian penyimpanan lokal, privasi data, izin aplikasi & kontak Admin iwa.ye"
                                } else {
                                    "Read full local storage details, data safety, permissions & Admin iwa.ye contact"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Privacy Policy",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // SECTION 4: NOTIFICATIONS & HAPTICS
            SettingsSectionCard(
                title = AppLocalization.get("sec_notifications", lang),
                description = AppLocalization.get("sec_notifications_desc", lang),
                icon = Icons.Default.NotificationsActive,
                accentColor = EmeraldPrimary
            ) {
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    title = AppLocalization.get("opt_daily_reminder", lang),
                    description = AppLocalization.get("opt_daily_reminder_desc", lang),
                    checked = currentSettings.dailyReminderNotification,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(dailyReminderNotification = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsToggleRow(
                    icon = Icons.Default.Vibration,
                    title = AppLocalization.get("opt_haptic", lang),
                    description = AppLocalization.get("opt_haptic_desc", lang),
                    checked = currentSettings.soundHapticFeedback,
                    onCheckedChange = { checked ->
                        val updated = currentSettings.copy(soundHapticFeedback = checked)
                        currentSettings = updated
                        onUpdateSettings(updated)
                    }
                )
            }

            // SECTION 5: STORAGE & PDF EXPORT
            SettingsSectionCard(
                title = AppLocalization.get("sec_data_storage", lang),
                description = AppLocalization.get("sec_data_storage_desc", lang),
                icon = Icons.Default.CloudDone,
                accentColor = GoldAccent
            ) {
                // WhatsApp-Style Permanent Multi-Month Cloud Storage Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BcaRoyalBlue.copy(alpha = 0.08f)),
                    border = BorderStroke(1.2.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permanent_cloud_sync_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BcaRoyalBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = BcaRoyalBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = AppLocalization.get("sec_cloud_whatsapp_title", lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = AppLocalization.get("cloud_status_synced", lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = AppLocalization.get("cloud_sync_info", lang),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val syncTimeStr = if (currentSettings.lastCloudSyncMillis > 0L) {
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(currentSettings.lastCloudSyncMillis))
                        } else {
                            AppLocalization.get("cloud_never_synced", lang)
                        }

                        Text(
                            text = String.format(AppLocalization.get("cloud_last_synced", lang), syncTimeStr),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                if (currentSettings.subscription.hasProAccess) {
                                    onShowMessage(AppLocalization.get("msg_syncing_cloud", lang))
                                    val updated = currentSettings.copy(lastCloudSyncMillis = System.currentTimeMillis())
                                    currentSettings = updated
                                    onUpdateSettings(updated)
                                    onShowMessage(AppLocalization.get("msg_sync_success", lang))
                                } else {
                                    onOpenPaywallWithTrigger(ProFeatureTrigger.AUTO_CLOUD_SYNC)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_now_cloud_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val syncBtnLabel = if (currentSettings.subscription.hasProAccess) {
                                AppLocalization.get("btn_sync_now", lang)
                            } else {
                                "${AppLocalization.get("btn_sync_now", lang)} • PRO"
                            }
                            Text(text = syncBtnLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsToggleRow(
                    icon = Icons.Default.CloudDone,
                    title = if (currentSettings.subscription.hasProAccess) {
                        AppLocalization.get("opt_cloud_backup", lang)
                    } else {
                        "${AppLocalization.get("opt_cloud_backup", lang)} • PRO"
                    },
                    description = AppLocalization.get("opt_cloud_backup_desc", lang),
                    checked = currentSettings.autoBackupCloud && currentSettings.subscription.hasProAccess,
                    onCheckedChange = { checked ->
                        if (currentSettings.subscription.hasProAccess) {
                            val updated = currentSettings.copy(autoBackupCloud = checked)
                            currentSettings = updated
                            onUpdateSettings(updated)
                        } else {
                            onOpenPaywallWithTrigger(ProFeatureTrigger.AUTO_CLOUD_SYNC)
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Full JSON Backup, Restore with SHA-256 Checksum & Storage Manager Card (100% Free Forever)
                Surface(
                    onClick = { showDataStorageDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.2.dp, BcaRoyalBlue.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_backup_restore_storage_dialog_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BcaRoyalBlue.copy(alpha = 0.22f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = BcaRoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) "📦 Pusat Backup, Restore (.JSON) & Penyimpanan" else "📦 Full JSON Backup, Restore & Storage Center",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isId) {
                                    "Cadangkan seluruh data, pulihkan (Restore) dengan validasi SHA-256, cek ukuran memori & reset data"
                                } else {
                                    "Create full JSON backups, restore with SHA-256 integrity check, inspect storage & reset data"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Backup & Storage",
                            tint = BcaRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    onClick = {
                        if (ArusKasFeatureGateEvaluator.canExportStyledPdf(currentSettings.subscription)) {
                            onExportPdf()
                        } else {
                            onOpenPaywallWithTrigger(ProFeatureTrigger.PDF_EXPORT)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_modern_pdf_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.22f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            val pdfOptTitle = if (ArusKasFeatureGateEvaluator.canExportStyledPdf(currentSettings.subscription)) {
                                AppLocalization.get("opt_export_pdf", lang)
                            } else {
                                "${AppLocalization.get("opt_export_pdf", lang)} • PRO"
                            }
                            Text(
                                text = pdfOptTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppLocalization.get("opt_export_pdf_desc", lang),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    onClick = {
                        val freedBytes = com.example.data.ArusDompetStorageInspector.clearTemporaryCacheOnly(appContext)
                        val freedKb = String.format(Locale.US, "%.1f KB", freedBytes / 1024.0)
                        onShowMessage(
                            if (isId) "${AppLocalization.get("cache_cleared_msg", lang)} ($freedKb dibebaskan, data transaksi aman)"
                            else "${AppLocalization.get("cache_cleared_msg", lang)} ($freedKb freed, financial records untouched)"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_cache_button_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLocalization.get("opt_clear_cache", lang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppLocalization.get("opt_clear_cache_desc", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SECTION 5.5: TOOLS HUB, FINANCIAL PROFILE QUESTIONNAIRE & USER GUIDE
            SettingsSectionCard(
                title = if (isId) "Pusat Alat, Kuesioner & Panduan" else "Tools Hub, Financial Profile & Guide",
                description = if (isId) {
                    "Pertanyaan & panduan hanya tampil 1x di awal, namun kamu selalu bisa membukanya kembali kapan saja dari sini:"
                } else {
                    "The initial questionnaire and user guide appear only once on first launch, and remain accessible anytime here:"
                },
                icon = Icons.Default.MenuBook,
                accentColor = EmeraldPrimary
            ) {
                // 1. Financial Profile & Country Questionnaire
                Card(
                    onClick = onOpenFinancialProfileWizard,
                    colors = CardDefaults.cardColors(
                        containerColor = BcaRoyalBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_financial_questionnaire_from_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) "📋 Kuesioner Profil, Negara & Dompet" else "📋 Financial Profile, Country & Wallets Setup",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isId) {
                                    "Atur ulang negara, mata uang, gaji, tambah/kurangi Bank & E-Wallet, dan limit otomatis"
                                } else {
                                    "Configure country, currency, income, add/remove local Banks & Digital Wallets, and auto-limits"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Questionnaire",
                            tint = BcaRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 2. App Guide
                Card(
                    onClick = onOpenUserGuide,
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_user_guide_from_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📖 ${AppLocalization.get("sec_guide_card_title", lang)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppLocalization.get("sec_guide_card_desc", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Guide",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3. Manage Banks, E-Wallets & Transfers
                Card(
                    onClick = onOpenWalletsSheet,
                    colors = CardDefaults.cardColors(
                        containerColor = GoldAccent.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.1.dp, GoldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_wallets_sheet_from_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) "🏦 Kelola Bank, E-Wallet & Pindah Saldo" else "🏦 Manage Banks, Digital Wallets & Transfers",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isId) {
                                    "Tambah/hapus rekening bank atau e-wallet dan top-up antar dompet"
                                } else {
                                    "Add or remove bank accounts & e-wallets and transfer funds between wallets"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Manage Wallets",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 4. Offline Financial Assistant & 12 Communication Personas
                Card(
                    onClick = { showOfflinePersonaDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = BcaRoyalBlue.copy(alpha = 0.11f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_offline_persona_settings_from_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) "🎭 Gaya Komunikasi & 12 Karakter Asisten Offline" else "🎭 Offline Assistant Style & 12 Personas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isId) {
                                    "Pilih Si Analis, Si Roaster, Si Kawan Dekat, tingkat ketegasan & panjang kalimat (10.000 kalimat lokal)"
                                } else {
                                    "Choose Analyst, Roaster, Bestie, strictness & sentence length (10,000 local phrases)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Persona Settings",
                            tint = BcaRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // SECTION 5.8: BANTUAN DAN MASUKAN (HELP, ADMIN SUPPORT iwa.ye / programerway@gmail.com, BUG REPORT & FAQ)
            SettingsSectionCard(
                title = if (isId) "Bantuan, Masukan & Kontak Admin" else "Help, Feedback & Admin Support",
                description = if (isId) {
                    "Hubungi Admin ${com.example.data.AdminSupportHelper.ADMIN_NAME} (${com.example.data.AdminSupportHelper.ADMIN_EMAIL}), laporkan masalah, kirim saran, atau baca 11 FAQ:"
                } else {
                    "Contact Admin ${com.example.data.AdminSupportHelper.ADMIN_NAME} (${com.example.data.AdminSupportHelper.ADMIN_EMAIL}), report bugs, send ideas, or browse 11 FAQs:"
                },
                icon = Icons.Default.ChatBubbleOutline,
                accentColor = BcaRoyalBlue
            ) {
                // Direct Admin Contact Card
                Card(
                    onClick = {
                        helpDialogInitialTab = 0
                        showHelpAndFeedbackDialog = true
                    },
                    colors = CardDefaults.cardColors(containerColor = BcaRoyalBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_contact_admin_from_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isId) "✉️ Hubungi Admin (${com.example.data.AdminSupportHelper.ADMIN_NAME})" else "✉️ Contact Admin (${com.example.data.AdminSupportHelper.ADMIN_NAME})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${com.example.data.AdminSupportHelper.ADMIN_EMAIL} • ${if (isId) "Bantuan langsung via Email" else "Direct support via Email"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BcaRoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Contact Admin",
                            tint = BcaRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Quick Action Row: Bug Report, Suggestion, 11 FAQs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            helpDialogInitialTab = 1
                            showHelpAndFeedbackDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_bug_report_from_settings")
                    ) {
                        Text(
                            text = if (isId) "🐞 Lapor Masalah" else "🐞 Report Bug",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            helpDialogInitialTab = 2
                            showHelpAndFeedbackDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_feature_suggestion_from_settings")
                    ) {
                        Text(
                            text = if (isId) "💡 Kirim Saran" else "💡 Send Idea",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            helpDialogInitialTab = 3
                            showHelpAndFeedbackDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_faq_from_settings")
                    ) {
                        Text(
                            text = "❓ FAQ (11)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Version & Build Status Strip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isId) "Status Versi Aplikasi" else "App Version Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Arus Dompet v${com.example.data.AdminSupportHelper.APP_VERSION_NAME} (Build ${com.example.data.AdminSupportHelper.APP_VERSION_CODE})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            // SECTION 6: TENTANG APLIKASI (INFORMASI RESMI & FORMAL)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_app_formal_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.app_logo),
                                contentDescription = "ArusKas Logo",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ArusKas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    border = BorderStroke(0.8.dp, EmeraldPrimary.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "Official Release",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isId) "Aplikasi Manajemen Finansial Pribadi Modern" else "Modern Personal Financial Management",
                                style = MaterialTheme.typography.labelMedium,
                                color = BcaRoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Versi ${com.example.data.AdminSupportHelper.APP_VERSION_NAME} • Build ${com.example.data.AdminSupportHelper.APP_VERSION_CODE}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    // Formal Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔒 Offline",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isId) "Privasi Aman" else "Private",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🛡️ Zero-Leak",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = if (isId) "Disiplin" else "Disciplined",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚡ Real-time",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = BcaRoyalBlue
                                )
                                Text(
                                    text = if (isId) "Akurat" else "Accurate",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isId) {
                            "ArusKas dikembangkan untuk menyediakan pencatatan keuangan pribadi yang aman, terstruktur, dan sepenuhnya beroperasi secara lokal tanpa ketergantungan pihak ketiga."
                        } else {
                            "ArusKas is engineered to provide safe, structured, and entirely offline personal financial management with zero third-party dependencies."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Core Pillars
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BcaElectricCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isId) "1. Saldo Siap Pakai Murni: Memisahkan uang harian dari tabungan masa depan." else "1. Pure Safe-to-Spend: Separates daily spending from future savings.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isId) "2. Simpanan Darurat Terpisah: Perlindungan finansial mandiri saat kondisi tak terduga." else "2. Isolated Emergency Shield: Standalone protection for unforeseen events.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isId) "3. Tabungan Target Terarah: Rencana pencapaian sasaran pengeluaran terukur." else "3. Goal Savings: Systematic plan to achieve financial targets.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = BcaCobaltBlue, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isId) "4. Kontrol Batas Pengeluaran: Monitoring limit harian dan kategori yang fleksibel." else "4. Flexible Limit Control: Real-time monitoring of daily and category caps.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = AppLocalization.get("opt_privacy_policy_desc", lang),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Done Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_and_close_settings_button")
            ) {
                Text(
                    text = AppLocalization.get("btn_close", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.18f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldPrimary
            )
        )
    }
}

