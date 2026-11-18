package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verify_custom_category_calendar_date_range_and_savings_suggestions() {
    val now = System.currentTimeMillis()
    val yesterday = now - 24L * 60L * 60L * 1000L
    val tomorrow = now + 24L * 60L * 60L * 1000L
    val threeDaysAgo = now - 3L * 24L * 60L * 60L * 1000L

    val transactions = listOf(
      com.example.data.TransactionRecord(
        id = "tx_1",
        walletId = "wallet_bca",
        walletName = "Bank BCA",
        type = "EXPENSE",
        amount = 45_000L,
        categoryKey = "cat_custom_kopi",
        categoryLabel = "Ngopi & Coworking",
        transactionDateMillis = yesterday
      ),
      com.example.data.TransactionRecord(
        id = "tx_2",
        walletId = "wallet_bca",
        walletName = "Bank BCA",
        type = "EXPENSE",
        amount = 55_000L,
        categoryKey = "cat_custom_kopi",
        categoryLabel = "Ngopi & Coworking",
        transactionDateMillis = now
      ),
      com.example.data.TransactionRecord(
        id = "tx_outside_range",
        walletId = "wallet_bca",
        walletName = "Bank BCA",
        type = "EXPENSE",
        amount = 90_000L,
        categoryKey = "cat_custom_kopi",
        categoryLabel = "Ngopi & Coworking",
        transactionDateMillis = threeDaysAgo
      )
    )

    val spentInsideCalendarWindow = com.example.ui.ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "cat_custom_kopi",
      period = "BULANAN",
      transactions = transactions,
      mandatoryBills = emptyList(),
      startDateMillis = yesterday,
      endDateMillis = tomorrow
    )
    assertEquals(100_000L, spentInsideCalendarWindow)

    assertTrue(com.example.data.PresetCategories.suggestedSavingsTargets.size >= 6)
    assertTrue(com.example.data.PresetCategories.suggestedSavingsTargets.any { it.first.contains("iPhone") })

    // Verify International English default formal category labels & Indonesian optional labels
    val gridEn = com.example.data.PresetCategories.expenseGrid3x3
    assertEquals("Mandatory Needs", gridEn.first { it.key == "tagihan_bulanan" }.label)
    assertEquals("Daily Essentials", gridEn.first { it.key == "kebutuhan_pokok" }.label)

    val gridId = com.example.data.PresetCategories.localizedExpenseCategories(com.example.data.AppLanguage.INDONESIAN)
    assertEquals("Kebutuhan Wajib", gridId.first { it.key == "tagihan_bulanan" }.label)
    assertEquals("Kebutuhan Harian", gridId.first { it.key == "kebutuhan_pokok" }.label)

    com.example.data.WorldCountryCurrencyRegistry.activeLanguage = com.example.data.AppLanguage.ENGLISH
    assertEquals(
      "Mandatory Needs",
      com.example.ui.normalizeFormalCategoryLabel("tagihan_bulanan", "Tagihan Wajib")
    )
    assertEquals(
      "Daily Essentials",
      com.example.ui.normalizeFormalCategoryLabel("kebutuhan_pokok", "Bensin & Harian")
    )

    com.example.data.WorldCountryCurrencyRegistry.activeLanguage = com.example.data.AppLanguage.INDONESIAN
    assertEquals(
      "Kebutuhan Wajib",
      com.example.ui.normalizeFormalCategoryLabel("tagihan_bulanan", "Tagihan Wajib")
    )
    assertEquals(
      "Kebutuhan Harian",
      com.example.ui.normalizeFormalCategoryLabel("kebutuhan_pokok", "Bensin & Harian")
    )
    com.example.data.WorldCountryCurrencyRegistry.activeLanguage = com.example.data.AppLanguage.ENGLISH

    // Verify feature guides default to hidden so they don't take up space
    assertFalse(com.example.ui.FeatureGuideVisibilityController.showInlineGuideButtons)
  }

  @Test
  fun verify_world_country_currency_and_international_english_defaults() {
    val defaultSettings = com.example.data.AppSettings()
    assertEquals(com.example.data.AppLanguage.ENGLISH, defaultSettings.language)
    assertEquals("US", defaultSettings.countryCode)
    assertEquals("USD", defaultSettings.currencyCode)

    // Verify countries across Americas, Asia-Pacific, and Europe/Middle East
    val countries = com.example.data.WorldCountryCurrencyRegistry.allCountries
    assertTrue(countries.size >= 15)
    assertTrue(countries.any { it.countryCode == "US" && it.currencyCode == "USD" })
    assertTrue(countries.any { it.countryCode == "ID" && it.currencyCode == "IDR" })
    assertTrue(countries.any { it.countryCode == "JP" && it.currencyCode == "JPY" })
    assertTrue(countries.any { it.countryCode == "SG" && it.currencyCode == "SGD" })
    assertTrue(countries.any { it.countryCode == "GB" && it.currencyCode == "GBP" })
    assertTrue(countries.any { it.countryCode == "EU" && it.currencyCode == "EUR" })

    // Verify country-specific banks & e-wallets follow the selected country
    val usCountry = com.example.data.WorldCountryCurrencyRegistry.getByCode("US")
    assertTrue(usCountry.presetBanks.any { it.contains("Chase") })
    assertTrue(usCountry.presetEWallets.any { it.contains("Apple Pay") || it.contains("PayPal") })

    val idCountry = com.example.data.WorldCountryCurrencyRegistry.getByCode("ID")
    assertTrue(idCountry.presetBanks.any { it.contains("BCA") })
    assertTrue(idCountry.presetEWallets.any { it.contains("GoPay") })

    val jpCountry = com.example.data.WorldCountryCurrencyRegistry.getByCode("JP")
    assertTrue(jpCountry.presetBanks.any { it.contains("MUFG") })
    assertTrue(jpCountry.presetEWallets.any { it.contains("PayPay") })

    // Verify currency formatting and exchange rate conversion sensitivity
    assertEquals("$ 4,500", com.example.data.WorldCountryCurrencyRegistry.formatMoney(4500L, "US"))
    assertEquals("Rp 1.500.000", com.example.data.WorldCountryCurrencyRegistry.formatMoney(1_500_000L, "ID"))
    assertEquals("¥ 520,000", com.example.data.WorldCountryCurrencyRegistry.formatMoney(520_000L, "JP"))

    val convertedUsdToIdr = com.example.data.WorldCountryCurrencyRegistry.convertAmount(100L, "US", "ID")
    assertEquals(1_580_000L, convertedUsdToIdr)
  }

  @Test
  fun verify_zero_nominal_fresh_start_and_accessibility_cloud_settings() {
    // 1. Fresh start: All starter wallets across all 24 countries must have exactly 0 balance
    val allCountries = com.example.data.WorldCountryCurrencyRegistry.allCountries
    for (c in allCountries) {
      for (w in c.starterWallets) {
        assertEquals("Wallet ${w.name} in country ${c.nameEn} should start with 0 balance", 0L, w.balance)
      }
    }

    // Default starter wallets must be 0
    val defaultWallets = com.example.data.PresetCategories.defaultStarterWallets
    for (w in defaultWallets) {
      assertEquals("Default wallet ${w.name} should start with 0 balance", 0L, w.balance)
    }

    // Emergency fund and financial profile must start at 0
    val defaultEmergency = com.example.data.EmergencyFundConfig()
    assertEquals(0L, defaultEmergency.emergencyBalance)
    assertEquals(0L, defaultEmergency.emergencyLimit)

    val defaultProfile = com.example.data.UserFinancialProfile()
    assertEquals(0L, defaultProfile.monthlyIncomeAmount)

    // 2. Accessibility & Cloud Sync Settings
    val settings = com.example.data.AppSettings()
    assertEquals(1.0f, settings.fontScaleFactor, 0.01f)
    assertFalse(settings.boldNumbers)
    assertFalse(settings.highContrastMode)
    assertEquals("ROYAL_BLUE", settings.accentTheme)
    assertEquals(0L, settings.lastCloudSyncMillis)
  }

  @Test
  fun verify_category_limit_periods_and_exact_nominal_formatting() {
    val calTodayNoon = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 12)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val todayNoon = calTodayNoon.timeInMillis
    val twoDaysAgo = todayNoon - 2L * 86_400_000L
    val twentyDaysAgo = todayNoon - 20L * 86_400_000L
    val tomorrow = todayNoon + 86_400_000L

    val txs = listOf(
      com.example.data.TransactionRecord(
        id = "tx_today",
        type = "EXPENSE",
        amount = 15_800L,
        categoryKey = "kebutuhan_pokok",
        transactionDateMillis = todayNoon
      ),
      com.example.data.TransactionRecord(
        id = "tx_2d_ago",
        type = "EXPENSE",
        amount = 42_500L,
        categoryKey = "kebutuhan_pokok",
        transactionDateMillis = twoDaysAgo
      ),
      com.example.data.TransactionRecord(
        id = "tx_20d_ago",
        type = "EXPENSE",
        amount = 120_000L,
        categoryKey = "kebutuhan_pokok",
        transactionDateMillis = twentyDaysAgo
      ),
      com.example.data.TransactionRecord(
        id = "tx_tomorrow",
        type = "EXPENSE",
        amount = 99_000L,
        categoryKey = "kebutuhan_pokok",
        transactionDateMillis = tomorrow
      )
    )

    // Stale monthly timestamps passed to HARIAN / MINGGUAN / BEBAS must NOT break period filtering
    val staleStart = twentyDaysAgo - 86_400_000L
    val staleEnd = tomorrow + 86_400_000L

    val dailySpent = com.example.ui.ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "kebutuhan_pokok",
      period = "HARIAN",
      transactions = txs,
      startDateMillis = staleStart,
      endDateMillis = staleEnd
    )
    assertEquals(15_800L, dailySpent)

    val weeklySpent = com.example.ui.ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "kebutuhan_pokok",
      period = "MINGGUAN",
      transactions = txs,
      startDateMillis = staleStart,
      endDateMillis = staleEnd
    )
    assertEquals(15_800L + 42_500L, weeklySpent)

    val freeSpent = com.example.ui.ArusKasViewModel.calculateSpentForCategoryLimit(
      categoryKey = "kebutuhan_pokok",
      period = "BEBAS",
      transactions = txs,
      startDateMillis = todayNoon,
      endDateMillis = todayNoon
    )
    assertEquals(15_800L + 42_500L + 120_000L + 99_000L, freeSpent)

    // Verify full exact nominal formatting preserves non-rounded numbers
    assertEquals("Rp 15.800", com.example.ui.ArusKasViewModel.formatMoney(15_800L, "ID"))
    assertEquals("Rp 1.250.000", com.example.ui.ArusKasViewModel.formatMoney(1_250_000L, "ID"))
    assertEquals("$ 1,250", com.example.ui.ArusKasViewModel.formatMoney(1_250L, "US"))
  }
}

