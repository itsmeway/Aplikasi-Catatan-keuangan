package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.AppSettings
import com.example.data.ArusKasLocalStore
import com.example.data.ArusKasRepository
import com.example.data.CategoryLimitConfig
import com.example.data.EmergencyFundConfig
import com.example.data.MandatoryBillItem
import com.example.data.PresetCategories
import com.example.data.SavingsGoal
import com.example.data.TransactionRecord
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface GuruAdviceState {
    data object Idle : GuruAdviceState
    data object Loading : GuruAdviceState
    data class Success(
        val statusBadge: String,
        val teguran: String,
        val motivasi: String,
        val langkahKonkret: String,
        val colorLevel: String = "GREEN", // GREEN, YELLOW, RED, BLUE
        val saldoCritique: String = "",
        val nominalBoxAnalysis: String = "",
        val categoryCritique: String = ""
    ) : GuruAdviceState
    data class Error(val message: String) : GuruAdviceState
}

class ArusKasViewModel(
    private val repository: ArusKasRepository? = null,
    private val currentUserId: String? = null,
    private val localStore: ArusKasLocalStore? = null
) : ViewModel() {

    private val _sessionSeed = MutableStateFlow(((System.currentTimeMillis() / 1000L) % 9973L).toInt())
    val sessionSeed: StateFlow<Int> = _sessionSeed.asStateFlow()

    private val guruSchema = Schema.obj(
        properties = mapOf(
            "statusBadge" to Schema.string(description = "Label notifikasi pengingat singkat 2-4 kata huruf kapital"),
            "colorLevel" to Schema.string(description = "Pilih satu: GREEN, YELLOW, RED, atau BLUE"),
            "saldoCritique" to Schema.string(description = "1 kalimat singkat kondisi saldo"),
            "nominalBoxAnalysis" to Schema.string(description = "Analisis singkat keuangan"),
            "categoryCritique" to Schema.string(description = "1 kalimat kritik kategori"),
            "teguran" to Schema.string(description = "Teguran jujur santai"),
            "motivasi" to Schema.string(description = "Kalimat penyemangat"),
            "langkahKonkret" to Schema.string(description = "1 saran angka praktis hari ini")
        )
    )

    private val guruModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = guruSchema
            }
        )
    }

    private val _userProfile = MutableStateFlow(
        localStore?.loadUserProfile() ?: UserFinancialProfile()
    )
    val userProfile: StateFlow<UserFinancialProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(
        localStore?.loadAppSettings() ?: AppSettings(
            language = AppLanguage.ENGLISH,
            countryCode = _userProfile.value.countryCode.ifBlank { "US" },
            currencyCode = _userProfile.value.currencyCode.ifBlank { "USD" }
        )
    )
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    init {
        val activeCountry = _appSettings.value.countryCode.ifBlank { _userProfile.value.countryCode.ifBlank { "US" } }
        com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = activeCountry
        com.example.data.WorldCountryCurrencyRegistry.activeLanguage = _appSettings.value.language
    }

    private val defaultInitialGoals: List<SavingsGoal>
        get() {
            val cCode = com.example.data.WorldCountryCurrencyRegistry.activeCountryCode
            val isId = com.example.data.WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.INDONESIAN
            val targetAmt = if (cCode == "ID") 10_000_000L else com.example.data.WorldCountryCurrencyRegistry.fromUsd(1_200.0, cCode)
            val savedAmt = 0L
            val dailyRec = if (cCode == "ID") 40_000L else com.example.data.WorldCountryCurrencyRegistry.fromUsd(10.0, cCode)
            return listOf(
                SavingsGoal(
                    id = "goal_laptop_utama",
                    userId = "local_preview",
                    title = if (isId) "Beli Laptop Impian" else "MacBook / Work Laptop",
                    targetAmount = targetAmt,
                    savedAmount = savedAmt,
                    dailyRecommendation = dailyRec,
                    note = if (isId) "Sisihkan rutin dari penghasilan" else "Consistent savings from monthly income",
                    iconKey = "laptop"
                )
            )
        }

    private class SyncStateHolder<T>(
        initialValue: T,
        private val onUpdated: (T) -> Unit
    ) {
        val flow = MutableStateFlow(initialValue)
        var value: T
            get() = flow.value
            set(newVal) {
                flow.value = newVal
                onUpdated(newVal)
            }
    }

    private val _localWalletsUiState = MutableStateFlow<UiState<List<Wallet>>>(
        UiState.Success(localStore?.loadWallets() ?: com.example.data.WorldCountryCurrencyRegistry.getActiveProfile().starterWallets)
    )
    private val _localTransactionsUiState = MutableStateFlow<UiState<List<TransactionRecord>>>(
        UiState.Success(localStore?.loadTransactions() ?: emptyList())
    )
    private val _localGoalsUiState = MutableStateFlow<UiState<List<SavingsGoal>>>(
        UiState.Success(localStore?.loadGoals(defaultInitialGoals) ?: defaultInitialGoals)
    )

    private val _localWallets = SyncStateHolder(
        (_localWalletsUiState.value as UiState.Success).data
    ) { _localWalletsUiState.value = UiState.Success(it) }

    private val _localTransactions = SyncStateHolder(
        (_localTransactionsUiState.value as UiState.Success).data
    ) { _localTransactionsUiState.value = UiState.Success(it) }

    private val _localGoals = SyncStateHolder(
        (_localGoalsUiState.value as UiState.Success).data
    ) { _localGoalsUiState.value = UiState.Success(it) }

    private val _mandatoryBills = MutableStateFlow(
        localStore?.loadMandatoryBills() ?: com.example.data.WorldCountryCurrencyRegistry.defaultMandatoryBillsForCountry(
            com.example.data.WorldCountryCurrencyRegistry.activeCountryCode,
            com.example.data.WorldCountryCurrencyRegistry.activeLanguage
        )
    )
    val mandatoryBills: StateFlow<List<MandatoryBillItem>> = _mandatoryBills.asStateFlow()

    /**
     * Dipanggil ketika pengguna menyelesaikan ATAU melewati (Skip) Kuesioner Awal / Panduan
     * agar kuesioner dan panduan HANYA muncul 1x seumur hidup saat pertama kali buka aplikasi,
     * dan tidak pernah muncul otomatis lagi saat aplikasi ditutup-buka berulang kali.
     */
    fun dismissOrSkipOnboarding() {
        val updatedProfile = _userProfile.value.copy(isCompleted = true)
        val updatedSettings = _appSettings.value.copy(hasSeenUserGuide = true)
        _userProfile.value = updatedProfile
        _appSettings.value = updatedSettings
        localStore?.markInitialOnboardingSeenOnce()
        localStore?.saveUserProfile(updatedProfile)
        localStore?.saveAppSettings(updatedSettings)
        localStore?.notifyWidgetDataChanged()
    }

    val walletsState: StateFlow<UiState<List<Wallet>>> =
        if (currentUserId != null && repository != null) {
            repository.observeWallets(currentUserId)
                .map<List<Wallet>, UiState<List<Wallet>>> { UiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    emit(UiState.Error(rootError.message ?: "Failed to load wallets"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = UiState.Loading
                )
        } else {
            _localWalletsUiState.asStateFlow()
        }

    val transactionsState: StateFlow<UiState<List<TransactionRecord>>> =
        if (currentUserId != null && repository != null) {
            repository.observeTransactions(currentUserId)
                .map<List<TransactionRecord>, UiState<List<TransactionRecord>>> { UiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    emit(UiState.Error(rootError.message ?: "Failed to load transactions"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = UiState.Loading
                )
        } else {
            _localTransactionsUiState.asStateFlow()
        }

    val goalsState: StateFlow<UiState<List<SavingsGoal>>> =
        if (currentUserId != null && repository != null) {
            repository.observeGoals(currentUserId)
                .map<List<SavingsGoal>, UiState<List<SavingsGoal>>> { UiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    emit(UiState.Error(rootError.message ?: "Failed to load savings goals"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = UiState.Loading
                )
        } else {
            _localGoalsUiState.asStateFlow()
        }

    private val _categoryLimits = MutableStateFlow(
        localStore?.loadCategoryLimits() ?: com.example.data.WorldCountryCurrencyRegistry.defaultCategoryLimitsForCountry(
            com.example.data.WorldCountryCurrencyRegistry.activeCountryCode,
            com.example.data.WorldCountryCurrencyRegistry.activeLanguage
        )
    )
    val categoryLimits: StateFlow<Map<String, CategoryLimitConfig>> = _categoryLimits.asStateFlow()

    private val _emergencyFund = MutableStateFlow(
        localStore?.loadEmergencyFund() ?: EmergencyFundConfig(
            emergencyBalance = 0L,
            emergencyLimit = 0L,
            limitPeriod = "BULANAN",
            storagePlaceNote = "Separate Vault / Account (Isolated from daily funds)"
        )
    )
    val emergencyFund: StateFlow<EmergencyFundConfig> = _emergencyFund.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _guruState = MutableStateFlow<GuruAdviceState>(GuruAdviceState.Idle)
    val guruState: StateFlow<GuruAdviceState> = _guruState.asStateFlow()

    private fun persistAllLocalData() {
        val store = localStore ?: return
        store.saveUserProfile(_userProfile.value)
        store.saveAppSettings(_appSettings.value)
        store.saveWallets(_localWallets.value)
        store.saveTransactions(_localTransactions.value)
        store.saveGoals(_localGoals.value)
        store.saveMandatoryBills(_mandatoryBills.value)
        store.saveCategoryLimits(_categoryLimits.value)
        store.saveEmergencyFund(_emergencyFund.value)
        store.notifyWidgetDataChanged()
    }

    /**
     * Memastikan data aplikasi langsung sinkron ketika pengguna kembali ke aplikasi
     * setelah mencatat transaksi dari Widget Beranda HP.
     */
    fun syncFromLocalStore() {
        if (currentUserId != null && repository != null) return
        val store = localStore ?: return
        val loadedSettings = store.loadAppSettings()
        com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = loadedSettings.countryCode.ifBlank { "US" }
        com.example.data.WorldCountryCurrencyRegistry.activeLanguage = loadedSettings.language
        _appSettings.value = loadedSettings
        _userProfile.value = store.loadUserProfile()
        _localWallets.value = store.loadWallets()
        _localTransactions.value = store.loadTransactions()
        _localGoals.value = store.loadGoals(defaultInitialGoals)
        _mandatoryBills.value = store.loadMandatoryBills()
        _categoryLimits.value = store.loadCategoryLimits()
        _emergencyFund.value = store.loadEmergencyFund()
    }

    fun createFullBackupJson(
        communicationPrefs: com.example.data.offlineinsight.UserCommunicationPreferences? = null
    ): String {
        return com.example.data.ArusDompetBackupManager.createFullBackupJson(
            userProfile = _userProfile.value,
            appSettings = _appSettings.value,
            wallets = _localWallets.value,
            transactions = _localTransactions.value,
            goals = _localGoals.value,
            mandatoryBills = _mandatoryBills.value,
            categoryLimits = _categoryLimits.value,
            emergencyFund = _emergencyFund.value,
            communicationPreferences = communicationPrefs
        )
    }

    fun restoreFromBackup(
        backup: com.example.data.ParsedArusDompetBackup,
        mergeWithoutDuplicates: Boolean = false
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (!mergeWithoutDuplicates) {
            // Mode Replace: Pulihkan kondisi persis sesuai file backup yang telah tervalidasi
            com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = backup.appSettings.countryCode
            com.example.data.WorldCountryCurrencyRegistry.activeLanguage = backup.appSettings.language
            _userProfile.value = backup.userProfile
            _appSettings.value = backup.appSettings
            _localWallets.value = backup.wallets
            _localTransactions.value = backup.transactions.sortedByDescending { it.transactionDateMillis }
            _localGoals.value = backup.goals
            _mandatoryBills.value = backup.mandatoryBills
            if (backup.categoryLimits.isNotEmpty()) {
                _categoryLimits.value = backup.categoryLimits
            }
            _emergencyFund.value = backup.emergencyFund
            persistAllLocalData()
            _actionFeedback.value = if (isId) {
                "Data berhasil dipulihkan! (${backup.wallets.size} dompet, ${backup.transactions.size} transaksi, ${backup.goals.size} target)"
            } else {
                "Data restored successfully! (${backup.wallets.size} wallets, ${backup.transactions.size} transactions, ${backup.goals.size} goals)"
            }
        } else {
            // Mode Merge: Gabungkan tanpa duplikasi transaksi atau saldo ganda
            val existingWalletIds = _localWallets.value.map { it.id }.toSet()
            val newWalletsFromBackup = backup.wallets.filter { it.id !in existingWalletIds }

            val existingTxIds = _localTransactions.value.map { it.id }.toSet()
            val newTxsFromBackup = backup.transactions.filter { it.id !in existingTxIds }

            // Sesuaikan saldo dompet lama hanya untuk transaksi baru yang belum pernah tercatat
            val walletDeltas = mutableMapOf<String, Long>()
            newTxsFromBackup.forEach { tx ->
                if (tx.walletId in existingWalletIds) {
                    val delta = when (tx.type) {
                        "INCOME" -> tx.amount
                        "EXPENSE", "SAVINGS_ALLOCATION" -> -tx.amount
                        else -> 0L
                    }
                    walletDeltas[tx.walletId] = safeAddBalance(walletDeltas[tx.walletId] ?: 0L, delta)
                }
            }

            val mergedWallets = _localWallets.value.map { w ->
                val d = walletDeltas[w.id] ?: 0L
                if (d != 0L) w.copy(balance = safeAddBalance(w.balance, d)) else w
            } + newWalletsFromBackup

            val mergedTxs = (_localTransactions.value + newTxsFromBackup)
                .distinctBy { it.id }
                .sortedByDescending { it.transactionDateMillis }

            val existingGoalIds = _localGoals.value.map { it.id }.toSet()
            val mergedGoals = _localGoals.value + backup.goals.filter { it.id !in existingGoalIds }

            val existingBillIds = _mandatoryBills.value.map { it.id }.toSet()
            val mergedBills = _mandatoryBills.value + backup.mandatoryBills.filter { it.id !in existingBillIds }

            val mergedLimits = _categoryLimits.value.toMutableMap()
            backup.categoryLimits.forEach { (k, v) ->
                if (!mergedLimits.containsKey(k)) {
                    mergedLimits[k] = v
                }
            }

            _localWallets.value = mergedWallets
            _localTransactions.value = mergedTxs
            _localGoals.value = mergedGoals
            _mandatoryBills.value = mergedBills
            _categoryLimits.value = mergedLimits
            persistAllLocalData()

            _actionFeedback.value = if (isId) {
                "Data digabungkan tanpa duplikat! (+${newTxsFromBackup.size} transaksi baru, +${newWalletsFromBackup.size} dompet baru)"
            } else {
                "Data merged without duplicates! (+${newTxsFromBackup.size} new transactions, +${newWalletsFromBackup.size} new wallets)"
            }
        }
    }

    fun resetAllFinancialDataToZero() {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        _localWallets.value = _localWallets.value.map { it.copy(balance = 0L) }
        _localTransactions.value = emptyList()
        _localGoals.value = _localGoals.value.map { it.copy(savedAmount = 0L) }
        _mandatoryBills.value = _mandatoryBills.value.map { it.copy(paidAmount = 0L, status = "UNPAID") }
        _emergencyFund.value = _emergencyFund.value.copy(emergencyBalance = 0L)
        persistAllLocalData()
        _actionFeedback.value = if (isId) {
            "Seluruh saldo, transaksi, dan progres tabungan berhasil direset ke 0!"
        } else {
            "All balances, transactions, and savings progress have been reset to 0!"
        }
    }

    fun updateLanguage(lang: AppLanguage) {
        com.example.data.WorldCountryCurrencyRegistry.activeLanguage = lang
        val updated = _appSettings.value.copy(language = lang)
        _appSettings.value = updated
        localStore?.saveAppSettings(updated)
        localStore?.notifyWidgetDataChanged()
        _actionFeedback.value = when (lang) {
            AppLanguage.ENGLISH -> "Language set to English (International)!"
            AppLanguage.INDONESIAN -> "Bahasa berhasil diubah ke Bahasa Indonesia!"
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        com.example.data.WorldCountryCurrencyRegistry.activeLanguage = newSettings.language
        com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = newSettings.countryCode
        _appSettings.value = newSettings
        localStore?.saveAppSettings(newSettings)
        localStore?.notifyWidgetDataChanged()
        val isId = newSettings.language == AppLanguage.INDONESIAN
        _actionFeedback.value = if (isId) "Pengaturan berhasil disimpan!" else "Settings saved successfully!"
    }

    /**
     * Switches Country & Currency across the entire application.
     * Sensitively converts existing wallet balances, category limits, mandatory bills,
     * emergency fund, and savings goals using reference exchange rates, and adapts bank/e-wallet names
     * to the selected country's financial ecosystem.
     */
    fun changeCountryAndCurrency(
        newCountryCode: String,
        convertExistingAmounts: Boolean = true,
        replaceWithCountryBanks: Boolean = true
    ) {
        val oldCountry = _appSettings.value.countryCode.ifBlank { "US" }
        val targetProfile = com.example.data.WorldCountryCurrencyRegistry.byCountryCode(newCountryCode)
        val lang = _appSettings.value.language
        val isId = lang == AppLanguage.INDONESIAN

        com.example.data.WorldCountryCurrencyRegistry.activeCountryCode = targetProfile.countryCode

        val updatedSettings = _appSettings.value.copy(
            countryCode = targetProfile.countryCode,
            currencyCode = targetProfile.currencyCode
        )
        _appSettings.value = updatedSettings

        val convertedIncome = if (convertExistingAmounts && !oldCountry.equals(targetProfile.countryCode, ignoreCase = true)) {
            com.example.data.WorldCountryCurrencyRegistry.convertAmount(_userProfile.value.monthlyIncomeAmount, oldCountry, targetProfile.countryCode)
        } else {
            targetProfile.defaultMonthlyIncome
        }
        _userProfile.value = _userProfile.value.copy(
            countryCode = targetProfile.countryCode,
            currencyCode = targetProfile.currencyCode,
            monthlyIncomeAmount = convertedIncome
        )

        if (replaceWithCountryBanks) {
            val countryStarterById = targetProfile.starterWallets.associateBy { it.id }
            val currentWallets = _localWallets.value
            val updatedWallets = if (currentWallets.isEmpty()) {
                targetProfile.starterWallets
            } else {
                currentWallets.mapIndexed { idx, w ->
                    val matchedPreset = countryStarterById[w.id] ?: targetProfile.starterWallets.getOrNull(idx)
                    val newBal = if (convertExistingAmounts && !oldCountry.equals(targetProfile.countryCode, ignoreCase = true)) {
                        com.example.data.WorldCountryCurrencyRegistry.convertAmount(w.balance, oldCountry, targetProfile.countryCode)
                    } else {
                        matchedPreset?.balance ?: w.balance
                    }
                    if (matchedPreset != null) {
                        w.copy(
                            name = matchedPreset.name,
                            walletType = matchedPreset.walletType,
                            balance = newBal,
                            colorHex = matchedPreset.colorHex,
                            iconKey = matchedPreset.iconKey
                        )
                    } else {
                        w.copy(balance = newBal)
                    }
                }
            }
            _localWallets.value = updatedWallets
        } else if (convertExistingAmounts && !oldCountry.equals(targetProfile.countryCode, ignoreCase = true)) {
            _localWallets.value = _localWallets.value.map { w ->
                w.copy(balance = com.example.data.WorldCountryCurrencyRegistry.convertAmount(w.balance, oldCountry, targetProfile.countryCode))
            }
        }

        if (!oldCountry.equals(targetProfile.countryCode, ignoreCase = true)) {
            if (convertExistingAmounts) {
                val defaultBillsForTarget = com.example.data.WorldCountryCurrencyRegistry.defaultMandatoryBillsForCountry(targetProfile.countryCode, lang).associateBy { it.id }
                _mandatoryBills.value = _mandatoryBills.value.map { b ->
                    val targetDefault = defaultBillsForTarget[b.id]
                    val newTotal = com.example.data.WorldCountryCurrencyRegistry.convertAmount(b.totalAmount, oldCountry, targetProfile.countryCode)
                    val newPaid = when (b.status) {
                        "PAID" -> newTotal
                        "HALF_PAID" -> newTotal / 2L
                        else -> com.example.data.WorldCountryCurrencyRegistry.convertAmount(b.paidAmount, oldCountry, targetProfile.countryCode)
                    }
                    b.copy(
                        name = targetDefault?.name ?: b.name,
                        totalAmount = newTotal,
                        paidAmount = newPaid,
                        dueScheduleLabel = targetDefault?.dueScheduleLabel ?: b.dueScheduleLabel
                    )
                }

                _categoryLimits.value = _categoryLimits.value.mapValues { (_, cfg) ->
                    cfg.copy(
                        limitAmount = if (cfg.limitAmount > 0L) {
                            com.example.data.WorldCountryCurrencyRegistry.convertAmount(cfg.limitAmount, oldCountry, targetProfile.countryCode)
                        } else 0L
                    )
                }
                syncMandatoryBillsLimitIfDefault()

                val em = _emergencyFund.value
                _emergencyFund.value = em.copy(
                    emergencyBalance = com.example.data.WorldCountryCurrencyRegistry.convertAmount(em.emergencyBalance, oldCountry, targetProfile.countryCode),
                    emergencyLimit = com.example.data.WorldCountryCurrencyRegistry.convertAmount(em.emergencyLimit, oldCountry, targetProfile.countryCode)
                )

                _localGoals.value = _localGoals.value.map { g ->
                    g.copy(
                        targetAmount = com.example.data.WorldCountryCurrencyRegistry.convertAmount(g.targetAmount, oldCountry, targetProfile.countryCode),
                        savedAmount = com.example.data.WorldCountryCurrencyRegistry.convertAmount(g.savedAmount, oldCountry, targetProfile.countryCode),
                        dailyRecommendation = com.example.data.WorldCountryCurrencyRegistry.convertAmount(g.dailyRecommendation, oldCountry, targetProfile.countryCode)
                    )
                }

                _localTransactions.value = _localTransactions.value.map { tx ->
                    tx.copy(
                        amount = com.example.data.WorldCountryCurrencyRegistry.convertAmount(tx.amount, oldCountry, targetProfile.countryCode)
                    )
                }
            } else {
                _mandatoryBills.value = com.example.data.WorldCountryCurrencyRegistry.defaultMandatoryBillsForCountry(targetProfile.countryCode, lang)
                _categoryLimits.value = com.example.data.WorldCountryCurrencyRegistry.defaultCategoryLimitsForCountry(targetProfile.countryCode, lang)
                _emergencyFund.value = EmergencyFundConfig(
                    emergencyBalance = targetProfile.defaultEmergencyBalance,
                    emergencyLimit = targetProfile.defaultEmergencyLimit,
                    limitPeriod = "BULANAN"
                )
            }
        }

        persistAllLocalData()
        _actionFeedback.value = if (isId) {
            "Negara & Mata Uang diubah ke ${targetProfile.flagEmoji} ${targetProfile.countryNameId} (${targetProfile.currencyCode} ${targetProfile.currencySymbol})!"
        } else {
            "Country & Currency updated to ${targetProfile.flagEmoji} ${targetProfile.countryNameEn} (${targetProfile.currencyCode} ${targetProfile.currencySymbol})!"
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    /**
     * Mengubah status pembayaran Tagihan Wajib (UNPAID -> HALF_PAID -> PAID)
     * dan otomatis menyesuaikan saldo dompet serta pencatatan transaksi secara presisi
     * (termasuk menghapus/menyesuaikan catatan transaksi jika status dikembalikan ke Belum Lunas).
     */
    fun updateMandatoryBillStatus(
        bill: MandatoryBillItem,
        newStatus: String,
        targetWallet: Wallet?,
        customPaidAmount: Long? = null
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val targetPaid = when (newStatus) {
            "PAID" -> bill.totalAmount
            "HALF_PAID" -> customPaidAmount?.coerceIn(1L, bill.totalAmount) ?: (bill.totalAmount / 2L)
            else -> 0L
        }
        val deltaToPay = targetPaid - bill.paidAmount

        _mandatoryBills.value = _mandatoryBills.value.map { item ->
            if (item.id == bill.id) {
                item.copy(
                    paidAmount = targetPaid,
                    status = newStatus
                )
            } else item
        }

        val billTxPrefix = "tx_bill_${bill.id}_"
        val billCategoryLabel = if (isId) "Tagihan Wajib" else "Mandatory Bill"

        if (deltaToPay != 0L && targetWallet != null) {
            _localWallets.value = _localWallets.value.map { w ->
                if (w.id == targetWallet.id) w.copy(balance = safeAddBalance(w.balance, -deltaToPay)) else w
            }
            if (deltaToPay > 0L) {
                val statusLabel = when (newStatus) {
                    "PAID" -> if (isId) "Lunas ✅" else "Paid ✅"
                    else -> if (isId) "Cicil Setengah (50%)" else "Half Paid (50%)"
                }
                val newTx = TransactionRecord(
                    id = "${billTxPrefix}${System.currentTimeMillis()}",
                    userId = currentUserId ?: "local_preview",
                    type = "EXPENSE",
                    amount = deltaToPay,
                    categoryKey = "tagihan_bulanan",
                    categoryLabel = billCategoryLabel,
                    frequencyTag = "WAJIB",
                    walletId = targetWallet.id,
                    walletName = targetWallet.name,
                    note = "${bill.name} ($statusLabel • ${bill.dueScheduleLabel})",
                    transactionDateMillis = System.currentTimeMillis()
                )
                _localTransactions.value = listOf(newTx) + _localTransactions.value
                _actionFeedback.value = if (isId) {
                    "${bill.name}: $statusLabel (-${formatMoney(deltaToPay)} dari ${targetWallet.name})"
                } else {
                    "${bill.name}: $statusLabel (-${formatMoney(deltaToPay)} from ${targetWallet.name})"
                }
            } else {
                val refundAmt = -deltaToPay
                val withoutThisBillTxs = _localTransactions.value.filterNot { tx ->
                    tx.id.startsWith(billTxPrefix) ||
                        (tx.id.startsWith("tx_bill_") && tx.note.startsWith(bill.name))
                }
                if (targetPaid > 0L) {
                    val statusLabel = if (isId) "Cicil Setengah (50%)" else "Half Paid (50%)"
                    val adjustedTx = TransactionRecord(
                        id = "${billTxPrefix}${System.currentTimeMillis()}",
                        userId = currentUserId ?: "local_preview",
                        type = "EXPENSE",
                        amount = targetPaid,
                        categoryKey = "tagihan_bulanan",
                        categoryLabel = billCategoryLabel,
                        frequencyTag = "WAJIB",
                        walletId = targetWallet.id,
                        walletName = targetWallet.name,
                        note = "${bill.name} ($statusLabel • ${bill.dueScheduleLabel})",
                        transactionDateMillis = System.currentTimeMillis()
                    )
                    _localTransactions.value = listOf(adjustedTx) + withoutThisBillTxs
                } else {
                    _localTransactions.value = withoutThisBillTxs
                }
                _actionFeedback.value = if (isId) {
                    "Status ${bill.name} diubah ke ${if (newStatus == "UNPAID") "Belum Lunas" else "Setengah"} (+${formatMoney(refundAmt)} kembali ke ${targetWallet.name})"
                } else {
                    "${bill.name} status changed to ${if (newStatus == "UNPAID") "Unpaid" else "Half Paid"} (+${formatMoney(refundAmt)} returned to ${targetWallet.name})"
                }
            }
        } else {
            if (targetPaid == 0L) {
                _localTransactions.value = _localTransactions.value.filterNot { tx ->
                    tx.id.startsWith(billTxPrefix) ||
                        (tx.id.startsWith("tx_bill_") && tx.note.startsWith(bill.name))
                }
            }
            val label = when (newStatus) {
                "PAID" -> if (isId) "Sudah Lunas ✅" else "Paid ✅"
                "HALF_PAID" -> if (isId) "Baru Setengah (${formatMoney(targetPaid)})" else "Half Paid (${formatMoney(targetPaid)})"
                else -> if (isId) "Belum Lunas" else "Unpaid"
            }
            _actionFeedback.value = if (isId) {
                "Tagihan '${bill.name}' ditandai: $label"
            } else {
                "Bill '${bill.name}' marked: $label"
            }
        }
        persistAllLocalData()
    }

    fun upsertMandatoryBill(
        billId: String?,
        name: String,
        totalAmount: Long,
        dueScheduleLabel: String,
        dueDayOfMonth: Int,
        renewalDayOfMonth: Int?,
        billType: String
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (totalAmount <= 0L) {
            _actionFeedback.value = if (isId) {
                "Nominal tagihan harus lebih besar dari ${formatMoney(0L)}"
            } else {
                "Bill amount must be greater than ${formatMoney(0L)}"
            }
            return
        }
        val previousTotalBills = _mandatoryBills.value.sumOf { it.totalAmount }
        val cleanTotal = totalAmount.coerceIn(1L, MAX_MONETARY_AMOUNT)
        val cleanDay = dueDayOfMonth.coerceIn(1, 31)
        val cleanName = name.trim().ifBlank { if (isId) "Tagihan Bulanan" else "Monthly Bill" }
        val cleanSchedule = dueScheduleLabel.trim().ifBlank {
            if (isId) "Bayar setiap tanggal $cleanDay" else "Due every day $cleanDay of month"
        }

        if (billId.isNullOrBlank()) {
            val newBill = MandatoryBillItem(
                id = "bill_${System.currentTimeMillis()}",
                name = cleanName,
                totalAmount = cleanTotal,
                paidAmount = 0L,
                dueScheduleLabel = cleanSchedule,
                dueDayOfMonth = cleanDay,
                renewalDayOfMonth = renewalDayOfMonth?.coerceIn(1, 31),
                status = "UNPAID",
                billType = billType
            )
            _mandatoryBills.value = _mandatoryBills.value + newBill
            _actionFeedback.value = if (isId) {
                "Tagihan '$cleanName' (${formatMoney(cleanTotal)}) ditambahkan ke daftar!"
            } else {
                "Bill '$cleanName' (${formatMoney(cleanTotal)}) added to list!"
            }
        } else {
            _mandatoryBills.value = _mandatoryBills.value.map { existing ->
                if (existing.id == billId) {
                    val adjustedPaid = when (existing.status) {
                        "PAID" -> cleanTotal
                        "HALF_PAID" -> cleanTotal / 2L
                        else -> 0L
                    }
                    existing.copy(
                        name = cleanName,
                        totalAmount = cleanTotal,
                        paidAmount = adjustedPaid,
                        dueScheduleLabel = cleanSchedule,
                        dueDayOfMonth = cleanDay,
                        renewalDayOfMonth = renewalDayOfMonth?.coerceIn(1, 31),
                        billType = billType
                    )
                } else existing
            }
            _actionFeedback.value = if (isId) {
                "Tagihan '$cleanName' berhasil diperbarui!"
            } else {
                "Bill '$cleanName' updated successfully!"
            }
        }
        syncMandatoryBillsLimitIfDefault(previousTotalBills)
        persistAllLocalData()
    }

    fun deleteMandatoryBill(billId: String) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val previousTotalBills = _mandatoryBills.value.sumOf { it.totalAmount }
        val target = _mandatoryBills.value.find { it.id == billId }
        _mandatoryBills.value = _mandatoryBills.value.filterNot { it.id == billId }
        syncMandatoryBillsLimitIfDefault(previousTotalBills)
        persistAllLocalData()
        if (target != null) {
            _actionFeedback.value = if (isId) {
                "Tagihan '${target.name}' dihapus dari daftar"
            } else {
                "Bill '${target.name}' removed from list"
            }
        }
    }

    private fun syncMandatoryBillsLimitIfDefault(previousTotalBills: Long? = null) {
        val totalBills = _mandatoryBills.value.sumOf { it.totalAmount }
        val currentMap = _categoryLimits.value.toMutableMap()
        val existingCfg = currentMap["tagihan_bulanan"]
        if (existingCfg == null) {
            if (totalBills > 0L) {
                currentMap["tagihan_bulanan"] = CategoryLimitConfig("tagihan_bulanan", totalBills, "BULANAN")
                _categoryLimits.value = currentMap
            }
            return
        }
        // Only sync automatically if user did NOT customize tagihan_bulanan limit away from previous bills total
        if (previousTotalBills != null && existingCfg.limitAmount == previousTotalBills) {
            currentMap["tagihan_bulanan"] = existingCfg.copy(limitAmount = totalBills)
            _categoryLimits.value = currentMap
        }
    }

    /**
     * Menerapkan seluruh jawaban kuesioner profil keuangan awal sebelum masuk dashboard,
     * termasuk daftar Bank & E-Wallet yang ditambah/dikurangi pengguna beserta saldonya,
     * daftar tagihan wajib, dan limit fleksibel.
     */
    fun applyFinancialProfileSetup(
        profile: UserFinancialProfile,
        configuredWallets: List<Wallet>,
        configuredBills: List<MandatoryBillItem>,
        customCategoryLimits: Map<String, CategoryLimitConfig>
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val completedProfile = profile.copy(isCompleted = true)
        val updatedSettings = _appSettings.value.copy(hasSeenUserGuide = true)
        _userProfile.value = completedProfile
        _appSettings.value = updatedSettings
        localStore?.markInitialOnboardingSeenOnce()

        if (configuredWallets.isNotEmpty()) {
            _localWallets.value = configuredWallets.map {
                it.copy(balance = it.balance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT))
            }
        }

        _mandatoryBills.value = configuredBills

        val mergedLimits = _categoryLimits.value.toMutableMap()
        customCategoryLimits.forEach { (k, v) -> mergedLimits[k] = v }
        val totalBills = configuredBills.sumOf { it.totalAmount }
        if (!customCategoryLimits.containsKey("tagihan_bulanan")) {
            mergedLimits["tagihan_bulanan"] = CategoryLimitConfig(
                categoryKey = "tagihan_bulanan",
                limitAmount = totalBills,
                period = "BULANAN"
            )
        }
        _categoryLimits.value = mergedLimits
        persistAllLocalData()

        _actionFeedback.value = if (isId) {
            "Profil Keuangan siap! ${configuredWallets.size} Dompet/Bank, Tagihan Wajib & Limit Fleksibel sudah disesuaikan."
        } else {
            "Financial profile ready! ${configuredWallets.size} Accounts, Mandatory Bills & Spending Limits synced."
        }
    }

    fun applyFinancialProfileSetup(
        profile: UserFinancialProfile,
        initialWalletBalances: Map<String, Long>,
        configuredBills: List<MandatoryBillItem>,
        customCategoryLimits: Map<String, CategoryLimitConfig>
    ) {
        val baseList = if (_localWallets.value.isNotEmpty()) _localWallets.value else PresetCategories.defaultStarterWallets
        val updatedWallets = baseList.map { w ->
            w.copy(balance = (initialWalletBalances[w.id] ?: w.balance).coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT))
        }
        applyFinancialProfileSetup(
            profile = profile,
            configuredWallets = updatedWallets,
            configuredBills = configuredBills,
            customCategoryLimits = customCategoryLimits
        )
    }

    fun updateCategoryLimit(
        categoryKey: String,
        categoryLabel: String,
        limitAmount: Long,
        period: String,
        customSubtitle: String = "",
        startDateMillis: Long = 0L,
        endDateMillis: Long = 0L,
        isCustomCategory: Boolean = false
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val cleanLimit = if (period == "BEBAS") 0L else limitAmount.coerceIn(0L, MAX_MONETARY_AMOUNT)
        val cleanStart = if (period == "RENTANG_TANGGAL" || (period == "BULANAN" && startDateMillis > 0L)) startDateMillis else 0L
        val cleanEnd = if (period == "RENTANG_TANGGAL" || (period == "BULANAN" && startDateMillis > 0L)) endDateMillis else 0L
        val existing = _categoryLimits.value[categoryKey]
        val finalIsCustom = isCustomCategory || (existing?.isCustomCategory == true) ||
            PresetCategories.expenseGrid3x3.none { it.key == categoryKey }
        _categoryLimits.value = _categoryLimits.value.toMutableMap().apply {
            put(
                categoryKey,
                CategoryLimitConfig(
                    categoryKey = categoryKey,
                    limitAmount = cleanLimit,
                    period = period,
                    customLabel = categoryLabel.trim(),
                    customSubtitle = customSubtitle.trim().ifBlank { existing?.customSubtitle ?: "" },
                    startDateMillis = cleanStart,
                    endDateMillis = cleanEnd,
                    isCustomCategory = finalIsCustom
                )
            )
        }
        localStore?.saveCategoryLimits(_categoryLimits.value)
        localStore?.notifyWidgetDataChanged()
        val periodLabel = periodDisplayLabel(period)
        _actionFeedback.value = if (cleanLimit > 0L) {
            if (isId) {
                "Kategori '$categoryLabel' disimpan: ${formatMoney(cleanLimit)} ($periodLabel)"
            } else {
                "Category '$categoryLabel' saved: ${formatMoney(cleanLimit)} ($periodLabel)"
            }
        } else {
            if (isId) {
                "Kategori '$categoryLabel' diatur ke Mode Bebas"
            } else {
                "Category '$categoryLabel' set to Unlimited Mode"
            }
        }
    }

    fun deleteCustomCategory(categoryKey: String) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val existing = _categoryLimits.value[categoryKey] ?: return
        if (!existing.isCustomCategory && PresetCategories.expenseGrid3x3.any { it.key == categoryKey }) {
            return
        }
        _categoryLimits.value = _categoryLimits.value.toMutableMap().apply {
            remove(categoryKey)
        }
        localStore?.saveCategoryLimits(_categoryLimits.value)
        localStore?.notifyWidgetDataChanged()
        val label = existing.customLabel.ifBlank { categoryKey }
        _actionFeedback.value = if (isId) {
            "Kategori '$label' dihapus"
        } else {
            "Category '$label' deleted"
        }
    }

    fun updateEmergencyFundAndAllocate(
        sourceWallet: Wallet?,
        addedEmergencyAmount: Long,
        newEmergencyLimit: Long,
        limitPeriod: String,
        storagePlaceNote: String
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val cleanAdd = addedEmergencyAmount.coerceIn(0L, MAX_MONETARY_AMOUNT)
                val cleanLimit = newEmergencyLimit.coerceIn(0L, MAX_MONETARY_AMOUNT)
                val currentCfg = _emergencyFund.value

                if (cleanAdd > 0L && sourceWallet != null) {
                    val liveSource = _localWallets.value.find { it.id == sourceWallet.id } ?: sourceWallet
                    if (currentUserId != null && repository != null) {
                        val newWalletBalance = safeAddBalance(liveSource.balance, -cleanAdd)
                        repository.updateWalletBalance(liveSource, newWalletBalance)
                    } else {
                        _localWallets.value = _localWallets.value.map { w ->
                            if (w.id == liveSource.id) w.copy(balance = safeAddBalance(w.balance, -cleanAdd)) else w
                        }
                    }
                }

                _emergencyFund.value = currentCfg.copy(
                    emergencyBalance = safeAddBalance(currentCfg.emergencyBalance, cleanAdd).coerceAtLeast(0L),
                    emergencyLimit = cleanLimit,
                    limitPeriod = limitPeriod,
                    storagePlaceNote = storagePlaceNote.ifBlank {
                        if (isId) "Rekening / Kantong Terpisah (Jangan dicampur uang harian)"
                        else "Separate Vault / Account (Isolated from daily funds)"
                    }
                )
                persistAllLocalData()

                _actionFeedback.value = if (cleanAdd > 0L && sourceWallet != null) {
                    if (isId) {
                        "${formatMoney(cleanAdd)} dipotong dari ${sourceWallet.name} ke Saldo Darurat Terpisah! (Limit: ${formatMoney(cleanLimit)})"
                    } else {
                        "${formatMoney(cleanAdd)} moved from ${sourceWallet.name} to Emergency Vault! (Limit: ${formatMoney(cleanLimit)})"
                    }
                } else {
                    if (isId) {
                        "Limit Pengeluaran Darurat diperbarui ke ${formatMoney(cleanLimit)} (${periodDisplayLabel(limitPeriod)})"
                    } else {
                        "Emergency Spending Limit updated to ${formatMoney(cleanLimit)} (${periodDisplayLabel(limitPeriod)})"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal memperbarui dana darurat: ${e.localizedMessage}"
                } else {
                    "Failed to update emergency fund: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun seedStarterSetup(
        walletBalances: Map<String, Long>,
        goalTitle: String,
        goalTarget: Long,
        goalDaily: Long,
        goalNote: String
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val safeTarget = goalTarget.coerceIn(0L, MAX_MONETARY_AMOUNT)
                val safeDaily = goalDaily.coerceIn(1L, MAX_MONETARY_AMOUNT)
                if (currentUserId != null && repository != null) {
                    repository.seedStarterSetup(
                        walletBalances = walletBalances,
                        initialGoalTitle = goalTitle,
                        initialGoalTarget = safeTarget,
                        initialGoalDaily = safeDaily,
                        initialGoalNote = goalNote
                    )
                } else {
                    val baseWallets = if (_localWallets.value.isNotEmpty()) _localWallets.value else PresetCategories.defaultStarterWallets
                    _localWallets.value = baseWallets.map { preset ->
                        val nextBal = (walletBalances[preset.id] ?: preset.balance)
                            .coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
                        preset.copy(balance = nextBal)
                    }
                    if (goalTitle.isNotBlank() && safeTarget > 0L) {
                        _localGoals.value = listOf(
                            SavingsGoal(
                                id = "goal_laptop_utama",
                                userId = "local_preview",
                                title = goalTitle.trim(),
                                targetAmount = safeTarget,
                                savedAmount = 0L,
                                dailyRecommendation = safeDaily,
                                note = goalNote.trim(),
                                iconKey = "laptop"
                            )
                        )
                    }
                    persistAllLocalData()
                }
                _actionFeedback.value = if (isId) {
                    "Dompet & Target Impian siap dipantau!"
                } else {
                    "Wallets & Savings Goal ready to track!"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menyimpan pengaturan awal: ${e.localizedMessage}"
                } else {
                    "Failed to save initial setup: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun recordTransaction(
        wallet: Wallet,
        type: String,
        amount: Long,
        categoryKey: String,
        categoryLabel: String,
        frequencyTag: String,
        note: String,
        customDateMillis: Long? = null,
        onComplete: () -> Unit = {}
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (amount !in 1L..MAX_MONETARY_AMOUNT) {
            _actionFeedback.value = if (isId) {
                if (amount <= 0L) "Masukkan nominal lebih dari ${formatMoney(0L)}"
                else "Nominal maksimal transaksi adalah ${formatMoney(MAX_MONETARY_AMOUNT)}"
            } else {
                if (amount <= 0L) "Enter an amount greater than ${formatMoney(0L)}"
                else "Maximum transaction amount is ${formatMoney(MAX_MONETARY_AMOUNT)}"
            }
            return
        }
        val txTimeMillis = customDateMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val isEmergencySpend = categoryKey == "pakai_darurat"
                if (isEmergencySpend) {
                    val currentEm = _emergencyFund.value
                    _emergencyFund.value = currentEm.copy(
                        emergencyBalance = (currentEm.emergencyBalance - amount).coerceAtLeast(0L)
                    )
                    val emWalletName = if (isId) "Saldo Darurat Terpisah" else "Separate Emergency Reserve"
                    val newTx = TransactionRecord(
                        id = "tx_${System.currentTimeMillis()}",
                        userId = currentUserId ?: "local_preview",
                        type = "EXPENSE",
                        amount = amount,
                        categoryKey = categoryKey,
                        categoryLabel = categoryLabel,
                        frequencyTag = "DARURAT",
                        walletId = "pos_darurat_terpisah",
                        walletName = emWalletName,
                        note = note.trim(),
                        transactionDateMillis = txTimeMillis
                    )
                    _localTransactions.value = (listOf(newTx) + _localTransactions.value)
                        .sortedByDescending { it.transactionDateMillis }
                    val formatted = formatMoney(amount)
                    _actionFeedback.value = if (isId) {
                        "-$formatted dipotong dari Saldo Darurat Terpisah"
                    } else {
                        "-$formatted deducted from Separate Emergency Reserve"
                    }
                } else {
                    if (currentUserId != null && repository != null) {
                        repository.recordTransactionAndAdjustWallet(
                            wallet = wallet,
                            type = type,
                            amount = amount,
                            categoryKey = categoryKey,
                            categoryLabel = categoryLabel,
                            frequencyTag = frequencyTag,
                            note = note,
                            transactionDateMillis = txTimeMillis
                        )
                    } else {
                        val delta = if (type == "INCOME") amount else -amount
                        _localWallets.value = _localWallets.value.map { w ->
                            if (w.id == wallet.id) w.copy(balance = safeAddBalance(w.balance, delta)) else w
                        }
                        val newTx = TransactionRecord(
                            id = "tx_${System.currentTimeMillis()}",
                            userId = "local_preview",
                            type = type,
                            amount = amount,
                            categoryKey = categoryKey,
                            categoryLabel = categoryLabel,
                            frequencyTag = frequencyTag,
                            walletId = wallet.id,
                            walletName = wallet.name,
                            note = note.trim(),
                            transactionDateMillis = txTimeMillis
                        )
                        _localTransactions.value = (listOf(newTx) + _localTransactions.value)
                            .sortedByDescending { it.transactionDateMillis }
                    }
                    val formatted = formatMoney(amount)
                    _actionFeedback.value = if (type == "INCOME") {
                        if (isId) "+$formatted masuk ke ${wallet.name} ($categoryLabel)"
                        else "+$formatted added to ${wallet.name} ($categoryLabel)"
                    } else {
                        if (isId) "-$formatted terpotong dari ${wallet.name} untuk $categoryLabel"
                        else "-$formatted deducted from ${wallet.name} for $categoryLabel"
                    }
                }
                persistAllLocalData()
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal mencatat transaksi: ${e.localizedMessage}"
                } else {
                    "Failed to record transaction: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Fitur Real-Life: Pindah Saldo / Top-Up E-Wallet / Tarik Tunai Antar Dompet.
     * Memindahkan saldo dari Bank (mis. BCA) ke E-Wallet (mis. DANA/GoPay/ShopeePay) atau Uang Tunai
     * tanpa membuat pengeluaran harian/bulanan membengkak palsu (kecuali jika ada biaya admin transfer).
     */
    fun transferBetweenWallets(
        fromWallet: Wallet,
        toWallet: Wallet,
        amount: Long,
        adminFee: Long = 0L,
        note: String = ""
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (amount !in 1L..MAX_MONETARY_AMOUNT) {
            _actionFeedback.value = if (isId) {
                if (amount <= 0L) "Nominal pindah saldo harus lebih dari ${formatMoney(0L)}"
                else "Nominal pindah saldo melebihi batas maksimal ${formatMoney(MAX_MONETARY_AMOUNT)}"
            } else {
                if (amount <= 0L) "Transfer amount must be greater than ${formatMoney(0L)}"
                else "Transfer amount exceeds limit ${formatMoney(MAX_MONETARY_AMOUNT)}"
            }
            return
        }
        val liveFrom = _localWallets.value.find { it.id == fromWallet.id } ?: fromWallet
        val liveTo = _localWallets.value.find { it.id == toWallet.id } ?: toWallet
        if (liveFrom.id == liveTo.id) {
            _actionFeedback.value = if (isId) {
                "Pilih dompet tujuan yang berbeda dari dompet asal"
            } else {
                "Please choose a destination account different from the source"
            }
            return
        }
        val cleanFee = adminFee.coerceIn(0L, MAX_MONETARY_AMOUNT)
        val totalBig = java.math.BigInteger.valueOf(amount).add(java.math.BigInteger.valueOf(cleanFee))
        if (totalBig > java.math.BigInteger.valueOf(MAX_MONETARY_AMOUNT)) {
            _actionFeedback.value = if (isId) {
                "Total pindah saldo dan biaya admin melebihi batas maksimal"
            } else {
                "Transfer plus fee exceeds maximum allowed amount"
            }
            return
        }
        val totalDeduct = totalBig.toLong()

        _localWallets.value = _localWallets.value.map { w ->
            when (w.id) {
                liveFrom.id -> w.copy(balance = safeAddBalance(w.balance, -totalDeduct))
                liveTo.id -> w.copy(balance = safeAddBalance(w.balance, amount))
                else -> w
            }
        }

        val now = System.currentTimeMillis()
        val transferTx = TransactionRecord(
            id = "tx_transfer_${liveFrom.id}_to_${liveTo.id}_$now",
            userId = currentUserId ?: "local_preview",
            type = "TRANSFER",
            amount = amount,
            categoryKey = "mutasi_dompet",
            categoryLabel = if (isId) "Pindah Saldo: ${liveFrom.name} → ${liveTo.name}" else "Transfer: ${liveFrom.name} → ${liveTo.name}",
            frequencyTag = "MUTASI",
            walletId = liveFrom.id,
            walletName = "${liveFrom.name} → ${liveTo.name}",
            note = note.trim().ifBlank {
                if (isId) "Top-Up / Pindah saldo ke ${liveTo.name}" else "Transfer to ${liveTo.name}"
            },
            transactionDateMillis = now
        )

        val newTxList = mutableListOf(transferTx)
        if (cleanFee > 0L) {
            val feeTx = TransactionRecord(
                id = "tx_fee_$now",
                userId = currentUserId ?: "local_preview",
                type = "EXPENSE",
                amount = cleanFee,
                categoryKey = "kebutuhan_pokok",
                categoryLabel = if (isId) "Biaya Admin Transfer" else "Transfer Admin Fee",
                frequencyTag = "HARIAN",
                walletId = liveFrom.id,
                walletName = liveFrom.name,
                note = if (isId) "Biaya admin top-up/transfer ke ${liveTo.name}" else "Transfer fee to ${liveTo.name}",
                transactionDateMillis = now + 1L
            )
            newTxList.add(0, feeTx)
        }

        _localTransactions.value = newTxList + _localTransactions.value
        persistAllLocalData()

        val feeSuffix = if (cleanFee > 0L) {
            if (isId) " (+Biaya Admin ${formatMoney(cleanFee)})" else " (+Fee ${formatMoney(cleanFee)})"
        } else {
            if (isId) " (Bebas Biaya Admin)" else " (Zero Fee)"
        }
        _actionFeedback.value = if (isId) {
            "Berhasil pindah ${formatMoney(amount)} dari ${liveFrom.name} ke ${liveTo.name}$feeSuffix"
        } else {
            "Transferred ${formatMoney(amount)} from ${liveFrom.name} to ${liveTo.name}$feeSuffix"
        }
    }

    fun addOrUpdateWallet(
        walletId: String?,
        name: String,
        walletType: String,
        balance: Long,
        colorHex: String,
        iconKey: String
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val cleanName = name.trim().take(60)
        if (cleanName.isBlank()) {
            _actionFeedback.value = if (isId) {
                "Nama dompet tidak boleh kosong"
            } else {
                "Account name cannot be empty"
            }
            return
        }
        val safeType = walletType.uppercase().takeIf { it in setOf("BANK", "EWALLET", "CASH") } ?: "EWALLET"
        val safeBalance = balance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                if (currentUserId != null && repository != null) {
                    repository.upsertWallet(
                        walletId = walletId,
                        name = cleanName,
                        walletType = safeType,
                        balance = safeBalance,
                        colorHex = colorHex,
                        iconKey = iconKey
                    )
                } else {
                    if (walletId.isNullOrBlank()) {
                        val newW = Wallet(
                            id = "wallet_${System.currentTimeMillis()}",
                            userId = "local_preview",
                            name = cleanName,
                            walletType = safeType,
                            balance = safeBalance,
                            colorHex = colorHex,
                            iconKey = iconKey
                        )
                        _localWallets.value = _localWallets.value + newW
                    } else {
                        _localWallets.value = _localWallets.value.map { w ->
                            if (w.id == walletId) {
                                w.copy(
                                    name = cleanName,
                                    walletType = safeType,
                                    balance = safeBalance,
                                    colorHex = colorHex,
                                    iconKey = iconKey
                                )
                            } else w
                        }
                    }
                    persistAllLocalData()
                }
                _actionFeedback.value = if (isId) {
                    "Dompet $cleanName berhasil disimpan (${formatMoney(safeBalance)})"
                } else {
                    "Account $cleanName saved (${formatMoney(safeBalance)})"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menyimpan dompet: ${e.localizedMessage}"
                } else {
                    "Failed to save account: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun adjustWalletBalanceDirectly(wallet: Wallet, newBalance: Long) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val safeBalance = newBalance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
        viewModelScope.launch {
            try {
                if (currentUserId != null && repository != null) {
                    repository.updateWalletBalance(wallet, safeBalance)
                } else {
                    _localWallets.value = _localWallets.value.map { w ->
                        if (w.id == wallet.id) w.copy(balance = safeBalance) else w
                    }
                    persistAllLocalData()
                }
                _actionFeedback.value = if (isId) {
                    "Saldo ${wallet.name} diperbarui menjadi ${formatMoney(safeBalance)}"
                } else {
                    "${wallet.name} balance updated to ${formatMoney(safeBalance)}"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal mengubah saldo: ${e.localizedMessage}"
                } else {
                    "Failed to update balance: ${e.localizedMessage}"
                }
            }
        }
    }

    fun deleteWallet(wallet: Wallet) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        viewModelScope.launch {
            try {
                if (currentUserId == null && _localWallets.value.size <= 1) {
                    _actionFeedback.value = if (isId) {
                        "Minimal harus ada 1 dompet aktif"
                    } else {
                        "At least 1 active account is required"
                    }
                    return@launch
                }
                if (currentUserId != null && repository != null) {
                    repository.deleteWallet(wallet.id)
                } else {
                    _localWallets.value = _localWallets.value.filterNot { it.id == wallet.id }
                    persistAllLocalData()
                }
                _actionFeedback.value = if (isId) {
                    "Dompet ${wallet.name} dihapus"
                } else {
                    "Account ${wallet.name} deleted"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menghapus dompet: ${e.localizedMessage}"
                } else {
                    "Failed to delete account: ${e.localizedMessage}"
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionRecord, wallets: List<Wallet>) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (currentUserId == null || repository == null) {
            if (tx.categoryKey == "pakai_darurat") {
                _localTransactions.value = _localTransactions.value.filterNot { it.id == tx.id }
                val currentEm = _emergencyFund.value
                _emergencyFund.value = currentEm.copy(
                    emergencyBalance = safeAddBalance(currentEm.emergencyBalance, tx.amount).coerceAtLeast(0L)
                )
                persistAllLocalData()
                _actionFeedback.value = if (isId) {
                    "Catatan darurat dihapus & Saldo Darurat Terpisah dikembalikan"
                } else {
                    "Emergency entry deleted & Emergency Reserve restored"
                }
                return
            }

            // 1. Jika transaksi adalah Mutasi / Pindah Saldo antar dompet (TRANSFER)
            if (tx.type == "TRANSFER" && tx.id.startsWith("tx_transfer_")) {
                _localTransactions.value = _localTransactions.value.filterNot { it.id == tx.id }
                val body = tx.id.removePrefix("tx_transfer_")
                val lastUnderscore = body.lastIndexOf('_')
                val idsPart = if (lastUnderscore > 0 && body.substring(lastUnderscore + 1).all { it.isDigit() }) {
                    body.substring(0, lastUnderscore)
                } else {
                    body
                }
                val knownIds = (_localWallets.value.map { it.id } + wallets.map { it.id }).distinct()
                val matchedFromId = knownIds.firstOrNull { fId ->
                    idsPart.startsWith("${fId}_to_") && knownIds.any { tId -> idsPart == "${fId}_to_$tId" }
                } ?: knownIds.firstOrNull { fId -> idsPart.startsWith("${fId}_to_") }
                val fromId: String
                val toId: String
                if (matchedFromId != null) {
                    fromId = matchedFromId
                    toId = idsPart.removePrefix("${matchedFromId}_to_")
                } else {
                    fromId = idsPart.substringBefore("_to_", missingDelimiterValue = tx.walletId)
                    toId = idsPart.substringAfter("_to_", missingDelimiterValue = "")
                }

                _localWallets.value = _localWallets.value.map { w ->
                    when (w.id) {
                        fromId -> w.copy(balance = safeAddBalance(w.balance, tx.amount))
                        toId -> w.copy(balance = safeAddBalance(w.balance, -tx.amount))
                        else -> w
                    }
                }
                persistAllLocalData()
                _actionFeedback.value = if (isId) {
                    "Mutasi saldo dibatalkan & saldo kedua dompet dikembalikan"
                } else {
                    "Transfer reversed & both account balances restored"
                }
                return
            }

            val matchingWallet = wallets.find { it.id == tx.walletId } ?: _localWallets.value.find { it.id == tx.walletId }
            _localTransactions.value = _localTransactions.value.filterNot { it.id == tx.id }
            if (matchingWallet != null) {
                val revertDelta = if (tx.type == "INCOME") -tx.amount else tx.amount
                _localWallets.value = _localWallets.value.map { w ->
                    if (w.id == matchingWallet.id) w.copy(balance = safeAddBalance(w.balance, revertDelta)) else w
                }
            }

            // 2. Jika transaksi adalah alokasi tabungan impian (SAVINGS_ALLOCATION), kurangi juga savedAmount di Goal!
            if (tx.type == "SAVINGS_ALLOCATION") {
                val goalTitleFromLabel = tx.categoryLabel.removePrefix("Tabungan:").removePrefix("Savings:").trim()
                _localGoals.value = _localGoals.value.map { g ->
                    if (g.title.equals(goalTitleFromLabel, ignoreCase = true) || tx.note.contains(g.title, ignoreCase = true)) {
                        g.copy(savedAmount = (g.savedAmount - tx.amount).coerceAtLeast(0L))
                    } else g
                }
            }

            // 3. Jika transaksi berasal dari ceklist Tagihan Wajib (tx_bill_), kembalikan status tagihan wajib!
            if (tx.id.startsWith("tx_bill_")) {
                _mandatoryBills.value = _mandatoryBills.value.map { b ->
                    val matchesBillId = tx.id.startsWith("tx_bill_${b.id}_")
                    val matchesBillName = tx.note.startsWith(b.name)
                    if (matchesBillId || matchesBillName) {
                        val revertedPaid = (b.paidAmount - tx.amount).coerceAtLeast(0L)
                        val revertedStatus = when {
                            revertedPaid <= 0L -> "UNPAID"
                            revertedPaid < b.totalAmount -> "HALF_PAID"
                            else -> "PAID"
                        }
                        b.copy(paidAmount = revertedPaid, status = revertedStatus)
                    } else b
                }
            }
            persistAllLocalData()
            _actionFeedback.value = if (isId) {
                "Catatan dihapus & saldo ${tx.walletName} dikembalikan"
            } else {
                "Entry deleted & ${tx.walletName} balance restored"
            }
            return
        }

        viewModelScope.launch {
            try {
                val matchingWallet = wallets.find { it.id == tx.walletId }
                repository.deleteTransactionAndRevertWallet(tx, matchingWallet)
                _actionFeedback.value = if (isId) {
                    "Catatan dihapus & saldo ${tx.walletName} dikembalikan"
                } else {
                    "Entry deleted & ${tx.walletName} balance restored"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menghapus catatan: ${e.localizedMessage}"
                } else {
                    "Failed to delete entry: ${e.localizedMessage}"
                }
            }
        }
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Long,
        dailyRecommendation: Long,
        note: String,
        iconKey: String,
        startDateMillis: Long = System.currentTimeMillis()
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        val cleanTitle = title.trim().take(80)
        if (cleanTitle.isBlank()) {
            _actionFeedback.value = if (isId) "Nama target impian tidak boleh kosong" else "Goal title cannot be empty"
            return
        }
        if (targetAmount !in 1L..MAX_MONETARY_AMOUNT) {
            _actionFeedback.value = if (isId) {
                "Nominal target harus antara ${formatMoney(1L)} dan ${formatMoney(MAX_MONETARY_AMOUNT)}"
            } else {
                "Target amount must be between ${formatMoney(1L)} and ${formatMoney(MAX_MONETARY_AMOUNT)}"
            }
            return
        }
        val safeDaily = dailyRecommendation.coerceIn(0L, MAX_MONETARY_AMOUNT)
        val safeStart = startDateMillis.takeIf { it > 0L } ?: System.currentTimeMillis()

        if (currentUserId == null || repository == null) {
            val newGoal = SavingsGoal(
                id = "goal_${System.currentTimeMillis()}",
                userId = "local_preview",
                title = cleanTitle,
                targetAmount = targetAmount,
                savedAmount = 0L,
                dailyRecommendation = safeDaily,
                note = note.trim(),
                iconKey = iconKey,
                startDateMillis = safeStart
            )
            _localGoals.value = _localGoals.value + newGoal
            persistAllLocalData()
            _actionFeedback.value = if (isId) {
                "Target '$cleanTitle' (${formatMoney(targetAmount)}) berhasil dibuat!"
            } else {
                "Goal '$cleanTitle' (${formatMoney(targetAmount)}) created!"
            }
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.createSavingsGoal(
                    title = cleanTitle,
                    targetAmount = targetAmount,
                    savedAmount = 0L,
                    dailyRecommendation = safeDaily,
                    note = note,
                    iconKey = iconKey
                )
                _actionFeedback.value = if (isId) {
                    "Target '$cleanTitle' berhasil ditambahkan!"
                } else {
                    "Goal '$cleanTitle' added!"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menambah impian: ${e.localizedMessage}"
                } else {
                    "Failed to add goal: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun updateSavingsGoal(
        goalId: String,
        title: String,
        targetAmount: Long,
        dailyRecommendation: Long,
        startDateMillis: Long,
        note: String = ""
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (targetAmount <= 0L) {
            _actionFeedback.value = if (isId) {
                "Nominal target tabungan harus lebih dari ${formatMoney(0L)}"
            } else {
                "Goal target amount must be greater than ${formatMoney(0L)}"
            }
            return
        }
        val cleanTitle = title.trim().ifBlank { if (isId) "Target Impian" else "Savings Goal" }
        val cleanTarget = targetAmount.coerceIn(1L, MAX_MONETARY_AMOUNT)
        val cleanDaily = dailyRecommendation.coerceIn(1L, MAX_MONETARY_AMOUNT)
        val safeStart = startDateMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        _localGoals.value = _localGoals.value.map { g ->
            if (g.id == goalId) {
                g.copy(
                    title = cleanTitle,
                    targetAmount = cleanTarget,
                    dailyRecommendation = cleanDaily,
                    startDateMillis = safeStart,
                    note = note.trim().ifBlank { g.note }
                )
            } else g
        }
        persistAllLocalData()
        _actionFeedback.value = if (isId) {
            "Target '$cleanTitle' (${formatMoney(cleanTarget)}) berhasil diperbarui!"
        } else {
            "Goal '$cleanTitle' (${formatMoney(cleanTarget)}) updated!"
        }
    }

    fun allocateSavingsToGoal(
        goal: SavingsGoal,
        wallet: Wallet,
        amount: Long,
        note: String
    ) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (amount !in 1L..MAX_MONETARY_AMOUNT) {
            _actionFeedback.value = if (isId) {
                if (amount <= 0L) "Nominal tabungan harus lebih dari ${formatMoney(0L)}"
                else "Nominal tabungan melebihi batas maksimal ${formatMoney(MAX_MONETARY_AMOUNT)}"
            } else {
                if (amount <= 0L) "Deposit amount must be greater than ${formatMoney(0L)}"
                else "Deposit amount exceeds maximum limit ${formatMoney(MAX_MONETARY_AMOUNT)}"
            }
            return
        }
        if (currentUserId == null || repository == null) {
            _localWallets.value = _localWallets.value.map { w ->
                if (w.id == wallet.id) w.copy(balance = safeAddBalance(w.balance, -amount)) else w
            }
            _localGoals.value = _localGoals.value.map { g ->
                if (g.id == goal.id) g.copy(savedAmount = safeAddBalance(g.savedAmount, amount).coerceAtLeast(0L)) else g
            }
            val newTx = TransactionRecord(
                id = "tx_${System.currentTimeMillis()}",
                userId = "local_preview",
                type = "SAVINGS_ALLOCATION",
                amount = amount,
                categoryKey = "tabungan_impian",
                categoryLabel = if (isId) "Tabungan: ${goal.title}" else "Savings: ${goal.title}",
                frequencyTag = "TABUNGAN",
                walletId = wallet.id,
                walletName = wallet.name,
                note = note.trim().ifBlank {
                    if (isId) "Setor tabungan ke ${goal.title}" else "Deposit to ${goal.title}"
                },
                transactionDateMillis = System.currentTimeMillis()
            )
            _localTransactions.value = listOf(newTx) + _localTransactions.value
            persistAllLocalData()
            _actionFeedback.value = if (isId) {
                "Mantap! ${formatMoney(amount)} dari ${wallet.name} masuk ke tabungan ${goal.title}"
            } else {
                "Nice! ${formatMoney(amount)} from ${wallet.name} allocated to ${goal.title}"
            }
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.allocateToGoalFromWallet(
                    goal = goal,
                    wallet = wallet,
                    amount = amount,
                    note = note
                )
                _actionFeedback.value = if (isId) {
                    "Mantap! ${formatMoney(amount)} dari ${wallet.name} masuk ke tabungan ${goal.title}"
                } else {
                    "Nice! ${formatMoney(amount)} from ${wallet.name} allocated to ${goal.title}"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menyetor tabungan: ${e.localizedMessage}"
                } else {
                    "Failed to deposit savings: ${e.localizedMessage}"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        if (currentUserId == null || repository == null) {
            _localGoals.value = _localGoals.value.filterNot { it.id == goal.id }
            persistAllLocalData()
            _actionFeedback.value = if (isId) "Target '${goal.title}' dihapus" else "Goal '${goal.title}' deleted"
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteSavingsGoal(goal.id)
                _actionFeedback.value = if (isId) "Target '${goal.title}' dihapus" else "Goal '${goal.title}' deleted"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionFeedback.value = if (isId) {
                    "Gagal menghapus target: ${e.localizedMessage}"
                } else {
                    "Failed to delete goal: ${e.localizedMessage}"
                }
            }
        }
    }

    fun rotateSessionSeed() {
        _sessionSeed.value = (_sessionSeed.value + 137 + ((System.currentTimeMillis() % 97L).toInt())) % 10000
    }

    fun requestGuruFinancialEvaluation(
        wallets: List<Wallet>,
        transactions: List<TransactionRecord>,
        goals: List<SavingsGoal>
    ) {
        rotateSessionSeed()
        val currentSeed = _sessionSeed.value
        val activeCountry = _appSettings.value.countryCode.ifBlank { "US" }
        val isId = _appSettings.value.language == AppLanguage.INDONESIAN
        viewModelScope.launch {
            _guruState.value = GuruAdviceState.Loading
            try {
                val totalBalance = wallets.sumOf { it.balance }
                val totalGoalSaved = goals.sumOf { it.savedAmount }
                val emergencyBalance = _emergencyFund.value.emergencyBalance
                val walletBreakdown = wallets.joinToString(", ") { "${it.name}: ${formatMoney(it.balance, activeCountry)}" }

                val todayStart = getStartOfDayMillis()
                val monthStart = getStartOfMonthMillis()

                val todayExpenses = transactions.filter { it.type == "EXPENSE" && it.transactionDateMillis >= todayStart }
                val todayExpenseSum = todayExpenses.sumOf { it.amount }
                val monthlyExpenseSum = transactions.filter { it.type == "EXPENSE" && it.transactionDateMillis >= monthStart }.sumOf { it.amount }
                val monthlyIncomeSum = transactions.filter { it.type == "INCOME" && it.transactionDateMillis >= monthStart }.sumOf { it.amount }

                val prompt = if (isId) {
                    """
                        Analisis singkat kondisi keuangan:
                        - Saldo: ${formatMoney(totalBalance, activeCountry)} ($walletBreakdown)
                        - Tabungan Target: ${formatMoney(totalGoalSaved, activeCountry)}
                        - Simpanan Darurat: ${formatMoney(emergencyBalance, activeCountry)}
                        - Pengeluaran Hari Ini: ${formatMoney(todayExpenseSum, activeCountry)}
                        - Pengeluaran Bulan Ini: ${formatMoney(monthlyExpenseSum, activeCountry)} vs Pemasukan: ${formatMoney(monthlyIncomeSum, activeCountry)}
                        Hasilkan JSON sesuai skema dalam Bahasa Indonesia.
                    """.trimIndent()
                } else {
                    """
                        Brief financial evaluation:
                        - Active Balance: ${formatMoney(totalBalance, activeCountry)} ($walletBreakdown)
                        - Goal Savings: ${formatMoney(totalGoalSaved, activeCountry)}
                        - Emergency Reserve: ${formatMoney(emergencyBalance, activeCountry)}
                        - Today's Spending: ${formatMoney(todayExpenseSum, activeCountry)}
                        - Monthly Expense: ${formatMoney(monthlyExpenseSum, activeCountry)} vs Income: ${formatMoney(monthlyIncomeSum, activeCountry)}
                        Respond strictly with JSON matching the schema in English.
                    """.trimIndent()
                }

                val response = withTimeout(30_000L) {
                    guruModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Respons kosong.")
                val json = JSONObject(jsonString)

                val teguranText = json.optString("teguran", "Kontrol arus kas harianmu agar tetap aman.")
                val motivasiText = json.optString("motivasi", "Disiplin kecil setiap hari mempercepat targetmu.")
                val langkahText = json.optString("langkahKonkret", "Pastikan tagihan wajib lunas tepat waktu.")

                _guruState.value = GuruAdviceState.Success(
                    statusBadge = json.optString("statusBadge", "STATUS KE-$currentSeed"),
                    teguran = teguranText,
                    motivasi = motivasiText,
                    langkahKonkret = langkahText,
                    colorLevel = json.optString("colorLevel", "GREEN"),
                    saldoCritique = json.optString("saldoCritique", teguranText),
                    nominalBoxAnalysis = json.optString("nominalBoxAnalysis", "$teguranText $langkahText"),
                    categoryCritique = json.optString("categoryCritique", motivasiText)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Evaluation fallback used: ${e.message}")
                _guruState.value = GuruAdviceState.Error("Mode Evaluasi Lokal Aktif")
            }
        }
    }

    companion object {
        private const val TAG = "ArusKasViewModel"
        const val MAX_MONETARY_AMOUNT = 1_000_000_000_000L

        fun sanitizeMoneyInput(raw: String, allowNegative: Boolean = false): String {
            val trimmed = raw.trim()
            val isNeg = allowNegative && trimmed.startsWith("-")
            val rawDigits = trimmed.filter { it.isDigit() }
            if (rawDigits.isEmpty()) return if (isNeg) "-" else ""
            val stripped = rawDigits.trimStart('0').ifEmpty { "0" }
            val capped = stripped.take(12)
            return if (isNeg && capped != "0") "-$capped" else capped
        }

        fun safeParseMoney(raw: String, allowNegative: Boolean = false): Long {
            val trimmed = raw.trim()
            val isNeg = allowNegative && trimmed.startsWith("-")
            val rawDigits = trimmed.filter { it.isDigit() }
            if (rawDigits.isEmpty()) return 0L
            val magnitude = runCatching { java.math.BigInteger(rawDigits) }.getOrNull() ?: return 0L
            val signed = if (isNeg) magnitude.negate() else magnitude
            val min = if (allowNegative) java.math.BigInteger.valueOf(-MAX_MONETARY_AMOUNT) else java.math.BigInteger.ZERO
            val max = java.math.BigInteger.valueOf(MAX_MONETARY_AMOUNT)
            return signed.coerceIn(min, max).toLong()
        }

        fun safeAddBalance(current: Long, delta: Long): Long {
            val result = java.math.BigInteger.valueOf(current).add(java.math.BigInteger.valueOf(delta))
            val min = java.math.BigInteger.valueOf(-MAX_MONETARY_AMOUNT)
            val max = java.math.BigInteger.valueOf(MAX_MONETARY_AMOUNT)
            return result.coerceIn(min, max).toLong()
        }

        fun formatRupiah(amount: Long): String {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getNumberInstance(localeID)
            return if (amount < 0) {
                "-Rp ${numberFormat.format(-amount)}"
            } else {
                "Rp ${numberFormat.format(amount)}"
            }
        }

        fun formatCompactRupiah(amount: Long): String {
            val abs = kotlin.math.abs(amount)
            val prefix = if (amount < 0) "-Rp " else "Rp "
            return when {
                abs >= 1_000_000_000L -> String.format(Locale("in", "ID"), "%s%.1f M", prefix, abs / 1_000_000_000.0)
                abs >= 1_000_000L -> String.format(Locale("in", "ID"), "%s%.1f Jt", prefix, abs / 1_000_000.0)
                abs >= 1_000L -> String.format(Locale("in", "ID"), "%s%d Rb", prefix, abs / 1_000L)
                else -> "$prefix$abs"
            }
        }

        /**
         * Formats full amount sensitively using the active country's currency symbol and thousands separator.
         */
        fun formatMoney(
            amount: Long,
            countryCode: String = com.example.data.WorldCountryCurrencyRegistry.activeCountryCode
        ): String {
            return com.example.data.WorldCountryCurrencyRegistry.formatCurrency(amount, countryCode)
        }

        /**
         * Formats compact amount sensitively using the active country's currency symbol and language.
         */
        fun formatCompactMoney(
            amount: Long,
            countryCode: String = com.example.data.WorldCountryCurrencyRegistry.activeCountryCode,
            lang: AppLanguage = com.example.data.WorldCountryCurrencyRegistry.activeLanguage
        ): String {
            return com.example.data.WorldCountryCurrencyRegistry.formatCompactCurrency(amount, countryCode, lang)
        }

        fun getStartOfDayMillis(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun getEndOfDayMillis(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

        fun getStartOfWeekMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun getStartOfMonthMillis(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun getEndOfMonthMillis(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

        fun getRemainingDaysInCurrentPeriod(period: String): Int {
            val cal = Calendar.getInstance()
            return when (period) {
                "HARIAN" -> 1
                "MINGGUAN" -> {
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1..7
                    (8 - dayOfWeek).coerceIn(1, 7)
                }
                else -> {
                    val today = cal.get(Calendar.DAY_OF_MONTH)
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    (maxDay - today + 1).coerceAtLeast(1)
                }
            }
        }

        fun calculateFlexibleAdaptiveQuotaPerDay(
            limitAmount: Long,
            spentAmount: Long,
            period: String
        ): Long {
            if (limitAmount <= 0L) return 0L
            val remainingQuota = (limitAmount - spentAmount).coerceAtLeast(0L)
            val remainingDays = getRemainingDaysInCurrentPeriod(period)
            return remainingQuota / remainingDays.toLong()
        }

        fun periodDisplayLabel(
            period: String,
            lang: AppLanguage = com.example.data.WorldCountryCurrencyRegistry.activeLanguage
        ): String {
            val isId = lang == AppLanguage.INDONESIAN
            return when (period) {
                "HARIAN" -> if (isId) "Per Hari" else "Daily"
                "MINGGUAN" -> if (isId) "Per Minggu" else "Weekly"
                "RENTANG_TANGGAL" -> if (isId) "Rentang Kalender" else "Custom Date Range"
                "BEBAS" -> if (isId) "Bebas Fleksibel" else "No Cap (Free)"
                else -> if (isId) "Per Bulan" else "Monthly"
            }
        }

        fun calculateSpentForCategoryLimit(
            categoryKey: String,
            period: String,
            transactions: List<TransactionRecord>,
            mandatoryBills: List<MandatoryBillItem> = emptyList(),
            startDateMillis: Long = 0L,
            endDateMillis: Long = 0L
        ): Long {
            val (startMillis, endMillis) = when (period) {
                "HARIAN" -> getStartOfDayMillis() to getEndOfDayMillis()
                "MINGGUAN" -> getStartOfWeekMillis() to getEndOfDayMillis()
                "BEBAS" -> 0L to Long.MAX_VALUE
                "RENTANG_TANGGAL" -> {
                    val s = if (startDateMillis > 0L) startDateMillis else getStartOfMonthMillis()
                    val e = if (endDateMillis > 0L) endDateMillis else Long.MAX_VALUE
                    s to e
                }
                else -> {
                    if (startDateMillis > 0L) {
                        val e = if (endDateMillis > 0L) endDateMillis else Long.MAX_VALUE
                        startDateMillis to e
                    } else {
                        getStartOfMonthMillis() to getEndOfMonthMillis()
                    }
                }
            }

            if (categoryKey == "tagihan_bulanan" && mandatoryBills.isNotEmpty()) {
                val useTransactionWindowOnly = period == "HARIAN" ||
                    period == "MINGGUAN" ||
                    period == "RENTANG_TANGGAL" ||
                    (period == "BULANAN" && startDateMillis > 0L)
                if (useTransactionWindowOnly) {
                    return transactions
                        .filter {
                            it.type == "EXPENSE" &&
                                it.categoryKey == "tagihan_bulanan" &&
                                it.transactionDateMillis in startMillis..endMillis
                        }
                        .sumOf { it.amount }
                }
                val paidFromChecklist = mandatoryBills.sumOf { it.paidAmount }
                val manualExtraTx = transactions
                    .filter {
                        it.type == "EXPENSE" &&
                            it.categoryKey == "tagihan_bulanan" &&
                            it.transactionDateMillis in startMillis..endMillis &&
                            !it.id.startsWith("tx_bill_")
                    }
                    .sumOf { it.amount }
                return paidFromChecklist + manualExtraTx
            }

            return transactions
                .filter {
                    it.type == "EXPENSE" &&
                        it.categoryKey == categoryKey &&
                        it.transactionDateMillis in startMillis..endMillis
                }
                .sumOf { it.amount }
        }
    }
}
