package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLanguage
import com.example.data.CategoryLimitConfig
import com.example.data.MandatoryBillItem
import com.example.data.PresetCategories
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue

/**
 * Global Pre-Dashboard Financial Profile Onboarding Dialog.
 * Designed with an ultra-minimalist One-Question-Per-Page UX and large, calm typography.
 * Appears automatically ONLY ONCE on first install, and can be reopened anytime from Settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinancialProfileOnboardingDialog(
    initialProfile: UserFinancialProfile,
    currentWallets: List<Wallet>,
    onDismiss: () -> Unit,
    onOpenUserGuide: () -> Unit = {},
    currentLanguage: AppLanguage = WorldCountryCurrencyRegistry.activeLanguage,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onCountryChange: (String) -> Unit = {},
    onCompleteSetup: (
        profile: UserFinancialProfile,
        configuredWallets: List<Wallet>,
        configuredBills: List<MandatoryBillItem>,
        customLimits: Map<String, CategoryLimitConfig>
    ) -> Unit
) {
    val totalSteps = 8
    var step by remember { mutableIntStateOf(1) }
    var activeLang by remember(currentLanguage) { mutableStateOf(currentLanguage) }
    val isId = activeLang == AppLanguage.INDONESIAN

    val royalBlue = Color(0xFF0066AE)
    val softBlueBg = Color(0xFFF0F7FF)

    var selectedCountryCode by remember {
        mutableStateOf(initialProfile.countryCode.ifBlank { WorldCountryCurrencyRegistry.activeCountryCode.ifBlank { "US" } })
    }
    var selectedRegionFilter by remember { mutableStateOf("ALL") }
    val countryProfile = remember(selectedCountryCode) {
        WorldCountryCurrencyRegistry.byCountryCode(selectedCountryCode)
    }
    val currencyPrefix = remember(countryProfile) { "${countryProfile.currencySymbol} " }

    // Question States (1 Question per Page)
    var nickname by remember { mutableStateOf(initialProfile.nickname) }
    var gender by remember { mutableStateOf(initialProfile.gender) } // LAKI_LAKI, PEREMPUAN
    var livingStatus by remember { mutableStateOf(initialProfile.livingStatus) } // NGEKOS, RUMAH_ORTU, KELUARGA
    var incomeSource by remember { mutableStateOf(initialProfile.incomeSourcePrimary) }
    var monthlyIncomeStr by remember {
        val initAmt = if (initialProfile.monthlyIncomeAmount > 0L) initialProfile.monthlyIncomeAmount.toString() else ""
        mutableStateOf(initAmt)
    }
    var controlMode by remember { mutableStateOf(initialProfile.controlMode) }
    var paydayDateStr by remember { mutableStateOf(initialProfile.paydayDate.toString()) }
    var transportMode by remember { mutableStateOf(initialProfile.transportMode) }
    var dependentsCount by remember { mutableIntStateOf(initialProfile.dependentsCount) }
    var primaryGoal by remember { mutableStateOf(initialProfile.primaryFinancialGoal) }

    val selectedHabits = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put("Fuel / Gas", transportMode == "MOTOR" || transportMode == "MOBIL")
            put("Daily Meals", true)
            put("Coffee & Snacks", true)
            put("Personal Care / Skincare", true)
            put("Mobile Data", true)
            put("Family & Social", dependentsCount > 0)
        }
    }

    // Question 7 State: Mandatory Bills (starts at 0 unless user enters nominal)
    var hasKosanBill by remember { mutableStateOf(livingStatus == "NGEKOS") }
    var kosanAmountStr by remember { mutableStateOf("") }
    var kosanDueDayStr by remember { mutableStateOf("1") }

    var hasKuotaBill by remember { mutableStateOf(true) }
    var kuotaAmountStr by remember { mutableStateOf("") }
    var kuotaExpireDayStr by remember { mutableStateOf("15") }
    var kuotaBuyAgainDayStr by remember { mutableStateOf("16") }

    var hasListrikWifiBill by remember { mutableStateOf(true) }
    var listrikAmountStr by remember { mutableStateOf("") }
    var listrikDueDayStr by remember { mutableStateOf("20") }

    var hasCicilanOrOtherBill by remember { mutableStateOf(false) }
    var otherBillName by remember { mutableStateOf(if (isId) "Cicilan / Langganan" else "Installment / Subscription") }
    var otherBillAmountStr by remember { mutableStateOf("") }
    var otherBillDueDayStr by remember { mutableStateOf("5") }

    // Question 8 State: Country-specific Banks & E-Wallets (all start with 0 balance)
    val selectedWallets = remember {
        val baseList = if (currentWallets.isNotEmpty()) currentWallets else countryProfile.starterWallets
        mutableStateListOf<Wallet>().apply { addAll(baseList) }
    }
    val walletBalancesMap = remember {
        mutableStateMapOf<String, String>().apply {
            val baseList = if (currentWallets.isNotEmpty()) currentWallets else countryProfile.starterWallets
            baseList.forEach { w -> put(w.id, w.balance.toString()) }
        }
    }
    var showCustomWalletCreator by remember { mutableStateOf(false) }
    var customWalletName by remember { mutableStateOf("") }
    var customWalletType by remember { mutableStateOf("BANK") }
    var customWalletBalanceStr by remember { mutableStateOf("") }

    val parsedIncome = monthlyIncomeStr.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val calcBillsTotal = remember(
        hasKosanBill, kosanAmountStr,
        hasKuotaBill, kuotaAmountStr,
        hasListrikWifiBill, listrikAmountStr,
        hasCicilanOrOtherBill, otherBillAmountStr
    ) {
        var total = 0L
        if (hasKosanBill) total += kosanAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (hasKuotaBill) total += kuotaAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (hasListrikWifiBill) total += listrikAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (hasCicilanOrOtherBill) total += otherBillAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
        total
    }

    val remainingAfterBills = (parsedIncome - calcBillsTotal).coerceAtLeast(0L)
    val autoLimitHarianBensinMakan = if (remainingAfterBills > 0L) ((remainingAfterBills * 42L) / 100L) else 0L
    val autoLimitJajanNgopi = if (remainingAfterBills > 0L) ((remainingAfterBills * 18L) / 100L) else 0L
    val autoLimitRawatDiri = if (remainingAfterBills > 0L) ((remainingAfterBills * 12L) / 100L) else 0L
    val autoLimitSosialOrtu = if (remainingAfterBills > 0L) ((remainingAfterBills * 10L) / 100L) else 0L
    val autoLimitServisKerja = if (remainingAfterBills > 0L) ((remainingAfterBills * 10L) / 100L) else 0L

    fun finishAndApplySetup() {
        val paydayClean = (paydayDateStr.toIntOrNull() ?: 25).coerceIn(1, 31)
        val debtClean = if (hasCicilanOrOtherBill) otherBillAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L else 0L

        val finalProfile = UserFinancialProfile(
            isCompleted = true,
            countryCode = countryProfile.countryCode,
            currencyCode = countryProfile.currencyCode,
            nickname = nickname.trim(),
            gender = gender,
            livingStatus = livingStatus,
            incomeSourcePrimary = incomeSource,
            monthlyIncomeAmount = parsedIncome,
            paydayDate = paydayClean,
            dependentsCount = dependentsCount,
            transportMode = transportMode,
            monthlyDebtObligation = debtClean,
            primaryFinancialGoal = primaryGoal,
            dailyHabits = selectedHabits.filterValues { it }.keys.toList(),
            controlMode = controlMode
        )

        val billsList = mutableListOf<MandatoryBillItem>()
        if (hasKosanBill) {
            val amt = kosanAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
            if (amt > 0L) {
                val d = (kosanDueDayStr.toIntOrNull() ?: 1).coerceIn(1, 31)
                billsList.add(
                    MandatoryBillItem(
                        id = "bill_kosan",
                        name = if (isId) "Sewa Kos / Tempat Tinggal" else "Rent / Housing",
                        totalAmount = amt,
                        paidAmount = 0L,
                        dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal $d" else "Due every day $d of month",
                        dueDayOfMonth = d,
                        status = "UNPAID",
                        billType = "KOSAN"
                    )
                )
            }
        }
        if (hasKuotaBill) {
            val amt = kuotaAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
            if (amt > 0L) {
                val expD = (kuotaExpireDayStr.toIntOrNull() ?: 15).coerceIn(1, 31)
                val buyD = (kuotaBuyAgainDayStr.toIntOrNull() ?: (expD + 1).coerceAtMost(31)).coerceIn(1, 31)
                billsList.add(
                    MandatoryBillItem(
                        id = "bill_kuota",
                        name = if (isId) "Paket Data & Internet" else "Mobile Data & Internet",
                        totalAmount = amt,
                        paidAmount = 0L,
                        dueScheduleLabel = if (isId) "Habis tgl $expD • Perpanjang tgl $buyD" else "Expires day $expD • Renews day $buyD",
                        dueDayOfMonth = expD,
                        renewalDayOfMonth = buyD,
                        status = "UNPAID",
                        billType = "KUOTA"
                    )
                )
            }
        }
        if (hasListrikWifiBill) {
            val amt = listrikAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
            if (amt > 0L) {
                val d = (listrikDueDayStr.toIntOrNull() ?: 20).coerceIn(1, 31)
                billsList.add(
                    MandatoryBillItem(
                        id = "bill_listrik",
                        name = if (isId) "Listrik & Air" else "Electricity & Utilities",
                        totalAmount = amt,
                        paidAmount = 0L,
                        dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal $d" else "Due every day $d of month",
                        dueDayOfMonth = d,
                        status = "UNPAID",
                        billType = "LISTRIK"
                    )
                )
            }
        }
        if (hasCicilanOrOtherBill && debtClean > 0L) {
            val d = (otherBillDueDayStr.toIntOrNull() ?: 5).coerceIn(1, 31)
            billsList.add(
                MandatoryBillItem(
                    id = "bill_cicilan",
                    name = otherBillName.ifBlank { if (isId) "Cicilan / Langganan" else "Installment / Subscription" },
                    totalAmount = debtClean,
                    paidAmount = 0L,
                    dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal $d" else "Due every day $d of month",
                    dueDayOfMonth = d,
                    status = "UNPAID",
                    billType = "CICILAN"
                )
            )
        }

        val customLimits = mapOf(
            "tagihan_bulanan" to CategoryLimitConfig("tagihan_bulanan", calcBillsTotal, "BULANAN"),
            "kebutuhan_pokok" to CategoryLimitConfig("kebutuhan_pokok", autoLimitHarianBensinMakan, "BULANAN"),
            "rawat_diri" to CategoryLimitConfig("rawat_diri", autoLimitRawatDiri, "BULANAN"),
            "gaya_hidup" to CategoryLimitConfig("gaya_hidup", autoLimitJajanNgopi, "BULANAN"),
            "sosial_darurat" to CategoryLimitConfig("sosial_darurat", autoLimitSosialOrtu, "BULANAN"),
            "modal_produktif" to CategoryLimitConfig("modal_produktif", autoLimitServisKerja, "BULANAN")
        )

        val finalWallets = selectedWallets.map { w ->
            val bal = walletBalancesMap[w.id]?.filter { it.isDigit() }?.toLongOrNull() ?: w.balance
            w.copy(balance = bal)
        }

        onCompleteSetup(finalProfile, finalWallets, billsList, customLimits)
    }

    // Large Question Title & Calm Subtitle per Page (Strictly 1 Question per Page)
    val questionHeadline = when (step) {
        1 -> if (isId) "Bahasa, negara & mata uang mana yang kamu gunakan?" else "Which language, country & currency do you use?"
        2 -> if (isId) "Apa jenis kelamin kamu?" else "What is your gender?"
        3 -> if (isId) "Dari mana sumber penghasilan utamamu?" else "Where does your primary income come from?"
        4 -> if (isId) "Berapa rata-rata penghasilanmu per bulan?" else "What is your average monthly income?"
        5 -> if (isId) "Tanggal berapa kamu biasanya gajian?" else "What day of the month do you get paid?"
        6 -> if (isId) "Bagaimana status tempat tinggalmu saat ini?" else "Where do you currently live?"
        7 -> if (isId) "Tagihan rutin apa yang kamu bayar tiap bulan?" else "Which monthly bills do you pay regularly?"
        else -> if (isId) {
            "Bank & e-wallet apa yang kamu pakai di ${countryProfile.countryNameId}?"
        } else {
            "Which banks & wallets do you use in ${countryProfile.countryNameEn}?"
        }
    }

    val questionSubtitle = when (step) {
        1 -> if (isId) {
            "Pilih bahasa tampilan aplikasi, lalu pilih negara agar simbol mata uang dan daftar bank lokal otomatis sesuai."
        } else {
            "Choose your app display language, then pick your country so currency symbols and local banks match."
        }
        2 -> if (isId) {
            "Membantu menyesuaikan porsi anggaran harian & perawatan diri secara nyaman."
        } else {
            "Helps personalize your daily essentials and self-care budget proportions."
        }
        3 -> if (isId) {
            "Pilih satu sumber pemasukan utama yang paling sering kamu terima."
        } else {
            "Select the main way you earn or receive your regular income."
        }
        4 -> if (isId) {
            "Masukkan perkiraan dalam ${countryProfile.currencyCode} (${countryProfile.currencySymbol}) atau pilih nominal cepat di bawah."
        } else {
            "Enter an estimate in ${countryProfile.currencyCode} (${countryProfile.currencySymbol}) or tap a quick preset below."
        }
        5 -> if (isId) {
            "Pilih tanggal 1 sampai 31 agar siklus anggaran bulananmu akurat."
        } else {
            "Pick a day from 1 to 31 to align your monthly budget cycle."
        }
        6 -> if (isId) {
            "Kami akan menyiapkan atau melewati tagihan tempat tinggal sesuai pilihanmu."
        } else {
            "We will automatically prepare or skip housing bills based on your choice."
        }
        7 -> if (isId) {
            "Aktifkan hanya tagihan yang kamu miliki. Nominalnya bebas kamu sesuaikan."
        } else {
            "Enable only the bills you have. You can adjust their amounts anytime."
        }
        else -> if (isId) {
            "Disesuaikan untuk ${countryProfile.flagEmoji} ${countryProfile.countryNameId} (${countryProfile.currencyCode}). Tambah atau hapus sesuai kebutuhan."
        } else {
            "Tailored for ${countryProfile.flagEmoji} ${countryProfile.countryNameEn} (${countryProfile.currencyCode}). Add or remove any account."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 14.dp)
                .testTag("financial_profile_onboarding_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, royalBlue.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Minimalist Top Navigation & Progress Header
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step Counter Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = softBlueBg
                        ) {
                            Text(
                                text = if (isId) "PERTANYAAN $step / $totalSteps" else "QUESTION $step / $totalSteps",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = royalBlue,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Minimalist Progress Bar
                    LinearProgressIndicator(
                        progress = { step / totalSteps.toFloat() },
                        color = royalBlue,
                        trackColor = royalBlue.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }

                // 2. Single-Question Hero Area & Answer Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Giant Minimalist Question Typography
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = questionHeadline,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("onboarding_question_headline")
                        )
                        Text(
                            text = questionSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Strictly ONE Question Input Per Page
                    when (step) {
                        1 -> {
                            // PAGE 1: App Language, Country & Currency Selection
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isId) "🌐 Pilih Bahasa Aplikasi (Language)" else "🌐 Choose App Language (Bahasa)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = royalBlue
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        Triple(AppLanguage.INDONESIAN, "🇮🇩 Bahasa Indonesia", "Bahasa Indonesia"),
                                        Triple(AppLanguage.ENGLISH, "🌐 English", "International")
                                    ).forEach { (langOption, langTitle, langSubtitle) ->
                                        val isLangSelected = activeLang == langOption
                                        Surface(
                                            onClick = {
                                                activeLang = langOption
                                                WorldCountryCurrencyRegistry.activeLanguage = langOption
                                                onLanguageChange(langOption)
                                                if (otherBillName == "Cicilan / Langganan" || otherBillName == "Installment / Subscription") {
                                                    otherBillName = if (langOption == AppLanguage.INDONESIAN) {
                                                        "Cicilan / Langganan"
                                                    } else {
                                                        "Installment / Subscription"
                                                    }
                                                }
                                                if (incomeSource == "Monthly Salary" || incomeSource == "Gaji Bulanan") {
                                                    incomeSource = if (langOption == AppLanguage.INDONESIAN) {
                                                        "Gaji Bulanan"
                                                    } else {
                                                        "Monthly Salary"
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isLangSelected) softBlueBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                            border = BorderStroke(
                                                width = if (isLangSelected) 1.8.dp else 1.dp,
                                                color = if (isLangSelected) royalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 58.dp)
                                                .testTag("onboarding_language_${langOption.name}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = langTitle,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isLangSelected) royalBlue else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = langSubtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isLangSelected) royalBlue.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Text(
                                text = if (isId) "🗺️ Pilih Negara & Mata Uang" else "🗺️ Select Country & Currency",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to (if (isId) "Semua" else "All"),
                                    "Asia-Pacific" to (if (isId) "Asia-Pasifik" else "Asia-Pacific"),
                                    "Americas" to (if (isId) "Amerika" else "Americas"),
                                    "Europe & Middle East" to (if (isId) "Eropa & Timteng" else "Europe & M.East")
                                ).forEach { (regKey, regLabel) ->
                                    val isRegSelected = selectedRegionFilter == regKey
                                    Surface(
                                        onClick = { selectedRegionFilter = regKey },
                                        shape = RoundedCornerShape(50),
                                        color = if (isRegSelected) royalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    ) {
                                        Text(
                                            text = regLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isRegSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isRegSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            val filteredCountries = remember(selectedRegionFilter) {
                                if (selectedRegionFilter == "ALL") {
                                    WorldCountryCurrencyRegistry.allCountries
                                } else {
                                    WorldCountryCurrencyRegistry.allCountries.filter { it.regionEn == selectedRegionFilter }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                filteredCountries.forEach { cProf ->
                                    val isSelectedCountry = cProf.countryCode == selectedCountryCode
                                    MinimalQuestionOptionCard(
                                        title = "${cProf.flagEmoji}  ${cProf.displayCountryName(activeLang)}",
                                        subtitle = "${cProf.displayCurrencyName(activeLang)} • ${cProf.starterWallets.take(3).joinToString(", ") { it.name }}",
                                        trailingBadge = "${cProf.currencyCode} (${cProf.currencySymbol})",
                                        selected = isSelectedCountry,
                                        accentColor = royalBlue,
                                        onClick = {
                                            selectedCountryCode = cProf.countryCode
                                            WorldCountryCurrencyRegistry.activeCountryCode = cProf.countryCode
                                            onCountryChange(cProf.countryCode)
                                            monthlyIncomeStr = if (initialProfile.monthlyIncomeAmount > 0L) initialProfile.monthlyIncomeAmount.toString() else ""
                                            kosanAmountStr = ""
                                            kuotaAmountStr = ""
                                            listrikAmountStr = ""
                                            otherBillAmountStr = ""
                                            selectedWallets.clear()
                                            selectedWallets.addAll(cProf.starterWallets)
                                            walletBalancesMap.clear()
                                            cProf.starterWallets.forEach { w ->
                                                walletBalancesMap[w.id] = "0"
                                            }
                                        },
                                        modifier = Modifier.testTag("onboarding_country_${cProf.countryCode}")
                                    )
                                }
                            }
                        }

                        2 -> {
                            // PAGE 2: Gender Selection
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    Triple(
                                        "LAKI_LAKI",
                                        if (isId) "👨  Laki-laki" else "👨  Male",
                                        if (isId) "Opsional — hanya untuk sapaan profil" else "Optional — profile greeting only"
                                    ),
                                    Triple(
                                        "PEREMPUAN",
                                        if (isId) "👩  Perempuan" else "👩  Female",
                                        if (isId) "Opsional — hanya untuk sapaan profil" else "Optional — profile greeting only"
                                    ),
                                    Triple(
                                        "TIDAK_MENJAWAB",
                                        if (isId) "🔒  Memilih untuk tidak menjawab" else "🔒  Prefer not to say",
                                        if (isId) "Lewati informasi gender tanpa memengaruhi analisis" else "Skip gender info without affecting analysis"
                                    )
                                ).forEach { (key, label, desc) ->
                                    MinimalQuestionOptionCard(
                                        title = label,
                                        subtitle = desc,
                                        selected = gender == key,
                                        accentColor = royalBlue,
                                        onClick = { gender = key },
                                        modifier = Modifier.testTag("gender_option_$key")
                                    )
                                }
                            }
                        }

                        3 -> {
                            // PAGE 3: Primary Income Source
                            val sources = if (isId) {
                                listOf(
                                    "Gaji Bulanan" to "💼  Gaji Bulanan Tetap",
                                    "Usaha / Dagang" to "🏪  Usaha / Bisnis Sendiri",
                                    "Freelance / Proyek" to "💻  Freelance / Kerja Remote",
                                    "Harian / Ngojek Online" to "🛵  Penghasilan Harian / Gig",
                                    "Kiriman / Uang Saku" to "🎓  Uang Saku / Kiriman Keluarga"
                                )
                            } else {
                                listOf(
                                    "Monthly Salary" to "💼  Full-Time Monthly Salary",
                                    "Business / Trade" to "🏪  Business / Self-Employed",
                                    "Freelance / Remote" to "💻  Freelance / Remote Work",
                                    "Daily / Gig Work" to "🛵  Daily / Gig Economy",
                                    "Allowance / Stipend" to "🎓  Allowance / Stipend"
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                sources.forEach { (rawValue, displayLabel) ->
                                    MinimalQuestionOptionCard(
                                        title = displayLabel,
                                        selected = incomeSource == rawValue,
                                        accentColor = royalBlue,
                                        onClick = { incomeSource = rawValue }
                                    )
                                }
                            }
                        }

                        4 -> {
                            // PAGE 4: Average Monthly Income
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = softBlueBg.copy(alpha = 0.7f),
                                border = BorderStroke(1.5.dp, royalBlue.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isId) {
                                            "NOMINAL PER BULAN (${countryProfile.currencyCode})"
                                        } else {
                                            "MONTHLY AMOUNT (${countryProfile.currencyCode})"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = royalBlue
                                    )
                                    OutlinedTextField(
                                        value = monthlyIncomeStr,
                                        onValueChange = { monthlyIncomeStr = it.filter { c -> c.isDigit() } },
                                        prefix = {
                                            Text(
                                                text = currencyPrefix,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = royalBlue
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("onboarding_income_input")
                                    )
                                }
                            }

                            val incomePresets = remember(countryProfile) {
                                val base = countryProfile.defaultMonthlyIncome
                                listOf(
                                    (base * 50L) / 100L,
                                    (base * 75L) / 100L,
                                    base,
                                    (base * 135L) / 100L,
                                    (base * 180L) / 100L,
                                    (base * 250L) / 100L
                                )
                            }

                            Text(
                                text = if (isId) "Pilih Cepat Nominal:" else "Quick Select Amount:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                incomePresets.forEach { preset ->
                                    val isChosen = parsedIncome == preset
                                    Surface(
                                        onClick = { monthlyIncomeStr = preset.toString() },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isChosen) royalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isChosen) royalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Text(
                                            text = ArusKasViewModel.formatMoney(preset, countryProfile.countryCode),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        5 -> {
                            // PAGE 5: Monthly Payday Date (1 - 31)
                            OutlinedTextField(
                                value = paydayDateStr,
                                onValueChange = { paydayDateStr = it.filter { c -> c.isDigit() }.take(2) },
                                label = {
                                    Text(if (isId) "Tanggal Gajian (1 - 31)" else "Payday Day of Month (1 - 31)")
                                },
                                prefix = {
                                    Text(
                                        text = if (isId) "Tanggal " else "Day ",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = royalBlue
                                    )
                                },
                                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = if (isId) "Pilih Cepat Tanggal:" else "Quick Pick Day:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("1", "5", "10", "15", "20", "25", "28", "30").forEach { dayOpt ->
                                    val isDaySelected = paydayDateStr == dayOpt
                                    Surface(
                                        onClick = { paydayDateStr = dayOpt },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isDaySelected) royalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    ) {
                                        Text(
                                            text = if (isId) "Tgl $dayOpt" else "Day $dayOpt",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        6 -> {
                            // PAGE 6: Living Arrangement
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    Triple(
                                        "NGEKOS",
                                        if (isId) "🏠  Sewa / Kos / Apartemen" else "🏠  Renting / Apartment",
                                        if (isId) "Menyiapkan pengingat tagihan sewa bulanan otomatis" else "Automatically sets up a monthly rent reminder"
                                    ),
                                    Triple(
                                        "RUMAH_ORTU",
                                        if (isId) "🏡  Tinggal Bersama Orang Tua" else "🏡  Living with Family / Parents",
                                        if (isId) "Tanpa tagihan sewa kos bulanan" else "Skips monthly rent bills automatically"
                                    ),
                                    Triple(
                                        "KELUARGA",
                                        if (isId) "👨‍👩‍👧  Rumah Sendiri / Berkeluarga" else "👨‍👩‍👧  Own Home / Household",
                                        if (isId) "Fokus pada kebutuhan rumah tangga & utilitas" else "Focuses on household utilities & family expenses"
                                    )
                                ).forEach { (key, title, desc) ->
                                    MinimalQuestionOptionCard(
                                        title = title,
                                        subtitle = desc,
                                        selected = livingStatus == key,
                                        accentColor = royalBlue,
                                        onClick = {
                                            livingStatus = key
                                            hasKosanBill = (key == "NGEKOS")
                                        }
                                    )
                                }
                            }
                        }

                        7 -> {
                            // PAGE 7: Recurring Monthly Bills
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OnboardingBillSetupCard(
                                    checked = hasKosanBill,
                                    onCheckedChange = { hasKosanBill = it },
                                    title = if (isId) "🏠  Sewa Tempat Tinggal" else "🏠  Rent / Housing",
                                    amountStr = kosanAmountStr,
                                    onAmountChange = { kosanAmountStr = it.filter { c -> c.isDigit() } },
                                    scheduleLabel = if (isId) "Tanggal (1-31)" else "Due day (1-31)",
                                    dayStr = kosanDueDayStr,
                                    onDayChange = { kosanDueDayStr = it.filter { c -> c.isDigit() }.take(2) },
                                    currencySymbol = countryProfile.currencySymbol,
                                    thousandsSeparator = countryProfile.thousandsSeparator,
                                    isId = isId
                                )

                                OnboardingBillSetupCard(
                                    checked = hasKuotaBill,
                                    onCheckedChange = { hasKuotaBill = it },
                                    title = if (isId) "📶  Paket Data & Internet" else "📶  Mobile Data & Internet",
                                    amountStr = kuotaAmountStr,
                                    onAmountChange = { kuotaAmountStr = it.filter { c -> c.isDigit() } },
                                    scheduleLabel = if (isId) "Tanggal (1-31)" else "Due day (1-31)",
                                    dayStr = kuotaExpireDayStr,
                                    onDayChange = {
                                        val clean = it.filter { c -> c.isDigit() }.take(2)
                                        kuotaExpireDayStr = clean
                                        val exp = clean.toIntOrNull() ?: 15
                                        kuotaBuyAgainDayStr = (exp + 1).coerceAtMost(31).toString()
                                    },
                                    currencySymbol = countryProfile.currencySymbol,
                                    thousandsSeparator = countryProfile.thousandsSeparator,
                                    isId = isId
                                )

                                OnboardingBillSetupCard(
                                    checked = hasListrikWifiBill,
                                    onCheckedChange = { hasListrikWifiBill = it },
                                    title = if (isId) "⚡  Listrik, Air & WiFi" else "⚡  Electricity, Water & WiFi",
                                    amountStr = listrikAmountStr,
                                    onAmountChange = { listrikAmountStr = it.filter { c -> c.isDigit() } },
                                    scheduleLabel = if (isId) "Tanggal (1-31)" else "Due day (1-31)",
                                    dayStr = listrikDueDayStr,
                                    onDayChange = { listrikDueDayStr = it.filter { c -> c.isDigit() }.take(2) },
                                    currencySymbol = countryProfile.currencySymbol,
                                    thousandsSeparator = countryProfile.thousandsSeparator,
                                    isId = isId
                                )

                                OnboardingBillSetupCard(
                                    checked = hasCicilanOrOtherBill,
                                    onCheckedChange = { hasCicilanOrOtherBill = it },
                                    title = if (isId) "💳  Cicilan / Langganan" else "💳  Installments / Subscriptions",
                                    amountStr = otherBillAmountStr,
                                    onAmountChange = { otherBillAmountStr = it.filter { c -> c.isDigit() } },
                                    scheduleLabel = if (isId) "Tanggal (1-31)" else "Due day (1-31)",
                                    dayStr = otherBillDueDayStr,
                                    onDayChange = { otherBillDueDayStr = it.filter { c -> c.isDigit() }.take(2) },
                                    currencySymbol = countryProfile.currencySymbol,
                                    thousandsSeparator = countryProfile.thousandsSeparator,
                                    isId = isId
                                )
                            }
                        }

                        else -> {
                            // PAGE 8: Country-Specific Banks & Digital Wallets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isId) {
                                        "${selectedWallets.size} Akun Aktif (${countryProfile.currencyCode})"
                                    } else {
                                        "${selectedWallets.size} Active Accounts (${countryProfile.currencyCode})"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = royalBlue
                                )
                                TextButton(
                                    onClick = onOpenUserGuide,
                                    modifier = Modifier.testTag("onboarding_open_guide_button")
                                ) {
                                    Text(
                                        text = if (isId) "📖 Panduan" else "📖 App Guide",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = royalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedWallets.forEach { w ->
                                    val typeBadge = when (w.walletType) {
                                        "BANK" -> "🏦 BANK"
                                        "CASH" -> if (isId) "💵 TUNAI" else "💵 CASH"
                                        else -> "📱 WALLET"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        border = BorderStroke(1.dp, royalBlue.copy(alpha = 0.22f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("onboarding_wallet_item_${w.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = royalBlue.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = typeBadge,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                            color = royalBlue,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = w.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                if (selectedWallets.size > 1) {
                                                    IconButton(
                                                        onClick = {
                                                            selectedWallets.removeAll { it.id == w.id }
                                                            walletBalancesMap.remove(w.id)
                                                        },
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .testTag("onboarding_remove_wallet_${w.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Remove ${w.name}",
                                                            tint = CoralExpense,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            OutlinedTextField(
                                                value = walletBalancesMap[w.id] ?: "0",
                                                onValueChange = { v -> walletBalancesMap[w.id] = v.filter { c -> c.isDigit() } },
                                                label = {
                                                    Text(
                                                        if (isId) "Saldo Awal (${countryProfile.currencySymbol})"
                                                        else "Starting Balance (${countryProfile.currencySymbol})"
                                                    )
                                                },
                                                prefix = { Text(currencyPrefix, fontWeight = FontWeight.Bold, color = royalBlue) },
                                                visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("onboarding_balance_input_${w.id}")
                                            )
                                        }
                                    }
                                }
                            }

                            val availableToAdd = countryProfile.bankAndWalletPresets.filter { preset ->
                                selectedWallets.none { it.id == preset.id || it.name.equals(preset.name, ignoreCase = true) }
                            }
                            if (availableToAdd.isNotEmpty()) {
                                Text(
                                    text = if (isId) "Tambah Cepat:" else "Quick Add:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    availableToAdd.forEach { preset ->
                                        Surface(
                                            onClick = {
                                                selectedWallets.add(preset)
                                                if (!walletBalancesMap.containsKey(preset.id)) {
                                                    walletBalancesMap[preset.id] = "0"
                                                }
                                            },
                                            shape = RoundedCornerShape(50),
                                            color = softBlueBg,
                                            border = BorderStroke(1.dp, royalBlue.copy(alpha = 0.35f)),
                                            modifier = Modifier.testTag("onboarding_quick_add_${preset.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = royalBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = preset.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = royalBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { showCustomWalletCreator = !showCustomWalletCreator },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, royalBlue.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_toggle_custom_wallet_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = royalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showCustomWalletCreator) {
                                        if (isId) "Tutup Form Kustom" else "Close Custom Form"
                                    } else {
                                        if (isId) "Tambah Bank / E-Wallet Lainnya" else "Add Custom Bank / Wallet"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = royalBlue
                                )
                            }

                            if (showCustomWalletCreator) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, royalBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                "BANK" to "🏦 Bank",
                                                "EWALLET" to "📱 E-Wallet",
                                                "CASH" to (if (isId) "💵 Tunai" else "💵 Cash")
                                            ).forEach { (tKey, tLabel) ->
                                                FilterChip(
                                                    selected = customWalletType == tKey,
                                                    onClick = { customWalletType = tKey },
                                                    label = { Text(tLabel, style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }
                                        OutlinedTextField(
                                            value = customWalletName,
                                            onValueChange = { customWalletName = it },
                                            label = { Text(if (isId) "Nama Bank / E-Wallet" else "Bank / Wallet Name") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("onboarding_custom_wallet_name_input")
                                        )
                                        OutlinedTextField(
                                            value = customWalletBalanceStr,
                                            onValueChange = { customWalletBalanceStr = it.filter { c -> c.isDigit() } },
                                            label = { Text(if (isId) "Saldo Awal (${countryProfile.currencySymbol})" else "Starting Balance (${countryProfile.currencySymbol})") },
                                            prefix = { Text(currencyPrefix, fontWeight = FontWeight.Bold) },
                                            visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("onboarding_custom_wallet_balance_input")
                                        )
                                        Button(
                                            onClick = {
                                                val cleanName = customWalletName.trim()
                                                if (cleanName.isNotBlank()) {
                                                    val newId = "wallet_custom_${System.currentTimeMillis()}"
                                                    val bal = customWalletBalanceStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                                    val colorHex = when (customWalletType) {
                                                        "BANK" -> "#0066AE"
                                                        "CASH" -> "#10B981"
                                                        else -> "#118EEA"
                                                    }
                                                    val iconKey = if (customWalletType == "BANK") "bank_bca" else "ewallet_dana"
                                                    val newW = Wallet(
                                                        id = newId,
                                                        userId = "local_preview",
                                                        name = cleanName,
                                                        walletType = customWalletType,
                                                        balance = bal,
                                                        colorHex = colorHex,
                                                        iconKey = iconKey
                                                    )
                                                    selectedWallets.add(newW)
                                                    walletBalancesMap[newId] = bal.toString()
                                                    customWalletName = ""
                                                    customWalletBalanceStr = ""
                                                    showCustomWalletCreator = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = royalBlue),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("onboarding_add_custom_wallet_confirm")
                                        ) {
                                            Text(
                                                text = if (isId) "Tambahkan Akun" else "Add Account",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Minimalist Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step -= 1 },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text(
                                text = if (isId) "← Kembali" else "← Back",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (step < totalSteps) {
                                step += 1
                            } else {
                                finishAndApplySetup()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = royalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("onboarding_next_or_finish_button")
                    ) {
                        Text(
                            text = if (step < totalSteps) {
                                if (isId) "Lanjut →" else "Continue →"
                            } else {
                                if (isId) "Selesai & Masuk Dashboard ✓" else "Finish & Open Dashboard ✓"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalQuestionOptionCard(
    title: String,
    subtitle: String? = null,
    trailingBadge: String? = null,
    selected: Boolean,
    accentColor: Color = Color(0xFF0066AE),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accentColor.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(
            width = if (selected) 1.8.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!trailingBadge.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) accentColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = trailingBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) accentColor else Color.Transparent,
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.size(22.dp)
                ) {
                    if (selected) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingBillSetupCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    amountStr: String,
    onAmountChange: (String) -> Unit,
    scheduleLabel: String,
    dayStr: String,
    onDayChange: (String) -> Unit,
    currencySymbol: String = WorldCountryCurrencyRegistry.getActiveProfile().currencySymbol,
    thousandsSeparator: Char = WorldCountryCurrencyRegistry.getActiveProfile().thousandsSeparator,
    isId: Boolean = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.INDONESIAN
) {
    val royalBlue = Color(0xFF0066AE)
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(18.dp),
        color = if (checked) royalBlue.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(
            width = if (checked) 1.6.dp else 1.dp,
            color = if (checked) royalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
                    fontWeight = if (checked) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (checked) royalBlue else MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (checked) royalBlue else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (checked) (if (isId) "Aktif ✓" else "Active ✓") else (if (isId) "Nonaktif" else "Off"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (checked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
            if (checked) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = onAmountChange,
                        label = { Text(if (isId) "Nominal ($currencySymbol)" else "Amount ($currencySymbol)") },
                        prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = royalBlue) },
                        visualTransformation = RupiahThousandsVisualTransformation(thousandsSeparator),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = onDayChange,
                        label = { Text(scheduleLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Mandatory Bills Checklist Section
 */
@Composable
fun MandatoryBillsChecklistSection(
    bills: List<MandatoryBillItem>,
    selectedWallet: Wallet?,
    onChangeBillStatus: (bill: MandatoryBillItem, newStatus: String) -> Unit,
    onAddBillClick: () -> Unit,
    onEditBillClick: (MandatoryBillItem) -> Unit,
    onDeleteBillClick: (MandatoryBillItem) -> Unit
) {
    val isId = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.INDONESIAN
    val totalBillsLimit = remember(bills) { bills.sumOf { it.totalAmount } }
    val totalPaid = remember(bills) { bills.sumOf { it.paidAmount } }
    val paidRatio = if (totalBillsLimit > 0L) (totalPaid.toFloat() / totalBillsLimit.toFloat()).coerceIn(0f, 1f) else 0f
    val paidCount = remember(bills) { bills.count { it.status == "PAID" } }
    val halfCount = remember(bills) { bills.count { it.status == "HALF_PAID" } }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = glassmorphicBorderStroke(accentColor = SkyBlue, width = 1.2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphicCardEffect(cornerRadius = 20.dp, accentTint = SkyBlue)
            .testTag("mandatory_bills_checklist_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Mandatory Bills",
                            tint = SkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isId) "CEKLIST TAGIHAN WAJIB" else "MANDATORY BILLS CHECKLIST",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SkyBlue
                        )
                        FeatureHelpButton(
                            content = FeatureHelpDatabase.TAGIHAN_WAJIB,
                            iconSize = 13,
                            tint = SkyBlue,
                            testTag = "help_mandatory_bills_checklist"
                        )
                    }
                    Text(
                        text = if (isId) {
                            "Lunas: $paidCount/${bills.size}" +
                                (if (halfCount > 0) " • $halfCount setengah" else "") +
                                (if (selectedWallet != null) " • ${selectedWallet.name}" else "")
                        } else {
                            "Paid: $paidCount/${bills.size}" +
                                (if (halfCount > 0) " • $halfCount partial" else "") +
                                (if (selectedWallet != null) " • ${selectedWallet.name}" else "")
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onAddBillClick,
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldPrimary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("add_mandatory_bill_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bill",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isId) "Tagihan" else "Add Bill",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (if (isId) "Terbayar: " else "Paid: ") + ArusKasViewModel.formatMoney(totalPaid),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = (if (isId) "Total: " else "Total: ") + ArusKasViewModel.formatMoney(totalBillsLimit),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(paidRatio)
                                .height(7.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        paidRatio >= 1f -> EmeraldPrimary
                                        paidRatio >= 0.5f -> GoldAccent
                                        else -> SkyBlue
                                    }
                                )
                        )
                    }
                }
            }

            bills.forEach { bill ->
                val statusColor = when (bill.status) {
                    "PAID" -> EmeraldPrimary
                    "HALF_PAID" -> GoldAccent
                    else -> CoralExpense
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.08f),
                    border = BorderStroke(0.9.dp, statusColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mandatory_bill_row_${bill.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (bill.status == "PAID") Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Toggle ${bill.name}",
                                    tint = statusColor,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            val next = if (bill.status == "PAID") "UNPAID" else "PAID"
                                            onChangeBillStatus(bill, next)
                                        }
                                        .testTag("checkbox_bill_${bill.id}")
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bill.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (bill.status == "PAID") TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = bill.dueScheduleLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                            color = GoldAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = ArusKasViewModel.formatMoney(bill.totalAmount),
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                    if (bill.status == "HALF_PAID") {
                                        Text(
                                            text = (if (isId) "Sisa " else "Left ") + ArusKasViewModel.formatCompactMoney(bill.remainingAmount),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                            color = GoldAccent
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onEditBillClick(bill) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit ${bill.name}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteBillClick(bill) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete ${bill.name}",
                                        tint = CoralExpense.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
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

/**
 * Mandatory Bill Add/Edit Dialog (International English default + Bahasa Indonesia optional)
 */
@Composable
fun MandatoryBillEditorDialog(
    initialBill: MandatoryBillItem?,
    onDismiss: () -> Unit,
    onSave: (
        billId: String?,
        name: String,
        totalAmount: Long,
        dueScheduleLabel: String,
        dueDay: Int,
        renewalDay: Int?,
        billType: String
    ) -> Unit
) {
    val isId = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.INDONESIAN
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val currencyPrefix = "${countryProfile.currencySymbol} "

    var name by remember(initialBill) { mutableStateOf(initialBill?.name ?: "") }
    var amountStr by remember(initialBill) { mutableStateOf(initialBill?.totalAmount?.toString() ?: "") }
    var scheduleType by remember(initialBill) {
        mutableStateOf(if (initialBill?.renewalDayOfMonth != null || initialBill?.billType == "KUOTA") "HABIS_BELI_LAGI" else "TANGGAL_TETAP")
    }
    var dueDayStr by remember(initialBill) { mutableStateOf((initialBill?.dueDayOfMonth ?: 1).toString()) }
    var renewalDayStr by remember(initialBill) { mutableStateOf((initialBill?.renewalDayOfMonth ?: 16).toString()) }
    var customScheduleNote by remember(initialBill) { mutableStateOf(initialBill?.dueScheduleLabel ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBill == null) {
                    if (isId) "Tambah Kebutuhan Wajib" else "Add Mandatory Bill"
                } else {
                    if (isId) "Atur: ${initialBill.name}" else "Edit Bill: ${initialBill.name}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            if (isId) "Nama Tagihan (mis: Kosan, Data, Listrik)"
                            else "Bill Name (e.g. Rent, Mobile Data, Electricity)"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_editor_name_input")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                    label = {
                        Text(
                            if (isId) "Nominal Tagihan (${countryProfile.currencySymbol})"
                            else "Bill Amount (${countryProfile.currencySymbol})"
                        )
                    },
                    prefix = { Text(currencyPrefix, fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_editor_amount_input")
                )

                Text(
                    text = if (isId) "Pilih Pola Jatuh Tempo:" else "Select Due Date Pattern:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = scheduleType == "TANGGAL_TETAP",
                        onClick = { scheduleType = "TANGGAL_TETAP" },
                        label = {
                            Text(
                                if (isId) "Setiap Tanggal X" else "Monthly on Day X",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    FilterChip(
                        selected = scheduleType == "HABIS_BELI_LAGI",
                        onClick = { scheduleType = "HABIS_BELI_LAGI" },
                        label = {
                            Text(
                                if (isId) "Habis Tgl X • Beli Tgl Y" else "Expires X • Renews Y",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }

                if (scheduleType == "TANGGAL_TETAP") {
                    OutlinedTextField(
                        value = dueDayStr,
                        onValueChange = {
                            dueDayStr = it.filter { c -> c.isDigit() }.take(2)
                            val d = dueDayStr.toIntOrNull() ?: 1
                            customScheduleNote = if (isId) "Bayar setiap tanggal $d" else "Due every day $d of month"
                        },
                        label = { Text(if (isId) "Jatuh Tempo Setiap Tanggal (1 - 31)" else "Due Day of Month (1 - 31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dueDayStr,
                            onValueChange = {
                                dueDayStr = it.filter { c -> c.isDigit() }.take(2)
                                val exp = dueDayStr.toIntOrNull() ?: 15
                                val buy = renewalDayStr.toIntOrNull() ?: (exp + 1)
                                customScheduleNote = if (isId) "Habis tgl $exp • Beli lagi tgl $buy" else "Expires day $exp • Renews day $buy"
                            },
                            label = { Text(if (isId) "Habis Tanggal (1-31)" else "Expires Day (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = renewalDayStr,
                            onValueChange = {
                                renewalDayStr = it.filter { c -> c.isDigit() }.take(2)
                                val exp = dueDayStr.toIntOrNull() ?: 15
                                val buy = renewalDayStr.toIntOrNull() ?: 16
                                customScheduleNote = if (isId) "Habis tgl $exp • Beli lagi tgl $buy" else "Expires day $exp • Renews day $buy"
                            },
                            label = { Text(if (isId) "Beli Lagi Tgl" else "Renews Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = customScheduleNote,
                    onValueChange = { customScheduleNote = it },
                    label = { Text(if (isId) "Keterangan Jadwal" else "Schedule Note") },
                    placeholder = {
                        Text(
                            if (isId) "Cth: Bayar setiap tanggal 1"
                            else "E.g. Due every 1st of month"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    val d = (dueDayStr.toIntOrNull() ?: 1).coerceIn(1, 31)
                    val ren = if (scheduleType == "HABIS_BELI_LAGI") (renewalDayStr.toIntOrNull() ?: d).coerceIn(1, 31) else null
                    val autoLabel = customScheduleNote.ifBlank {
                        if (ren != null) {
                            if (isId) "Habis tgl $d • Beli lagi tgl $ren" else "Expires day $d • Renews day $ren"
                        } else {
                            if (isId) "Bayar setiap tanggal $d" else "Due every day $d of month"
                        }
                    }
                    if (name.isNotBlank() && amt > 0L) {
                        onSave(
                            initialBill?.id,
                            name,
                            amt,
                            autoLabel,
                            d,
                            ren,
                            if (ren != null) "KUOTA" else "KOSAN"
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_mandatory_bill_button")
            ) {
                Text(if (isId) "Simpan" else "Save Bill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isId) "Batal" else "Cancel")
            }
        }
    )
}
