package com.example.data

/**
 * Supported UI languages in ArusKas: Indonesian and English only.
 */
enum class AppLanguage(val code: String, val displayName: String, val regionLabel: String) {
    ENGLISH("en", "English (International)", "Default • Global International"),
    INDONESIAN("id", "Bahasa Indonesia", "Optional • Bahasa Nasional")
}

/**
 * Currency display format preference
 */
enum class CurrencyFormatOption(val code: String, val label: String) {
    FULL_RUPIAH("full", "Full Amount ($150,000 / Rp 150.000)"),
    COMPACT("compact", "Compact ($150K / Rp 1,5 Jt)")
}

/**
 * Supported PRO Plan Tiers in Hybrid Freemium + 3-Tier Monetization Model (Model D)
 */
enum class ProPlanTier(
    val planCode: String,
    val productId: String,
    val isRecurring: Boolean
) {
    MONTHLY("MONTHLY", "aruskas_pro_monthly", true),
    ANNUAL("ANNUAL", "aruskas_pro_annual", true),
    LIFETIME("LIFETIME", "aruskas_pro_lifetime", false);

    companion object {
        fun fromCodeOrProductId(planTier: String, productId: String): ProPlanTier {
            return when {
                planTier.equals("LIFETIME", ignoreCase = true) || productId.contains("lifetime", ignoreCase = true) -> LIFETIME
                planTier.equals("ANNUAL", ignoreCase = true) || productId.contains("annual", ignoreCase = true) || productId.contains("yearly", ignoreCase = true) -> ANNUAL
                else -> MONTHLY
            }
        }
    }
}

/**
 * Contextual Soft Paywall Triggers explaining why a user encountered a PRO gate
 * while reassuring them that their core financial ledger remains 100% Free Forever.
 */
enum class ProFeatureTrigger(
    private val titleId: String,
    private val titleEn: String,
    private val subtitleId: String,
    private val subtitleEn: String
) {
    GENERAL_UPGRADE(
        titleId = "ArusKas PRO • Kendali Keuangan Tanpa Batas",
        titleEn = "ArusKas PRO • Unlimited Financial Control",
        subtitleId = "Buka seluruh 12 karakter asisten offline, laporan PDF resmi, dompet & target tanpa batas.",
        subtitleEn = "Unlock all 12 offline assistant personas, executive PDF reports, and unlimited wallets & goals."
    ),
    TRIAL_ENDED_NOTICE(
        titleId = "Masa Uji Coba 30 Hari PRO Telah Selesai",
        titleEn = "Your 30-Day PRO Free Trial Has Concluded",
        subtitleId = "Tenang! Seluruh data saldo, riwayat transaksi, dompet, dan cadangan JSON Anda 100% AMAN & tetap gratis dipakai selamanya. Upgrade ke PRO jika ingin membuka fitur lanjutan.",
        subtitleEn = "Rest assured! All your balances, transaction history, wallets, and JSON backups are 100% SAFE & remain free forever. Upgrade to PRO anytime for advanced power tools."
    ),
    UNLIMITED_WALLETS(
        titleId = "Fitur PRO: Rekening & Dompet Tanpa Batas (6+ Akun)",
        titleEn = "PRO Feature: Unlimited Wallets & Bank Accounts (6+)",
        subtitleId = "Versi Gratis mendukung hingga 5 rekening/dompet aktif. Upgrade ke PRO untuk menambah dompet ke-6 dan seterusnya tanpa batas.",
        subtitleEn = "The Free Forever tier includes up to 5 active accounts. Upgrade to PRO to add a 6th wallet and unlimited accounts."
    ),
    UNLIMITED_CUSTOM_CATEGORIES(
        titleId = "Fitur PRO: Kategori Pengeluaran Kustom Tanpa Batas",
        titleEn = "PRO Feature: Unlimited Custom Spending Categories",
        subtitleId = "Versi Gratis mencakup 6 kategori utama + Dana Darurat + 1 kategori kustom. Upgrade ke PRO untuk membuat kategori kustom tanpa batas.",
        subtitleEn = "Free Forever includes 6 core categories + Emergency Fund + 1 custom category. Upgrade to PRO for unlimited custom categories."
    ),
    CUSTOM_DATE_RANGE_LIMIT(
        titleId = "Fitur PRO: Siklus Kalender Gajian Kustom",
        titleEn = "PRO Feature: Custom Payday Calendar Date Ranges",
        subtitleId = "Periode Bulanan, Mingguan, Harian & Fleksibel gratis selamanya. Upgrade ke PRO untuk mengatur rentang tanggal kalender kustom (misal: tgl 25 ke 24).",
        subtitleEn = "Monthly, Weekly, Daily & Flexible limits are free forever. Upgrade to PRO to set custom payday-to-payday calendar date ranges."
    ),
    UNLIMITED_GOALS(
        titleId = "Fitur PRO: Celengan Target Impian Tanpa Batas (3+ Target)",
        titleEn = "PRO Feature: Unlimited Dream Savings Goals (3+ Goals)",
        subtitleId = "Versi Gratis mendukung hingga 2 target tabungan aktif. Upgrade ke PRO untuk menabung banyak impian sekaligus tanpa batas.",
        subtitleEn = "Free Forever supports up to 2 simultaneous savings goals. Upgrade to PRO for unlimited parallel savings goals."
    ),
    UNLIMITED_BILLS(
        titleId = "Fitur PRO: Ceklist Tagihan & Cicilan Tanpa Batas (5+ Tagihan)",
        titleEn = "PRO Feature: Unlimited Mandatory Bills & Subscriptions (5+)",
        subtitleId = "Versi Gratis mendukung hingga 4 tagihan wajib bulanan. Upgrade ke PRO untuk memantau seluruh cicilan dan langganan tanpa batas.",
        subtitleEn = "Free Forever supports up to 4 mandatory monthly bills. Upgrade to PRO to track unlimited recurring bills and subscriptions."
    ),
    PDF_EXPORT(
        titleId = "Fitur PRO: Ekspor Dokumen Laporan Tabel PDF Resmi",
        titleEn = "PRO Feature: Executive Styled A4 PDF Ledger Report",
        subtitleId = "Ekspor Excel (.CSV) dan Backup (.JSON) 100% gratis selamanya. Upgrade ke PRO untuk mencetak dokumen laporan PDF A4 berdesain tabel modern.",
        subtitleEn = "Excel (.CSV) export and full (.JSON) backup are 100% free forever. Upgrade to PRO to generate executive A4 styled PDF ledger statements."
    ),
    SMART_MERGE_RESTORE(
        titleId = "Fitur PRO: Smart Merge Restore (Gabung Tanpa Duplikat)",
        titleEn = "PRO Feature: Smart Duplicate-Safe Merge Restore",
        subtitleId = "Pulihkan Data Penuh (Replace) gratis selamanya. Upgrade ke PRO untuk menggabungkan file backup dari beberapa perangkat tanpa duplikat transaksi.",
        subtitleEn = "Full Replace Restore is free forever. Upgrade to PRO to merge backups across devices without duplicate transactions."
    ),
    AUTO_CLOUD_SYNC(
        titleId = "Fitur PRO: Sinkronisasi Cloud Multi-Bulan Otomatis",
        titleEn = "PRO Feature: Automatic Multi-Month Cloud Sync",
        subtitleId = "Backup manual (.JSON) ke penyimpanan/Google Drive gratis selamanya. Upgrade ke PRO untuk sinkronisasi otomatis ke Cloud Firestore.",
        subtitleEn = "Manual (.JSON) backup to device/Drive is free forever. Upgrade to PRO for automatic background Cloud Firestore sync."
    ),
    DEEP_OFFLINE_ANALYSIS(
        titleId = "Fitur PRO: Diagnostik Mendalam 5-Bagian & Pola 30 Hari",
        titleEn = "PRO Feature: 5-Part Deep Diagnostic & 30-Day Habit Analysis",
        subtitleId = "Kalimat analisis utama di beranda gratis selamanya. Upgrade ke PRO untuk membuka rincian fakta angka, pola 30 hari, dan rekomendasi strategis lengkap.",
        subtitleEn = "Dashboard daily insights are free forever. Upgrade to PRO to unlock full factual breakdowns, 30-day habit diagnostics, and action plans."
    ),
    UNLIMITED_INSIGHT_REFRESH(
        titleId = "Fitur PRO: Acak Kalimat Insight Tanpa Batas Harian",
        titleEn = "PRO Feature: Unlimited Daily Insight Refreshes",
        subtitleId = "Anda telah menggunakan 3x kuota ganti kalimat gratis hari ini. Upgrade ke PRO untuk menjelajahi 10.000 kalimat lokal sepuasnya tanpa batas harian!",
        subtitleEn = "You've used your 3 free daily insight refreshes today. Upgrade to PRO for unlimited access to all 10,000 local phrases!"
    ),
    ALL_12_PERSONAS(
        titleId = "Fitur PRO: Buka Seluruh 12 Karakter Asisten Offline",
        titleEn = "PRO Feature: Unlock All 12 Offline Assistant Personas",
        subtitleId = "Karakter Si Bestie, Si Analis & Si Motivator gratis selamanya. Upgrade ke PRO untuk membuka Si Roaster 🔥, Si Sarkas 😏, Si Detektif 🕵️, Si Mentor 🎓, dan 9 karakter lainnya!",
        subtitleEn = "Bestie, Analyst & Motivator personas are free forever. Upgrade to PRO to unlock The Roaster 🔥, The Sarcastic 😏, The Detective 🕵️, Wise Mentor 🎓, and all 12 personas!"
    ),
    PREMIUM_THEMES(
        titleId = "Fitur PRO: Tema Warna Eksklusif (Emerald, Cobalt & Amethyst)",
        titleEn = "PRO Feature: Exclusive Accent Themes (Emerald, Cobalt & Amethyst)",
        subtitleId = "Tema Royal Blue, Mode Gelap & Ukuran Huruf gratis selamanya. Upgrade ke PRO untuk personalisasi warna Emerald, Navy Cobalt, dan Amethyst Violet.",
        subtitleEn = "Royal Blue theme, Dark Mode & Font Scaling are free forever. Upgrade to PRO to customize with Emerald, Navy Cobalt, and Amethyst Violet."
    ),
    PRIVACY_STEALTH(
        titleId = "Fitur PRO: Mode Siluman Sensor Nominal Notifikasi",
        titleEn = "PRO Feature: Notification Amount Stealth Masking",
        subtitleId = "Kunci PIN SHA-256 dan sensor saldo beranda gratis selamanya. Upgrade ke PRO untuk menyensor angka nominal uang pada banner notifikasi.",
        subtitleEn = "Salted SHA-256 PIN lock and dashboard balance masking are free forever. Upgrade to PRO to mask currency amounts inside notification banners."
    );

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.INDONESIAN) titleId else titleEn
    fun subtitle(lang: AppLanguage): String = if (lang == AppLanguage.INDONESIAN) subtitleId else subtitleEn
}

/**
 * Localized PPP (Purchasing Power Parity) Pricing Snapshot for a specific country/currency.
 */
data class LocalizedPlanPricing(
    val countryCode: String,
    val currencyCode: String,
    val monthlyFormatted: String,
    val monthlyPerDayHintId: String,
    val monthlyPerDayHintEn: String,
    val annualFormatted: String,
    val annualMonthlyEquivalentFormatted: String,
    val annualSavingsPercent: Int,
    val lifetimeFormatted: String
)

/**
 * Global Purchasing Power Parity (PPP) Pricing Catalog & Ethical Feature Gate Evaluator.
 */
object MonetizationPricingCatalog {

    fun getPricingForCountry(countryCode: String): LocalizedPlanPricing {
        return when (countryCode.uppercase()) {
            "ID" -> LocalizedPlanPricing(
                countryCode = "ID",
                currencyCode = "IDR",
                monthlyFormatted = "Rp 15.000",
                monthlyPerDayHintId = "Hanya ~Rp 500/hari • Lebih hemat dari parkir motor",
                monthlyPerDayHintEn = "Only ~IDR 500/day • Less than parking change",
                annualFormatted = "Rp 99.000",
                annualMonthlyEquivalentFormatted = "Rp 8.250",
                annualSavingsPercent = 45,
                lifetimeFormatted = "Rp 199.000"
            )
            "MY" -> LocalizedPlanPricing(
                countryCode = "MY",
                currencyCode = "MYR",
                monthlyFormatted = "RM 6.90",
                monthlyPerDayHintId = "Hanya ~RM 0.23/hari",
                monthlyPerDayHintEn = "Only ~RM 0.23/day",
                annualFormatted = "RM 39.90",
                annualMonthlyEquivalentFormatted = "RM 3.32",
                annualSavingsPercent = 52,
                lifetimeFormatted = "RM 89.00"
            )
            "SG" -> LocalizedPlanPricing(
                countryCode = "SG",
                currencyCode = "SGD",
                monthlyFormatted = "S$ 2.98",
                monthlyPerDayHintId = "Hanya ~S$ 0.10/hari",
                monthlyPerDayHintEn = "Only ~S$ 0.10/day",
                annualFormatted = "S$ 17.98",
                annualMonthlyEquivalentFormatted = "S$ 1.50",
                annualSavingsPercent = 50,
                lifetimeFormatted = "S$ 34.98"
            )
            "IN" -> LocalizedPlanPricing(
                countryCode = "IN",
                currencyCode = "INR",
                monthlyFormatted = "₹99",
                monthlyPerDayHintId = "Hanya ~₹3.3/hari",
                monthlyPerDayHintEn = "Only ~₹3.3/day",
                annualFormatted = "₹599",
                annualMonthlyEquivalentFormatted = "₹49.9",
                annualSavingsPercent = 50,
                lifetimeFormatted = "₹1,299"
            )
            "PH" -> LocalizedPlanPricing(
                countryCode = "PH",
                currencyCode = "PHP",
                monthlyFormatted = "₱79",
                monthlyPerDayHintId = "Hanya ~₱2.6/hari",
                monthlyPerDayHintEn = "Only ~₱2.6/day",
                annualFormatted = "₱499",
                annualMonthlyEquivalentFormatted = "₱41.5",
                annualSavingsPercent = 47,
                lifetimeFormatted = "₱999"
            )
            "TH" -> LocalizedPlanPricing(
                countryCode = "TH",
                currencyCode = "THB",
                monthlyFormatted = "฿49",
                monthlyPerDayHintId = "Hanya ~฿1.6/hari",
                monthlyPerDayHintEn = "Only ~฿1.6/day",
                annualFormatted = "฿299",
                annualMonthlyEquivalentFormatted = "฿24.9",
                annualSavingsPercent = 49,
                lifetimeFormatted = "฿599"
            )
            "VN" -> LocalizedPlanPricing(
                countryCode = "VN",
                currencyCode = "VND",
                monthlyFormatted = "29.000 ₫",
                monthlyPerDayHintId = "Hanya ~960 ₫/hari",
                monthlyPerDayHintEn = "Only ~960 ₫/day",
                annualFormatted = "179.000 ₫",
                annualMonthlyEquivalentFormatted = "14.900 ₫",
                annualSavingsPercent = 48,
                lifetimeFormatted = "349.000 ₫"
            )
            "JP" -> LocalizedPlanPricing(
                countryCode = "JP",
                currencyCode = "JPY",
                monthlyFormatted = "¥350",
                monthlyPerDayHintId = "Hanya ~¥11/hari",
                monthlyPerDayHintEn = "Only ~¥11/day",
                annualFormatted = "¥2,100",
                annualMonthlyEquivalentFormatted = "¥175",
                annualSavingsPercent = 50,
                lifetimeFormatted = "¥4,200"
            )
            "GB" -> LocalizedPlanPricing(
                countryCode = "GB",
                currencyCode = "GBP",
                monthlyFormatted = "£1.99",
                monthlyPerDayHintId = "Hanya ~£0.06/hari",
                monthlyPerDayHintEn = "Only ~£0.06/day",
                annualFormatted = "£11.99",
                annualMonthlyEquivalentFormatted = "£1.00",
                annualSavingsPercent = 50,
                lifetimeFormatted = "£24.99"
            )
            "DE", "FR", "NL", "ES", "IT" -> LocalizedPlanPricing(
                countryCode = countryCode.uppercase(),
                currencyCode = "EUR",
                monthlyFormatted = "€2.29",
                monthlyPerDayHintId = "Hanya ~€0.07/hari",
                monthlyPerDayHintEn = "Only ~€0.07/day",
                annualFormatted = "€13.99",
                annualMonthlyEquivalentFormatted = "€1.16",
                annualSavingsPercent = 49,
                lifetimeFormatted = "€27.99"
            )
            "AU" -> LocalizedPlanPricing(
                countryCode = "AU",
                currencyCode = "AUD",
                monthlyFormatted = "A$ 3.49",
                monthlyPerDayHintId = "Hanya ~A$ 0.11/hari",
                monthlyPerDayHintEn = "Only ~A$ 0.11/day",
                annualFormatted = "A$ 19.99",
                annualMonthlyEquivalentFormatted = "A$ 1.66",
                annualSavingsPercent = 52,
                lifetimeFormatted = "A$ 39.99"
            )
            else -> LocalizedPlanPricing(
                countryCode = "US",
                currencyCode = "USD",
                monthlyFormatted = "$2.49",
                monthlyPerDayHintId = "Hanya ~$0.08/hari • Lebih murah dari secangkir kopi",
                monthlyPerDayHintEn = "Only ~$0.08/day • Less than a cup of coffee",
                annualFormatted = "$14.99",
                annualMonthlyEquivalentFormatted = "$1.25",
                annualSavingsPercent = 50,
                lifetimeFormatted = "$29.99"
            )
        }
    }
}

/**
 * Pure, Deterministic Feature Gate Evaluator implementing the Ethical Freemium + Hybrid PRO Matrix.
 * Guarantees 100% Grandfathering: Existing user data is NEVER hidden, deleted, or locked on downgrade.
 */
object ArusKasFeatureGateEvaluator {
    const val FREE_MAX_WALLETS = 5
    const val FREE_MAX_CUSTOM_CATEGORIES = 1
    const val FREE_MAX_SAVINGS_GOALS = 2
    const val FREE_MAX_MANDATORY_BILLS = 4
    const val FREE_MAX_DAILY_INSIGHT_REFRESHES = 3

    val FREE_UNLOCKED_PERSONA_NAMES: Set<String> = setOf(
        "SI_BESTIE",
        "SI_ANALIS",
        "SI_MOTIVATOR"
    )

    val FREE_UNLOCKED_ACCENT_THEMES: Set<String> = setOf(
        "ROYAL_BLUE"
    )

    fun canAddNewWallet(currentWalletCount: Int, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || currentWalletCount < FREE_MAX_WALLETS
    }

    fun canAddNewCustomCategory(currentCustomCategoryCount: Int, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || currentCustomCategoryCount < FREE_MAX_CUSTOM_CATEGORIES
    }

    fun canUseCustomDateRangePeriod(sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess
    }

    fun canAddNewSavingsGoal(currentGoalCount: Int, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || currentGoalCount < FREE_MAX_SAVINGS_GOALS
    }

    fun canAddNewMandatoryBill(currentBillCount: Int, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || currentBillCount < FREE_MAX_MANDATORY_BILLS
    }

    fun canExportStyledPdf(sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess
    }

    fun canUseSmartMergeRestore(sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess
    }

    fun canOpenDeepInsightAnalysis(sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess
    }

    fun canRefreshInsightToday(
        sub: SubscriptionConfig,
        todayEpochDay: Long = System.currentTimeMillis() / 86_400_000L
    ): Boolean {
        if (sub.hasProAccess) return true
        val usedToday = if (sub.lastInsightRefreshDayEpoch == todayEpochDay) {
            sub.dailyInsightRefreshCount
        } else {
            0
        }
        return usedToday < FREE_MAX_DAILY_INSIGHT_REFRESHES
    }

    fun isPersonaUnlocked(personaName: String, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || personaName in FREE_UNLOCKED_PERSONA_NAMES
    }

    fun isAccentThemeUnlocked(themeKey: String, sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess || themeKey in FREE_UNLOCKED_ACCENT_THEMES
    }

    fun canUseNotificationStealthMasking(sub: SubscriptionConfig): Boolean {
        return sub.hasProAccess
    }
}

/**
 * Subscription & Free Trial Configuration
 */
data class SubscriptionConfig(
    val isProActive: Boolean = false,
    val trialStartDateMillis: Long = 0L,
    val proActivatedDateMillis: Long = 0L,
    val productId: String = "aruskas_pro_monthly_7k",
    val planTier: String = "TRIAL",
    val hasDismissedExpiredTrialNotice: Boolean = false,
    val dailyInsightRefreshCount: Int = 0,
    val lastInsightRefreshDayEpoch: Long = 0L
) {
    val effectiveTrialStartMillis: Long
        get() = if (trialStartDateMillis <= 0L) System.currentTimeMillis() else trialStartDateMillis

    val isTrialActive: Boolean
        get() {
            if (isProActive) return false
            val start = effectiveTrialStartMillis
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(0L)
            val thirtyDays = 30L * 24 * 60 * 60 * 1000L
            return elapsed < thirtyDays
        }

    val hasProAccess: Boolean
        get() = isProActive || isTrialActive

    val isLifetimePro: Boolean
        get() = isProActive && (planTier.equals("LIFETIME", ignoreCase = true) || productId.contains("lifetime", ignoreCase = true))

    val activePlanTierEnum: ProPlanTier
        get() = ProPlanTier.fromCodeOrProductId(planTier, productId)

    val remainingTrialDays: Int
        get() {
            if (isProActive) return 0
            val start = effectiveTrialStartMillis
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(0L)
            val thirtyDays = 30L * 24 * 60 * 60 * 1000L
            val remaining = (thirtyDays - elapsed).coerceAtLeast(0L)
            return ((remaining + (24 * 60 * 60 * 1000L - 1)) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(0, 30)
        }

    val statusTier: String
        get() = when {
            isLifetimePro -> "PRO_LIFETIME"
            isProActive -> "PRO_ACTIVE"
            isTrialActive -> "FREE_TRIAL"
            else -> "FREE_FOREVER"
        }

    fun remainingFreeInsightRefreshesToday(todayEpochDay: Long = System.currentTimeMillis() / 86_400_000L): Int {
        if (hasProAccess) return Int.MAX_VALUE
        val used = if (lastInsightRefreshDayEpoch == todayEpochDay) dailyInsightRefreshCount else 0
        return (ArusKasFeatureGateEvaluator.FREE_MAX_DAILY_INSIGHT_REFRESHES - used).coerceAtLeast(0)
    }

    fun recordManualInsightRefresh(todayEpochDay: Long = System.currentTimeMillis() / 86_400_000L): SubscriptionConfig {
        val currentUsed = if (lastInsightRefreshDayEpoch == todayEpochDay) dailyInsightRefreshCount else 0
        return copy(
            dailyInsightRefreshCount = currentUsed + 1,
            lastInsightRefreshDayEpoch = todayEpochDay
        )
    }
}

/**
 * Comprehensive Application Settings (Default: International English & USD)
 */
data class AppSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val countryCode: String = "US",
    val currencyCode: String = "USD",
    val isDarkMode: Boolean = false,
    val useSystemTheme: Boolean = false,
    val currencyFormat: CurrencyFormatOption = CurrencyFormatOption.FULL_RUPIAH,
    val isAppLockEnabled: Boolean = false,
    val hideNominalOnDashboard: Boolean = false,
    val dailyReminderNotification: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val soundHapticFeedback: Boolean = true,
    val autoBackupCloud: Boolean = true,
    val hasSeenUserGuide: Boolean = false,
    val subscription: SubscriptionConfig = SubscriptionConfig(),
    // Accessibility & Appearance Scaling
    val fontScaleFactor: Float = 1.0f, // 1.0f (Normal), 1.15f (Large), 1.3f (Extra Large)
    val boldNumbers: Boolean = false,
    val highContrastMode: Boolean = false,
    val accentTheme: String = "ROYAL_BLUE", // ROYAL_BLUE, EMERALD, COBALT, VIOLET
    // Permanent Cloud Storage & Multi-Month Sync (like WhatsApp)
    val lastCloudSyncMillis: Long = 0L,
    val cloudAccountEmail: String = "",
    // Date/Time Formatting, Notification Privacy & Dashboard Preferences
    val dateFormatPattern: String = "dd MMM yyyy",
    val timeFormat24H: Boolean = true,
    val hideAmountInNotifications: Boolean = false,
    val showInsightCardOnDashboard: Boolean = true
)

/**
 * Full Dictionary providing 100% translation across Indonesian and English.
 */
object AppLocalization {

    fun get(key: String, lang: AppLanguage): String {
        val dict = when (lang) {
            AppLanguage.INDONESIAN -> ID_STRINGS
            AppLanguage.ENGLISH -> EN_STRINGS
        }
        return dict[key] ?: ID_STRINGS[key] ?: key
    }

    private val ID_STRINGS = mapOf(
        // Header & Balance
        "app_title" to "ArusKas",
        "free_to_use_money" to "UANG BEBAS PAKAI (AMAN BUAT HARIAN)",
        "total_wallet_money" to "Total Seluruh Uangmu: %s (Termasuk Tabungan & Darurat)",
        "see_all_wallet_details" to "Lihat rincian semua uang di kantong",
        "btn_signin" to "Masuk",
        "btn_signout" to "Keluar",
        "btn_graphic" to "Grafik",
        "btn_settings" to "Setelan",
        "btn_close" to "Tutup",
        "btn_save" to "Simpan",
        "btn_cancel" to "Batal",
        "btn_delete" to "Hapus",

        // Emergency strip
        "emergency_limit_label" to "Limit Darurat: %s/%s • Simpanan: %s",
        "btn_add_emergency" to "+ Darurat",
        "status_safe" to "AMAN",
        "status_warning" to "AWAS",
        "status_over" to "OVER!",
        "status_free" to "BEBAS",

        // Metrics & Tabungan Target
        "label_today" to "Hari Ini",
        "label_this_month" to "Bulan Ini",
        "target_savings" to "Tabungan Target",
        "target_create_hint" to "+ Buat Impian Kamu",
        "target_detail" to "Rincian Target",
        "setor_tabungan" to "Setor Nabung",

        // Recording cockpit
        "expense_mode" to "Pengeluaran Duit",
        "income_mode" to "Uang Masuk",
        "source_cuan" to "Sumber Cuan:",
        "expense_post" to "Pos Pengeluaran:",
        "nominal_input_label_expense" to "Nominal Pengeluaran (Rp)",
        "nominal_input_label_income" to "Nominal Masuk (Rp)",
        "nominal_input_label_emergency" to "Nominal Pengeluaran Darurat (Rp)",
        "nominal_input_placeholder" to "Ketik nominal (misal: 25000)",
        "select_wallet_expense" to "Pakai Dompet:",
        "select_wallet_income" to "Simpan ke Dompet:",
        "note_input_expense" to "Beli / bayar apa nih?",
        "note_input_income" to "Catatan cuan (opsional)",
        "btn_submit_expense" to "Potong",
        "btn_submit_income" to "+ Masuk",
        "emergency_cut_notice" to "Dipotong dari Simpanan Darurat Terpisah: %s",
        "emergency_place_hint" to "Lokasi simpan: %s. Limit darurat terpantau di bawah Total Saldo.",

        // Categories (6 Categories)
        "cat_makan_label" to "Makan & Ngopi",
        "cat_makan_sub" to "Kebutuhan Pokok",
        "cat_belanja_label" to "Belanja Bulanan",
        "cat_belanja_sub" to "Dapur & Kosan",
        "cat_transport_label" to "Bensin & Ojol",
        "cat_transport_sub" to "Mobilitas",
        "cat_tagihan_label" to "Listrik & Kuota",
        "cat_tagihan_sub" to "Tagihan Rutin",
        "cat_jajan_label" to "Jajan & Hiburan",
        "cat_jajan_sub" to "Gaya Hidup",
        "cat_darurat_label" to "Sosial & Tak Terduga",
        "cat_darurat_sub" to "Kondangan/Keluarga",

        // Incomes
        "income_gaji" to "Gaji Bulanan",
        "income_freelance" to "Side Hustle/Proyek",
        "income_bisnis" to "Hasil Usaha/Jualan",
        "income_investasi" to "Dividen & Cuan",
        "income_hadiah" to "Bonus/Transferan",

        // Income monitor bottom box
        "income_monitor_title" to "MONITOR UANG MASUK (%sx Masuk)",
        "income_today_title" to "UANG MASUK • HARI INI: +%s",
        "income_month_total" to "Total Masuk Bulan Ini",
        "income_ratio_label" to "Arus Kas Bersih (Surplus)",
        "income_rule_note" to "Saran: Simpan minimal 20%% (%s) buat Tabungan Target & Darurat.",
        "btn_see_income_details" to "Lihat Sumber & Rincian",

        // Bestie / Guru Roast
        "bestie_analysis_title" to "KATA BESTIE KEUANGAN HARI INI",
        "btn_refresh_word" to "Acak Kata",

        // Settings Screen Strings
        "settings_title" to "Pengaturan Aplikasi",
        "settings_subtitle" to "Kelola bahasa, ekspor laporan PDF, privasi data & preferensi",
        "sec_language" to "Pilihan Bahasa",
        "sec_language_desc" to "Ganti seluruh teks aplikasi ke Bahasa Indonesia atau English",
        "sec_appearance" to "Tampilan & Format Uang",
        "sec_appearance_desc" to "Format nominal Rupiah & preferensi sensor saldo",
        "sec_security" to "Keamanan & Privasi Data",
        "sec_security_desc" to "Kunci aplikasi & perlindungan data keuangan",
        "sec_notifications" to "Pengingat & Notifikasi",
        "sec_notifications_desc" to "Pengingat catat kas harian & getaran respon",
        "sec_data_storage" to "Penyimpanan & Cadangkan Dokumen PDF",
        "sec_data_storage_desc" to "Cadangkan laporan kas ke PDF modern atau cloud realtime",
        "sec_about" to "Tentang Aplikasi ArusKas",
        "sec_about_desc" to "ArusKas adalah aplikasi manajemen finansial pribadi yang mengedepankan keamanan data, privasi penuh, dan pencatatan yang terstruktur. Seluruh data transaksi dan saldo tersimpan 100% lokal di perangkat pengguna.",
        "sec_about_mission_title" to "Prinsip Utama ArusKas:",
        "sec_about_pillar1" to "1. Saldo Siap Pakai Murni: Memisahkan uang harian dari tabungan masa depan.",
        "sec_about_pillar2" to "2. Simpanan Darurat Terpisah: Perlindungan finansial mandiri saat kondisi tak terduga.",
        "sec_about_pillar3" to "3. Tabungan Target Terarah: Rencana pencapaian sasaran keuangan terukur.",
        "sec_about_pillar4" to "4. Kontrol Batas Pengeluaran: Monitoring limit harian dan kategori yang fleksibel.",
        "sec_about_tech_title" to "Standar Kualitas & Keamanan:",
        "sec_about_tech_desc" to "• 100% Offline-First: Seluruh data finansial tidak dikirim ke server eksternal, privasi mutlak pada perangkat Anda.\n• Desain Modern: Antarmuka clean, intuitif, dan responsif.\n• Native Android: Dibangun menggunakan teknologi modern Kotlin & Jetpack Compose.",

        // Setting Options
        "opt_lang_id" to "Bahasa Indonesia (Nasional)",
        "opt_lang_en" to "English (Global)",
        "opt_format_full" to "Format Rupiah Lengkap (Rp 150.000)",
        "opt_format_compact" to "Format Rupiah Ringkas (Rp 150 Rb / 1,5 Jt)",
        "opt_hide_balance" to "Sembunyikan Saldo di Beranda",
        "opt_hide_balance_desc" to "Sensor nominal saldo dengan '••••••' agar aman saat dilihat orang lain",
        "opt_app_lock" to "Kunci Keamanan Aplikasi",
        "opt_app_lock_desc" to "Gunakan PIN / Biometrik bawaan HP saat membuka aplikasi",
        "opt_daily_reminder" to "Pengingat Catat Harian",
        "opt_daily_reminder_desc" to "Ingatkan setiap malam jam 20:00 biar gak ada kas terlewat",
        "opt_haptic" to "Getaran Umpan Balik (Haptic)",
        "opt_haptic_desc" to "Getar halus saat tombol transaksi atau nominal dipencet",
        "opt_cloud_backup" to "Cadangkan Otomatis ke Cloud",
        "opt_cloud_backup_desc" to "Sinkronisasi realtime dengan Google Cloud Firestore",
        "opt_export_pdf" to "Cadangkan Laporan Rekap PDF (Tabel Modern)",
        "opt_export_pdf_desc" to "Cetak dokumen PDF resmi berdesain tabel Gen Z, ringkasan saldo & riwayat transaksi",
        "opt_clear_cache" to "Hapus Cache Aplikasi",
        "opt_clear_cache_desc" to "Segarkan ruang penyimpanan sementara perangkat",
        "opt_privacy_policy" to "Kebijakan Privasi & Ketentuan",
        "opt_privacy_policy_desc" to "Data 100% milik Anda dan dienkripsi aman",
        "cache_cleared_msg" to "Cache aplikasi berhasil disegarkan!",
        "pdf_generating_msg" to "Sedang menyusun dokumen PDF berdesain modern...",
        "pdf_saved_msg" to "File PDF '%s' berhasil disimpan di folder Dokumen perangkat!",
        "pdf_failed_msg" to "Gagal mencadangkan PDF: %s",

        // Starter setup
        "starter_title" to "Baru Pertama Kali Pakai ArusKas?",
        "starter_desc" to "Aktifkan dompet BCA, Bank Mas, DANA, ShopeePay, GoPay & target Laptop dalam 1 klik.",
        "starter_btn" to "Siapkan",

        // User Guide (Panduan Awal Pengguna)
        "btn_user_guide" to "Panduan",
        "btn_user_guide_full" to "Panduan Cara Kerja",
        "guide_badge" to "PANDUAN PEMULA • ARUSKAS",
        "guide_title" to "Cara Kerja ArusKas",
        "guide_subtitle" to "Biar gak bingung dan langsung jago kendalikan uangmu!",
        "guide_step_counter" to "Langkah %d dari %d",
        
        "guide_step1_badge" to "PILAR 1 • SALDO SIAP PAKAI",
        "guide_step1_title" to "Beda Saldo Bebas Belanja vs Total Uang",
        "guide_step1_desc" to "Saldo paling atas adalah Uang Siap Pakai. Ini uang yang benar-benar AMAN kamu belanjakan hari ini tanpa takut tekor. Duit tabungan & daruratmu sudah otomatis diproteksi dan dikunci di brankas terpisah.",
        "guide_step1_tip" to "💡 Kuncinya: Selama saldo siap pakai ini aman (hijau), kamu bebas jajan tanpa rasa bersalah!",
        
        "guide_step2_badge" to "PILAR 2 • CATAT KILAT 3 DETIK",
        "guide_step2_title" to "Catat Cepat dengan Garis Warna Pintar",
        "guide_step2_desc" to "Tinggal tap kategori (Makan, Jajan, atau tab Uang Masuk), ketik nominal, klik Simpan! Kotak nominal dilengkapi garis warna pintar (Hijau = Aman, Kuning = Waspada, Merah = Bahaya) serta sentilan jujur dari Bestie Keuangan.",
        "guide_step2_tip" to "💡 Kuncinya: Jangan tunda catat. Butuh cuma 3 detik sehabis bayar!",
        
        "guide_step3_badge" to "PILAR 3 • LIMIT KATEGORI & DANA DARURAT",
        "guide_step3_title" to "Pasang Rem Belanja & Amankan Darurat",
        "guide_step3_desc" to "Setiap pos pengeluaran punya batas limit (misal: Limit Jajan Rp 500rb/bulan). Kalau ada musibah atau kebutuhan mendadak, gunakan kategori 'Sosial & Tak Terduga' atau potong dari Brankas Darurat tanpa ganggu tabungan impian.",
        "guide_step3_tip" to "💡 Kuncinya: Tap kartu kategori kapan saja untuk menyesuaikan limit yang pas buat gaya hidupmu.",
        
        "guide_step4_badge" to "PILAR 4 • CELENGAN IMPIAN & PDF",
        "guide_step4_title" to "Kunci Target Impian & Pantau Grafik Cuan",
        "guide_step4_desc" to "Mau beli laptop baru atau traveling? Buat Celengan Target dan setor sisa uang harianmu. Buka tombol 'Grafik' untuk adu cuan pemasukan vs pengeluaran, atau unduh laporan tabel PDF modern di menu Pengaturan.",
        "guide_step4_tip" to "💡 Kuncinya: Duit yang masuk ke Celengan Target otomatis aman dan gak bakal kepotong belanja harian.",

        "guide_btn_skip" to "Lewati",
        "guide_btn_prev" to "Kembali",
        "guide_btn_next" to "Lanjut",
        "guide_btn_finish" to "Paham, Mulai Sekarang! 🚀",
        "guide_btn_setup_now" to "Atur Saldo Awal (BCA, DANA, dll)",
        "sec_guide_card_title" to "Panduan Cara Kerja ArusKas",
        "sec_guide_card_desc" to "Pelajari kembali 4 pilar penyelamat finansial anti-boncos",

        // Subscription & Free Trial (Rp 7.000/bln + Free Trial 30 Hari)
        "pro_badge" to "ARUSKAS PRO",
        "pro_trial_chip" to "PRO Trial • Sisa %d Hari",
        "pro_active_chip" to "PRO Aktif ⭐",
        "pro_expired_chip" to "Trial Berakhir • Aktifkan PRO",
        "paywall_title" to "ArusKas PRO • Penyelamat Finansial",
        "paywall_subtitle" to "Bebas atur seluruh uang, capai impian lebih cepat & hindari boncos tanpa batas.",
        "paywall_price_headline" to "Rp 7.000 / Bulan",
        "paywall_price_sub" to "Hanya ~Rp 230/hari • Lebih hemat dari sebutir permen!",
        "paywall_trial_highlight" to "GRATIS 30 HARI PERTAMA (Rp 0 HARI INI)",
        "paywall_feature1" to "Dompet & Rekening Tanpa Batas (BCA, DANA, GoPay, ShopeePay, Bank Mas)",
        "paywall_feature2" to "Ekspor Dokumen Laporan Tabel PDF Modern Tanpa Batas",
        "paywall_feature3" to "Bestie Keuangan AI Real-time & Analisis Cashflow Harian",
        "paywall_feature4" to "Celengan Target Impian Tanpa Batas dengan Proteksi Saldo Terkunci",
        "paywall_feature5" to "Pencadangan Realtime Cloud & Keamanan Enkripsi Google",
        "paywall_policy_transparency" to "Langganan otomatis diperpanjang Rp 7.000/bulan setelah uji coba gratis 30 hari selesai. Anda dapat membatalkan langganan kapan saja dengan mudah melalui Google Play Store tanpa biaya.",
        "btn_start_free_trial" to "Mulai Uji Coba Gratis 30 Hari (Rp 0)",
        "btn_subscribe_now" to "Langganan Sekarang (Rp 7.000/bln)",
        "btn_restore_purchase" to "Pulihkan Pembelian",
        "btn_manage_subscription" to "Kelola Langganan di Play Store",
        "msg_trial_activated" to "Selamat! Uji Coba Gratis 30 Hari ArusKas PRO Berhasil Aktif!",
        "msg_pro_subscribed" to "Terima kasih! ArusKas PRO berhasil diaktifkan sepenuhnya!",
        "msg_subscription_restored" to "Status langganan ArusKas PRO berhasil diverifikasi!",
        "pro_lifetime_chip" to "PRO Selamanya 👑",
        "pro_free_forever_chip" to "Versi Gratis Selamanya • Upgrade PRO",
        "sec_subscription_title" to "Status Langganan ArusKas PRO",
        "sec_subscription_desc" to "Bulanan • Tahunan (Hemat 45%) • Sekali Bayar Selamanya",

        // Report Saving & Export Tool
        "btn_save_report" to "Simpan Laporan",
        "report_modal_title" to "Rekap Laporan Keuangan",
        "report_modal_subtitle" to "Tabel rapi siap ekspor ke Excel / Google Sheets & PDF resmi",
        "report_export_csv" to "Ekspor Tabel Excel (.csv)",
        "report_export_pdf" to "Ekspor Laporan PDF (.pdf)",
        "report_copy_text" to "Salin Rangkuman Teks",
        "report_filter_all" to "Semua",
        "report_filter_this_month" to "Bulan Ini",
        "report_filter_7days" to "7 Hari Terakhir",
        "report_total_inflow" to "Total Masuk",
        "report_total_outflow" to "Total Keluar",
        "report_net_cashflow" to "Arus Kas Bersih",
        "report_no_data" to "Belum ada transaksi pada periode ini.",
        "msg_copied_clipboard" to "Rangkuman laporan disalin ke papan klip!",

        // Accessibility Settings (Fonts & Contrast)
        "sec_accessibility" to "Aksesibilitas & Tampilan Huruf",
        "sec_accessibility_desc" to "Atur ukuran teks, angka nominal, kontras, dan tema visual",
        "opt_font_size" to "Ukuran Huruf & Angka",
        "opt_font_normal" to "Standar (100%)",
        "opt_font_large" to "Besar (115%)",
        "opt_font_xlarge" to "Ekstra Besar (130%)",
        "opt_bold_numbers" to "Tebalkan Angka Nominal",
        "opt_bold_numbers_desc" to "Tampilkan angka lebih tebal agar cepat dibaca",
        "opt_high_contrast" to "Mode Kontras Tinggi",
        "opt_high_contrast_desc" to "Pertajam kontras kartu & batas elemen untuk kenyamanan mata",
        "opt_theme_accent" to "Pilihan Aksen Warna",

        // WhatsApp-Style Permanent Cloud Sync
        "sec_cloud_whatsapp_title" to "Penyimpanan & Sinkronisasi Cloud",
        "sec_cloud_whatsapp_desc" to "Cadangan otomatis multi-bulan seperti WhatsApp / Google Drive",
        "cloud_sync_info" to "Data berbulan-bulan aman tersimpan di Google Firestore. Ganti HP tanpa takut kehilangan catatan, saldo, atau tagihan.",
        "cloud_status_synced" to "Tersinkronkan ke Cloud",
        "cloud_status_offline" to "Mode Penyimpanan Lokal",
        "cloud_last_synced" to "Sinkronisasi Terakhir: %s",
        "cloud_never_synced" to "Belum pernah disinkronkan",
        "btn_sync_now" to "Sinkronkan Sekarang",
        "msg_syncing_cloud" to "Sedang menyinkronkan data ke Cloud Firestore...",
        "msg_sync_success" to "Data berhasil disinkronkan ke Cloud Firestore!",

        // Revisit Onboarding
        "sec_reopen_onboarding_title" to "Pertanyaan Awal & Profil Keuangan",
        "sec_reopen_onboarding_desc" to "Atur ulang jawaban negara, mata uang, pekerjaan, dan jadwal harian",
        "btn_reopen_onboarding" to "Buka Kuesioner Profil"
    )

    private val EN_STRINGS = mapOf(
        // Header & Balance
        "app_title" to "ArusKas",
        "free_to_use_money" to "SAFE-TO-SPEND (READY FOR DAILY EXPENSES)",
        "total_wallet_money" to "Total Net Worth: %s (Including Savings & Emergency)",
        "see_all_wallet_details" to "View breakdown of all funds in wallet",
        "btn_signin" to "Sign In",
        "btn_signout" to "Sign Out",
        "btn_graphic" to "Charts",
        "btn_settings" to "Settings",
        "btn_close" to "Close",
        "btn_save" to "Save",
        "btn_cancel" to "Cancel",
        "btn_delete" to "Delete",

        // Emergency strip
        "emergency_limit_label" to "Emergency Limit: %s/%s • Stash: %s",
        "btn_add_emergency" to "+ Emergency",
        "status_safe" to "SAFE",
        "status_warning" to "WARN",
        "status_over" to "OVER!",
        "status_free" to "FREE",

        // Metrics & Tabungan Target
        "label_today" to "Today",
        "label_this_month" to "This Month",
        "target_savings" to "Target Savings",
        "target_create_hint" to "+ Create Your Dream Goal",
        "target_detail" to "Target Details",
        "setor_tabungan" to "Deposit Fund",

        // Recording cockpit
        "expense_mode" to "Record Expense",
        "income_mode" to "Cash Inflow",
        "source_cuan" to "Income Stream:",
        "expense_post" to "Expense Category:",
        "nominal_input_label_expense" to "Expense Amount (IDR)",
        "nominal_input_label_income" to "Inflow Amount (IDR)",
        "nominal_input_label_emergency" to "Emergency Amount (IDR)",
        "nominal_input_placeholder" to "Enter amount (e.g., 25000)",
        "select_wallet_expense" to "Charge From Wallet:",
        "select_wallet_income" to "Deposit To Wallet:",
        "note_input_expense" to "What was this purchase for?",
        "note_input_income" to "Income note (optional)",
        "btn_submit_expense" to "Deduct",
        "btn_submit_income" to "+ Deposit",
        "emergency_cut_notice" to "Deducted from Isolated Emergency Stash: %s",
        "emergency_place_hint" to "Stored at: %s. Emergency limit monitored below Total Balance.",

        // Categories (6 Categories)
        "cat_makan_label" to "Food & Coffee",
        "cat_makan_sub" to "Daily Meals",
        "cat_belanja_label" to "Monthly Grocery",
        "cat_belanja_sub" to "Kitchen & Home",
        "cat_transport_label" to "Fuel & Commute",
        "cat_transport_sub" to "Rides & Transit",
        "cat_tagihan_label" to "Power & Data",
        "cat_tagihan_sub" to "Bills & Utilities",
        "cat_jajan_label" to "Fun & Treats",
        "cat_jajan_sub" to "Lifestyle",
        "cat_darurat_label" to "Social & Family",
        "cat_darurat_sub" to "Unplanned Aid",

        // Incomes
        "income_gaji" to "Monthly Salary",
        "income_freelance" to "Freelance / Hustle",
        "income_bisnis" to "Business Profits",
        "income_investasi" to "Dividends & Returns",
        "income_hadiah" to "Bonus / Gifts",

        // Income monitor bottom box
        "income_monitor_title" to "INCOME INFLOW RADAR (%sx Inflows)",
        "income_today_title" to "INCOME INFLOW • TODAY: +%s",
        "income_month_total" to "Total Inflows This Month",
        "income_ratio_label" to "Net Monthly Cashflow (Surplus)",
        "income_rule_note" to "Tip: Set aside at least 20%% (%s) into Target Savings & Emergency Stash.",
        "btn_see_income_details" to "View Streams & Details",

        // Bestie / Guru Roast
        "bestie_analysis_title" to "FINANCIAL BESTIE QUOTE OF THE DAY",
        "btn_refresh_word" to "Shuffle Quote",

        // Settings Screen Strings
        "settings_title" to "App Settings",
        "settings_subtitle" to "Manage language, export modern PDF table report, security & preferences",
        "sec_language" to "Language Options",
        "sec_language_desc" to "Switch app language between Bahasa Indonesia and English",
        "sec_appearance" to "Display & Currency Format",
        "sec_appearance_desc" to "Rupiah display formatting and privacy balance masking",
        "sec_security" to "Security & Privacy",
        "sec_security_desc" to "App lock and personal financial privacy protection",
        "sec_notifications" to "Reminders & Notifications",
        "sec_notifications_desc" to "Daily 20:00 bookkeeping reminder and haptic touch feedback",
        "sec_data_storage" to "Backup & Modern PDF Table Report",
        "sec_data_storage_desc" to "Export full ledger to sleek Gen Z PDF report or realtime cloud sync",
        "sec_about" to "About ArusKas Application",
        "sec_about_desc" to "ArusKas is a personal financial management platform built for robust privacy, zero third-party data leaks, and disciplined budgeting. All transaction records and balances reside 100% locally on your device.",
        "sec_about_mission_title" to "Core ArusKas Principles:",
        "sec_about_pillar1" to "1. Pure Safe-to-Spend: Shields your daily routine from eating into future savings.",
        "sec_about_pillar2" to "2. Isolated Emergency Shield: Standalone fortress for unforeseen emergencies.",
        "sec_about_pillar3" to "3. Goal Milestone Piggy Bank: Systematic discipline to realize your biggest ambitions.",
        "sec_about_pillar4" to "4. Flexible Limit Control: Real-time monitoring of daily and category caps.",
        "sec_about_tech_title" to "Quality & Security Standards:",
        "sec_about_tech_desc" to "• 100% Offline-First: No financial records are transmitted to external servers; total device privacy.\n• Modern Clean Design: Sleek, intuitive, and responsive financial dashboard.\n• Native Android: Built with pure Kotlin & high-performance Jetpack Compose.",

        // Setting Options
        "opt_lang_id" to "Bahasa Indonesia (National)",
        "opt_lang_en" to "English (Global)",
        "opt_format_full" to "Full Currency Format (Rp 150.000)",
        "opt_format_compact" to "Compact Currency Format (Rp 150 Rb / 1,5 Jt)",
        "opt_hide_balance" to "Hide Balance on Dashboard",
        "opt_hide_balance_desc" to "Mask balance with '••••••' to prevent onlookers from seeing your money",
        "opt_app_lock" to "App Security Lock",
        "opt_app_lock_desc" to "Use device PIN or Fingerprint biometrics upon opening",
        "opt_daily_reminder" to "Daily Log Reminder",
        "opt_daily_reminder_desc" to "Get an alert every evening at 20:00 to log daily expenses",
        "opt_haptic" to "Haptic Feedback",
        "opt_haptic_desc" to "Gentle tactile vibration when tapping buttons or recording numbers",
        "opt_cloud_backup" to "Realtime Cloud Sync",
        "opt_cloud_backup_desc" to "Instant synchronization with Google Cloud Firestore",
        "opt_export_pdf" to "Backup to PDF Document (Modern Table)",
        "opt_export_pdf_desc" to "Generate a modern Gen Z PDF report with structured tables, summary metrics & logs",
        "opt_clear_cache" to "Clear App Cache",
        "opt_clear_cache_desc" to "Free up temporary cached device storage",
        "opt_privacy_policy" to "Privacy Policy & Terms",
        "opt_privacy_policy_desc" to "100% user-owned data, encrypted and protected",
        "cache_cleared_msg" to "App cache successfully refreshed!",
        "pdf_generating_msg" to "Generating modern PDF ledger document...",
        "pdf_saved_msg" to "PDF file '%s' saved to device Documents folder!",
        "pdf_failed_msg" to "Failed to backup PDF: %s",

        // Starter setup
        "starter_title" to "First Time Using ArusKas?",
        "starter_desc" to "Setup BCA, Bank Mas, DANA, ShopeePay, GoPay & Laptop goal in 1 click.",
        "starter_btn" to "Setup Now",

        // User Guide (Initial User Guide)
        "btn_user_guide" to "Guide",
        "btn_user_guide_full" to "App Quick Guide",
        "guide_badge" to "BEGINNER GUIDE • ARUSKAS",
        "guide_title" to "How ArusKas Works",
        "guide_subtitle" to "Master your cashflow in minutes with zero guesswork!",
        "guide_step_counter" to "Step %d of %d",
        
        "guide_step1_badge" to "PILLAR 1 • SAFE-TO-SPEND",
        "guide_step1_title" to "Safe-to-Spend vs Total Net Worth",
        "guide_step1_desc" to "The top balance shows your Safe-to-Spend cash. This is the exact amount you can spend today without worrying. Your dream savings and emergency reserves are automatically protected and locked in isolated vaults.",
        "guide_step1_tip" to "💡 Key Rule: As long as Safe-to-Spend is green, spend with zero guilt!",
        
        "guide_step2_badge" to "PILLAR 2 • 3-SECOND LIGHTNING LOG",
        "guide_step2_title" to "Smart Color Lines & Instant Feedback",
        "guide_step2_desc" to "Tap a category (Meals, Treats, or switch to Inflow), enter the amount, and tap Save! The amount field features an active color line (Green = Safe, Yellow = Caution, Red = Danger) plus blunt commentary from your Financial Bestie.",
        "guide_step2_tip" to "💡 Key Rule: Never postpone logging. It takes barely 3 seconds right after checkout!",
        
        "guide_step3_badge" to "PILLAR 3 • LIMIT CAPS & EMERGENCY SHIELD",
        "guide_step3_title" to "Set Spending Caps & Guard Emergencies",
        "guide_step3_desc" to "Each expense category has a custom cap (e.g. Treats cap at Rp 500k/mo). In case of genuine emergencies, tap the Emergency category to draw from your isolated stash without sacrificing your savings goals.",
        "guide_step3_tip" to "💡 Key Rule: Tap any category card anytime to customize its budget limit.",
        
        "guide_step4_badge" to "PILLAR 4 • GOAL PIGGY BANK & PDF",
        "guide_step4_title" to "Lock In Dreams & Inspect Visual Charts",
        "guide_step4_desc" to "Saving for a laptop or dream getaway? Set up Target Savings and deposit spare cash. Tap 'Charts' to see your inflow vs outflow ratio, or export a sleek Gen Z table PDF report in Settings.",
        "guide_step4_tip" to "💡 Key Rule: Any money deposited into Target Savings is segregated and cannot be spent accidentally.",

        "guide_btn_skip" to "Skip",
        "guide_btn_prev" to "Back",
        "guide_btn_next" to "Next",
        "guide_btn_finish" to "Got It, Let's Begin! 🚀",
        "guide_btn_setup_now" to "Set Up Starter Balances (BCA, DANA, etc)",
        "sec_guide_card_title" to "How ArusKas Works (Guide)",
        "sec_guide_card_desc" to "Review the 4 financial lifesaver pillars and key rules",

        // Subscription & Free Trial (IDR 7,000/mo + 30-Day Free Trial)
        "pro_badge" to "ARUSKAS PRO",
        "pro_trial_chip" to "PRO Trial • %d Days Left",
        "pro_active_chip" to "PRO Active ⭐",
        "pro_expired_chip" to "Trial Expired • Upgrade to PRO",
        "paywall_title" to "ArusKas PRO • Financial Lifesaver",
        "paywall_subtitle" to "Master your cashflow, crush savings goals faster & eliminate money stress.",
        "paywall_price_headline" to "IDR 7,000 / Month",
        "paywall_price_sub" to "Only ~IDR 230/day • Cheaper than a piece of candy!",
        "paywall_trial_highlight" to "FIRST 30 DAYS 100% FREE (IDR 0 TODAY)",
        "paywall_feature1" to "Unlimited Wallets & Bank Accounts (BCA, DANA, GoPay, ShopeePay, Bank Mas)",
        "paywall_feature2" to "Unlimited Modern Gen Z Table PDF Ledger Exports",
        "paywall_feature3" to "Realtime AI Financial Bestie & Daily Cashflow Roasts",
        "paywall_feature4" to "Unlimited Goal Piggy Banks with Automatic Fund Locking",
        "paywall_feature5" to "Realtime Google Cloud Sync & Encrypted Security",
        "paywall_policy_transparency" to "Auto-renews at IDR 7,000/month after 30-day free trial. Cancel anytime in Google Play Store subscriptions at zero fee.",
        "btn_start_free_trial" to "Start 30-Day Free Trial (IDR 0)",
        "btn_subscribe_now" to "Subscribe Now (IDR 7,000/mo)",
        "btn_restore_purchase" to "Restore Purchases",
        "btn_manage_subscription" to "Manage Subscription in Play Store",
        "msg_trial_activated" to "Congrats! 30-day free trial of ArusKas PRO is active!",
        "msg_pro_subscribed" to "Thank you! ArusKas PRO is now fully unlocked!",
        "msg_subscription_restored" to "ArusKas PRO subscription status verified!",
        "pro_lifetime_chip" to "PRO Lifetime 👑",
        "pro_free_forever_chip" to "Free Forever Tier • Upgrade to PRO",
        "sec_subscription_title" to "ArusKas PRO Subscription Status",
        "sec_subscription_desc" to "Monthly • Annual (Save 45%) • One-Time Lifetime",

        // Report Saving & Export Tool
        "btn_save_report" to "Save Report",
        "report_modal_title" to "Financial Statement & Report",
        "report_modal_subtitle" to "Structured tables ready for Excel, Google Sheets, or PDF",
        "report_export_csv" to "Export Excel / CSV (.csv)",
        "report_export_pdf" to "Export PDF Statement (.pdf)",
        "report_copy_text" to "Copy Text Summary",
        "report_filter_all" to "All Time",
        "report_filter_this_month" to "This Month",
        "report_filter_7days" to "Last 7 Days",
        "report_total_inflow" to "Total Inflow",
        "report_total_outflow" to "Total Outflow",
        "report_net_cashflow" to "Net Cashflow",
        "report_no_data" to "No transactions recorded for this period.",
        "msg_copied_clipboard" to "Report summary copied to clipboard!",

        // Accessibility Settings (Fonts & Contrast)
        "sec_accessibility" to "Accessibility & Typography",
        "sec_accessibility_desc" to "Configure text size, number boldness, high contrast, and visual theme",
        "opt_font_size" to "Font & Number Size",
        "opt_font_normal" to "Normal (100%)",
        "opt_font_large" to "Large (115%)",
        "opt_font_xlarge" to "Extra Large (130%)",
        "opt_bold_numbers" to "Bold Amounts & Digits",
        "opt_bold_numbers_desc" to "Emphasize numbers for effortless scanning",
        "opt_high_contrast" to "High Contrast Mode",
        "opt_high_contrast_desc" to "Enhance card outlines & typography for visual clarity",
        "opt_theme_accent" to "Accent Color Theme",

        // WhatsApp-Style Permanent Cloud Sync
        "sec_cloud_whatsapp_title" to "Permanent Cloud Storage & Sync",
        "sec_cloud_whatsapp_desc" to "Continuous multi-month backup like WhatsApp / Google Drive",
        "cloud_sync_info" to "Months of records securely preserved in Google Firestore. Switch devices without losing balances, transactions, or bills.",
        "cloud_status_synced" to "Synced to Cloud Firestore",
        "cloud_status_offline" to "Device-Local Storage Mode",
        "cloud_last_synced" to "Last Synced: %s",
        "cloud_never_synced" to "Never synced yet",
        "btn_sync_now" to "Sync Now",
        "msg_syncing_cloud" to "Syncing data to Cloud Firestore...",
        "msg_sync_success" to "Data successfully backed up to Cloud Firestore!",

        // Revisit Onboarding
        "sec_reopen_onboarding_title" to "Initial Setup & Questions",
        "sec_reopen_onboarding_desc" to "Revisit your country, currency, employment, and daily routine settings",
        "btn_reopen_onboarding" to "Open Profile Questionnaire"
    )
}
