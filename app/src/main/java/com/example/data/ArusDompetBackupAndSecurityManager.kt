package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.data.offlineinsight.CommunicationStylePreference
import com.example.data.offlineinsight.InsightPersona
import com.example.data.offlineinsight.UserCommunicationPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedArusDompetBackup(
    val schemaVersion: Int,
    val appName: String,
    val appVersionName: String,
    val createdAtMillis: Long,
    val sha256Checksum: String,
    val userProfile: UserFinancialProfile,
    val appSettings: AppSettings,
    val wallets: List<Wallet>,
    val transactions: List<TransactionRecord>,
    val goals: List<SavingsGoal>,
    val mandatoryBills: List<MandatoryBillItem>,
    val categoryLimits: Map<String, CategoryLimitConfig>,
    val emergencyFund: EmergencyFundConfig,
    val communicationPreferences: UserCommunicationPreferences?
)

sealed interface BackupValidationResult {
    data class Valid(val backup: ParsedArusDompetBackup) : BackupValidationResult
    data class Invalid(val reasonId: String, val reasonEn: String) : BackupValidationResult
}

data class StorageBreakdownInfo(
    val financialDataBytes: Long,
    val offlineEngineDbBytes: Long,
    val temporaryCacheBytes: Long
) {
    val totalBytes: Long
        get() = financialDataBytes + offlineEngineDbBytes + temporaryCacheBytes

    fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024L -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

/**
 * Full Backup & Restore Manager with Schema Versioning, SHA-256 Checksum Integrity,
 * and Duplicate-Safe Restoration for Arus Dompet (ArusKas).
 */
object ArusDompetBackupManager {

    const val CURRENT_SCHEMA_VERSION = 1
    const val APP_IDENTIFIER = "Arus Dompet"
    private const val MAX_MONETARY_AMOUNT = 1_000_000_000_000L

    fun createFullBackupJson(
        userProfile: UserFinancialProfile,
        appSettings: AppSettings,
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>,
        mandatoryBills: List<MandatoryBillItem>,
        categoryLimits: Map<String, CategoryLimitConfig>,
        emergencyFund: EmergencyFundConfig,
        communicationPreferences: UserCommunicationPreferences? = null,
        createdAtMillis: Long = System.currentTimeMillis()
    ): String {
        val payloadObj = buildPayloadJsonObject(
            userProfile = userProfile,
            appSettings = appSettings,
            wallets = wallets,
            transactions = transactions,
            goals = goals,
            mandatoryBills = mandatoryBills,
            categoryLimits = categoryLimits,
            emergencyFund = emergencyFund,
            communicationPreferences = communicationPreferences
        )

        val canonicalPayloadString = buildCanonicalDigestSource(
            wallets = wallets,
            transactions = transactions,
            goals = goals,
            mandatoryBills = mandatoryBills,
            emergencyFund = emergencyFund
        )
        val checksum = sha256Hex(canonicalPayloadString)

        val root = JSONObject().apply {
            put("appName", APP_IDENTIFIER)
            put("packageName", "com.aistudio.aruskas.vqmzpx")
            put("schemaVersion", CURRENT_SCHEMA_VERSION)
            put("appVersionName", "1.0")
            put("createdAtMillis", createdAtMillis)
            put("sha256Checksum", checksum)
            put("payload", payloadObj)
        }
        return root.toString(2)
    }

    fun validateAndParseBackupJson(rawJson: String): BackupValidationResult {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return BackupValidationResult.Invalid(
                reasonId = "File atau teks backup kosong.",
                reasonEn = "Backup file or text is empty."
            )
        }
        return try {
            val root = JSONObject(trimmed)
            val schemaVersion = root.optInt("schemaVersion", -1)
            if (schemaVersion <= 0 || schemaVersion > CURRENT_SCHEMA_VERSION) {
                return BackupValidationResult.Invalid(
                    reasonId = "Versi skema backup tidak didukung (versi: $schemaVersion).",
                    reasonEn = "Unsupported backup schema version (version: $schemaVersion)."
                )
            }
            val payload = root.optJSONObject("payload")
                ?: return BackupValidationResult.Invalid(
                    reasonId = "Struktur file backup tidak valid: objek 'payload' tidak ditemukan.",
                    reasonEn = "Invalid backup structure: 'payload' object is missing."
                )

            val walletsArr = payload.optJSONArray("wallets")
                ?: return BackupValidationResult.Invalid(
                    reasonId = "Data dompet ('wallets') tidak ditemukan di dalam file backup.",
                    reasonEn = "Wallets array is missing inside backup file."
                )
            val transactionsArr = payload.optJSONArray("transactions") ?: JSONArray()
            val goalsArr = payload.optJSONArray("goals") ?: JSONArray()
            val billsArr = payload.optJSONArray("mandatoryBills") ?: JSONArray()
            val limitsArr = payload.optJSONArray("categoryLimits") ?: JSONArray()

            val parsedWallets = mutableListOf<Wallet>()
            for (i in 0 until walletsArr.length()) {
                val obj = walletsArr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").trim()
                if (id.isEmpty()) continue
                parsedWallets.add(
                    Wallet(
                        id = id,
                        userId = obj.optString("userId", "local_preview"),
                        name = obj.optString("name", "Wallet").trim().ifBlank { "Wallet" },
                        walletType = obj.optString("walletType", "BANK").ifBlank { "BANK" },
                        balance = obj.optLong("balance", 0L).coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT),
                        colorHex = obj.optString("colorHex", "#0066AE"),
                        iconKey = obj.optString("iconKey", "bank_bca")
                    )
                )
            }

            if (parsedWallets.isEmpty()) {
                return BackupValidationResult.Invalid(
                    reasonId = "File backup tidak memiliki data dompet yang valid.",
                    reasonEn = "Backup file contains no valid wallet entries."
                )
            }

            val parsedTransactions = mutableListOf<TransactionRecord>()
            for (i in 0 until transactionsArr.length()) {
                val obj = transactionsArr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").trim()
                if (id.isEmpty()) continue
                val amt = obj.optLong("amount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT)
                if (amt <= 0L) continue
                parsedTransactions.add(
                    TransactionRecord(
                        id = id,
                        userId = obj.optString("userId", "local_preview"),
                        type = obj.optString("type", "EXPENSE").ifBlank { "EXPENSE" },
                        amount = amt,
                        categoryKey = obj.optString("categoryKey", "kebutuhan_pokok"),
                        categoryLabel = obj.optString("categoryLabel", "Pengeluaran"),
                        frequencyTag = obj.optString("frequencyTag", "HARIAN"),
                        walletId = obj.optString("walletId", ""),
                        walletName = obj.optString("walletName", ""),
                        note = obj.optString("note", ""),
                        transactionDateMillis = obj.optLong("transactionDateMillis", System.currentTimeMillis()).coerceAtLeast(1L)
                    )
                )
            }

            val parsedGoals = mutableListOf<SavingsGoal>()
            for (i in 0 until goalsArr.length()) {
                val obj = goalsArr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").trim()
                if (id.isEmpty()) continue
                parsedGoals.add(
                    SavingsGoal(
                        id = id,
                        userId = obj.optString("userId", "local_preview"),
                        title = obj.optString("title", "Target Impian").trim().ifBlank { "Target Impian" },
                        targetAmount = obj.optLong("targetAmount", 1_000_000L).coerceIn(1L, MAX_MONETARY_AMOUNT),
                        savedAmount = obj.optLong("savedAmount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                        dailyRecommendation = obj.optLong("dailyRecommendation", 20_000L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                        note = obj.optString("note", ""),
                        iconKey = obj.optString("iconKey", "laptop"),
                        startDateMillis = obj.optLong("startDateMillis", System.currentTimeMillis()).coerceAtLeast(1L)
                    )
                )
            }

            val parsedBills = mutableListOf<MandatoryBillItem>()
            for (i in 0 until billsArr.length()) {
                val obj = billsArr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "").trim()
                if (id.isEmpty()) continue
                val hasRenewal = obj.has("renewalDayOfMonth") && !obj.isNull("renewalDayOfMonth")
                val safeTotal = obj.optLong("totalAmount", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT)
                parsedBills.add(
                    MandatoryBillItem(
                        id = id,
                        name = obj.optString("name", "Tagihan").trim().ifBlank { "Tagihan" },
                        totalAmount = safeTotal,
                        paidAmount = obj.optLong("paidAmount", 0L).coerceIn(0L, safeTotal),
                        dueScheduleLabel = obj.optString("dueScheduleLabel", ""),
                        dueDayOfMonth = obj.optInt("dueDayOfMonth", 1).coerceIn(1, 31),
                        renewalDayOfMonth = if (hasRenewal) obj.optInt("renewalDayOfMonth").coerceIn(1, 31) else null,
                        status = obj.optString("status", "UNPAID"),
                        billType = obj.optString("billType", "KOSAN")
                    )
                )
            }

            val parsedLimits = mutableMapOf<String, CategoryLimitConfig>()
            for (i in 0 until limitsArr.length()) {
                val obj = limitsArr.optJSONObject(i) ?: continue
                val key = obj.optString("categoryKey", "").trim()
                if (key.isEmpty()) continue
                parsedLimits[key] = CategoryLimitConfig(
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

            val emObj = payload.optJSONObject("emergencyFund")
            val parsedEmergency = if (emObj != null) {
                EmergencyFundConfig(
                    emergencyBalance = emObj.optLong("emergencyBalance", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                    emergencyLimit = emObj.optLong("emergencyLimit", 0L).coerceIn(0L, MAX_MONETARY_AMOUNT),
                    limitPeriod = emObj.optString("limitPeriod", "BULANAN"),
                    storagePlaceNote = emObj.optString("storagePlaceNote", "")
                )
            } else {
                EmergencyFundConfig()
            }

            val expectedChecksum = root.optString("sha256Checksum", "")
            val actualCanonical = buildCanonicalDigestSource(
                wallets = parsedWallets,
                transactions = parsedTransactions,
                goals = parsedGoals,
                mandatoryBills = parsedBills,
                emergencyFund = parsedEmergency
            )
            val actualChecksum = sha256Hex(actualCanonical)
            if (expectedChecksum.isNotBlank() && !expectedChecksum.equals(actualChecksum, ignoreCase = true)) {
                return BackupValidationResult.Invalid(
                    reasonId = "Checksum integritas SHA-256 tidak cocok. File backup mungkin rusak atau telah dimodifikasi.",
                    reasonEn = "SHA-256 integrity checksum mismatch. The backup file may be corrupted or modified."
                )
            }

            val parsedProfile = parseUserProfile(payload.optJSONObject("userProfile"))
            val parsedSettings = parseAppSettings(payload.optJSONObject("appSettings"), parsedProfile)
            val parsedCommPrefs = parseCommunicationPrefs(payload.optJSONObject("communicationPreferences"))

            BackupValidationResult.Valid(
                ParsedArusDompetBackup(
                    schemaVersion = schemaVersion,
                    appName = root.optString("appName", APP_IDENTIFIER),
                    appVersionName = root.optString("appVersionName", "1.0"),
                    createdAtMillis = root.optLong("createdAtMillis", System.currentTimeMillis()),
                    sha256Checksum = actualChecksum,
                    userProfile = parsedProfile,
                    appSettings = parsedSettings,
                    wallets = parsedWallets,
                    transactions = parsedTransactions,
                    goals = parsedGoals,
                    mandatoryBills = parsedBills,
                    categoryLimits = parsedLimits,
                    emergencyFund = parsedEmergency,
                    communicationPreferences = parsedCommPrefs
                )
            )
        } catch (e: Exception) {
            BackupValidationResult.Invalid(
                reasonId = "Format JSON backup rusak atau tidak dapat dibaca: ${e.localizedMessage ?: "Invalid JSON"}",
                reasonEn = "Corrupted or unreadable backup JSON format: ${e.localizedMessage ?: "Invalid JSON"}"
            )
        }
    }

    fun exportBackupToCacheFile(context: Context, backupJson: String): File {
        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "ArusDompet_Backup_$dateStamp.json")
        file.writeText(backupJson, StandardCharsets.UTF_8)
        return file
    }

    fun shareBackupFile(context: Context, file: File, lang: AppLanguage) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val isId = lang == AppLanguage.INDONESIAN
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                if (isId) "Cadangan Data Lengkap Arus Dompet (${file.name})"
                else "Arus Dompet Full Data Backup (${file.name})"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(
            shareIntent,
            if (isId) "Simpan / Bagikan File Backup (.json)" else "Save / Share Backup File (.json)"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun writeBackupToUri(context: Context, uri: Uri, backupJson: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(backupJson.toByteArray(StandardCharsets.UTF_8))
                out.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readBackupFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use {
                it.readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildCanonicalDigestSource(
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>,
        mandatoryBills: List<MandatoryBillItem>,
        emergencyFund: EmergencyFundConfig
    ): String {
        val sb = StringBuilder()
        sb.append("W:")
        wallets.sortedBy { it.id }.forEach { w ->
            sb.append("${w.id}=${w.balance};")
        }
        sb.append("|T:")
        transactions.sortedBy { it.id }.forEach { tx ->
            sb.append("${tx.id}:${tx.type}:${tx.amount}:${tx.walletId};")
        }
        sb.append("|G:")
        goals.sortedBy { it.id }.forEach { g ->
            sb.append("${g.id}:${g.targetAmount}:${g.savedAmount};")
        }
        sb.append("|B:")
        mandatoryBills.sortedBy { it.id }.forEach { b ->
            sb.append("${b.id}:${b.totalAmount}:${b.paidAmount}:${b.status};")
        }
        sb.append("|E:${emergencyFund.emergencyBalance}:${emergencyFund.emergencyLimit}")
        return sb.toString()
    }

    private fun buildPayloadJsonObject(
        userProfile: UserFinancialProfile,
        appSettings: AppSettings,
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>,
        mandatoryBills: List<MandatoryBillItem>,
        categoryLimits: Map<String, CategoryLimitConfig>,
        emergencyFund: EmergencyFundConfig,
        communicationPreferences: UserCommunicationPreferences?
    ): JSONObject {
        val habitsArray = JSONArray()
        userProfile.dailyHabits.forEach { habitsArray.put(it) }

        val profileObj = JSONObject().apply {
            put("isCompleted", userProfile.isCompleted)
            put("countryCode", userProfile.countryCode)
            put("currencyCode", userProfile.currencyCode)
            put("nickname", userProfile.nickname)
            put("gender", userProfile.gender)
            put("livingStatus", userProfile.livingStatus)
            put("incomeSourcePrimary", userProfile.incomeSourcePrimary)
            put("monthlyIncomeAmount", userProfile.monthlyIncomeAmount)
            put("paydayDate", userProfile.paydayDate)
            put("dependentsCount", userProfile.dependentsCount)
            put("transportMode", userProfile.transportMode)
            put("monthlyDebtObligation", userProfile.monthlyDebtObligation)
            put("primaryFinancialGoal", userProfile.primaryFinancialGoal)
            put("dailyHabits", habitsArray)
            put("controlMode", userProfile.controlMode)
            put("workShiftType", userProfile.workShiftType)
            put("workStartHour", userProfile.workStartHour)
            put("workStartMinute", userProfile.workStartMinute)
            put("lunchBreakHour", userProfile.lunchBreakHour)
            put("lunchBreakMinute", userProfile.lunchBreakMinute)
            put("workEndHour", userProfile.workEndHour)
            put("workEndMinute", userProfile.workEndMinute)
            put("eveningReviewHour", userProfile.eveningReviewHour)
            put("eveningReviewMinute", userProfile.eveningReviewMinute)
            put("activeDaysType", userProfile.activeDaysType)
            put("isPostActivityReminderEnabled", userProfile.isPostActivityReminderEnabled)
            put("isSmartScheduledReminderEnabled", userProfile.isSmartScheduledReminderEnabled)
        }

        val settingsObj = JSONObject().apply {
            put("language", appSettings.language.name)
            put("countryCode", appSettings.countryCode)
            put("currencyCode", appSettings.currencyCode)
            put("isDarkMode", appSettings.isDarkMode)
            put("useSystemTheme", appSettings.useSystemTheme)
            put("currencyFormat", appSettings.currencyFormat.name)
            put("isAppLockEnabled", appSettings.isAppLockEnabled)
            put("hideNominalOnDashboard", appSettings.hideNominalOnDashboard)
            put("dailyReminderNotification", appSettings.dailyReminderNotification)
            put("reminderHour", appSettings.reminderHour)
            put("reminderMinute", appSettings.reminderMinute)
            put("soundHapticFeedback", appSettings.soundHapticFeedback)
            put("autoBackupCloud", appSettings.autoBackupCloud)
            put("hasSeenUserGuide", appSettings.hasSeenUserGuide)
            put("fontScaleFactor", appSettings.fontScaleFactor.toDouble())
            put("boldNumbers", appSettings.boldNumbers)
            put("highContrastMode", appSettings.highContrastMode)
            put("accentTheme", appSettings.accentTheme)
            put("dateFormatPattern", appSettings.dateFormatPattern)
            put("timeFormat24H", appSettings.timeFormat24H)
            put("hideAmountInNotifications", appSettings.hideAmountInNotifications)
            put("showInsightCardOnDashboard", appSettings.showInsightCardOnDashboard)
        }

        val walletsArr = JSONArray()
        wallets.forEach { w ->
            walletsArr.put(
                JSONObject().apply {
                    put("id", w.id)
                    put("userId", w.userId)
                    put("name", w.name)
                    put("walletType", w.walletType)
                    put("balance", w.balance)
                    put("colorHex", w.colorHex)
                    put("iconKey", w.iconKey)
                }
            )
        }

        val txArr = JSONArray()
        transactions.forEach { tx ->
            txArr.put(
                JSONObject().apply {
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
            )
        }

        val goalsArr = JSONArray()
        goals.forEach { g ->
            goalsArr.put(
                JSONObject().apply {
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
            )
        }

        val billsArr = JSONArray()
        mandatoryBills.forEach { b ->
            billsArr.put(
                JSONObject().apply {
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
            )
        }

        val limitsArr = JSONArray()
        categoryLimits.forEach { (_, cfg) ->
            limitsArr.put(
                JSONObject().apply {
                    put("categoryKey", cfg.categoryKey)
                    put("limitAmount", cfg.limitAmount)
                    put("period", cfg.period)
                    put("customLabel", cfg.customLabel)
                    put("customSubtitle", cfg.customSubtitle)
                    put("startDateMillis", cfg.startDateMillis)
                    put("endDateMillis", cfg.endDateMillis)
                    put("isCustomCategory", cfg.isCustomCategory)
                }
            )
        }

        val emObj = JSONObject().apply {
            put("emergencyBalance", emergencyFund.emergencyBalance)
            put("emergencyLimit", emergencyFund.emergencyLimit)
            put("limitPeriod", emergencyFund.limitPeriod)
            put("storagePlaceNote", emergencyFund.storagePlaceNote)
        }

        val commObj = communicationPreferences?.let { cp ->
            JSONObject().apply {
                put("isManualPersonaMode", cp.isManualPersonaMode)
                put("manualPersona", cp.manualPersona.name)
                put("stylePreference", cp.stylePreference.name)
                put("allowRoastingAndSarcasm", cp.allowRoastingAndSarcasm)
                put("strictnessLevel", cp.strictnessLevel)
                put("sentenceLength", cp.sentenceLength)
                put("focusPreference", cp.focusPreference)
                put("ageRange", cp.ageRange)
                put("activityType", cp.activityType)
                put("selfPerceivedCondition", cp.selfPerceivedCondition)
                put("primaryFinancialGoal", cp.primaryFinancialGoal)
            }
        }

        return JSONObject().apply {
            put("userProfile", profileObj)
            put("appSettings", settingsObj)
            put("wallets", walletsArr)
            put("transactions", txArr)
            put("goals", goalsArr)
            put("mandatoryBills", billsArr)
            put("categoryLimits", limitsArr)
            put("emergencyFund", emObj)
            if (commObj != null) {
                put("communicationPreferences", commObj)
            }
        }
    }

    private fun parseUserProfile(obj: JSONObject?): UserFinancialProfile {
        val defaults = UserFinancialProfile(isCompleted = true)
        if (obj == null) return defaults
        val habitsJson = obj.optJSONArray("dailyHabits")
        val habits = mutableListOf<String>()
        if (habitsJson != null) {
            for (i in 0 until habitsJson.length()) {
                val h = habitsJson.optString(i)
                if (h.isNotBlank()) habits.add(h)
            }
        }
        val cCode = obj.optString("countryCode", defaults.countryCode).ifBlank { "US" }
        val currCode = obj.optString("currencyCode", WorldCountryCurrencyRegistry.byCountryCode(cCode).currencyCode)
        return UserFinancialProfile(
            isCompleted = obj.optBoolean("isCompleted", true),
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
    }

    private fun parseAppSettings(obj: JSONObject?, profile: UserFinancialProfile): AppSettings {
        val defaults = AppSettings(
            countryCode = profile.countryCode,
            currencyCode = profile.currencyCode,
            hasSeenUserGuide = true
        )
        if (obj == null) return defaults
        val langName = obj.optString("language", defaults.language.name)
        val lang = runCatching { AppLanguage.valueOf(langName) }.getOrDefault(defaults.language)
        val cCode = obj.optString("countryCode", profile.countryCode).ifBlank { "US" }
        val currCode = obj.optString("currencyCode", WorldCountryCurrencyRegistry.byCountryCode(cCode).currencyCode)
        val fmtName = obj.optString("currencyFormat", defaults.currencyFormat.name)
        val fmt = runCatching { CurrencyFormatOption.valueOf(fmtName) }.getOrDefault(defaults.currencyFormat)
        return defaults.copy(
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
            hasSeenUserGuide = true,
            fontScaleFactor = obj.optDouble("fontScaleFactor", defaults.fontScaleFactor.toDouble()).toFloat(),
            boldNumbers = obj.optBoolean("boldNumbers", defaults.boldNumbers),
            highContrastMode = obj.optBoolean("highContrastMode", defaults.highContrastMode),
            accentTheme = obj.optString("accentTheme", defaults.accentTheme),
            dateFormatPattern = obj.optString("dateFormatPattern", defaults.dateFormatPattern).ifBlank { "dd MMM yyyy" },
            timeFormat24H = obj.optBoolean("timeFormat24H", defaults.timeFormat24H),
            hideAmountInNotifications = obj.optBoolean("hideAmountInNotifications", defaults.hideAmountInNotifications),
            showInsightCardOnDashboard = obj.optBoolean("showInsightCardOnDashboard", defaults.showInsightCardOnDashboard)
        )
    }

    private fun parseCommunicationPrefs(obj: JSONObject?): UserCommunicationPreferences? {
        if (obj == null) return null
        val defaults = UserCommunicationPreferences()
        return UserCommunicationPreferences(
            isManualPersonaMode = obj.optBoolean("isManualPersonaMode", defaults.isManualPersonaMode),
            manualPersona = runCatching { InsightPersona.valueOf(obj.optString("manualPersona")) }.getOrDefault(defaults.manualPersona),
            stylePreference = CommunicationStylePreference.fromNameOrDefault(obj.optString("stylePreference")),
            allowRoastingAndSarcasm = obj.optBoolean("allowRoastingAndSarcasm", defaults.allowRoastingAndSarcasm),
            strictnessLevel = obj.optString("strictnessLevel", defaults.strictnessLevel),
            sentenceLength = obj.optString("sentenceLength", defaults.sentenceLength),
            focusPreference = obj.optString("focusPreference", defaults.focusPreference),
            ageRange = obj.optString("ageRange", defaults.ageRange),
            activityType = obj.optString("activityType", defaults.activityType),
            selfPerceivedCondition = obj.optString("selfPerceivedCondition", defaults.selfPerceivedCondition),
            primaryFinancialGoal = obj.optString("primaryFinancialGoal", defaults.primaryFinancialGoal)
        )
    }

    internal fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Storage Inspector & Safe Cache Cleaner
 */
object ArusDompetStorageInspector {

    fun calculateStorageBreakdown(context: Context): StorageBreakdownInfo {
        val dataDir = context.applicationInfo.dataDir?.let { File(it) }
        val prefsFile = if (dataDir != null) {
            File(File(dataDir, "shared_prefs"), "aruskas_local_storage_v1.xml")
        } else null
        val financialBytes = if (prefsFile != null && prefsFile.exists()) {
            prefsFile.length()
        } else {
            val prefs = context.getSharedPreferences("aruskas_local_storage_v1", Context.MODE_PRIVATE)
            prefs.all.entries.sumOf { (k, v) -> (k.length + (v?.toString()?.length ?: 0)).toLong() }
        }

        val dbFile = context.getDatabasePath("offline_financial_insight_v1.db")
        val dbBytes = if (dbFile != null && dbFile.exists()) dbFile.length() else 0L

        val cacheBytes = calculateDirSize(context.cacheDir)

        return StorageBreakdownInfo(
            financialDataBytes = financialBytes.coerceAtLeast(512L),
            offlineEngineDbBytes = dbBytes,
            temporaryCacheBytes = cacheBytes
        )
    }

    /**
     * Safely deletes temporary cache files (such as temporary PDF/CSV/JSON exports in cacheDir)
     * WITHOUT touching any user financial records, preferences, or SQLite database.
     */
    fun clearTemporaryCacheOnly(context: Context): Long {
        val before = calculateDirSize(context.cacheDir)
        context.cacheDir.listFiles()?.forEach { child ->
            deleteRecursively(child)
        }
        val after = calculateDirSize(context.cacheDir)
        return (before - after).coerceAtLeast(0L)
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
        return total
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        runCatching { file.delete() }
    }
}

/**
 * Cryptographic Salted SHA-256 PIN Security Manager.
 * Never stores user PIN in plain text.
 */
object PinSecurityManager {
    private const val PREF_VAULT = "arus_dompet_security_vault"
    private const val KEY_PIN_SALT = "pin_salt_hex"
    private const val KEY_PIN_HASH = "pin_sha256_hash"

    fun hasPinConfigured(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREF_VAULT, Context.MODE_PRIVATE)
        val salt = prefs.getString(KEY_PIN_SALT, null)
        val hash = prefs.getString(KEY_PIN_HASH, null)
        return !salt.isNullOrBlank() && !hash.isNullOrBlank()
    }

    fun setPin(context: Context, rawPin: String): Boolean {
        val cleaned = rawPin.trim()
        if (cleaned.length < 4 || cleaned.length > 6 || !cleaned.all { it.isDigit() }) {
            return false
        }
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltHex = saltBytes.joinToString("") { "%02x".format(it) }
        val hashHex = computeSaltedHash(saltHex, cleaned)

        context.applicationContext
            .getSharedPreferences(PREF_VAULT, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN_SALT, saltHex)
            .putString(KEY_PIN_HASH, hashHex)
            .apply()
        return true
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREF_VAULT, Context.MODE_PRIVATE)
        val saltHex = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val candidateHash = computeSaltedHash(saltHex, inputPin.trim())
        return MessageDigest.isEqual(
            storedHash.toByteArray(StandardCharsets.UTF_8),
            candidateHash.toByteArray(StandardCharsets.UTF_8)
        )
    }

    fun clearPin(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREF_VAULT, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_HASH)
            .apply()
    }

    internal fun getStoredHashForAudit(context: Context): String? {
        val prefs = context.applicationContext.getSharedPreferences(PREF_VAULT, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PIN_HASH, null)
    }

    private fun computeSaltedHash(saltHex: String, pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val payload = "$saltHex:arus_dompet_pin_v1:$pin"
        val bytes = digest.digest(payload.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Official Support, Bug Reporting, and Feedback Helper for Admin iwa.ye (programerway@gmail.com).
 * Guarantees zero financial/transaction data leakage in bug reports.
 */
object AdminSupportHelper {
    const val ADMIN_NAME = "iwa.ye"
    const val ADMIN_EMAIL = "programerway@gmail.com"
    const val APP_VERSION_NAME = "1.0.1"
    const val APP_VERSION_CODE = 2

    fun copyAdminEmailToClipboard(context: Context, lang: AppLanguage, onMessage: (String) -> Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Admin Email", ADMIN_EMAIL))
        val isId = lang == AppLanguage.INDONESIAN
        onMessage(
            if (isId) "Email admin ($ADMIN_EMAIL) disalin ke papan klip!"
            else "Admin email ($ADMIN_EMAIL) copied to clipboard!"
        )
    }

    fun launchContactAdminEmail(
        context: Context,
        lang: AppLanguage,
        onFallbackMessage: (String) -> Unit
    ) {
        val isId = lang == AppLanguage.INDONESIAN
        val subject = if (isId) {
            "Bantuan Aplikasi Arus Dompet - Kepada Admin $ADMIN_NAME"
        } else {
            "Arus Dompet App Support Request - To Admin $ADMIN_NAME"
        }
        val body = if (isId) {
            "Halo Admin Arus Dompet ($ADMIN_NAME),\n\n" +
                "Saya ingin meminta bantuan mengenai aplikasi.\n\n" +
                "Pertanyaan / Kendala saya:\n- \n\n" +
                "---\n" +
                "Info Teknis Dasar (Tanpa Data Keuangan Pribadi):\n" +
                "• Aplikasi: Arus Dompet v$APP_VERSION_NAME ($APP_VERSION_CODE)\n" +
                "• Perangkat: ${Build.MANUFACTURER} ${Build.MODEL} (Android SDK ${Build.VERSION.SDK_INT})"
        } else {
            "Halo Admin Arus Dompet ($ADMIN_NAME), saya ingin meminta bantuan mengenai aplikasi.\n\n" +
                "My Question / Inquiry:\n- \n\n" +
                "---\n" +
                "Basic Technical Info (Zero Personal Financial Data):\n" +
                "• App: Arus Dompet v$APP_VERSION_NAME ($APP_VERSION_CODE)\n" +
                "• Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android SDK ${Build.VERSION.SDK_INT})"
        }

        launchEmailIntentSafely(
            context = context,
            subject = subject,
            body = body,
            attachmentUri = null,
            lang = lang,
            onFallbackMessage = onFallbackMessage
        )
    }

    fun launchBugReportEmail(
        context: Context,
        issueType: String,
        issueTitle: String,
        issueDescription: String,
        stepsToReproduce: String,
        attachmentUri: Uri?,
        lang: AppLanguage,
        onFallbackMessage: (String) -> Unit
    ) {
        val isId = lang == AppLanguage.INDONESIAN
        val cleanTitle = issueTitle.trim().ifBlank { if (isId) "Laporan Kendala Teknis" else "Technical Issue Report" }
        val subject = "[Bug Report - $issueType] Arus Dompet: $cleanTitle"
        val body = buildString {
            appendLine("Halo Admin Arus Dompet ($ADMIN_NAME),")
            appendLine()
            appendLine(if (isId) "Berikut laporan masalah yang saya temukan di aplikasi:" else "Here is an issue report I encountered in the app:")
            appendLine("• ${if (isId) "Jenis Masalah" else "Issue Category"}: $issueType")
            appendLine("• ${if (isId) "Judul Masalah" else "Issue Title"}: $cleanTitle")
            appendLine()
            appendLine("${if (isId) "Deskripsi Detail" else "Detailed Description"}:")
            appendLine(issueDescription.trim().ifBlank { "-" })
            appendLine()
            appendLine("${if (isId) "Langkah Kejadian (Reproduksi)" else "Steps to Reproduce"}:")
            appendLine(stepsToReproduce.trim().ifBlank { "-" })
            appendLine()
            appendLine("---")
            appendLine(if (isId) "Diagnostik Sistem (Aman & Tanpa Data Saldo/Transaksi):" else "System Diagnostics (Safe & Zero Financial/Transaction Data):")
            appendLine("• App Version: Arus Dompet v$APP_VERSION_NAME (Build $APP_VERSION_CODE)")
            appendLine("• Android Version: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("• Device Model: ${Build.MANUFACTURER} ${Build.MODEL}")
        }

        launchEmailIntentSafely(
            context = context,
            subject = subject,
            body = body,
            attachmentUri = attachmentUri,
            lang = lang,
            onFallbackMessage = onFallbackMessage
        )
    }

    fun launchFeatureSuggestionEmail(
        context: Context,
        category: String,
        suggestionTitle: String,
        suggestionDetail: String,
        lang: AppLanguage,
        onFallbackMessage: (String) -> Unit
    ) {
        val isId = lang == AppLanguage.INDONESIAN
        val cleanTitle = suggestionTitle.trim().ifBlank { if (isId) "Usulan Fitur Baru" else "New Feature Suggestion" }
        val subject = "[Saran Fitur - $category] Arus Dompet: $cleanTitle"
        val body = buildString {
            appendLine("Halo Admin Arus Dompet ($ADMIN_NAME),")
            appendLine()
            appendLine(if (isId) "Saya ingin mengirimkan saran pengembangan untuk Arus Dompet:" else "I would like to share a feature suggestion for Arus Dompet:")
            appendLine("• ${if (isId) "Kategori" else "Category"}: $category")
            appendLine("• ${if (isId) "Judul Saran" else "Title"}: $cleanTitle")
            appendLine()
            appendLine("${if (isId) "Rincian Ide / Masukan" else "Detailed Suggestion"}:")
            appendLine(suggestionDetail.trim())
            appendLine()
            appendLine("---")
            appendLine("Arus Dompet v$APP_VERSION_NAME ($APP_VERSION_CODE)")
        }

        launchEmailIntentSafely(
            context = context,
            subject = subject,
            body = body,
            attachmentUri = null,
            lang = lang,
            onFallbackMessage = onFallbackMessage
        )
    }

    private fun launchEmailIntentSafely(
        context: Context,
        subject: String,
        body: String,
        attachmentUri: Uri?,
        lang: AppLanguage,
        onFallbackMessage: (String) -> Unit
    ) {
        val isId = lang == AppLanguage.INDONESIAN
        try {
            if (attachmentUri == null) {
                val mailtoUri = Uri.parse(
                    "mailto:$ADMIN_EMAIL?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
                )
                val sendToIntent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendToIntent)
            } else {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    putExtra(Intent.EXTRA_STREAM, attachmentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(
                    sendIntent,
                    if (isId) "Kirim Laporan via Email ke $ADMIN_NAME" else "Send Report via Email to $ADMIN_NAME"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            try {
                val fallbackShare = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, "Kepada: $ADMIN_NAME ($ADMIN_EMAIL)\n\n$body")
                }
                val chooser = Intent.createChooser(
                    fallbackShare,
                    if (isId) "Hubungi Admin $ADMIN_NAME ($ADMIN_EMAIL)" else "Contact Admin $ADMIN_NAME ($ADMIN_EMAIL)"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (ex: Exception) {
                copyAdminEmailToClipboard(context, lang) {}
                onFallbackMessage(
                    if (isId) "Aplikasi email tidak ditemukan. Email admin ($ADMIN_EMAIL) telah disalin ke papan klip!"
                    else "No email client found. Admin email ($ADMIN_EMAIL) has been copied to your clipboard!"
                )
            }
        }
    }
}
