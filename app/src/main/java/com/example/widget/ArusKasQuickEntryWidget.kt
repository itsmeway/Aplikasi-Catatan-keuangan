package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.AppLanguage
import com.example.data.ArusKasLocalStore
import com.example.data.DailyRealLifeQuickItem
import com.example.data.PresetCategories
import com.example.data.QuickCategoryItem
import com.example.data.TransactionRecord
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.normalizeFormalCategoryLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WidgetPresetAmount(
    val amount: Long,
    val label: String
)

data class WidgetLoadedData(
    val language: AppLanguage,
    val countryCode: String,
    val currencyCode: String,
    val currencySymbol: String,
    val userGender: String,
    val emergencyBalance: Long,
    val wallets: List<Wallet>,
    val expenseCategories: List<QuickCategoryItem>,
    val incomeCategories: List<QuickCategoryItem>
)

data class WidgetSaveResult(
    val success: Boolean,
    val feedbackMessage: String,
    val recordedTransaction: TransactionRecord? = null,
    val updatedWalletBalance: Long? = null
)

/**
 * Engine lokal untuk Home Screen Widget ArusKas.
 * Diselaraskan penuh dengan BottomPopupNominalInputSheet di dalam aplikasi
 * (1. Jenis Transaksi, 2. Tanggal Kemarin/Hari Ini/Besok, 3. Kategori Formal & Darurat,
 * 4. Pintasan Item Cepat + Kotak Nominal + Keypad langsung, 5. Sumber Dompet + Saldo,
 * 6. Tombol Simpan Transaksi), sekaligus patuh pada batas tata letak Glance (maks 10 child/container).
 */
object ArusKasWidgetEngine {

    private const val MAX_AMOUNT_DIGITS = 11 // Up to 99,999,999,999
    private const val DAY_MILLIS = 86_400_000L

    fun syncLocaleFromSettings(store: ArusKasLocalStore): Pair<AppLanguage, String> {
        val settings = store.loadAppSettings()
        val cCode = settings.countryCode.ifBlank { "ID" }
        WorldCountryCurrencyRegistry.activeLanguage = settings.language
        WorldCountryCurrencyRegistry.activeCountryCode = cCode
        return settings.language to cCode
    }

    fun loadWidgetData(context: Context): WidgetLoadedData {
        val store = ArusKasLocalStore(context)
        val (lang, cCode) = syncLocaleFromSettings(store)
        val countryProfile = WorldCountryCurrencyRegistry.byCountryCode(cCode)
        val userProfile = store.loadUserProfile()
        val wallets = store.loadWallets()
        val limits = store.loadCategoryLimits()
        val emergencyFund = store.loadEmergencyFund()

        val baseExp = PresetCategories.localizedExpenseCategories(lang).map { cat ->
            cat.copy(label = normalizeFormalCategoryLabel(cat.key, cat.label, lang))
        }
        val baseKeys = baseExp.map { it.key }.toSet()
        val customExp = limits.values
            .filter { it.categoryKey !in baseKeys && it.categoryKey != "pakai_darurat" }
            .map { cfg ->
                val rawLabel = cfg.customLabel.ifBlank {
                    if (lang == AppLanguage.INDONESIAN) "Kategori Baru" else "Custom Category"
                }
                QuickCategoryItem(
                    key = cfg.categoryKey,
                    label = normalizeFormalCategoryLabel(cfg.categoryKey, rawLabel, lang),
                    subtitle = cfg.customSubtitle,
                    frequencyTag = "FLEKSIBEL",
                    type = "EXPENSE",
                    defaultNoteHint = "",
                    defaultLimitAmount = cfg.limitAmount,
                    defaultLimitPeriod = cfg.period
                )
            }
        val rawEmCat = PresetCategories.localizedEmergencyCategory(lang)
        val emergencyCat = rawEmCat.copy(
            label = normalizeFormalCategoryLabel(rawEmCat.key, rawEmCat.label, lang)
        )
        val expenseCategories = baseExp + customExp + emergencyCat
        val incomeCategories = PresetCategories.localizedIncomePills(lang)

        return WidgetLoadedData(
            language = lang,
            countryCode = cCode,
            currencyCode = countryProfile.currencyCode,
            currencySymbol = countryProfile.currencySymbol,
            userGender = userProfile.gender,
            emergencyBalance = emergencyFund.emergencyBalance,
            wallets = wallets,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories
        )
    }

    fun resolveQuickItemsForCategory(
        countryCode: String,
        lang: AppLanguage,
        categoryKey: String,
        txType: String,
        userGender: String
    ): List<DailyRealLifeQuickItem> {
        if (txType == "INCOME" || categoryKey == "pakai_darurat") return emptyList()
        return WorldCountryCurrencyRegistry.quickItemsForCountry(countryCode, lang).filter { item ->
            item.categoryKey == categoryKey &&
                (item.genderFilter == "ALL" || item.genderFilter == userGender)
        }
    }

    fun quickPresetsForCountry(countryCode: String, lang: AppLanguage): List<WidgetPresetAmount> {
        val isLargeUnit = countryCode.uppercase() in setOf("ID", "VN", "KR", "JP")
        val isId = lang == AppLanguage.INDONESIAN
        return if (isLargeUnit) {
            val kSuffix = if (isId) "rb" else "k"
            listOf(
                WidgetPresetAmount(10_000L, "+10$kSuffix"),
                WidgetPresetAmount(25_000L, "+25$kSuffix"),
                WidgetPresetAmount(50_000L, "+50$kSuffix"),
                WidgetPresetAmount(100_000L, "+100$kSuffix")
            )
        } else {
            listOf(
                WidgetPresetAmount(5L, "+5"),
                WidgetPresetAmount(10L, "+10"),
                WidgetPresetAmount(25L, "+25"),
                WidgetPresetAmount(50L, "+50")
            )
        }
    }

    fun multiZeroTokenForCountry(countryCode: String): String {
        return if (countryCode.uppercase() in setOf("ID", "VN", "KR", "JP")) "000" else "00"
    }

    fun effectiveDateMillisForOffset(dayOffset: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        return when (dayOffset) {
            -1 -> nowMillis - DAY_MILLIS
            1 -> nowMillis + DAY_MILLIS
            else -> nowMillis
        }
    }

    fun applyKeypadInput(currentDigits: String, token: String): String {
        val cleanCurrent = currentDigits.filter { it.isDigit() }
        return when (token) {
            "CLEAR" -> ""
            "BACKSPACE" -> if (cleanCurrent.isNotEmpty()) cleanCurrent.dropLast(1) else ""
            "0", "00", "000" -> {
                if (cleanCurrent.isEmpty() || cleanCurrent == "0") {
                    ""
                } else {
                    val combined = cleanCurrent + token
                    if (combined.length <= MAX_AMOUNT_DIGITS) combined else cleanCurrent
                }
            }
            else -> {
                val cleanToken = token.filter { it.isDigit() }
                if (cleanToken.isEmpty()) return cleanCurrent
                val base = if (cleanCurrent == "0") "" else cleanCurrent
                val combined = base + cleanToken
                if (combined.length <= MAX_AMOUNT_DIGITS) combined else cleanCurrent
            }
        }
    }

    fun addPresetAmount(currentDigits: String, deltaAmount: Long): String {
        val currentVal = currentDigits.toLongOrNull() ?: 0L
        val nextVal = (currentVal + deltaAmount).coerceAtMost(99_999_999_999L)
        return if (nextVal > 0L) nextVal.toString() else ""
    }

    fun parseWalletHexColor(hex: String): Color {
        return runCatching {
            Color(android.graphics.Color.parseColor(hex.trim()))
        }.getOrDefault(Color(0xFF0066AE))
    }

    fun recordTransactionFromWidget(
        context: Context,
        txType: String,
        amount: Long,
        categoryIndex: Int,
        walletIndex: Int,
        dayOffset: Int = 0,
        note: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): WidgetSaveResult {
        val store = ArusKasLocalStore(context)
        val (lang, cCode) = syncLocaleFromSettings(store)
        val isId = lang == AppLanguage.INDONESIAN

        if (amount <= 0L) {
            return WidgetSaveResult(
                success = false,
                feedbackMessage = if (isId) "Masukkan nominal > 0" else "Enter amount > 0"
            )
        }

        val data = loadWidgetData(context)
        val normalizedType = if (txType == "INCOME") "INCOME" else "EXPENSE"
        val categories = if (normalizedType == "INCOME") data.incomeCategories else data.expenseCategories
        if (categories.isEmpty()) {
            return WidgetSaveResult(
                success = false,
                feedbackMessage = if (isId) "Kategori belum tersedia" else "No categories available"
            )
        }
        val chosenCat = categories[categoryIndex.floorMod(categories.size)]
        val formalCatLabel = normalizeFormalCategoryLabel(chosenCat.key, chosenCat.label, lang)
        val resolvedNote = note.trim().ifBlank { formalCatLabel }
        val txDateMillis = effectiveDateMillisForOffset(dayOffset, nowMillis)
        val isEmergencySpend = normalizedType == "EXPENSE" && chosenCat.key == "pakai_darurat"
        val formattedMoney = WorldCountryCurrencyRegistry.formatMoney(amount, cCode)

        if (isEmergencySpend) {
            val currentEm = store.loadEmergencyFund()
            val nextEmBalance = (currentEm.emergencyBalance - amount).coerceAtLeast(0L)
            store.saveEmergencyFund(currentEm.copy(emergencyBalance = nextEmBalance))

            val existingTxs = store.loadTransactions()
            val newTx = TransactionRecord(
                id = "tx_w_$nowMillis",
                userId = "local_preview",
                type = "EXPENSE",
                amount = amount,
                categoryKey = chosenCat.key,
                categoryLabel = formalCatLabel,
                frequencyTag = "DARURAT",
                walletId = "pos_darurat_terpisah",
                walletName = if (isId) "Saldo Darurat Terpisah" else "Separate Emergency Fund",
                note = resolvedNote,
                transactionDateMillis = txDateMillis
            )
            store.saveTransactions((listOf(newTx) + existingTxs).sortedByDescending { it.transactionDateMillis })

            val msg = if (isId) {
                "✓ -$formattedMoney • Saldo Darurat"
            } else {
                "✓ -$formattedMoney • Emergency Fund"
            }
            return WidgetSaveResult(
                success = true,
                feedbackMessage = msg,
                recordedTransaction = newTx,
                updatedWalletBalance = nextEmBalance
            )
        }

        val wallets = data.wallets
        if (wallets.isEmpty()) {
            return WidgetSaveResult(
                success = false,
                feedbackMessage = if (isId) "Belum ada dompet aktif" else "No active wallet"
            )
        }

        val targetWallet = wallets[walletIndex.floorMod(wallets.size)]
        val delta = if (normalizedType == "INCOME") amount else -amount
        val newBalance = targetWallet.balance + delta

        val updatedWallets = wallets.map { w ->
            if (w.id == targetWallet.id) w.copy(balance = newBalance) else w
        }
        store.saveWallets(updatedWallets)

        val existingTxs = store.loadTransactions()
        val newTx = TransactionRecord(
            id = "tx_w_$nowMillis",
            userId = "local_preview",
            type = normalizedType,
            amount = amount,
            categoryKey = chosenCat.key,
            categoryLabel = formalCatLabel,
            frequencyTag = chosenCat.frequencyTag,
            walletId = targetWallet.id,
            walletName = targetWallet.name,
            note = resolvedNote,
            transactionDateMillis = txDateMillis
        )
        store.saveTransactions((listOf(newTx) + existingTxs).sortedByDescending { it.transactionDateMillis })

        val sign = if (normalizedType == "INCOME") "+" else "-"
        val feedback = "✓ $sign$formattedMoney • ${targetWallet.name}"
        return WidgetSaveResult(
            success = true,
            feedbackMessage = feedback,
            recordedTransaction = newTx,
            updatedWalletBalance = newBalance
        )
    }

    internal fun Int.floorMod(size: Int): Int {
        if (size <= 0) return 0
        val m = this % size
        return if (m < 0) m + size else m
    }
}

class ArusKasQuickEntryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArusKasQuickEntryWidget()
}

class ArusKasQuickEntryWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_BUCKETS)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            @Suppress("UNUSED_VARIABLE")
            val revision = prefs[KEY_REVISION] ?: 0
            val loadedData = ArusKasWidgetEngine.loadWidgetData(context)

            val txType = prefs[KEY_TX_TYPE] ?: "EXPENSE"
            val dayOffset = prefs[KEY_DAY_OFFSET] ?: 0
            val amountDigits = prefs[KEY_AMOUNT_DIGITS] ?: ""
            val activeNote = prefs[KEY_NOTE] ?: ""
            val catIndex = prefs[KEY_CATEGORY_INDEX] ?: 0
            val walletIndex = prefs[KEY_WALLET_INDEX] ?: 0
            val quickItemPage = prefs[KEY_QUICK_ITEM_PAGE] ?: 0
            val feedbackMsg = prefs[KEY_FEEDBACK] ?: ""

            GlanceTheme {
                QuickTransactionWidgetContent(
                    loadedData = loadedData,
                    txType = txType,
                    dayOffset = dayOffset,
                    amountDigits = amountDigits,
                    activeNote = activeNote,
                    categoryIndex = catIndex,
                    walletIndex = walletIndex,
                    quickItemPage = quickItemPage,
                    feedbackMessage = feedbackMsg
                )
            }
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        val fallback = RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "ArusKas • Catat Uang Masuk / Keluar")
        }
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
    }

    companion object {
        val KEY_TX_TYPE = stringPreferencesKey("widget_tx_type")
        val KEY_DAY_OFFSET = intPreferencesKey("widget_day_offset")
        val KEY_AMOUNT_DIGITS = stringPreferencesKey("widget_amount_digits")
        val KEY_NOTE = stringPreferencesKey("widget_note")
        val KEY_CATEGORY_INDEX = intPreferencesKey("widget_category_index")
        val KEY_WALLET_INDEX = intPreferencesKey("widget_wallet_index")
        val KEY_QUICK_ITEM_PAGE = intPreferencesKey("widget_quick_item_page")
        val KEY_FEEDBACK = stringPreferencesKey("widget_feedback")
        val KEY_REVISION = intPreferencesKey("widget_revision")

        private val RESPONSIVE_BUCKETS = setOf(
            DpSize(220.dp, 160.dp),
            DpSize(250.dp, 250.dp),
            DpSize(280.dp, 315.dp),
            DpSize(300.dp, 380.dp)
        )

        fun notifyDataChangedAsync(context: Context) {
            val appCtx = context.applicationContext
            CoroutineScope(Dispatchers.Default).launch {
                runCatching {
                    val manager = GlanceAppWidgetManager(appCtx)
                    val glanceIds = manager.getGlanceIds(ArusKasQuickEntryWidget::class.java)
                    val widget = ArusKasQuickEntryWidget()
                    for (gId in glanceIds) {
                        updateAppWidgetState(appCtx, gId) { prefs ->
                            prefs[KEY_REVISION] = (prefs[KEY_REVISION] ?: 0) + 1
                        }
                        widget.update(appCtx, gId)
                    }
                }
            }
        }
    }
}

/**
 * Tata letak disusun ke dalam tepat 7 sub-blok terkelompok pada Column utama
 * agar tidak pernah melewati batas maksimal 10 child/container milik Jetpack Glance,
 * sekaligus meniru struktur 1..6 milik BottomPopupNominalInputSheet secara presisi.
 */
@Composable
private fun QuickTransactionWidgetContent(
    loadedData: WidgetLoadedData,
    txType: String,
    dayOffset: Int,
    amountDigits: String,
    activeNote: String,
    categoryIndex: Int,
    walletIndex: Int,
    quickItemPage: Int,
    feedbackMessage: String
) {
    val size = LocalSize.current
    val isCompactHeight = size.height < 235.dp
    val isTallLayout = size.height >= 305.dp
    val isUltraTall = size.height >= 365.dp
    val isId = loadedData.language == AppLanguage.INDONESIAN
    val isIncome = txType == "INCOME"

    val categories = if (isIncome) loadedData.incomeCategories else loadedData.expenseCategories
    val safeCatIdx = if (categories.isNotEmpty()) {
        with(ArusKasWidgetEngine) { categoryIndex.floorMod(categories.size) }
    } else 0
    val selectedCategory = categories.getOrNull(safeCatIdx)
    val isEmergencyExpense = !isIncome && selectedCategory?.key == "pakai_darurat"

    val wallets = loadedData.wallets
    val safeWalletIdx = if (wallets.isNotEmpty()) {
        with(ArusKasWidgetEngine) { walletIndex.floorMod(wallets.size) }
    } else 0

    val typedAmount = amountDigits.toLongOrNull() ?: 0L
    val formattedNominal = WorldCountryCurrencyRegistry.formatMoney(typedAmount, loadedData.countryCode)

    // Palet warna persis Theme.kt & BottomPopupNominalInputSheet
    val bgColor = DayNightColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF0E1B2E))
    val segmentedBgColor = DayNightColorProvider(day = Color(0xFFEFF4FA), night = Color(0xFF172840))
    val chipUnselectedBg = DayNightColorProvider(day = Color(0xFFF1F5F9), night = Color(0xFF1C304D))
    val chipOutlineColor = DayNightColorProvider(day = Color(0xFFCBD5E1), night = Color(0xFF334155))
    val keyBgColor = DayNightColorProvider(day = Color(0xFFEEF4FB), night = Color(0xFF1F3452))
    val inputSurfaceBg = DayNightColorProvider(day = Color(0xFFF8FAFC), night = Color(0xFF132238))
    val primaryTextColor = DayNightColorProvider(day = Color(0xFF0F2942), night = Color(0xFFF8FAFC))
    val mutedTextColor = DayNightColorProvider(day = Color(0xFF526377), night = Color(0xFFA3B4C8))
    val royalBlueColor = DayNightColorProvider(day = Color(0xFF0066AE), night = Color(0xFF60A5FA))
    val royalBlueSoftBg = DayNightColorProvider(day = Color(0xFFDCECFB), night = Color(0xFF163559))
    val emeraldColor = DayNightColorProvider(day = Color(0xFF00A86B), night = Color(0xFF34D399))
    val emeraldSoftBg = DayNightColorProvider(day = Color(0xFFD6F4E8), night = Color(0xFF0D3D2B))
    val coralColor = DayNightColorProvider(day = Color(0xFFD9383A), night = Color(0xFFF87171))
    val coralSoftBg = DayNightColorProvider(day = Color(0xFFFCE2E2), night = Color(0xFF471C20))
    val goldColor = DayNightColorProvider(day = Color(0xFFD97706), night = Color(0xFFFBBF24))
    val goldSoftBg = DayNightColorProvider(day = Color(0xFFFEF0D1), night = Color(0xFF422F0B))

    val activeAccent = when {
        isIncome -> emeraldColor
        isEmergencyExpense -> goldColor
        else -> coralColor
    }
    val activeSoftBg = when {
        isIncome -> emeraldSoftBg
        isEmergencyExpense -> goldSoftBg
        else -> coralSoftBg
    }
    val whiteColor = ColorProvider(Color.White)
    val transparentColor = ColorProvider(Color.Transparent)

    val quickItems = selectedCategory?.let {
        ArusKasWidgetEngine.resolveQuickItemsForCategory(
            countryCode = loadedData.countryCode,
            lang = loadedData.language,
            categoryKey = it.key,
            txType = txType,
            userGender = loadedData.userGender
        )
    } ?: emptyList()
    val presets = ArusKasWidgetEngine.quickPresetsForCountry(loadedData.countryCode, loadedData.language)
    val zeroZeroToken = ArusKasWidgetEngine.multiZeroTokenForCountry(loadedData.countryCode)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .cornerRadius(22.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        // [BLOK 1] HEADER: "Catat Uang Masuk / Keluar" + Feedback + Tombol Buka App
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isId) "Catat Uang Masuk / Keluar" else "Record Transaction",
                style = TextStyle(
                    color = primaryTextColor,
                    fontSize = if (isCompactHeight) 12.5.sp else 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            if (feedbackMessage.isNotBlank()) {
                val fbColor = if (feedbackMessage.startsWith("✓")) emeraldColor else coralColor
                val fbBg = if (feedbackMessage.startsWith("✓")) emeraldSoftBg else coralSoftBg
                Box(
                    modifier = GlanceModifier
                        .background(fbBg)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = feedbackMessage,
                        style = TextStyle(
                            color = fbColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.width(4.dp))
            }
            Box(
                modifier = GlanceModifier
                    .background(chipUnselectedBg)
                    .cornerRadius(8.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↗",
                    style = TextStyle(
                        color = royalBlueColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // [BLOK 2] 1. PILIH JENIS TRANSAKSI (Pengeluaran | Pemasukan)
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
            if (!isCompactHeight) {
                Text(
                    text = if (isId) "1. Pilih Jenis Transaksi" else "1. Transaction Type",
                    style = TextStyle(
                        color = mutedTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(bottom = 2.dp)
                )
            }
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(segmentedBgColor)
                    .cornerRadius(12.dp)
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(if (!isIncome) coralColor else transparentColor)
                        .cornerRadius(9.dp)
                        .clickable(
                            actionRunCallback<WidgetSelectTypeAction>(
                                actionParametersOf(WidgetSelectTypeAction.KEY_TX_TYPE to "EXPENSE")
                            )
                        )
                        .padding(vertical = if (isCompactHeight) 4.dp else 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isId) "↘ Pengeluaran" else "↘ Expense",
                        style = TextStyle(
                            color = if (!isIncome) whiteColor else mutedTextColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.width(4.dp))
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(if (isIncome) emeraldColor else transparentColor)
                        .cornerRadius(9.dp)
                        .clickable(
                            actionRunCallback<WidgetSelectTypeAction>(
                                actionParametersOf(WidgetSelectTypeAction.KEY_TX_TYPE to "INCOME")
                            )
                        )
                        .padding(vertical = if (isCompactHeight) 4.dp else 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isId) "↗ Pemasukan" else "↗ Income",
                        style = TextStyle(
                            color = if (isIncome) whiteColor else mutedTextColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // [BLOK 3] 2. TANGGAL (Kemarin | Hari Ini | Besok)
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isId) "2. Tanggal" else "2. Date",
                    style = TextStyle(
                        color = mutedTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(end = 6.dp)
                )
                val dateOptions = listOf(
                    -1 to (if (isId) "Kemarin" else "Yesterday"),
                    0 to (if (isId) "Hari Ini" else "Today"),
                    1 to (if (isId) "Besok" else "Tomorrow")
                )
                dateOptions.forEachIndexed { idx, (offset, label) ->
                    val isSel = dayOffset == offset
                    OutlinedPillChip(
                        text = label,
                        isSelected = isSel,
                        selectedBorderColor = royalBlueColor,
                        selectedFillColor = royalBlueSoftBg,
                        selectedTextColor = royalBlueColor,
                        unselectedBorderColor = chipOutlineColor,
                        unselectedFillColor = chipUnselectedBg,
                        unselectedTextColor = mutedTextColor,
                        cornerRadius = 10.dp,
                        verticalPad = 3.dp,
                        fontSizeSp = 10.5f,
                        modifier = GlanceModifier.defaultWeight().let { m ->
                            if (idx > 0) m.padding(start = 3.dp) else m
                        },
                        onClick = actionRunCallback<WidgetSelectDateAction>(
                            actionParametersOf(WidgetSelectDateAction.KEY_DAY_OFFSET to offset)
                        )
                    )
                }
            }
        }

        // [BLOK 4] 3. PILIH KATEGORI (Halaman tetap 2x2 di mode tinggi atau 1x2 di mode ringkas - anti loncat!)
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
            val pageSize = if (isTallLayout) 4 else 2
            val totalCats = categories.size
            val totalPages = if (totalCats == 0) 1 else ((totalCats + pageSize - 1) / pageSize)
            val currentPage = if (totalCats == 0) 0 else (safeCatIdx / pageSize)
            val pageStart = currentPage * pageSize
            val visibleCats = if (totalCats == 0) {
                emptyList()
            } else {
                categories.subList(pageStart, minOf(pageStart + pageSize, totalCats))
            }

            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isId) "3. Pilih Kategori" else "3. Category",
                    style = TextStyle(
                        color = mutedTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                if (totalPages > 1) {
                    Text(
                        text = "${currentPage + 1}/$totalPages",
                        style = TextStyle(
                            color = mutedTextColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(end = 4.dp)
                    )
                    SmallArrowButton(
                        symbol = "‹",
                        bgColor = chipUnselectedBg,
                        textColor = mutedTextColor,
                        onClick = actionRunCallback<WidgetPageCategoryAction>(
                            actionParametersOf(
                                WidgetPageCategoryAction.KEY_STEP to -1,
                                WidgetPageCategoryAction.KEY_PAGE_SIZE to pageSize
                            )
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(3.dp))
                    SmallArrowButton(
                        symbol = "›",
                        bgColor = chipUnselectedBg,
                        textColor = mutedTextColor,
                        onClick = actionRunCallback<WidgetPageCategoryAction>(
                            actionParametersOf(
                                WidgetPageCategoryAction.KEY_STEP to 1,
                                WidgetPageCategoryAction.KEY_PAGE_SIZE to pageSize
                            )
                        )
                    )
                }
            }

            val catRows = visibleCats.chunked(2)
            catRows.forEachIndexed { rowIdx, rowItems ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().let { m ->
                        if (rowIdx > 0) m.padding(top = 3.dp) else m
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEachIndexed { colIdx, cat ->
                        val absIdx = pageStart + rowIdx * 2 + colIdx
                        val isSel = absIdx == safeCatIdx
                        val itemAccent = when {
                            isIncome -> emeraldColor
                            cat.key == "pakai_darurat" -> goldColor
                            else -> coralColor
                        }
                        val itemSoftBg = when {
                            isIncome -> emeraldSoftBg
                            cat.key == "pakai_darurat" -> goldSoftBg
                            else -> coralSoftBg
                        }
                        OutlinedPillChip(
                            text = cat.label,
                            isSelected = isSel,
                            selectedBorderColor = itemAccent,
                            selectedFillColor = itemSoftBg,
                            selectedTextColor = itemAccent,
                            unselectedBorderColor = chipOutlineColor,
                            unselectedFillColor = chipUnselectedBg,
                            unselectedTextColor = primaryTextColor,
                            cornerRadius = 9.dp,
                            verticalPad = 3.5.dp,
                            fontSizeSp = 10.5f,
                            modifier = GlanceModifier.defaultWeight().let { m ->
                                if (colIdx > 0) m.padding(start = 4.dp) else m
                            },
                            onClick = actionRunCallback<WidgetSelectCategoryAction>(
                                actionParametersOf(WidgetSelectCategoryAction.KEY_CATEGORY_INDEX to absIdx)
                            )
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Box(modifier = GlanceModifier.defaultWeight()) {}
                    }
                }
            }
        }

        // [BLOK 5] 4. MASUKKAN JUMLAH UANG (Pintasan Item Cepat + Kotak Outlined Nominal + Keypad)
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
            if (!isCompactHeight) {
                Text(
                    text = if (isId) "4. Masukkan Jumlah Uang" else "4. Amount",
                    style = TextStyle(
                        color = mutedTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(bottom = 2.dp)
                )
            }

            // Pintasan Item Cepat negara sesuai kategori (atau preset angka untuk Income/Darurat)
            if (quickItems.isNotEmpty()) {
                val itemCount = quickItems.size
                val idx0 = with(ArusKasWidgetEngine) { quickItemPage.floorMod(itemCount) }
                val idx1 = (idx0 + 1) % itemCount
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val q0 = quickItems[idx0]
                    val q0Price = WorldCountryCurrencyRegistry.formatCompactCurrency(
                        q0.suggestedAmount,
                        loadedData.countryCode,
                        loadedData.language
                    )
                    OutlinedPillChip(
                        text = "${q0.label} • $q0Price",
                        isSelected = activeNote == q0.label,
                        selectedBorderColor = activeAccent,
                        selectedFillColor = activeSoftBg,
                        selectedTextColor = activeAccent,
                        unselectedBorderColor = chipOutlineColor,
                        unselectedFillColor = chipUnselectedBg,
                        unselectedTextColor = primaryTextColor,
                        cornerRadius = 8.dp,
                        verticalPad = 2.5.dp,
                        fontSizeSp = 10f,
                        modifier = GlanceModifier.defaultWeight(),
                        onClick = actionRunCallback<WidgetSelectQuickItemAction>(
                            actionParametersOf(
                                WidgetSelectQuickItemAction.KEY_AMOUNT to q0.suggestedAmount,
                                WidgetSelectQuickItemAction.KEY_LABEL to q0.label
                            )
                        )
                    )
                    if (itemCount > 1) {
                        val q1 = quickItems[idx1]
                        val q1Price = WorldCountryCurrencyRegistry.formatCompactCurrency(
                            q1.suggestedAmount,
                            loadedData.countryCode,
                            loadedData.language
                        )
                        OutlinedPillChip(
                            text = "${q1.label} • $q1Price",
                            isSelected = activeNote == q1.label,
                            selectedBorderColor = activeAccent,
                            selectedFillColor = activeSoftBg,
                            selectedTextColor = activeAccent,
                            unselectedBorderColor = chipOutlineColor,
                            unselectedFillColor = chipUnselectedBg,
                            unselectedTextColor = primaryTextColor,
                            cornerRadius = 8.dp,
                            verticalPad = 2.5.dp,
                            fontSizeSp = 10f,
                            modifier = GlanceModifier.defaultWeight().padding(start = 3.dp),
                            onClick = actionRunCallback<WidgetSelectQuickItemAction>(
                                actionParametersOf(
                                    WidgetSelectQuickItemAction.KEY_AMOUNT to q1.suggestedAmount,
                                    WidgetSelectQuickItemAction.KEY_LABEL to q1.label
                                )
                            )
                        )
                    }
                    if (itemCount > 2) {
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        SmallArrowButton(
                            symbol = "›",
                            bgColor = chipUnselectedBg,
                            textColor = mutedTextColor,
                            onClick = actionRunCallback<WidgetCycleQuickItemAction>()
                        )
                    }
                }
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presets.forEachIndexed { idx, preset ->
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .let { m -> if (idx > 0) m.padding(start = 3.dp) else m }
                                .background(chipUnselectedBg)
                                .cornerRadius(8.dp)
                                .clickable(
                                    actionRunCallback<WidgetQuickAddAction>(
                                        actionParametersOf(WidgetQuickAddAction.KEY_ADD_AMOUNT to preset.amount)
                                    )
                                )
                                .padding(vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                style = TextStyle(
                                    color = royalBlueColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Kotak Nominal Outlined (menyerupai OutlinedTextField di BottomPopupNominalInputSheet)
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(activeAccent)
                    .cornerRadius(11.dp)
                    .padding(1.5.dp)
            ) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(inputSurfaceBg)
                        .cornerRadius(9.5.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        val captionText = when {
                            activeNote.isNotBlank() -> "${if (isId) "Catatan" else "Note"}: $activeNote"
                            isIncome -> if (isId) "Jumlah Uang Masuk (${loadedData.currencyCode})" else "Income Amount (${loadedData.currencyCode})"
                            else -> if (isId) "Jumlah Uang Keluar (${loadedData.currencyCode})" else "Expense Amount (${loadedData.currencyCode})"
                        }
                        Text(
                            text = captionText,
                            style = TextStyle(
                                color = if (activeNote.isNotBlank()) activeAccent else mutedTextColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = formattedNominal,
                            style = TextStyle(
                                color = if (typedAmount > 0L) activeAccent else mutedTextColor,
                                fontSize = if (isCompactHeight) 16.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                    if (activeNote.isNotBlank()) {
                        Box(
                            modifier = GlanceModifier
                                .background(chipUnselectedBg)
                                .cornerRadius(7.dp)
                                .clickable(actionRunCallback<WidgetClearNoteAction>())
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                style = TextStyle(
                                    color = mutedTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                    Box(
                        modifier = GlanceModifier
                            .background(keyBgColor)
                            .cornerRadius(7.dp)
                            .clickable(
                                actionRunCallback<WidgetDigitAction>(
                                    actionParametersOf(WidgetDigitAction.KEY_TOKEN to "CLEAR")
                                )
                            )
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            style = TextStyle(
                                color = coralColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Keypad Angka Langsung di Widget (pakai tinggi tombol eksplisit & padding, tanpa nested weight+fillMaxSize)
            if (!isCompactHeight) {
                val keyHeight = if (isUltraTall) 28.dp else 22.dp
                Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 3.dp)) {
                    if (isUltraTall) {
                        WidgetKeypadRow(
                            tokens = listOf("1", "2", "3", "BACKSPACE"),
                            labels = listOf("1", "2", "3", "⌫"),
                            keyHeight = keyHeight,
                            keyBgColor = keyBgColor,
                            primaryTextColor = primaryTextColor,
                            accentTextColor = coralColor
                        )
                        WidgetKeypadRow(
                            tokens = listOf("4", "5", "6", zeroZeroToken),
                            labels = listOf("4", "5", "6", zeroZeroToken),
                            keyHeight = keyHeight,
                            keyBgColor = keyBgColor,
                            primaryTextColor = primaryTextColor,
                            accentTextColor = royalBlueColor,
                            modifier = GlanceModifier.padding(top = 3.dp)
                        )
                        WidgetKeypadRow(
                            tokens = listOf("7", "8", "9", "0"),
                            labels = listOf("7", "8", "9", "0"),
                            keyHeight = keyHeight,
                            keyBgColor = keyBgColor,
                            primaryTextColor = primaryTextColor,
                            accentTextColor = primaryTextColor,
                            modifier = GlanceModifier.padding(top = 3.dp)
                        )
                    } else {
                        WidgetKeypadRow(
                            tokens = listOf("1", "2", "3", "4", "5", "BACKSPACE"),
                            labels = listOf("1", "2", "3", "4", "5", "⌫"),
                            keyHeight = keyHeight,
                            keyBgColor = keyBgColor,
                            primaryTextColor = primaryTextColor,
                            accentTextColor = coralColor
                        )
                        WidgetKeypadRow(
                            tokens = listOf("6", "7", "8", "9", "0", zeroZeroToken),
                            labels = listOf("6", "7", "8", "9", "0", zeroZeroToken),
                            keyHeight = keyHeight,
                            keyBgColor = keyBgColor,
                            primaryTextColor = primaryTextColor,
                            accentTextColor = royalBlueColor,
                            modifier = GlanceModifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        // [BLOK 6] 5. PILIH SUMBER DOMPET / BANK (Disembunyikan saat "Darurat", diganti info Saldo Darurat)
        if (isEmergencyExpense) {
            val emFormatted = WorldCountryCurrencyRegistry.formatMoney(loadedData.emergencyBalance, loadedData.countryCode)
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .background(goldSoftBg)
                    .cornerRadius(9.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (isId) {
                        "Potong Saldo Darurat Terpisah: $emFormatted"
                    } else {
                        "Deduct Separate Emergency Fund: $emFormatted"
                    },
                    style = TextStyle(
                        color = goldColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        } else if (wallets.isNotEmpty()) {
            val walletPageSize = 2
            val totalWallets = wallets.size
            val totalWalletPages = (totalWallets + walletPageSize - 1) / walletPageSize
            val walletPage = safeWalletIdx / walletPageSize
            val wPageStart = walletPage * walletPageSize
            val visibleWallets = wallets.subList(wPageStart, minOf(wPageStart + walletPageSize, totalWallets))

            Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isId) "5. Pilih Sumber Dompet / Bank" else "5. Choose Wallet / Bank",
                        style = TextStyle(
                            color = mutedTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (totalWalletPages > 1) {
                        SmallArrowButton(
                            symbol = "‹",
                            bgColor = chipUnselectedBg,
                            textColor = mutedTextColor,
                            onClick = actionRunCallback<WidgetPageWalletAction>(
                                actionParametersOf(WidgetPageWalletAction.KEY_STEP to -1)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        SmallArrowButton(
                            symbol = "›",
                            bgColor = chipUnselectedBg,
                            textColor = mutedTextColor,
                            onClick = actionRunCallback<WidgetPageWalletAction>(
                                actionParametersOf(WidgetPageWalletAction.KEY_STEP to 1)
                            )
                        )
                    }
                }
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    visibleWallets.forEachIndexed { idx, w ->
                        val absWalletIdx = wPageStart + idx
                        val isSel = absWalletIdx == safeWalletIdx
                        val wAccent = DayNightColorProvider(
                            day = ArusKasWidgetEngine.parseWalletHexColor(w.colorHex),
                            night = ArusKasWidgetEngine.parseWalletHexColor(w.colorHex)
                        )
                        val wBal = WorldCountryCurrencyRegistry.formatCompactCurrency(
                            w.balance,
                            loadedData.countryCode,
                            loadedData.language
                        )
                        OutlinedPillChip(
                            text = "${w.name}: $wBal",
                            isSelected = isSel,
                            selectedBorderColor = wAccent,
                            selectedFillColor = royalBlueSoftBg,
                            selectedTextColor = wAccent,
                            unselectedBorderColor = chipOutlineColor,
                            unselectedFillColor = chipUnselectedBg,
                            unselectedTextColor = primaryTextColor,
                            cornerRadius = 9.dp,
                            verticalPad = 3.5.dp,
                            fontSizeSp = 10.5f,
                            modifier = GlanceModifier.defaultWeight().let { m ->
                                if (idx > 0) m.padding(start = 4.dp) else m
                            },
                            onClick = actionRunCallback<WidgetSelectWalletAction>(
                                actionParametersOf(WidgetSelectWalletAction.KEY_WALLET_INDEX to absWalletIdx)
                            )
                        )
                    }
                    if (visibleWallets.size == 1 && totalWalletPages > 1) {
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Box(modifier = GlanceModifier.defaultWeight()) {}
                    }
                }
            }
        }

        // [BLOK 7] 6. TOMBOL SIMPAN TRANSAKSI (full width, identik dengan Button di aplikasi)
        val canSave = typedAmount > 0L && (isEmergencyExpense || wallets.isNotEmpty())
        val saveBg = if (canSave) activeAccent else segmentedBgColor
        val saveTextColor = if (canSave) whiteColor else mutedTextColor
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(saveBg)
                .cornerRadius(12.dp)
                .clickable(actionRunCallback<WidgetSaveTransactionAction>())
                .padding(vertical = if (isCompactHeight) 6.dp else 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isId) "Simpan Transaksi" else "Save Transaction",
                style = TextStyle(
                    color = saveTextColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SmallArrowButton(
    symbol: String,
    bgColor: ColorProvider,
    textColor: ColorProvider,
    onClick: Action
) {
    Box(
        modifier = GlanceModifier
            .background(bgColor)
            .cornerRadius(7.dp)
            .clickable(onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = TextStyle(
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun OutlinedPillChip(
    text: String,
    isSelected: Boolean,
    selectedBorderColor: ColorProvider,
    selectedFillColor: ColorProvider,
    selectedTextColor: ColorProvider,
    unselectedBorderColor: ColorProvider,
    unselectedFillColor: ColorProvider,
    unselectedTextColor: ColorProvider,
    cornerRadius: Dp,
    verticalPad: Dp,
    fontSizeSp: Float,
    modifier: GlanceModifier = GlanceModifier,
    onClick: Action
) {
    val borderColor = if (isSelected) selectedBorderColor else unselectedBorderColor
    val fillColor = if (isSelected) selectedFillColor else unselectedFillColor
    val textColor = if (isSelected) selectedTextColor else unselectedTextColor
    val innerCorner = (cornerRadius - 1.dp).coerceAtLeast(4.dp)

    Box(
        modifier = modifier
            .background(borderColor)
            .cornerRadius(cornerRadius)
            .clickable(onClick)
            .padding(1.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(fillColor)
                .cornerRadius(innerCorner)
                .padding(horizontal = 5.dp, vertical = verticalPad),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = textColor,
                    fontSize = fontSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WidgetKeypadRow(
    tokens: List<String>,
    labels: List<String>,
    keyHeight: Dp,
    keyBgColor: ColorProvider,
    primaryTextColor: ColorProvider,
    accentTextColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tokens.forEachIndexed { idx, token ->
            val isLastAction = idx == tokens.lastIndex && (token == "BACKSPACE" || token == "000" || token == "00")
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .let { m -> if (idx > 0) m.padding(start = 3.dp) else m }
                    .height(keyHeight)
                    .background(keyBgColor)
                    .cornerRadius(7.dp)
                    .clickable(
                        actionRunCallback<WidgetDigitAction>(
                            actionParametersOf(WidgetDigitAction.KEY_TOKEN to token)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labels[idx],
                    style = TextStyle(
                        color = if (isLastAction) accentTextColor else primaryTextColor,
                        fontSize = if (token.length >= 3 && token != "BACKSPACE") 11.sp else 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

class WidgetDigitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val token = parameters[KEY_TOKEN] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] ?: ""
            prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] =
                ArusKasWidgetEngine.applyKeypadInput(current, token)
            if (token == "CLEAR") {
                prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_TOKEN = ActionParameters.Key<String>("digit_token")
    }
}

class WidgetQuickAddAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val addAmount = parameters[KEY_ADD_AMOUNT] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] ?: ""
            prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] =
                ArusKasWidgetEngine.addPresetAmount(current, addAmount)
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_ADD_AMOUNT = ActionParameters.Key<Long>("add_amount")
    }
}

class WidgetSelectQuickItemAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val amount = parameters[KEY_AMOUNT] ?: return
        val label = parameters[KEY_LABEL] ?: ""
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] = amount.toString()
            prefs[ArusKasQuickEntryWidget.KEY_NOTE] = label
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_AMOUNT = ActionParameters.Key<Long>("quick_item_amount")
        val KEY_LABEL = ActionParameters.Key<String>("quick_item_label")
    }
}

class WidgetCycleQuickItemAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val cur = prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] ?: 0
            prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = cur + 1
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }
}

class WidgetClearNoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }
}

class WidgetSelectTypeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val targetType = parameters[KEY_TX_TYPE] ?: return
        val normalized = if (targetType == "INCOME") "INCOME" else "EXPENSE"
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] ?: "EXPENSE"
            if (current != normalized) {
                prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] = normalized
                prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] = 0
                prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = 0
                prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_TX_TYPE = ActionParameters.Key<String>("tx_type")
    }
}

class WidgetToggleTypeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] ?: "EXPENSE"
            prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] = if (current == "INCOME") "EXPENSE" else "INCOME"
            prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] = 0
            prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = 0
            prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }
}

class WidgetSelectDateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val offset = parameters[KEY_DAY_OFFSET] ?: 0
        val clamped = when (offset) {
            -1 -> -1
            1 -> 1
            else -> 0
        }
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[ArusKasQuickEntryWidget.KEY_DAY_OFFSET] = clamped
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_DAY_OFFSET = ActionParameters.Key<Int>("day_offset")
    }
}

class WidgetSelectCategoryAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val idx = parameters[KEY_CATEGORY_INDEX] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            val prevIdx = prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] ?: 0
            if (prevIdx != idx) {
                prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] = idx
                prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = 0
                prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_CATEGORY_INDEX = ActionParameters.Key<Int>("category_index")
    }
}

class WidgetPageCategoryAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val step = parameters[KEY_STEP] ?: 1
        val pageSize = (parameters[KEY_PAGE_SIZE] ?: 2).coerceAtLeast(1)
        val data = withContext(Dispatchers.IO) { ArusKasWidgetEngine.loadWidgetData(context) }
        updateAppWidgetState(context, glanceId) { prefs ->
            val txType = prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] ?: "EXPENSE"
            val count = if (txType == "INCOME") data.incomeCategories.size else data.expenseCategories.size
            if (count > 0) {
                val totalPages = ((count + pageSize - 1) / pageSize).coerceAtLeast(1)
                val curIdx = prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] ?: 0
                val curPage = (with(ArusKasWidgetEngine) { curIdx.floorMod(count) }) / pageSize
                val nextPage = ((curPage + step) % totalPages + totalPages) % totalPages
                prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] = nextPage * pageSize
                prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = 0
                prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_STEP = ActionParameters.Key<Int>("cat_page_step")
        val KEY_PAGE_SIZE = ActionParameters.Key<Int>("cat_page_size")
    }
}

class WidgetCycleCategoryAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val step = parameters[KEY_STEP] ?: 1
        val data = withContext(Dispatchers.IO) { ArusKasWidgetEngine.loadWidgetData(context) }
        updateAppWidgetState(context, glanceId) { prefs ->
            val txType = prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] ?: "EXPENSE"
            val count = if (txType == "INCOME") data.incomeCategories.size else data.expenseCategories.size
            val cur = prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] ?: 0
            val next = if (count > 0) ((cur + step) % count + count) % count else 0
            prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] = next
            prefs[ArusKasQuickEntryWidget.KEY_QUICK_ITEM_PAGE] = 0
            prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_STEP = ActionParameters.Key<Int>("category_step")
    }
}

class WidgetSelectWalletAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val idx = parameters[KEY_WALLET_INDEX] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] = idx
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_WALLET_INDEX = ActionParameters.Key<Int>("wallet_index")
    }
}

class WidgetPageWalletAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val step = parameters[KEY_STEP] ?: 1
        val pageSize = 2
        val data = withContext(Dispatchers.IO) { ArusKasWidgetEngine.loadWidgetData(context) }
        updateAppWidgetState(context, glanceId) { prefs ->
            val count = data.wallets.size
            if (count > 0) {
                val totalPages = ((count + pageSize - 1) / pageSize).coerceAtLeast(1)
                val curIdx = prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] ?: 0
                val curPage = (with(ArusKasWidgetEngine) { curIdx.floorMod(count) }) / pageSize
                val nextPage = ((curPage + step) % totalPages + totalPages) % totalPages
                prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] = nextPage * pageSize
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_STEP = ActionParameters.Key<Int>("wallet_page_step")
    }
}

class WidgetCycleWalletAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val step = parameters[KEY_STEP] ?: 1
        val data = withContext(Dispatchers.IO) { ArusKasWidgetEngine.loadWidgetData(context) }
        updateAppWidgetState(context, glanceId) { prefs ->
            val count = data.wallets.size
            val cur = prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] ?: 0
            val next = if (count > 0) ((cur + step) % count + count) % count else 0
            prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] = next
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = ""
        }
        ArusKasQuickEntryWidget().update(context, glanceId)
    }

    companion object {
        val KEY_STEP = ActionParameters.Key<Int>("wallet_step")
    }
}

class WidgetSaveTransactionAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        var result: WidgetSaveResult? = null
        updateAppWidgetState(context, glanceId) { prefs ->
            val txType = prefs[ArusKasQuickEntryWidget.KEY_TX_TYPE] ?: "EXPENSE"
            val dayOffset = prefs[ArusKasQuickEntryWidget.KEY_DAY_OFFSET] ?: 0
            val amount = (prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] ?: "").toLongOrNull() ?: 0L
            val catIdx = prefs[ArusKasQuickEntryWidget.KEY_CATEGORY_INDEX] ?: 0
            val walletIdx = prefs[ArusKasQuickEntryWidget.KEY_WALLET_INDEX] ?: 0
            val note = prefs[ArusKasQuickEntryWidget.KEY_NOTE] ?: ""

            val saveRes = withContext(Dispatchers.IO) {
                ArusKasWidgetEngine.recordTransactionFromWidget(
                    context = context,
                    txType = txType,
                    amount = amount,
                    categoryIndex = catIdx,
                    walletIndex = walletIdx,
                    dayOffset = dayOffset,
                    note = note
                )
            }
            result = saveRes
            if (saveRes.success) {
                prefs[ArusKasQuickEntryWidget.KEY_AMOUNT_DIGITS] = ""
                prefs[ArusKasQuickEntryWidget.KEY_NOTE] = ""
            }
            prefs[ArusKasQuickEntryWidget.KEY_FEEDBACK] = saveRes.feedbackMessage
            prefs[ArusKasQuickEntryWidget.KEY_REVISION] = (prefs[ArusKasQuickEntryWidget.KEY_REVISION] ?: 0) + 1
        }
        if (result?.success == true) {
            ArusKasQuickEntryWidget().updateAll(context)
        } else {
            ArusKasQuickEntryWidget().update(context, glanceId)
        }
    }
}
