package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PresetCategories
import com.example.ui.ArusKasViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ArusKas", appName)
  }

  @Test
  fun `verify rupiah formatter and expense categories integrity`() {
    val formatted = ArusKasViewModel.formatRupiah(150000L)
    assertEquals("Rp 150.000", formatted)
    val compact = ArusKasViewModel.formatCompactRupiah(1500000L)
    assertEquals("Rp 1,5 Jt", compact)
    assertEquals(6, PresetCategories.expenseGrid3x3.size)
  }

  @Test
  fun `verify user guide keys and app settings default`() {
    val defaultSettings = com.example.data.AppSettings()
    org.junit.Assert.assertFalse(defaultSettings.hasSeenUserGuide)
    val titleId = com.example.data.AppLocalization.get("guide_title", com.example.data.AppLanguage.INDONESIAN)
    val titleEn = com.example.data.AppLocalization.get("guide_title", com.example.data.AppLanguage.ENGLISH)
    assertEquals("Cara Kerja ArusKas", titleId)
    assertEquals("How ArusKas Works", titleEn)
  }

  @Test
  fun `verify subscription 7k pricing and free trial logic`() {
    val sub = com.example.data.SubscriptionConfig()
    org.junit.Assert.assertEquals("aruskas_pro_monthly_7k", sub.productId)
    org.junit.Assert.assertTrue(sub.isTrialActive)
    org.junit.Assert.assertEquals(30, sub.remainingTrialDays)
    val priceId = com.example.data.AppLocalization.get("paywall_price_headline", com.example.data.AppLanguage.INDONESIAN)
    val priceEn = com.example.data.AppLocalization.get("paywall_price_headline", com.example.data.AppLanguage.ENGLISH)
    assertEquals("Rp 7.000 / Bulan", priceId)
    assertEquals("IDR 7,000 / Month", priceEn)
  }

  @Test
  fun `verify rupiah input formatting with thousands dots and Rp prefix`() {
    val formatted50k = com.example.ui.CurrencyInputFormatter.formatDigits("50000")
    assertEquals("50.000", formatted50k)

    val formatted1_5M = com.example.ui.CurrencyInputFormatter.formatDigits("1500000")
    assertEquals("1.500.000", formatted1_5M)

    val withPrefix = com.example.ui.CurrencyInputFormatter.formatWithPrefix("50000")
    assertEquals("Rp 50.000", withPrefix)

    val parsedLong = com.example.ui.CurrencyInputFormatter.parseToLong("Rp 1.500.000")
    assertEquals(1500000L, parsedLong)
  }

  @Test
  fun `verify mandatory bills checklist states and flexible real life expense logic`() {
    val defaultBills = PresetCategories.defaultMandatoryBills
    org.junit.Assert.assertTrue(defaultBills.any { it.name.contains("Kosan") && it.dueDayOfMonth == 1 })
    org.junit.Assert.assertTrue(defaultBills.any { it.name.contains("Kuota") && it.renewalDayOfMonth != null })

    val kosanBill = defaultBills.first { it.id == "bill_kosan" }.copy(totalAmount = 800_000L, paidAmount = 400_000L, status = "HALF_PAID")
    val kuotaBill = defaultBills.first { it.id == "bill_kuota" }.copy(totalAmount = 100_000L, paidAmount = 100_000L, status = "PAID")
    val spentBills = ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "tagihan_bulanan",
      period = "BULANAN",
      transactions = emptyList(),
      mandatoryBills = listOf(kosanBill, kuotaBill)
    )
    assertEquals(500_000L, spentBills) // 400k (half kosan) + 100k (paid kuota)

    val quickItems = PresetCategories.realLifeQuickItems
    org.junit.Assert.assertTrue(quickItems.any { it.label.contains("Bensin") })
    org.junit.Assert.assertTrue(quickItems.any { it.label.contains("Rokok") })

    val adaptivePerDay = ArusKasViewModel.calculateFlexibleAdaptiveQuotaPerDay(
      limitAmount = 450_000L,
      spentAmount = 150_000L,
      period = "BULANAN"
    )
    org.junit.Assert.assertTrue(adaptivePerDay > 0L)
  }

  @Test
  fun `verify feature help cloud content database for all core tools`() {
    val totalSaldoHelp = com.example.ui.FeatureHelpDatabase.TOTAL_SALDO
    assertEquals("Total Saldo & Multi-Dompet", totalSaldoHelp.title)
    org.junit.Assert.assertTrue(totalSaldoHelp.summary.isNotBlank())
    org.junit.Assert.assertTrue(totalSaldoHelp.keyPoints.isNotEmpty())

    val billsHelp = com.example.ui.FeatureHelpDatabase.TAGIHAN_WAJIB
    org.junit.Assert.assertTrue(billsHelp.title.contains("Kebutuhan Wajib") || billsHelp.title.contains("Tagihan"))
    org.junit.Assert.assertTrue(billsHelp.keyPoints.isNotEmpty())

    val pintasanHelp = com.example.ui.FeatureHelpDatabase.PINTASAN_SEHARI_HARI
    org.junit.Assert.assertTrue(pintasanHelp.summary.contains("Bebas Kapan Aja") || pintasanHelp.title.contains("Pintasan"))

    val limitHelp = com.example.ui.FeatureHelpDatabase.KATEGORI_LIMIT
    org.junit.Assert.assertTrue(limitHelp.title.contains("Limit"))
  }

  @Test
  fun `verify financial profile defaults and help database integrity`() {
    val profile = com.example.data.UserFinancialProfile(
      countryCode = "ID",
      currencyCode = "IDR",
      monthlyIncomeAmount = 5_000_000L,
      paydayDate = 25
    )
    assertEquals("ID", profile.countryCode)
    assertEquals("IDR", profile.currencyCode)
    assertEquals(5_000_000L, profile.monthlyIncomeAmount)
    assertEquals(25, profile.paydayDate)

    val help = com.example.ui.FeatureHelpDatabase.MONITOR_UANG_MASUK
    org.junit.Assert.assertTrue(help.title.isNotBlank())
    org.junit.Assert.assertTrue(help.summaryEn.isNotBlank())
  }

  @Test
  fun `verify strictly one-time onboarding and user guide persistence across multiple app restarts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store1 = com.example.data.ArusKasLocalStore(context)
    val dummyRepo: com.example.data.ArusKasRepository? = null

    // Launch #1 (Fresh Install): Questionnaire & Guide have NOT been seen yet
    val vmLaunch1 = ArusKasViewModel(dummyRepo, null, store1)
    org.junit.Assert.assertFalse(vmLaunch1.userProfile.value.isCompleted)
    org.junit.Assert.assertFalse(vmLaunch1.appSettings.value.hasSeenUserGuide)

    // User customizes Banks & E-Wallets in Questionnaire (removes unused, adds BRI & Uang Tunai)
    val customWallets = listOf(
      com.example.data.Wallet(id = "wallet_bca", name = "Bank BCA", walletType = "BANK", balance = 4_000_000L),
      com.example.data.Wallet(id = "wallet_bri", name = "Bank BRI", walletType = "BANK", balance = 1_500_000L),
      com.example.data.Wallet(id = "wallet_dana", name = "DANA", walletType = "EWALLET", balance = 500_000L),
      com.example.data.Wallet(id = "wallet_tunai", name = "Uang Tunai (Cash)", walletType = "CASH", balance = 250_000L)
    )
    val configuredBills = listOf(
      com.example.data.MandatoryBillItem(
        id = "bill_kosan",
        name = "Bayar Kosan",
        totalAmount = 850_000L,
        paidAmount = 0L,
        dueScheduleLabel = "Bayar setiap tanggal 1",
        dueDayOfMonth = 1,
        status = "UNPAID",
        billType = "KOSAN"
      )
    )

    vmLaunch1.applyFinancialProfileSetup(
      profile = com.example.data.UserFinancialProfile(
        nickname = "Kang Iwa",
        monthlyIncomeAmount = 6_000_000L
      ),
      configuredWallets = customWallets,
      configuredBills = configuredBills,
      customCategoryLimits = PresetCategories.defaultCategoryLimits
    )

    org.junit.Assert.assertTrue(vmLaunch1.userProfile.value.isCompleted)
    org.junit.Assert.assertTrue(vmLaunch1.appSettings.value.hasSeenUserGuide)

    // Simulate Restart #2, #3, #4: User closes and reopens the app repeatedly
    repeat(3) {
      val restartedStore = com.example.data.ArusKasLocalStore(context)
      val restartedVm = ArusKasViewModel(dummyRepo, null, restartedStore)
      // Must NEVER ask onboarding questions or auto-show guide again!
      org.junit.Assert.assertTrue(restartedVm.userProfile.value.isCompleted)
      org.junit.Assert.assertTrue(restartedVm.appSettings.value.hasSeenUserGuide)
      assertEquals("Kang Iwa", restartedVm.userProfile.value.nickname)
      assertEquals(6_000_000L, restartedVm.userProfile.value.monthlyIncomeAmount)

      val loadedWallets = (restartedVm.walletsState.value as com.example.ui.UiState.Success).data
      assertEquals(4, loadedWallets.size)
      org.junit.Assert.assertTrue(loadedWallets.any { it.name == "Bank BRI" && it.balance == 1_500_000L })
      org.junit.Assert.assertTrue(loadedWallets.any { it.name == "Uang Tunai (Cash)" && it.balance == 250_000L })
      org.junit.Assert.assertFalse(loadedWallets.any { it.name == "ShopeePay" })
    }
  }

  @Test
  fun `verify skipping onboarding also marks one-time completion permanently`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    val dummyRepo: com.example.data.ArusKasRepository? = null
    val vm1 = ArusKasViewModel(dummyRepo, null, store)
    org.junit.Assert.assertFalse(vm1.userProfile.value.isCompleted)

    // User clicks "Lewati" (Skip)
    vm1.dismissOrSkipOnboarding()
    org.junit.Assert.assertTrue(vm1.userProfile.value.isCompleted)
    org.junit.Assert.assertTrue(vm1.appSettings.value.hasSeenUserGuide)

    // Reopen app: still completed, never bothers user again
    val vm2 = ArusKasViewModel(dummyRepo, null, com.example.data.ArusKasLocalStore(context))
    org.junit.Assert.assertTrue(vm2.userProfile.value.isCompleted)
    org.junit.Assert.assertTrue(vm2.appSettings.value.hasSeenUserGuide)
  }

  @Test
  fun `verify mandatory bill uncheck refunds wallet and cleans up transaction history`() {
    val previousCountry = com.example.data.WorldCountryCurrencyRegistry.activeCountryCode
    val previousLang = com.example.data.WorldCountryCurrencyRegistry.activeLanguage
    try {
      com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = "ID"
      com.example.data.WorldCountryCurrencyRegistry.activeLanguage = com.example.data.AppLanguage.INDONESIAN

      val context = ApplicationProvider.getApplicationContext<Context>()
      context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()
      val vm = ArusKasViewModel(null, null, com.example.data.ArusKasLocalStore(context))

      val bcaBefore = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
      val initialBalance = bcaBefore.balance
      val kosanBill = vm.mandatoryBills.value.first { it.id == "bill_kosan" } // 850_000 UNPAID

      // 1. Check Kosan as PAID -> deducts 850_000 and creates tx_bill_bill_kosan_*
      vm.updateMandatoryBillStatus(kosanBill, "PAID", bcaBefore)
      val bcaAfterPaid = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
      assertEquals(initialBalance - kosanBill.totalAmount, bcaAfterPaid.balance)
      val txsAfterPaid = (vm.transactionsState.value as com.example.ui.UiState.Success).data
      org.junit.Assert.assertTrue(txsAfterPaid.any { it.id.startsWith("tx_bill_bill_kosan_") && it.amount == kosanBill.totalAmount })

      // 2. Uncheck Kosan back to UNPAID -> refunds 850_000 AND removes the bill transaction so expenses don't stay inflated!
      val paidKosanBill = vm.mandatoryBills.value.first { it.id == "bill_kosan" }
      vm.updateMandatoryBillStatus(paidKosanBill, "UNPAID", bcaAfterPaid)
      val bcaAfterRefund = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
      assertEquals(initialBalance, bcaAfterRefund.balance)
      val txsAfterUnpaid = (vm.transactionsState.value as com.example.ui.UiState.Success).data
      org.junit.Assert.assertFalse(txsAfterUnpaid.any { it.id.startsWith("tx_bill_bill_kosan_") })
    } finally {
      com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = previousCountry
      com.example.data.WorldCountryCurrencyRegistry.activeLanguage = previousLang
    }
  }

  @Test
  fun `verify inter-wallet transfer and savings allocation deletion reversal`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = ArusKasViewModel(null, null, com.example.data.ArusKasLocalStore(context))

    val wallets0 = (vm.walletsState.value as com.example.ui.UiState.Success).data
    val bca0 = wallets0.first { it.id == "wallet_bca" }
    val dana0 = wallets0.first { it.id == "wallet_dana" }

    // 1. Transfer Rp 200.000 from BCA to DANA with Rp 1.000 admin fee
    vm.transferBetweenWallets(
      fromWallet = bca0,
      toWallet = dana0,
      amount = 200_000L,
      adminFee = 1_000L,
      note = "Top up DANA"
    )

    val wallets1 = (vm.walletsState.value as com.example.ui.UiState.Success).data
    val bca1 = wallets1.first { it.id == "wallet_bca" }
    val dana1 = wallets1.first { it.id == "wallet_dana" }
    assertEquals(bca0.balance - 201_000L, bca1.balance)
    assertEquals(dana0.balance + 200_000L, dana1.balance)

    // Only the Rp 1.000 admin fee is an EXPENSE; the Rp 200.000 transfer is type TRANSFER
    val txs1 = (vm.transactionsState.value as com.example.ui.UiState.Success).data
    val totalExpenses = txs1.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    assertEquals(1_000L, totalExpenses)

    // 2. Allocate Rp 100.000 savings to Goal, then delete that transaction -> both wallet & goal revert
    val goal0 = (vm.goalsState.value as com.example.ui.UiState.Success).data.first()
    val savedBefore = goal0.savedAmount
    vm.allocateSavingsToGoal(goal0, dana1, 100_000L, "Nabung rutin")

    val goalAfterSave = (vm.goalsState.value as com.example.ui.UiState.Success).data.first()
    assertEquals(savedBefore + 100_000L, goalAfterSave.savedAmount)

    val savingsTx = (vm.transactionsState.value as com.example.ui.UiState.Success).data.first { it.type == "SAVINGS_ALLOCATION" }
    val currentWallets = (vm.walletsState.value as com.example.ui.UiState.Success).data
    vm.deleteTransaction(savingsTx, currentWallets)

    val goalAfterDelete = (vm.goalsState.value as com.example.ui.UiState.Success).data.first()
    val danaAfterDelete = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_dana" }
    assertEquals(savedBefore, goalAfterDelete.savedAmount)
    assertEquals(dana1.balance, danaAfterDelete.balance)
  }

  @Test
  fun `verify custom category calendar dates multi-day transactions and savings target updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = ArusKasViewModel(null, null, com.example.data.ArusKasLocalStore(context))

    val now = System.currentTimeMillis()
    val yesterday = now - 24L * 60L * 60L * 1000L
    val tomorrow = now + 24L * 60L * 60L * 1000L

    // 1. Add custom category with calendar date range (Dari Kapan -> Sampai Kapan)
    vm.updateCategoryLimit(
      categoryKey = "cat_custom_kopi",
      categoryLabel = "Ngopi & Coworking",
      limitAmount = 300_000L,
      period = "BULANAN",
      customSubtitle = "WFC Bandung",
      startDateMillis = yesterday,
      endDateMillis = tomorrow,
      isCustomCategory = true
    )

    val customCat = vm.categoryLimits.value["cat_custom_kopi"]
    org.junit.Assert.assertNotNull(customCat)
    assertEquals("Ngopi & Coworking", customCat?.customLabel)
    assertEquals("WFC Bandung", customCat?.customSubtitle)
    assertEquals(300_000L, customCat?.limitAmount)
    org.junit.Assert.assertTrue(customCat?.isCustomCategory == true)

    // 2. Record transactions on Yesterday, Today, and Tomorrow
    val bca = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    vm.recordTransaction(
      wallet = bca,
      type = "EXPENSE",
      amount = 45_000L,
      categoryKey = "cat_custom_kopi",
      categoryLabel = "Ngopi & Coworking",
      frequencyTag = "HARIAN",
      note = "Kopi Kemarin",
      customDateMillis = yesterday
    )
    val bcaAfter1 = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    vm.recordTransaction(
      wallet = bcaAfter1,
      type = "EXPENSE",
      amount = 55_000L,
      categoryKey = "cat_custom_kopi",
      categoryLabel = "Ngopi & Coworking",
      frequencyTag = "HARIAN",
      note = "Kopi Hari Ini",
      customDateMillis = now
    )

    val spentCustomRange = ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "cat_custom_kopi",
      period = "BULANAN",
      transactions = (vm.transactionsState.value as com.example.ui.UiState.Success).data,
      mandatoryBills = emptyList(),
      startDateMillis = yesterday,
      endDateMillis = tomorrow
    )
    assertEquals(100_000L, spentCustomRange)

    // 3. Update Savings Goal target name, price, and start date
    val firstGoal = (vm.goalsState.value as com.example.ui.UiState.Success).data.first()
    vm.updateSavingsGoal(
      goalId = firstGoal.id,
      title = "MacBook Air M3 15-inch",
      targetAmount = 21_500_000L,
      dailyRecommendation = 100_000L,
      startDateMillis = yesterday,
      note = "Target Kerja Remote"
    )
    val updatedGoal = (vm.goalsState.value as com.example.ui.UiState.Success).data.first { it.id == firstGoal.id }
    assertEquals("MacBook Air M3 15-inch", updatedGoal.title)
    assertEquals(21_500_000L, updatedGoal.targetAmount)
    assertEquals(yesterday, updatedGoal.startDateMillis)
  }

  @Test
  fun `verify offline 10000 phrase sqlite database and four financial insight engines`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.offlineinsight.OfflineInsightDatabaseHelper.resetInstanceForTesting()
    val dbHelper = com.example.data.offlineinsight.OfflineInsightDatabaseHelper.getInstance(context)

    // B. PENGUJIAN DATABASE (10.000 Kalimat Unik & Validasi Penuh)
    val report = dbHelper.validateDatabaseIntegrity()
    org.junit.Assert.assertTrue("Validation errors: ${report.validationErrors}", report.isValid)
    assertEquals(10_000, report.totalCount)
    assertEquals(10_000, report.uniqueIdCount)
    assertEquals(0, report.emptyTextCount)
    assertEquals(0, report.duplicateTextCount)
    assertEquals(0, report.invalidPersonaCount)
    assertEquals(0, report.invalidCategoryCount)
    assertEquals(0, report.invalidPlaceholderCount)
    assertEquals(0, report.missingConditionCount)

    // Verify exact distribution per persona (900x5 + 800x5 + 750x2 = 10,000)
    assertEquals(900, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_ANALIS])
    assertEquals(900, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_ROASTER])
    assertEquals(900, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_BESTIE])
    assertEquals(900, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_MOTIVATOR])
    assertEquals(900, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_STRATEGIS])
    assertEquals(800, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_REALISTIS])
    assertEquals(800, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_DETEKTIF])
    assertEquals(800, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_EMPATI])
    assertEquals(800, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_AMBISIUS])
    assertEquals(800, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_SARKAS])
    assertEquals(750, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_MENTOR])
    assertEquals(750, report.countsByPersona[com.example.data.offlineinsight.InsightPersona.SI_HYPE])

    // C. PENGUJIAN FINANCIAL ANALYSIS ENGINE & RESPONSE ENGINE
    val onboardingEngine = com.example.data.offlineinsight.OnboardingEngine(dbHelper)
    val analysisEngine = com.example.data.offlineinsight.FinancialAnalysisEngine()
    val personalityEngine = com.example.data.offlineinsight.PersonalityEngine()
    val responseEngine = com.example.data.offlineinsight.ResponseEngine(
      dbHelper = dbHelper,
      onboardingEngine = onboardingEngine,
      analysisEngine = analysisEngine,
      personalityEngine = personalityEngine
    )

    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L

    // 1. Kondisi Tidak Ada Transaksi (Fresh Start / 0 Data)
    val emptySnapshot = analysisEngine.analyze(
      wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 0L)),
      transactions = emptyList(),
      goals = emptyList(),
      categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
      mandatoryBills = emptyList(),
      emergencyFund = com.example.data.EmergencyFundConfig(0L, 0L),
      userProfile = com.example.data.UserFinancialProfile(countryCode = "ID", currencyCode = "IDR"),
      nowMillis = now
    )
    org.junit.Assert.assertFalse(emptySnapshot.hasSufficientTransactions)

    // 2. Kondisi Saldo Kritis + Tagihan Mendekati Jatuh Tempo (Combined Condition)
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = now }
    val todayDom = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val dueSoonDay = if (todayDom <= 26) todayDom + 2 else todayDom
    val criticalSnapshot = analysisEngine.analyze(
      wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 100_000L)),
      transactions = listOf(
        com.example.data.TransactionRecord(
          id = "tx1",
          type = "EXPENSE",
          amount = 2_500_000L,
          categoryKey = "kebutuhan_pokok",
          categoryLabel = "Kebutuhan Harian",
          transactionDateMillis = now - dayMs
        )
      ),
      goals = emptyList(),
      categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
      mandatoryBills = listOf(
        com.example.data.MandatoryBillItem(
          id = "b1",
          name = "Sewa Kos",
          totalAmount = 850_000L,
          paidAmount = 0L,
          dueScheduleLabel = "Jatuh tempo segera",
          dueDayOfMonth = dueSoonDay,
          status = "UNPAID"
        )
      ),
      emergencyFund = com.example.data.EmergencyFundConfig(0L, 0L),
      userProfile = com.example.data.UserFinancialProfile(
        countryCode = "ID",
        currencyCode = "IDR",
        monthlyIncomeAmount = 4_000_000L
      ),
      nowMillis = now
    )
    org.junit.Assert.assertTrue(
      criticalSnapshot.detectedConditions.contains(com.example.data.offlineinsight.FinancialConditionCode.BALANCE_INSUFFICIENT_FOR_BILLS) ||
        criticalSnapshot.detectedConditions.contains(com.example.data.offlineinsight.FinancialConditionCode.COMBO_LOW_BALANCE_BILL_DUE)
    )
    assertEquals(1, criticalSnapshot.highestPriorityLevel)

    // 3. Kondisi Saldo Aman + Target Tabungan Tercapai + Pengeluaran Terkendali
    val achievedSnapshot = analysisEngine.analyze(
      wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 8_500_000L)),
      transactions = listOf(
        com.example.data.TransactionRecord(
          id = "tx_inc_prev",
          type = "INCOME",
          amount = 5_000_000L,
          categoryKey = "gaji_bulanan",
          transactionDateMillis = now - 40L * dayMs
        ),
        com.example.data.TransactionRecord(
          id = "tx_inc_curr",
          type = "INCOME",
          amount = 6_500_000L,
          categoryKey = "gaji_bulanan",
          transactionDateMillis = now - 5L * dayMs
        ),
        com.example.data.TransactionRecord(
          id = "tx_exp_curr",
          type = "EXPENSE",
          amount = 400_000L,
          categoryKey = "kebutuhan_pokok",
          categoryLabel = "Kebutuhan Harian",
          transactionDateMillis = now - 2L * dayMs
        )
      ),
      goals = listOf(
        com.example.data.SavingsGoal(
          id = "g1",
          title = "Dana Laptop",
          targetAmount = 5_000_000L,
          savedAmount = 5_000_000L
        )
      ),
      categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
      mandatoryBills = emptyList(),
      emergencyFund = com.example.data.EmergencyFundConfig(3_000_000L, 2_000_000L),
      userProfile = com.example.data.UserFinancialProfile(
        countryCode = "ID",
        currencyCode = "IDR",
        monthlyIncomeAmount = 6_500_000L
      ),
      nowMillis = now
    )
    org.junit.Assert.assertTrue(
      achievedSnapshot.detectedConditions.contains(com.example.data.offlineinsight.FinancialConditionCode.SAVINGS_GOAL_REACHED)
    )
    org.junit.Assert.assertTrue(
      achievedSnapshot.detectedConditions.contains(com.example.data.offlineinsight.FinancialConditionCode.INCOME_INCREASED)
    )
    org.junit.Assert.assertTrue(
      achievedSnapshot.detectedConditions.contains(com.example.data.offlineinsight.FinancialConditionCode.COMBO_GOAL_REACHED_EXPENSE_CONTROLLED)
    )

    // D. PENGUJIAN PERSONALISASI (12 Persona, Anti-Roast, Anti-Repetition, Placeholder Validation)
    for (persona in com.example.data.offlineinsight.InsightPersona.entries) {
      val manualPref = com.example.data.offlineinsight.UserCommunicationPreferences(
        isManualPersonaMode = true,
        manualPersona = persona,
        allowRoastingAndSarcasm = true
      )
      val insight = responseEngine.generateInsight(
        wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 2_500_000L)),
        transactions = listOf(
          com.example.data.TransactionRecord(
            id = "tx1",
            type = "EXPENSE",
            amount = 1_950_000L,
            categoryKey = "kebutuhan_pokok",
            categoryLabel = "Kebutuhan Harian",
            transactionDateMillis = now - dayMs
          )
        ),
        goals = listOf(com.example.data.SavingsGoal(id = "g1", title = "Tabungan", targetAmount = 4_000_000L, savedAmount = 1_000_000L)),
        categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
        mandatoryBills = emptyList(),
        emergencyFund = com.example.data.EmergencyFundConfig(500_000L, 1_000_000L),
        userProfile = com.example.data.UserFinancialProfile(countryCode = "ID", currencyCode = "IDR", monthlyIncomeAmount = 4_500_000L),
        preferencesOverride = manualPref,
        nowMillis = now
      )
      assertEquals(persona, insight.persona)
      // Pastikan TIDAK ADA placeholder mentah {saldo}, {total_pengeluaran}, dll
      org.junit.Assert.assertFalse("Raw placeholder found in ID: ${insight.resolvedMessageId}", insight.resolvedMessageId.contains("{"))
      org.junit.Assert.assertFalse("Raw placeholder found in EN: ${insight.resolvedMessageEn}", insight.resolvedMessageEn.contains("{"))
      org.junit.Assert.assertFalse("Raw placeholder found in Action: ${insight.recommendedActionId}", insight.recommendedActionId.contains("{"))
    }

    // Uji Anti-Roast (ketika allowRoastingAndSarcasm = false, SI_ROASTER dan SI_SARKAS tidak digunakan)
    val noRoastPrefs = com.example.data.offlineinsight.UserCommunicationPreferences(
      isManualPersonaMode = true,
      manualPersona = com.example.data.offlineinsight.InsightPersona.SI_ROASTER,
      allowRoastingAndSarcasm = false
    )
    val resolvedNoRoast = personalityEngine.resolveActivePersona(
      prefs = noRoastPrefs,
      primaryCondition = com.example.data.offlineinsight.FinancialConditionCode.BUDGET_EXCEEDED,
      priorityLevel = 2
    )
    org.junit.Assert.assertFalse(resolvedNoRoast.usesHumorOrRoast)

    // Uji Anti-Repetition: 5 pemanggilan berturut-turut menghasilkan phraseId yang berbeda
    val seenPhraseIds = mutableSetOf<String>()
    for (step in 0 until 5) {
      val nextInsight = responseEngine.generateInsight(
        wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 1_200_000L)),
        transactions = listOf(
          com.example.data.TransactionRecord(
            id = "tx_ar_$step",
            type = "EXPENSE",
            amount = 750_000L,
            categoryKey = "gaya_hidup",
            categoryLabel = "Konsumsi & Hiburan",
            transactionDateMillis = now - dayMs
          )
        ),
        goals = emptyList(),
        categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
        mandatoryBills = emptyList(),
        emergencyFund = com.example.data.EmergencyFundConfig(0L, 0L),
        userProfile = com.example.data.UserFinancialProfile(countryCode = "ID", currencyCode = "IDR", monthlyIncomeAmount = 4_000_000L),
        variationOffset = step,
        nowMillis = now + step * 1000L
      )
      seenPhraseIds.add(nextInsight.phraseId)
    }
    assertEquals(5, seenPhraseIds.size)
  }

  @Test
  fun `verify full json backup restore salted sha256 pin storage inspector and admin contact`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()
    com.example.data.PinSecurityManager.clearPin(context)

    // 1. Verify Official Admin Identity & Email Constants
    assertEquals("iwa.ye", com.example.data.AdminSupportHelper.ADMIN_NAME)
    assertEquals("programerway@gmail.com", com.example.data.AdminSupportHelper.ADMIN_EMAIL)

    // 2. Verify Salted SHA-256 PIN Security (never stored in plain text)
    org.junit.Assert.assertFalse(com.example.data.PinSecurityManager.hasPinConfigured(context))
    org.junit.Assert.assertFalse(com.example.data.PinSecurityManager.setPin(context, "12")) // too short
    org.junit.Assert.assertTrue(com.example.data.PinSecurityManager.setPin(context, "258096"))
    org.junit.Assert.assertTrue(com.example.data.PinSecurityManager.hasPinConfigured(context))
    val storedHash = com.example.data.PinSecurityManager.getStoredHashForAudit(context)
    org.junit.Assert.assertNotNull(storedHash)
    org.junit.Assert.assertNotEquals("258096", storedHash)
    assertEquals(64, storedHash?.length) // 256-bit hex digest
    org.junit.Assert.assertTrue(com.example.data.PinSecurityManager.verifyPin(context, "258096"))
    org.junit.Assert.assertFalse(com.example.data.PinSecurityManager.verifyPin(context, "111111"))

    // 3. Setup ViewModel with real transactions, wallets, goals, and communication preferences
    val store = com.example.data.ArusKasLocalStore(context)
    val vm = ArusKasViewModel(null, null, store)
    val bca0 = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    vm.recordTransaction(
      wallet = bca0,
      type = "INCOME",
      amount = 3_000_000L,
      categoryKey = "gaji_bulanan",
      categoryLabel = "Gaji Bulanan",
      frequencyTag = "BULANAN",
      note = "Gaji November"
    )
    val bca1 = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    vm.recordTransaction(
      wallet = bca1,
      type = "EXPENSE",
      amount = 250_000L,
      categoryKey = "kebutuhan_pokok",
      categoryLabel = "Kebutuhan Harian",
      frequencyTag = "HARIAN",
      note = "Belanja Dapur"
    )

    val expectedBcaBalance = bca0.balance + 3_000_000L - 250_000L
    val currentBca = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    assertEquals(expectedBcaBalance, currentBca.balance)

    val customCommPrefs = com.example.data.offlineinsight.UserCommunicationPreferences(
      isManualPersonaMode = true,
      manualPersona = com.example.data.offlineinsight.InsightPersona.SI_STRATEGIS,
      allowRoastingAndSarcasm = false,
      strictnessLevel = "STRICT"
    )

    // 4. Create Full Backup JSON & Validate Integrity
    val backupJson = vm.createFullBackupJson(customCommPrefs)
    org.junit.Assert.assertTrue(backupJson.contains("sha256Checksum"))
    val validationRes = com.example.data.ArusDompetBackupManager.validateAndParseBackupJson(backupJson)
    org.junit.Assert.assertTrue(validationRes is com.example.data.BackupValidationResult.Valid)
    val parsedBackup = (validationRes as com.example.data.BackupValidationResult.Valid).backup
    assertEquals(2, parsedBackup.transactions.size)
    assertEquals(com.example.data.offlineinsight.InsightPersona.SI_STRATEGIS, parsedBackup.communicationPreferences?.manualPersona)

    // 5. Verify Corrupted JSON / Tampered Balance is Rejected by SHA-256 Checksum
    val tamperedJson = backupJson.replace("Belanja Dapur", "Belanja Tampered").replace("250000", "999999")
    val tamperedRes = com.example.data.ArusDompetBackupManager.validateAndParseBackupJson(tamperedJson)
    org.junit.Assert.assertTrue(tamperedRes is com.example.data.BackupValidationResult.Invalid)

    val malformedRes = com.example.data.ArusDompetBackupManager.validateAndParseBackupJson("{broken_json")
    org.junit.Assert.assertTrue(malformedRes is com.example.data.BackupValidationResult.Invalid)

    // 6. Test Safe Cache Cleaner (clears temporary cache without touching transactions/wallets)
    val tempExportFile = com.example.data.ArusDompetBackupManager.exportBackupToCacheFile(context, backupJson)
    org.junit.Assert.assertTrue(tempExportFile.exists())
    val breakdownBefore = com.example.data.ArusDompetStorageInspector.calculateStorageBreakdown(context)
    org.junit.Assert.assertTrue(breakdownBefore.temporaryCacheBytes > 0L)
    org.junit.Assert.assertTrue(breakdownBefore.financialDataBytes > 0L)

    com.example.data.ArusDompetStorageInspector.clearTemporaryCacheOnly(context)
    val txsAfterCacheClean = (vm.transactionsState.value as com.example.ui.UiState.Success).data
    assertEquals(2, txsAfterCacheClean.size)

    // 7. Test Factory Reset to 0 -> Then Restore from Validated Backup (Replace & Merge without duplicates)
    vm.resetAllFinancialDataToZero()
    assertEquals(0, (vm.transactionsState.value as com.example.ui.UiState.Success).data.size)
    assertEquals(0L, (vm.walletsState.value as com.example.ui.UiState.Success).data.sumOf { it.balance })

    // Restore (Replace Mode)
    vm.restoreFromBackup(parsedBackup, mergeWithoutDuplicates = false)
    assertEquals(2, (vm.transactionsState.value as com.example.ui.UiState.Success).data.size)
    val restoredBca = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    assertEquals(expectedBcaBalance, restoredBca.balance)

    // Restore again in Merge Mode -> Must NOT duplicate existing transactions or double-count balance!
    vm.restoreFromBackup(parsedBackup, mergeWithoutDuplicates = true)
    assertEquals(2, (vm.transactionsState.value as com.example.ui.UiState.Success).data.size)
    val mergedBca = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    assertEquals(expectedBcaBalance, mergedBca.balance)
  }

  @Test
  fun `verify ethical freemium 3-tier monetization feature gates and non-lockout state machine`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    // 1. Verify PPP Pricing Catalog (ID vs US) and 3-Tier Plan Structure
    val idPricing = com.example.data.MonetizationPricingCatalog.getPricingForCountry("ID")
    assertEquals("Rp 15.000", idPricing.monthlyFormatted)
    assertEquals("Rp 99.000", idPricing.annualFormatted)
    assertEquals("Rp 199.000", idPricing.lifetimeFormatted)
    assertEquals(45, idPricing.annualSavingsPercent)

    val usPricing = com.example.data.MonetizationPricingCatalog.getPricingForCountry("US")
    assertEquals("$2.49", usPricing.monthlyFormatted)
    assertEquals("$14.99", usPricing.annualFormatted)
    assertEquals("$29.99", usPricing.lifetimeFormatted)

    // 2. Verify Expired Trial transitions gracefully to Free Forever (hasProAccess = false)
    val thirtyFiveDaysAgo = System.currentTimeMillis() - (35L * 86_400_000L)
    var freeSub = com.example.data.SubscriptionConfig(
      isProActive = false,
      trialStartDateMillis = thirtyFiveDaysAgo
    )
    org.junit.Assert.assertFalse(freeSub.isTrialActive)
    org.junit.Assert.assertFalse(freeSub.hasProAccess)
    assertEquals(0, freeSub.remainingTrialDays)
    assertEquals("FREE_FOREVER", freeSub.statusTier)

    // 3. Verify Ethical Feature Gate Evaluator on Free Tier vs PRO Tier
    val evaluator = com.example.data.ArusKasFeatureGateEvaluator
    // Wallets: up to 5 free, 6th requires PRO
    org.junit.Assert.assertTrue(evaluator.canAddNewWallet(4, freeSub))
    org.junit.Assert.assertFalse(evaluator.canAddNewWallet(5, freeSub))
    // Custom Categories: 1st custom category free, 2nd requires PRO
    org.junit.Assert.assertTrue(evaluator.canAddNewCustomCategory(0, freeSub))
    org.junit.Assert.assertFalse(evaluator.canAddNewCustomCategory(1, freeSub))
    // Custom Calendar Date Range (RENTANG_TANGGAL): requires PRO
    org.junit.Assert.assertFalse(evaluator.canUseCustomDateRangePeriod(freeSub))
    // Savings Goals: up to 2 free, 3rd requires PRO
    org.junit.Assert.assertTrue(evaluator.canAddNewSavingsGoal(1, freeSub))
    org.junit.Assert.assertFalse(evaluator.canAddNewSavingsGoal(2, freeSub))
    // Mandatory Bills: up to 4 free, 5th requires PRO
    org.junit.Assert.assertTrue(evaluator.canAddNewMandatoryBill(3, freeSub))
    org.junit.Assert.assertFalse(evaluator.canAddNewMandatoryBill(4, freeSub))
    // Styled PDF Export & Smart Merge Restore & Deep Insight Analysis: require PRO
    org.junit.Assert.assertFalse(evaluator.canExportStyledPdf(freeSub))
    org.junit.Assert.assertFalse(evaluator.canUseSmartMergeRestore(freeSub))
    org.junit.Assert.assertFalse(evaluator.canOpenDeepInsightAnalysis(freeSub))
    // Manual Insight Refreshes: 3 per day free, 4th requires PRO
    assertEquals(3, freeSub.remainingFreeInsightRefreshesToday())
    org.junit.Assert.assertTrue(evaluator.canRefreshInsightToday(freeSub))
    freeSub = freeSub.recordManualInsightRefresh().recordManualInsightRefresh().recordManualInsightRefresh()
    assertEquals(0, freeSub.remainingFreeInsightRefreshesToday())
    org.junit.Assert.assertFalse(evaluator.canRefreshInsightToday(freeSub))
    // Personas: SI_BESTIE, SI_ANALIS, SI_MOTIVATOR are 100% Free; SI_ROASTER, SI_SARKAS, etc. require PRO
    org.junit.Assert.assertTrue(evaluator.isPersonaUnlocked("SI_BESTIE", freeSub))
    org.junit.Assert.assertTrue(evaluator.isPersonaUnlocked("SI_ANALIS", freeSub))
    org.junit.Assert.assertTrue(evaluator.isPersonaUnlocked("SI_MOTIVATOR", freeSub))
    org.junit.Assert.assertFalse(evaluator.isPersonaUnlocked("SI_ROASTER", freeSub))
    org.junit.Assert.assertFalse(evaluator.isPersonaUnlocked("SI_SARKAS", freeSub))
    // Themes & Stealth Masking: ROYAL_BLUE is Free; EMERALD/COBALT/VIOLET & stealth masking require PRO
    org.junit.Assert.assertTrue(evaluator.isAccentThemeUnlocked("ROYAL_BLUE", freeSub))
    org.junit.Assert.assertFalse(evaluator.isAccentThemeUnlocked("EMERALD", freeSub))
    org.junit.Assert.assertFalse(evaluator.canUseNotificationStealthMasking(freeSub))

    // 4. Upgrade to LIFETIME PRO and verify all gates unlock and persist via ArusKasLocalStore
    val lifetimeSub = freeSub.copy(
      isProActive = true,
      planTier = com.example.data.ProPlanTier.LIFETIME.planCode,
      productId = com.example.data.ProPlanTier.LIFETIME.productId,
      proActivatedDateMillis = System.currentTimeMillis(),
      hasDismissedExpiredTrialNotice = true
    )
    org.junit.Assert.assertTrue(lifetimeSub.hasProAccess)
    org.junit.Assert.assertTrue(lifetimeSub.isLifetimePro)
    assertEquals("PRO_LIFETIME", lifetimeSub.statusTier)
    org.junit.Assert.assertTrue(evaluator.canAddNewWallet(10, lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canAddNewCustomCategory(5, lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canUseCustomDateRangePeriod(lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canAddNewSavingsGoal(8, lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canAddNewMandatoryBill(12, lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canExportStyledPdf(lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canUseSmartMergeRestore(lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canOpenDeepInsightAnalysis(lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canRefreshInsightToday(lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.isPersonaUnlocked("SI_ROASTER", lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.isAccentThemeUnlocked("EMERALD", lifetimeSub))
    org.junit.Assert.assertTrue(evaluator.canUseNotificationStealthMasking(lifetimeSub))

    val localStore = com.example.data.ArusKasLocalStore(context)
    localStore.saveAppSettings(com.example.data.AppSettings(subscription = lifetimeSub))
    val loadedSub = localStore.loadAppSettings().subscription
    org.junit.Assert.assertTrue(loadedSub.isProActive)
    org.junit.Assert.assertTrue(loadedSub.isLifetimePro)
    assertEquals("LIFETIME", loadedSub.planTier)
    assertEquals("aruskas_pro_lifetime", loadedSub.productId)
    org.junit.Assert.assertTrue(loadedSub.hasDismissedExpiredTrialNotice)

    // 5. Verify PlayBillingMonetizationManager: No-purchase restore, Pending Payment -> Settled, HMAC Tamper Detection & Anti-Clock-Rollback
    context.getSharedPreferences(com.example.data.PlayBillingMonetizationManager.VAULT_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
    val billingManager = com.example.data.PlayBillingMonetizationManager(context)

    // 5a. Restore when no purchase exists -> must return NoActivePurchaseFound (not a fake success)
    var restoreRes1: com.example.data.RestorePurchaseOutcome? = null
    billingManager.restorePurchases(freeSub) { restoreRes1 = it }
    org.junit.Assert.assertTrue(restoreRes1 is com.example.data.RestorePurchaseOutcome.NoActivePurchaseFound)

    // 5b. When strict official Play Billing is required and Play Store is disconnected -> BILLING_UNAVAILABLE (never grants PRO on click!)
    var unavailableOutcome: com.example.data.BillingPurchaseOutcome? = null
    billingManager.launchPurchaseFlow(
      activity = null,
      selectedPlan = com.example.data.ProPlanTier.ANNUAL,
      currentSub = freeSub,
      allowQaSandboxFallback = false
    ) { unavailableOutcome = it }
    org.junit.Assert.assertTrue(unavailableOutcome is com.example.data.BillingPurchaseOutcome.BillingUnavailable)
    assertEquals(com.example.data.EntitlementState.BILLING_UNAVAILABLE, billingManager.entitlementState.value)

    // 5c. Pending retail/e-wallet payment (Indomaret / GoPay / VA) sets PREMIUM_PENDING and does NOT grant PRO until settled
    var pendingOutcome: com.example.data.BillingPurchaseOutcome? = null
    billingManager.launchPurchaseFlow(
      activity = null,
      selectedPlan = com.example.data.ProPlanTier.ANNUAL,
      currentSub = freeSub,
      simulatePendingRetailPayment = true
    ) { pendingOutcome = it }
    org.junit.Assert.assertTrue(pendingOutcome is com.example.data.BillingPurchaseOutcome.PendingPayment)
    assertEquals(com.example.data.EntitlementState.PREMIUM_PENDING, billingManager.entitlementState.value)
    val pendingReceipt = (pendingOutcome as com.example.data.BillingPurchaseOutcome.PendingPayment).receipt
    org.junit.Assert.assertFalse(pendingReceipt.isCurrentlyEntitled())

    // 5d. Settle pending payment -> transitions to PURCHASED_AND_ACKNOWLEDGED and PREMIUM_ACTIVE
    var settledOutcome: com.example.data.BillingPurchaseOutcome? = null
    billingManager.settlePendingPayment(freeSub) { settledOutcome = it }
    org.junit.Assert.assertTrue(settledOutcome is com.example.data.BillingPurchaseOutcome.Activated)
    assertEquals(com.example.data.EntitlementState.PREMIUM_ACTIVE, billingManager.entitlementState.value)
    val settledAct = settledOutcome as com.example.data.BillingPurchaseOutcome.Activated
    org.junit.Assert.assertFalse(settledAct.isDuplicateIgnored)
    val settledSub = settledAct.updatedSubscription
    org.junit.Assert.assertTrue(settledSub.isProActive)
    assertEquals("ANNUAL", settledSub.planTier)
    assertEquals("aruskas_pro_annual", settledSub.productId)

    // 5e. Idempotent duplicate purchase callback protection -> repeated callback with same purchaseToken is ignored
    var duplicateOutcome: com.example.data.BillingPurchaseOutcome? = null
    billingManager.processVerifiedPurchaseToken(
      orderId = settledAct.receipt.orderId,
      productId = settledAct.receipt.productId,
      plan = com.example.data.ProPlanTier.ANNUAL,
      purchaseToken = settledAct.receipt.purchaseToken,
      purchaseTimeMillis = settledAct.receipt.purchaseTimeMillis,
      verificationSource = settledAct.receipt.verificationSource,
      autoRenewing = true,
      baseSub = settledSub
    ) { duplicateOutcome = it }
    org.junit.Assert.assertTrue((duplicateOutcome as com.example.data.BillingPurchaseOutcome.Activated).isDuplicateIgnored)

    // 5f. Real Restore Purchases restores the verified HMAC-SHA256 receipt
    var restoreRes2: com.example.data.RestorePurchaseOutcome? = null
    billingManager.restorePurchases(freeSub) { restoreRes2 = it }
    org.junit.Assert.assertTrue(restoreRes2 is com.example.data.RestorePurchaseOutcome.Restored)

    // 5g. Subscription Expiration -> transitions to PREMIUM_EXPIRED & preserves 100% of user financial data
    val vm = ArusKasViewModel(null, null, localStore)
    val bcaWallet = (vm.walletsState.value as com.example.ui.UiState.Success).data.first()
    vm.recordTransaction(bcaWallet, "EXPENSE", 75_000L, "kebutuhan_pokok", "Kebutuhan Harian", "HARIAN", "Makan Siang")
    val txCountBeforeExpire = (vm.transactionsState.value as com.example.ui.UiState.Success).data.size
    org.junit.Assert.assertTrue(txCountBeforeExpire >= 1)

    val expiredSub = billingManager.expireSubscriptionPeriod(settledSub)
    org.junit.Assert.assertFalse(expiredSub.isProActive)
    assertEquals(com.example.data.EntitlementState.PREMIUM_EXPIRED, billingManager.entitlementState.value)
    assertEquals(txCountBeforeExpire, (vm.transactionsState.value as com.example.ui.UiState.Success).data.size)

    // 5h. Refund & Revocation -> transitions to PREMIUM_REVOKED & preserves 100% of user financial data
    val revokedSub = billingManager.revokeEntitlementOnRefund(settledSub)
    org.junit.Assert.assertFalse(revokedSub.isProActive)
    assertEquals(com.example.data.EntitlementState.PREMIUM_REVOKED, billingManager.entitlementState.value)
    assertEquals(txCountBeforeExpire, (vm.transactionsState.value as com.example.ui.UiState.Success).data.size)

    // 5i. Tamper with stored receipt in SharedPreferences -> Rejected by HMAC-SHA256!
    val vaultPrefs = context.getSharedPreferences(com.example.data.PlayBillingMonetizationManager.VAULT_PREFS_NAME, Context.MODE_PRIVATE)
    val rawVaultJson = vaultPrefs.getString(com.example.data.PlayBillingMonetizationManager.KEY_SIGNED_RECEIPT_JSON, "") ?: ""
    val forgedJson = rawVaultJson.replace("\"planTier\":\"ANNUAL\"", "\"planTier\":\"LIFETIME\"")
    vaultPrefs.edit().putString(com.example.data.PlayBillingMonetizationManager.KEY_SIGNED_RECEIPT_JSON, forgedJson).commit()

    var restoreResTampered: com.example.data.RestorePurchaseOutcome? = null
    billingManager.restorePurchases(freeSub) { restoreResTampered = it }
    org.junit.Assert.assertTrue(restoreResTampered is com.example.data.RestorePurchaseOutcome.TamperedReceiptRejected)

    // 5j. Promotional Frequency Cap (72h cooldown) & Anti-Clock-Rollback
    val futureObserved = System.currentTimeMillis() + (10L * 86_400_000L)
    billingManager.recordMonotonicTimestamp(futureObserved)
    val rolledBackSystemClock = System.currentTimeMillis() - (5L * 86_400_000L)
    assertEquals(futureObserved, billingManager.getMonotonicSafeNowMillis(rolledBackSystemClock))

    org.junit.Assert.assertTrue(billingManager.canShowPassivePromotionalPrompt(futureObserved))
    billingManager.recordPassivePromotionalPromptShown(futureObserved)
    org.junit.Assert.assertFalse(billingManager.canShowPassivePromotionalPrompt(futureObserved + 3_600_000L)) // 1h later -> blocked
    org.junit.Assert.assertTrue(billingManager.canShowPassivePromotionalPrompt(futureObserved + (73L * 3_600_000L))) // 73h later -> allowed
  }

  @Test
  fun `verify custom category limit nominal preserved across bill check and period updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = ArusKasViewModel(null, null, com.example.data.ArusKasLocalStore(context))

    // 1. User customizes Mandatory Needs (tagihan_bulanan) limit to an exact custom nominal
    vm.updateCategoryLimit(
      categoryKey = "tagihan_bulanan",
      categoryLabel = "Kebutuhan Wajib",
      limitAmount = 2_345_600L,
      period = "BULANAN"
    )
    assertEquals(2_345_600L, vm.categoryLimits.value["tagihan_bulanan"]?.limitAmount)

    // 2. Checking / unchecking mandatory bill must NEVER overwrite user's custom limit nominal
    val bca = (vm.walletsState.value as com.example.ui.UiState.Success).data.first()
    val kosanBill = vm.mandatoryBills.value.first { it.id == "bill_kosan" }
    vm.updateMandatoryBillStatus(kosanBill, "PAID", bca)
    assertEquals(2_345_600L, vm.categoryLimits.value["tagihan_bulanan"]?.limitAmount)

    // 3. Switching to HARIAN clears stale range and saves 0L timestamps
    vm.updateCategoryLimit(
      categoryKey = "kebutuhan_pokok",
      categoryLabel = "Kebutuhan Harian",
      limitAmount = 75_500L,
      period = "HARIAN",
      startDateMillis = 123_456L,
      endDateMillis = 789_012L
    )
    val dailyCfg = vm.categoryLimits.value["kebutuhan_pokok"]
    assertEquals("HARIAN", dailyCfg?.period)
    assertEquals(75_500L, dailyCfg?.limitAmount)
    assertEquals(0L, dailyCfg?.startDateMillis)
    assertEquals(0L, dailyCfg?.endDateMillis)

    // 4. Setting period to BEBAS forces limitAmount = 0L and clears timestamps
    vm.updateCategoryLimit(
      categoryKey = "gaya_hidup",
      categoryLabel = "Konsumsi & Hiburan",
      limitAmount = 500_000L,
      period = "BEBAS"
    )
    val freeCfg = vm.categoryLimits.value["gaya_hidup"]
    assertEquals("BEBAS", freeCfg?.period)
    assertEquals(0L, freeCfg?.limitAmount)
    assertEquals(0L, freeCfg?.startDateMillis)
  }

  @Test
  fun `verify emergency fund warnings never appear in insight phrase box`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.offlineinsight.OfflineInsightDatabaseHelper.resetInstanceForTesting()
    val dbHelper = com.example.data.offlineinsight.OfflineInsightDatabaseHelper.getInstance(context)
    val onboardingEngine = com.example.data.offlineinsight.OnboardingEngine(dbHelper)
    val analysisEngine = com.example.data.offlineinsight.FinancialAnalysisEngine()
    val responseEngine = com.example.data.offlineinsight.ResponseEngine(
      dbHelper = dbHelper,
      onboardingEngine = onboardingEngine,
      analysisEngine = analysisEngine
    )

    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L
    val forbiddenEmergencyConditions = setOf(
      com.example.data.offlineinsight.FinancialConditionCode.EMERGENCY_NONE,
      com.example.data.offlineinsight.FinancialConditionCode.EMERGENCY_INSUFFICIENT,
      com.example.data.offlineinsight.FinancialConditionCode.EMERGENCY_SUFFICIENT,
      com.example.data.offlineinsight.FinancialConditionCode.EMERGENCY_USED,
      com.example.data.offlineinsight.FinancialConditionCode.EMERGENCY_REBUILD_NEEDED,
      com.example.data.offlineinsight.FinancialConditionCode.COMBO_NO_EMERGENCY_UNEXPECTED_EXPENSE
    )

    val txs = listOf(
      com.example.data.TransactionRecord(
        id = "tx_e1",
        type = "EXPENSE",
        amount = 50_000L,
        categoryKey = "kebutuhan_pokok",
        categoryLabel = "Kebutuhan Harian",
        transactionDateMillis = now - 3L * dayMs
      ),
      com.example.data.TransactionRecord(
        id = "tx_e2",
        type = "EXPENSE",
        amount = 60_000L,
        categoryKey = "kebutuhan_pokok",
        categoryLabel = "Kebutuhan Harian",
        transactionDateMillis = now - 2L * dayMs
      ),
      com.example.data.TransactionRecord(
        id = "tx_spike",
        type = "EXPENSE",
        amount = 1_200_000L,
        categoryKey = "pakai_darurat",
        categoryLabel = "Darurat",
        transactionDateMillis = now - dayMs
      )
    )

    val wallets = listOf(com.example.data.Wallet(id = "w1", name = "BCA", balance = 3_000_000L))
    val emptyEmergency = com.example.data.EmergencyFundConfig(0L, 0L)
    val profile = com.example.data.UserFinancialProfile(
      countryCode = "ID",
      currencyCode = "IDR",
      monthlyIncomeAmount = 5_000_000L
    )

    val snapshot = analysisEngine.analyze(
      wallets = wallets,
      transactions = txs,
      goals = emptyList(),
      categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
      mandatoryBills = emptyList(),
      emergencyFund = emptyEmergency,
      userProfile = profile,
      nowMillis = now
    )
    org.junit.Assert.assertTrue(snapshot.detectedConditions.none { it in forbiddenEmergencyConditions })
    org.junit.Assert.assertFalse(snapshot.primaryCondition in forbiddenEmergencyConditions)

    for (step in 0..10) {
      val insight = responseEngine.generateInsight(
        wallets = wallets,
        transactions = txs,
        goals = emptyList(),
        categoryLimits = com.example.data.PresetCategories.defaultCategoryLimits,
        mandatoryBills = emptyList(),
        emergencyFund = emptyEmergency,
        userProfile = profile,
        variationOffset = step,
        nowMillis = now + step * 1000L
      )
      org.junit.Assert.assertFalse(
        "Emergency condition ${insight.condition} must not appear in phrase box",
        insight.condition in forbiddenEmergencyConditions
      )
    }
  }

  @Test
  fun `verify home screen widget keypad transaction entry and viewmodel sync`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    store.saveAppSettings(
      com.example.data.AppSettings(
        language = com.example.data.AppLanguage.INDONESIAN,
        countryCode = "ID",
        currencyCode = "IDR"
      )
    )
    val initialWallet = com.example.data.Wallet(id = "wallet_bca", name = "Bank BCA", balance = 1_000_000L)
    store.saveWallets(listOf(initialWallet))

    // 1. Keypad digit formatting ("2", "5", "000" -> 25000, BACKSPACE -> 2500, CLEAR -> "", quick add +50000 -> 50000)
    var digits = ""
    digits = com.example.widget.ArusKasWidgetEngine.applyKeypadInput(digits, "2")
    digits = com.example.widget.ArusKasWidgetEngine.applyKeypadInput(digits, "5")
    digits = com.example.widget.ArusKasWidgetEngine.applyKeypadInput(digits, "000")
    assertEquals("25000", digits)
    digits = com.example.widget.ArusKasWidgetEngine.applyKeypadInput(digits, "BACKSPACE")
    assertEquals("2500", digits)
    digits = com.example.widget.ArusKasWidgetEngine.applyKeypadInput(digits, "CLEAR")
    assertEquals("", digits)
    digits = com.example.widget.ArusKasWidgetEngine.addPresetAmount(digits, 50_000L)
    assertEquals("50000", digits)

    // 2. ViewModel already running in background
    val vm = ArusKasViewModel(null, null, store)
    assertEquals(1_000_000L, (vm.walletsState.value as com.example.ui.UiState.Success).data.first().balance)

    // 3. User records EXPENSE of 50_000 from Home Screen Widget without opening app
    val expRes = com.example.widget.ArusKasWidgetEngine.recordTransactionFromWidget(
      context = context,
      txType = "EXPENSE",
      amount = digits.toLong(),
      categoryIndex = 1,
      walletIndex = 0
    )
    org.junit.Assert.assertTrue(expRes.success)
    assertEquals(950_000L, expRes.updatedWalletBalance)

    // 4. User records INCOME of 200_000 from Home Screen Widget
    val incRes = com.example.widget.ArusKasWidgetEngine.recordTransactionFromWidget(
      context = context,
      txType = "INCOME",
      amount = 200_000L,
      categoryIndex = 0,
      walletIndex = 0
    )
    org.junit.Assert.assertTrue(incRes.success)
    assertEquals(1_150_000L, incRes.updatedWalletBalance)

    // 5. Resuming app syncs ViewModel state with transactions & updated balance from widget
    vm.syncFromLocalStore()
    val syncedWallet = (vm.walletsState.value as com.example.ui.UiState.Success).data.first { it.id == "wallet_bca" }
    val syncedTxs = (vm.transactionsState.value as com.example.ui.UiState.Success).data
    assertEquals(1_150_000L, syncedWallet.balance)
    assertEquals(2, syncedTxs.size)
  }

  @Test
  fun `verify widget parity with transaction sheet date offset quick items and emergency fund`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    store.saveAppSettings(
      com.example.data.AppSettings(
        language = com.example.data.AppLanguage.INDONESIAN,
        countryCode = "ID",
        currencyCode = "IDR"
      )
    )
    val bcaWallet = com.example.data.Wallet(id = "wallet_bca", name = "Bank BCA", balance = 1_500_000L)
    store.saveWallets(listOf(bcaWallet))
    store.saveEmergencyFund(com.example.data.EmergencyFundConfig(emergencyBalance = 800_000L, emergencyLimit = 500_000L))

    val loaded = com.example.widget.ArusKasWidgetEngine.loadWidgetData(context)
    val dailyCatIdx = loaded.expenseCategories.indexOfFirst { it.key == "kebutuhan_pokok" }
    val emergencyCatIdx = loaded.expenseCategories.indexOfFirst { it.key == "pakai_darurat" }
    org.junit.Assert.assertTrue(dailyCatIdx >= 0)
    org.junit.Assert.assertTrue(emergencyCatIdx >= 0)
    assertEquals("Kebutuhan Harian", loaded.expenseCategories[dailyCatIdx].label)
    assertEquals("Darurat", loaded.expenseCategories[emergencyCatIdx].label)

    // Quick items match category "kebutuhan_pokok"
    val quickItems = com.example.widget.ArusKasWidgetEngine.resolveQuickItemsForCategory(
      countryCode = "ID",
      lang = com.example.data.AppLanguage.INDONESIAN,
      categoryKey = "kebutuhan_pokok",
      txType = "EXPENSE",
      userGender = loaded.userGender
    )
    org.junit.Assert.assertTrue(quickItems.isNotEmpty())
    val pickedItem = quickItems.first()

    // Record EXPENSE for "Kemarin" (dayOffset = -1) using quick item nominal & note
    val baseNow = 1_750_000_000_000L
    val yesterdayRes = com.example.widget.ArusKasWidgetEngine.recordTransactionFromWidget(
      context = context,
      txType = "EXPENSE",
      amount = pickedItem.suggestedAmount,
      categoryIndex = dailyCatIdx,
      walletIndex = 0,
      dayOffset = -1,
      note = pickedItem.label,
      nowMillis = baseNow
    )
    org.junit.Assert.assertTrue(yesterdayRes.success)
    assertEquals(baseNow - 86_400_000L, yesterdayRes.recordedTransaction?.transactionDateMillis)
    assertEquals(pickedItem.label, yesterdayRes.recordedTransaction?.note)
    assertEquals("Kebutuhan Harian", yesterdayRes.recordedTransaction?.categoryLabel)
    assertEquals(1_500_000L - pickedItem.suggestedAmount, store.loadWallets().first().balance)

    // Record Emergency Expense ("pakai_darurat") for "Besok" (dayOffset = 1) -> deducts Emergency Fund, NOT BCA wallet
    val emRes = com.example.widget.ArusKasWidgetEngine.recordTransactionFromWidget(
      context = context,
      txType = "EXPENSE",
      amount = 150_000L,
      categoryIndex = emergencyCatIdx,
      walletIndex = 0,
      dayOffset = 1,
      note = "",
      nowMillis = baseNow
    )
    org.junit.Assert.assertTrue(emRes.success)
    assertEquals(650_000L, emRes.updatedWalletBalance)
    assertEquals(650_000L, store.loadEmergencyFund().emergencyBalance)
    assertEquals("pos_darurat_terpisah", emRes.recordedTransaction?.walletId)
    assertEquals("Darurat", emRes.recordedTransaction?.note)
    assertEquals(baseNow + 86_400_000L, emRes.recordedTransaction?.transactionDateMillis)
    // Bank BCA balance must remain unchanged after emergency spend
    assertEquals(1_500_000L - pickedItem.suggestedAmount, store.loadWallets().first().balance)
  }

  @Test
  fun `verify money sanitization parsing and overflow clamping`() {
    assertEquals("999999999999", ArusKasViewModel.sanitizeMoneyInput("000999999999999888777"))
    assertEquals("-123456789012", ArusKasViewModel.sanitizeMoneyInput("  -00123456789012345 ", allowNegative = true))
    assertEquals("", ArusKasViewModel.sanitizeMoneyInput("abc"))
    assertEquals(ArusKasViewModel.MAX_MONETARY_AMOUNT, ArusKasViewModel.safeParseMoney("99999999999999999999"))
    assertEquals(-ArusKasViewModel.MAX_MONETARY_AMOUNT, ArusKasViewModel.safeParseMoney("-99999999999999999999", allowNegative = true))
    assertEquals(
      ArusKasViewModel.MAX_MONETARY_AMOUNT,
      ArusKasViewModel.safeAddBalance(Long.MAX_VALUE - 5L, 1_000L)
    )
    assertEquals(
      -ArusKasViewModel.MAX_MONETARY_AMOUNT,
      ArusKasViewModel.safeAddBalance(Long.MIN_VALUE + 5L, -1_000L)
    )
  }

  @Test
  fun `verify non-IDR currency bills goals and note preservation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    store.saveAppSettings(
      com.example.data.AppSettings(
        language = com.example.data.AppLanguage.ENGLISH,
        countryCode = "US",
        currencyCode = "USD"
      )
    )
    val vm = ArusKasViewModel(null, null, store)

    // 1. Bill of $15 must NOT be forced up to 1,000 IDR floor
    vm.upsertMandatoryBill(
      billId = null,
      name = "Cloud Storage",
      totalAmount = 15L,
      dueScheduleLabel = "Due every 5th",
      dueDayOfMonth = 5,
      renewalDayOfMonth = null,
      billType = "WIFI"
    )
    val savedBill = vm.mandatoryBills.value.first { it.name == "Cloud Storage" }
    assertEquals(15L, savedBill.totalAmount)

    // 2. Goal of $500 / $5 per day must NOT be forced up to 10,000 IDR floor, and custom note must stay preserved on empty-note update
    vm.addSavingsGoal(
      title = "Noise Cancelling Headphones",
      targetAmount = 500L,
      dailyRecommendation = 5L,
      note = "From freelance design gigs",
      iconKey = "laptop"
    )
    val goalsList = (vm.goalsState.value as com.example.ui.UiState.Success).data
    val createdGoal = goalsList.first { it.title == "Noise Cancelling Headphones" }
    assertEquals(500L, createdGoal.targetAmount)
    assertEquals(5L, createdGoal.dailyRecommendation)

    vm.updateSavingsGoal(
      goalId = createdGoal.id,
      title = "Studio Headphones",
      targetAmount = 450L,
      dailyRecommendation = 4L,
      startDateMillis = createdGoal.startDateMillis,
      note = ""
    )
    val updatedGoal = (vm.goalsState.value as com.example.ui.UiState.Success).data.first { it.id == createdGoal.id }
    assertEquals("Studio Headphones", updatedGoal.title)
    assertEquals(450L, updatedGoal.targetAmount)
    assertEquals(4L, updatedGoal.dailyRecommendation)
    assertEquals("From freelance design gigs", updatedGoal.note)
  }

  @Test
  fun `verify localized action feedback and multi-currency formatter parsing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    store.saveAppSettings(
      com.example.data.AppSettings(
        language = com.example.data.AppLanguage.ENGLISH,
        countryCode = "US",
        currencyCode = "USD"
      )
    )
    val vm = ArusKasViewModel(null, null, store)
    val chase = com.example.data.Wallet(id = "wallet_chase", name = "Chase Bank", balance = 1_200L)
    store.saveWallets(listOf(chase))
    vm.syncFromLocalStore()

    assertEquals(1_250L, com.example.ui.CurrencyInputFormatter.parseToLong("$ 1,250"))
    assertEquals("1,250", com.example.ui.CurrencyInputFormatter.formatDigitsForCountry("1250", "US"))

    vm.recordTransaction(
      wallet = chase,
      type = "EXPENSE",
      amount = 25L,
      categoryKey = "kebutuhan_pokok",
      categoryLabel = "Daily Essentials",
      frequencyTag = "HARIAN",
      note = "Lunch"
    )
    val feedbackEn = vm.actionFeedback.value ?: ""
    org.junit.Assert.assertTrue("Expected USD formatted symbol in feedback: $feedbackEn", feedbackEn.contains("$ 25"))
    org.junit.Assert.assertTrue("Expected English phrasing in feedback: $feedbackEn", feedbackEn.contains("deducted from"))

    vm.updateLanguage(com.example.data.AppLanguage.INDONESIAN)
    val idFeedback = vm.actionFeedback.value ?: ""
    org.junit.Assert.assertTrue("Expected Indonesian feedback after language switch: $idFeedback", idFeedback.contains("Bahasa Indonesia"))
  }

  @Test
  fun `verify transfer and deletion reversal with underscore wallet IDs`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    val bankFrom = com.example.data.Wallet(id = "bank_a_to_b", name = "Bank Alpha", balance = 500_000L)
    val ewalletTo = com.example.data.Wallet(id = "ewallet_dana_2", name = "DANA 2", balance = 100_000L)
    store.saveWallets(listOf(bankFrom, ewalletTo))

    val vm = ArusKasViewModel(null, null, store)
    vm.transferBetweenWallets(
      fromWallet = bankFrom,
      toWallet = ewalletTo,
      amount = 150_000L,
      adminFee = 0L,
      note = "Topup"
    )

    val walletsAfterTransfer = (vm.walletsState.value as com.example.ui.UiState.Success).data
    assertEquals(350_000L, walletsAfterTransfer.first { it.id == "bank_a_to_b" }.balance)
    assertEquals(250_000L, walletsAfterTransfer.first { it.id == "ewallet_dana_2" }.balance)

    val transferTx = (vm.transactionsState.value as com.example.ui.UiState.Success).data.first { it.type == "TRANSFER" }
    vm.deleteTransaction(transferTx, walletsAfterTransfer)

    val walletsAfterRevert = (vm.walletsState.value as com.example.ui.UiState.Success).data
    assertEquals(500_000L, walletsAfterRevert.first { it.id == "bank_a_to_b" }.balance)
    assertEquals(100_000L, walletsAfterRevert.first { it.id == "ewallet_dana_2" }.balance)
  }

  @Test
  fun `verify blank wallet name and blank goal title rejection`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE).edit().clear().commit()

    val store = com.example.data.ArusKasLocalStore(context)
    store.saveAppSettings(
      com.example.data.AppSettings(
        language = com.example.data.AppLanguage.ENGLISH,
        countryCode = "US",
        currencyCode = "USD"
      )
    )
    val initialWallet = com.example.data.Wallet(id = "w_1", name = "Main", balance = 100L)
    store.saveWallets(listOf(initialWallet))
    val vm = ArusKasViewModel(null, null, store)

    val initialGoalCount = (vm.goalsState.value as com.example.ui.UiState.Success).data.size
    vm.addOrUpdateWallet(
      walletId = null,
      name = "   ",
      walletType = "BANK",
      balance = 500L,
      colorHex = "#0066AE",
      iconKey = "bank_bca"
    )
    assertEquals(1, (vm.walletsState.value as com.example.ui.UiState.Success).data.size)
    assertEquals("Account name cannot be empty", vm.actionFeedback.value)

    vm.addSavingsGoal(
      title = "   ",
      targetAmount = 1_000L,
      dailyRecommendation = 10L,
      note = "",
      iconKey = "laptop"
    )
    assertEquals(initialGoalCount, (vm.goalsState.value as com.example.ui.UiState.Success).data.size)
    assertEquals("Goal title cannot be empty", vm.actionFeedback.value)
  }
}



