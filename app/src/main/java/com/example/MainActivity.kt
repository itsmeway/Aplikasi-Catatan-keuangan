package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import android.app.Activity
import com.example.data.AppLanguage
import com.example.data.AppLocalization
import com.example.data.AppSettings
import com.example.data.ArusKasLocalStore
import com.example.data.ArusKasRepository
import com.example.data.BillingPurchaseOutcome
import com.example.data.ModernPdfBackupGenerator
import com.example.data.PlayBillingMonetizationManager
import com.example.data.ProFeatureTrigger
import com.example.data.ProPlanTier
import com.example.data.RestorePurchaseOutcome
import com.example.data.SavingsGoal
import com.example.data.TransactionRecord
import com.example.data.Wallet
import com.example.ui.AppSettingsBottomSheet
import com.example.ui.ArusKasViewModel
import com.example.ui.BerandaExpenseDashboard
import com.example.ui.FinancialProfileOnboardingDialog
import com.example.ui.MyBcaLiquidGradientBackground
import com.example.ui.FirstTimeSetupDialog
import com.example.ui.GuruFinancialMentorScreen
import com.example.ui.ImpianSavingsScreen
import com.example.ui.UiState
import com.example.ui.WalletsBreakdownBottomSheet
import com.example.ui.UserGuideDialog
import com.example.ui.ArusKasProPaywallDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArusKasAppNavigation()
                }
            }
        }
    }
}

@Composable
fun ArusKasAppNavigation() {
    MainArusKasScreen()
}

@Composable
fun MainArusKasScreen() {
    val viewModel: ArusKasViewModel = viewModel(
        key = "local_offline_dashboard",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val localStore = ArusKasLocalStore(app)
                ArusKasViewModel(ArusKasRepository(db), null, localStore)
            }
        }
    )

    val walletsUiState by viewModel.walletsState.collectAsStateWithLifecycle()
    val transactionsUiState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val goalsUiState by viewModel.goalsState.collectAsStateWithLifecycle()
    val categoryLimits by viewModel.categoryLimits.collectAsStateWithLifecycle()
    val emergencyFund by viewModel.emergencyFund.collectAsStateWithLifecycle()
    val mandatoryBills by viewModel.mandatoryBills.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val guruState by viewModel.guruState.collectAsStateWithLifecycle()
    val sessionSeed by viewModel.sessionSeed.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val feedback by viewModel.actionFeedback.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val wallets: List<Wallet> = (walletsUiState as? UiState.Success)?.data ?: emptyList()
    val transactions: List<TransactionRecord> = (transactionsUiState as? UiState.Success)?.data ?: emptyList()
    val goals: List<SavingsGoal> = (goalsUiState as? UiState.Success)?.data ?: emptyList()

    val context = LocalContext.current
    val billingManager = remember(context) { PlayBillingMonetizationManager(context) }
    val isPlayStoreConnected by billingManager.isPlayStoreConnected.collectAsStateWithLifecycle()
    val activeReceipt by billingManager.activeReceipt.collectAsStateWithLifecycle()
    val entitlementState by billingManager.entitlementState.collectAsStateWithLifecycle()
    val liveProductDetails by billingManager.liveProductDetails.collectAsStateWithLifecycle()
    val lastBillingError by billingManager.lastErrorMessage.collectAsStateWithLifecycle()

    var showWalletsSheet by remember { mutableStateOf(false) }
    var showStarterDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var showFinancialProfileDialog by remember(userProfile.isCompleted) {
        mutableStateOf(!userProfile.isCompleted)
    }
    var showPaywallDialog by remember { mutableStateOf(false) }
    var paywallTrigger by remember { mutableStateOf(ProFeatureTrigger.GENERAL_UPGRADE) }
    var hasTriggeredOpenAnalysis by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncFromLocalStore()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initialize 30-day free trial & audit cryptographic HMAC-SHA256 entitlement vault + Google Play Billing
    LaunchedEffect(Unit) {
        billingManager.startBillingConnection()
        val reconciledSub = billingManager.auditAndReconcileSubscriptionState(appSettings.subscription)
        if (reconciledSub != appSettings.subscription) {
            viewModel.updateSettings(appSettings.copy(subscription = reconciledSub))
        }
    }

    // Every time the app opens, generate a fresh, different Bestie financial analysis & reminder notification
    LaunchedEffect(wallets.isNotEmpty()) {
        if (!hasTriggeredOpenAnalysis && wallets.isNotEmpty()) {
            hasTriggeredOpenAnalysis = true
            viewModel.requestGuruFinancialEvaluation(wallets, transactions, goals)
        }
    }

    LaunchedEffect(feedback) {
        val msg = feedback
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    var isPinUnlockedThisSession by remember { mutableStateOf(false) }

    MyApplicationTheme(
        darkTheme = appSettings.isDarkMode,
        fontScaleFactor = appSettings.fontScaleFactor,
        highContrast = appSettings.highContrastMode,
        accentTheme = appSettings.accentTheme
    ) {
        if (appSettings.isAppLockEnabled &&
            com.example.data.PinSecurityManager.hasPinConfigured(context) &&
            !isPinUnlockedThisSession
        ) {
            com.example.ui.AppPinLockGateOverlay(
                language = appSettings.language,
                onUnlocked = { isPinUnlockedThisSession = true }
            )
            return@MyApplicationTheme
        }

        MyBcaLiquidGradientBackground(isDarkMode = appSettings.isDarkMode) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
            BerandaExpenseDashboard(
                wallets = wallets,
                transactions = transactions,
                isSubmitting = isSubmitting,
                guruState = guruState,
                sessionSeed = sessionSeed,
                onRefreshBestieAnalysis = {
                    viewModel.requestGuruFinancialEvaluation(wallets, transactions, goals)
                },
                categoryLimits = categoryLimits,
                onUpdateCategoryLimit = { key, label, limitAmount, period ->
                    viewModel.updateCategoryLimit(key, label, limitAmount, period)
                },
                onUpdateCategoryFull = { key, label, subtitle, limitAmount, period, startMillis, endMillis, isCustom ->
                    viewModel.updateCategoryLimit(
                        categoryKey = key,
                        categoryLabel = label,
                        limitAmount = limitAmount,
                        period = period,
                        customSubtitle = subtitle,
                        startDateMillis = startMillis,
                        endDateMillis = endMillis,
                        isCustomCategory = isCustom
                    )
                },
                onDeleteCustomCategory = { key ->
                    viewModel.deleteCustomCategory(key)
                },
                emergencyFund = emergencyFund,
                onUpdateEmergencyFund = { sourceWallet, addedAmount, newLimit, period, storageNote ->
                    viewModel.updateEmergencyFundAndAllocate(
                        sourceWallet = sourceWallet,
                        addedEmergencyAmount = addedAmount,
                        newEmergencyLimit = newLimit,
                        limitPeriod = period,
                        storagePlaceNote = storageNote
                    )
                },
                goals = goals,
                onAddGoal = { title, target, daily, note, iconKey ->
                    viewModel.addSavingsGoal(title, target, daily, note, iconKey)
                },
                onAddGoalWithStartDate = { title, target, daily, note, iconKey, startMillis ->
                    viewModel.addSavingsGoal(title, target, daily, note, iconKey, startMillis)
                },
                onUpdateGoalInfo = { goalId, title, target, startMillis ->
                    viewModel.updateSavingsGoal(
                        goalId = goalId,
                        title = title,
                        targetAmount = target,
                        dailyRecommendation = (target / 30L).coerceAtLeast(1L),
                        startDateMillis = startMillis,
                        note = ""
                    )
                },
                onAllocateSavings = { goal, wallet, amount, note ->
                    viewModel.allocateSavingsToGoal(goal, wallet, amount, note)
                },
                onDeleteGoal = { goal ->
                    viewModel.deleteGoal(goal)
                },
                mandatoryBills = mandatoryBills,
                onUpdateMandatoryBillStatus = { bill, newStatus, wallet ->
                    viewModel.updateMandatoryBillStatus(bill, newStatus, wallet)
                },
                onUpsertMandatoryBill = { billId, name, totalAmount, dueScheduleLabel, dueDay, renewalDay, billType ->
                    viewModel.upsertMandatoryBill(
                        billId = billId,
                        name = name,
                        totalAmount = totalAmount,
                        dueScheduleLabel = dueScheduleLabel,
                        dueDayOfMonth = dueDay,
                        renewalDayOfMonth = renewalDay,
                        billType = billType
                    )
                },
                onDeleteMandatoryBill = { billId ->
                    viewModel.deleteMandatoryBill(billId)
                },
                userProfile = userProfile,
                onOpenFinancialProfileWizard = { showFinancialProfileDialog = true },
                onOpenWalletsSheet = { showWalletsSheet = true },
                onOpenStarterSetup = { showStarterDialog = true },
                onOpenUserGuide = { showUserGuideDialog = true },
                onOpenPaywall = {
                    paywallTrigger = ProFeatureTrigger.GENERAL_UPGRADE
                    showPaywallDialog = true
                },
                onOpenPaywallWithTrigger = { trigger ->
                    paywallTrigger = trigger
                    showPaywallDialog = true
                },
                onRecordInsightRefresh = {
                    val updatedSub = appSettings.subscription.recordManualInsightRefresh()
                    viewModel.updateSettings(appSettings.copy(subscription = updatedSub))
                },
                appSettings = appSettings,
                onOpenSettings = { showSettingsSheet = true },
                onRecordTransaction = { wallet, type, amount, catKey, catLabel, freqTag, note ->
                    viewModel.recordTransaction(
                        wallet = wallet,
                        type = type,
                        amount = amount,
                        categoryKey = catKey,
                        categoryLabel = catLabel,
                        frequencyTag = freqTag,
                        note = note
                    )
                },
                onRecordTransactionWithDate = { wallet, type, amount, catKey, catLabel, freqTag, note, customDateMillis ->
                    viewModel.recordTransaction(
                        wallet = wallet,
                        type = type,
                        amount = amount,
                        categoryKey = catKey,
                        categoryLabel = catLabel,
                        frequencyTag = freqTag,
                        note = note,
                        customDateMillis = customDateMillis
                    )
                },
                onDeleteTransaction = { tx ->
                    viewModel.deleteTransaction(tx, wallets)
                }
            )
                }
            }
        }

    if (showFinancialProfileDialog) {
        FinancialProfileOnboardingDialog(
            initialProfile = userProfile,
            currentWallets = wallets,
            currentLanguage = appSettings.language,
            onLanguageChange = { newLang ->
                viewModel.updateLanguage(newLang)
            },
            onCountryChange = { newCountryCode ->
                viewModel.changeCountryAndCurrency(
                    newCountryCode = newCountryCode,
                    convertExistingAmounts = true,
                    replaceWithCountryBanks = true
                )
            },
            onDismiss = {
                viewModel.dismissOrSkipOnboarding()
                showFinancialProfileDialog = false
            },
            onOpenUserGuide = {
                showUserGuideDialog = true
            },
            onCompleteSetup = { profile, configuredWallets, configuredBills, customLimits ->
                viewModel.applyFinancialProfileSetup(
                    profile = profile,
                    configuredWallets = configuredWallets,
                    configuredBills = configuredBills,
                    customCategoryLimits = customLimits
                )
                showFinancialProfileDialog = false
            }
        )
    }

    if (showWalletsSheet) {
        WalletsBreakdownBottomSheet(
            wallets = wallets,
            totalTabunganSaved = goals.sumOf { it.savedAmount },
            emergencyBalance = emergencyFund.emergencyBalance,
            subscription = appSettings.subscription,
            onOpenPaywallWithTrigger = { trigger ->
                showWalletsSheet = false
                paywallTrigger = trigger
                showPaywallDialog = true
            },
            onDismiss = { showWalletsSheet = false },
            onSaveWallet = { id, name, type, balance, colorHex ->
                viewModel.addOrUpdateWallet(
                    walletId = id,
                    name = name,
                    walletType = type,
                    balance = balance,
                    colorHex = colorHex,
                    iconKey = when (type) {
                        "BANK" -> "bank_bca"
                        "CASH" -> "cash_wallet"
                        else -> "ewallet_dana"
                    }
                )
            },
            onDeleteWallet = { w -> viewModel.deleteWallet(w) },
            onTransferWallet = { fromWallet, toWallet, amount, adminFee, note ->
                viewModel.transferBetweenWallets(
                    fromWallet = fromWallet,
                    toWallet = toWallet,
                    amount = amount,
                    adminFee = adminFee,
                    note = note
                )
            }
        )
    }

    if (showStarterDialog) {
        FirstTimeSetupDialog(
            onDismiss = { showStarterDialog = false },
            onConfirmSetup = { walletBalances, goalTitle, goalTarget, goalDaily, goalNote ->
                viewModel.seedStarterSetup(
                    walletBalances = walletBalances,
                    goalTitle = goalTitle,
                    goalTarget = goalTarget,
                    goalDaily = goalDaily,
                    goalNote = goalNote
                )
                showStarterDialog = false
            }
        )
    }

    if (showSettingsSheet) {
        val currentContext = LocalContext.current
        AppSettingsBottomSheet(
            settings = appSettings,
            onUpdateSettings = { newSettings ->
                viewModel.updateSettings(newSettings)
            },
            onChangeCountryAndCurrency = { newCountryCode ->
                viewModel.changeCountryAndCurrency(
                    newCountryCode = newCountryCode,
                    convertExistingAmounts = true,
                    replaceWithCountryBanks = true
                )
            },
            onExportPdf = {
                val result = ModernPdfBackupGenerator.generateModernLedgerPdf(
                    context = currentContext,
                    wallets = wallets,
                    transactions = transactions,
                    goals = goals,
                    emergencyFund = emergencyFund,
                    lang = appSettings.language
                )
                if (result.isSuccess) {
                    val msg = String.format(
                        AppLocalization.get("pdf_saved_msg", appSettings.language),
                        result.fileName
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                    val pdfFile = java.io.File(result.filePath)
                    if (pdfFile.exists()) {
                        ModernPdfBackupGenerator.openOrSharePdf(currentContext, pdfFile)
                    }
                } else {
                    val failMsg = String.format(
                        AppLocalization.get("pdf_failed_msg", appSettings.language),
                        result.errorMessage ?: ""
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(failMsg)
                    }
                }
            },
            onExportCsv = {
                val csvRes = ModernPdfBackupGenerator.generateCsvReport(
                    context = currentContext,
                    transactions = transactions,
                    lang = appSettings.language
                )
                if (csvRes.isSuccess) {
                    val csvFile = java.io.File(csvRes.filePath)
                    if (csvFile.exists()) {
                        ModernPdfBackupGenerator.openOrShareCsv(currentContext, csvFile)
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(csvRes.errorMessage ?: "Failed to export CSV")
                    }
                }
            },
            onGenerateBackupJson = {
                val dbHelper = com.example.data.offlineinsight.OfflineInsightDatabaseHelper.getInstance(currentContext)
                val commPrefs = com.example.data.offlineinsight.OnboardingEngine(dbHelper).loadPreferences()
                viewModel.createFullBackupJson(commPrefs)
            },
            onRestoreValidatedBackup = { parsedBackup, mergeWithoutDuplicates ->
                viewModel.restoreFromBackup(parsedBackup, mergeWithoutDuplicates)
                parsedBackup.communicationPreferences?.let { comm ->
                    val dbHelper = com.example.data.offlineinsight.OfflineInsightDatabaseHelper.getInstance(currentContext)
                    com.example.data.offlineinsight.OnboardingEngine(dbHelper).savePreferences(comm)
                }
            },
            onResetAllFinancialData = {
                viewModel.resetAllFinancialDataToZero()
            },
            onOpenUserGuide = {
                showSettingsSheet = false
                showUserGuideDialog = true
            },
            onOpenFinancialProfileWizard = {
                showSettingsSheet = false
                showFinancialProfileDialog = true
            },
            onOpenWalletsSheet = {
                showSettingsSheet = false
                showWalletsSheet = true
            },
            onOpenPaywall = {
                showSettingsSheet = false
                paywallTrigger = ProFeatureTrigger.GENERAL_UPGRADE
                showPaywallDialog = true
            },
            onOpenPaywallWithTrigger = { trigger ->
                showSettingsSheet = false
                paywallTrigger = trigger
                showPaywallDialog = true
            },
            onDismiss = { showSettingsSheet = false },
            onShowMessage = { msg ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    if (showUserGuideDialog) {
        UserGuideDialog(
            language = appSettings.language,
            onDismiss = {
                viewModel.updateSettings(appSettings.copy(hasSeenUserGuide = true))
                showUserGuideDialog = false
            },
            onCompleteGuide = {
                viewModel.updateSettings(appSettings.copy(hasSeenUserGuide = true))
            },
            onOpenStarterSetup = {
                showStarterDialog = true
            }
        )
    }

    // Ethical Freemium: Never lock users out of their own financial data!
    // Show a one-time dismissible notice when the 30-day PRO trial ends (respecting the 72h passive frequency cap),
    // then let the user continue on Free Forever without repeated interruptions.
    val shouldShowOneTimeTrialEndedNotice =
        !appSettings.subscription.hasProAccess &&
            appSettings.subscription.trialStartDateMillis > 0L &&
            !appSettings.subscription.hasDismissedExpiredTrialNotice &&
            billingManager.canShowPassivePromotionalPrompt()

    if (showPaywallDialog || shouldShowOneTimeTrialEndedNotice) {
        val activeTrigger = if (shouldShowOneTimeTrialEndedNotice && !showPaywallDialog) {
            ProFeatureTrigger.TRIAL_ENDED_NOTICE
        } else {
            paywallTrigger
        }
        val hostActivity = context as? Activity
        val isIdLang = appSettings.language == AppLanguage.INDONESIAN
        val resolvedPrices = remember(appSettings.countryCode, liveProductDetails) {
            ProPlanTier.entries.associateWith { tier ->
                billingManager.resolvePlanPriceInfo(tier, appSettings.countryCode)
            }
        }
        ArusKasProPaywallDialog(
            subscription = appSettings.subscription,
            language = appSettings.language,
            trigger = activeTrigger,
            countryCode = appSettings.countryCode,
            activeReceipt = activeReceipt,
            entitlementState = entitlementState,
            isPlayStoreConnected = isPlayStoreConnected,
            resolvedPrices = resolvedPrices,
            lastErrorMessage = lastBillingError,
            onDismiss = {
                showPaywallDialog = false
                if (shouldShowOneTimeTrialEndedNotice) {
                    billingManager.recordPassivePromotionalPromptShown()
                    val updatedSub = appSettings.subscription.copy(
                        hasDismissedExpiredTrialNotice = true
                    )
                    viewModel.updateSettings(appSettings.copy(subscription = updatedSub))
                }
            },
            onActivateFreeTrial = {
                val safeNow = billingManager.getMonotonicSafeNowMillis()
                val updatedSub = appSettings.subscription.copy(
                    trialStartDateMillis = safeNow,
                    hasDismissedExpiredTrialNotice = false
                )
                viewModel.updateSettings(appSettings.copy(subscription = updatedSub))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        AppLocalization.get("msg_trial_activated", appSettings.language)
                    )
                }
            },
            onSubscribePro = {
                billingManager.launchPurchaseFlow(
                    activity = hostActivity,
                    selectedPlan = ProPlanTier.ANNUAL,
                    currentSub = appSettings.subscription
                ) { outcome ->
                    if (outcome is BillingPurchaseOutcome.Activated) {
                        viewModel.updateSettings(appSettings.copy(subscription = outcome.updatedSubscription))
                        showPaywallDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                AppLocalization.get("msg_pro_subscribed", appSettings.language)
                            )
                        }
                    }
                }
            },
            onSubscribeProWithPlan = { selectedPlan ->
                billingManager.launchPurchaseFlow(
                    activity = hostActivity,
                    selectedPlan = selectedPlan,
                    currentSub = appSettings.subscription
                ) { outcome ->
                    when (outcome) {
                        is BillingPurchaseOutcome.Activated -> {
                            viewModel.updateSettings(appSettings.copy(subscription = outcome.updatedSubscription))
                            showPaywallDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    AppLocalization.get("msg_pro_subscribed", appSettings.language) +
                                        " (${outcome.receipt.orderId})"
                                )
                            }
                        }
                        is BillingPurchaseOutcome.PendingPayment -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) outcome.instructionsId else outcome.instructionsEn
                                )
                            }
                        }
                        is BillingPurchaseOutcome.BillingUnavailable -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) outcome.reasonId else outcome.reasonEn
                                )
                            }
                        }
                        is BillingPurchaseOutcome.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) outcome.messageId else outcome.messageEn
                                )
                            }
                        }
                        BillingPurchaseOutcome.UserCanceled -> Unit
                    }
                }
            },
            onSimulatePendingRetailPayment = { selectedPlan ->
                billingManager.launchPurchaseFlow(
                    activity = hostActivity,
                    selectedPlan = selectedPlan,
                    currentSub = appSettings.subscription,
                    simulatePendingRetailPayment = true
                ) { outcome ->
                    if (outcome is BillingPurchaseOutcome.PendingPayment) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (isIdLang) outcome.instructionsId else outcome.instructionsEn
                            )
                        }
                    }
                }
            },
            onSettlePendingPayment = {
                billingManager.settlePendingPayment(appSettings.subscription) { outcome ->
                    if (outcome is BillingPurchaseOutcome.Activated) {
                        viewModel.updateSettings(appSettings.copy(subscription = outcome.updatedSubscription))
                        showPaywallDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                AppLocalization.get("msg_pro_subscribed", appSettings.language) +
                                    " (${outcome.receipt.orderId})"
                            )
                        }
                    }
                }
            },
            onSimulateSubscriptionExpiration = {
                val expiredSub = billingManager.expireSubscriptionPeriod(appSettings.subscription)
                viewModel.updateSettings(appSettings.copy(subscription = expiredSub))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (isIdLang) {
                            "Status langganan diperbarui ke PREMIUM_EXPIRED. Seluruh data keuangan tetap utuh di paket Gratis."
                        } else {
                            "Subscription transitioned to PREMIUM_EXPIRED. All financial records remain intact on Free tier."
                        }
                    )
                }
            },
            onSimulateRefundRevocation = {
                val revokedSub = billingManager.revokeEntitlementOnRefund(appSettings.subscription)
                viewModel.updateSettings(appSettings.copy(subscription = revokedSub))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (isIdLang) {
                            "Lisensi PRO dicabut karena Refund (PREMIUM_REVOKED). Data keuangan pengguna tetap aman."
                        } else {
                            "PRO entitlement revoked due to refund (PREMIUM_REVOKED). Personal financial data remains safe."
                        }
                    )
                }
            },
            onDowngradeToFreeForever = {
                val downgradedSub = billingManager.cancelOrDowngradeToFreeForever(appSettings.subscription)
                viewModel.updateSettings(appSettings.copy(subscription = downgradedSub))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (isIdLang) {
                            "Berhasil beralih ke Paket Gratis Selamanya. Seluruh saldo, riwayat & dompet Anda 100% utuh!"
                        } else {
                            "Switched to Free Forever tier. All your balances, history & wallets remain 100% intact!"
                        }
                    )
                }
            },
            onRestorePurchase = {
                billingManager.restorePurchases(appSettings.subscription) { restoreOutcome ->
                    when (restoreOutcome) {
                        is RestorePurchaseOutcome.Restored -> {
                            viewModel.updateSettings(appSettings.copy(subscription = restoreOutcome.updatedSubscription))
                            showPaywallDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    AppLocalization.get("msg_subscription_restored", appSettings.language) +
                                        " (${restoreOutcome.receipt.orderId})"
                                )
                            }
                        }
                        is RestorePurchaseOutcome.ExpiredOrRevokedFound -> {
                            viewModel.updateSettings(appSettings.copy(subscription = restoreOutcome.updatedSubscription))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) restoreOutcome.reasonId else restoreOutcome.reasonEn
                                )
                            }
                        }
                        RestorePurchaseOutcome.NoActivePurchaseFound -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) {
                                        "Belum ditemukan riwayat pembelian PRO aktif pada perangkat/akun Google Play ini."
                                    } else {
                                        "No active PRO purchase found on this device or Google Play account."
                                    }
                                )
                            }
                        }
                        is RestorePurchaseOutcome.TamperedReceiptRejected -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isIdLang) {
                                        "Peringatan Keamanan: Tanda tangan lisensi tidak valid (${restoreOutcome.reason})."
                                    } else {
                                        "Security Alert: Invalid entitlement signature (${restoreOutcome.reason})."
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
    }
}
