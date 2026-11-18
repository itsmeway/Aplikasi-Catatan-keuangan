package com.example.data.offlineinsight

import com.example.data.AppLanguage
import com.example.data.CategoryLimitConfig
import com.example.data.EmergencyFundConfig
import com.example.data.MandatoryBillItem
import com.example.data.PresetCategories
import com.example.data.SavingsGoal
import com.example.data.TransactionRecord
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.ArusKasViewModel
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 2. FINANCIAL ANALYSIS ENGINE
 * Membaca dan mengolah data aktual aplikasi secara offline:
 * - Pemasukan (A), Pengeluaran (B), Saldo (C), Anggaran & Limit (D),
 *   Tabungan (E), Dana Darurat (F), Utang & Cicilan (G),
 *   Kebiasaan 30 Hari (H), dan Kondisi Gabungan (I).
 * - Memisahkan Fakta (angka terukur), Indikasi (pola), dan Saran (langkah praktis).
 * - Tidak pernah mengarang angka atau membuat kesimpulan palsu jika data belum cukup.
 */
class FinancialAnalysisEngine {

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }

    fun analyze(
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>,
        categoryLimits: Map<String, CategoryLimitConfig>,
        mandatoryBills: List<MandatoryBillItem>,
        emergencyFund: EmergencyFundConfig,
        userProfile: UserFinancialProfile,
        nowMillis: Long = System.currentTimeMillis()
    ): FinancialAnalysisSnapshot {
        val activeBalance = wallets.sumOf { it.balance }
        val window30Start = nowMillis - 30L * DAY_MS
        val prev30Start = nowMillis - 60L * DAY_MS

        val recent30Txs = transactions.filter { it.transactionDateMillis in window30Start..nowMillis }
        val prev30Txs = transactions.filter { it.transactionDateMillis in prev30Start until window30Start }

        val income30 = recent30Txs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val prevIncome30 = prev30Txs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val effectiveIncome = when {
            income30 > 0L -> income30
            userProfile.monthlyIncomeAmount > 0L -> userProfile.monthlyIncomeAmount
            else -> 0L
        }

        val expense30 = recent30Txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val prevExpense30 = prev30Txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        val totalSavings = goals.sumOf { it.savedAmount }
        val totalSavingsTarget = goals.sumOf { it.targetAmount }
        val savingsAlloc30 = recent30Txs.filter { it.type == "SAVINGS_ALLOCATION" }.sumOf { it.amount }

        // Mandatory Bills & Debt Obligations
        val unpaidBills = mandatoryBills.filter { it.status != "PAID" && it.remainingAmount > 0L }
        val unpaidBillsTotal = unpaidBills.sumOf { it.remainingAmount } + userProfile.monthlyDebtObligation

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val maxDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDaysInMonth = (maxDayInMonth - currentDayOfMonth).coerceAtLeast(1)

        var overdueBillsTotal = 0L
        var dueSoonBillsTotal = 0L
        var minDaysUntilNextBill = remainingDaysInMonth

        for (bill in unpaidBills) {
            val dayDiff = bill.dueDayOfMonth - currentDayOfMonth
            when {
                dayDiff < 0 -> overdueBillsTotal += bill.remainingAmount
                dayDiff in 0..5 -> {
                    dueSoonBillsTotal += bill.remainingAmount
                    if (dayDiff < minDaysUntilNextBill) minDaysUntilNextBill = max(1, dayDiff)
                }
                dayDiff > 5 -> {
                    if (dayDiff < minDaysUntilNextBill) minDaysUntilNextBill = dayDiff
                }
            }
        }

        // Category & Budget Limits evaluation
        val localizedCats = PresetCategories.localizedExpenseCategories(WorldCountryCurrencyRegistry.activeLanguage)
        var topCategoryKey = "kebutuhan_pokok"
        var topCategoryLabel = localizedCats.firstOrNull { it.key == "kebutuhan_pokok" }?.label ?: "Kebutuhan Harian"
        var topCategorySpent = 0L
        var topCategoryLimit = 0L
        var highestCategoryUsagePct = 0

        var totalBudgetLimit = 0L
        var totalBudgetSpent = 0L

        for ((catKey, limitCfg) in categoryLimits) {
            if (catKey == "pakai_darurat") continue
            val limitAmt = limitCfg.limitAmount
            val spentAmt = ArusKasViewModel.calculateSpentForCategoryLimit(
                categoryKey = catKey,
                period = limitCfg.period,
                transactions = transactions,
                mandatoryBills = mandatoryBills,
                startDateMillis = limitCfg.startDateMillis,
                endDateMillis = limitCfg.endDateMillis
            )
            if (limitAmt > 0L) {
                totalBudgetLimit += limitAmt
                totalBudgetSpent += spentAmt
            }
            val usagePct = if (limitAmt > 0L) ((spentAmt * 100L) / limitAmt).toInt() else 0
            if (spentAmt > topCategorySpent || (topCategorySpent == 0L && usagePct > highestCategoryUsagePct)) {
                topCategoryKey = catKey
                topCategoryLabel = limitCfg.customLabel.ifBlank {
                    localizedCats.find { it.key == catKey }?.label ?: catKey.replace('_', ' ')
                }
                topCategorySpent = spentAmt
                topCategoryLimit = limitAmt
                highestCategoryUsagePct = usagePct
            }
        }

        // If no category had spending yet, inspect raw recent30Txs by category
        if (topCategorySpent == 0L && recent30Txs.any { it.type == "EXPENSE" }) {
            val grouped = recent30Txs.filter { it.type == "EXPENSE" }
                .groupBy { it.categoryKey.ifBlank { "kebutuhan_pokok" } }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            val maxEntry = grouped.maxByOrNull { it.value }
            if (maxEntry != null) {
                topCategoryKey = maxEntry.key
                topCategorySpent = maxEntry.value
                val matchedTxLabel = recent30Txs.firstOrNull { it.categoryKey == topCategoryKey }?.categoryLabel
                topCategoryLabel = matchedTxLabel?.takeIf { it.isNotBlank() }
                    ?: localizedCats.find { it.key == topCategoryKey }?.label
                    ?: topCategoryKey
                topCategoryLimit = categoryLimits[topCategoryKey]?.limitAmount ?: 0L
                highestCategoryUsagePct = if (topCategoryLimit > 0L) {
                    ((topCategorySpent * 100L) / topCategoryLimit).toInt()
                } else 0
            }
        }

        val overallBudgetUsagePercent = when {
            totalBudgetLimit > 0L -> ((totalBudgetSpent.coerceAtLeast(expense30) * 100L) / totalBudgetLimit).toInt()
            effectiveIncome > 0L -> ((expense30 * 100L) / effectiveIncome).toInt()
            else -> 0
        }

        val effectiveBudgetCap = if (totalBudgetLimit > 0L) totalBudgetLimit else effectiveIncome
        val remainingBudgetAllowance = (effectiveBudgetCap - expense30).coerceAtLeast(0L)

        // 30-Day Habit Analysis
        val habitReport = analyzeThirtyDayHabits(
            recent30Txs = recent30Txs,
            prev30Txs = prev30Txs,
            topCategoryLabel = topCategoryLabel,
            topCategorySpent = topCategorySpent,
            expense30 = expense30,
            prevExpense30 = prevExpense30,
            savingsAlloc30 = savingsAlloc30,
            categoryLimits = categoryLimits,
            nowMillis = nowMillis
        )

        // Detect all applicable FinancialConditionCodes
        val detected = LinkedHashSet<FinancialConditionCode>()
        val hasTxData = transactions.isNotEmpty()

        // --- C. SALDO ---
        if (unpaidBillsTotal > 0L && activeBalance < unpaidBillsTotal) {
            detected.add(FinancialConditionCode.BALANCE_INSUFFICIENT_FOR_BILLS)
        }
        when {
            hasTxData && activeBalance <= 0L -> detected.add(FinancialConditionCode.BALANCE_CRITICAL)
            effectiveIncome > 0L && activeBalance < (effectiveIncome * 10L) / 100L -> detected.add(FinancialConditionCode.BALANCE_CRITICAL)
            effectiveIncome > 0L && activeBalance < (effectiveIncome * 25L) / 100L -> detected.add(FinancialConditionCode.BALANCE_LOW)
            activeBalance > 0L && expense30 > 0L && activeBalance < expense30 / 3L -> detected.add(FinancialConditionCode.BALANCE_LOW)
            activeBalance > 0L -> detected.add(FinancialConditionCode.BALANCE_SAFE)
        }

        // --- G. UTANG DAN CICILAN ---
        if (overdueBillsTotal > 0L) {
            detected.add(FinancialConditionCode.BILL_OVERDUE)
        }
        if (dueSoonBillsTotal > 0L) {
            detected.add(FinancialConditionCode.BILL_DUE_SOON)
        }
        if (effectiveIncome > 0L && unpaidBillsTotal > (effectiveIncome * 40L) / 100L) {
            detected.add(FinancialConditionCode.DEBT_HEAVY_BURDEN)
            detected.add(FinancialConditionCode.DEBT_TO_INCOME_HIGH)
        } else if (mandatoryBills.isNotEmpty() && unpaidBillsTotal == 0L) {
            detected.add(FinancialConditionCode.DEBT_CONTROLLED)
        }

        // --- D. ANGGARAN DAN LIMIT ---
        val maxPct = max(overallBudgetUsagePercent, highestCategoryUsagePct)
        when {
            maxPct > 100 -> detected.add(FinancialConditionCode.BUDGET_EXCEEDED)
            maxPct == 100 -> detected.add(FinancialConditionCode.BUDGET_EXHAUSTED)
            maxPct in 90..99 -> detected.add(FinancialConditionCode.BUDGET_ALMOST_EXHAUSTED)
            maxPct in 75..89 -> detected.add(FinancialConditionCode.BUDGET_NEAR_LIMIT)
            hasTxData && maxPct in 1..74 -> detected.add(FinancialConditionCode.BUDGET_SAFE)
        }
        if (highestCategoryUsagePct >= 80 && currentDayOfMonth <= 15) {
            detected.add(FinancialConditionCode.CATEGORY_SWELLING)
        }

        // --- B. PENGELUARAN ---
        if (effectiveIncome > 0L && expense30 > (effectiveIncome * 85L) / 100L) {
            detected.add(FinancialConditionCode.EXPENSE_EXCESSIVE)
        }
        if (expense30 > 0L && topCategorySpent > (expense30 * 55L) / 100L && recent30Txs.count { it.type == "EXPENSE" } >= 2) {
            detected.add(FinancialConditionCode.CATEGORY_OVERSPENT)
        }
        val expenseTxs = recent30Txs.filter { it.type == "EXPENSE" }
        if (expenseTxs.size >= 3) {
            val avgTx = expense30 / expenseTxs.size.coerceAtLeast(1)
            val maxSingleTx = expenseTxs.maxOfOrNull { it.amount } ?: 0L
            if (avgTx > 0L && maxSingleTx >= avgTx * 3L) {
                detected.add(FinancialConditionCode.EXPENSE_UNUSUAL_SPIKE)
            }
        }
        if (prevExpense30 > 0L) {
            when {
                expense30 > (prevExpense30 * 115L) / 100L -> detected.add(FinancialConditionCode.EXPENSE_INCREASED)
                expense30 < (prevExpense30 * 88L) / 100L -> detected.add(FinancialConditionCode.EXPENSE_DECREASED)
                else -> detected.add(FinancialConditionCode.EXPENSE_NORMAL)
            }
        } else if (expense30 > 0L && maxPct <= 75) {
            detected.add(FinancialConditionCode.EXPENSE_NORMAL)
        }

        // --- A. PEMASUKAN ---
        val incomeTxs30 = recent30Txs.filter { it.type == "INCOME" }
        if (hasTxData && income30 == 0L && userProfile.monthlyIncomeAmount == 0L) {
            detected.add(FinancialConditionCode.NO_INCOME_PERIOD)
        } else if (prevIncome30 > 0L) {
            when {
                income30 > (prevIncome30 * 110L) / 100L -> detected.add(FinancialConditionCode.INCOME_INCREASED)
                income30 < (prevIncome30 * 90L) / 100L -> detected.add(FinancialConditionCode.INCOME_DECREASED)
                else -> detected.add(FinancialConditionCode.INCOME_STABLE)
            }
        } else if (incomeTxs30.size >= 3) {
            val amounts = incomeTxs30.map { it.amount }
            val minInc = amounts.minOrNull() ?: 0L
            val maxInc = amounts.maxOrNull() ?: 0L
            if (minInc > 0L && maxInc > minInc * 2L) {
                detected.add(FinancialConditionCode.INCOME_IRREGULAR)
            } else {
                detected.add(FinancialConditionCode.INCOME_STABLE)
            }
        } else if (effectiveIncome > 0L) {
            detected.add(FinancialConditionCode.INCOME_STABLE)
        }

        // --- E. TABUNGAN ---
        if (totalSavingsTarget > 0L) {
            when {
                totalSavings >= totalSavingsTarget -> detected.add(FinancialConditionCode.SAVINGS_GOAL_REACHED)
                savingsAlloc30 > 0L -> {
                    detected.add(FinancialConditionCode.SAVINGS_INCREASED)
                    if (recent30Txs.count { it.type == "SAVINGS_ALLOCATION" } >= 2) {
                        detected.add(FinancialConditionCode.SAVINGS_CONSISTENT)
                    }
                }
                totalSavings == 0L && hasTxData -> detected.add(FinancialConditionCode.SAVINGS_STAGNANT)
                totalSavings < (totalSavingsTarget * 35L) / 100L -> detected.add(FinancialConditionCode.SAVINGS_GOAL_BEHIND)
            }
        }

        // --- F. DANA DARURAT ---
        // Peringatan/status dana darurat dikelola terpisah pada kartu Dana Darurat di tab Limit,
        // sehingga tidak ditampilkan di dalam kotak kata (SmartOfflineInsightCard).

        // --- H. KEBIASAAN 30 HARI ---
        if (habitReport.hasSufficientData) {
            if (habitReport.isWeekendSpendingHigher) detected.add(FinancialConditionCode.HABIT_WEEKEND_HEAVY)
            if (habitReport.isConsistentSaverAfterIncome) detected.add(FinancialConditionCode.HABIT_CONSISTENT_SAVER)
            if (habitReport.isLeisureFrequentlyOverBudget) detected.add(FinancialConditionCode.HABIT_LEISURE_INCREASED)
            if (habitReport.hasRapidImpulsiveBurst) detected.add(FinancialConditionCode.HABIT_IMPULSIVE_INDICATOR)
            if (habitReport.isHabitImprovingVsPreviousPeriod) detected.add(FinancialConditionCode.HABIT_IMPROVING)
            if (habitReport.isHabitWorseningVsPreviousPeriod) detected.add(FinancialConditionCode.HABIT_WORSENING)
        }

        // --- I. KONDISI GABUNGAN (COMBINED) ---
        if (detected.contains(FinancialConditionCode.INCOME_DECREASED) &&
            (detected.contains(FinancialConditionCode.EXPENSE_INCREASED) || detected.contains(FinancialConditionCode.EXPENSE_EXCESSIVE))
        ) {
            detected.add(FinancialConditionCode.COMBO_INCOME_DOWN_EXPENSE_UP)
        }
        if ((detected.contains(FinancialConditionCode.BALANCE_LOW) || detected.contains(FinancialConditionCode.BALANCE_CRITICAL)) &&
            (detected.contains(FinancialConditionCode.BILL_DUE_SOON) || detected.contains(FinancialConditionCode.BILL_OVERDUE))
        ) {
            detected.add(FinancialConditionCode.COMBO_LOW_BALANCE_BILL_DUE)
        }
        if (detected.contains(FinancialConditionCode.SAVINGS_INCREASED) &&
            (detected.contains(FinancialConditionCode.BUDGET_EXCEEDED) || detected.contains(FinancialConditionCode.HABIT_LEISURE_INCREASED))
        ) {
            detected.add(FinancialConditionCode.COMBO_SAVINGS_UP_LEISURE_OVER)
        }
        if (detected.contains(FinancialConditionCode.SAVINGS_GOAL_REACHED) &&
            (detected.contains(FinancialConditionCode.EXPENSE_NORMAL) || detected.contains(FinancialConditionCode.BUDGET_SAFE))
        ) {
            detected.add(FinancialConditionCode.COMBO_GOAL_REACHED_EXPENSE_CONTROLLED)
        }
        if (detected.contains(FinancialConditionCode.INCOME_IRREGULAR) && unpaidBillsTotal > 0L) {
            detected.add(FinancialConditionCode.COMBO_IRREGULAR_INCOME_FIXED_BILLS)
        }

        if (detected.isEmpty()) {
            detected.add(FinancialConditionCode.BALANCE_SAFE)
        }

        // Sort detected conditions by priority ascending (1 = highest urgency, 7 = general)
        // Prefer combined conditions when tied on priority
        val sortedConditions = detected.sortedWith(
            compareBy<FinancialConditionCode> { it.defaultPriority }
                .thenBy { if (it.name.startsWith("COMBO_")) 0 else 1 }
        )
        val primaryCondition = sortedConditions.first()
        val highestPriority = primaryCondition.defaultPriority

        val cCode = userProfile.countryCode.ifBlank { WorldCountryCurrencyRegistry.activeCountryCode }
        val fmtSaldo = WorldCountryCurrencyRegistry.formatMoney(activeBalance, cCode)
        val fmtIncome = WorldCountryCurrencyRegistry.formatMoney(effectiveIncome, cCode)
        val fmtExpense = WorldCountryCurrencyRegistry.formatMoney(expense30, cCode)
        val fmtSavings = WorldCountryCurrencyRegistry.formatMoney(totalSavings, cCode)
        val fmtTarget = WorldCountryCurrencyRegistry.formatMoney(totalSavingsTarget, cCode)
        val fmtBills = WorldCountryCurrencyRegistry.formatMoney(unpaidBillsTotal, cCode)
        val fmtRemaining = WorldCountryCurrencyRegistry.formatMoney(remainingBudgetAllowance, cCode)

        // Build Factual Observations, Indications, and Recommendations
        val factsId = mutableListOf<String>()
        val factsEn = mutableListOf<String>()
        factsId.add("Saldo aktif: $fmtSaldo | Pemasukan 30 hari: $fmtIncome | Pengeluaran 30 hari: $fmtExpense.")
        factsEn.add("Active balance: $fmtSaldo | 30-day income: $fmtIncome | 30-day expenses: $fmtExpense.")

        if (topCategorySpent > 0L) {
            val fmtTop = WorldCountryCurrencyRegistry.formatMoney(topCategorySpent, cCode)
            factsId.add("Kategori terbesar: $topCategoryLabel ($fmtTop • ${max(highestCategoryUsagePct, overallBudgetUsagePercent)}% anggaran).")
            factsEn.add("Largest category: $topCategoryLabel ($fmtTop • ${max(highestCategoryUsagePct, overallBudgetUsagePercent)}% of budget).")
        }
        if (unpaidBillsTotal > 0L) {
            factsId.add("Kewajiban/tagihan belum lunas: $fmtBills (jatuh tempo terdekat dalam $minDaysUntilNextBill hari).")
            factsEn.add("Unpaid bill obligations: $fmtBills (nearest due in $minDaysUntilNextBill days).")
        }
        if (totalSavingsTarget > 0L) {
            factsId.add("Progres tabungan: $fmtSavings terkumpul dari target $fmtTarget.")
            factsEn.add("Savings progress: $fmtSavings saved out of $fmtTarget target.")
        }

        val indicationsId = mutableListOf<String>()
        val indicationsEn = mutableListOf<String>()
        if (!hasTxData && activeBalance == 0L) {
            indicationsId.add("Indikasi: Data transaksi belum tercatat (pengguna baru/mulai dari nol). Analisis kebiasaan 30 hari akan semakin akurat seiring bertambahnya catatan transaksi.")
            indicationsEn.add("Indication: No transactions recorded yet (fresh start). 30-day habit analysis will sharpen as you log daily transactions.")
        } else {
            indicationsId.addAll(habitReport.habitIndicationsId)
            indicationsEn.addAll(habitReport.habitIndicationsEn)
        }

        val recsId = mutableListOf<String>()
        val recsEn = mutableListOf<String>()
        if (remainingBudgetAllowance > 0L) {
            val dailySafe = WorldCountryCurrencyRegistry.formatMoney(remainingBudgetAllowance / remainingDaysInMonth.coerceAtLeast(1), cCode)
            recsId.add("Batas aman belanja harian untuk $remainingDaysInMonth hari ke depan adalah $dailySafe/hari dari sisa limit $fmtRemaining.")
            recsEn.add("Safe daily spending allowance for the next $remainingDaysInMonth days is $dailySafe/day from your $fmtRemaining remaining limit.")
        }
        if (unpaidBillsTotal > 0L) {
            recsId.add("Sisihkan $fmtBills lebih dulu dari saldo $fmtSaldo agar tagihan wajib tidak terganggu belanja fleksibel.")
            recsEn.add("Set aside $fmtBills first from your $fmtSaldo balance so mandatory bills stay protected.")
        }

        val signatureHash = listOf(
            activeBalance,
            effectiveIncome,
            expense30,
            totalSavings,
            totalSavingsTarget,
            emergencyFund.emergencyBalance,
            unpaidBillsTotal,
            topCategoryKey,
            topCategorySpent,
            primaryCondition.name,
            transactions.size
        ).joinToString("|")

        return FinancialAnalysisSnapshot(
            timestampMillis = nowMillis,
            hasSufficientTransactions = transactions.size >= 3,
            totalTransactionsCount = transactions.size,
            activeBalance = activeBalance,
            totalIncomePeriod = effectiveIncome,
            totalExpensePeriod = expense30,
            totalSavingsAccumulated = totalSavings,
            targetSavingsAmount = totalSavingsTarget,
            emergencyBalance = emergencyFund.emergencyBalance,
            emergencyTarget = emergencyFund.emergencyLimit,
            unpaidBillsTotal = unpaidBillsTotal,
            dueSoonBillsTotal = dueSoonBillsTotal,
            overdueBillsTotal = overdueBillsTotal,
            daysUntilNextBill = minDaysUntilNextBill.coerceAtLeast(1),
            topCategoryKey = topCategoryKey,
            topCategoryLabel = topCategoryLabel,
            topCategorySpent = topCategorySpent,
            topCategoryLimit = topCategoryLimit,
            overallBudgetUsagePercent = max(overallBudgetUsagePercent, highestCategoryUsagePct),
            topCategoryUsagePercent = highestCategoryUsagePct,
            remainingBudgetAllowance = remainingBudgetAllowance,
            activeCycleDays = remainingDaysInMonth.coerceAtLeast(1),
            detectedConditions = sortedConditions,
            primaryCondition = primaryCondition,
            highestPriorityLevel = highestPriority,
            thirtyDayHabits = habitReport,
            factualObservationsId = factsId,
            factualObservationsEn = factsEn,
            indicationsId = indicationsId,
            indicationsEn = indicationsEn,
            actionableRecommendationsId = recsId,
            actionableRecommendationsEn = recsEn,
            stateSignatureHash = signatureHash
        )
    }

    private fun analyzeThirtyDayHabits(
        recent30Txs: List<TransactionRecord>,
        prev30Txs: List<TransactionRecord>,
        topCategoryLabel: String,
        topCategorySpent: Long,
        expense30: Long,
        prevExpense30: Long,
        savingsAlloc30: Long,
        categoryLimits: Map<String, CategoryLimitConfig>,
        nowMillis: Long
    ): ThirtyDayHabitAnalysis {
        val expenseTxs = recent30Txs.filter { it.type == "EXPENSE" }.sortedBy { it.transactionDateMillis }
        val txCount = recent30Txs.size
        val hasEnough = expenseTxs.size >= 4

        val earliestMillis = recent30Txs.minOfOrNull { it.transactionDateMillis } ?: nowMillis
        val spanDays = (((nowMillis - earliestMillis) / DAY_MS).toInt() + 1).coerceIn(1, 30)

        val topSharePct = if (expense30 > 0L) ((topCategorySpent * 100L) / expense30).toInt() else 0

        // Weekend vs Weekday average spending
        var weekendTotal = 0L
        var weekdayTotal = 0L
        val cal = Calendar.getInstance()
        for (tx in expenseTxs) {
            cal.timeInMillis = tx.transactionDateMillis
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                weekendTotal += tx.amount
            } else {
                weekdayTotal += tx.amount
            }
        }
        val weekendDays = max(1, (spanDays * 2) / 7)
        val weekdayDays = max(1, spanDays - weekendDays)
        val weekendAvg = weekendTotal / weekendDays
        val weekdayAvg = weekdayTotal / weekdayDays
        val isWeekendHeavy = hasEnough && weekendAvg > (weekdayAvg * 125L) / 100L && weekendTotal > 0L

        // Consistent saver after income
        val hasIncomeAndSavings = savingsAlloc30 > 0L && recent30Txs.count { it.type == "SAVINGS_ALLOCATION" } >= 1

        // Leisure over budget
        val leisureSpent = expenseTxs.filter { it.categoryKey == "gaya_hidup" }.sumOf { it.amount }
        val leisureLimit = categoryLimits["gaya_hidup"]?.limitAmount ?: 0L
        val isLeisureOver = (leisureLimit > 0L && leisureSpent > leisureLimit) ||
            (expense30 > 0L && leisureSpent > (expense30 * 35L) / 100L && expenseTxs.size >= 4)

        // Rapid back-to-back impulsive burst indicator (>= 3 expense transactions within 4 hours)
        var hasRapidBurst = false
        if (expenseTxs.size >= 3) {
            for (i in 0..(expenseTxs.size - 3)) {
                val span = expenseTxs[i + 2].transactionDateMillis - expenseTxs[i].transactionDateMillis
                if (span in 0..(4L * 60L * 60L * 1000L)) {
                    hasRapidBurst = true
                    break
                }
            }
        }

        // Fair period comparison vs previous 30-day window
        val deltaPct = if (prevExpense30 > 0L) {
            (((expense30 - prevExpense30).toDouble() / prevExpense30.toDouble()) * 100.0).roundToInt()
        } else 0
        val isImproving = prevExpense30 > 0L && deltaPct <= -10
        val isWorsening = prevExpense30 > 0L && deltaPct >= 15

        val factsId = mutableListOf<String>()
        val factsEn = mutableListOf<String>()
        val indId = mutableListOf<String>()
        val indEn = mutableListOf<String>()

        if (!hasEnough) {
            indId.add("Indikasi Awal ($txCount transaksi tercatat): Pola kebiasaan 30 hari masih bersifat indikatif dan membutuhkan minimal 4-5 transaksi untuk kesimpulan pola yang kuat.")
            indEn.add("Early Indication ($txCount logged transactions): 30-day habit patterns are preliminary and require at least 4-5 transactions for strong conclusions.")
        } else {
            factsId.add("Selama $spanDays hari terakhir tercatat $txCount transaksi, dengan porsi terbesar pada $topCategoryLabel ($topSharePct%).")
            factsEn.add("Over the last $spanDays days, $txCount transactions were logged, led by $topCategoryLabel ($topSharePct%).")

            if (isWeekendHeavy) {
                indId.add("Pola Akhir Pekan: Rata-rata pengeluaran Sabtu-Minggu tercatat lebih tinggi dibanding hari kerja.")
                indEn.add("Weekend Pattern: Average Saturday-Sunday spending is noticeably higher than weekdays.")
            }
            if (hasRapidBurst) {
                indId.add("Indikasi Belanja Beruntun: Terdeteksi 3+ transaksi pengeluaran dalam rentang kurang dari 4 jam.")
                indEn.add("Rapid Spending Indicator: Detected 3+ expense transactions within a 4-hour window.")
            }
            if (hasIncomeAndSavings) {
                indId.add("Kebiasaan Positif: Tercatat alokasi tabungan aktif pada periode berjalan.")
                indEn.add("Positive Habit: Active savings allocations recorded during the current period.")
            }
            if (isImproving) {
                indId.add("Tren Membaik: Pengeluaran turun ${abs(deltaPct)}% dibanding periode 30 hari sebelumnya.")
                indEn.add("Improving Trend: Spending decreased by ${abs(deltaPct)}% compared to the prior 30-day period.")
            } else if (isWorsening) {
                indId.add("Tren Meningkat: Pengeluaran naik $deltaPct% dibanding periode 30 hari sebelumnya.")
                indEn.add("Accelerating Trend: Spending rose by $deltaPct% compared to the prior 30-day period.")
            }
        }

        return ThirtyDayHabitAnalysis(
            hasSufficientData = hasEnough,
            analyzedTransactionCount = txCount,
            spanDays = spanDays,
            topSpendingCategoryLabel = topCategoryLabel,
            topSpendingCategoryAmount = topCategorySpent,
            topSpendingCategorySharePercent = topSharePct,
            isWeekendSpendingHigher = isWeekendHeavy,
            weekendAvgPerDay = weekendAvg,
            weekdayAvgPerDay = weekdayAvg,
            isConsistentSaverAfterIncome = hasIncomeAndSavings,
            isLeisureFrequentlyOverBudget = isLeisureOver,
            hasRapidImpulsiveBurst = hasRapidBurst,
            isHabitImprovingVsPreviousPeriod = isImproving,
            isHabitWorseningVsPreviousPeriod = isWorsening,
            periodComparisonDeltaPercent = deltaPct,
            habitSummaryFactsId = factsId,
            habitSummaryFactsEn = factsEn,
            habitIndicationsId = indId,
            habitIndicationsEn = indEn
        )
    }
}

/**
 * 4. RESPONSE ENGINE
 * Mencari dan memilih kalimat yang cocok dari database 10.000 kalimat:
 * - Memprioritaskan peringatan penting (Prioritas 1-2) di atas motivasi umum.
 * - Menggunakan query SQLite terindeks (tidak pernah memuat 10.000 kalimat sekaligus ke RAM).
 * - Menerapkan anti-repetition (riwayat penggunaan + masa jeda cooldown + rotasi kandidat).
 * - Mengisi seluruh placeholder dinamis dengan data valid dan memvalidasi bahwa
 *   TIDAK ADA placeholder mentah `{...}` yang tampil ke pengguna.
 */
class ResponseEngine(
    private val dbHelper: OfflineInsightDatabaseHelper,
    private val onboardingEngine: OnboardingEngine = OnboardingEngine(dbHelper),
    private val analysisEngine: FinancialAnalysisEngine = FinancialAnalysisEngine(),
    private val personalityEngine: PersonalityEngine = PersonalityEngine()
) {
    companion object {
        // Masa jeda 6 jam sebelum kalimat yang sama diprioritaskan kembali
        private const val COOLDOWN_WINDOW_MS = 6L * 60L * 60L * 1000L
        private val ANY_BRACE_PLACEHOLDER = Regex("\\{[a-zA-Z0-9_]+\\}")

        // Kondisi Dana Darurat tidak boleh muncul di kotak kata (SmartOfflineInsightCard)
        val EXCLUDED_PHRASE_BOX_CONDITIONS: Set<FinancialConditionCode> = setOf(
            FinancialConditionCode.EMERGENCY_NONE,
            FinancialConditionCode.EMERGENCY_INSUFFICIENT,
            FinancialConditionCode.EMERGENCY_SUFFICIENT,
            FinancialConditionCode.EMERGENCY_USED,
            FinancialConditionCode.EMERGENCY_REBUILD_NEEDED,
            FinancialConditionCode.COMBO_NO_EMERGENCY_UNEXPECTED_EXPENSE
        )
    }

    // Menyimpan frase yang baru saja ditampilkan di sesi aktif agar tombol "Ganti Kalimat" selalu segar
    private val sessionShownIds = LinkedHashSet<String>()

    fun generateInsight(
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>,
        categoryLimits: Map<String, CategoryLimitConfig>,
        mandatoryBills: List<MandatoryBillItem>,
        emergencyFund: EmergencyFundConfig,
        userProfile: UserFinancialProfile,
        preferencesOverride: UserCommunicationPreferences? = null,
        variationOffset: Int = 0,
        recordUsageInDb: Boolean = true,
        nowMillis: Long = System.currentTimeMillis()
    ): ResolvedFinancialInsight {
        val prefs = preferencesOverride ?: onboardingEngine.loadPreferences()
        val snapshot = analysisEngine.analyze(
            wallets = wallets,
            transactions = transactions,
            goals = goals,
            categoryLimits = categoryLimits,
            mandatoryBills = mandatoryBills,
            emergencyFund = emergencyFund,
            userProfile = userProfile,
            nowMillis = nowMillis
        )

        val allowedDetectedConditions = snapshot.detectedConditions
            .filterNot { it in EXCLUDED_PHRASE_BOX_CONDITIONS }
            .ifEmpty { listOf(FinancialConditionCode.BALANCE_SAFE) }
        val effectivePrimaryCondition = allowedDetectedConditions.first()

        val activePersona = personalityEngine.resolveActivePersona(
            prefs = prefs,
            primaryCondition = effectivePrimaryCondition,
            priorityLevel = effectivePrimaryCondition.defaultPriority
        )

        val cooldownCutoff = nowMillis - COOLDOWN_WINDOW_MS
        var candidates = dbHelper.queryCandidatePhrases(
            persona = activePersona,
            conditionCodes = allowedDetectedConditions,
            preferredLength = prefs.sentenceLength,
            cooldownCutoffMillis = cooldownCutoff,
            limit = 30
        )

        if (candidates.isEmpty()) {
            // Fallback query across all non-emergency conditions for the active persona
            val allowedFallbackConditions = FinancialConditionCode.entries
                .filterNot { it in EXCLUDED_PHRASE_BOX_CONDITIONS }
            candidates = dbHelper.queryCandidatePhrases(
                persona = activePersona,
                conditionCodes = allowedFallbackConditions,
                preferredLength = "ANY",
                cooldownCutoffMillis = cooldownCutoff,
                limit = 20
            )
        }

        // Pilih kandidat dengan mempertimbangkan anti-repetition sesi & offset tombol "Ganti Insight"
        val unshownCandidates = candidates.filter { it.id !in sessionShownIds }
        val pool = if (unshownCandidates.isNotEmpty()) unshownCandidates else candidates
        val safeIndex = if (pool.isNotEmpty()) {
            abs(variationOffset) % pool.size
        } else 0

        val chosen = pool.getOrNull(safeIndex) ?: createEmergencyFallbackRecord(activePersona, effectivePrimaryCondition)

        if (recordUsageInDb) {
            sessionShownIds.add(chosen.id)
            if (sessionShownIds.size > 200) {
                val first = sessionShownIds.first()
                sessionShownIds.remove(first)
            }
            dbHelper.recordPhraseUsage(chosen.id, chosen.conditionCode, nowMillis)
        }

        val cCode = userProfile.countryCode.ifBlank { WorldCountryCurrencyRegistry.activeCountryCode }
        val resolvedTextId = resolvePlaceholdersSafely(
            template = chosen.textId,
            snapshot = snapshot,
            countryCode = cCode,
            lang = AppLanguage.INDONESIAN
        )
        val resolvedTextEn = resolvePlaceholdersSafely(
            template = chosen.textEn,
            snapshot = snapshot,
            countryCode = cCode,
            lang = AppLanguage.ENGLISH
        )
        val resolvedActionId = resolvePlaceholdersSafely(
            template = chosen.actionId,
            snapshot = snapshot,
            countryCode = cCode,
            lang = AppLanguage.INDONESIAN
        )
        val resolvedActionEn = resolvePlaceholdersSafely(
            template = chosen.actionEn,
            snapshot = snapshot,
            countryCode = cCode,
            lang = AppLanguage.ENGLISH
        )

        val isStarterZeroData = transactions.isEmpty() && snapshot.activeBalance == 0L
        val finalMsgId = if (isStarterZeroData) {
            "Catatan awal masih kosong (Saldo ${WorldCountryCurrencyRegistry.formatMoney(0L, cCode)}). $resolvedTextId"
        } else {
            resolvedTextId
        }
        val finalMsgEn = if (isStarterZeroData) {
            "Your ledger is currently fresh (Balance ${WorldCountryCurrencyRegistry.formatMoney(0L, cCode)}). $resolvedTextEn"
        } else {
            resolvedTextEn
        }

        return ResolvedFinancialInsight(
            phraseId = chosen.id,
            persona = chosen.persona,
            category = chosen.category,
            condition = chosen.conditionCode,
            priorityLevel = chosen.priority,
            headlineBadgeId = "${chosen.category.labelId} • ${chosen.conditionCode.titleId}",
            headlineBadgeEn = "${chosen.category.labelEn} • ${chosen.conditionCode.titleEn}",
            resolvedMessageId = finalMsgId,
            resolvedMessageEn = finalMsgEn,
            recommendedActionId = resolvedActionId,
            recommendedActionEn = resolvedActionEn,
            factualBulletsId = snapshot.factualObservationsId,
            factualBulletsEn = snapshot.factualObservationsEn,
            indicationBulletsId = snapshot.indicationsId,
            indicationBulletsEn = snapshot.indicationsEn,
            habitAnalysis = snapshot.thirtyDayHabits,
            candidateIndex = safeIndex + 1,
            totalRelevantCandidates = candidates.size.coerceAtLeast(1),
            isLimitedDataNotice = !snapshot.hasSufficientTransactions
        )
    }

    /**
     * Mengisi seluruh placeholder dinamis menggunakan angka aktual dan memastikan
     * TIDAK PERNAH ada placeholder mentah `{...}` yang lolos ke layar pengguna.
     */
    fun resolvePlaceholdersSafely(
        template: String,
        snapshot: FinancialAnalysisSnapshot,
        countryCode: String,
        lang: AppLanguage
    ): String {
        val fmtSaldo = WorldCountryCurrencyRegistry.formatMoney(snapshot.activeBalance, countryCode)
        val fmtIncome = WorldCountryCurrencyRegistry.formatMoney(snapshot.totalIncomePeriod, countryCode)
        val fmtExpense = WorldCountryCurrencyRegistry.formatMoney(snapshot.totalExpensePeriod, countryCode)
        val fmtRemainingLimit = WorldCountryCurrencyRegistry.formatMoney(snapshot.remainingBudgetAllowance, countryCode)
        val fmtTargetSavings = WorldCountryCurrencyRegistry.formatMoney(snapshot.targetSavingsAmount, countryCode)
        val fmtSavedAmount = WorldCountryCurrencyRegistry.formatMoney(snapshot.totalSavingsAccumulated, countryCode)
        val fmtBillsAmount = WorldCountryCurrencyRegistry.formatMoney(snapshot.unpaidBillsTotal, countryCode)

        val defaultCatName = if (lang == AppLanguage.INDONESIAN) "Kebutuhan Harian" else "Daily Essentials"
        val cleanCategory = snapshot.topCategoryLabel.ifBlank { defaultCatName }
        val pctStr = snapshot.overallBudgetUsagePercent.coerceAtLeast(0).toString()
        val daysStr = snapshot.activeCycleDays.coerceAtLeast(1).toString()

        var result = template
            .replace("{saldo}", fmtSaldo)
            .replace("{total_pemasukan}", fmtIncome)
            .replace("{total_pengeluaran}", fmtExpense)
            .replace("{nama_kategori}", cleanCategory)
            .replace("{persentase_anggaran}", pctStr)
            .replace("{sisa_limit}", fmtRemainingLimit)
            .replace("{target_tabungan}", fmtTargetSavings)
            .replace("{jumlah_tabungan}", fmtSavedAmount)
            .replace("{jumlah_hari}", daysStr)
            .replace("{jumlah_tagihan}", fmtBillsAmount)

        // Safety net: pastikan tidak ada token {apapun} yang tersisa mentah
        if (result.contains('{')) {
            result = ANY_BRACE_PLACEHOLDER.replace(result, fmtSaldo)
        }
        return result
    }

    private fun createEmergencyFallbackRecord(
        persona: InsightPersona,
        condition: FinancialConditionCode
    ): InsightPhraseRecord {
        return InsightPhraseRecord(
            id = "${persona.codePrefix}_0001",
            persona = persona,
            category = condition.defaultCategory,
            conditionCode = condition,
            priority = condition.defaultPriority,
            lengthType = "SHORT",
            requiredPlaceholders = listOf("{saldo}", "{total_pengeluaran}"),
            textId = "Saldo aktif tercatat {saldo} dengan total pengeluaran {total_pengeluaran}, mari jaga ritme anggaran tetap terkendali.",
            textEn = "Active balance stands at {saldo} with total expenses of {total_pengeluaran}; let's keep your budget pace steady.",
            actionId = "Pertahankan sisa limit {sisa_limit} untuk {jumlah_hari} hari ke depan.",
            actionEn = "Preserve your {sisa_limit} remaining limit for the next {jumlah_hari} days."
        )
    }
}
