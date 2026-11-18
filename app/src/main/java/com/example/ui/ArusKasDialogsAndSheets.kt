package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.auth.GoogleSignInButton
import com.example.data.AppLanguage
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.PresetCategories
import com.example.data.ProFeatureTrigger
import com.example.data.SubscriptionConfig
import com.example.data.Wallet
import com.example.data.WorldCountryCurrencyRegistry
import com.example.ui.theme.CoralExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

fun parseColorSafely(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        EmeraldPrimary
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1411),
                        Color(0xFF0D261D),
                        Color(0xFF0A1411)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(2.dp, GoldAccent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_guru_avatar_1794085706193),
                    contentDescription = "ArusKas Mentor",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = GoldAccent.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isEn) "GLOBAL PERSONAL FINANCE COCKPIT" else "FINANCIAL COCKPIT PRIBADI",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ArusKas",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "Track every bank account, digital wallet, and cash balance in your country's currency. Manage spending limits, daily transaction sheets, and savings targets effortlessly."
                    } else {
                        "Pantau penuh setiap rekening bank, e-wallet, dan uang tunai. Catat pengeluaran secepat kilat, kelola limit kategori, dan capai tabungan targetmu."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureRowItem(
                        title = if (isEn) "Smart Category Spending Limits" else "Limit Pengeluaran & Kalender",
                        subtitle = if (isEn) "Set custom date ranges and budget limits per category" else "Satu layar langsung potong saldo dari dompet pilihan"
                    )
                    FeatureRowItem(
                        title = if (isEn) "Multi-Country Banks & Wallets" else "Semua Bank & Dompet Terpantau",
                        subtitle = if (isEn) "Supports major global currencies, banks, and digital wallets" else "Mendukung bank, e-wallet, dan mata uang berbagai negara"
                    )
                    FeatureRowItem(
                        title = if (isEn) "Savings Target & Mandatory Needs" else "Tabungan Target & Kebutuhan Wajib",
                        subtitle = if (isEn) "Track savings goals and monthly recurring bills side-by-side" else "Pantau tabungan target dan ceklist tagihan wajib bulanan"
                    )
                }
            }

            errorMessage?.let { msg ->
                Surface(
                    color = CoralExpense.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CoralExpense)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFECDD3),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            GoogleSignInButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onAuthSuccess = onAuthSuccess,
                onAuthError = { errorMessage = it }
            )
        }
    }
}

@Composable
private fun FeatureRowItem(title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = EmeraldPrimary.copy(alpha = 0.2f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthModalBottomSheet(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(2.dp, GoldAccent)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_guru_avatar_1794085706193),
                    contentDescription = "ArusKas Mentor",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEn) "Back Up & Sync ArusKas" else "Cadangkan & Sinkronkan ArusKas",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "Sign in with Google to securely back up all your bank accounts, digital wallets, and savings progress in the Cloud."
                    } else {
                        "Masuk dengan akun Google agar semua catatan saldo bank, e-wallet, dan progres tabungan targetmu tersimpan aman di Cloud."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            errorMessage?.let { msg ->
                Surface(
                    color = CoralExpense.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CoralExpense)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFECDD3),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            GoogleSignInButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onAuthSuccess = onAuthSuccess,
                onAuthError = { errorMessage = it }
            )

            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Continue Using Dashboard Offline" else "Lanjut Pakai Dashboard Tanpa Masuk")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WalletsBreakdownBottomSheet(
    wallets: List<Wallet>,
    totalTabunganSaved: Long = 0L,
    emergencyBalance: Long = 0L,
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveWallet: (walletId: String?, name: String, type: String, balance: Long, colorHex: String) -> Unit,
    onDeleteWallet: (Wallet) -> Unit,
    onTransferWallet: (fromWallet: Wallet, toWallet: Wallet, amount: Long, adminFee: Long, note: String) -> Unit = { _, _, _, _, _ -> }
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingWallet by remember { mutableStateOf<Wallet?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var showTransferForm by remember { mutableStateOf(false) }

    val activeBalance = wallets.sumOf { it.balance }
    val grandTotalInPocket = activeBalance + totalTabunganSaved + emergencyBalance

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) {
                            "${countryProfile.flagEmoji} Accounts & Wallets (${countryProfile.currencyCode})"
                        } else {
                            "${countryProfile.flagEmoji} Semua Duit di Kantongmu (${countryProfile.currencyCode})"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (isEn) {
                            "Combined Net Worth: ${ArusKasViewModel.formatMoney(grandTotalInPocket)}"
                        } else {
                            "Total Gabungan Utuh: ${ArusKasViewModel.formatMoney(grandTotalInPocket)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (wallets.size >= 2) {
                        OutlinedButton(
                            onClick = {
                                showTransferForm = !showTransferForm
                                showAddForm = false
                                editingWallet = null
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("transfer_wallet_sheet_button")
                        ) {
                            Text(
                                text = if (showTransferForm) {
                                    if (isEn) "Close" else "Tutup"
                                } else {
                                    if (isEn) "⇄ Transfer" else "⇄ Pindah Saldo"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (!showAddForm && !ArusKasFeatureGateEvaluator.canAddNewWallet(wallets.size, subscription)) {
                                onOpenPaywallWithTrigger(ProFeatureTrigger.UNLIMITED_WALLETS)
                            } else {
                                editingWallet = null
                                showTransferForm = false
                                showAddForm = !showAddForm
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_wallet_sheet_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddForm) {
                                if (isEn) "Close" else "Tutup"
                            } else if (!ArusKasFeatureGateEvaluator.canAddNewWallet(wallets.size, subscription)) {
                                if (isEn) "Account • PRO" else "Dompet • PRO"
                            } else {
                                if (isEn) "Account" else "Dompet"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Summary of 3 Separated Pockets
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) {
                                "1. Active Balance (${wallets.size} Accounts)"
                            } else {
                                "1. Saldo Siap Pakai (${wallets.size} Dompet Harian)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ArusKasViewModel.formatMoney(activeBalance),
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "2. Savings Target (Kept Separate)" else "2. Tabungan Target (Simpan Terpisah)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ArusKasViewModel.formatMoney(totalTabunganSaved),
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "3. Emergency Fund (Kept Separate)" else "3. Simpanan Darurat (Simpan Terpisah)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ArusKasViewModel.formatMoney(emergencyBalance),
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralExpense,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isEn) {
                            "💡 Topping up a digital wallet or withdrawing cash? Use '⇄ Transfer' so your monthly expenses aren't artificially inflated."
                        } else {
                            "💡 Top-up E-Wallet atau Tarik Tunai gunakan tombol '⇄ Pindah Saldo' agar pengeluaran bulananmu tidak bengkak palsu."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = showTransferForm && wallets.size >= 2) {
                WalletTransferForm(
                    wallets = wallets,
                    onCancel = { showTransferForm = false },
                    onConfirmTransfer = { fromW, toW, amt, fee, note ->
                        onTransferWallet(fromW, toW, amt, fee, note)
                        showTransferForm = false
                    }
                )
            }

            AnimatedVisibility(visible = showAddForm || editingWallet != null) {
                WalletEditorForm(
                    initialWallet = editingWallet,
                    existingWallets = wallets,
                    onCancel = {
                        showAddForm = false
                        editingWallet = null
                    },
                    onSave = { id, name, type, balance, colorHex ->
                        onSaveWallet(id, name, type, balance, colorHex)
                        showAddForm = false
                        editingWallet = null
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(320.dp)
            ) {
                items(wallets, key = { it.id }) { wallet ->
                    val brandColor = parseColorSafely(wallet.colorHex)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = brandColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (wallet.walletType == "BANK") Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                            contentDescription = wallet.name,
                                            tint = brandColor
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (wallet.walletType) {
                                            "BANK" -> if (isEn) "Bank Account" else "Rekening Bank"
                                            "CASH" -> if (isEn) "Cash on Hand" else "Uang Tunai (Cash)"
                                            else -> if (isEn) "Digital Wallet / E-Wallet" else "E-Wallet / Dompet Digital"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ArusKasViewModel.formatMoney(wallet.balance),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (wallet.balance < 0) CoralExpense else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        editingWallet = wallet
                                        showAddForm = false
                                        showTransferForm = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = if (isEn) "Edit balance ${wallet.name}" else "Ubah saldo ${wallet.name}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (wallets.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteWallet(wallet) },
                                        modifier = Modifier.testTag("delete_wallet_button_${wallet.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = if (isEn) "Delete ${wallet.name}" else "Hapus ${wallet.name}",
                                            tint = CoralExpense.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WalletTransferForm(
    wallets: List<Wallet>,
    onCancel: () -> Unit,
    onConfirmTransfer: (fromWallet: Wallet, toWallet: Wallet, amount: Long, adminFee: Long, note: String) -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    var fromWalletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var toWalletId by remember(wallets) {
        mutableStateOf(wallets.getOrNull(1)?.id ?: wallets.firstOrNull()?.id ?: "")
    }
    var amountStr by remember { mutableStateOf("") }
    var adminFeeStr by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEn) {
                    "⇄ Transfer Balance / Top Up Wallet / Cash Withdrawal"
                } else {
                    "⇄ Pindah Saldo / Top-Up E-Wallet / Tarik Tunai"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
            Text(
                text = if (isEn) "From Account (Source):" else "Pilih Dompet Asal (Sumber Dana):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                wallets.forEach { w ->
                    FilterChip(
                        selected = fromWalletId == w.id,
                        onClick = {
                            fromWalletId = w.id
                            if (toWalletId == w.id) {
                                toWalletId = wallets.firstOrNull { it.id != w.id }?.id ?: w.id
                            }
                        },
                        label = { Text("${w.name} (${ArusKasViewModel.formatCompactMoney(w.balance)})", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Text(
                text = if (isEn) "To Account (Destination):" else "Pilih Dompet Tujuan (Penerima):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                wallets.filter { it.id != fromWalletId }.forEach { w ->
                    FilterChip(
                        selected = toWalletId == w.id,
                        onClick = { toWalletId = w.id },
                        label = { Text(w.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                    label = { Text(if (isEn) "Transfer Amount (${countryProfile.currencyCode})" else "Nominal Pindah (${countryProfile.currencyCode})") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("transfer_amount_input")
                )
                OutlinedTextField(
                    value = adminFeeStr,
                    onValueChange = { adminFeeStr = ArusKasViewModel.sanitizeMoneyInput(it) },
                    label = { Text(if (isEn) "Transfer Fee" else "Biaya Admin") },
                    prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    visualTransformation = RupiahThousandsVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transfer_admin_fee_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val fromW = wallets.find { it.id == fromWalletId }
                        val toW = wallets.find { it.id == toWalletId }
                        val amt = amountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                        val fee = adminFeeStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                        if (fromW != null && toW != null && fromW.id != toW.id && amt > 0L) {
                            onConfirmTransfer(fromW, toW, amt, fee, note)
                        }
                    },
                    modifier = Modifier.testTag("confirm_transfer_wallet_button")
                ) {
                    Text(if (isEn) "Transfer Balance" else "Pindahkan Saldo")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WalletEditorForm(
    initialWallet: Wallet?,
    existingWallets: List<Wallet> = emptyList(),
    onCancel: () -> Unit,
    onSave: (id: String?, name: String, type: String, balance: Long, colorHex: String) -> Unit
) {
    val isEn = WorldCountryCurrencyRegistry.activeLanguage == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    var name by remember(initialWallet) { mutableStateOf(initialWallet?.name ?: "") }
    var balanceStr by remember(initialWallet) { mutableStateOf(initialWallet?.balance?.toString() ?: "") }
    var walletType by remember(initialWallet) { mutableStateOf(initialWallet?.walletType ?: "EWALLET") }
    var colorHex by remember(initialWallet) { mutableStateOf(initialWallet?.colorHex ?: "#10B981") }

    val colorChoices = listOf("#0066AE", "#F59E0B", "#118EEA", "#EE4D2D", "#00AED6", "#10B981")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (initialWallet == null) {
                    if (isEn) {
                        "Add New Bank / Digital Wallet (${countryProfile.flagEmoji} ${countryProfile.nameEn})"
                    } else {
                        "Tambah Dompet / Bank / E-Wallet Baru (${countryProfile.flagEmoji} ${countryProfile.nameId})"
                    }
                } else {
                    if (isEn) "Update Balance: ${initialWallet.name}" else "Sesuaikan Saldo ${initialWallet.name}"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (initialWallet == null) {
                val quickSuggestions = countryProfile.bankAndWalletPresets.filter { preset ->
                    existingWallets.none { it.name.equals(preset.name, ignoreCase = true) }
                }.take(8)
                if (quickSuggestions.isNotEmpty()) {
                    Text(
                        text = if (isEn) {
                            "Quick Select ${countryProfile.nameEn} Banks & Digital Wallets:"
                        } else {
                            "Pilih Cepat Bank / E-Wallet (${countryProfile.nameId}):"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickSuggestions.forEach { preset ->
                            FilterChip(
                                selected = name.equals(preset.name, ignoreCase = true),
                                onClick = {
                                    name = preset.name
                                    walletType = preset.walletType
                                    colorHex = preset.colorHex
                                },
                                label = { Text("+ ${preset.name}", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            val exampleNames = remember(countryProfile) {
                countryProfile.bankAndWalletPresets.take(4).joinToString(", ") { it.name }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        if (isEn) "Name (e.g., $exampleNames)" else "Nama (mis: $exampleNames)"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = balanceStr,
                onValueChange = { input -> balanceStr = ArusKasViewModel.sanitizeMoneyInput(input, allowNegative = true) },
                label = { Text(if (isEn) "Current Balance (${countryProfile.currencyCode})" else "Saldo Saat Ini (${countryProfile.currencyCode})") },
                prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                visualTransformation = RupiahThousandsVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = walletType == "BANK",
                    onClick = { walletType = "BANK" },
                    label = { Text("Bank") }
                )
                FilterChip(
                    selected = walletType == "EWALLET",
                    onClick = { walletType = "EWALLET" },
                    label = { Text(if (isEn) "Digital Wallet" else "E-Wallet") }
                )
                FilterChip(
                    selected = walletType == "CASH",
                    onClick = { walletType = "CASH" },
                    label = { Text(if (isEn) "Cash" else "Tunai") }
                )
                Spacer(modifier = Modifier.weight(1f))
                colorChoices.forEach { hex ->
                    val c = parseColorSafely(hex)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { colorHex = hex }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val cleanBalance = balanceStr.toLongOrNull() ?: 0L
                        if (name.isNotBlank()) {
                            onSave(initialWallet?.id, name.trim(), walletType, cleanBalance, colorHex)
                        }
                    }
                ) {
                    Text(if (isEn) "Save" else "Simpan")
                }
            }
        }
    }
}

@Composable
fun FirstTimeSetupDialog(
    onDismiss: () -> Unit,
    onConfirmSetup: (
        walletBalances: Map<String, Long>,
        goalTitle: String,
        goalTarget: Long,
        goalDaily: Long,
        goalNote: String
    ) -> Unit
) {
    val lang = WorldCountryCurrencyRegistry.activeLanguage
    val isEn = lang == AppLanguage.ENGLISH
    val countryProfile = WorldCountryCurrencyRegistry.getActiveProfile()
    val starterWallets = remember(countryProfile.countryCode) {
        WorldCountryCurrencyRegistry.defaultStarterWallets(countryProfile.countryCode)
    }
    val suggestedTargets = remember(countryProfile.countryCode, lang) {
        WorldCountryCurrencyRegistry.suggestedSavingsTargetsForCountry(countryProfile.countryCode, lang)
    }
    val firstSuggested = suggestedTargets.firstOrNull() ?: ((if (isEn) "MacBook Air M3" else "Beli Laptop Impian") to 1200L)

    val balances = remember(starterWallets) {
        mutableStateMapOf<String, String>().apply {
            starterWallets.forEach { w -> put(w.id, w.balance.toString()) }
        }
    }
    var goalTitle by remember(firstSuggested) { mutableStateOf(firstSuggested.first) }
    var goalTarget by remember(firstSuggested) { mutableStateOf(firstSuggested.second.toString()) }
    var goalDaily by remember(firstSuggested) {
        mutableStateOf((firstSuggested.second / 180L).coerceAtLeast(1L).toString())
    }
    var goalNote by remember(isEn) {
        mutableStateOf(
            if (isEn) "Saved from monthly income & side projects"
            else "Sisihkan dari gaji & penghasilan tambahan"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isEn) "Set Initial Balances & Savings Goal" else "Atur Saldo Awal & Impianmu",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = if (isEn) {
                        "Customize your ${countryProfile.nameEn} accounts (${countryProfile.currencyCode}) so everything is tracked accurately."
                    } else {
                        "Sesuaikan saldo di rekening & dompet (${countryProfile.currencyCode}) agar langsung terpantau akurat."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                starterWallets.forEach { preset ->
                    OutlinedTextField(
                        value = balances[preset.id] ?: "0",
                        onValueChange = { v -> balances[preset.id] = ArusKasViewModel.sanitizeMoneyInput(v) },
                        label = {
                            Text(
                                if (isEn) "${preset.name} Balance (${countryProfile.currencyCode})"
                                else "Saldo ${preset.name} (${countryProfile.currencyCode})"
                            )
                        },
                        prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                        visualTransformation = RupiahThousandsVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Text(
                    text = if (isEn) "Primary Savings Target" else "Target Tabungan Utama",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text(if (isEn) "Goal Name (e.g., MacBook Air)" else "Nama Impian (mis: Beli Laptop)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = ArusKasViewModel.sanitizeMoneyInput(it) },
                        label = { Text(if (isEn) "Target (${countryProfile.currencyCode})" else "Target (${countryProfile.currencyCode})") },
                        prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                        visualTransformation = RupiahThousandsVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = goalDaily,
                        onValueChange = { goalDaily = ArusKasViewModel.sanitizeMoneyInput(it) },
                        label = { Text(if (isEn) "Save/Day" else "Nabung/Hari") },
                        prefix = { Text("${countryProfile.currencySymbol} ", fontWeight = FontWeight.Bold) },
                        visualTransformation = RupiahThousandsVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = goalNote,
                    onValueChange = { goalNote = it },
                    label = { Text(if (isEn) "Savings Plan Note" else "Catatan Rencana Nabung") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedMap = balances.mapValues { it.value.toLongOrNull() ?: 0L }
                    onConfirmSetup(
                        parsedMap,
                        goalTitle.ifBlank { firstSuggested.first },
                        goalTarget.toLongOrNull() ?: firstSuggested.second,
                        goalDaily.toLongOrNull() ?: (firstSuggested.second / 180L).coerceAtLeast(1L),
                        goalNote
                    )
                },
                modifier = Modifier.testTag("confirm_starter_setup_button")
            ) {
                Text(if (isEn) "Start Tracking" else "Mulai Pantau Uang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Later" else "Nanti Saja")
            }
        }
    )
}
