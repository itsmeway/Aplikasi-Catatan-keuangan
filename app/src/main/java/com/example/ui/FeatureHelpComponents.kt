package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.AppLanguage
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue

data class FeatureHelpContent(
    val title: String,
    val categoryLabel: String,
    val icon: ImageVector = Icons.Default.Lightbulb,
    val layoutLocation: String,
    val summary: String,
    val howToUseStepByStep: List<String> = emptyList(),
    val keyPoints: List<Pair<String, String>>,
    val proTip: String? = null,
    val accentColor: Color = SkyBlue,
    val titleEn: String = title,
    val categoryLabelEn: String = categoryLabel,
    val layoutLocationEn: String = layoutLocation,
    val summaryEn: String = summary,
    val howToUseStepByStepEn: List<String> = howToUseStepByStep,
    val keyPointsEn: List<Pair<String, String>> = keyPoints,
    val proTipEn: String? = proTip
)

object FeatureGuideVisibilityController {
    var showInlineGuideButtons by mutableStateOf(false)
}

@Composable
fun FeatureHelpButton(
    content: FeatureHelpContent,
    modifier: Modifier = Modifier,
    iconSize: Int = 14,
    tint: Color = SkyBlue,
    testTag: String = "feature_help_button"
) {
    if (!FeatureGuideVisibilityController.showInlineGuideButtons) return

    var isExpanded by remember { mutableStateOf(false) }
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH

    Box(modifier = modifier) {
        Surface(
            onClick = { isExpanded = !isExpanded },
            shape = CircleShape,
            color = tint.copy(alpha = 0.14f),
            border = BorderStroke(0.8.dp, tint.copy(alpha = 0.45f)),
            modifier = Modifier
                .size((iconSize + 6).dp)
                .testTag(testTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = if (isEn) "Feature Guide ${content.titleEn}" else "Panduan Fitur ${content.title}",
                    tint = tint,
                    modifier = Modifier.size(iconSize.dp)
                )
            }
        }

        if (isExpanded) {
            FloatingFeatureCloudPopup(
                content = content,
                onDismiss = { isExpanded = false }
            )
        }
    }
}

@Composable
fun FloatingFeatureCloudPopup(
    content: FeatureHelpContent,
    onDismiss: () -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val activeTitle = if (isEn) content.titleEn else content.title
    val activeCat = if (isEn) content.categoryLabelEn else content.categoryLabel
    val activeLocation = if (isEn) content.layoutLocationEn else content.layoutLocation
    val activeSummary = if (isEn) content.summaryEn else content.summary
    val activeSteps = if (isEn) content.howToUseStepByStepEn else content.howToUseStepByStep
    val activeKeyPoints = if (isEn) content.keyPointsEn else content.keyPoints
    val activeProTip = if (isEn) content.proTipEn else content.proTip

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF041C38).copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .widthIn(max = 390.dp)
                    .glassmorphicCardEffect(
                        cornerRadius = 24.dp,
                        accentTint = content.accentColor
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("floating_cloud_popup_${content.title.replace(" ", "_").lowercase()}"),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = glassmorphicBorderStroke(accentColor = content.accentColor, width = 1.3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                shape = RoundedCornerShape(50),
                                color = content.accentColor.copy(alpha = 0.16f),
                                border = BorderStroke(0.8.dp, content.accentColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = content.icon,
                                        contentDescription = null,
                                        tint = content.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = activeCat.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = content.accentColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("close_floating_cloud_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close Guide" else "Tutup Panduan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = activeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(content.accentColor, content.accentColor.copy(alpha = 0.2f))
                                )
                            )
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = content.accentColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, content.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📍", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = if (isEn) "FEATURE LAYOUT LOCATION:" else "POSISI TATA LETAK FITUR INI:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = content.accentColor
                                )
                                Text(
                                    text = activeLocation,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Text(
                        text = activeSummary,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (activeSteps.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "👉", fontSize = 14.sp)
                                Text(
                                    text = if (isEn) "STEP-BY-STEP HOW TO USE:" else "CARA PAKAI LANGKAH DEMI LANGKAH:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = content.accentColor
                                )
                            }
                            activeSteps.forEachIndexed { index, stepDesc ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = content.accentColor.copy(alpha = 0.25f),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = content.accentColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = stepDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (activeKeyPoints.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeKeyPoints.forEach { (label, desc) ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        color = content.accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!activeProTip.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldAccent.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "💡", fontSize = 15.sp)
                                Text(
                                    text = activeProTip,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.5.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                FeatureGuideVisibilityController.showInlineGuideButtons = false
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_hide_all_feature_guides")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "Hide Guides" else "Sembunyikan Panduan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            color = content.accentColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, content.accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_dismiss_floating_cloud")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "Got It ✓" else "Saya Paham ✓",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = content.accentColor,
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

object FeatureHelpDatabase {

    val TOTAL_SALDO = FeatureHelpContent(
        title = "Total Saldo & Multi-Dompet",
        categoryLabel = "Keuangan Inti",
        layoutLocation = "Kotak besar paling atas layar (di bawah logo & menu bar)",
        summary = "Pusat informasi total seluruh uang kas dan tabungan Anda. Menggabungkan saldo rekening bank, dompet digital (e-wallet), dan uang tunai tanpa perlu membuka aplikasi perbankan satu per satu.",
        howToUseStepByStep = listOf(
            "Lihat nominal angka besar untuk mengetahui total uang kas siap pakai saat ini.",
            "Sentuh/klik kotak saldo ini untuk membuka lembar rincian dompet.",
            "Di dalam lembar rincian, Anda bisa menambah dompet baru, mengedit saldo, atau mencatat mutasi antar dompet."
        ),
        keyPoints = listOf(
            "Saldo Utama" to "Menunjukkan uang kas siap pakai yang berada di dompet aktif.",
            "Rincian Multi-Dompet" to "Sentuh kotak saldo untuk melihat rincian isi saldo atau menambah dompet baru.",
            "Simpanan Terpisah" to "Total mencakup simpanan Pos Darurat dan Tabungan Target."
        ),
        proTip = "Klik pada angka saldo kapan saja untuk menambah, mengedit, atau mentransfer uang antar dompet.",
        accentColor = EmeraldPrimary,
        titleEn = "Total Balance & Multi-Accounts",
        categoryLabelEn = "Core Finance",
        layoutLocationEn = "Top hero card (directly below the app header bar)",
        summaryEn = "Your unified balance hub combining all bank accounts, digital wallets, and cash in your selected country's currency.",
        howToUseStepByStepEn = listOf(
            "Check the main figure to see your active spendable balance across all accounts.",
            "Tap any account pill or 'Manage' to add banks/digital wallets, edit balances, or transfer funds between accounts."
        ),
        keyPointsEn = listOf(
            "Direct Account Visibility" to "Every bank, digital wallet, and cash account is visible right inside the top card.",
            "Country & Currency Aware" to "Automatically formats amounts and suggests banks/wallets for your country."
        ),
        proTipEn = "Use 'Transfer' when topping up a digital wallet or withdrawing cash so your expenses stay accurate."
    )

    val POS_DARURAT = FeatureHelpContent(
        title = "Pos Pengeluaran Darurat",
        categoryLabel = "Proteksi Finansial",
        layoutLocation = "Kapsul oval di atap kotak kategori Limit Pengeluaran",
        summary = "Kantong simpanan terpisah khusus untuk kejadian tak terduga agar uang belanja harian Anda tidak terganggu.",
        howToUseStepByStep = listOf(
            "Lihat bar kiri untuk jumlah Dana Darurat dan bar kanan untuk Limit Darurat.",
            "Ketuk kapsul oval ini untuk menyetor simpanan darurat atau mengubah batas limit darurat."
        ),
        keyPoints = listOf(
            "Limit Darurat" to "Batas maksimal uang darurat yang diizinkan untuk ditarik dalam periode tertentu.",
            "Rekening Terpisah" to "Disarankan disimpan di rekening terpisah dari uang belanja harian."
        ),
        proTip = "Usahakan memiliki cadangan darurat minimal 1 sampai 3 bulan biaya hidup.",
        accentColor = GoldAccent,
        titleEn = "Emergency Fund & Limit",
        categoryLabelEn = "Financial Protection",
        layoutLocationEn = "Compact oval pill attached to the roof of the Spending Limits section",
        summaryEn = "Dedicated reserve for unexpected emergencies so your daily budget remains untouched.",
        howToUseStepByStepEn = listOf(
            "Check the left bar for your Emergency Fund balance and the right bar for your Emergency Limit.",
            "Tap the oval roof pill to allocate funds or adjust your emergency spending cap."
        ),
        keyPointsEn = listOf(
            "Emergency Reserve" to "Kept separate from your daily spending balance.",
            "Emergency Limit" to "Caps how much emergency money can be spent in a given period."
        ),
        proTipEn = "Aim to build an emergency reserve covering 1 to 3 months of essential living expenses."
    )

    val GRAFIK_EXPENSE = FeatureHelpContent(
        title = "Analisis Keuangan",
        categoryLabel = "Analisis Keuangan",
        layoutLocation = "Bagian ringkasan pengeluaran",
        summary = "Memantau perbandingan laju uang keluar dan uang masuk.",
        howToUseStepByStep = emptyList(),
        keyPoints = emptyList(),
        accentColor = GoldAccent,
        titleEn = "Financial Analysis",
        categoryLabelEn = "Cashflow Analysis",
        layoutLocationEn = "Spending summary section",
        summaryEn = "Tracks your cash inflow vs outflow balance."
    )

    val TABUNGAN_TARGET = FeatureHelpContent(
        title = "Tabungan Target",
        categoryLabel = "Rencana Masa Depan",
        layoutLocation = "Kotak sebelah kiri di bawah Tabel Riwayat Transaksi",
        summary = "Fitur untuk menyisihkan dana demi mewujudkan target pembelian dengan harga, progres terkumpul, kekurangan, dan riwayat setoran.",
        howToUseStepByStep = listOf(
            "Ketuk kotak Tabungan Target untuk melihat detail barang yang ingin dibeli, harga, terkumpul, kekurangan, tanggal mulai, dan riwayat setoran.",
            "Masukkan nominal setoran dan pilih rekening sumber untuk menabung."
        ),
        keyPoints = listOf(
            "Progres Transparan" to "Menampilkan nominal terkumpul dan kekurangan secara akurat.",
            "Riwayat Setoran" to "Mencatat setiap setoran tabungan lengkap dengan tanggal dan rekening asal."
        ),
        proTip = "Sisihkan tabungan secara rutin setiap menerima pemasukan.",
        accentColor = GoldAccent,
        titleEn = "Savings Target",
        categoryLabelEn = "Future Planning",
        layoutLocationEn = "Left card below the Transaction History table",
        summaryEn = "Track your savings goal with item name, target price, saved amount, remaining shortfall, start date, and deposit history.",
        howToUseStepByStepEn = listOf(
            "Tap the Savings Target card to view or edit your target item, price, start date, and deposit history.",
            "Enter a deposit amount and select a source account to allocate savings."
        ),
        keyPointsEn = listOf(
            "Clear Progress" to "Displays exact saved amount and remaining shortfall.",
            "Deposit History" to "Logs every savings contribution with timestamp and source account."
        ),
        proTipEn = "Save consistently right after receiving your monthly income."
    )

    val INPUT_NOMINAL = FeatureHelpContent(
        title = "Pencatatan Masuk & Keluar",
        categoryLabel = "Pencatatan Cepat",
        layoutLocation = "Tombol uang melayang di bagian bawah layar",
        summary = "Alat input cepat untuk mencatat pengeluaran atau pemasukan dengan format mata uang negara Anda.",
        howToUseStepByStep = listOf(
            "Ketuk tombol uang di bawah layar untuk membuka pop-up input transaksi.",
            "Pilih Pengeluaran atau Pemasukan, pilih tanggal, kategori, dan rekening, lalu ketik nominal."
        ),
        keyPoints = listOf(
            "Format Mata Uang Otomatis" to "Menyesuaikan simbol mata uang dan pemisah ribuan negara Anda."
        ),
        accentColor = CoralExpense,
        titleEn = "Record Income & Expense",
        categoryLabelEn = "Quick Entry",
        layoutLocationEn = "Floating money button at the bottom of the screen",
        summaryEn = "Fast bottom-sheet popup to record expenses or income in your active currency.",
        howToUseStepByStepEn = listOf(
            "Tap the floating money button at the bottom to open the transaction input sheet.",
            "Choose Expense or Income, pick the date, category, and account, then enter the amount."
        ),
        keyPointsEn = listOf(
            "Auto Currency Formatting" to "Automatically formats thousands separators and currency symbols for your country."
        )
    )

    val PINTASAN_SEHARI_HARI = FeatureHelpContent(
        title = "Pintasan Kebutuhan Harian",
        categoryLabel = "Pengeluaran Riil",
        layoutLocation = "Baris chip di dalam pop-up input nominal",
        summary = "Tombol instan untuk mengisi nominal dan catatan pengeluaran harian secara cepat.",
        howToUseStepByStep = emptyList(),
        keyPoints = emptyList(),
        accentColor = SkyBlue,
        titleEn = "Daily Expense Shortcuts",
        categoryLabelEn = "Quick Presets",
        layoutLocationEn = "Shortcut chips inside the transaction input sheet",
        summaryEn = "One-tap presets for common daily expenses in your country's currency."
    )

    val KATEGORI_LIMIT = FeatureHelpContent(
        title = "Limit Pengeluaran & Kalender",
        categoryLabel = "Rem Pengeluaran",
        layoutLocation = "Grup kotak kategori di bawah kotak Total Saldo",
        summary = "Membagi pengeluaran ke dalam pos kategori dengan batas limit dan rentang tanggal kalender (Dari Tanggal - Sampai Tanggal).",
        howToUseStepByStep = listOf(
            "Ketuk ikon pensil pada kotak kategori untuk mengubah nama kategori, nominal limit, dan tanggal mulai/berakhir di kalender.",
            "Ketuk tombol '+ Kategori' di kanan atas untuk menambah pos pengeluaran baru."
        ),
        keyPoints = listOf(
            "Kalender Limit" to "Atur dari tanggal berapa sampai tanggal berapa limit kategori berlaku.",
            "Indikator Warna" to "Hijau (Aman), Kuning (≥75%), Merah (Melewati Limit)."
        ),
        proTip = "Sesuaikan rentang tanggal limit dengan siklus gajian Anda.",
        accentColor = EmeraldPrimary,
        titleEn = "Spending Limits & Calendar Period",
        categoryLabelEn = "Budget Control",
        layoutLocationEn = "Category grid directly below the Total Balance card",
        summaryEn = "Organizes your spending into categories with customizable budget limits and calendar date ranges (Start Date - End Date).",
        howToUseStepByStepEn = listOf(
            "Tap the edit icon on any category card to rename it, change its budget limit, or pick start/end dates on the calendar.",
            "Tap '+ Category' to add your own custom spending category."
        ),
        keyPointsEn = listOf(
            "Calendar Date Range" to "Set exact start and end dates for each category limit.",
            "Visual Status Bar" to "Green (Safe), Amber (≥75%), Red (Over Limit)."
        ),
        proTipEn = "Align your category limit dates with your payday cycle."
    )

    val TAGIHAN_WAJIB = FeatureHelpContent(
        title = "Kebutuhan Wajib (Ceklist Tagihan)",
        categoryLabel = "Kewajiban Bulanan",
        layoutLocation = "Kotak sebelah kanan di bawah Tabel Riwayat Transaksi",
        summary = "Daftar ceklist pembayaran rutin bulanan (listrik, internet, tempat tinggal, cicilan).",
        howToUseStepByStep = listOf(
            "Centang kotak di samping nama tagihan saat sudah dibayar untuk langsung memotong saldo dan mencatat transaksi.",
            "Ketuk '+' untuk menambah tagihan wajib baru atau ketuk nama tagihan untuk mengedit."
        ),
        keyPoints = listOf(
            "1-Klik Lunas" to "Mencentang tagihan otomatis memotong saldo rekening aktif."
        ),
        accentColor = SkyBlue,
        titleEn = "Mandatory Needs (Bills Checklist)",
        categoryLabelEn = "Monthly Obligations",
        layoutLocationEn = "Right card below the Transaction History table",
        summaryEn = "Simple checklist for recurring monthly bills (housing, electricity, internet, subscriptions).",
        howToUseStepByStepEn = listOf(
            "Check the box next to any bill when paid to automatically deduct from your active account and log the transaction.",
            "Tap '+' to add a new recurring bill or tap a bill name to edit its amount and due day."
        ),
        keyPointsEn = listOf(
            "One-Tap Paid" to "Checking off a bill automatically updates your account balance and transaction history."
        )
    )

    val MONITOR_UANG_MASUK = FeatureHelpContent(
        title = "Rincian Pemasukan",
        categoryLabel = "Evaluasi Pemasukan",
        layoutLocation = "Dialog rincian uang masuk",
        summary = "Melacak seluruh pemasukan ke rekening Anda.",
        howToUseStepByStep = emptyList(),
        keyPoints = emptyList(),
        accentColor = EmeraldPrimary,
        titleEn = "Income Breakdown",
        categoryLabelEn = "Income Evaluation",
        layoutLocationEn = "Income detail dialog",
        summaryEn = "Tracks all incoming funds across your accounts."
    )
}
