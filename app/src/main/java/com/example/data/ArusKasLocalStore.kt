package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Penyimpanan Lokal Permanen (Offline-First Persistent Store) untuk ArusKas.
 * Memastikan:
 * 1. Kuesioner Profil Keuangan & Panduan Aplikasi HANYA muncul 1x seumur hidup saat pertama kali pakai
 *    (tidak akan pernah muncul berulang kali walaupun aplikasi ditutup-buka berkali-kali),
 *    namun tetap bisa dibuka manual kapan saja lewat menu Pengaturan / Tombol Alat di Dashboard.
 * 2. Daftar Bank & E-Wallet yang ditambah/dikurangi pengguna, Saldo, Riwayat Transaksi,
 *    Tagihan Wajib, Limit Kategori, Tabungan Impian, dan Dana Darurat tersimpan permanen di perangkat.
 */
class ArusKasLocalStore(context: Context) {

    private val appContext: Context = context.applicationContext

    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun hasCompletedInitialOnboardingOnce(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_SEEN_ONCE, false)
    }

    fun markInitialOnboardingSeenOnce() {
        prefs.edit().putBoolean(KEY_ONBOARDING_SEEN_ONCE, true).apply()
    }

    fun saveUserProfile(profile: UserFinancialProfile) {
        val habitsArray = JSONArray()
        profile.dailyHabits.forEach { habitsArray.put(it) }

        val obj = JSONObject().apply {
            put("isCompleted", profile.isCompleted)
            put("countryCode", profile.countryCode)
            put("currencyCode", profile.currencyCode)
            put("nickname", profile.nickname)
            put("gender", profile.gender)
            put("livingStatus", profile.livingStatus)
            put("incomeSourcePrimary", profile.incomeSourcePrimary)
            put("monthlyIncomeAmount", profile.monthlyIncomeAmount)
            put("paydayDate", profile.paydayDate)
            put("dependentsCount", profile.dependentsCount)
            put("transportMode", profile.transportMode)
            put("monthlyDebtObligation", profile.monthlyDebtObligation)
            put("primaryFinancialGoal", profile.primaryFinancialGoal)
            put("dailyHabits", habitsArray)
            put("controlMode", profile.controlMode)
            put("workShiftType", profile.workShiftType)
            put("workStartHour", profile.workStartHour)
            put("workStartMinute", profile.workStartMinute)
            put("lunchBreakHour", profile.lunchBreakHour)
            put("lunchBreakMinute", profile.lunchBreakMinute)
            put("workEndHour", profile.workEndHour)
            put("workEndMinute", profile.workEndMinute)
            put("eveningReviewHour", profile.eveningReviewHour)
            put("eveningReviewMinute", profile.eveningReviewMinute)
            put("activeDaysType", profile.activeDaysType)
            put("isPostActivityReminderEnabled", profile.isPostActivityReminderEnabled)
            put("isSmartScheduledReminderEnabled", profile.isSmartScheduledReminderEnabled)
        }
        val editor = prefs.edit().putString(KEY_USER_PROFILE, obj.toString())
        if (profile.isCompleted) {
            editor.putBoolean(KEY_ONBOARDING_SEEN_ONCE, true)
        }
        editor.apply()
    }

    fun loadUserProfile(): UserFinancialProfile {
        val seenOnce = hasCompletedInitialOnboardingOnce()
        val raw = prefs.getString(KEY_USER_PROFILE, null)
            ?: return UserFinancialProfile(isCompleted = seenOnce)
        return try {
            val obj = JSONObject(raw)
            val habitsJson = obj.optJSONArray("dailyHabits")
            val habits = mutableListOf<String>()
            if (habitsJson != null) {
                for (i in 0 until habitsJson.length()) {
                    val h = habitsJson.optString(i)
                    if (h.isNotBlank()) habits.add(h)
                }
            }
            val defaults = UserFinancialProfile()
            val cCode = obj.optString("countryCode", defaults.countryCode).ifBlank { "US" }
            val currCode = obj.optString("currencyCode", WorldCountryCurrencyRegistry.byCountryCode(cCode).currencyCode)
            UserFinancialProfile(
                isCompleted = obj.optBoolean("isCompleted", false) || seenOnce,
                countryCode = cCode,
                currencyCode = currCode,
                nickname = obj.optString("nickname", defaults.nickname),
                gender = obj.optString("gender", defaults.gender),
                livingStatus = obj.optString("livingStatus", defaults.livingStatus),
                incomeSourcePrimary = obj.optString("incomeSourcePrimary", defaults.incomeSourcePrimary),
                monthlyIncomeAmount = obj.optLong("monthlyIncomeAmount", defaults.monthlyIncomeAmount).coerceIn(0L, MAX_MONETARY_AMOUNT),
                paydayDate = obj.optInt("paydayDate", defaults.paydayDate).coerceIn(1, 31),
                dependentsCount = obj.optInt("dependentsCount", defaults.dependentsCount).coerceIn(0, 50),
                transportMode = obj.optString("transportMode", defaults.transportMode),
                monthlyDebtObligation = obj.optLong("monthlyDebtObligation", defaults.monthlyDebtObligation).coerceIn(0L, MAX_MONETARY_AMOUNT),
                primaryFinancialGoal = obj.optString("primaryFinancialGoal", defaults.primaryFinancialGoal),
                dailyHabits = if (habits.isNotEmpty()) habits else defaults.dailyHabits,
                controlMode = obj.optString("controlMode", defaults.controlMode),
                workShiftType = obj.optString("workShiftType", defaults.workShiftType),
                workStartHour = obj.optInt("workStartHour", defaults.workStartHour),
                workStartMinute = obj.optInt("workStartMinute", defaults.workStartMinute),
                lunchBreakHour = obj.optInt("lunchBreakHour", defaults.lunchBreakHour),
                lunchBreakMinute = obj.optInt("lunchBreakMinute", defaults.lunchBreakMinute),
                workEndHour = obj.optInt("workEndHour", defaults.workEndHour),
                workEndMinute = obj.optInt("workEndMinute", defaults.workEndMinute),
                eveningReviewHour = obj.optInt("eveningReviewHour", defaults.eveningReviewHour),
                eveningReviewMinute = obj.optInt("eveningReviewMinute", defaults.eveningReviewMinute),
                activeDaysType = obj.optString("activeDaysType", defaults.activeDaysType),
                isPostActivityReminderEnabled = obj.optBoolean("isPostActivityReminderEnabled", defaults.isPostActivityReminderEnabled),
                isSmartScheduledReminderEnabled = obj.optBoolean("isSmartScheduledReminderEnabled", defaults.isSmartScheduledReminderEnabled)
            )
        } catch (e: Exception) {
            UserFinancialProfile(isCompleted = seenOnce)
        }
    }

    fun saveAppSettings(settings: AppSettings) {
        val subObj = JSONObject().apply {
            put("isProActive", settings.subscription.isProActive)
            put("trialStartDateMillis", settings.subscription.trialStartDateMillis)
            put("proActivatedDateMillis", settings.subscription.proActivatedDateMillis)
            put("productId", settings.subscription.productId)
            put("planTier", settings.subscription.planTier)
            put("hasDismissedExpiredTrialNotice", settings.subscription.hasDismissedExpiredTrialNotice)
            put("dailyInsightRefreshCount", settings.subscription.dailyInsightRefreshCount)
            put("lastInsightRefreshDayEpoch", settings.subscription.lastInsightRefreshDayEpoch)
        }
        val obj = JSONObject().apply {
            put("language", settings.language.name)
            put("countryCode", settings.countryCode)
            put("currencyCode", settings.currencyCode)
            put("isDarkMode", settings.isDarkMode)
            put("useSystemTheme", settings.useSystemTheme)
            put("currencyFormat", settings.currencyFormat.name)
            put("isAppLockEnabled", settings.isAppLockEnabled)
            put("hideNominalOnDashboard", settings.hideNominalOnDashboard)
            put("dailyReminderNotification", settings.dailyReminderNotification)
            put("reminderHour", settings.reminderHour)
            put("reminderMinute", settings.reminderMinute)
            put("soundHapticFeedback", settings.soundHapticFeedback)
            put("autoBackupCloud", settings.autoBackupCloud)
            put("hasSeenUserGuide", settings.hasSeenUserGuide)
            put("subscription", subObj)
            put("fontScaleFactor", settings.fontScaleFactor.toDouble())
            put("boldNumbers", settings.boldNumbers)
            put("highContrastMode", settings.highContrastMode)
            put("accentTheme", settings.accentTheme)
            put("lastCloudSyncMillis", settings.lastCloudSyncMillis)
            put("cloudAccountEmail", settings.cloudAccountEmail)
            put("dateFormatPattern", settings.dateFormatPattern)
            put("timeFormat24H", settings.timeFormat24H)
            put("hideAmountInNotifications", settings.hideAmountInNotifications)
            put("showInsightCardOnDashboard", settings.showInsightCardOnDashboard)
        }
        prefs.edit().putString(KEY_APP_SETTINGS, obj.toString()).apply()
    }

    fun loadAppSettings(): AppSettings {
        val seenOnce = hasCompletedInitialOnboardingOnce()
        val raw = prefs.getString(KEY_APP_SETTINGS, null)
            ?: return AppSettings(hasSeenUserGuide = seenOnce)
        return try {
            val obj = JSONObject(raw)
            val defaults = AppSettings()
            val langName = obj.optString("language", defaults.language.name)
            val lang = runCatching { AppLanguage.valueOf(langName) }.getOrDefault(defaults.language)
            val cCode = obj.optString("countryCode", defaults.countryCode).ifBlank { "US" }
            val currCode = obj.optString("currencyCode", WorldCountryCurrencyRegistry.byCountryCode(cCode).currencyCode)
            val fmtName = obj.optString("currencyFormat", defaults.currencyFormat.name)
            val fmt = runCatching { CurrencyFormatOption.valueOf(fmtName) }.getOrDefault(defaults.currencyFormat)
            val subObj = obj.optJSONObject("subscription")
            val sub = if (subObj != null) {
                SubscriptionConfig(
                    isProActive = subObj.optBoolean("isProActive", false),
                    trialStartDateMillis = subObj.optLong("trialStartDateMillis", 0L),
                    proActivatedDateMillis = subObj.optLong("proActivatedDateMillis", 0L),
                    productId = subObj.optString("productId", "aruskas_pro_monthly_7k"),
                    planTier = subObj.optString("planTier", "TRIAL"),
                    hasDismissedExpiredTrialNotice = subObj.optBoolean("hasDismissedExpiredTrialNotice", false),
                    dailyInsightRefreshCount = subObj.optInt("dailyInsightRefreshCount", 0),
                    lastInsightRefreshDayEpoch = subObj.optLong("lastInsightRefreshDayEpoch", 0L)
                )
            } else {
                defaults.subscription
            }
            AppSettings(
                language = lang,
                countryCode = cCode,
                currencyCode = currCode,
                isDarkMode = obj.optBoolean("isDarkMode", defaults.isDarkMode),
                useSystemTheme = obj.optBoolean("useSystemTheme", defaults.useSystemTheme),
                currencyFormat = fmt,
                isAppLockEnabled = obj.optBoolean("isAppLockEnabled", defaults.isAppLockEnabled),
                hideNominalOnDashboard = obj.optBoolean("hideNominalOnDashboard", defaults.hideNominalOnDashboard),
                dailyReminderNotification = obj.optBoolean("dailyReminderNotification", defaults.dailyReminderNotification),
                reminderHour = obj.optInt("reminderHour", defaults.reminderHour),
                reminderMinute = obj.optInt("reminderMinute", defaults.reminderMinute),
                soundHapticFeedback = obj.optBoolean("soundHapticFeedback", defaults.soundHapticFeedback),
                autoBackupCloud = obj.optBoolean("autoBackupCloud", defaults.autoBackupCloud),
                hasSeenUserGuide = obj.optBoolean("hasSeenUserGuide", false) || seenOnce,
                subscription = sub,
                fontScaleFactor = obj.optDouble("fontScaleFactor", defaults.fontScaleFactor.toDouble()).toFloat(),
                boldNumbers = obj.optBoolean("boldNumbers", defaults.boldNumbers),
                highContrastMode = obj.optBoolean("highContrastMode", defaults.highContrastMode),
                accentTheme = obj.optString("accentTheme", defaults.accentTheme),
                lastCloudSyncMillis = obj.optLong("lastCloudSyncMillis", defaults.lastCloudSyncMillis),
                cloudAccountEmail = obj.optString("cloudAccountEmail", defaults.cloudAccountEmail),
                dateFormatPattern = obj.optString("dateFormatPattern", defaults.dateFormatPattern).ifBlank { "dd MMM yyyy" },
                timeFormat24H = obj.optBoolean("timeFormat24H", defaults.timeFormat24H),
                hideAmountInNotifications = obj.optBoolean("hideAmountInNotifications", defaults.hideAmountInNotifications),
                showInsightCardOnDashboard = obj.optBoolean("showInsightCardOnDashboard", defaults.showInsightCardOnDashboard)
            )
        } catch (e: Exception) {
            AppSettings(hasSeenUserGuide = seenOnce)
        }
    }

    fun saveWallets(wallets: List<Wallet>) {
        val arr = JSONArray()
        wallets.forEach { w ->
            val obj = JSONObject().apply {
                put("id", w.id)
                put("userId", w.userId)
                put("name", w.name)
                put("walletType", w.walletType)
                put("balance", w.balance)
                put("colorHex", w.colorHex)
                put("iconKey", w.iconKey)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_WALLETS, arr.toString()).apply()
    }

    fun loadWallets(): List<Wallet> {
        val defaultWallets = WorldCountryCurrencyRegistry.getActiveProfile().starterWallets
        val raw = prefs.getString(KEY_WALLETS, null) ?: return defaultWallets
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<Wallet>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "wallet_$i").trim().ifBlank { "wallet_$i" }
                val name = obj.optString("name", "Wallet").trim().ifBlank { "Wallet" }
                list.add(
                    Wallet(
                        id = id,
                        userId = obj.optString("userId", "local_preview"),
                        name = name,
                        walletType = obj.optString("walletType", "BANK").ifBlank { "BANK" },
                        balance = obj.optLong("balance", 0L).coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT),
                        colorHex = obj.optString("colorHex", "#0066AE").ifBlank { "#0066AE" },
                        iconKey = obj.optString("iconKey", "bank_bca").ifBlank { "bank_bca" }
                    )
                )
            }
            if (list.isNotEmpty()) list else defaultWallets
        } catch (e: Exception) {
            defaultWallets
        }
    }

    fun saveTransactions(transactions: List<TransactionRecord>) {
        val arr = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("userId", tx.userId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("categoryKey", tx.categoryKey)
                put("categoryLabel", tx.categoryLabel)
                put("frequencyTag", tx.frequencyTag)
                put("walletId", tx.walletId)
                put("walletName", tx.walletName)
                put("note", tx.note)
                put("transactionDateMillis", tx.transactionDateMillis)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, arr.toString()).apply()
    }

    fun loadTransactions(): List<TransactionRecord> {
        val raw = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<TransactionRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val amt = obj.optLong("amount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT)
                if (amt <= 0L) continue
                list.add(
                    TransactionRecord(
                        id = obj.optString("id", "tx_$i").trim().ifBlank { "tx_$i" },
                        userId = obj.optString("userId", "local_preview"),
                        type = obj.optString("type", "EXPENSE").ifBlank { "EXPENSE" },
                        amount = amt,
                        categoryKey = obj.optString("categoryKey", "kebutuhan_pokok").ifBlank { "kebutuhan_pokok" },
                        categoryLabel = obj.optString("categoryLabel", "Expense").ifBlank { "Expense" },
                        frequencyTag = obj.optString("frequencyTag", "HARIAN").ifBlank { "HARIAN" },
                        walletId = obj.optString("walletId", ""),
                        walletName = obj.optString("walletName", ""),
                        note = obj.optString("note", ""),
                        transactionDateMillis = obj.optLong("transactionDateMillis", System.currentTimeMillis()).coerceAtLeast(1L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveGoals(goals: List<SavingsGoal>) {
        val arr = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("userId", g.userId)
                put("title", g.title)
                put("targetAmount", g.targetAmount)
                put("savedAmount", g.savedAmount)
                put("dailyRecommendation", g.dailyRecommendation)
                put("note", g.note)
                put("iconKey", g.iconKey)
                put("startDateMillis", g.startDateMillis)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_GOALS, arr.toString()).apply()
    }

    fun loadGoals(defaultGoals: List<SavingsGoal>): List<SavingsGoal> {
        val raw = prefs.getString(KEY_GOALS, null) ?: return defaultGoals
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SavingsGoal>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    SavingsGoal(
                        id = obj.optString("id", "goal_$i").trim().ifBlank { "goal_$i" },
                        userId = obj.optString("userId", "local_preview"),
                        title = obj.optString("title", "Target Impian").trim().ifBlank { "Target Impian" },
                        targetAmount = obj.optLong("targetAmount", 1_000L).coerceIn(1L, MAX_MONETARY_AMOUNT),
                        savedAmount = obj.optLong("savedAmount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                        dailyRecommendation = obj.optLong("dailyRecommendation", 10L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                        note = obj.optString("note", ""),
                        iconKey = obj.optString("iconKey", "laptop"),
                        startDateMillis = obj.optLong("startDateMillis", System.currentTimeMillis()).coerceAtLeast(1L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaultGoals
        }
    }

    fun saveMandatoryBills(bills: List<MandatoryBillItem>) {
        val arr = JSONArray()
        bills.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("totalAmount", b.totalAmount)
                put("paidAmount", b.paidAmount)
                put("dueScheduleLabel", b.dueScheduleLabel)
                put("dueDayOfMonth", b.dueDayOfMonth)
                if (b.renewalDayOfMonth != null) {
                    put("renewalDayOfMonth", b.renewalDayOfMonth)
                }
                put("status", b.status)
                put("billType", b.billType)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_MANDATORY_BILLS, arr.toString()).apply()
    }

    fun loadMandatoryBills(): List<MandatoryBillItem> {
        val defaults = WorldCountryCurrencyRegistry.defaultMandatoryBillsForCountry(
            WorldCountryCurrencyRegistry.activeCountryCode,
            WorldCountryCurrencyRegistry.activeLanguage
        )
        val raw = prefs.getString(KEY_MANDATORY_BILLS, null) ?: return defaults
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MandatoryBillItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val hasRenewal = obj.has("renewalDayOfMonth") && !obj.isNull("renewalDayOfMonth")
                val safeTotal = obj.optLong("totalAmount", 100L).coerceIn(1L, MAX_MONETARY_AMOUNT)
                list.add(
                    MandatoryBillItem(
                        id = obj.optString("id", "bill_$i").trim().ifBlank { "bill_$i" },
                        name = obj.optString("name", "Bill").trim().ifBlank { "Bill" },
                        totalAmount = safeTotal,
                        paidAmount = obj.optLong("paidAmount", 0L).coerceIn(0L, safeTotal),
                        dueScheduleLabel = obj.optString("dueScheduleLabel", "Due every 1st"),
                        dueDayOfMonth = obj.optInt("dueDayOfMonth", 1).coerceIn(1, 31),
                        renewalDayOfMonth = if (hasRenewal) obj.optInt("renewalDayOfMonth").coerceIn(1, 31) else null,
                        status = obj.optString("status", "UNPAID"),
                        billType = obj.optString("billType", "KOSAN")
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaults
        }
    }

    fun saveCategoryLimits(limits: Map<String, CategoryLimitConfig>) {
        val arr = JSONArray()
        limits.forEach { (_, cfg) ->
            val obj = JSONObject().apply {
                put("categoryKey", cfg.categoryKey)
                put("limitAmount", cfg.limitAmount)
                put("period", cfg.period)
                put("customLabel", cfg.customLabel)
                put("customSubtitle", cfg.customSubtitle)
                put("startDateMillis", cfg.startDateMillis)
                put("endDateMillis", cfg.endDateMillis)
                put("isCustomCategory", cfg.isCustomCategory)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CATEGORY_LIMITS, arr.toString()).apply()
    }

    fun loadCategoryLimits(): Map<String, CategoryLimitConfig> {
        val defaults = WorldCountryCurrencyRegistry.defaultCategoryLimitsForCountry(
            WorldCountryCurrencyRegistry.activeCountryCode,
            WorldCountryCurrencyRegistry.activeLanguage
        )
        val raw = prefs.getString(KEY_CATEGORY_LIMITS, null) ?: return defaults
        return try {
            val arr = JSONArray(raw)
            val map = defaults.toMutableMap()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val key = obj.optString("categoryKey", "").trim()
                if (key.isNotBlank()) {
                    map[key] = CategoryLimitConfig(
                        categoryKey = key,
                        limitAmount = obj.optLong("limitAmount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                        period = obj.optString("period", "BULANAN"),
                        customLabel = obj.optString("customLabel", ""),
                        customSubtitle = obj.optString("customSubtitle", ""),
                        startDateMillis = obj.optLong("startDateMillis", 0L).coerceAtLeast(0L),
                        endDateMillis = obj.optLong("endDateMillis", 0L).coerceAtLeast(0L),
                        isCustomCategory = obj.optBoolean("isCustomCategory", false)
                    )
                }
            }
            map
        } catch (e: Exception) {
            defaults
        }
    }

    fun saveEmergencyFund(config: EmergencyFundConfig) {
        val obj = JSONObject().apply {
            put("emergencyBalance", config.emergencyBalance)
            put("emergencyLimit", config.emergencyLimit)
            put("limitPeriod", config.limitPeriod)
            put("storagePlaceNote", config.storagePlaceNote)
        }
        prefs.edit().putString(KEY_EMERGENCY_FUND, obj.toString()).apply()
    }

    fun loadEmergencyFund(): EmergencyFundConfig {
        val p = WorldCountryCurrencyRegistry.getActiveProfile()
        val isId = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.INDONESIAN
        val defaultCfg = EmergencyFundConfig(
            emergencyBalance = 0L,
            emergencyLimit = 0L,
            limitPeriod = "BULANAN",
            storagePlaceNote = if (isId) {
                "Rekening / Kantong Terpisah (Jangan dicampur uang harian)"
            } else {
                "Separate Vault / Account (Isolated from daily funds)"
            }
        )
        val raw = prefs.getString(KEY_EMERGENCY_FUND, null) ?: return defaultCfg
        return try {
            val obj = JSONObject(raw)
            EmergencyFundConfig(
                emergencyBalance = obj.optLong("emergencyBalance", defaultCfg.emergencyBalance).coerceIn(0L, MAX_MONETARY_AMOUNT),
                emergencyLimit = obj.optLong("emergencyLimit", defaultCfg.emergencyLimit).coerceIn(0L, MAX_MONETARY_AMOUNT),
                limitPeriod = obj.optString("limitPeriod", defaultCfg.limitPeriod),
                storagePlaceNote = obj.optString("storagePlaceNote", defaultCfg.storagePlaceNote)
            )
        } catch (e: Exception) {
            defaultCfg
        }
    }

    fun notifyWidgetDataChanged() {
        com.example.widget.ArusKasQuickEntryWidget.notifyDataChangedAsync(appContext)
    }

    companion object {
        private const val MAX_MONETARY_AMOUNT = 1_000_000_000_000L
        private const val PREFS_NAME = "aruskas_local_storage_v1"
        private const val KEY_ONBOARDING_SEEN_ONCE = "has_seen_onboarding_once"
        private const val KEY_USER_PROFILE = "user_financial_profile"
        private const val KEY_APP_SETTINGS = "app_settings"
        private const val KEY_WALLETS = "local_wallets"
        private const val KEY_TRANSACTIONS = "local_transactions"
        private const val KEY_GOALS = "local_goals"
        private const val KEY_MANDATORY_BILLS = "mandatory_bills"
        private const val KEY_CATEGORY_LIMITS = "category_limits"
        private const val KEY_EMERGENCY_FUND = "emergency_fund"
    }
}
