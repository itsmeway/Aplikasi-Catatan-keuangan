package com.example.data.offlineinsight

import com.example.data.AppLanguage

/**
 * 12 Karakter Komunikasi (Personas) dengan kuota kalimat wajib:
 * 1. SI_ANALIS: 900
 * 2. SI_ROASTER: 900
 * 3. SI_BESTIE: 900
 * 4. SI_MOTIVATOR: 900
 * 5. SI_STRATEGIS: 900
 * 6. SI_REALISTIS: 800
 * 7. SI_DETEKTIF: 800
 * 8. SI_EMPATI: 800
 * 9. SI_AMBISIUS: 800
 * 10. SI_SARKAS: 800
 * 11. SI_MENTOR: 750
 * 12. SI_HYPE: 750
 * TOTAL = 10.000 Kalimat Unik.
 */
enum class InsightPersona(
    val codePrefix: String,
    val expectedQuota: Int,
    val labelId: String,
    val labelEn: String,
    val descId: String,
    val descEn: String,
    val badgeEmoji: String,
    val usesHumorOrRoast: Boolean = false
) {
    SI_ANALIS(
        codePrefix = "ANL",
        expectedQuota = 900,
        labelId = "Si Analis",
        labelEn = "The Analyst",
        descId = "Logis, berbasis angka, objektif, dan menjelaskan situasi secara terukur.",
        descEn = "Logical, data-driven, objective, and explains situations measurably.",
        badgeEmoji = "📊",
        usesHumorOrRoast = false
    ),
    SI_ROASTER(
        codePrefix = "RST",
        expectedQuota = 900,
        labelId = "Si Roaster",
        labelEn = "The Roaster",
        descId = "Ceplas-ceplos, lucu, santai, dan menyindir kebiasaan boros tanpa menghina.",
        descEn = "Witty, playful, casual, and teases overspending habits without insulting.",
        badgeEmoji = "🔥",
        usesHumorOrRoast = true
    ),
    SI_BESTIE(
        codePrefix = "BST",
        expectedQuota = 900,
        labelId = "Si Kawan Dekat",
        labelEn = "The Bestie",
        descId = "Gaya teman dekat, akrab, santai, menggunakan bahasa sehari-hari.",
        descEn = "Warm close-friend style, relatable, casual everyday language.",
        badgeEmoji = "🤝",
        usesHumorOrRoast = false
    ),
    SI_MOTIVATOR(
        codePrefix = "MTV",
        expectedQuota = 900,
        labelId = "Si Motivator",
        labelEn = "The Motivator",
        descId = "Mendorong konsistensi dan semangat memperbaiki kondisi keuangan.",
        descEn = "Encourages consistency and positive drive to improve finances.",
        badgeEmoji = "⚡",
        usesHumorOrRoast = false
    ),
    SI_STRATEGIS(
        codePrefix = "STR",
        expectedQuota = 900,
        labelId = "Si Strategis",
        labelEn = "The Strategist",
        descId = "Memberikan solusi konkret dan langkah taktis yang bisa langsung dilakukan.",
        descEn = "Delivers concrete solutions and immediate tactical action steps.",
        badgeEmoji = "🎯",
        usesHumorOrRoast = false
    ),
    SI_REALISTIS(
        codePrefix = "RLS",
        expectedQuota = 800,
        labelId = "Si Realistis",
        labelEn = "The Realist",
        descId = "Berbicara jujur, tanpa harapan palsu, menjelaskan konsekuensi finansial.",
        descEn = "Speaks honestly without sugarcoating, explaining financial consequences.",
        badgeEmoji = "⚖️",
        usesHumorOrRoast = false
    ),
    SI_DETEKTIF(
        codePrefix = "DTK",
        expectedQuota = 800,
        labelId = "Si Detektif",
        labelEn = "The Detective",
        descId = "Mencari pola transaksi, kebiasaan berulang, dan kategori paling boros.",
        descEn = "Tracks transaction patterns, recurring habits, and top spending categories.",
        badgeEmoji = "🔍",
        usesHumorOrRoast = false
    ),
    SI_EMPATI(
        codePrefix = "EMP",
        expectedQuota = 800,
        labelId = "Si Empati",
        labelEn = "The Empath",
        descId = "Memahami kondisi pengguna, tidak menghakimi, dan tetap memberi arahan tenang.",
        descEn = "Understanding, non-judgmental, and offers calm, supportive guidance.",
        badgeEmoji = "🌿",
        usesHumorOrRoast = false
    ),
    SI_AMBISIUS(
        codePrefix = "AMB",
        expectedQuota = 800,
        labelId = "Si Ambisius",
        labelEn = "The Ambitious",
        descId = "Berfokus pada target, pencapaian, akselerasi tabungan, dan pertumbuhan aset.",
        descEn = "Focused on milestones, achievements, savings velocity, and asset growth.",
        badgeEmoji = "🚀",
        usesHumorOrRoast = false
    ),
    SI_SARKAS(
        codePrefix = "SRK",
        expectedQuota = 800,
        labelId = "Si Sarkas",
        labelEn = "The Sarcastic",
        descId = "Humor tajam yang cerdas, tetap sopan, dan tidak merendahkan pengguna.",
        descEn = "Clever, dry wit that stays polite and never belittles the user.",
        badgeEmoji = "🎭",
        usesHumorOrRoast = true
    ),
    SI_MENTOR(
        codePrefix = "MNT",
        expectedQuota = 750,
        labelId = "Si Mentor",
        labelEn = "The Mentor",
        descId = "Memberikan nasihat matang, tenang, dan membantu mengambil keputusan bijak.",
        descEn = "Offers mature, calm wisdom to help make sound long-term decisions.",
        badgeEmoji = "🧭",
        usesHumorOrRoast = false
    ),
    SI_HYPE(
        codePrefix = "HYP",
        expectedQuota = 750,
        labelId = "Si Hype",
        labelEn = "The Hype",
        descId = "Merayakan pencapaian kecil maupun besar dan memberi dorongan positif.",
        descEn = "Celebrates small and big wins alike with energetic positive reinforcement.",
        badgeEmoji = "🎉",
        usesHumorOrRoast = false
    );

    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) labelId else labelEn

    fun description(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) descId else descEn

    companion object {
        fun fromNameOrDefault(raw: String?, default: InsightPersona = SI_BESTIE): InsightPersona {
            if (raw.isNullOrBlank()) return default
            return entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: default
        }
    }
}

enum class InsightCategory(
    val labelId: String,
    val labelEn: String
) {
    PERINGATAN("Peringatan", "Warning"),
    KEUANGAN("Keuangan", "Finance"),
    TABUNGAN("Tabungan", "Savings"),
    APRESIASI("Apresiasi", "Appreciation"),
    ANGGARAN("Anggaran", "Budget"),
    TAGIHAN("Tagihan", "Bills"),
    KEBIASAAN("Kebiasaan", "Habits"),
    STRATEGI("Strategi", "Strategy");

    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) labelId else labelEn

    companion object {
        fun fromNameOrDefault(raw: String?): InsightCategory {
            if (raw.isNullOrBlank()) return KEUANGAN
            return entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: KEUANGAN
        }
    }
}

/**
 * 50 Kode Kondisi Keuangan Terukur (A s/d I).
 * Priority: 1 = Paling Kritis/Mendesak ... 7 = Motivasi Umum.
 */
enum class FinancialConditionCode(
    val defaultCategory: InsightCategory,
    val defaultPriority: Int,
    val titleId: String,
    val titleEn: String
) {
    // A. PEMASUKAN
    INCOME_INCREASED(InsightCategory.APRESIASI, 6, "Pemasukan Meningkat", "Income Increased"),
    INCOME_DECREASED(InsightCategory.PERINGATAN, 4, "Pemasukan Menurun", "Income Decreased"),
    INCOME_STABLE(InsightCategory.KEUANGAN, 6, "Pemasukan Stabil", "Income Stable"),
    INCOME_IRREGULAR(InsightCategory.STRATEGI, 4, "Pemasukan Tidak Teratur", "Irregular Income"),
    NO_INCOME_PERIOD(InsightCategory.PERINGATAN, 3, "Belum Ada Pemasukan Tercatat", "No Recorded Income in Period"),

    // B. PENGELUARAN
    EXPENSE_NORMAL(InsightCategory.KEUANGAN, 6, "Pengeluaran Terkendali Normal", "Normal Controlled Spending"),
    EXPENSE_INCREASED(InsightCategory.PERINGATAN, 4, "Pengeluaran Meningkat", "Spending Increased"),
    EXPENSE_DECREASED(InsightCategory.APRESIASI, 6, "Pengeluaran Menurun Hemat", "Spending Decreased"),
    EXPENSE_EXCESSIVE(InsightCategory.PERINGATAN, 2, "Pengeluaran Berlebihan", "Excessive Spending"),
    CATEGORY_OVERSPENT(InsightCategory.ANGGARAN, 3, "Kategori Pengeluaran Terlalu Besar", "Overspent Category"),
    EXPENSE_UNUSUAL_SPIKE(InsightCategory.PERINGATAN, 3, "Lonjakan Pengeluaran Tidak Biasa", "Unusual Spending Spike"),

    // C. SALDO
    BALANCE_SAFE(InsightCategory.KEUANGAN, 6, "Kondisi Saldo Aman", "Safe Balance Level"),
    BALANCE_LOW(InsightCategory.PERINGATAN, 3, "Saldo Mulai Menipis", "Low Balance Warning"),
    BALANCE_CRITICAL(InsightCategory.PERINGATAN, 1, "Saldo Kritis", "Critical Balance Alert"),
    BALANCE_INSUFFICIENT_FOR_BILLS(InsightCategory.PERINGATAN, 1, "Saldo Kurang untuk Kewajiban", "Balance Insufficient for Bills"),

    // D. ANGGARAN DAN LIMIT
    BUDGET_SAFE(InsightCategory.ANGGARAN, 6, "Anggaran Masih Aman", "Budget Within Safe Limit"),
    BUDGET_NEAR_LIMIT(InsightCategory.ANGGARAN, 4, "Anggaran Mendekati Batas", "Budget Approaching Limit"),
    BUDGET_ALMOST_EXHAUSTED(InsightCategory.PERINGATAN, 3, "Anggaran Hampir Habis", "Budget Nearly Exhausted"),
    BUDGET_EXHAUSTED(InsightCategory.PERINGATAN, 2, "Anggaran Habis 100%", "Budget 100% Exhausted"),
    BUDGET_EXCEEDED(InsightCategory.PERINGATAN, 2, "Pengeluaran Melewati Limit", "Budget Limit Exceeded"),
    CATEGORY_SWELLING(InsightCategory.ANGGARAN, 4, "Pembengkakan Kategori Cepat", "Rapid Category Swelling"),

    // E. TABUNGAN
    SAVINGS_INCREASED(InsightCategory.TABUNGAN, 6, "Tabungan Meningkat", "Savings Increased"),
    SAVINGS_DECREASED(InsightCategory.PERINGATAN, 4, "Tabungan Menurun", "Savings Decreased"),
    SAVINGS_STAGNANT(InsightCategory.TABUNGAN, 5, "Tabungan Belum Bertambah", "Savings Stagnant"),
    SAVINGS_GOAL_REACHED(InsightCategory.APRESIASI, 6, "Target Tabungan Tercapai", "Savings Goal Reached"),
    SAVINGS_GOAL_BEHIND(InsightCategory.TABUNGAN, 4, "Target Tabungan Tertinggal", "Savings Goal Behind Schedule"),
    SAVINGS_CONSISTENT(InsightCategory.APRESIASI, 6, "Konsisten Menyisihkan Uang", "Consistent Saving Discipline"),

    // F. DANA DARURAT
    EMERGENCY_NONE(InsightCategory.PERINGATAN, 3, "Belum Memiliki Dana Darurat", "No Emergency Fund Yet"),
    EMERGENCY_INSUFFICIENT(InsightCategory.KEUANGAN, 5, "Dana Darurat Masih Kurang", "Emergency Fund Below Target"),
    EMERGENCY_SUFFICIENT(InsightCategory.APRESIASI, 6, "Dana Darurat Memadai", "Sufficient Emergency Fund"),
    EMERGENCY_USED(InsightCategory.PERINGATAN, 4, "Dana Darurat Digunakan", "Emergency Fund Utilized"),
    EMERGENCY_REBUILD_NEEDED(InsightCategory.STRATEGI, 4, "Dana Darurat Perlu Dibangun Kembali", "Emergency Fund Needs Rebuilding"),

    // G. UTANG DAN CICILAN
    DEBT_CONTROLLED(InsightCategory.KEUANGAN, 6, "Kewajiban Rutin Terkendali", "Obligations Controlled"),
    DEBT_HEAVY_BURDEN(InsightCategory.PERINGATAN, 3, "Beban Cicilan & Tagihan Besar", "Heavy Bill & Debt Burden"),
    BILL_DUE_SOON(InsightCategory.TAGIHAN, 3, "Tagihan Mendekati Jatuh Tempo", "Bill Due Soon"),
    BILL_OVERDUE(InsightCategory.PERINGATAN, 2, "Tagihan Melewati Jatuh Tempo", "Overdue Mandatory Bill"),
    DEBT_TO_INCOME_HIGH(InsightCategory.PERINGATAN, 3, "Rasio Kewajiban Terhadap Pemasukan Tinggi", "High Obligation-to-Income Ratio"),

    // H. KEBIASAAN KEUANGAN (30 HARI)
    HABIT_FREQUENT_OVERBUDGET(InsightCategory.KEBIASAAN, 4, "Sering Melewati Batas Anggaran", "Frequently Exceeding Budget"),
    HABIT_CONSISTENT_SAVER(InsightCategory.APRESIASI, 6, "Pola Konsisten Menabung", "Consistent Saver Pattern"),
    HABIT_LEISURE_INCREASED(InsightCategory.KEBIASAAN, 4, "Pengeluaran Hiburan Meningkat", "Increased Leisure Spending"),
    HABIT_WEEKEND_HEAVY(InsightCategory.KEBIASAAN, 5, "Pengeluaran Akhir Pekan Tinggi", "Higher Weekend Spending"),
    HABIT_IMPULSIVE_INDICATOR(InsightCategory.KEBIASAAN, 4, "Indikasi Transaksi Beruntun Cepat", "Rapid Back-to-Back Spending Indicator"),
    HABIT_IMPROVING(InsightCategory.APRESIASI, 6, "Kebiasaan Keuangan Membaik", "Financial Habits Improving"),
    HABIT_WORSENING(InsightCategory.PERINGATAN, 4, "Kebiasaan Keuangan Memburuk", "Financial Habits Worsening"),

    // I. KONDISI GABUNGAN
    COMBO_INCOME_DOWN_EXPENSE_UP(InsightCategory.PERINGATAN, 2, "Pemasukan Turun & Pengeluaran Naik", "Income Down & Expenses Up"),
    COMBO_LOW_BALANCE_BILL_DUE(InsightCategory.PERINGATAN, 1, "Saldo Menipis & Tagihan Jatuh Tempo", "Low Balance & Bill Due Soon"),
    COMBO_SAVINGS_UP_LEISURE_OVER(InsightCategory.STRATEGI, 4, "Tabungan Naik Namun Limit Hiburan Terlampaui", "Savings Up but Leisure Over Limit"),
    COMBO_NO_EMERGENCY_UNEXPECTED_EXPENSE(InsightCategory.PERINGATAN, 2, "Tanpa Dana Darurat & Muncul Biaya Mendadak", "No Emergency Fund & Unexpected Expense"),
    COMBO_GOAL_REACHED_EXPENSE_CONTROLLED(InsightCategory.APRESIASI, 6, "Target Tercapai & Pengeluaran Terkendali", "Goal Reached & Spending Controlled"),
    COMBO_IRREGULAR_INCOME_FIXED_BILLS(InsightCategory.STRATEGI, 3, "Pemasukan Fluktuatif & Tagihan Tetap", "Irregular Income & Fixed Bills");

    companion object {
        fun fromCodeOrDefault(raw: String?): FinancialConditionCode {
            if (raw.isNullOrBlank()) return BALANCE_SAFE
            return entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: BALANCE_SAFE
        }
    }
}

/**
 * Gaya komunikasi yang dipilih pengguna pada Onboarding atau Pengaturan.
 * Tidak pernah ditentukan berdasarkan gender atau usia.
 */
enum class CommunicationStylePreference(
    val labelId: String,
    val labelEn: String,
    val descId: String,
    val descEn: String
) {
    AUTO_ADAPTIVE(
        "Otomatis Menyesuaikan Kondisi",
        "Auto-Adapt to Financial Condition",
        "Menyesuaikan karakter bicara secara cerdas berdasarkan situasi dompet saat ini.",
        "Automatically adapts the tone based on your real-time financial situation."
    ),
    FUN_CASUAL(
        "Lucu dan Santai",
        "Fun & Casual",
        "Akrab, ringan, seperti ngobrol dengan kawan dekat atau roaster santai.",
        "Friendly, light-hearted, like chatting with a close friend."
    ),
    FIRM_DIRECT(
        "Tegas dan Blak-blakan",
        "Firm & Direct",
        "Jujur, apa adanya, langsung ke inti konsekuensi finansial tanpa basa-basi.",
        "Candid, straightforward, and direct about financial consequences."
    ),
    SUPPORTIVE_GENTLE(
        "Suportif dan Lembut",
        "Supportive & Gentle",
        "Penuh empati, tenang, tidak menghakimi, dan memberi semangat positif.",
        "Empathetic, calm, non-judgmental, and encouraging."
    ),
    LOGICAL_DATA(
        "Logis dan Berbasis Angka",
        "Logical & Data-Driven",
        "Objektif, fokus pada rasio, persentase, dan pola data terukur.",
        "Objective, focusing on ratios, percentages, and measurable patterns."
    ),
    STRATEGIC_SOLUTION(
        "Strategis dan Fokus Solusi",
        "Strategic & Solution-Focused",
        "Fokus pada langkah konkret, taktis, dan perencanaan jangka panjang.",
        "Focused on concrete action steps, tactics, and long-term planning."
    );

    companion object {
        fun fromNameOrDefault(raw: String?): CommunicationStylePreference {
            if (raw.isNullOrBlank()) return AUTO_ADAPTIVE
            return entries.find { it.name.equals(raw.trim(), ignoreCase = true) } ?: AUTO_ADAPTIVE
        }
    }
}

data class UserCommunicationPreferences(
    val isManualPersonaMode: Boolean = false,
    val manualPersona: InsightPersona = InsightPersona.SI_BESTIE,
    val stylePreference: CommunicationStylePreference = CommunicationStylePreference.AUTO_ADAPTIVE,
    val allowRoastingAndSarcasm: Boolean = true,
    val strictnessLevel: String = "BALANCED", // GENTLE, BALANCED, STRICT
    val sentenceLength: String = "ANY", // SHORT, DETAILED, ANY
    val focusPreference: String = "BALANCED", // MOTIVATION, SOLUTION, BALANCED
    // Optional demographic/context fields from Onboarding (NEVER used to force persona)
    val ageRange: String = "PREFER_NOT_TO_SAY", // UNDER_20, 21_25, 26_30, 31_40, OVER_40, PREFER_NOT_TO_SAY
    val activityType: String = "EMPLOYEE", // STUDENT, EMPLOYEE, FREELANCER, ENTREPRENEUR, UNEMPLOYED, OTHER
    val selfPerceivedCondition: String = "STABLE", // STABLE, ENOUGH_HARD_TO_SAVE, OFTEN_RUNS_OUT, IRREGULAR_INCOME, MANY_BILLS_DEBT, RECOVERING
    val primaryFinancialGoal: String = "CONTROL_EXPENSE" // ADD_SAVINGS, BUILD_EMERGENCY, PAY_DEBT, BUY_ITEM, CONTROL_EXPENSE, FINANCIAL_FREEDOM, OTHER
)

data class InsightPhraseRecord(
    val id: String,
    val persona: InsightPersona,
    val category: InsightCategory,
    val conditionCode: FinancialConditionCode,
    val priority: Int,
    val lengthType: String, // SHORT or DETAILED
    val requiredPlaceholders: List<String>,
    val textId: String,
    val textEn: String,
    val actionId: String,
    val actionEn: String
)

data class PhraseDatabaseValidationReport(
    val isValid: Boolean,
    val totalCount: Int,
    val uniqueIdCount: Int,
    val emptyTextCount: Int,
    val duplicateTextCount: Int,
    val invalidPersonaCount: Int,
    val invalidCategoryCount: Int,
    val invalidPlaceholderCount: Int,
    val missingConditionCount: Int,
    val countsByPersona: Map<InsightPersona, Int>,
    val validationErrors: List<String> = emptyList()
)

data class ThirtyDayHabitAnalysis(
    val hasSufficientData: Boolean,
    val analyzedTransactionCount: Int,
    val spanDays: Int,
    val topSpendingCategoryLabel: String,
    val topSpendingCategoryAmount: Long,
    val topSpendingCategorySharePercent: Int,
    val isWeekendSpendingHigher: Boolean,
    val weekendAvgPerDay: Long,
    val weekdayAvgPerDay: Long,
    val isConsistentSaverAfterIncome: Boolean,
    val isLeisureFrequentlyOverBudget: Boolean,
    val hasRapidImpulsiveBurst: Boolean,
    val isHabitImprovingVsPreviousPeriod: Boolean,
    val isHabitWorseningVsPreviousPeriod: Boolean,
    val periodComparisonDeltaPercent: Int,
    val habitSummaryFactsId: List<String>,
    val habitSummaryFactsEn: List<String>,
    val habitIndicationsId: List<String>,
    val habitIndicationsEn: List<String>
)

data class FinancialAnalysisSnapshot(
    val timestampMillis: Long,
    val hasSufficientTransactions: Boolean,
    val totalTransactionsCount: Int,
    val activeBalance: Long,
    val totalIncomePeriod: Long,
    val totalExpensePeriod: Long,
    val totalSavingsAccumulated: Long,
    val targetSavingsAmount: Long,
    val emergencyBalance: Long,
    val emergencyTarget: Long,
    val unpaidBillsTotal: Long,
    val dueSoonBillsTotal: Long,
    val overdueBillsTotal: Long,
    val daysUntilNextBill: Int,
    val topCategoryKey: String,
    val topCategoryLabel: String,
    val topCategorySpent: Long,
    val topCategoryLimit: Long,
    val overallBudgetUsagePercent: Int,
    val topCategoryUsagePercent: Int,
    val remainingBudgetAllowance: Long,
    val activeCycleDays: Int,
    val detectedConditions: List<FinancialConditionCode>,
    val primaryCondition: FinancialConditionCode,
    val highestPriorityLevel: Int,
    val thirtyDayHabits: ThirtyDayHabitAnalysis,
    val factualObservationsId: List<String>,
    val factualObservationsEn: List<String>,
    val indicationsId: List<String>,
    val indicationsEn: List<String>,
    val actionableRecommendationsId: List<String>,
    val actionableRecommendationsEn: List<String>,
    val stateSignatureHash: String
)

data class ResolvedFinancialInsight(
    val phraseId: String,
    val persona: InsightPersona,
    val category: InsightCategory,
    val condition: FinancialConditionCode,
    val priorityLevel: Int,
    val headlineBadgeId: String,
    val headlineBadgeEn: String,
    val resolvedMessageId: String,
    val resolvedMessageEn: String,
    val recommendedActionId: String,
    val recommendedActionEn: String,
    val factualBulletsId: List<String>,
    val factualBulletsEn: List<String>,
    val indicationBulletsId: List<String>,
    val indicationBulletsEn: List<String>,
    val habitAnalysis: ThirtyDayHabitAnalysis,
    val candidateIndex: Int,
    val totalRelevantCandidates: Int,
    val isLimitedDataNotice: Boolean = false
) {
    fun message(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) resolvedMessageId else resolvedMessageEn

    fun action(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) recommendedActionId else recommendedActionEn

    fun badge(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) headlineBadgeId else headlineBadgeEn

    fun facts(lang: AppLanguage): List<String> =
        if (lang == AppLanguage.INDONESIAN) factualBulletsId else factualBulletsEn

    fun indications(lang: AppLanguage): List<String> =
        if (lang == AppLanguage.INDONESIAN) indicationBulletsId else indicationBulletsEn
}
