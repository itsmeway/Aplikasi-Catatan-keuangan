package com.example.ui

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.example.data.AppLocalization
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.data.ModernPdfBackupGenerator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.CategoryLimitConfig
import com.example.data.EmergencyFundConfig
import com.example.data.MandatoryBillItem
import com.example.data.PresetCategories
import com.example.data.ProFeatureTrigger
import com.example.data.QuickCategoryItem
import com.example.data.SavingsGoal
import com.example.data.SubscriptionConfig
import com.example.data.TransactionRecord
import com.example.data.UserFinancialProfile
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaElectricCyan
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SpaceGroteskFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun activeLocale(lang: AppLanguage): Locale {
    return if (lang == AppLanguage.INDONESIAN) Locale("in", "ID") else Locale.US
}

/**
 * 1. TOTAL BALANCE CARD (All Banks, Digital Wallets, and Cash Directly Visible).
 * - International English default: "Total Balance" ("Total Saldo" in Indonesian)
 * - Shows active country flag & currency code badge.
 */
@Composable
fun TotalSaldoDirectWalletsCard(
    wallets: List<Wallet>,
    totalBalance: Long,
    totalSemuaDuitDiKantong: Long,
    saldoLineColor: Color,
    formatMoneyDisplay: (Long) -> String,
    onOpenWalletsSheet: () -> Unit,
    onOpenCountryCurrencySheet: (() -> Unit)? = null
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()

    MyBcaHeroLiquidCard(
        onClick = onOpenWalletsSheet,
        accentLineColor = saldoLineColor,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_minimalist_balance_pill")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = if (isEn) "Total Balance" else "Total Saldo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isEn) "Total Balance" else "Total Saldo",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                color = Color(0xFFDFF2FF),
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${countryProfile.flagEmoji} ${countryProfile.currencyCode}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = formatMoneyDisplay(totalBalance),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Surface(
                    onClick = onOpenWalletsSheet,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = if (isEn) "Manage Wallets" else "Atur Dompet",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isEn) "Manage" else "Atur",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Clean readable 2-column grid of Wallets with clear icons & larger typography
            val walletChunks = wallets.chunked(2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("direct_visible_wallets_grid"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                walletChunks.forEach { rowWallets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowWallets.forEach { w ->
                            val walletIcon = when (w.walletType.uppercase()) {
                                "BANK" -> Icons.Default.AccountBalance
                                "CASH" -> Icons.Default.Payments
                                else -> Icons.Default.AccountBalanceWallet
                            }
                            Surface(
                                onClick = onOpenWalletsSheet,
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.38f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 56.dp)
                                    .testTag("direct_wallet_pill_${w.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = walletIcon,
                                        contentDescription = w.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = w.name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                            color = Color(0xFFE5F4FF),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatMoneyDisplay(w.balance),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 14.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        if (rowWallets.size == 1) {
                            Surface(
                                onClick = onOpenWalletsSheet,
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.32f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 56.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Add Wallet" else "Tambah Dompet",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
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
 * 2. SPENDING LIMITS (FULL-WIDTH CLEAN CARDS) + CLEAR EMERGENCY FUND CARD
 */
@Composable
fun UpgradedCategoryGroupSection(
    allCategoryItems: List<QuickCategoryItem>,
    categoryLimits: Map<String, CategoryLimitConfig>,
    transactions: List<TransactionRecord>,
    mandatoryBills: List<MandatoryBillItem>,
    emergencyFund: EmergencyFundConfig,
    emergencySpent: Long,
    selectedCategoryKey: String,
    isDarkMode: Boolean,
    onSelectCategoryAndOpenInput: (QuickCategoryItem) -> Unit,
    onEditCategoryLimit: (QuickCategoryItem) -> Unit,
    onAddNewCategory: () -> Unit,
    onOpenEmergencyDialog: () -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val dateShortFmt = remember(lang) { SimpleDateFormat("dd MMM", activeLocale(lang)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_group_below_balance"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header with clear Add Category button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Spending Limits" else "Batas Pengeluaran",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEn) "Tap a category to record or tap Edit to set limit" else "Ketuk kategori untuk catat, atau ketuk Atur untuk ubah batas",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAddNewCategory,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BcaRoyalBlue.copy(alpha = 0.12f),
                    contentColor = BcaRoyalBlue
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("add_new_category_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isEn) "Add Category" else "Tambah Kategori",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEn) "Add" else "Tambah",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dedicated Clear Emergency Fund Card
        EmergencyRoofOvalPill(
            emergencyFund = emergencyFund,
            emergencySpent = emergencySpent,
            onClick = onOpenEmergencyDialog
        )

        // Full-Width Single-Column Category Limit Cards (Spacious & Easy to Scan)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            allCategoryItems.forEach { item ->
                val limitCfg = categoryLimits[item.key] ?: CategoryLimitConfig(
                    categoryKey = item.key,
                    limitAmount = item.defaultLimitAmount,
                    period = item.defaultLimitPeriod
                )
                val displayLabel = normalizeFormalCategoryLabel(
                    key = item.key,
                    rawLabel = limitCfg.customLabel.ifBlank { item.label },
                    lang = lang
                )
                val displaySub = normalizeFormalCategorySubtitle(
                    key = item.key,
                    rawSub = limitCfg.customSubtitle.ifBlank { item.subtitle },
                    lang = lang
                )

                val spent = remember(
                    transactions,
                    mandatoryBills,
                    item.key,
                    limitCfg.period,
                    limitCfg.startDateMillis,
                    limitCfg.endDateMillis
                ) {
                    ArusKasViewModel.calculateSpentForCategoryLimit(
                        categoryKey = item.key,
                        period = limitCfg.period,
                        transactions = transactions,
                        mandatoryBills = mandatoryBills,
                        startDateMillis = limitCfg.startDateMillis,
                        endDateMillis = limitCfg.endDateMillis
                    )
                }

                val dateRangeText = remember(limitCfg.startDateMillis, limitCfg.endDateMillis, limitCfg.period, lang) {
                    val cal = Calendar.getInstance()
                    when (limitCfg.period) {
                        "RENTANG_TANGGAL" -> if (limitCfg.startDateMillis > 0L && limitCfg.endDateMillis > 0L) {
                            "${dateShortFmt.format(Date(limitCfg.startDateMillis))} - ${dateShortFmt.format(Date(limitCfg.endDateMillis))}"
                        } else {
                            if (isEn) "Custom Range" else "Rentang Kalender"
                        }
                        "HARIAN" -> if (isEn) "Daily • Today" else "Harian • Hari Ini"
                        "MINGGUAN" -> {
                            val endTxt = dateShortFmt.format(cal.time)
                            cal.add(Calendar.DAY_OF_YEAR, -6)
                            val startTxt = dateShortFmt.format(cal.time)
                            (if (isEn) "Weekly • " else "Mingguan • ") + "$startTxt - $endTxt"
                        }
                        "BEBAS" -> if (isEn) "Flexible (No Limit)" else "Bebas (Tanpa Batas)"
                        else -> {
                            if (limitCfg.startDateMillis > 0L && limitCfg.endDateMillis > 0L) {
                                (if (isEn) "Monthly • " else "Bulanan • ") + "${dateShortFmt.format(Date(limitCfg.startDateMillis))} - ${dateShortFmt.format(Date(limitCfg.endDateMillis))}"
                            } else {
                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                val s = dateShortFmt.format(cal.time)
                                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                val e = dateShortFmt.format(cal.time)
                                (if (isEn) "Monthly • " else "Bulanan • ") + "$s - $e"
                            }
                        }
                    }
                }

                ModernDetailedCategoryCard(
                    modifier = Modifier.fillMaxWidth(),
                    item = item.copy(label = displayLabel, subtitle = displaySub),
                    spentAmount = spent,
                    limitConfig = limitCfg,
                    dateRangeLabel = dateRangeText,
                    isSelected = selectedCategoryKey == item.key,
                    isAttachedToRoof = false,
                    isDarkMode = isDarkMode,
                    onClick = { onSelectCategoryAndOpenInput(item.copy(label = displayLabel, subtitle = displaySub)) },
                    onEditLimit = { onEditCategoryLimit(item.copy(label = displayLabel, subtitle = displaySub)) }
                )
            }
        }
    }
}

/**
 * Normalizes category labels to formal International English (default) or formal Indonesian (optional).
 */
fun normalizeFormalCategoryLabel(
    key: String,
    rawLabel: String,
    lang: AppLanguage = WorldCountryCurrencyRegistry.activeLanguage
): String {
    val trimmed = rawLabel.trim()
    val isEn = lang == AppLanguage.ENGLISH
    val knownDefaults = setOf(
        "",
        "Tagihan Wajib", "Kebutuhan Wajib", "Mandatory Needs", "Mandatory Bills",
        "Bensin & Harian", "Kebutuhan Harian", "Daily Essentials", "Fuel & Daily Needs",
        "Rawat Diri", "Perawatan Diri", "Personal Care",
        "Jajan & Ngopi", "Konsumsi & Hiburan", "Dining & Leisure", "Coffee & Snacks",
        "Sosial & Ortu", "Sosial & Keluarga", "Family & Social",
        "Servis & Kerja", "Operasional & Kerja", "Work & Mobility",
        "Pakai Uang Darurat", "Dana Darurat", "Emergency Expense", "Emergency Fund"
    )
    if (trimmed in knownDefaults) {
        return when (key) {
            "tagihan_bulanan" -> if (isEn) "Mandatory Needs" else "Kebutuhan Wajib"
            "kebutuhan_pokok" -> if (isEn) "Daily Essentials" else "Kebutuhan Harian"
            "rawat_diri" -> if (isEn) "Personal Care" else "Perawatan Diri"
            "gaya_hidup" -> if (isEn) "Dining & Leisure" else "Konsumsi & Hiburan"
            "sosial_darurat" -> if (isEn) "Family & Social" else "Sosial & Keluarga"
            "modal_produktif" -> if (isEn) "Work & Mobility" else "Operasional & Kerja"
            "pakai_darurat" -> if (isEn) "Emergency Expense" else "Pakai Uang Darurat"
            else -> trimmed
        }
    }
    return trimmed
}

private fun normalizeFormalCategorySubtitle(
    key: String,
    rawSub: String,
    lang: AppLanguage = WorldCountryCurrencyRegistry.activeLanguage
): String {
    val trimmed = rawSub.trim()
    val isEn = lang == AppLanguage.ENGLISH
    val knownDefaultSubs = setOf(
        "",
        "Listrik, Air, Internet, Cicilan", "Electricity, Water, Internet, Rent",
        "Sewa, Data, Listrik", "Rent, Data, Utilities",
        "Makan, Bensin, Belanja Harian", "Makan, Bensin, Harian", "Meals, Fuel, Groceries, Transit", "Meals, Fuel, Groceries",
        "Sabun, Potong Rambut, Skincare", "Sabun, Laundry, Pangkas", "Toiletries, Grooming, Wellness", "Grooming, Laundry, Care",
        "Kopi, Camilan, Hiburan", "Kopi, Cemilan, Rekreasi", "Coffee, Snacks, Entertainment", "Coffee, Snacks, Leisure",
        "Orang Tua, Sedekah, Hadiah", "Keluarga, Dana Sosial", "Parents, Gifts, Donations", "Family Support, Gifts",
        "Servis Kendaraan, Kuota Kerja", "Servis, Kebutuhan Kerja", "Maintenance, Tools, Work", "Transit, Maintenance, Work"
    )
    if (trimmed in knownDefaultSubs) {
        return when (key) {
            "tagihan_bulanan" -> if (isEn) "Electricity, Water, Internet, Rent" else "Listrik, Air, Internet, Cicilan"
            "kebutuhan_pokok" -> if (isEn) "Meals, Fuel, Groceries, Transit" else "Makan, Bensin, Belanja Harian"
            "rawat_diri" -> if (isEn) "Toiletries, Grooming, Wellness" else "Sabun, Potong Rambut, Skincare"
            "gaya_hidup" -> if (isEn) "Coffee, Snacks, Entertainment" else "Kopi, Camilan, Hiburan"
            "sosial_darurat" -> if (isEn) "Parents, Gifts, Donations" else "Orang Tua, Sedekah, Hadiah"
            "modal_produktif" -> if (isEn) "Maintenance, Tools, Work" else "Servis Kendaraan, Kuota Kerja"
            else -> trimmed
        }
    }
    return trimmed
}

/**
 * Compact oval pill on the roof of the category group (attached on top of Mandatory Needs & Daily Essentials).
 * Displays only the Emergency Fund Balance Bar and Emergency Limit Bar.
 */
@Composable
private fun EmergencyRoofOvalPill(
    emergencyFund: EmergencyFundConfig,
    emergencySpent: Long,
    onClick: () -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val limitAmt = emergencyFund.emergencyLimit.coerceAtLeast(1L)
    val balanceAmt = emergencyFund.emergencyBalance.coerceAtLeast(0L)
    val limitUsedRatio = (emergencySpent.toFloat() / limitAmt.toFloat()).coerceIn(0f, 1f)
    val fundFillRatio = (balanceAmt.toFloat() / maxOf(limitAmt, balanceAmt, 1L).toFloat()).coerceIn(0.15f, 1f)

    val isOverEmergencyLimit = emergencyFund.emergencyLimit > 0L && emergencySpent > emergencyFund.emergencyLimit
    val isEmergencyEmpty = balanceAmt <= 0L
    val cardAccent = when {
        isOverEmergencyLimit -> CoralExpense
        isEmergencyEmpty -> GoldAccent
        else -> BcaRoyalBlue
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.2.dp, cardAccent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("under_balance_emergency_limit_row")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = cardAccent.copy(alpha = 0.14f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = if (isEn) "Emergency Fund" else "Dana Darurat",
                                tint = cardAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isEn) "Emergency Fund" else "Dana Darurat",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                isOverEmergencyLimit -> if (isEn) {
                                    "Emergency limit exceeded • Tap to review"
                                } else {
                                    "Pemakaian melewati batas darurat • Ketuk untuk cek"
                                }
                                isEmergencyEmpty -> if (isEn) {
                                    "No emergency fund yet • Tap to set aside"
                                } else {
                                    "Belum memiliki dana darurat • Ketuk untuk atur"
                                }
                                else -> if (isEn) {
                                    "Tap to top up or adjust safety limit"
                                } else {
                                    "Ketuk untuk tambah saldo atau atur batas darurat"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = if (isOverEmergencyLimit || isEmergencyEmpty) cardAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverEmergencyLimit || isEmergencyEmpty) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cardAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isEn) "Edit" else "Atur",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = cardAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isEn) "Saved Balance" else "Saldo Tersimpan",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ArusKasViewModel.formatMoney(balanceAmt),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 15.sp,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { fundFillRatio },
                        color = EmeraldPrimary,
                        trackColor = EmeraldPrimary.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isEn) "Safety Limit" else "Batas Pakai",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ArusKasViewModel.formatMoney(emergencyFund.emergencyLimit),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 15.sp,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = BcaRoyalBlue,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { if (emergencySpent > 0L) limitUsedRatio else 0f },
                        color = if (emergencySpent > emergencyFund.emergencyLimit) CoralExpense else BcaRoyalBlue,
                        trackColor = BcaRoyalBlue.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernDetailedCategoryCard(
    modifier: Modifier = Modifier,
    item: QuickCategoryItem,
    spentAmount: Long,
    limitConfig: CategoryLimitConfig,
    dateRangeLabel: String,
    isSelected: Boolean,
    isAttachedToRoof: Boolean = false,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onEditLimit: () -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val limitAmt = limitConfig.limitAmount
    val ratio = if (limitAmt > 0L) (spentAmount.toFloat() / limitAmt.toFloat()) else 0f
    val remainingAmt = (limitAmt - spentAmount).coerceAtLeast(0L)
    val isOverLimit = limitAmt > 0L && spentAmount > limitAmt
    val overAmt = (spentAmount - limitAmt).coerceAtLeast(0L)

    val statusColor = when {
        limitAmt <= 0L -> SkyBlue
        ratio >= 1f -> CoralExpense
        ratio >= 0.75f -> GoldAccent
        else -> EmeraldPrimary
    }

    val statusBadgeText = when {
        limitAmt <= 0L -> if (isEn) "Flexible" else "Tanpa Batas"
        ratio >= 1f -> if (isEn) "Over Limit" else "Lewat Batas"
        ratio >= 0.75f -> if (isEn) "Almost Full" else "Hampir Habis"
        else -> if (isEn) "Safe" else "Aman"
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = glassmorphicBorderStroke(
            accentColor = if (isSelected) BcaRoyalBlue else statusColor,
            width = if (isSelected) 1.6.dp else 1.dp,
            isSelected = isSelected
        ),
        modifier = modifier
            .glassmorphicCardEffect(
                cornerRadius = 18.dp,
                accentTint = if (isSelected) BcaRoyalBlue else statusColor,
                isDark = isDarkMode
            )
            .testTag("category_tile_${item.key}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconForCategory(item.key),
                                contentDescription = item.label,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = statusBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = dateRangeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = BcaCobaltBlue,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = onEditLimit,
                    shape = RoundedCornerShape(10.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.11f),
                    border = BorderStroke(0.8.dp, BcaRoyalBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("edit_limit_${item.key}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isEn) "Edit ${item.label}" else "Atur ${item.label}",
                            tint = BcaRoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isEn) "Edit" else "Atur",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = BcaRoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Clear Numeric Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Spent" else "Terpakai",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ArusKasViewModel.formatMoney(spentAmount),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.5.sp,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEn) "Limit" else "Batas Limit",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (limitAmt > 0L) ArusKasViewModel.formatMoney(limitAmt) else (if (isEn) "None" else "Bebas"),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.5.sp,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = BcaRoyalBlue,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (limitAmt > 0L) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (isOverLimit) {
                                if (isEn) "Over by" else "Lebih"
                            } else {
                                if (isEn) "Remaining" else "Sisa"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = if (isOverLimit) CoralExpense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverLimit) {
                                "+${ArusKasViewModel.formatMoney(overAmt)}"
                            } else {
                                ArusKasViewModel.formatMoney(remainingAmt)
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.5.sp,
                                fontFamily = SpaceGroteskFontFamily
                            ),
                            color = if (isOverLimit) CoralExpense else EmeraldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = {
                    if (limitAmt > 0L) ratio.coerceIn(0f, 1f) else if (spentAmount > 0L) 0.3f else 0f
                },
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

/**
 * Dialog to Add / Edit Category + Edit Name + Calendar Date Range Tool
 */
@Composable
fun CategoryEditorWithCalendarDialog(
    category: QuickCategoryItem?,
    initialConfig: CategoryLimitConfig?,
    isCreatingNew: Boolean = false,
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    onDismiss: () -> Unit,
    onDeleteCategory: ((String) -> Unit)? = null,
    onSave: (
        categoryKey: String,
        customLabel: String,
        customSubtitle: String,
        limitAmount: Long,
        period: String,
        startDateMillis: Long,
        endDateMillis: Long,
        isCustomCategory: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val canUseCustomRange = ArusKasFeatureGateEvaluator.canUseCustomDateRangePeriod(subscription)
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val dateFmt = remember(lang) { SimpleDateFormat("dd MMM yyyy", activeLocale(lang)) }

    val defaultStartMillis = remember(initialConfig) {
        val cfgStart = initialConfig?.startDateMillis ?: 0L
        if (cfgStart > 0L) cfgStart else ArusKasViewModel.getStartOfMonthMillis()
    }
    val defaultEndMillis = remember(initialConfig) {
        val cfgEnd = initialConfig?.endDateMillis ?: 0L
        if (cfgEnd > 0L) cfgEnd else ArusKasViewModel.getEndOfMonthMillis()
    }

    var nameStr by remember(category, initialConfig, lang) {
        val raw = initialConfig?.customLabel?.ifBlank { category?.label ?: "" } ?: (category?.label ?: "")
        mutableStateOf(if (category != null) normalizeFormalCategoryLabel(category.key, raw, lang) else raw)
    }
    var subtitleStr by remember(category, initialConfig, lang) {
        val raw = initialConfig?.customSubtitle?.ifBlank { category?.subtitle ?: "" } ?: (category?.subtitle ?: "")
        mutableStateOf(if (category != null) normalizeFormalCategorySubtitle(category.key, raw, lang) else raw)
    }
    val presetLimits = remember(countryProfile.countryCode) {
        WorldCountryCurrencyRegistry.presetLimitOptions(countryProfile.countryCode)
    }
    var limitStr by remember(initialConfig, category, isCreatingNew, countryProfile.countryCode) {
        val defaultAmt = if (isCreatingNew) 0L else (initialConfig?.limitAmount ?: category?.defaultLimitAmount ?: 0L)
        mutableStateOf(if (defaultAmt > 0L) defaultAmt.toString() else "")
    }
    var selectedPeriod by remember(initialConfig, category, isCreatingNew) {
        mutableStateOf(
            if (isCreatingNew) "BULANAN" else (initialConfig?.period ?: category?.defaultLimitPeriod ?: "BULANAN")
        )
    }
    var startDateMillis by remember(defaultStartMillis) { mutableLongStateOf(defaultStartMillis) }
    var endDateMillis by remember(defaultEndMillis) { mutableLongStateOf(defaultEndMillis) }

    fun openCalendarPicker(initialMillis: Long, onPicked: (Long, Boolean) -> Unit, isEndOfDay: Boolean) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    if (isEndOfDay) {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    } else {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }
                onPicked(pickedCal.timeInMillis, isEndOfDay)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCreatingNew) {
                            if (isEn) "Add Spending Category" else "Tambah Kategori Pengeluaran"
                        } else {
                            if (isEn) "Spending Limit Settings" else "Atur Batas Kategori"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = BcaRoyalBlue
                    )
                    Text(
                        text = if (isEn) {
                            "Set category name, max budget (${countryProfile.currencyCode}), and period"
                        } else {
                            "Atur nama kategori, batas maksimal (${countryProfile.currencyCode}), dan waktunya"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isCreatingNew && initialConfig?.isCustomCategory == true && category != null && onDeleteCategory != null) {
                    IconButton(
                        onClick = {
                            onDeleteCategory(category.key)
                            onDismiss()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = if (isEn) "Delete Category" else "Hapus Kategori",
                            tint = CoralExpense,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameStr,
                    onValueChange = { nameStr = it },
                    label = { Text(if (isEn) "Category Name" else "Nama Kategori", fontSize = 14.sp) },
                    placeholder = { Text(if (isEn) "e.g., Daily Essentials" else "Cth: Kebutuhan Harian") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_edit_input")
                )

                OutlinedTextField(
                    value = subtitleStr,
                    onValueChange = { subtitleStr = it },
                    label = { Text(if (isEn) "Short Description" else "Keterangan Singkat", fontSize = 14.sp) },
                    placeholder = { Text(if (isEn) "e.g., Meals, Fuel, Transit" else "Cth: Makan, Bensin, Harian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { input ->
                        val normalized = ArusKasViewModel.sanitizeMoneyInput(input)
                        limitStr = normalized
                        if (ArusKasViewModel.safeParseMoney(normalized) > 0L && selectedPeriod == "BEBAS") {
                            selectedPeriod = "BULANAN"
                        }
                    },
                    label = { Text(if (isEn) "Max Limit (${countryProfile.currencyCode})" else "Batas Maksimal (${countryProfile.currencyCode})", fontSize = 14.sp) },
                    placeholder = { Text(if (isEn) "0 (No limit)" else "0 (Tanpa batas)") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold, color = BcaRoyalBlue, fontSize = 16.sp) },
                    visualTransformation = RupiahThousandsVisualTransformation(countryProfile.thousandsSeparator),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_limit_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetLimits.forEach { preset ->
                        val isChosen = limitStr == preset.toString()
                        Surface(
                            onClick = {
                                limitStr = preset.toString()
                                if (selectedPeriod == "BEBAS") {
                                    selectedPeriod = "BULANAN"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChosen) BcaRoyalBlue.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.2.dp, if (isChosen) BcaRoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.heightIn(min = 42.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = ArusKasViewModel.formatMoney(preset),
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                    color = if (isChosen) BcaRoyalBlue else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // STANDARD FREE FOREVER PERIODS (BULANAN, MINGGUAN, HARIAN, BEBAS)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "Reset Period (Free)" else "Waktu Reset Batas (Gratis)",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "BULANAN" to (if (isEn) "Monthly" else "Bulanan"),
                            "MINGGUAN" to (if (isEn) "Weekly" else "Mingguan"),
                            "HARIAN" to (if (isEn) "Daily" else "Harian"),
                            "BEBAS" to (if (isEn) "No Limit" else "Bebas")
                        ).forEach { (periodCode, periodLbl) ->
                            val isSelectedStd = selectedPeriod == periodCode
                            Surface(
                                onClick = {
                                    selectedPeriod = periodCode
                                    if (periodCode == "BEBAS") {
                                        limitStr = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelectedStd) EmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.2.dp, if (isSelectedStd) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .testTag("period_chip_$periodCode")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = periodLbl,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                        color = if (isSelectedStd) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // CALENDAR TOOL: START DATE -> END DATE FOR SPENDING LIMIT (PRO FEATURE AFTER TRIAL)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = if (isEn) "Limit Calendar" else "Kalender Limit",
                                    tint = BcaRoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isEn) "Custom Date Range" else "Pilih Tanggal Sendiri (Gajian)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BcaRoyalBlue
                                )
                            }
                            if (!canUseCustomRange) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = GoldAccent.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "PRO",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldAccent
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = {
                                    if (!canUseCustomRange) {
                                        onOpenPaywallWithTrigger(ProFeatureTrigger.CUSTOM_DATE_RANGE_LIMIT)
                                    } else {
                                        openCalendarPicker(
                                            initialMillis = startDateMillis,
                                            isEndOfDay = false,
                                            onPicked = { picked, _ ->
                                                startDateMillis = picked
                                                selectedPeriod = "RENTANG_TANGGAL"
                                                if (endDateMillis < picked) {
                                                    val calEnd = Calendar.getInstance().apply {
                                                        timeInMillis = picked
                                                        add(Calendar.DAY_OF_YEAR, 30)
                                                        set(Calendar.HOUR_OF_DAY, 23)
                                                        set(Calendar.MINUTE, 59)
                                                        set(Calendar.SECOND, 59)
                                                        set(Calendar.MILLISECOND, 999)
                                                    }
                                                    endDateMillis = calEnd.timeInMillis
                                                }
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp)
                                    .testTag("calendar_start_date_button")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isEn) "Start Date:" else "Dari Tanggal:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFmt.format(Date(startDateMillis)),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BcaRoyalBlue
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    if (!canUseCustomRange) {
                                        onOpenPaywallWithTrigger(ProFeatureTrigger.CUSTOM_DATE_RANGE_LIMIT)
                                    } else {
                                        openCalendarPicker(
                                            initialMillis = endDateMillis,
                                            isEndOfDay = true,
                                            onPicked = { picked, _ ->
                                                endDateMillis = maxOf(picked, startDateMillis)
                                                selectedPeriod = "RENTANG_TANGGAL"
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp)
                                    .testTag("calendar_end_date_button")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isEn) "End Date:" else "Sampai Tanggal:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFmt.format(Date(endDateMillis)),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                (if (isEn) "7 Days" else "7 Hari") to 7,
                                (if (isEn) "14 Days" else "14 Hari") to 14,
                                (if (isEn) "30 Days" else "30 Hari") to 30
                            ).forEach { (lbl, days) ->
                                Surface(
                                    onClick = {
                                        if (!canUseCustomRange) {
                                            onOpenPaywallWithTrigger(ProFeatureTrigger.CUSTOM_DATE_RANGE_LIMIT)
                                        } else {
                                            val calStart = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            val calEnd = (calStart.clone() as Calendar).apply {
                                                add(Calendar.DAY_OF_YEAR, days - 1)
                                                set(Calendar.HOUR_OF_DAY, 23)
                                                set(Calendar.MINUTE, 59)
                                                set(Calendar.SECOND, 59)
                                                set(Calendar.MILLISECOND, 999)
                                            }
                                            startDateMillis = calStart.timeInMillis
                                            endDateMillis = calEnd.timeInMillis
                                            selectedPeriod = "RENTANG_TANGGAL"
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, BcaCobaltBlue.copy(alpha = 0.45f)),
                                    modifier = Modifier.heightIn(min = 38.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = lbl,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                            color = BcaCobaltBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = nameStr.trim().ifBlank { category?.label ?: (if (isEn) "New Category" else "Kategori Baru") }
                    val cleanSub = subtitleStr.trim().ifBlank { category?.subtitle ?: (if (isEn) "Spending Category" else "Pos Pengeluaran") }
                    val key = if (isCreatingNew || category == null) {
                        "custom_cat_${System.currentTimeMillis()}"
                    } else {
                        category.key
                    }
                    val isCustomDateRange = selectedPeriod == "RENTANG_TANGGAL"
                    val amt = if (selectedPeriod == "BEBAS") 0L else (limitStr.toLongOrNull() ?: 0L)
                    onSave(
                        key,
                        cleanName,
                        cleanSub,
                        amt,
                        selectedPeriod,
                        if (isCustomDateRange) startDateMillis else 0L,
                        if (isCustomDateRange) endDateMillis else 0L,
                        isCreatingNew || (initialConfig?.isCustomCategory == true)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BcaRoyalBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_category_limit_button")
            ) {
                Text(if (isEn) "Save" else "Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "Batal")
            }
        }
    )
}

/**
 * 3. TRANSACTION HISTORY (DENSE EXCEL-STYLE TABLE WITH FIXED HEIGHT & HIDEABLE)
 * - Formal & general title: "Transaction History" (English default) / "Riwayat Transaksi" (Indonesian)
 * - Resembles an Excel spreadsheet (Time | Item | Category | Account | Amount).
 * - Full numbers (never abbreviated) in active country's currency.
 * - Chronologically sorted by date ascending: Yesterday (top) -> Today -> Tomorrow / Future (bottom).
 * - Fixed max height (168.dp) and hideable so Savings Target & Mandatory Needs below never shift.
 */
@Composable
fun DailySeparatedTransactionTablesSection(
    transactions: List<TransactionRecord>,
    wallets: List<Wallet> = emptyList(),
    goals: List<SavingsGoal> = emptyList(),
    emergencyFund: EmergencyFundConfig = EmergencyFundConfig(),
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    isDarkMode: Boolean,
    hideBalance: Boolean = false,
    onDeleteTransaction: (TransactionRecord) -> Unit,
    onOpenMoneyInputPopup: () -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val dayKeyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val fullHeaderDateFmt = remember(lang) { SimpleDateFormat("EEEE, dd MMM yyyy", activeLocale(lang)) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.US) }

    var showFullExcelModal by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    fun formatFullMoney(amount: Long): String {
        return if (hideBalance) "${countryProfile.currencySymbol} ••••••" else ArusKasViewModel.formatMoney(amount)
    }

    val todayStart = remember { ArusKasViewModel.getStartOfDayMillis() }
    val yesterdayStart = todayStart - 86_400_000L
    val tomorrowStart = todayStart + 86_400_000L
    val dayAfterTomorrowStart = todayStart + (2 * 86_400_000L)

    fun relativeDayBadge(millis: Long): Pair<String, Color> {
        return when {
            millis in yesterdayStart until todayStart -> (if (isEn) "Yesterday" else "Kemarin") to GoldAccent
            millis in todayStart until tomorrowStart -> (if (isEn) "Today" else "Hari Ini") to BcaRoyalBlue
            millis in tomorrowStart until dayAfterTomorrowStart -> (if (isEn) "Tomorrow" else "Besok") to SkyBlue
            millis < yesterdayStart -> (if (isEn) "Earlier" else "Sebelumnya") to BcaCobaltBlue
            else -> (if (isEn) "Upcoming" else "Mendatang") to SkyBlue
        }
    }

    val groupedByDayDescending = remember(transactions) {
        transactions
            .sortedByDescending { it.transactionDateMillis }
            .groupBy { tx -> dayKeyFmt.format(Date(tx.transactionDateMillis)) }
            .entries
            .toList()
    }
    val groupedByDayAscending = remember(transactions) {
        transactions
            .sortedBy { it.transactionDateMillis }
            .groupBy { tx -> dayKeyFmt.format(Date(tx.transactionDateMillis)) }
            .entries
            .toList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_transaction_tables_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Clear Header + Report & Full Table Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Transaction History" else "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEn) {
                        "${transactions.size} recorded entries"
                    } else {
                        "${transactions.size} catatan keluar masuk uang"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { showReportDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("open_save_report_dialog_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = if (isEn) "Save Report" else "Simpan Laporan",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isEn) "Report" else "Laporan",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (transactions.isNotEmpty()) {
                    Surface(
                        onClick = { showFullExcelModal = true },
                        shape = RoundedCornerShape(12.dp),
                        color = BcaRoyalBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("open_full_excel_history_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableView,
                                contentDescription = if (isEn) "View Table" else "Lihat Tabel",
                                tint = BcaRoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) "Table" else "Tabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = BcaRoyalBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (groupedByDayDescending.isEmpty()) {
            Card(
                onClick = onOpenMoneyInputPopup,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
                border = BorderStroke(1.2.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_daily_table_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BcaRoyalBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = BcaRoyalBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isEn) "No transactions yet" else "Belum ada catatan transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) {
                            "Tap here or press '+ Record' below to log your first expense or income."
                        } else {
                            "Ketuk di sini atau tombol '+ Catat' di bawah untuk mencatat pemasukan & pengeluaran pertama."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                groupedByDayDescending.forEach { (dayKey, dayTxs) ->
                    val sampleMillis = dayTxs.first().transactionDateMillis
                    val fullDateHeader = fullHeaderDateFmt.format(Date(sampleMillis))
                    val (badgeLabel, badgeColor) = relativeDayBadge(sampleMillis)
                    val dayExpense = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val dayIncome = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = glassmorphicBorderStroke(accentColor = badgeColor, width = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphicCardEffect(cornerRadius = 18.dp, accentTint = badgeColor, isDark = isDarkMode)
                            .testTag("daily_table_card_$dayKey")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Day Header
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
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeColor
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = fullDateHeader,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Daily Totals Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (dayExpense > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CoralExpense.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = if (isEn) "Out: -${formatFullMoney(dayExpense)}" else "Keluar: -${formatFullMoney(dayExpense)}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 12.5.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = CoralExpense,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (dayIncome > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = if (isEn) "In: +${formatFullMoney(dayIncome)}" else "Masuk: +${formatFullMoney(dayIncome)}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 12.5.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Spacious Friendly Transaction Items
                            dayTxs.forEach { tx ->
                                val isIncome = tx.type == "INCOME"
                                val isSavings = tx.type == "SAVINGS_ALLOCATION"
                                val isTransfer = tx.type == "TRANSFER"
                                val rowColor = when {
                                    isIncome -> EmeraldPrimary
                                    isSavings -> GoldAccent
                                    isTransfer -> SkyBlue
                                    else -> CoralExpense
                                }
                                val signPrefix = when {
                                    isIncome -> "+"
                                    isTransfer -> "⇄ "
                                    else -> "-"
                                }
                                val jamStr = timeFmt.format(Date(tx.transactionDateMillis))
                                val catName = normalizeFormalCategoryLabel(tx.categoryKey, tx.categoryLabel, lang)

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("table_row_tx_${tx.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = rowColor.copy(alpha = 0.14f),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = iconForCategory(tx.categoryKey),
                                                    contentDescription = catName,
                                                    tint = rowColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = tx.note.ifBlank { catName },
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$catName • ${tx.walletName.ifBlank { "-" }} • $jamStr",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "$signPrefix${formatFullMoney(tx.amount)}",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontSize = 15.sp,
                                                    fontFamily = SpaceGroteskFontFamily
                                                ),
                                                color = rowColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteTransaction(tx) },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .testTag("delete_tx_${tx.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = if (isEn) "Delete Transaction" else "Hapus Transaksi",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
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
    }

    if (showFullExcelModal) {
        AlertDialog(
            onDismissRequest = { showFullExcelModal = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Transaction Table" else "Tabel Transaksi Lengkap",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = BcaRoyalBlue
                        )
                        Text(
                            text = if (isEn) {
                                "Sorted oldest to newest by date"
                            } else {
                                "Urutan tanggal terlama hingga terbaru"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showFullExcelModal = false }, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup", modifier = Modifier.size(22.dp))
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedByDayAscending.forEach { (dayKey, dayTxs) ->
                        ExcelDailySheetBlock(
                            dayKey = dayKey,
                            dayTxs = dayTxs,
                            fullHeaderDateFmt = fullHeaderDateFmt,
                            timeFmt = timeFmt,
                            relativeDayBadge = ::relativeDayBadge,
                            formatFullMoney = ::formatFullMoney,
                            onDeleteTransaction = onDeleteTransaction,
                            onClickTable = {}
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFullExcelModal = false }) {
                    Text(if (isEn) "Close" else "Tutup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        )
    }

    if (showReportDialog) {
        TransactionReportSaveDialog(
            transactions = transactions,
            wallets = wallets,
            goals = goals,
            emergencyFund = emergencyFund,
            subscription = subscription,
            onOpenPaywallWithTrigger = onOpenPaywallWithTrigger,
            isDarkMode = isDarkMode,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
fun TransactionReportSaveDialog(
    transactions: List<TransactionRecord>,
    wallets: List<Wallet> = emptyList(),
    goals: List<SavingsGoal> = emptyList(),
    emergencyFund: EmergencyFundConfig = EmergencyFundConfig(),
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, THIS_MONTH, 7_DAYS

    val now = remember { System.currentTimeMillis() }
    val thisMonthStart = remember(now) {
        java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sevenDaysAgo = remember(now) { now - (7L * 86_400_000L) }

    val filteredList = remember(transactions, selectedFilter) {
        val list = when (selectedFilter) {
            "THIS_MONTH" -> transactions.filter { it.transactionDateMillis >= thisMonthStart }
            "7_DAYS" -> transactions.filter { it.transactionDateMillis >= sevenDaysAgo }
            else -> transactions
        }
        list.sortedByDescending { it.transactionDateMillis }
    }

    val totalInflow = remember(filteredList) {
        filteredList.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalOutflow = remember(filteredList) {
        filteredList.filter { it.type == "EXPENSE" || it.type == "SAVINGS_ALLOCATION" }.sumOf { it.amount }
    }
    val netCashflow = totalInflow - totalOutflow

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = BcaRoyalBlue, modifier = Modifier.size(24.dp))
                        Text(
                            text = AppLocalization.get("report_modal_title", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = BcaRoyalBlue
                        )
                    }
                    Text(
                        text = AppLocalization.get("report_modal_subtitle", lang),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(22.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to AppLocalization.get("report_filter_all", lang),
                        "THIS_MONTH" to AppLocalization.get("report_filter_this_month", lang),
                        "7_DAYS" to AppLocalization.get("report_filter_7days", lang)
                    ).forEach { (fKey, fLabel) ->
                        val isSel = selectedFilter == fKey
                        Surface(
                            onClick = { selectedFilter = fKey },
                            shape = RoundedCornerShape(50),
                            color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = fLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Summary Cashflow Banner
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = AppLocalization.get("report_total_inflow", lang), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = EmeraldPrimary)
                            Text(text = ArusKasViewModel.formatMoney(totalInflow), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = AppLocalization.get("report_total_outflow", lang), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = CoralExpense)
                            Text(text = ArusKasViewModel.formatMoney(totalOutflow), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp), fontWeight = FontWeight.Bold, color = CoralExpense)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = AppLocalization.get("report_net_cashflow", lang), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = BcaRoyalBlue)
                            Text(text = ArusKasViewModel.formatMoney(netCashflow), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp), fontWeight = FontWeight.Bold, color = BcaRoyalBlue)
                        }
                    }
                }

                // Clean Structured Table
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BcaRoyalBlue.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isEn) "Date & Item" else "Tanggal & Rincian", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), fontWeight = FontWeight.Bold, color = BcaRoyalBlue, modifier = Modifier.weight(1.8f))
                            Text(text = if (isEn) "Account" else "Dompet", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), fontWeight = FontWeight.Bold, color = BcaRoyalBlue, modifier = Modifier.weight(1f))
                            Text(text = if (isEn) "Amount" else "Nominal", style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), fontWeight = FontWeight.Bold, color = BcaRoyalBlue, modifier = Modifier.weight(1.3f))
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AppLocalization.get("report_no_data", lang),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            filteredList.take(60).forEachIndexed { idx, tx ->
                                val isIn = tx.type == "INCOME"
                                val bg = if (idx % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bg)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.8f)) {
                                        Text(text = dateFmt.format(Date(tx.transactionDateMillis)), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = tx.categoryLabel.ifBlank { tx.note.ifBlank { "Item" } }, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(text = tx.walletName.ifBlank { "-" }, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = (if (isIn) "+ " else "- ") + ArusKasViewModel.formatMoney(tx.amount),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIn) EmeraldPrimary else CoralExpense,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                }
                                if (idx < filteredList.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                }
                            }
                        }
                    }
                }

                // Action Export Buttons (CSV & Copy Text = 100% Free Forever | Styled A4 PDF = PRO)
                val canExportPdf = ArusKasFeatureGateEvaluator.canExportStyledPdf(subscription)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val res = ModernPdfBackupGenerator.generateCsvReport(context, filteredList, lang)
                            if (res.isSuccess) {
                                val f = java.io.File(res.filePath)
                                if (f.exists()) {
                                    ModernPdfBackupGenerator.openOrShareCsv(context, f)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_csv_action_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppLocalization.get("report_export_csv", lang) + (if (isEn) " (FREE)" else " (GRATIS)"),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (canExportPdf) {
                                val res = ModernPdfBackupGenerator.generateModernLedgerPdf(
                                    context = context,
                                    wallets = wallets,
                                    transactions = filteredList,
                                    goals = goals,
                                    emergencyFund = emergencyFund,
                                    lang = lang
                                )
                                if (res.isSuccess) {
                                    val f = java.io.File(res.filePath)
                                    if (f.exists()) {
                                        ModernPdfBackupGenerator.openOrSharePdf(context, f)
                                    }
                                }
                            } else {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.PDF_EXPORT)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_pdf_action_button")
                    ) {
                        Icon(
                            imageVector = if (canExportPdf) Icons.Default.PictureAsPdf else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) {
                                if (canExportPdf) "Export A4 PDF Report" else "Export A4 PDF Report (PRO)"
                            } else {
                                if (canExportPdf) "Cetak Laporan PDF A4" else "Cetak Laporan PDF A4 (PRO)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val sb = java.lang.StringBuilder()
                            sb.append("=== ${AppLocalization.get("report_modal_title", lang)} ===\n")
                            sb.append("${AppLocalization.get("report_total_inflow", lang)}: ${ArusKasViewModel.formatMoney(totalInflow)}\n")
                            sb.append("${AppLocalization.get("report_total_outflow", lang)}: ${ArusKasViewModel.formatMoney(totalOutflow)}\n")
                            sb.append("${AppLocalization.get("report_net_cashflow", lang)}: ${ArusKasViewModel.formatMoney(netCashflow)}\n\n")
                            filteredList.forEach { tx ->
                                val sign = if (tx.type == "INCOME") "+ " else "- "
                                sb.append("${dateFmt.format(Date(tx.transactionDateMillis))} | ${tx.categoryLabel} | ${tx.walletName} | $sign${ArusKasViewModel.formatMoney(tx.amount)}\n")
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ArusKas Statement", sb.toString()))
                            Toast.makeText(context, AppLocalization.get("msg_copied_clipboard", lang), Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("copy_text_summary_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BcaRoyalBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = AppLocalization.get("report_copy_text", lang), style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = BcaRoyalBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isEn) "Close" else "Tutup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    )
}

@Composable
private fun ExcelDailySheetBlock(
    dayKey: String,
    dayTxs: List<TransactionRecord>,
    fullHeaderDateFmt: SimpleDateFormat,
    timeFmt: SimpleDateFormat,
    relativeDayBadge: (Long) -> Pair<String, Color>,
    formatFullMoney: (Long) -> String,
    onDeleteTransaction: (TransactionRecord) -> Unit,
    onClickTable: () -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val sampleMillis = dayTxs.first().transactionDateMillis
    val fullDateHeader = fullHeaderDateFmt.format(Date(sampleMillis))
    val (badgeLabel, badgeColor) = relativeDayBadge(sampleMillis)

    val dayExpense = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val dayIncome = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
    val gridBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClickTable() }
    ) {
        ExcelDayHeaderRow(
            badgeLabel = badgeLabel,
            badgeColor = badgeColor,
            fullDateHeader = fullDateHeader,
            txCount = dayTxs.size
        )

        ExcelColumnHeaderRow()

        dayTxs.forEachIndexed { idx, tx ->
            val isIncome = tx.type == "INCOME"
            val isSavings = tx.type == "SAVINGS_ALLOCATION"
            val isTransfer = tx.type == "TRANSFER"
            val rowColor = when {
                isIncome -> EmeraldPrimary
                isSavings -> GoldAccent
                isTransfer -> SkyBlue
                else -> CoralExpense
            }
            val signPrefix = when {
                isIncome -> "+"
                isTransfer -> ""
                else -> "-"
            }
            val jamStr = timeFmt.format(Date(tx.transactionDateMillis))
            val rowBg = if (idx % 2 == 0) {
                MaterialTheme.colorScheme.surface
            } else {
                BcaRoyalBlue.copy(alpha = 0.035f)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(rowBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jamStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontFamily = SpaceGroteskFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(46.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
                ExcelCellVerticalDivider(gridBorderColor)

                Text(
                    text = tx.note.ifBlank { tx.categoryLabel },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1.25f)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                )
                ExcelCellVerticalDivider(gridBorderColor)

                Text(
                    text = tx.walletName.replace("Bank ", ""),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BcaRoyalBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(56.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                )
                ExcelCellVerticalDivider(gridBorderColor)

                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$signPrefix${formatFullMoney(tx.amount)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = rowColor,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = gridBorderColor)
        }

        ExcelDaySummaryFooterRow(
            dayExpense = dayExpense,
            dayIncome = dayIncome,
            formatFullMoney = formatFullMoney
        )
    }
}

@Composable
private fun ExcelCellVerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(0.6.dp)
            .background(color)
    )
}

@Composable
private fun ExcelDayHeaderRow(
    badgeLabel: String,
    badgeColor: Color,
    fullDateHeader: String,
    txCount: Int
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(badgeColor.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = fullDateHeader,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = if (isEn) "$txCount Items" else "$txCount Catatan",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
            color = badgeColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExcelColumnHeaderRow() {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val currencySymbol = WorldCountryCurrencyRegistry.getActiveProfile().currencySymbol
    val headerBorder = BcaRoyalBlue.copy(alpha = 0.25f)
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 0.6.dp, color = headerBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(BcaRoyalBlue.copy(alpha = 0.08f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "Time" else "Jam",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.ExtraBold,
                color = BcaRoyalBlue,
                modifier = Modifier
                    .width(46.dp)
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            ExcelCellVerticalDivider(headerBorder)
            Text(
                text = if (isEn) "Item" else "Keterangan",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.ExtraBold,
                color = BcaRoyalBlue,
                modifier = Modifier
                    .weight(1.25f)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
            ExcelCellVerticalDivider(headerBorder)
            Text(
                text = if (isEn) "Account" else "Dompet",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.ExtraBold,
                color = BcaRoyalBlue,
                modifier = Modifier
                    .width(56.dp)
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            ExcelCellVerticalDivider(headerBorder)
            Text(
                text = if (isEn) "Amount ($currencySymbol)" else "Nominal ($currencySymbol)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.ExtraBold,
                color = BcaRoyalBlue,
                modifier = Modifier
                    .weight(1.3f)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                textAlign = TextAlign.End
            )
        }
        HorizontalDivider(thickness = 0.6.dp, color = headerBorder)
    }
}

@Composable
private fun ExcelDaySummaryFooterRow(
    dayExpense: Long,
    dayIncome: Long,
    formatFullMoney: (Long) -> String
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BcaRoyalBlue.copy(alpha = 0.06f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isEn) "Out: -${formatFullMoney(dayExpense)}" else "Keluar: -${formatFullMoney(dayExpense)}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontFamily = SpaceGroteskFontFamily
            ),
            color = CoralExpense,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (isEn) "In: +${formatFullMoney(dayIncome)}" else "Masuk: +${formatFullMoney(dayIncome)}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontFamily = SpaceGroteskFontFamily
            ),
            color = EmeraldPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * 4. SIDE-BY-SIDE ROW BELOW TRANSACTION HISTORY:
 *    - LEFT: Savings Target ("Tabungan Target")
 *    - RIGHT: Mandatory Needs ("Kebutuhan Wajib")
 */
@Composable
fun SideBySideTargetAndBillsRow(
    activeGoal: SavingsGoal?,
    allGoals: List<SavingsGoal>,
    mandatoryBills: List<MandatoryBillItem>,
    selectedWallet: Wallet?,
    isDarkMode: Boolean,
    onOpenTargetDetailModal: () -> Unit,
    onOpenCreateTargetModal: () -> Unit,
    onToggleBillPaidStatus: (MandatoryBillItem, String) -> Unit,
    onAddBillClick: () -> Unit,
    onEditBillClick: (MandatoryBillItem) -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val dateShortFmt = remember(lang) { SimpleDateFormat("dd MMM yyyy", activeLocale(lang)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("side_by_side_target_and_bills_row"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. SAVINGS TARGET (FULL-WIDTH SPACIOUS CARD)
        val targetAmt = (activeGoal?.targetAmount ?: 1L).coerceAtLeast(1L)
        val savedAmt = activeGoal?.savedAmount ?: 0L
        val shortfallAmt = (targetAmt - savedAmt).coerceAtLeast(0L)
        val progress = if (activeGoal != null) (savedAmt.toFloat() / targetAmt.toFloat()).coerceIn(0f, 1f) else 0f
        val pct = (progress * 100f).toInt()
        val startStr = remember(activeGoal?.startDateMillis, lang) {
            dateShortFmt.format(Date(activeGoal?.startDateMillis ?: System.currentTimeMillis()))
        }

        Card(
            onClick = onOpenTargetDetailModal,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = glassmorphicBorderStroke(accentColor = EmeraldPrimary, width = 1.2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCardEffect(cornerRadius = 20.dp, accentTint = EmeraldPrimary, isDark = isDarkMode)
                .testTag("tabungan_target_minimalist_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary.copy(alpha = 0.14f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = if (isEn) "Savings Target" else "Tabungan Target",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isEn) "Savings Target" else "Tabungan Target",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = if (isEn) "Started: $startStr" else "Mulai menabung: $startStr",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = onOpenCreateTargetModal,
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("quick_add_target_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isEn) "New Target" else "Target Baru",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) "New Target" else "Target Baru",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeGoal?.title ?: (if (isEn) "Create Savings Goal" else "Buat Target Impian"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeGoal != null) {
                                ArusKasViewModel.formatMoney(activeGoal.targetAmount)
                            } else {
                                if (isEn) "Tap '+ New Target' to begin" else "Ketuk '+ Target Baru' untuk mulai"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            ),
                            color = BcaRoyalBlue
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EmeraldPrimary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    color = EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isEn) "Saved" else "Sudah Terkumpul",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ArusKasViewModel.formatMoney(savedAmt),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.5.sp,
                                fontFamily = SpaceGroteskFontFamily
                            ),
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isEn) "Remaining" else "Kurang Lagi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ArusKasViewModel.formatMoney(shortfallAmt),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.5.sp,
                                fontFamily = SpaceGroteskFontFamily
                            ),
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Button(
                    onClick = onOpenTargetDetailModal,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Deposit / Manage Target" else "Setor Uang / Atur Target",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.5.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. MANDATORY NEEDS (FULL-WIDTH SPACIOUS BILL CHECKLIST)
        val paidCount = remember(mandatoryBills) { mandatoryBills.count { it.status == "PAID" } }
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = glassmorphicBorderStroke(accentColor = SkyBlue, width = 1.2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphicCardEffect(cornerRadius = 20.dp, accentTint = SkyBlue, isDark = isDarkMode)
                .testTag("mandatory_bills_checklist_section")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SkyBlue.copy(alpha = 0.14f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = if (isEn) "Mandatory Needs" else "Kebutuhan Wajib",
                                    tint = SkyBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isEn) "Mandatory Bills" else "Tagihan & Kebutuhan Wajib",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = BcaRoyalBlue
                            )
                            Text(
                                text = if (isEn) {
                                    "$paidCount of ${mandatoryBills.size} paid"
                                } else {
                                    "$paidCount dari ${mandatoryBills.size} sudah dibayar"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = onAddBillClick,
                        shape = RoundedCornerShape(12.dp),
                        color = BcaRoyalBlue.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("add_mandatory_bill_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isEn) "Add Mandatory Bill" else "Tambah Tagihan Wajib",
                                tint = BcaRoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) "Add" else "Tambah",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = BcaRoyalBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (mandatoryBills.isEmpty()) {
                    Text(
                        text = if (isEn) "No monthly bills yet. Tap '+ Add' to list rent, Wi-Fi, or electricity." else "Belum ada daftar tagihan rutin. Ketuk '+ Tambah' untuk mencatat listrik, kos, atau Wi-Fi.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        mandatoryBills.forEach { bill ->
                            val isPaid = bill.status == "PAID"
                            val rowColor = if (isPaid) EmeraldPrimary else MaterialTheme.colorScheme.onSurface

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isPaid) EmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPaid) EmeraldPrimary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mandatory_bill_row_${bill.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val nextStatus = if (isPaid) "UNPAID" else "PAID"
                                            onToggleBillPaidStatus(bill, nextStatus)
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("checkbox_bill_${bill.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isPaid) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = "Check ${bill.name}",
                                            tint = if (isPaid) EmeraldPrimary else BcaRoyalBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onEditBillClick(bill) },
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = bill.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.5.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = rowColor,
                                            textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isEn) {
                                                "Due day ${bill.dueDayOfMonth} • ${ArusKasViewModel.formatMoney(bill.totalAmount)}"
                                            } else {
                                                "Jatuh tempo tgl ${bill.dueDayOfMonth} • ${ArusKasViewModel.formatMoney(bill.totalAmount)}"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 13.sp,
                                                fontFamily = SpaceGroteskFontFamily
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isPaid) EmeraldPrimary.copy(alpha = 0.16f) else GoldAccent.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = if (isPaid) (if (isEn) "Paid" else "Lunas") else (if (isEn) "Unpaid" else "Belum"),
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                            color = if (isPaid) EmeraldPrimary else GoldAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onEditBillClick(bill) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = if (isEn) "Edit Bill" else "Ubah Tagihan",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
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
}

/**
 * SAVINGS TARGET DETAIL & DEPOSIT DIALOG:
 * 1. What is being purchased (Modern OutfitFontFamily + country-aware suggested targets)
 * 2. Target price below
 * 3. Saved & Remaining shortfall
 * 4. Start date (with Calendar tool)
 * 5. Deposit input tool
 * 6. Savings deposit history
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavingsTargetDetailAndDepositDialog(
    activeGoal: SavingsGoal?,
    allGoals: List<SavingsGoal>,
    wallets: List<Wallet>,
    transactions: List<TransactionRecord>,
    onSelectGoal: (SavingsGoal) -> Unit,
    onCreateNewTarget: (title: String, targetAmount: Long, startDateMillis: Long) -> Unit,
    onUpdateTargetInfo: (goalId: String, title: String, targetAmount: Long, startDateMillis: Long) -> Unit,
    onAllocateSavings: (goal: SavingsGoal, wallet: Wallet, amount: Long, note: String) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val dateFmt = remember(lang) { SimpleDateFormat("dd MMM yyyy", activeLocale(lang)) }
    val dateTimeFmt = remember(lang) { SimpleDateFormat("dd MMM yyyy • HH:mm", activeLocale(lang)) }

    val suggestedTargets = remember(countryProfile.countryCode, lang) {
        WorldCountryCurrencyRegistry.suggestedSavingsTargetsForCountry(countryProfile.countryCode, lang)
    }
    val defaultSuggestedPrice = suggestedTargets.firstOrNull()?.second ?: 1000L

    var isEditingTargetMeta by remember { mutableStateOf(activeGoal == null) }
    var targetNameStr by remember(activeGoal) { mutableStateOf(activeGoal?.title ?: "") }
    var targetPriceStr by remember(activeGoal, defaultSuggestedPrice) {
        mutableStateOf((activeGoal?.targetAmount ?: defaultSuggestedPrice).toString())
    }
    var targetStartMillis by remember(activeGoal) {
        mutableLongStateOf(activeGoal?.startDateMillis ?: System.currentTimeMillis())
    }

    var depositAmountStr by remember { mutableStateOf("") }
    var selectedWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }

    val savedAmt = activeGoal?.savedAmount ?: 0L
    val targetAmt = (targetPriceStr.filter { it.isDigit() }.toLongOrNull() ?: activeGoal?.targetAmount ?: defaultSuggestedPrice).coerceAtLeast(1L)
    val shortfallAmt = (targetAmt - savedAmt).coerceAtLeast(0L)
    val progress = (savedAmt.toFloat() / targetAmt.toFloat()).coerceIn(0f, 1f)
    val pct = (progress * 100f).toInt()

    val goalHistory = remember(transactions, activeGoal) {
        transactions.filter { tx ->
            tx.type == "SAVINGS_ALLOCATION" &&
                (activeGoal == null || tx.categoryLabel.contains(activeGoal.title, ignoreCase = true) || tx.note.contains(activeGoal.title, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isEn) "Savings Target" else "Tabungan Target",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = BcaRoyalBlue
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup", modifier = Modifier.size(22.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (allGoals.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allGoals.forEach { g ->
                            val isSel = g.id == activeGoal?.id
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    onSelectGoal(g)
                                    isEditingTargetMeta = false
                                },
                                label = { Text(g.title, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)) }
                            )
                        }
                    }
                }

                // 1. WHAT IS BEING PURCHASED & 2. TARGET PRICE BELOW
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BcaRoyalBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = if (isEn) "Target Item:" else "Target Impian:",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = BcaRoyalBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                            TextButton(
                                onClick = { isEditingTargetMeta = !isEditingTargetMeta },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isEditingTargetMeta) {
                                        if (isEn) "Done" else "Selesai"
                                    } else {
                                        if (isEn) "Edit Target / Price" else "Ubah Target / Harga"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                    color = BcaRoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = targetNameStr.ifBlank { if (isEn) "Enter Target Name..." else "Ketik Nama Target..." },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Price: ${ArusKasViewModel.formatMoney(targetAmt)}"
                            } else {
                                "Harga: ${ArusKasViewModel.formatMoney(targetAmt)}"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = EmeraldPrimary
                        )

                        if (isEditingTargetMeta) {
                            HorizontalDivider()
                            Text(
                                text = if (isEn) "Suggested Target Names:" else "Saran Nama Target:",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                suggestedTargets.forEach { (suggestedTitle, suggestedPrice) ->
                                    val isChosen = targetNameStr == suggestedTitle
                                    Surface(
                                        onClick = {
                                            targetNameStr = suggestedTitle
                                            targetPriceStr = suggestedPrice.toString()
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = if (isChosen) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isChosen) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = suggestedTitle,
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                color = if (isChosen) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = targetNameStr,
                                onValueChange = { targetNameStr = it },
                                label = { Text(if (isEn) "Item / Target Name" else "Nama Barang / Target", fontSize = 14.sp) },
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("target_name_input")
                            )

                            OutlinedTextField(
                                value = targetPriceStr,
                                onValueChange = { targetPriceStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                                label = { Text(if (isEn) "Target Price (${countryProfile.currencyCode})" else "Harga Target (${countryProfile.currencyCode})", fontSize = 14.sp) },
                                prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                                visualTransformation = RupiahThousandsVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("target_price_input")
                            )

                            Button(
                                onClick = {
                                    val cleanName = targetNameStr.trim().ifBlank { if (isEn) "Dream Goal" else "Target Impian" }
                                    val cleanPrice = (targetPriceStr.toLongOrNull() ?: defaultSuggestedPrice).coerceAtLeast(1L)
                                    if (activeGoal == null) {
                                        onCreateNewTarget(cleanName, cleanPrice, targetStartMillis)
                                    } else {
                                        onUpdateTargetInfo(activeGoal.id, cleanName, cleanPrice, targetStartMillis)
                                    }
                                    isEditingTargetMeta = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "Save Target" else "Simpan Target", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                // 3. SAVED & REMAINING SHORTFALL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isEn) "Saved" else "Terkumpul",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ArusKasViewModel.formatMoney(savedAmt),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isEn) "Remaining" else "Kekurangan",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ArusKasViewModel.formatMoney(shortfallAmt),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = GoldAccent
                            )
                        }
                    }
                }

                // 4. START DATE (WITH CALENDAR)
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = targetStartMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val picked = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
                                targetStartMillis = picked
                                if (activeGoal != null) {
                                    onUpdateTargetInfo(activeGoal.id, targetNameStr.ifBlank { activeGoal.title }, targetAmt, picked)
                                }
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Started: ${dateFmt.format(Date(targetStartMillis))} (Tap to change)"
                                } else {
                                    "Mulai: ${dateFmt.format(Date(targetStartMillis))} (Ketuk ubah)"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = BcaRoyalBlue
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = EmeraldPrimary,
                            trackColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }

                // 5. SAVINGS DEPOSIT INPUT TOOL
                if (activeGoal != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isEn) "Add Savings Deposit" else "Tambah Setoran Tabungan",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                wallets.forEach { w ->
                                    val isSel = w.id == selectedWalletId
                                    Surface(
                                        onClick = { selectedWalletId = w.id },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) BcaRoyalBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.2.dp, if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                        modifier = Modifier.heightIn(min = 42.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "${w.name} (${ArusKasViewModel.formatMoney(w.balance)})",
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = depositAmountStr,
                                    onValueChange = { depositAmountStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                                    label = { Text(if (isEn) "Deposit (${countryProfile.currencyCode})" else "Setoran (${countryProfile.currencyCode})", fontSize = 13.sp) },
                                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
                                    visualTransformation = RupiahThousandsVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("deposit_target_amount_input")
                                )

                                Button(
                                    onClick = {
                                        val w = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                                        val amt = depositAmountStr.toLongOrNull() ?: 0L
                                        if (w != null && amt > 0L) {
                                            onAllocateSavings(
                                                activeGoal,
                                                w,
                                                amt,
                                                if (isEn) "Savings: ${activeGoal.title}" else "Tabungan: ${activeGoal.title}"
                                            )
                                            depositAmountStr = ""
                                        }
                                    },
                                    enabled = (depositAmountStr.toLongOrNull() ?: 0L) > 0L && wallets.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("confirm_deposit_target_button")
                                ) {
                                    Text(if (isEn) "Save" else "Setor", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                // 6. SAVINGS DEPOSIT HISTORY
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "Deposit History (${goalHistory.size})" else "Riwayat Setoran (${goalHistory.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = BcaRoyalBlue
                    )

                    if (goalHistory.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) "No savings deposits recorded yet." else "Belum ada riwayat setoran tabungan.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        goalHistory.forEach { tx ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) "From ${tx.walletName}" else "Dari ${tx.walletName}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dateTimeFmt.format(Date(tx.transactionDateMillis)),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+${ArusKasViewModel.formatMoney(tx.amount)}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.5.sp
                                        ),
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Close" else "Tutup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    )
}

/**
 * 5. PRIMARY QUICK-ACTION FAB & BOTTOM SHEET NOMINAL INPUT POP-UP
 */
@Composable
fun FloatingMoneyToggleButton(
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = BcaRoyalBlue,
        border = BorderStroke(1.5.dp, BcaElectricCyan),
        modifier = modifier
            .heightIn(min = 52.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(50))
            .testTag("floating_money_toggle_button")
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF004899),
                            BcaRoyalBlue,
                            EmeraldPrimary.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isOpen) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isEn) "Record Transaction" else "Catat Transaksi",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = if (isOpen) {
                    if (isEn) "Close" else "Tutup"
                } else {
                    if (isEn) "Record" else "Catat"
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomPopupNominalInputSheet(
    wallets: List<Wallet>,
    availableExpenseCategories: List<QuickCategoryItem>,
    selectedCategory: QuickCategoryItem,
    onSelectCategory: (QuickCategoryItem) -> Unit,
    selectedWalletId: String,
    onSelectWalletId: (String) -> Unit,
    nominalText: String,
    onNominalTextChange: (String) -> Unit,
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    userProfile: UserFinancialProfile,
    emergencyFund: EmergencyFundConfig,
    isSubmitting: Boolean,
    formatMoneyDisplay: (Long) -> String,
    onDismiss: () -> Unit,
    onSubmitTransaction: (
        wallet: Wallet,
        type: String,
        amount: Long,
        categoryKey: String,
        categoryLabel: String,
        frequencyTag: String,
        note: String,
        customDateMillis: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isIncomeMode = selectedCategory.type == "INCOME"
    val isEmergencyExpenseSelected = selectedCategory.key == "pakai_darurat"

    val activeAccent = when {
        isIncomeMode -> EmeraldPrimary
        isEmergencyExpenseSelected -> GoldAccent
        else -> CoralExpense
    }

    val availableCategories = if (isIncomeMode) {
        PresetCategories.localizedIncomePills(lang)
    } else {
        availableExpenseCategories + PresetCategories.localizedEmergencyCategory(lang)
    }

    var selectedDayOffset by remember { mutableStateOf(0) }
    var customPickedMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val dateShortFmt = remember(lang) { SimpleDateFormat("dd MMM yyyy", activeLocale(lang)) }

    val effectiveDateMillis = remember(selectedDayOffset, customPickedMillis) {
        when (selectedDayOffset) {
            -1 -> System.currentTimeMillis() - 86_400_000L
            1 -> System.currentTimeMillis() + 86_400_000L
            99 -> customPickedMillis
            else -> System.currentTimeMillis()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "Record Transaction" else "Catat Uang Masuk / Keluar",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup", modifier = Modifier.size(22.dp))
                }
            }

            // 1. Switcher: Expense vs Income
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isEn) "1. Transaction Type" else "1. Pilih Jenis Transaksi",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            if (isIncomeMode) {
                                onSelectCategory(availableExpenseCategories.firstOrNull() ?: PresetCategories.localizedExpenseCategories(lang).first())
                            }
                        },
                        shape = RoundedCornerShape(11.dp),
                        color = if (!isIncomeMode) CoralExpense else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("mode_switch_expense")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (!isIncomeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Expense" else "Pengeluaran",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                color = if (!isIncomeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            if (!isIncomeMode) {
                                onSelectCategory(PresetCategories.localizedIncomePills(lang).first())
                            }
                        },
                        shape = RoundedCornerShape(11.dp),
                        color = if (isIncomeMode) EmeraldPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("mode_switch_income")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isIncomeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Income" else "Pemasukan",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                color = if (isIncomeMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Select Transaction Date
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isEn) "2. Date" else "2. Tanggal",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        -1 to (if (isEn) "Yesterday" else "Kemarin"),
                        0 to (if (isEn) "Today" else "Hari Ini"),
                        1 to (if (isEn) "Tomorrow" else "Besok")
                    ).forEach { (offset, label) ->
                        val isSel = selectedDayOffset == offset
                        Surface(
                            onClick = { selectedDayOffset = offset },
                            shape = RoundedCornerShape(50),
                            color = if (isSel) BcaRoyalBlue.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSel) BcaRoyalBlue else Color.Transparent),
                            modifier = Modifier.heightIn(min = 42.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                    color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = effectiveDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val picked = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
                                    customPickedMillis = picked
                                    selectedDayOffset = 99
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(50),
                        color = if (selectedDayOffset == 99) BcaRoyalBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (selectedDayOffset == 99) BcaRoyalBlue else Color.Transparent),
                        modifier = Modifier.heightIn(min = 42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (selectedDayOffset == 99) BcaRoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (selectedDayOffset == 99) dateShortFmt.format(Date(customPickedMillis)) else (if (isEn) "Pick Date" else "Pilih Tanggal"),
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = if (selectedDayOffset == 99) BcaRoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Select Category
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isEn) "3. Category" else "3. Pilih Kategori",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { catItem ->
                        val isSel = selectedCategory.key == catItem.key
                        val formalLabel = normalizeFormalCategoryLabel(catItem.key, catItem.label, lang)
                        Surface(
                            onClick = { onSelectCategory(catItem.copy(label = formalLabel)) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) activeAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) activeAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("inline_category_chip_${catItem.key}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = iconForCategory(catItem.key),
                                    contentDescription = null,
                                    tint = if (isSel) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = formalLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                                    color = if (isSel) activeAccent else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 4. Nominal Input + Quick Shortcuts
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isEn) "4. Amount" else "4. Masukkan Jumlah Uang",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isIncomeMode && !isEmergencyExpenseSelected) {
                    val quickItems = remember(selectedCategory.key, userProfile.gender, countryProfile.countryCode, lang) {
                        WorldCountryCurrencyRegistry.quickItemsForCountry(countryProfile.countryCode, lang).filter { item ->
                            item.categoryKey == selectedCategory.key &&
                                (item.genderFilter == "ALL" || item.genderFilter == userProfile.gender)
                        }
                    }
                    if (quickItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickItems.forEach { qItem ->
                                Surface(
                                    onClick = {
                                        onNominalTextChange(qItem.suggestedAmount.toString())
                                        onNoteTextChange(qItem.label)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier.heightIn(min = 40.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${qItem.label} • ${ArusKasViewModel.formatMoney(qItem.suggestedAmount)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = { input -> onNominalTextChange(ArusKasViewModel.sanitizeMoneyInput(input)) },
                    label = {
                        Text(
                            if (isIncomeMode) {
                                if (isEn) "Income Amount (${countryProfile.currencyCode})" else "Jumlah Uang Masuk (${countryProfile.currencyCode})"
                            } else {
                                if (isEn) "Expense Amount (${countryProfile.currencyCode})" else "Jumlah Uang Keluar (${countryProfile.currencyCode})"
                            },
                            fontSize = 14.sp
                        )
                    },
                    prefix = {
                        Text(
                            text = "${countryProfile.currencySymbol} ",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            color = activeAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = activeAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nominal_input_field")
                )
            }

            // 5. Select Bank Account / Digital Wallet
            if (!isEmergencyExpenseSelected && wallets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "5. Choose Wallet / Bank" else "5. Pilih Sumber Dompet / Bank",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallets.forEach { wallet ->
                            val isSel = wallet.id == selectedWalletId
                            val wColor = parseColorSafely(wallet.colorHex)
                            Surface(
                                onClick = { onSelectWalletId(wallet.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) wColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) wColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier.heightIn(min = 44.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${wallet.name}: ${ArusKasViewModel.formatMoney(wallet.balance)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) wColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Transaction Note & Save Button
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = onNoteTextChange,
                    label = { Text(if (isEn) "Note (Optional)" else "Catatan Tambahan (Opsional)", fontSize = 14.sp) },
                    placeholder = { Text(if (isEn) "e.g., Lunch with friends" else "Cth: Makan siang") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input_field")
                )

                Button(
                    onClick = {
                        val targetWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
                        val amt = nominalText.toLongOrNull() ?: 0L
                        val formalLabel = normalizeFormalCategoryLabel(selectedCategory.key, selectedCategory.label, lang)
                        if (targetWallet != null && amt > 0L) {
                            onSubmitTransaction(
                                targetWallet,
                                selectedCategory.type,
                                amt,
                                selectedCategory.key,
                                formalLabel,
                                selectedCategory.frequencyTag,
                                noteText.ifBlank { formalLabel },
                                effectiveDateMillis
                            )
                        }
                    },
                    enabled = !isSubmitting && wallets.isNotEmpty() && (nominalText.toLongOrNull() ?: 0L) > 0L,
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_transaction_button")
                ) {
                    Text(
                        text = if (isEn) "Save Transaction" else "Simpan Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
