package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import com.example.R
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.data.AppLanguage
import com.example.data.AppLocalization
import com.example.data.AppSettings
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.CategoryLimitConfig
import com.example.data.CurrencyFormatOption
import com.example.data.EmergencyFundConfig
import com.example.data.GenZQuotesDatabase
import com.example.data.MandatoryBillItem
import com.example.data.PresetCategories
import com.example.data.ProFeatureTrigger
import com.example.data.QuickCategoryItem
import com.example.data.SavingsGoal
import com.example.data.TransactionRecord
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaElectricCyan
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

fun iconForCategory(key: String): ImageVector {
    return when (key) {
        "pakai_darurat" -> Icons.Default.HealthAndSafety
        "tagihan_bulanan", "listrik_kos", "kuota_pulsa" -> Icons.Default.ElectricBolt
        "kebutuhan_pokok", "belanja_dapur", "makan_harian", "bensin_ojek" -> Icons.Default.ShoppingBag
        "rawat_diri" -> Icons.Default.Spa
        "gaya_hidup", "jajan_cemilan" -> Icons.Default.Favorite
        "sosial_darurat", "ada_tamu", "darurat_lain" -> Icons.Default.Groups
        "modal_produktif", "modal_usaha" -> Icons.Default.Storefront
        "ngojek_online" -> Icons.Default.DirectionsBike
        else -> Icons.Default.Bolt
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BerandaExpenseDashboard(
    wallets: List<Wallet>,
    transactions: List<TransactionRecord>,
    goals: List<SavingsGoal> = emptyList(),
    isSubmitting: Boolean,
    guruState: GuruAdviceState = GuruAdviceState.Idle,
    sessionSeed: Int = 1,
    onRefreshBestieAnalysis: () -> Unit = {},
    categoryLimits: Map<String, CategoryLimitConfig> = PresetCategories.defaultCategoryLimits,
    onUpdateCategoryLimit: (categoryKey: String, categoryLabel: String, limitAmount: Long, period: String) -> Unit = { _, _, _, _ -> },
    onUpdateCategoryFull: (
        categoryKey: String,
        customLabel: String,
        customSubtitle: String,
        limitAmount: Long,
        period: String,
        startDateMillis: Long,
        endDateMillis: Long,
        isCustomCategory: Boolean
    ) -> Unit = { k, l, _, amt, p, _, _, _ -> onUpdateCategoryLimit(k, l, amt, p) },
    onDeleteCustomCategory: (String) -> Unit = {},
    mandatoryBills: List<MandatoryBillItem> = PresetCategories.defaultMandatoryBills,
    onUpdateMandatoryBillStatus: (bill: MandatoryBillItem, newStatus: String, wallet: Wallet?) -> Unit = { _, _, _ -> },
    onUpsertMandatoryBill: (billId: String?, name: String, totalAmount: Long, dueScheduleLabel: String, dueDay: Int, renewalDay: Int?, billType: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onDeleteMandatoryBill: (String) -> Unit = {},
    userProfile: UserFinancialProfile = UserFinancialProfile(),
    onOpenFinancialProfileWizard: () -> Unit = {},
    emergencyFund: EmergencyFundConfig = EmergencyFundConfig(),
    onUpdateEmergencyFund: (sourceWallet: Wallet?, addedAmount: Long, newLimit: Long, period: String, storageNote: String) -> Unit = { _, _, _, _, _ -> },
    onAddGoal: (title: String, targetAmount: Long, dailyRec: Long, note: String, iconKey: String) -> Unit = { _, _, _, _, _ -> },
    onAddGoalWithStartDate: (title: String, targetAmount: Long, dailyRec: Long, note: String, iconKey: String, startDateMillis: Long) -> Unit = { t, target, d, n, icon, _ ->
        onAddGoal(t, target, d, n, icon)
    },
    onUpdateGoalInfo: (goalId: String, title: String, targetAmount: Long, startDateMillis: Long) -> Unit = { _, _, _, _ -> },
    onAllocateSavings: (goal: SavingsGoal, wallet: Wallet, amount: Long, note: String) -> Unit = { _, _, _, _ -> },
    onDeleteGoal: (SavingsGoal) -> Unit = {},
    onOpenWalletsSheet: () -> Unit,
    onOpenStarterSetup: () -> Unit,
    onOpenUserGuide: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = { onOpenPaywall() },
    onRecordInsightRefresh: () -> Unit = {},
    onNavigateToGuru: () -> Unit = {},
    appSettings: AppSettings = AppSettings(),
    onOpenSettings: () -> Unit = {},
    onRecordTransaction: (
        wallet: Wallet,
        type: String,
        amount: Long,
        categoryKey: String,
        categoryLabel: String,
        frequencyTag: String,
        note: String
    ) -> Unit,
    onRecordTransactionWithDate: (
        wallet: Wallet,
        type: String,
        amount: Long,
        categoryKey: String,
        categoryLabel: String,
        frequencyTag: String,
        note: String,
        customDateMillis: Long
    ) -> Unit = { w, t, a, ck, cl, ft, n, _ ->
        onRecordTransaction(w, t, a, ck, cl, ft, n)
    },
    onDeleteTransaction: (TransactionRecord) -> Unit
) {
    val lang = appSettings.language
    val isEn = lang == AppLanguage.ENGLISH
    val activeCountryProfile = WorldCountryCurrencyRegistry.getByCountryCode(appSettings.countryCode)
    val hideBalance = appSettings.hideNominalOnDashboard
    val isCompactFormat = appSettings.currencyFormat == CurrencyFormatOption.COMPACT

    fun formatMoneyDisplay(amount: Long): String {
        return when {
            hideBalance -> "${activeCountryProfile.currencySymbol} ••••••"
            isCompactFormat -> ArusKasViewModel.formatCompactMoney(amount)
            else -> ArusKasViewModel.formatMoney(amount)
        }
    }

    val totalBalance = remember(wallets) { wallets.sumOf { it.balance } }
    val totalTabunganSaved = remember(goals) { goals.sumOf { it.savedAmount } }
    val totalSemuaDuitDiKantong = remember(totalBalance, totalTabunganSaved, emergencyFund.emergencyBalance) {
        totalBalance + totalTabunganSaved + emergencyFund.emergencyBalance
    }

    val baseLocalizedCategories = remember(lang) {
        PresetCategories.localizedExpenseCategories(lang)
    }

    // Combine 6 core categories + user-created custom categories
    val allExpenseCategories = remember(categoryLimits, baseLocalizedCategories, isEn) {
        val baseKeys = baseLocalizedCategories.map { it.key }.toSet()
        val mappedBase = baseLocalizedCategories.map { base ->
            val cfg = categoryLimits[base.key]
            if (cfg != null) {
                base.copy(
                    label = normalizeFormalCategoryLabel(base.key, cfg.customLabel.ifBlank { base.label }, lang),
                    subtitle = cfg.customSubtitle.ifBlank { base.subtitle },
                    defaultLimitAmount = cfg.limitAmount,
                    defaultLimitPeriod = cfg.period
                )
            } else base
        }
        val extraCustom = categoryLimits.values
            .filter { it.categoryKey !in baseKeys && it.categoryKey != "pakai_darurat" }
            .map { cfg ->
                QuickCategoryItem(
                    key = cfg.categoryKey,
                    label = cfg.customLabel.ifBlank { if (isEn) "New Category" else "Kategori Baru" },
                    subtitle = cfg.customSubtitle.ifBlank { if (isEn) "Spending Category" else "Pos Pengeluaran" },
                    frequencyTag = "FLEKSIBEL",
                    type = "EXPENSE",
                    defaultNoteHint = if (isEn) {
                        "Note for ${cfg.customLabel.ifBlank { "Category" }}..."
                    } else {
                        "Catatan untuk ${cfg.customLabel.ifBlank { "Kategori" }}..."
                    },
                    defaultLimitAmount = cfg.limitAmount,
                    defaultLimitPeriod = cfg.period
                )
            }
        mappedBase + extraCustom
    }

    var selectedCategory by remember(lang) { mutableStateOf(PresetCategories.localizedExpenseCategories(lang).first()) }
    var editingLimitCategory by remember { mutableStateOf<QuickCategoryItem?>(null) }
    var showAddNewCategoryDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showBillEditorDialog by remember { mutableStateOf(false) }
    var editingBillItem by remember { mutableStateOf<MandatoryBillItem?>(null) }
    var showTargetDetailModal by remember { mutableStateOf(false) }
    var showCreateTargetDialog by remember { mutableStateOf(false) }
    var showMoneyNominalPopup by remember { mutableStateOf(false) }

    var selectedGoalId by remember(goals) { mutableStateOf(goals.firstOrNull()?.id ?: "") }
    LaunchedEffect(goals) {
        if (selectedGoalId.isBlank() || goals.none { it.id == selectedGoalId }) {
            selectedGoalId = goals.firstOrNull()?.id ?: ""
        }
    }

    var selectedWalletId by remember(wallets) {
        mutableStateOf(wallets.firstOrNull()?.id ?: "")
    }
    LaunchedEffect(wallets) {
        if (selectedWalletId.isBlank() || wallets.none { it.id == selectedWalletId }) {
            selectedWalletId = wallets.firstOrNull()?.id ?: ""
        }
    }

    var nominalText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var showGraphSummary by remember { mutableStateOf(false) }
    var graphMode by remember { mutableStateOf("HARIAN") }

    val todayStart = remember { ArusKasViewModel.getStartOfDayMillis() }
    val monthStart = remember { ArusKasViewModel.getStartOfMonthMillis() }

    val emergencySpent = remember(transactions, emergencyFund.limitPeriod) {
        ArusKasViewModel.calculateSpentForCategoryLimit(
            categoryKey = "pakai_darurat",
            period = emergencyFund.limitPeriod,
            transactions = transactions
        )
    }

    val todayExpense = remember(transactions, todayStart) {
        transactions.filter { it.type == "EXPENSE" && it.transactionDateMillis >= todayStart }.sumOf { it.amount }
    }
    val todayIncome = remember(transactions, todayStart) {
        transactions.filter { it.type == "INCOME" && it.transactionDateMillis >= todayStart }.sumOf { it.amount }
    }
    val monthExpense = remember(transactions, monthStart) {
        transactions.filter { it.type == "EXPENSE" && it.transactionDateMillis >= monthStart }.sumOf { it.amount }
    }
    val monthIncomeTransactions = remember(transactions, monthStart) {
        transactions.filter { it.type == "INCOME" && it.transactionDateMillis >= monthStart }
    }
    val monthIncome = remember(monthIncomeTransactions) {
        monthIncomeTransactions.sumOf { it.amount }
    }
    var inspectingIncomeCategoryKey by remember { mutableStateOf<String?>(null) }

    val activeSelectedCategory = remember(selectedCategory, allExpenseCategories) {
        allExpenseCategories.find { it.key == selectedCategory.key } ?: selectedCategory
    }

    val genZAnalysis = remember(
        totalBalance,
        totalTabunganSaved,
        emergencyFund,
        todayExpense,
        monthExpense,
        monthIncome,
        activeSelectedCategory,
        categoryLimits,
        transactions,
        mandatoryBills,
        goals,
        sessionSeed,
        guruState,
        lang
    ) {
        val successGuru = guruState as? GuruAdviceState.Success
        val rawAiQuote = successGuru?.nominalBoxAnalysis?.takeIf { it.isNotBlank() }
        GenZQuotesDatabase.generatePersonaAnalysis(
            totalBalance = totalBalance,
            totalTabunganSaved = totalTabunganSaved,
            emergencyBalance = emergencyFund.emergencyBalance,
            todayExpense = todayExpense,
            monthExpense = monthExpense,
            monthIncome = monthIncome,
            selectedCategory = activeSelectedCategory,
            categoryLimits = categoryLimits,
            transactions = transactions,
            goals = goals,
            sessionSeed = sessionSeed,
            language = lang,
            aiOverrideText = rawAiQuote,
            aiColorLevel = successGuru?.colorLevel,
            mandatoryBills = mandatoryBills
        )
    }

    val activeGoal = goals.find { it.id == selectedGoalId } ?: goals.firstOrNull()
    val currentSelectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()

    var activeTab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = activeTab != 0) { activeTab = 0 }

    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val latestThreeTransactions = remember(transactions) {
        transactions.sortedByDescending { it.transactionDateMillis }.take(3)
    }
    val unpaidBillsCount = remember(mandatoryBills) {
        mandatoryBills.count { it.status != "PAID" }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // CLEAN & UNCLUTTERED TOP BAR (Logo + Name + Currency Chip | Panduan | Pengaturan)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.5.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "ArusKas Logo",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Column {
                            Text(
                                text = "ArusKas",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = BcaRoyalBlue
                            )
                            Text(
                                text = when (activeTab) {
                                    0 -> if (isEn) "Overview" else "Ringkasan Keuangan"
                                    1 -> if (isEn) "Spending Limits" else "Batas Pengeluaran"
                                    2 -> if (isEn) "History & Report" else "Riwayat & Laporan"
                                    else -> if (isEn) "Goals & Bills" else "Tabungan & Tagihan"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Country & Currency Badge
                        Surface(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .testTag("top_country_currency_chip")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${activeCountryProfile.flagEmoji} ${activeCountryProfile.currencyCode}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                    color = BcaRoyalBlue,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // User Guide Button
                        Surface(
                            onClick = onOpenUserGuide,
                            shape = CircleShape,
                            color = BcaRoyalBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("open_user_guide_top_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = if (isEn) "User Guide" else "Panduan",
                                    tint = BcaRoyalBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Settings Button
                        Surface(
                            onClick = onOpenSettings,
                            shape = CircleShape,
                            color = BcaRoyalBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("open_settings_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = if (isEn) "Settings" else "Pengaturan",
                                    tint = BcaRoyalBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BcaRoyalBlue,
                    selectedTextColor = BcaRoyalBlue,
                    indicatorColor = BcaRoyalBlue.copy(alpha = 0.14f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = if (isEn) "Home" else "Beranda",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isEn) "Home" else "Beranda",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            fontWeight = if (activeTab == 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = if (isEn) "Limits" else "Limit",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isEn) "Limits" else "Limit",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            fontWeight = if (activeTab == 1) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_limits")
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = if (isEn) "History" else "Riwayat",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isEn) "History" else "Riwayat",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            fontWeight = if (activeTab == 2) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = if (isEn) "Goals" else "Target",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isEn) "Goals" else "Target",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                            fontWeight = if (activeTab == 3) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_goals")
                )
            }
        },
        floatingActionButton = {
            FloatingMoneyToggleButton(
                isOpen = showMoneyNominalPopup,
                onClick = { showMoneyNominalPopup = !showMoneyNominalPopup }
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .padding(bottom = 86.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 0: BERANDA (Calm, spacious overview + clear Quick Actions)
                    TotalSaldoDirectWalletsCard(
                        wallets = wallets,
                        totalBalance = totalBalance,
                        totalSemuaDuitDiKantong = totalSemuaDuitDiKantong,
                        saldoLineColor = genZAnalysis.saldoLineColor,
                        formatMoneyDisplay = ::formatMoneyDisplay,
                        onOpenWalletsSheet = onOpenWalletsSheet,
                        onOpenCountryCurrencySheet = onOpenSettings
                    )

                    // 3 CLEAR QUICK ACTIONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Expense
                        Surface(
                            onClick = {
                                val firstExp = allExpenseCategories.firstOrNull()
                                    ?: PresetCategories.localizedExpenseCategories(lang).first()
                                selectedCategory = firstExp
                                showMoneyNominalPopup = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            border = BorderStroke(1.2.dp, CoralExpense.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 90.dp)
                                .testTag("quick_action_expense")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CoralExpense.copy(alpha = 0.14f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = if (isEn) "Add Expense" else "Catat Keluar",
                                            tint = CoralExpense,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEn) "Expense" else "Catat Keluar",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        // Quick Income
                        Surface(
                            onClick = {
                                selectedCategory = PresetCategories.localizedIncomePills(lang).first()
                                showMoneyNominalPopup = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 90.dp)
                                .testTag("quick_action_income")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = if (isEn) "Add Income" else "Catat Masuk",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEn) "Income" else "Catat Masuk",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        // Manage Wallets
                        Surface(
                            onClick = onOpenWalletsSheet,
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            border = BorderStroke(1.2.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 90.dp)
                                .testTag("quick_action_wallets")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BcaRoyalBlue.copy(alpha = 0.14f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = if (isEn) "Wallets" else "Atur Dompet",
                                            tint = BcaRoyalBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEn) "Wallets" else "Atur Dompet",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // OFFLINE SMART INSIGHT CARD
                    if (appSettings.showInsightCardOnDashboard) {
                        SmartOfflineInsightCard(
                            wallets = wallets,
                            transactions = transactions,
                            goals = goals,
                            categoryLimits = categoryLimits,
                            mandatoryBills = mandatoryBills,
                            emergencyFund = emergencyFund,
                            userProfile = userProfile,
                            language = lang,
                            isDarkMode = appSettings.isDarkMode,
                            subscription = appSettings.subscription,
                            onOpenPaywallWithTrigger = onOpenPaywallWithTrigger,
                            onRecordInsightRefresh = onRecordInsightRefresh
                        )
                    }

                    // TODAY SUMMARY + UNPAID BILLS REMINDER + LATEST 3 TRANSACTIONS
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Today's Activity" else "Aktivitas Hari Ini",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(
                                    onClick = { activeTab = 2 },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isEn) "See All" else "Lihat Semua",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                        color = BcaRoyalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = BcaRoyalBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isEn) "Today In" else "Masuk Hari Ini",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "+${formatMoneyDisplay(todayIncome)}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 15.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CoralExpense.copy(alpha = 0.1f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isEn) "Today Out" else "Keluar Hari Ini",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            color = CoralExpense
                                        )
                                        Text(
                                            text = "-${formatMoneyDisplay(todayExpense)}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 15.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = CoralExpense,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (unpaidBillsCount > 0) {
                                Surface(
                                    onClick = { activeTab = 3 },
                                    shape = RoundedCornerShape(12.dp),
                                    color = GoldAccent.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = null,
                                                tint = GoldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = if (isEn) {
                                                    "$unpaidBillsCount mandatory bills not paid yet"
                                                } else {
                                                    "Ada $unpaidBillsCount tagihan wajib belum dibayar"
                                                },
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = if (isEn) "Check" else "Cek",
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                            color = BcaRoyalBlue,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                            if (latestThreeTransactions.isEmpty()) {
                                Text(
                                    text = if (isEn) {
                                        "No transactions recorded yet. Tap '+ Record' below to start."
                                    } else {
                                        "Belum ada catatan transaksi. Ketuk '+ Catat' di bawah untuk memulai."
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            } else {
                                latestThreeTransactions.forEach { tx ->
                                    val isInc = tx.type == "INCOME"
                                    val rowClr = when (tx.type) {
                                        "INCOME" -> EmeraldPrimary
                                        "SAVINGS_ALLOCATION" -> GoldAccent
                                        "TRANSFER" -> SkyBlue
                                        else -> CoralExpense
                                    }
                                    val prefix = when (tx.type) {
                                        "INCOME" -> "+"
                                        "TRANSFER" -> "⇄ "
                                        else -> "-"
                                    }
                                    val catLbl = normalizeFormalCategoryLabel(tx.categoryKey, tx.categoryLabel, lang)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeTab = 2 }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = rowClr.copy(alpha = 0.14f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = iconForCategory(tx.categoryKey),
                                                    contentDescription = catLbl,
                                                    tint = rowClr,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tx.note.ifBlank { catLbl },
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$catLbl • ${tx.walletName} • ${timeFmt.format(Date(tx.transactionDateMillis))}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "$prefix${formatMoneyDisplay(tx.amount)}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 14.5.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = if (isInc) EmeraldPrimary else rowClr,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: LIMIT KATEGORI & DANA DARURAT
                    UpgradedCategoryGroupSection(
                        allCategoryItems = allExpenseCategories,
                        categoryLimits = categoryLimits,
                        transactions = transactions,
                        mandatoryBills = mandatoryBills,
                        emergencyFund = emergencyFund,
                        emergencySpent = emergencySpent,
                        selectedCategoryKey = selectedCategory.key,
                        isDarkMode = appSettings.isDarkMode,
                        onSelectCategoryAndOpenInput = { pickedCat ->
                            selectedCategory = pickedCat
                            showMoneyNominalPopup = true
                        },
                        onEditCategoryLimit = { catToEdit ->
                            editingLimitCategory = catToEdit
                        },
                        onAddNewCategory = {
                            val baseKeys = baseLocalizedCategories.map { it.key }.toSet()
                            val customCategoryCount = categoryLimits.values.count {
                                it.isCustomCategory || (it.categoryKey !in baseKeys && it.categoryKey != "pakai_darurat")
                            }
                            if (ArusKasFeatureGateEvaluator.canAddNewCustomCategory(customCategoryCount, appSettings.subscription)) {
                                showAddNewCategoryDialog = true
                            } else {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_CUSTOM_CATEGORIES)
                            }
                        },
                        onOpenEmergencyDialog = {
                            showEmergencyDialog = true
                        }
                    )
                }

                2 -> {
                    // TAB 2: RIWAYAT TRANSAKSI & LAPORAN
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isEn) "Month In" else "Masuk Bulan Ini",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = formatMoneyDisplay(monthIncome),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 14.5.sp,
                                        fontFamily = SpaceGroteskFontFamily
                                    ),
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isEn) "Month Out" else "Keluar Bulan Ini",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = CoralExpense
                                )
                                Text(
                                    text = formatMoneyDisplay(monthExpense),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 14.5.sp,
                                        fontFamily = SpaceGroteskFontFamily
                                    ),
                                    color = CoralExpense,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    DailySeparatedTransactionTablesSection(
                        transactions = transactions,
                        wallets = wallets,
                        goals = goals,
                        emergencyFund = emergencyFund,
                        subscription = appSettings.subscription,
                        onOpenPaywallWithTrigger = onOpenPaywallWithTrigger,
                        isDarkMode = appSettings.isDarkMode,
                        hideBalance = hideBalance,
                        onDeleteTransaction = onDeleteTransaction,
                        onOpenMoneyInputPopup = { showMoneyNominalPopup = true }
                    )
                }

                else -> {
                    // TAB 3: TABUNGAN TARGET & TAGIHAN WAJIB
                    SideBySideTargetAndBillsRow(
                        activeGoal = activeGoal,
                        allGoals = goals,
                        mandatoryBills = mandatoryBills,
                        selectedWallet = currentSelectedWallet,
                        isDarkMode = appSettings.isDarkMode,
                        onOpenTargetDetailModal = { showTargetDetailModal = true },
                        onOpenCreateTargetModal = {
                            if (ArusKasFeatureGateEvaluator.canAddNewSavingsGoal(goals.size, appSettings.subscription)) {
                                showCreateTargetDialog = true
                            } else {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_GOALS)
                            }
                        },
                        onToggleBillPaidStatus = { bill, newStatus ->
                            onUpdateMandatoryBillStatus(bill, newStatus, currentSelectedWallet)
                        },
                        onAddBillClick = {
                            if (ArusKasFeatureGateEvaluator.canAddNewMandatoryBill(mandatoryBills.size, appSettings.subscription)) {
                                editingBillItem = null
                                showBillEditorDialog = true
                            } else {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_BILLS)
                            }
                        },
                        onEditBillClick = { bill ->
                            editingBillItem = bill
                            showBillEditorDialog = true
                        }
                    )
                }
            }
        }
    }

    // BOTTOM POP-UP NOMINAL INPUT SHEET
    if (showMoneyNominalPopup) {
        BottomPopupNominalInputSheet(
            wallets = wallets,
            availableExpenseCategories = allExpenseCategories,
            selectedCategory = activeSelectedCategory,
            onSelectCategory = { selectedCategory = it },
            selectedWalletId = selectedWalletId,
            onSelectWalletId = { selectedWalletId = it },
            nominalText = nominalText,
            onNominalTextChange = { nominalText = it },
            noteText = noteText,
            onNoteTextChange = { noteText = it },
            userProfile = userProfile,
            emergencyFund = emergencyFund,
            isSubmitting = isSubmitting,
            formatMoneyDisplay = ::formatMoneyDisplay,
            onDismiss = { showMoneyNominalPopup = false },
            onSubmitTransaction = { wallet, type, amount, catKey, catLabel, freqTag, note, customDateMillis ->
                onRecordTransactionWithDate(
                    wallet,
                    type,
                    amount,
                    catKey,
                    catLabel,
                    freqTag,
                    note,
                    customDateMillis
                )
                nominalText = ""
                noteText = ""
                showMoneyNominalPopup = false
            }
        )
    }

    // ADD / EDIT CATEGORY DIALOG WITH CALENDAR (START DATE - END DATE)
    if (editingLimitCategory != null || showAddNewCategoryDialog) {
        val cat = editingLimitCategory
        val currentCfg = if (cat != null) {
            categoryLimits[cat.key] ?: CategoryLimitConfig(
                categoryKey = cat.key,
                limitAmount = cat.defaultLimitAmount,
                period = cat.defaultLimitPeriod,
                customLabel = cat.label,
                customSubtitle = cat.subtitle
            )
        } else null

        CategoryEditorWithCalendarDialog(
            category = cat,
            initialConfig = currentCfg,
            isCreatingNew = showAddNewCategoryDialog,
            subscription = appSettings.subscription,
            onOpenPaywallWithTrigger = onOpenPaywallWithTrigger,
            onDismiss = {
                editingLimitCategory = null
                showAddNewCategoryDialog = false
            },
            onDeleteCategory = { keyToDelete ->
                onDeleteCustomCategory(keyToDelete)
            },
            onSave = { key, customLabel, customSubtitle, limitAmount, period, startMillis, endMillis, isCustom ->
                onUpdateCategoryFull(
                    key,
                    customLabel,
                    customSubtitle,
                    limitAmount,
                    period,
                    startMillis,
                    endMillis,
                    isCustom
                )
                editingLimitCategory = null
                showAddNewCategoryDialog = false
            }
        )
    }

    // SAVINGS TARGET DETAIL DIALOG
    if (showTargetDetailModal) {
        SavingsTargetDetailAndDepositDialog(
            activeGoal = activeGoal,
            allGoals = goals,
            wallets = wallets,
            transactions = transactions,
            onSelectGoal = { picked -> selectedGoalId = picked.id },
            onCreateNewTarget = { title, targetAmt, startMillis ->
                if (ArusKasFeatureGateEvaluator.canAddNewSavingsGoal(goals.size, appSettings.subscription)) {
                    val daily = (targetAmt / 180L).coerceAtLeast(1L)
                    onAddGoalWithStartDate(title, targetAmt, daily, if (isEn) "Dream Goal" else "Target Impian", "laptop", startMillis)
                } else {
                    onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_GOALS)
                }
            },
            onUpdateTargetInfo = { goalId, title, targetAmt, startMillis ->
                onUpdateGoalInfo(goalId, title, targetAmt, startMillis)
            },
            onAllocateSavings = { goal, wallet, amount, note ->
                onAllocateSavings(goal, wallet, amount, note)
            },
            onDeleteGoal = { goal ->
                onDeleteGoal(goal)
                showTargetDetailModal = false
            },
            onDismiss = { showTargetDetailModal = false }
        )
    }

    if (showBillEditorDialog) {
        MandatoryBillEditorDialog(
            initialBill = editingBillItem,
            onDismiss = {
                showBillEditorDialog = false
                editingBillItem = null
            },
            onSave = { billId, name, totalAmount, dueScheduleLabel, dueDay, renewalDay, billType ->
                onUpsertMandatoryBill(billId, name, totalAmount, dueScheduleLabel, dueDay, renewalDay, billType)
                showBillEditorDialog = false
                editingBillItem = null
            }
        )
    }

    inspectingIncomeCategoryKey?.let { filterKey ->
        IncomeHistoryDetailDialog(
            initialFilterKey = filterKey,
            monthIncomeTransactions = monthIncomeTransactions,
            todayIncome = todayIncome,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            onDismiss = { inspectingIncomeCategoryKey = null }
        )
    }

    if (showEmergencyDialog) {
        EmergencyFundDialog(
            wallets = wallets,
            currentConfig = emergencyFund,
            currentSpent = emergencySpent,
            onDismiss = { showEmergencyDialog = false },
            onSave = { sourceWallet, addedAmount, newLimit, period, storageNote ->
                onUpdateEmergencyFund(sourceWallet, addedAmount, newLimit, period, storageNote)
                showEmergencyDialog = false
            }
        )
    }

    if (showCreateTargetDialog) {
        TabunganTargetCreateDialog(
            onDismiss = { showCreateTargetDialog = false },
            onSave = { title, target, daily, note ->
                onAddGoalWithStartDate(title, target, daily, note, "laptop", System.currentTimeMillis())
                showCreateTargetDialog = false
            }
        )
    }
}

@Composable
fun IncomeHistoryDetailDialog(
    initialFilterKey: String,
    monthIncomeTransactions: List<TransactionRecord>,
    todayIncome: Long,
    monthIncome: Long,
    monthExpense: Long,
    onDismiss: () -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    var activeFilter by remember(initialFilterKey) { mutableStateOf(initialFilterKey) }
    val filteredList = remember(monthIncomeTransactions, activeFilter) {
        if (activeFilter == "ALL") {
            monthIncomeTransactions
        } else {
            monthIncomeTransactions.filter { it.categoryKey == activeFilter }
        }
    }
    val walletBreakdown = remember(monthIncomeTransactions, isEn) {
        monthIncomeTransactions
            .groupBy { it.walletName.ifBlank { if (isEn) "Account" else "Dompet" } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }
    val netCashflow = monthIncome - monthExpense
    val dateFormat = remember(isEn) {
        SimpleDateFormat("dd MMM, HH:mm", if (isEn) Locale.US else Locale("in", "ID"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isEn) "Income Breakdown" else "Rincian Masuknya Uang",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEn) {
                        "Today: +${ArusKasViewModel.formatMoney(todayIncome)} • This Month: +${ArusKasViewModel.formatMoney(monthIncome)}"
                    } else {
                        "Hari Ini: +${ArusKasViewModel.formatMoney(todayIncome)} • Bulan Ini: +${ArusKasViewModel.formatMoney(monthIncome)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (netCashflow >= 0L) EmeraldPrimary else GoldAccent).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, (if (netCashflow >= 0L) EmeraldPrimary else GoldAccent).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (netCashflow >= 0L) {
                                if (isEn) "🟢 Monthly Surplus: +${ArusKasViewModel.formatMoney(netCashflow)}"
                                else "🟢 Surplus Bulan Ini: +${ArusKasViewModel.formatMoney(netCashflow)}"
                            } else {
                                if (isEn) "🟡 Monthly Deficit: -${ArusKasViewModel.formatMoney(kotlin.math.abs(netCashflow))}"
                                else "🟡 Defisit Bulan Ini: -${ArusKasViewModel.formatMoney(kotlin.math.abs(netCashflow))}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (netCashflow >= 0L) EmeraldPrimary else GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) {
                                "Healthy habit tip: allocate at least 20% (${ArusKasViewModel.formatMoney((monthIncome * 20L) / 100L)}) of your income to Savings Target or Emergency Fund."
                            } else {
                                "Saran sehat finansial: amankan minimal 20% (${ArusKasViewModel.formatMoney((monthIncome * 20L) / 100L)}) dari total uang masuk ke Tabungan Target / Dana Darurat."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (walletBreakdown.isNotEmpty()) {
                    Text(
                        text = if (isEn) "Received Across Accounts (This Month):" else "Masuk ke Dompet Mana Saja (Bulan Ini):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        walletBreakdown.forEach { (walletName, sumAmt) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.8.dp, EmeraldPrimary.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "$walletName: +${ArusKasViewModel.formatCompactMoney(sumAmt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == "ALL",
                        onClick = { activeFilter = "ALL" },
                        label = {
                            Text(
                                if (isEn) "All (${monthIncomeTransactions.size})" else "Semua (${monthIncomeTransactions.size})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    PresetCategories.localizedIncomePills(lang).forEach { inc ->
                        val count = monthIncomeTransactions.count { it.categoryKey == inc.key }
                        FilterChip(
                            selected = activeFilter == inc.key,
                            onClick = { activeFilter = inc.key },
                            label = { Text("${inc.label} ($count)", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (filteredList.isEmpty()) {
                    Text(
                        text = if (isEn) "No income recorded in this category this month." else "Belum ada catatan uang masuk pada kategori ini bulan ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    filteredList.take(10).forEach { tx ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.note.ifBlank { tx.categoryLabel },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${tx.categoryLabel} • ${tx.walletName} • ${dateFormat.format(Date(tx.transactionDateMillis))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+${ArusKasViewModel.formatCompactMoney(tx.amount)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Close" else "Tutup", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TabunganTargetDepositDialog(
    goal: SavingsGoal,
    allGoals: List<SavingsGoal> = emptyList(),
    onSelectGoal: (SavingsGoal) -> Unit = {},
    onCreateNewTarget: () -> Unit = {},
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {},
    onConfirm: (wallet: Wallet, amount: Long, note: String) -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    var selectedWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var amountStr by remember(goal) {
        mutableStateOf(goal.dailyRecommendation.coerceAtLeast(1L).toString())
    }
    var noteStr by remember(goal, isEn) {
        mutableStateOf(
            if (isEn) "Savings Target: ${goal.title} (Kept Separate)"
            else "Tabungan Target: ${goal.title} (Simpan Terpisah)"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Savings: ${goal.title}" else "Tabungan: ${goal.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = onCreateNewTarget,
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.testTag("quick_add_target_button")
                    ) {
                        Text(
                            text = if (isEn) "+ New Target" else "+ Target Baru",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = if (isEn) {
                        "Saved: ${ArusKasViewModel.formatMoney(goal.savedAmount)} / ${ArusKasViewModel.formatMoney(goal.targetAmount)}"
                    } else {
                        "Terkumpul: ${ArusKasViewModel.formatMoney(goal.savedAmount)} / ${ArusKasViewModel.formatMoney(goal.targetAmount)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (allGoals.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allGoals.forEach { g ->
                            val isSel = g.id == goal.id
                            FilterChip(
                                selected = isSel,
                                onClick = { onSelectGoal(g) },
                                label = { Text(g.title, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isEn) "⚠️ IMPORTANT: Keep Savings in a Separate Account!" else "⚠️ PENTING: Pisahkan Tempat Simpan Uang Tabungan!",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) {
                                "Store your savings target in a dedicated savings account or sub-pocket separate from daily spending. This deposit automatically deducts from your Active Balance."
                            } else {
                                "Pindahkan/simpan uang tabungan target ini di rekening atau kantong khusus yang berbeda dari uang jajan harian. Setoran ini otomatis memotong Saldo Siap Pakai."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (isEn) "Deduct from Account:" else "Potong dari Saldo Dompet Mana:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wallets.forEach { w ->
                        val isSel = w.id == selectedWalletId
                        Surface(
                            onClick = { selectedWalletId = w.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) EmeraldPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(w.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(ArusKasViewModel.formatCompactMoney(w.balance), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isEn) "Deposit Amount (${countryProfile.currencyCode})" else "Nominal Setor Tabungan (${countryProfile.currencyCode})") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_target_amount_input")
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text(if (isEn) "Where It Is Stored / Note" else "Disimpan di Mana / Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                    val amt = amountStr.toLongOrNull() ?: 0L
                    if (w != null && amt > 0L) {
                        onConfirm(w, amt, noteStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color(0xFF042619)
                ),
                modifier = Modifier.testTag("confirm_deposit_target_button")
            ) {
                Text(if (isEn) "Deduct & Save" else "Potong Saldo & Tabung", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDelete) {
                    Text(if (isEn) "Delete Target" else "Hapus Target", color = CoralExpense)
                }
                TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "Batal") }
            }
        }
    )
}

@Composable
fun TabunganTargetCreateDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, target: Long, daily: Long, note: String) -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val defaultDepositPreset = remember(countryProfile.countryCode) {
        WorldCountryCurrencyRegistry.presetSavingsDepositOptions(countryProfile.countryCode).firstOrNull() ?: 25L
    }

    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var dailyStr by remember(defaultDepositPreset) { mutableStateOf(defaultDepositPreset.toString()) }
    var note by remember(isEn) {
        mutableStateOf(
            if (isEn) "Kept separate in dedicated savings account"
            else "Disimpan terpisah di rekening tabungan"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEn) "Create New Savings Target" else "Buat Tabungan Target Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isEn) {
                        "💡 Keep your Savings Target money separate from daily spending accounts so you reach your goal faster."
                    } else {
                        "💡 Pastikan uang Tabungan Target disimpan terpisah dari saldo jajan harian agar targetmu cepat tercapai."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isEn) "Target Name (e.g., MacBook / Phone / Vehicle)" else "Nama Target (misal: Beli Laptop / HP / Motor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isEn) "Target Amount (${countryProfile.currencyCode})" else "Target Nominal (${countryProfile.currencyCode})") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dailyStr,
                    onValueChange = { dailyStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isEn) "Daily Deposit Target (${countryProfile.currencyCode})" else "Target Setoran per Hari (${countryProfile.currencyCode})") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isEn) "Separate Storage Account & Note" else "Tempat Simpan Terpisah & Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = targetStr.toLongOrNull() ?: 0L
                    val d = dailyStr.toLongOrNull() ?: defaultDepositPreset
                    if (title.isNotBlank() && t > 0L) {
                        onSave(title.trim(), t, d, note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = Color(0xFF231700)
                )
            ) {
                Text(if (isEn) "Save Target" else "Simpan Target", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "Batal") }
        }
    )
}

@Composable
fun EmergencyFundDialog(
    wallets: List<Wallet>,
    currentConfig: EmergencyFundConfig,
    currentSpent: Long,
    onDismiss: () -> Unit,
    onSave: (
        sourceWallet: Wallet?,
        addedAmount: Long,
        newLimit: Long,
        period: String,
        storageNote: String
    ) -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    var addAmountStr by remember { mutableStateOf("") }
    var limitStr by remember(currentConfig) { mutableStateOf(currentConfig.emergencyLimit.toString()) }
    var selectedPeriod by remember(currentConfig) { mutableStateOf(currentConfig.limitPeriod) }
    var storageNote by remember(currentConfig) { mutableStateOf(currentConfig.storagePlaceNote) }
    var selectedSourceWalletId by remember(wallets) {
        mutableStateOf(wallets.firstOrNull()?.id ?: "")
    }

    val periodOptions = listOf(
        "BULANAN" to (if (isEn) "Monthly" else "Per Bulan"),
        "MINGGUAN" to (if (isEn) "Weekly" else "Per Minggu"),
        "HARIAN" to (if (isEn) "Daily" else "Per Hari"),
        "BEBAS" to (if (isEn) "Flexible" else "Semaunya / Bebas")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isEn) "Emergency Fund & Limit" else "Pos Dana & Limit Uang Darurat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEn) {
                        "Current Reserve: ${ArusKasViewModel.formatMoney(currentConfig.emergencyBalance)} • Used: ${ArusKasViewModel.formatMoney(currentSpent)}"
                    } else {
                        "Saldo Darurat Saat Ini: ${ArusKasViewModel.formatMoney(currentConfig.emergencyBalance)} • Terpakai: ${ArusKasViewModel.formatMoney(currentSpent)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isEn) "⚠️ IMPORTANT: Keep Emergency Funds Separate!" else "⚠️ PENTING: Pisahkan Penyimpanan Uang Darurat!",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) {
                                "Store emergency funds in a dedicated savings account separate from daily spending. Adding to your Emergency Fund below automatically deducts from your Active Balance."
                            } else {
                                "Simpan uang darurat di tempat/rekening yang berbeda dari uang jajan harian. Setiap penambahan Saldo Darurat di bawah ini otomatis memotong Total Saldo Aktifmu supaya tidak ikut terpakai."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. Add Emergency Balance
                Text(
                    text = if (isEn) "1. Add / Allocate to Separate Emergency Reserve" else "1. Tambah / Sisihkan ke Saldo Darurat Terpisah",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                OutlinedTextField(
                    value = addAmountStr,
                    onValueChange = { input -> addAmountStr = input.filter { it.isDigit() } },
                    label = { Text(if (isEn) "Add Emergency Reserve (${countryProfile.currencyCode})" else "Nominal Tambah Saldo Darurat (${countryProfile.currencyCode})") },
                    placeholder = { Text(if (isEn) "Leave empty if only changing limit" else "Kosongkan jika hanya ubah limit") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_emergency_balance_input")
                )

                if (wallets.isNotEmpty()) {
                    Text(
                        text = if (isEn) "Deduct from Active Account:" else "Potong dari Saldo Dompet Mana (Agar Total Saldo Langsung Terpotong):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.forEach { w ->
                            val isSel = w.id == selectedSourceWalletId
                            Surface(
                                onClick = { selectedSourceWalletId = w.id },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(w.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(ArusKasViewModel.formatCompactMoney(w.balance), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = storageNote,
                    onValueChange = { storageNote = it },
                    label = { Text(if (isEn) "Where Is It Stored? (Reminder)" else "Disimpan Terpisah di Mana? (Pengingat)") },
                    placeholder = { Text(if (isEn) "e.g., Dedicated High-Yield Savings Account" else "Misal: Rekening Khusus / Amplop Darurat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 2. Emergency Spending Limit
                Text(
                    text = if (isEn) "2. Set Emergency Spending Limit" else "2. Atur Limit Pengeluaran Uang Darurat",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralExpense
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    periodOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = { selectedPeriod = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { input -> limitStr = input.filter { it.isDigit() } },
                    label = { Text(if (isEn) "Emergency Spending Limit (${countryProfile.currencyCode})" else "Target Limit Pengeluaran Darurat (${countryProfile.currencyCode})") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_limit_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sourceW = wallets.find { it.id == selectedSourceWalletId } ?: wallets.firstOrNull()
                    val added = addAmountStr.toLongOrNull() ?: 0L
                    val newLim = limitStr.toLongOrNull() ?: currentConfig.emergencyLimit
                    onSave(sourceW, added, newLim, selectedPeriod, storageNote)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = Color(0xFF231700)
                ),
                modifier = Modifier.testTag("save_emergency_fund_button")
            ) {
                Text(if (isEn) "Save Emergency Fund" else "Simpan Pos Darurat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "Batal")
            }
        }
    )
}

@Composable
private fun Expense3x3Tile(
    modifier: Modifier = Modifier,
    item: QuickCategoryItem,
    spentAmount: Long,
    limitConfig: CategoryLimitConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditLimit: () -> Unit
) {
    val limitAmt = limitConfig.limitAmount
    val ratio = if (limitAmt > 0L) (spentAmount.toFloat() / limitAmt.toFloat()) else 0f
    val pctUsed = (ratio * 100f).toInt().coerceAtLeast(0)
    val remainingAmt = (limitAmt - spentAmount).coerceAtLeast(0L)
    val statusColor = when {
        limitAmt <= 0L -> SkyBlue
        ratio >= 1f -> CoralExpense
        ratio >= 0.75f -> GoldAccent
        else -> EmeraldPrimary
    }
    val shortPeriod = when (limitConfig.period) {
        "HARIAN" -> "/hr"
        "MINGGUAN" -> "/mgg"
        "BULANAN" -> "/bln"
        else -> ""
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = glassmorphicBorderStroke(
            accentColor = statusColor,
            width = 1.1.dp,
            isSelected = isSelected
        ),
        modifier = modifier
            .height(110.dp)
            .glassmorphicCardEffect(
                cornerRadius = 16.dp,
                accentTint = statusColor
            )
            .testTag("category_tile_${item.key}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: Judul Kotak Kecil + Tombol Edit Limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = iconForCategory(item.key),
                        contentDescription = item.label,
                        tint = if (isSelected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = onEditLimit,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("edit_limit_${item.key}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Atur Limit ${item.label}",
                            tint = statusColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            val isEnBar = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
            val barCountry = WorldCountryCurrencyRegistry.getActiveProfile()

            // BAR 1: BAR BATAS LIMIT (Plafon 100%)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnBar) "Budget Limit" else "Batas Limit",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (limitAmt > 0L) {
                            "${ArusKasViewModel.formatCompactMoney(limitAmt).removePrefix("${barCountry.currencySymbol} ")}$shortPeriod"
                        } else {
                            if (isEnBar) "Free" else "Bebas"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.8.sp),
                        color = SkyBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SkyBlue.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(SkyBlue.copy(alpha = 0.7f), SkyBlue)
                                )
                            )
                    )
                }
            }

            // BAR 2: BAR LIMIT YANG SUDAH TERPAKAI
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnBar) "Spent" else "Terpakai",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (limitAmt > 0L) {
                            "${ArusKasViewModel.formatCompactMoney(spentAmount).removePrefix("${barCountry.currencySymbol} ")} ($pctUsed%)"
                        } else {
                            ArusKasViewModel.formatCompactMoney(spentAmount).removePrefix("${barCountry.currencySymbol} ")
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.8.sp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                LinearProgressIndicator(
                    progress = {
                        if (limitAmt > 0L) ratio.coerceIn(0f, 1f) else if (spentAmount > 0L) 0.35f else 0f
                    },
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
                Text(
                    text = if (limitAmt > 0L) {
                        if (spentAmount <= limitAmt) {
                            if (isEnBar) "Left: ${ArusKasViewModel.formatCompactMoney(remainingAmt)}" else "Sisa: ${ArusKasViewModel.formatCompactMoney(remainingAmt)}"
                        } else {
                            if (isEnBar) "Over: +${ArusKasViewModel.formatCompactMoney(spentAmount - limitAmt)}" else "Lewat: +${ArusKasViewModel.formatCompactMoney(spentAmount - limitAmt)}"
                        }
                    } else {
                        if (isEnBar) "Flexible Mode" else "Mode Fleksibel"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.4.sp),
                    color = if (limitAmt > 0L && spentAmount > limitAmt) CoralExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryLimitDialog(
    category: QuickCategoryItem,
    initialConfig: CategoryLimitConfig,
    onDismiss: () -> Unit,
    onSave: (limitAmount: Long, period: String) -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    var limitStr by remember(initialConfig) {
        mutableStateOf(if (initialConfig.limitAmount > 0L) initialConfig.limitAmount.toString() else "")
    }
    var selectedPeriod by remember(initialConfig) {
        mutableStateOf(initialConfig.period)
    }

    val periodOptions = if (isEn) {
        listOf(
            "BULANAN" to "Monthly",
            "MINGGUAN" to "Weekly",
            "HARIAN" to "Daily",
            "BEBAS" to "Flexible / No Cap"
        )
    } else {
        listOf(
            "BULANAN" to "Per Bulan",
            "MINGGUAN" to "Per Minggu",
            "HARIAN" to "Per Hari",
            "BEBAS" to "Semaunya / Bebas"
        )
    }
    val presetLimits = countryProfile.presetLimitOptions

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isEn) "Limit Target: ${category.label}" else "Target Limit: ${category.label}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEn) "Select Limit Period:" else "Pilih Periode Target Limit:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    periodOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = {
                                selectedPeriod = key
                                if (key == "BEBAS") limitStr = ""
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { input ->
                        val normalized = ArusKasViewModel.sanitizeMoneyInput(input)
                        limitStr = normalized
                        if (ArusKasViewModel.safeParseMoney(normalized) > 0L && selectedPeriod == "BEBAS") {
                            selectedPeriod = "BULANAN"
                        }
                    },
                    label = { Text(if (isEn) "Maximum Spending Limit (${countryProfile.currencyCode})" else "Batas Maksimal Pengeluaran (${countryProfile.currencyCode})") },
                    placeholder = { Text(if (isEn) "0 (Unlimited)" else "0 (Bebas / Tanpa batas)") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_limit_input")
                )

                Text(
                    text = if (isEn) "Quick Limit Presets:" else "Pilihan Cepat Nominal Limit:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLimits.forEach { preset ->
                        Surface(
                            onClick = {
                                limitStr = preset.toString()
                                if (selectedPeriod == "BEBAS") selectedPeriod = "BULANAN"
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (limitStr == preset.toString()) EmeraldPrimary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = ArusKasViewModel.formatMoney(preset),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (limitStr == preset.toString()) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isEn) {
                        "Status color updates automatically from SAFE (Green), NEAR LIMIT (Yellow ≥75%), to OVER LIMIT (Red ≥100%)."
                    } else {
                        "Tanda indikator akan otomatis berubah dari AMAN (Hijau), HAMPIR LIMIT (Kuning ≥75%), hingga LEWAT LIMIT! (Merah ≥100%)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = if (selectedPeriod == "BEBAS") 0L else ArusKasViewModel.safeParseMoney(limitStr)
                    onSave(amt, selectedPeriod)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color(0xFF042619)
                ),
                modifier = Modifier.testTag("save_category_limit_button")
            ) {
                Text(if (isEn) "Save Limit" else "Simpan Limit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "Batal")
            }
        }
    )
}

@Composable
private fun TabunganTargetRoundButton(
    activeGoal: SavingsGoal?,
    progress: Float,
    percent: Int,
    isSelected: Boolean,
    lang: AppLanguage = AppLanguage.INDONESIAN,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = glassmorphicBorderStroke(
            accentColor = EmeraldPrimary,
            width = 1.dp,
            isSelected = isSelected
        ),
        modifier = Modifier
            .glassmorphicCardEffect(cornerRadius = 14.dp, accentTint = EmeraldPrimary)
            .testTag("tabungan_target_minimalist_card")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Celengan Round Medallion with Progress Arc
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track and Progress Arc
                Canvas(modifier = Modifier.size(46.dp)) {
                    val strokeWidth = 3.dp.toPx()
                    drawCircle(
                        color = BcaRoyalBlue.copy(alpha = 0.16f),
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = BcaRoyalBlue,
                        startAngle = -90f,
                        sweepAngle = (progress.coerceIn(0f, 1f)) * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Round Piggy Bank / Celengan Coin
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) BcaRoyalBlue else BcaRoyalBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Coin slot decorative accent
                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .height(1.8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) Color.White else BcaElectricCyan)
                        )
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = AppLocalization.get("target_savings", lang),
                            tint = if (isSelected) Color.White else BcaRoyalBlue,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Text Label & Subtitle Info
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = AppLocalization.get("target_savings", lang),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BcaRoyalBlue.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = BcaRoyalBlue,
                            modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                        )
                    }
                }
                Text(
                    text = if (activeGoal != null) {
                        "${activeGoal.title.take(10)} • ${ArusKasViewModel.formatCompactMoney(activeGoal.savedAmount)}"
                    } else {
                        AppLocalization.get("target_create_hint", lang)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.2.sp),
                    color = BcaRoyalBlue,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = Color.Transparent,
        border = glassmorphicBorderStroke(accentColor = accentColor, width = 0.9.dp),
        modifier = modifier.glassmorphicCardEffect(cornerRadius = 11.dp, accentTint = accentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = value.removePrefix("${WorldCountryCurrencyRegistry.getActiveProfile().currencySymbol} "),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.6.sp),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = label,
                tint = accentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

private data class GenZDayStat(
    val dayShort: String,
    val fullDateLabel: String,
    val expenseAmount: Long,
    val incomeAmount: Long,
    val topCategoryLabel: String,
    val txCount: Int,
    val isToday: Boolean
)

@Composable
fun GraphicalExpenseSummaryCard(
    transactions: List<TransactionRecord>,
    mode: String,
    onModeChange: (String) -> Unit,
    onClose: () -> Unit,
    totalBalance: Long = 0L,
    totalTabunganSaved: Long = 0L,
    emergencyBalance: Long = 0L,
    categoryLimits: Map<String, CategoryLimitConfig> = PresetCategories.defaultCategoryLimits
) {
    val monthStart = remember { ArusKasViewModel.getStartOfMonthMillis() }
    val monthExpenses = remember(transactions, monthStart) {
        transactions.filter { it.type == "EXPENSE" && it.transactionDateMillis >= monthStart }
    }
    val monthIncomes = remember(transactions, monthStart) {
        transactions.filter { it.type == "INCOME" && it.transactionDateMillis >= monthStart }
    }
    val totalMonthExpense = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val totalMonthIncome = remember(monthIncomes) { monthIncomes.sumOf { it.amount } }

    val cCodeSummary = WorldCountryCurrencyRegistry.activeCountryCode
    val coffeeDivisor = if (cCodeSummary == "ID") 22_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCodeSummary).coerceAtLeast(1L)
    val mealDivisor = if (cCodeSummary == "ID") 18_000L else WorldCountryCurrencyRegistry.fromUsd(8.0, cCodeSummary).coerceAtLeast(1L)
    val fuelDivisor = if (cCodeSummary == "ID") 10_000L else WorldCountryCurrencyRegistry.fromUsd(4.0, cCodeSummary).coerceAtLeast(1L)
    val coffeeCupsEq = (totalMonthExpense / coffeeDivisor).coerceAtLeast(if (totalMonthExpense > 0L) 1L else 0L)
    val seblakBowlsEq = (totalMonthExpense / mealDivisor).coerceAtLeast(if (totalMonthExpense > 0L) 1L else 0L)
    val bensinLitersEq = (totalMonthExpense / fuelDivisor).coerceAtLeast(if (totalMonthExpense > 0L) 1L else 0L)

    // Battery / Survival Meter Calculation
    val totalPool = (totalBalance + totalMonthExpense).coerceAtLeast(1L)
    val batteryPct = ((totalBalance.toFloat() / totalPool.toFloat()) * 100f).toInt().coerceIn(0, 100)
    val batteryColor = when {
        batteryPct >= 60 -> EmeraldPrimary
        batteryPct >= 30 -> GoldAccent
        else -> CoralExpense
    }
    val batteryVibeText = when {
        batteryPct >= 70 -> "🔋 $batteryPct% Full Senyum (Masih Seger)"
        batteryPct >= 40 -> "⚡ $batteryPct% Mode Waspada (Rem Jajan)"
        else -> "🪫 $batteryPct% Lowbat Parah (Mode Hemat Darurat!)"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
        ),
        border = BorderStroke(1.2.dp, batteryColor.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("genz_graphical_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. HEADER: RADAR DOMPET GEN Z + TOMBOL TUTUP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ RADAR VISUAL DOMPET LO",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = batteryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = when (mode) {
                            "HARIAN" -> "Mood Bakar Duit 7 Hari Terakhir"
                            "ADU_CUAN" -> "Battle: Duit Masuk vs Duit Keluar"
                            else -> "Peta Dompet Bocor Bulan Ini"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    onClick = onClose,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Tutup ✕",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. BATERAI NYAWA DOMPET & KONVERSI KEHIDUPAN NYATA (Kopi, Seblak, Bensin)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                border = BorderStroke(0.8.dp, batteryColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nyawa Dompet Harian Lo:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = batteryVibeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                            color = batteryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Segmented Battery Capsule Bar (10 kapsul baterai biar gampang kebayang)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val filledSegments = (batteryPct / 10).coerceIn(1, 10)
                        for (i in 1..10) {
                            val active = i <= filledSegments
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (active) batteryColor else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    // Konversi Kehidupan Sehari-hari ("Pengeluaran bulan ini setara apa sih?")
                    Text(
                        text = "Bulan ini lo udah ngeluarin ${ArusKasViewModel.formatCompactMoney(totalMonthExpense)}, itu setara sama:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RealLifeEquivalentBadge(
                            modifier = Modifier.weight(1f),
                            emoji = "☕",
                            value = "$coffeeCupsEq Cup",
                            subtitle = "Es Kopi Susu",
                            accent = GoldAccent
                        )
                        RealLifeEquivalentBadge(
                            modifier = Modifier.weight(1f),
                            emoji = "🍜",
                            value = "$seblakBowlsEq Porsi",
                            subtitle = "Seblak / Mie",
                            accent = CoralExpense
                        )
                        RealLifeEquivalentBadge(
                            modifier = Modifier.weight(1f),
                            emoji = "⛽",
                            value = "$bensinLitersEq Liter",
                            subtitle = "Bensin Motor",
                            accent = SkyBlue
                        )
                    }
                }
            }

            // 3. PILIHAN MODE VISUAL GEN Z (3 Tab yang Gampang Dipahami)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "HARIAN" to "🔥 7 Hari Vibe",
                    "BULANAN" to "🕳️ Peta Bocor",
                    "ADU_CUAN" to "⚡ Cuan vs Boncos"
                )
                tabs.forEach { (tabKey, tabLabel) ->
                    val isSelected = mode == tabKey
                    Surface(
                        onClick = { onModeChange(tabKey) },
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSelected) GoldAccent else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tabLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected) Color(0xFF1F1400) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            // 4. ISI GRAFIK SESUAI TAB YANG DIPILIH
            when (mode) {
                "HARIAN" -> {
                    val dailyStats = remember(transactions) {
                        val dayShortFmt = SimpleDateFormat("EEE", Locale("in", "ID"))
                        val fullDateFmt = SimpleDateFormat("EEEE, dd MMM", Locale("in", "ID"))
                        (6 downTo 0).map { daysAgo ->
                            val c = Calendar.getInstance()
                            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
                            c.set(Calendar.HOUR_OF_DAY, 0)
                            c.set(Calendar.MINUTE, 0)
                            c.set(Calendar.SECOND, 0)
                            c.set(Calendar.MILLISECOND, 0)
                            val start = c.timeInMillis
                            val end = start + 86_400_000L
                            val dayTxs = transactions.filter { it.transactionDateMillis in start until end }
                            val expTxs = dayTxs.filter { it.type == "EXPENSE" }
                            val incTxs = dayTxs.filter { it.type == "INCOME" }
                            val sumExp = expTxs.sumOf { it.amount }
                            val sumInc = incTxs.sumOf { it.amount }
                            val topCat = expTxs
                                .groupBy { it.categoryLabel.ifBlank { "Pengeluaran" } }
                                .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                                ?.key ?: "Nggak ada jajan"
                            GenZDayStat(
                                dayShort = if (daysAgo == 0) "Skrg" else dayShortFmt.format(Date(start)),
                                fullDateLabel = if (daysAgo == 0) "Hari Ini (${fullDateFmt.format(Date(start))})" else fullDateFmt.format(Date(start)),
                                expenseAmount = sumExp,
                                incomeAmount = sumInc,
                                topCategoryLabel = topCat,
                                txCount = expTxs.size,
                                isToday = daysAgo == 0
                            )
                        }
                    }

                    // Pilih hari untuk di-inspect (default hari ini / index terakhir)
                    var selectedDayIndex by remember { mutableStateOf(6) }
                    val selectedDay = dailyStats.getOrElse(selectedDayIndex) { dailyStats.last() }

                    val cProfileChart = WorldCountryCurrencyRegistry.getActiveProfile()
                    val cCodeChart = cProfileChart.countryCode
                    val safeDailyLimit = if (cCodeChart == "ID") 100_000L else WorldCountryCurrencyRegistry.fromUsd(30.0, cCodeChart).coerceAtLeast(1L)
                    val lowDailyLimit = if (cCodeChart == "ID") 50_000L else WorldCountryCurrencyRegistry.fromUsd(15.0, cCodeChart).coerceAtLeast(1L)
                    val maxVal = maxOf(dailyStats.maxOfOrNull { it.expenseAmount } ?: 1L, safeDailyLimit, 1L)
                    val avgDaily = dailyStats.sumOf { it.expenseAmount } / 7L

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ketuk tiang hari buat liat detail bocornya 👇",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rata-rata: ${ArusKasViewModel.formatCompactMoney(avgDaily)}/hr",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bar Chart Kapsul Modern dengan Emoji Mood di Atas Tiap Hari
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(136.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dailyStats.forEachIndexed { idx, stat ->
                                val isSel = idx == selectedDayIndex
                                val ratio = (stat.expenseAmount.toFloat() / maxVal.toFloat()).coerceIn(0.08f, 1f)
                                val moodEmoji = when {
                                    stat.expenseAmount == 0L -> "🧊"
                                    stat.expenseAmount <= lowDailyLimit -> "😎"
                                    stat.expenseAmount <= safeDailyLimit -> "☕"
                                    else -> "🔥"
                                }
                                val barColor = when {
                                    stat.expenseAmount == 0L -> SkyBlue.copy(alpha = 0.45f)
                                    stat.expenseAmount <= lowDailyLimit -> EmeraldPrimary
                                    stat.expenseAmount <= safeDailyLimit -> GoldAccent
                                    else -> CoralExpense
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSel) barColor.copy(alpha = 0.14f) else Color.Transparent
                                        )
                                        .clickable { selectedDayIndex = idx }
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = moodEmoji,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                    )
                                    Text(
                                        text = if (stat.expenseAmount > 0L) {
                                            ArusKasViewModel.formatCompactMoney(stat.expenseAmount).removePrefix("${cProfileChart.currencySymbol} ")
                                        } else {
                                            "0"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = if (isSel) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(if (isSel) 22.dp else 18.dp)
                                            .height((64 * ratio).dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        barColor,
                                                        barColor.copy(alpha = 0.45f)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stat.dayShort,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                                        color = if (isSel || stat.isToday) barColor else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSel || stat.isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Keterangan Emoji & Detail Hari yang Dipilih
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📌 ${selectedDay.fullDateLabel}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (selectedDay.expenseAmount > 0L) {
                                            "Keluar ${ArusKasViewModel.formatMoney(selectedDay.expenseAmount)} (${selectedDay.txCount}x) • Terbanyak: ${selectedDay.topCategoryLabel}"
                                        } else {
                                            "🧊 Nggak keluar duit blas di hari ini! Dompet full senyum."
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.6.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (selectedDay.incomeAmount > 0L) {
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "+${ArusKasViewModel.formatCompactMoney(selectedDay.incomeAmount)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.2.sp),
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Arti Mood: 🧊 ${ArusKasViewModel.formatMoney(0L)} (Puasa Jajan) • 😎 ≤${ArusKasViewModel.formatCompactMoney(lowDailyLimit)} (Santuy) • ☕ ≤${ArusKasViewModel.formatCompactMoney(safeDailyLimit)} (Wajar) • 🔥 >${ArusKasViewModel.formatCompactMoney(safeDailyLimit)} (Boros!)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                "ADU_CUAN" -> {
                    // TAB 3: BATTLE DUIT MASUK VS DUIT KELUAR VS TABUNGAN
                    val totalFlow = (totalMonthIncome + totalMonthExpense + totalTabunganSaved).coerceAtLeast(1L)
                    val netDiff = totalMonthIncome - totalMonthExpense
                    val isSurplus = netDiff >= 0L

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (isSurplus) EmeraldPrimary else CoralExpense).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, (if (isSurplus) EmeraldPrimary else CoralExpense).copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isSurplus) {
                                        "🏆 LO MENANG BULAN INI! (Surplus +${ArusKasViewModel.formatMoney(netDiff)})"
                                    } else {
                                        "💀 AWAS KENA COMEBACK! (Besar Pasak dari Tiang -${ArusKasViewModel.formatMoney(kotlin.math.abs(netDiff))})"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSurplus) EmeraldPrimary else CoralExpense,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isSurplus) {
                                        "Pemasukan lo lebih gede dari pengeluaran cuy! Pertahanin biar sisa uangnya bisa langsung diamankan ke Tabungan Target."
                                    } else {
                                        "Woy pengeluaran bulan ini udah ngebalap uang masuk lo! Stop checkout yang nggak penting sekarang juga."
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.6.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Tug-of-War Visual Bar
                        BattleComparisonRow(
                            label = "🟢 Cuan Masuk (Gaji, Ngojek, Usaha)",
                            amount = totalMonthIncome,
                            ratio = (totalMonthIncome.toFloat() / totalFlow.toFloat()).coerceIn(0.05f, 1f),
                            color = EmeraldPrimary
                        )
                        BattleComparisonRow(
                            label = "🔴 Duit Keluar (Jajan, Pokok, Tagihan)",
                            amount = totalMonthExpense,
                            ratio = (totalMonthExpense.toFloat() / totalFlow.toFloat()).coerceIn(0.05f, 1f),
                            color = CoralExpense
                        )
                        BattleComparisonRow(
                            label = "🛡️ Diamankan (Tabungan Target + Darurat)",
                            amount = totalTabunganSaved + emergencyBalance,
                            ratio = ((totalTabunganSaved + emergencyBalance).toFloat() / (totalFlow + emergencyBalance).coerceAtLeast(1L).toFloat()).coerceIn(0.05f, 1f),
                            color = GoldAccent
                        )
                    }
                }

                else -> {
                    // TAB 2 ("BULANAN"): PETA BOCOR DOMPET (Stacked DNA Bar + Top Tersangka + Real-Life Analogy)
                    val grouped = remember(monthExpenses) {
                        monthExpenses
                            .groupBy { it.categoryKey }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                    }
                    val totalMonth = grouped.values.sum().coerceAtLeast(1L)
                    val catColors = listOf(
                        SkyBlue,
                        EmeraldPrimary,
                        Color(0xFFE879F9),
                        CoralExpense,
                        GoldAccent,
                        Color(0xFF38BDF8)
                    )

                    val sortedCategories = remember(grouped) {
                        PresetCategories.expenseGrid3x3.mapIndexed { idx, cat ->
                            Triple(cat, grouped[cat.key] ?: 0L, catColors[idx % catColors.size])
                        }.sortedByDescending { it.second }
                    }
                    val topCulprit = sortedCategories.firstOrNull()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Top Tersangka Utama Callout
                        if (topCulprit != null && topCulprit.second > 0L) {
                            val topPct = ((topCulprit.second * 100L) / totalMonth).toInt()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CoralExpense.copy(alpha = 0.12f),
                                border = BorderStroke(0.9.dp, CoralExpense.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🚨", fontSize = 16.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "TERSANGKA #1 DOMPET BOCOR: ${topCulprit.first.label.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                                            color = CoralExpense,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Nyerap $topPct% (${ArusKasViewModel.formatMoney(topCulprit.second)}) dari seluruh pengeluaran bulan ini!",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.4.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Single Multi-Color DNA Bar (Proporsi semua kategori dalam 1 bar utuh)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            sortedCategories.forEach { (_, amt, color) ->
                                val weightVal = (amt.toFloat() / totalMonth.toFloat()).coerceAtLeast(if (amt > 0L) 0.04f else 0f)
                                if (weightVal > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(weightVal)
                                            .fillMaxSize()
                                            .background(color)
                                    )
                                }
                            }
                        }

                        // Daftar Kategori dengan Real-Life Analogy & Status Limit
                        val cCodeAnalogy = WorldCountryCurrencyRegistry.activeCountryCode
                        val coffeeUnit = if (cCodeAnalogy == "ID") 22_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCodeAnalogy).coerceAtLeast(1L)
                        val fuelUnit = if (cCodeAnalogy == "ID") 10_000L else WorldCountryCurrencyRegistry.fromUsd(4.0, cCodeAnalogy).coerceAtLeast(1L)
                        val careUnit = if (cCodeAnalogy == "ID") 50_000L else WorldCountryCurrencyRegistry.fromUsd(15.0, cCodeAnalogy).coerceAtLeast(1L)
                        val miscUnit = if (cCodeAnalogy == "ID") 20_000L else WorldCountryCurrencyRegistry.fromUsd(10.0, cCodeAnalogy).coerceAtLeast(1L)
                        sortedCategories.forEach { (cat, amt, color) ->
                            val pct = ((amt.toFloat() / totalMonth.toFloat()) * 100f).toInt().coerceIn(0, 100)
                            val limitCfg = categoryLimits[cat.key]
                            val limitAmt = limitCfg?.limitAmount ?: cat.defaultLimitAmount
                            val limitRatio = if (limitAmt > 0L) (amt.toFloat() / limitAmt.toFloat()) else 0f
                            val realLifeHint = when (cat.key) {
                                "gaya_hidup" -> "≈ ${(amt / coffeeUnit)} cup kopi"
                                "kebutuhan_pokok" -> "≈ ${(amt / fuelUnit)}L bensin/pokok"
                                "rawat_diri" -> "≈ ${(amt / careUnit)} item perawatan"
                                "tagihan_bulanan" -> "tagihan wajib"
                                else -> "≈ ${(amt / miscUnit)}x transaksi"
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = cat.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.2.sp),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "($pct% • $realLifeHint)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = ArusKasViewModel.formatCompactMoney(amt),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.2.sp),
                                            color = color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { (amt.toFloat() / totalMonth.toFloat()).coerceIn(0f, 1f) },
                                        color = color,
                                        trackColor = color.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50))
                                    )

                                    if (limitAmt > 0L) {
                                        Text(
                                            text = "Target Limit Kategori: ${(limitRatio * 100).toInt()}% dari ${ArusKasViewModel.formatCompactMoney(limitAmt)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.8.sp),
                                            color = if (limitRatio >= 1f) CoralExpense else MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun RealLifeEquivalentBadge(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.1f),
        border = BorderStroke(0.7.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$emoji $value",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BattleComparisonRow(
    label: String,
    amount: Long,
    ratio: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = ArusKasViewModel.formatMoney(amount),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.16f),
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

@Composable
fun TabunganTargetVisualRadarCard(
    activeGoal: SavingsGoal?,
    allGoals: List<SavingsGoal>,
    wallets: List<Wallet>,
    transactions: List<TransactionRecord>,
    mode: String,
    onModeChange: (String) -> Unit,
    onSelectGoal: (SavingsGoal) -> Unit,
    onAllocateSavings: (goal: SavingsGoal, wallet: Wallet, amount: Long, note: String) -> Unit,
    onOpenCreateTarget: () -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    onClose: () -> Unit
) {
    val cProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val cCode = cProfile.countryCode
    val coffeeUnit = if (cCode == "ID") 22_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCode).coerceAtLeast(1L)
    val defaultDailyFallback = if (cCode == "ID") 40_000L else WorldCountryCurrencyRegistry.fromUsd(10.0, cCode).coerceAtLeast(1L)

    val saved = activeGoal?.savedAmount ?: 0L
    val target = (activeGoal?.targetAmount ?: 1L).coerceAtLeast(1L)
    val remaining = (target - saved).coerceAtLeast(0L)
    val progress = if (activeGoal != null) (saved.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    val goalPct = (progress * 100f).toInt().coerceIn(0, 100)
    val dailyRec = (activeGoal?.dailyRecommendation ?: defaultDailyFallback).coerceAtLeast(1L)

    // Real-life Gen Z conversions for the target
    val coffeeCupsRemaining = (remaining / coffeeUnit).coerceAtLeast(if (remaining > 0L) 1L else 0L)
    val daysLeftEstimate = if (remaining > 0L) ceil(remaining.toDouble() / dailyRec.toDouble()).toInt() else 0
    val totalAllTargetsSaved = remember(allGoals) { allGoals.sumOf { it.savedAmount } }

    val radarAccent = when {
        activeGoal == null -> GoldAccent
        goalPct >= 100 -> EmeraldPrimary
        goalPct >= 50 -> EmeraldPrimary
        else -> GoldAccent
    }
    val statusVibeText = when {
        activeGoal == null -> "🎯 Belum ada target, gas bikin dulu!"
        remaining <= 0L -> "🎉 100% LUNAS! Siap checkout impian lo!"
        goalPct >= 75 -> "🔥 $goalPct% Dikit lagi kebeli cuy!"
        goalPct >= 30 -> "⚡ $goalPct% On Track! Jangan kendor!"
        else -> "🚀 $goalPct% Baru mulai, konsisten ya bro!"
    }

    var selectedWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var depositAmountStr by remember(activeGoal) {
        mutableStateOf(dailyRec.toString())
    }
    var depositNoteStr by remember(activeGoal) {
        mutableStateOf("Tabungan Target: ${activeGoal?.title ?: "Impian"} (Simpan di Rekening Terpisah)")
    }

    val savingsHistory = remember(transactions) {
        transactions.filter { it.type == "SAVINGS_ALLOCATION" }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("in", "ID")) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
        ),
        border = BorderStroke(1.2.dp, radarAccent.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("genz_target_visual_radar_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. HEADER: RADAR VISUAL TABUNGAN TARGET + TOMBOL + TARGET & TUTUP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎯 RADAR VISUAL TABUNGAN TARGET LO",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = radarAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (activeGoal != null) {
                            "Target Impian: ${activeGoal.title}"
                        } else {
                            "Mulai Rakit Tabungan Impian Lo"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onOpenCreateTarget,
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.18f),
                        border = BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("quick_add_target_button")
                    ) {
                        Text(
                            text = "+ Target",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = onClose,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Tutup ✕",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Switcher kalau punya lebih dari 1 target
            if (allGoals.size > 1 && activeGoal != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allGoals.forEach { g ->
                        val isSel = g.id == activeGoal.id
                        val gPct = ((g.savedAmount.toFloat() / g.targetAmount.coerceAtLeast(1L).toFloat()) * 100f).toInt().coerceIn(0, 100)
                        Surface(
                            onClick = { onSelectGoal(g) },
                            shape = RoundedCornerShape(50),
                            color = if (isSel) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🎯 ${g.title} ($gPct%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (activeGoal == null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lo belum punya Tabungan Target aktif nih cuy!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "Bikin target (misal: Beli Laptop, Upgrade HP, atau Liburan) supaya uang tabungan lo terpisah dari saldo jajan harian.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onOpenCreateTarget,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color(0xFF1F1400)
                            )
                        ) {
                            Text("+ Buat Target Impian Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // 2. BATERAI PROGRESS IMPIAN & KONVERSI KEHIDUPAN NYATA (Persis Seperti UI/UX Grafik!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(0.8.dp, radarAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Terkumpul: ${ArusKasViewModel.formatCompactMoney(saved)} / ${ArusKasViewModel.formatCompactMoney(target)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = statusVibeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = radarAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 10-Capsule Segmented Battery Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            val filledSegments = if (goalPct == 0) 0 else (goalPct / 10).coerceIn(1, 10)
                            for (i in 1..10) {
                                val active = i <= filledSegments
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (active) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                )
                            }
                        }

                        Text(
                            text = if (remaining > 0L) {
                                "Kurang ${ArusKasViewModel.formatMoney(remaining)} lagi • ⚠️ Simpan di rekening/kantong terpisah dari uang jajan!"
                            } else {
                                "Target udah 100% terkumpul aman di kantong tabungan terpisah!"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.2.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 3 Real-Life Equivalent Badges (Kopi, Estimasi Hari, Total Diamankan)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RealLifeEquivalentBadge(
                                modifier = Modifier.weight(1f),
                                emoji = "☕",
                                value = "$coffeeCupsRemaining Cup",
                                subtitle = "Sisa Setara Kopi",
                                accent = GoldAccent
                            )
                            RealLifeEquivalentBadge(
                                modifier = Modifier.weight(1f),
                                emoji = "⏳",
                                value = if (daysLeftEstimate > 0) "±$daysLeftEstimate Hari" else "Lunas!",
                                subtitle = "Estimasi Lunas",
                                accent = SkyBlue
                            )
                            RealLifeEquivalentBadge(
                                modifier = Modifier.weight(1f),
                                emoji = "🛡️",
                                value = ArusKasViewModel.formatCompactMoney(totalAllTargetsSaved),
                                subtitle = "Aman Terpisah",
                                accent = EmeraldPrimary
                            )
                        }
                    }
                }

                // 3. PILIHAN MODE VISUAL TABUNGAN TARGET (3 Tab Interaktif Persis Seperti Grafik)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val targetTabs = listOf(
                        "SETOR" to "⚡ Gas Setor",
                        "SIMULASI" to "🚀 Simulasi Lunas",
                        "KELOLA" to "📜 Jejak & Kelola"
                    )
                    targetTabs.forEach { (tabKey, tabLabel) ->
                        val isSelected = mode == tabKey
                        Surface(
                            onClick = { onModeChange(tabKey) },
                            shape = RoundedCornerShape(9.dp),
                            color = if (isSelected) EmeraldPrimary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tabLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isSelected) Color(0xFF042619) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                // 4. ISI KONTEN SESUAI TAB RADAR TABUNGAN TARGET
                when (mode) {
                    "SIMULASI" -> {
                        // TAB 2: SIMULASI KECEPATAN LUNAS ALA GEN Z (Bar Perbandingan Visual)
                        val paceSantuy = if (cCode == "ID") 20_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCode).coerceAtLeast(1L)
                        val paceNormal = dailyRec.coerceAtLeast(paceSantuy)
                        val paceSultan = (dailyRec * 2L).coerceAtLeast(paceNormal * 2L)

                        val daysSantuy = if (remaining > 0L) ceil(remaining.toDouble() / paceSantuy.toDouble()).toInt() else 0
                        val daysNormal = if (remaining > 0L) ceil(remaining.toDouble() / paceNormal.toDouble()).toInt() else 0
                        val daysSultan = if (remaining > 0L) ceil(remaining.toDouble() / paceSultan.toDouble()).toInt() else 0

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(0.9.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "💡 Kapan ${activeGoal.title} Lo Bakal Kebeli?",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Makin rajin lo nahan jajan nggak penting & mindahin ke rekening tabungan terpisah, makin cepet target ini lunas cuy!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            BattleComparisonRow(
                                label = "🐢 Mode Santuy (${ArusKasViewModel.formatCompactMoney(paceSantuy)}/hr ≈ Tahan 1 Kopi) → ±$daysSantuy Hari",
                                amount = paceSantuy,
                                ratio = 0.42f,
                                color = SkyBlue
                            )
                            BattleComparisonRow(
                                label = "⚡ Mode Ideal (${ArusKasViewModel.formatCompactMoney(paceNormal)}/hr ≈ Rekomendasi) → ±$daysNormal Hari",
                                amount = paceNormal,
                                ratio = 0.72f,
                                color = GoldAccent
                            )
                            BattleComparisonRow(
                                label = "🔥 Mode Ngebut (${ArusKasViewModel.formatCompactMoney(paceSultan)}/hr ≈ Gas Pol) → ±$daysSultan Hari",
                                amount = paceSultan,
                                ratio = 1f,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    "KELOLA" -> {
                        // TAB 3: JEJAK SETORAN & KELOLA TARGET
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jejak Setoran Tabungan Terakhir:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.8.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = { onDeleteGoal(activeGoal) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Hapus Target Ini",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                        color = CoralExpense,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (savingsHistory.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Belum ada riwayat setoran tabungan tercatat. Yuk mulai setor dari tab '⚡ Gas Setor'!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            } else {
                                savingsHistory.take(4).forEach { tx ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tx.note.ifBlank { tx.categoryLabel },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Potong dari ${tx.walletName} • ${dateFormat.format(Date(tx.transactionDateMillis))}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "+${ArusKasViewModel.formatCompactMoney(tx.amount)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.2.sp),
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // TAB 1 ("SETOR"): GAS SETOR TABUNGAN LANGSUNG DI DALAM KARTU RADAR
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Pilihan Cepat Nominal Setoran ala Gen Z
                            val quickCoffeeAmt = if (cCode == "ID") 25_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCode).coerceAtLeast(1L)
                            val quickGasAmt = if (cCode == "ID") 100_000L else WorldCountryCurrencyRegistry.fromUsd(25.0, cCode).coerceAtLeast(1L)
                            val quickAmounts = listOf(
                                quickCoffeeAmt to "☕ Setara 1 Kopi (${ArusKasViewModel.formatCompactMoney(quickCoffeeAmt)})",
                                dailyRec to "⚡ Jatah Harian (${ArusKasViewModel.formatCompactMoney(dailyRec)})",
                                quickGasAmt to "🔥 Gas Pol (${ArusKasViewModel.formatCompactMoney(quickGasAmt)})"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickAmounts.forEach { (amtVal, labelStr) ->
                                    val isChosen = depositAmountStr == amtVal.toString()
                                    Surface(
                                        onClick = { depositAmountStr = amtVal.toString() },
                                        shape = RoundedCornerShape(50),
                                        color = if (isChosen) GoldAccent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            0.9.dp,
                                            if (isChosen) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                        )
                                    ) {
                                        Text(
                                            text = labelStr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.4.sp),
                                            color = if (isChosen) GoldAccent else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Pilih Potong dari Dompet Mana
                            if (wallets.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    wallets.forEach { w ->
                                        val isSel = w.id == selectedWalletId
                                        Surface(
                                            onClick = { selectedWalletId = w.id },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${w.name}: ${ArusKasViewModel.formatCompactMoney(w.balance)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                                    color = if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Input Nominal Setor & Tombol Eksekusi Potong Saldo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = depositAmountStr,
                                    onValueChange = { depositAmountStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                                    label = { Text("Nominal Tabung (${cProfile.currencyCode})") },
                                    prefix = { Text("${cProfile.currencySymbol} ", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
                                    visualTransformation = RupiahThousandsVisualTransformation(cProfile.thousandsSeparator),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("deposit_target_amount_input")
                                )

                                Button(
                                    onClick = {
                                        val w = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                                        val amt = ArusKasViewModel.safeParseMoney(depositAmountStr)
                                        if (w != null && amt > 0L) {
                                            onAllocateSavings(activeGoal, w, amt, depositNoteStr)
                                        }
                                    },
                                    enabled = wallets.isNotEmpty() && ArusKasViewModel.safeParseMoney(depositAmountStr) > 0L,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color(0xFF042619)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("confirm_deposit_target_button")
                                ) {
                                    Text(
                                        text = "Potong & Tabung",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun TransactionRowCard(
    tx: TransactionRecord,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == "INCOME"
    val isSavings = tx.type == "SAVINGS_ALLOCATION"
    val isTransfer = tx.type == "TRANSFER"
    val amountColor = when {
        isIncome -> EmeraldPrimary
        isSavings -> GoldAccent
        isTransfer -> SkyBlue
        else -> CoralExpense
    }
    val prefix = when {
        isIncome -> "+"
        isTransfer -> "⇄ "
        else -> "-"
    }
    val dateStr = remember(tx.transactionDateMillis) {
        SimpleDateFormat("dd MMM, HH:mm", Locale("in", "ID")).format(Date(tx.transactionDateMillis))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = glassmorphicBorderStroke(accentColor = amountColor, width = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphicCardEffect(cornerRadius = 16.dp, accentTint = amountColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = amountColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconForCategory(tx.categoryKey),
                            contentDescription = tx.categoryLabel,
                            tint = amountColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = tx.note.ifBlank { tx.categoryLabel },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tx.categoryLabel} • ${tx.walletName} • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${ArusKasViewModel.formatMoney(tx.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus transaksi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class BestieFinancialInsight(
    val saldoSymbolColor: Color,
    val saldoBadge: String,
    val saldoComment: String,
    val nominalSymbolColor: Color,
    val notificationBadge: String,
    val nominalBoxAnalysis: String,
    val categorySymbolColor: Color,
    val categoryComment: String
)

fun buildDynamicBestieInsight(
    totalBalance: Long,
    totalTabunganSaved: Long,
    emergencyBalance: Long,
    todayExpense: Long,
    monthExpense: Long,
    monthIncome: Long,
    selectedCategory: QuickCategoryItem,
    categoryLimits: Map<String, CategoryLimitConfig>,
    transactions: List<TransactionRecord>,
    goals: List<SavingsGoal>,
    sessionSeed: Int,
    guruState: GuruAdviceState,
    mandatoryBills: List<MandatoryBillItem> = emptyList()
): BestieFinancialInsight {
    val cCodeInsight = WorldCountryCurrencyRegistry.activeCountryCode
    val saldoLowThreshold = if (cCodeInsight == "ID") 150_000L else WorldCountryCurrencyRegistry.fromUsd(30.0, cCodeInsight).coerceAtLeast(1L)
    val todayHighThreshold = if (cCodeInsight == "ID") 150_000L else WorldCountryCurrencyRegistry.fromUsd(30.0, cCodeInsight).coerceAtLeast(1L)
    val todayNoteThreshold = if (cCodeInsight == "ID") 100_000L else WorldCountryCurrencyRegistry.fromUsd(25.0, cCodeInsight).coerceAtLeast(1L)
    val suggestLow = if (cCodeInsight == "ID") 25_000L else WorldCountryCurrencyRegistry.fromUsd(5.0, cCodeInsight).coerceAtLeast(1L)
    val suggestHigh = if (cCodeInsight == "ID") 40_000L else WorldCountryCurrencyRegistry.fromUsd(10.0, cCodeInsight).coerceAtLeast(1L)

    val activeGoal = goals.firstOrNull()
    val goalTitle = activeGoal?.title ?: "Target Impian"
    val goalRemaining = if (activeGoal != null) (activeGoal.targetAmount - activeGoal.savedAmount).coerceAtLeast(0L) else 0L

    // 1. Kotak Total Saldo evaluation
    val (saldoColor, saldoBadge, localSaldoComments) = when {
        totalBalance <= saldoLowThreshold -> Triple(
            CoralExpense,
            "🔴 SALDO TIPIS",
            listOf(
                "Waduh bestie, saldo siap pakai tinggal ${ArusKasViewModel.formatCompactMoney(totalBalance)} nih, mode bertahan hidup aktif ya! 😂",
                "Dompet harian lagi diet ketat bro (${ArusKasViewModel.formatCompactMoney(totalBalance)}), jangan coba-coba lirik promo dulu!",
                "Sisa ${ArusKasViewModel.formatCompactMoney(totalBalance)} di kantong aktif, untung tabungan & darurat udah dipisah aman!"
            )
        )
        totalTabunganSaved == 0L && activeGoal != null -> Triple(
            GoldAccent,
            "🟡 TARGET BELUM DIISI",
            listOf(
                "Duit siap pakai ${ArusKasViewModel.formatCompactMoney(totalBalance)} ngumpul semua nih bro, sisihin dikit ke Tabungan $goalTitle yuk!",
                "Dompet cukup aman (${ArusKasViewModel.formatCompactMoney(totalBalance)}), yuk mulai cicil saldo ke $goalTitle biar target makin dekat!",
                "Yuk amankan sebagian dari ${ArusKasViewModel.formatCompactMoney(totalBalance)} ke kantong Tabungan $goalTitle!"
            )
        )
        todayExpense > todayHighThreshold -> Triple(
            GoldAccent,
            "🟡 REM DIKIT BRO",
            listOf(
                "Hari ini udah keluar ${ArusKasViewModel.formatCompactMoney(todayExpense)} bestie, rem dikit biar saldo ${ArusKasViewModel.formatCompactMoney(totalBalance)} awet!",
                "Jempolmu lagi lincah banget hari ini (${ArusKasViewModel.formatCompactMoney(todayExpense)}), kasihan dompet kalau digas terus 😂",
                "Saldo masih ${ArusKasViewModel.formatCompactMoney(totalBalance)}, tapi pengeluaran hari ini udah lumayan ngebut nih bro!"
            )
        )
        else -> Triple(
            EmeraldPrimary,
            "🟢 DOMPET SEGER",
            listOf(
                "Saldo siap pakai ${ArusKasViewModel.formatCompactMoney(totalBalance)} aman terkendali bosku, tabungan & darurat juga udah rapi!",
                "Nah gini dong bestie, duit harian ${ArusKasViewModel.formatCompactMoney(totalBalance)} jelas posnya, tidur pun nyenyak 😎",
                "Pertahankan bro! Saldo aktif ${ArusKasViewModel.formatCompactMoney(totalBalance)} sehat, tinggal gas cicil $goalTitle pelan-pelan."
            )
        )
    }

    // 2. Category evaluation
    val isIncome = selectedCategory.type == "INCOME"
    val activeLimitCfg = categoryLimits[selectedCategory.key] ?: CategoryLimitConfig(
        categoryKey = selectedCategory.key,
        limitAmount = selectedCategory.defaultLimitAmount,
        period = selectedCategory.defaultLimitPeriod
    )
    val catSpent = ArusKasViewModel.calculateSpentForCategoryLimit(
        categoryKey = selectedCategory.key,
        period = activeLimitCfg.period,
        transactions = transactions,
        mandatoryBills = mandatoryBills,
        startDateMillis = activeLimitCfg.startDateMillis,
        endDateMillis = activeLimitCfg.endDateMillis
    )
    val catLimit = activeLimitCfg.limitAmount
    val catRatio = if (catLimit > 0L) (catSpent.toFloat() / catLimit.toFloat()) else 0f

    val (catColor, catComment) = if (isIncome) {
        EmeraldPrimary to when (sessionSeed % 3) {
            0 -> "🟢 Mantap bestie! Begitu uang ${selectedCategory.label} masuk, langsung amankan jatah $goalTitle sebelum numpang lewat doang 😆"
            1 -> "🟢 Rezeki ${selectedCategory.label} mengalir nih bro! Sisihkan minimal 10-20% ke kantong terpisah biar masa depan cerah 🔥"
            else -> "🟢 Cuan dari ${selectedCategory.label} siap dicatat! Ingat rumus orang kaya: nabung di awal, bukan nunggu sisa jajan ya bosku!"
        }
    } else {
        when {
            catLimit > 0L && catRatio >= 1f -> CoralExpense to "🔴 ALARM MERAH BESTIE! Pos ${selectedCategory.label} udah jebol ${(catRatio * 100).toInt()}% dari limit, stop dulu kalau nggak darurat banget 😭"
            catLimit > 0L && catRatio >= 0.75f -> GoldAccent to "🟡 Awas lampu kuning bro! Pos ${selectedCategory.label} udah kepake ${(catRatio * 100).toInt()}%, tahan godaan checkout biar nggak over limit!"
            catLimit > 0L && catRatio >= 0.4f -> SkyBlue to "🔵 Pos ${selectedCategory.label} baru jalan ${(catRatio * 100).toInt()}% sih bestie, masih aman asal jangan lapar mata pas jam rawan 🤣"
            else -> EmeraldPrimary to "🟢 Pos ${selectedCategory.label} masih hijau seger (${(catRatio * 100).toInt()}% dari limit). Tetap bijak ya bro demi kejar $goalTitle!"
        }
    }

    // 3. Minimalist Rectangular Analysis Box below Nominal Input (Rotates every time the app opens!)
    val localAnalysisVariations = listOf(
        Triple(
            if (todayExpense > todayNoteThreshold) GoldAccent else EmeraldPrimary,
            "🔔 PENGINGAT BESTIE #${(sessionSeed % 9) + 1}",
            "Bro, rasio kas aktifmu sekarang ${ArusKasViewModel.formatCompactMoney(totalBalance)} dengan tabungan terpisah ${ArusKasViewModel.formatCompactMoney(totalTabunganSaved)}. Hari ini udah keluar ${ArusKasViewModel.formatCompactMoney(todayExpense)}—jangan biarin recehan kopi & jajan diem-diem nyabotase $goalTitle ya, dikit lagi lunas tuh! 😎"
        ),
        Triple(
            if (monthExpense > monthIncome && monthIncome > 0L) CoralExpense else SkyBlue,
            "📊 ANALISIS TEMEN DEKET",
            "Secara finansial nih bestie, pengeluaran bulan ini ${ArusKasViewModel.formatCompactMoney(monthExpense)} dengan saldo siap pakai ${ArusKasViewModel.formatCompactMoney(totalBalance)}. Kalau gatel pengen jajan yang nggak penting, inget rasanya nyesel pas cek mutasi di akhir bulan ya hahaha 🤣"
        ),
        Triple(
            GoldAccent,
            "💡 SARAN CERDAS HARI INI",
            "Cek fakta dompetmu bro: total semua duitmu ${ArusKasViewModel.formatCompactMoney(totalBalance + totalTabunganSaved + emergencyBalance)}, tapi yang boleh diutak-atik cuma ${ArusKasViewModel.formatCompactMoney(totalBalance)}. Sisihkan ${ArusKasViewModel.formatCompactMoney(suggestLow)}–${ArusKasViewModel.formatCompactMoney(suggestHigh)} hari ini ke $goalTitle (kurang ${ArusKasViewModel.formatCompactMoney(goalRemaining)} lagi) biar makin cepet pamer hasil tabungan!"
        ),
        Triple(
            if (catRatio >= 0.75f) CoralExpense else EmeraldPrimary,
            "🔥 KRITIK & SEMANGAT",
            "Jujur-jujuran aja nih bestie, ngatur duit di rekening & e-wallet itu paling rawan bocor halus kalau males nyatet! Untung lu rajin buka aplikasi ini—pertahanin disiplin limit kategorinya biar dompet tetep ganteng sampai gajian berikutnya 💪"
        )
    )

    val pickedLocal = localAnalysisVariations[kotlin.math.abs(sessionSeed) % localAnalysisVariations.size]
    val pickedSaldoComment = localSaldoComments[kotlin.math.abs(sessionSeed) % localSaldoComments.size]

    return if (guruState is GuruAdviceState.Success) {
        val aiColor = when (guruState.colorLevel.uppercase(Locale.ROOT)) {
            "RED" -> CoralExpense
            "YELLOW" -> GoldAccent
            "BLUE" -> SkyBlue
            else -> EmeraldPrimary
        }
        BestieFinancialInsight(
            saldoSymbolColor = aiColor,
            saldoBadge = guruState.statusBadge.take(18),
            saldoComment = guruState.saldoCritique.ifBlank { pickedSaldoComment },
            nominalSymbolColor = aiColor,
            notificationBadge = "🔔 ${guruState.statusBadge}",
            nominalBoxAnalysis = guruState.nominalBoxAnalysis.ifBlank { "${guruState.teguran} ${guruState.langkahKonkret}" },
            categorySymbolColor = catColor,
            categoryComment = if (guruState.categoryCritique.isNotBlank() && sessionSeed % 2 == 0) {
                "💡 ${guruState.categoryCritique}"
            } else {
                catComment
            }
        )
    } else {
        BestieFinancialInsight(
            saldoSymbolColor = saldoColor,
            saldoBadge = saldoBadge,
            saldoComment = pickedSaldoComment,
            nominalSymbolColor = pickedLocal.first,
            notificationBadge = pickedLocal.second,
            nominalBoxAnalysis = pickedLocal.third,
            categorySymbolColor = catColor,
            categoryComment = catComment
        )
    }
}

fun buildLiveNominalBestieReaction(
    typedAmount: Long,
    isIncomeMode: Boolean,
    isEmergencyExpenseSelected: Boolean,
    selectedCategoryLabel: String,
    totalBalance: Long,
    emergencyBalance: Long
): Triple<Color, String, String>? {
    if (typedAmount <= 0L) return null
    val cCodeLive = WorldCountryCurrencyRegistry.activeCountryCode
    val largeThreshold = if (cCodeLive == "ID") 250_000L else WorldCountryCurrencyRegistry.fromUsd(50.0, cCodeLive).coerceAtLeast(1L)
    val formatted = ArusKasViewModel.formatMoney(typedAmount)
    return when {
        isIncomeMode -> Triple(
            EmeraldPrimary,
            "🟢 CUAN MASUK!",
            "Asik masuk $formatted dari $selectedCategoryLabel nih bosku! Begitu masuk, langsung amankan 10–20% ke Tabungan Target atau Darurat biar nggak numpang lewat doang di dompet ya 😎"
        )
        isEmergencyExpenseSelected -> {
            if (typedAmount > emergencyBalance) {
                Triple(
                    CoralExpense,
                    "🔴 LEBIH DARI SALDO DARURAT!",
                    "Waduh bestie, pengeluaran darurat $formatted ini lebih gede dari Simpanan Daruratmu (${ArusKasViewModel.formatMoney(emergencyBalance)})! Pastikan beneran mendesak ya."
                )
            } else {
                Triple(
                    GoldAccent,
                    "🟡 POS DARURAT TERPAKAI",
                    "Oke bro, $formatted bakal dipotong dari Simpanan Darurat Terpisah (sisa nanti ${ArusKasViewModel.formatMoney(emergencyBalance - typedAmount)}). Semoga urusannya cepet beres ya!"
                )
            }
        }
        typedAmount > totalBalance -> Triple(
            CoralExpense,
            "🔴 SALDO NGGAK CUKUP BRO!",
            "Eh buset bestie, mau keluarin $formatted buat $selectedCategoryLabel padahal Saldo Siap Pakai cuma ${ArusKasViewModel.formatMoney(totalBalance)}? Jangan ganggu jatah Tabungan/Darurat lho! 😭"
        )
        typedAmount >= largeThreshold -> Triple(
            GoldAccent,
            "🟡 NOMINAL GEDE NIH!",
            "Seriusan mau keluarin $formatted sekaligus buat $selectedCategoryLabel bro? Coba tarik napas 3 detik, ini beneran kebutuhan atau cuma impulsif sesaat nih? 😂"
        )
        else -> Triple(
            EmeraldPrimary,
            "🟢 SIAP DICATAT BESTIE",
            "Pengeluaran $formatted untuk $selectedCategoryLabel masih terukur nih bro (sisa saldo siap pakai nanti ${ArusKasViewModel.formatMoney((totalBalance - typedAmount).coerceAtLeast(0L))}). Gas catat biar arus kasmu transparan!"
        )
    }
}

