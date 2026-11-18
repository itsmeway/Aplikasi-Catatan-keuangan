package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AdminSupportHelper
import com.example.data.AppLanguage
import com.example.data.ArusDompetBackupManager
import com.example.data.ArusDompetStorageInspector
import com.example.data.ArusKasFeatureGateEvaluator
import com.example.data.BackupValidationResult
import com.example.data.ParsedArusDompetBackup
import com.example.data.PinSecurityManager
import com.example.data.ProFeatureTrigger
import com.example.data.SubscriptionConfig
import com.example.ui.theme.BcaCobaltBlue
import com.example.ui.theme.BcaRoyalBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-Screen App PIN Lock Gate Overlay (Salted SHA-256 Verification)
 */
@Composable
fun AppPinLockGateOverlay(
    language: AppLanguage,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN
    var pinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_pin_lock_gate_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = BcaRoyalBlue.copy(alpha = 0.14f),
                border = BorderStroke(1.5.dp, BcaRoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BcaRoyalBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isId) "Arus Dompet Terkunci" else "Arus Dompet Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isId) {
                    "Masukkan PIN keamanan 4–6 digit Anda untuk membuka data keuangan."
                } else {
                    "Enter your 4–6 digit security PIN to unlock your financial records."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTextField(
                value = pinInput,
                onValueChange = { value ->
                    val digitsOnly = value.filter { it.isDigit() }.take(6)
                    pinInput = digitsOnly
                    errorMsg = null
                    if (digitsOnly.length >= 4 && PinSecurityManager.verifyPin(context, digitsOnly)) {
                        onUnlocked()
                    }
                },
                label = {
                    Text(if (isId) "PIN Keamanan (4–6 Digit)" else "Security PIN (4–6 Digits)")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_lock_gate_input")
            )

            errorMsg?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (PinSecurityManager.verifyPin(context, pinInput)) {
                        onUnlocked()
                    } else {
                        errorMsg = if (isId) "PIN tidak sesuai. Silakan coba lagi." else "Incorrect PIN. Please try again."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pin_lock_gate_unlock_button")
            ) {
                Text(
                    text = if (isId) "Buka Kunci Aplikasi" else "Unlock Application",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isId) "🔒 Dienkripsi dengan SHA-256 + Cryptographic Salt" else "🔒 Protected with SHA-256 + Cryptographic Salt",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Dialog for Setting / Changing / Removing App Security PIN (Salted SHA-256)
 */
@Composable
fun PinConfigurationDialog(
    language: AppLanguage,
    isCurrentlyEnabled: Boolean,
    onPinSavedAndEnabled: () -> Unit,
    onPinDisabled: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN
    val hasExistingPin = remember { PinSecurityManager.hasPinConfigured(context) }

    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BcaRoyalBlue)
                Text(
                    text = if (isId) "Kunci PIN Keamanan (SHA-256)" else "Security PIN Lock (SHA-256)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isId) {
                            "PIN Anda TIDAK PERNAH disimpan dalam teks biasa (plain text). Sistem mengenkripsi PIN satu arah menggunakan algoritma SHA-256 + 16-byte Cryptographic Salt."
                        } else {
                            "Your PIN is NEVER stored in plain text. It is one-way hashed using SHA-256 with a 16-byte cryptographic random salt."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (hasExistingPin) {
                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = {
                            currentPinInput = it.filter { ch -> ch.isDigit() }.take(6)
                            errorMessage = null
                        },
                        label = { Text(if (isId) "PIN Lama Saat Ini" else "Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_config_current_input")
                    )
                }

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = {
                        newPinInput = it.filter { ch -> ch.isDigit() }.take(6)
                        errorMessage = null
                    },
                    label = { Text(if (isId) "PIN Baru (4–6 Angka)" else "New PIN (4–6 Digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_config_new_input")
                )

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = {
                        confirmPinInput = it.filter { ch -> ch.isDigit() }.take(6)
                        errorMessage = null
                    },
                    label = { Text(if (isId) "Ulangi PIN Baru" else "Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_config_confirm_input")
                )

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (hasExistingPin && isCurrentlyEnabled) {
                    OutlinedButton(
                        onClick = {
                            if (!PinSecurityManager.verifyPin(context, currentPinInput)) {
                                errorMessage = if (isId) "Masukkan PIN lama yang benar untuk menonaktifkan kunci." else "Enter correct current PIN to disable lock."
                            } else {
                                PinSecurityManager.clearPin(context)
                                onPinDisabled()
                            }
                        },
                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_config_disable_button")
                    ) {
                        Text(
                            text = if (isId) "Matikan & Hapus Kunci PIN" else "Disable & Remove PIN Lock",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (hasExistingPin && !PinSecurityManager.verifyPin(context, currentPinInput)) {
                        errorMessage = if (isId) "PIN lama tidak sesuai!" else "Current PIN is incorrect!"
                        return@Button
                    }
                    if (newPinInput.length < 4 || newPinInput.length > 6) {
                        errorMessage = if (isId) "PIN baru harus terdiri dari 4 hingga 6 angka!" else "New PIN must be 4 to 6 digits!"
                        return@Button
                    }
                    if (newPinInput != confirmPinInput) {
                        errorMessage = if (isId) "Konfirmasi PIN baru tidak cocok!" else "New PIN confirmation does not match!"
                        return@Button
                    }
                    if (PinSecurityManager.setPin(context, newPinInput)) {
                        onPinSavedAndEnabled()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("pin_config_save_button")
            ) {
                Text(
                    text = if (isId) "Simpan & Aktifkan PIN" else "Save & Enable PIN",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isId) "Batal" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Comprehensive Data, Full JSON Backup & Restore, Storage Inspector, Cache Cleaner, and Factory Reset Dialog
 */
@Composable
fun DataStorageAndBackupDialog(
    language: AppLanguage,
    subscription: SubscriptionConfig = SubscriptionConfig(isProActive = true),
    onOpenPaywallWithTrigger: (ProFeatureTrigger) -> Unit = {},
    onGenerateBackupJson: () -> String,
    onRestoreValidatedBackup: (ParsedArusDompetBackup, Boolean) -> Unit,
    onExportPdfReport: () -> Unit,
    onExportCsvReport: () -> Unit,
    onResetAllFinancialData: () -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Backup/Restore, 1 = Storage & Reset
    var storageInfo by remember { mutableStateOf(ArusDompetStorageInspector.calculateStorageBreakdown(context)) }

    var restoreJsonInput by remember { mutableStateOf("") }
    var pendingValidatedBackup by remember { mutableStateOf<ParsedArusDompetBackup?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var mergeModeSelected by remember { mutableStateOf(false) } // false = Replace, true = Merge without duplicates
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    var resetConfirmationText by remember { mutableStateOf("") }

    // SAF CreateDocument launcher for saving .json backup directly to user's chosen folder
    val createBackupDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val json = onGenerateBackupJson()
            val ok = ArusDompetBackupManager.writeBackupToUri(context, uri, json)
            onShowMessage(
                if (ok) {
                    if (isId) "File Backup JSON berhasil disimpan ke penyimpanan perangkat!"
                    else "Full JSON Backup saved to device storage!"
                } else {
                    if (isId) "Gagal menyimpan file backup ke lokasi tersebut."
                    else "Failed to write backup file to selected location."
                }
            )
        }
    }

    // SAF OpenDocument launcher for selecting a .json backup file to restore
    val openBackupDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = ArusDompetBackupManager.readBackupFromUri(context, uri)
            if (content == null) {
                validationError = if (isId) "Gagal membaca file dari penyimpanan." else "Failed to read file from storage."
                pendingValidatedBackup = null
            } else {
                restoreJsonInput = content
                when (val res = ArusDompetBackupManager.validateAndParseBackupJson(content)) {
                    is BackupValidationResult.Valid -> {
                        pendingValidatedBackup = res.backup
                        validationError = null
                    }
                    is BackupValidationResult.Invalid -> {
                        pendingValidatedBackup = null
                        validationError = if (isId) res.reasonId else res.reasonEn
                    }
                }
            }
        }
    }

    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Text(
                        text = if (isId) "Konfirmasi Reset Seluruh Data" else "Confirm Full Data Reset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isId) {
                            "PERINGATAN: Tindakan ini akan menghapus seluruh riwayat transaksi dan mengembalikan semua saldo dompet, tabungan target, serta dana darurat menjadi 0. Disarankan melakukan Backup JSON terlebih dahulu.\n\nKetik RESET di bawah ini untuk melanjutkan:"
                        } else {
                            "WARNING: This action will erase all transaction history and reset all wallet balances, savings goals, and emergency funds to 0. We strongly recommend creating a JSON Backup first.\n\nType RESET below to confirm:"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = resetConfirmationText,
                        onValueChange = { resetConfirmationText = it.trim().uppercase(Locale.US) },
                        label = { Text(if (isId) "Ketik RESET" else "Type RESET") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_data_confirm_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetConfirmationText == "RESET") {
                            onResetAllFinancialData()
                            showResetConfirmationDialog = false
                            storageInfo = ArusDompetStorageInspector.calculateStorageBreakdown(context)
                            onDismiss()
                        }
                    },
                    enabled = resetConfirmationText == "RESET",
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_reset_all_data_button")
                ) {
                    Text(if (isId) "Hapus & Reset ke 0" else "Wipe & Reset to 0", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmationDialog = false }) {
                    Text(if (isId) "Batal" else "Cancel")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("data_storage_and_backup_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            shape = CircleShape,
                            color = BcaRoyalBlue.copy(alpha = 0.14f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = BcaRoyalBlue)
                            }
                        }
                        Column {
                            Text(
                                text = if (isId) "Data, Backup & Penyimpanan" else "Data, Backup & Storage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isId) "Cadangkan, pulihkan (Restore), ekspor laporan & kelola ruang" else "Backup, restore with integrity check, export & manage storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Tab Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to (if (isId) "📦 Backup, Restore & Ekspor" else "📦 Backup, Restore & Export"),
                        1 to (if (isId) "💾 Penyimpanan & Reset" else "💾 Storage & Reset")
                    ).forEach { (tabIdx, tabTitle) ->
                        val isSel = selectedTab == tabIdx
                        Surface(
                            onClick = {
                                selectedTab = tabIdx
                                storageInfo = ArusDompetStorageInspector.calculateStorageBreakdown(context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("storage_dialog_tab_$tabIdx")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp)
                            ) {
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // 1. FULL JSON BACKUP CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldPrimary)
                                Text(
                                    text = if (isId) "1. Cadangkan Seluruh Data (Backup JSON)" else "1. Full App Data Backup (.json)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isId) {
                                    "Mencadangkan seluruh saldo dompet, riwayat transaksi, kategori & limit, tabungan target, tagihan wajib, profil, serta preferensi asisten offline dilengkapi Checksum Integritas SHA-256."
                                } else {
                                    "Backs up all wallets, transactions, category limits, savings goals, mandatory bills, profile, and offline assistant preferences with a SHA-256 integrity checksum."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                        createBackupDocLauncher.launch("ArusDompet_Backup_$stamp.json")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_backup_json_file_button")
                                ) {
                                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isId) "Simpan File .JSON" else "Save .JSON File",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val json = onGenerateBackupJson()
                                        val file = ArusDompetBackupManager.exportBackupToCacheFile(context, json)
                                        ArusDompetBackupManager.shareBackupFile(context, file, language)
                                    },
                                    border = BorderStroke(1.dp, EmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_backup_json_file_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isId) "Bagikan / Drive" else "Share / Drive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val json = onGenerateBackupJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("ArusDompet_Backup", json))
                                    onShowMessage(
                                        if (isId) "Kode Backup JSON berhasil disalin ke papan klip!"
                                        else "Backup JSON copied to clipboard!"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("copy_backup_json_clipboard_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isId) "Salin Teks Backup JSON ke Papan Klip" else "Copy Backup JSON to Clipboard",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // 2. RESTORE & INTEGRITY VALIDATION CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BcaRoyalBlue.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = BcaRoyalBlue)
                                Text(
                                    text = if (isId) "2. Pulihkan Data (Restore & Validasi SHA-256)" else "2. Restore Data (SHA-256 Integrity Verified)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isId) {
                                    "Pilih file backup (.json) dari HP/Google Drive atau tempel kode backup. Sistem memverifikasi keutuhan file terlebih dahulu agar data yang ada tidak rusak."
                                } else {
                                    "Select a backup (.json) file from storage/Drive or paste backup JSON. The system verifies checksum integrity before restoring so existing data is never corrupted."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    openBackupDocLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("select_restore_json_file_button")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isId) "Pilih File Backup (.JSON) dari Perangkat" else "Select Backup File (.JSON) from Device",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = restoreJsonInput,
                                onValueChange = { raw ->
                                    restoreJsonInput = raw
                                    if (raw.isBlank()) {
                                        pendingValidatedBackup = null
                                        validationError = null
                                    } else {
                                        when (val res = ArusDompetBackupManager.validateAndParseBackupJson(raw)) {
                                            is BackupValidationResult.Valid -> {
                                                pendingValidatedBackup = res.backup
                                                validationError = null
                                            }
                                            is BackupValidationResult.Invalid -> {
                                                pendingValidatedBackup = null
                                                validationError = if (isId) res.reasonId else res.reasonEn
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        if (isId) "Atau tempel (Paste) teks JSON Backup di sini"
                                        else "Or paste Backup JSON text here"
                                    )
                                },
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("restore_json_text_input")
                            )

                            validationError?.let { err ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "❌ $err",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            val validBackup = pendingValidatedBackup
                            if (validBackup != null) {
                                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
                                    .format(Date(validBackup.createdAtMillis))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.14f),
                                    border = BorderStroke(1.2.dp, EmeraldPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = if (isId) "File Backup Valid & Terverifikasi SHA-256" else "Valid & SHA-256 Verified Backup",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldPrimary
                                            )
                                        }
                                        Text(
                                            text = if (isId) {
                                                "• Tanggal Backup: $dateStr\n" +
                                                    "• Isi Data: ${validBackup.wallets.size} Dompet, ${validBackup.transactions.size} Transaksi, ${validBackup.goals.size} Target Tabungan, ${validBackup.mandatoryBills.size} Tagihan"
                                            } else {
                                                "• Backup Date: $dateStr\n" +
                                                    "• Contents: ${validBackup.wallets.size} Wallets, ${validBackup.transactions.size} Transactions, ${validBackup.goals.size} Savings Goals, ${validBackup.mandatoryBills.size} Bills"
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )

                                        // Mode Selection: Replace (Free Forever) vs Merge without duplicates (PRO)
                                        val canMerge = ArusKasFeatureGateEvaluator.canUseSmartMergeRestore(subscription)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { mergeModeSelected = false }
                                        ) {
                                            RadioButton(selected = !mergeModeSelected, onClick = { mergeModeSelected = false })
                                            Text(
                                                text = if (isId) "Ganti Semua Data (Sesuai Isi Backup • Gratis)" else "Replace All Data (Exact Backup State • Free)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (canMerge) {
                                                        mergeModeSelected = true
                                                    } else {
                                                        onOpenPaywallWithTrigger(ProFeatureTrigger.SMART_MERGE_RESTORE)
                                                    }
                                                }
                                        ) {
                                            RadioButton(
                                                selected = mergeModeSelected,
                                                onClick = {
                                                    if (canMerge) {
                                                        mergeModeSelected = true
                                                    } else {
                                                        onOpenPaywallWithTrigger(ProFeatureTrigger.SMART_MERGE_RESTORE)
                                                    }
                                                }
                                            )
                                            val proMergeTag = if (canMerge) "" else " • 🔒 PRO"
                                            Text(
                                                text = (if (isId) "Gabungkan Tanpa Duplikat Transaksi/Saldo" else "Merge Without Duplicate Transactions/Balances") + proMergeTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onRestoreValidatedBackup(validBackup, mergeModeSelected)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("confirm_restore_backup_button")
                                        ) {
                                            Text(
                                                text = if (isId) "Konfirmasi & Pulihkan Data Sekarang" else "Confirm & Restore Data Now",
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. EXPORT READABLE REPORTS (PDF [PRO] & EXCEL/CSV [FREE FOREVER])
                    val canExportPdf = ArusKasFeatureGateEvaluator.canExportStyledPdf(subscription)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isId) "3. Ekspor Laporan Siap Baca (PDF & Excel/CSV)" else "3. Export Readable Reports (PDF & Excel/CSV)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (canExportPdf) {
                                            onExportPdfReport()
                                        } else {
                                            onOpenPaywallWithTrigger(ProFeatureTrigger.PDF_EXPORT)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dialog_export_pdf_button")
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val pdfLabel = if (canExportPdf) {
                                        if (isId) "Ekspor PDF" else "Export PDF"
                                    } else {
                                        if (isId) "Ekspor PDF • PRO" else "Export PDF • PRO"
                                    }
                                    Text(pdfLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onExportCsvReport,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dialog_export_csv_button")
                                ) {
                                    Icon(Icons.Default.TableView, contentDescription = null, tint = BcaRoyalBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isId) "Ekspor Excel (.CSV)" else "Export Excel (.CSV)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: STORAGE BREAKDOWN, SAFE CACHE CLEANER & DATA RESET
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isId) "Rincian Penggunaan Penyimpanan Lokal" else "Local Storage Usage Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )

                            StorageRowItem(
                                label = if (isId) "Data Transaksi, Dompet & Profil" else "Transactions, Wallets & Profile Data",
                                value = storageInfo.formatSize(storageInfo.financialDataBytes)
                            )
                            StorageRowItem(
                                label = if (isId) "Database Asisten Offline (10.000 Kalimat)" else "Offline Assistant DB (10,000 Phrases)",
                                value = storageInfo.formatSize(storageInfo.offlineEngineDbBytes)
                            )
                            StorageRowItem(
                                label = if (isId) "File Cache & Ekspor Sementara" else "Temporary Cache & Export Files",
                                value = storageInfo.formatSize(storageInfo.temporaryCacheBytes)
                            )
                            HorizontalDivider()
                            StorageRowItem(
                                label = if (isId) "Total Ruang Penyimpanan Aplikasi" else "Total App Data Footprint",
                                value = storageInfo.formatSize(storageInfo.totalBytes),
                                isHighlight = true
                            )
                        }
                    }

                    // Safe Cache Cleaner
                    Button(
                        onClick = {
                            val freed = ArusDompetStorageInspector.clearTemporaryCacheOnly(context)
                            storageInfo = ArusDompetStorageInspector.calculateStorageBreakdown(context)
                            val freedText = storageInfo.formatSize(freed)
                            onShowMessage(
                                if (isId) "Cache sementara berhasil dibersihkan ($freedText dibebaskan). Data transaksi & saldo 100% aman!"
                                else "Temporary cache cleared ($freedText freed). Your transactions & balances are 100% safe!"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_temp_cache_only_button")
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isId) "Bersihkan Cache Sementara (Tanpa Hapus Data)" else "Clear Temporary Cache Only (Keeps All Data)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset All Financial Data (Danger Zone)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isId) "Zona Berbahaya: Reset Seluruh Data Keuangan" else "Danger Zone: Reset All Financial Records",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = if (isId) {
                                    "Menghapus seluruh catatan transaksi dan mengembalikan semua saldo dompet, tabungan target, serta dana darurat kembali ke 0."
                                } else {
                                    "Deletes all transaction records and resets all wallet balances, savings goals, and emergency funds back to 0."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = {
                                    resetConfirmationText = ""
                                    showResetConfirmationDialog = true
                                },
                                border = BorderStroke(1.2.dp, Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_reset_all_data_confirmation_button")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isId) "Reset / Hapus Seluruh Data Keuangan" else "Reset / Wipe All Financial Data",
                                    color = Color(0xFFD32F2F),
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

@Composable
private fun StorageRowItem(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlight) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isHighlight) EmeraldPrimary else BcaRoyalBlue
        )
    }
}

/**
 * Help, Admin Support (iwa.ye / programerway@gmail.com), Structured Bug Report,
 * Feature Suggestion, and 11-Topic Interactive FAQ Hub Dialog.
 */
@Composable
fun HelpAndFeedbackHubDialog(
    language: AppLanguage,
    initialTab: Int = 0, // 0 = Admin Support, 1 = Bug Report, 2 = Suggestion, 3 = FAQ
    onOpenUserGuide: () -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isId = language == AppLanguage.INDONESIAN
    var activeTab by remember { mutableIntStateOf(initialTab) }

    // Bug Report Form State
    val issueTypes = if (isId) {
        listOf("Tampilan / UI", "Perhitungan Saldo", "Backup / Restore", "Notifikasi", "Asisten Offline", "Lainnya")
    } else {
        listOf("UI / Display", "Balance Calculation", "Backup / Restore", "Notifications", "Offline Assistant", "Other")
    }
    var selectedIssueType by remember { mutableStateOf(issueTypes.first()) }
    var bugTitle by remember { mutableStateOf("") }
    var bugDescription by remember { mutableStateOf("") }
    var bugSteps by remember { mutableStateOf("") }
    var bugScreenshotUri by remember { mutableStateOf<Uri?>(null) }

    val pickScreenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        bugScreenshotUri = uri
    }

    // Feature Suggestion State
    var suggestionCategory by remember { mutableStateOf(if (isId) "Fitur Baru" else "New Feature") }
    var suggestionTitle by remember { mutableStateOf("") }
    var suggestionDetail by remember { mutableStateOf("") }

    // FAQ State
    var faqSearchQuery by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 14.dp)
                .testTag("help_and_feedback_hub_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.16f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = EmeraldPrimary)
                            }
                        }
                        Column {
                            Text(
                                text = if (isId) "Bantuan, Masukan & Kontak Admin" else "Help, Feedback & Admin Support",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Admin: ${AdminSupportHelper.ADMIN_NAME} • ${AdminSupportHelper.ADMIN_EMAIL}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // 4 Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to (if (isId) "Admin" else "Admin"),
                        1 to (if (isId) "Lapor Bug" else "Bug Report"),
                        2 to (if (isId) "Saran" else "Suggest"),
                        3 to "FAQ (11)"
                    ).forEach { (idx, label) ->
                        val isSel = activeTab == idx
                        Surface(
                            onClick = { activeTab = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BcaRoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("help_hub_tab_$idx")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                when (activeTab) {
                    0 -> {
                        // TAB 0: HUBUNGI ADMIN (iwa.ye / programerway@gmail.com) & INFO VERSI
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BcaRoyalBlue.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, BcaRoyalBlue.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isId) "Kontak Resmi Admin & Pengembang" else "Official Admin & Developer Contact",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BcaRoyalBlue
                                )
                                Text(
                                    text = if (isId) {
                                        "• Nama Admin: ${AdminSupportHelper.ADMIN_NAME}\n" +
                                            "• Email Resmi: ${AdminSupportHelper.ADMIN_EMAIL}\n" +
                                            "• Versi Aplikasi: Arus Dompet v${AdminSupportHelper.APP_VERSION_NAME} (Build ${AdminSupportHelper.APP_VERSION_CODE})"
                                    } else {
                                        "• Admin Name: ${AdminSupportHelper.ADMIN_NAME}\n" +
                                            "• Official Email: ${AdminSupportHelper.ADMIN_EMAIL}\n" +
                                            "• App Version: Arus Dompet v${AdminSupportHelper.APP_VERSION_NAME} (Build ${AdminSupportHelper.APP_VERSION_CODE})"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isId) {
                                        "Butuh bantuan langsung atau ingin berkonsultasi mengenai penggunaan aplikasi Arus Dompet? Klik tombol di bawah untuk membuka aplikasi email Anda dengan pesan yang sudah disiapkan."
                                    } else {
                                        "Need direct assistance with Arus Dompet? Tap the button below to open your email app with a pre-filled support message."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        AdminSupportHelper.launchContactAdminEmail(
                                            context = context,
                                            lang = language,
                                            onFallbackMessage = onShowMessage
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("contact_admin_email_button")
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isId) "Hubungi Admin (${AdminSupportHelper.ADMIN_NAME})" else "Contact Admin (${AdminSupportHelper.ADMIN_NAME})",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        AdminSupportHelper.copyAdminEmailToClipboard(context, language, onShowMessage)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("copy_admin_email_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isId) "Salin Alamat Email (${AdminSupportHelper.ADMIN_EMAIL})" else "Copy Email (${AdminSupportHelper.ADMIN_EMAIL})",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Quick access to Interactive App Guide & FAQ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenUserGuide()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isId) "📖 Panduan Pengguna" else "📖 User Guide", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { activeTab = 3 },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isId) "❓ Buka 11 FAQ" else "❓ Open 11 FAQs", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: LAPORKAN MASALAH (STRUCTURED BUG REPORT)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isId) {
                                    "🛡️ Jaminan Privasi: Laporan masalah hanya menyertakan informasi versi aplikasi & versi Android. Data saldo, rekening, dan transaksi Anda TIDAK PERNAH dikirim."
                                } else {
                                    "🛡️ Privacy Guarantee: Bug reports only include app version & Android OS version. Your balances, accounts, and transactions are NEVER attached."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Text(
                            text = if (isId) "Pilih Jenis Masalah:" else "Select Issue Type:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            issueTypes.take(3).forEach { type ->
                                val isSel = selectedIssueType == type
                                Surface(
                                    onClick = { selectedIssueType = type },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) BcaRoyalBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSel) BcaRoyalBlue else Color.Transparent),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            issueTypes.drop(3).forEach { type ->
                                val isSel = selectedIssueType == type
                                Surface(
                                    onClick = { selectedIssueType = type },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) BcaRoyalBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSel) BcaRoyalBlue else Color.Transparent),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = bugTitle,
                            onValueChange = { bugTitle = it },
                            label = { Text(if (isId) "Judul Masalah" else "Issue Title") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bug_report_title_input")
                        )

                        OutlinedTextField(
                            value = bugDescription,
                            onValueChange = { bugDescription = it },
                            label = { Text(if (isId) "Deskripsi Detail Masalah" else "Detailed Issue Description") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bug_report_description_input")
                        )

                        OutlinedTextField(
                            value = bugSteps,
                            onValueChange = { bugSteps = it },
                            label = { Text(if (isId) "Langkah Kejadian (Opsional)" else "Steps to Reproduce (Optional)") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bug_report_steps_input")
                        )

                        // Auto-detected system metadata preview
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isId) {
                                    "Info Sistem Otomatis: Arus Dompet v${AdminSupportHelper.APP_VERSION_NAME} (${AdminSupportHelper.APP_VERSION_CODE}) • Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) • ${Build.MANUFACTURER} ${Build.MODEL}"
                                } else {
                                    "Auto System Info: Arus Dompet v${AdminSupportHelper.APP_VERSION_NAME} (${AdminSupportHelper.APP_VERSION_CODE}) • Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) • ${Build.MANUFACTURER} ${Build.MODEL}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        // Optional Screenshot Picker (Zero-permission Android Photo Picker)
                        OutlinedButton(
                            onClick = {
                                pickScreenshotLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (bugScreenshotUri != null) {
                                    if (isId) "✅ Screenshot Terlampir (Ketuk untuk Ganti)" else "✅ Screenshot Attached (Tap to Change)"
                                } else {
                                    if (isId) "Lampirkan Screenshot Masalah (Opsional)" else "Attach Issue Screenshot (Optional)"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Button(
                            onClick = {
                                if (bugTitle.isBlank() && bugDescription.isBlank()) {
                                    onShowMessage(
                                        if (isId) "Mohon isi judul atau deskripsi masalah terlebih dahulu."
                                        else "Please enter an issue title or description first."
                                    )
                                } else {
                                    AdminSupportHelper.launchBugReportEmail(
                                        context = context,
                                        issueType = selectedIssueType,
                                        issueTitle = bugTitle,
                                        issueDescription = bugDescription,
                                        stepsToReproduce = bugSteps,
                                        attachmentUri = bugScreenshotUri,
                                        lang = language,
                                        onFallbackMessage = onShowMessage
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BcaRoyalBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_bug_report_button")
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isId) "Kirim Laporan ke ${AdminSupportHelper.ADMIN_EMAIL}" else "Send Bug Report to ${AdminSupportHelper.ADMIN_EMAIL}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    2 -> {
                        // TAB 2: KIRIM SARAN FITUR
                        Text(
                            text = if (isId) {
                                "Punya ide agar Arus Dompet makin nyaman dipakai? Kirimkan saran Anda langsung kepada Admin ${AdminSupportHelper.ADMIN_NAME} (${AdminSupportHelper.ADMIN_EMAIL})."
                            } else {
                                "Have an idea to make Arus Dompet even better? Send your feature suggestion directly to Admin ${AdminSupportHelper.ADMIN_NAME} (${AdminSupportHelper.ADMIN_EMAIL})."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = suggestionTitle,
                            onValueChange = { suggestionTitle = it },
                            label = { Text(if (isId) "Judul Ide / Fitur yang Diinginkan" else "Feature Idea Title") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("suggestion_title_input")
                        )

                        OutlinedTextField(
                            value = suggestionDetail,
                            onValueChange = { suggestionDetail = it },
                            label = { Text(if (isId) "Ceritakan Detail Saran Anda" else "Describe Your Suggestion in Detail") },
                            minLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("suggestion_detail_input")
                        )

                        Button(
                            onClick = {
                                if (suggestionTitle.isBlank() && suggestionDetail.isBlank()) {
                                    onShowMessage(
                                        if (isId) "Mohon tuliskan saran Anda terlebih dahulu."
                                        else "Please write your suggestion first."
                                    )
                                } else {
                                    AdminSupportHelper.launchFeatureSuggestionEmail(
                                        context = context,
                                        category = suggestionCategory,
                                        suggestionTitle = suggestionTitle,
                                        suggestionDetail = suggestionDetail,
                                        lang = language,
                                        onFallbackMessage = onShowMessage
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_suggestion_button")
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isId) "Kirim Saran ke Admin ${AdminSupportHelper.ADMIN_NAME}" else "Send Suggestion to Admin ${AdminSupportHelper.ADMIN_NAME}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    else -> {
                        // TAB 3: 11 INTERACTIVE FAQs BASED ON REAL APP FEATURES
                        OutlinedTextField(
                            value = faqSearchQuery,
                            onValueChange = { faqSearchQuery = it },
                            label = { Text(if (isId) "Cari topik bantuan (misal: saldo, limit, backup)..." else "Search help topics (e.g., balance, limit, backup)...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("faq_search_input")
                        )

                        val faqList = remember(isId) { buildRealAppFaqItems(isId) }
                        val filteredFaqs = remember(faqList, faqSearchQuery) {
                            if (faqSearchQuery.isBlank()) faqList
                            else faqList.filter {
                                it.first.contains(faqSearchQuery, ignoreCase = true) ||
                                    it.second.contains(faqSearchQuery, ignoreCase = true)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredFaqs.forEachIndexed { idx, (question, answer) ->
                                val isExpanded = expandedFaqIndex == idx
                                Card(
                                    onClick = { expandedFaqIndex = if (isExpanded) -1 else idx },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isExpanded) BcaRoyalBlue.copy(alpha = 0.09f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isExpanded) BcaRoyalBlue.copy(alpha = 0.5f) else Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = question,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = BcaRoyalBlue
                                            )
                                        }
                                        AnimatedVisibility(visible = isExpanded) {
                                            Column {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = answer,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.5.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

private fun buildRealAppFaqItems(isId: Boolean): List<Pair<String, String>> {
    return if (isId) {
        listOf(
            "1. Bagaimana cara mencatat pemasukan (Uang Masuk)?" to
                "Ketuk tombol bulat berlambang uang di bagian bawah layar Beranda, pilih tab 'Uang Masuk', pilih sumber pemasukan (Gaji, Freelance, Bisnis, dll.), ketik nominal, pilih dompet penerima (misal BCA atau DANA), lalu tekan '+ Masuk'. Saldo dompet dan tabel riwayat harian akan otomatis bertambah.",
            "2. Bagaimana cara mencatat pengeluaran harian?" to
                "Ketuk salah satu kotak kategori di bawah Total Saldo atau ketuk tombol uang di bawah, pilih pos pengeluaran (Kebutuhan Harian, Kebutuhan Wajib, atau kategori kustom), masukkan nominal, pilih dompet sumber uang, lalu tekan 'Potong'.",
            "3. Bagaimana cara menambah atau mengubah kategori pengeluaran?" to
                "Pada grup kotak 'Limit Pengeluaran' di bawah Total Saldo, ketuk tombol '+ Kategori' untuk membuat pos pengeluaran baru, atau ketuk kartu kategori mana saja untuk mengubah nama kategori, nominal limit, serta rentang tanggal kalender.",
            "4. Apa perbedaan Uang Bebas Pakai dan Total Saldo?" to
                "Kotak Total Saldo menampilkan rincian seluruh uang Anda di semua Bank, E-Wallet, dan Tunai secara langsung. Sementara Simpanan Darurat dan Tabungan Target dipisahkan agar uang tabungan masa depan Anda tidak terpakai tanpa sengaja untuk jajan harian.",
            "5. Bagaimana cara mengatur Limit Pengeluaran dan Kalender?" to
                "Ketuk salah satu kotak kategori di bagian 'Limit Pengeluaran'. Anda dapat memilih periode limit Harian, Mingguan, Bulanan, atau menggunakan fitur Kalender (Rentang Tanggal Kustom dari tanggal mulai hingga selesai).",
            "6. Bagaimana cara melihat laporan dan tabel riwayat transaksi?" to
                "Tabel 'Riwayat Transaksi' berada tepat di bawah grup kategori, tersusun rapi per tanggal seperti Excel (lengkap dengan jam, dompet, nominal utuh, dan total harian). Ketuk tombol 'Simpan Laporan' pada header tabel untuk membuka rekap statistik, atau ketuk tombol 'Grafik' di bagian atas Beranda.",
            "7. Bagaimana cara kerja Asisten Keuangan Pintar Offline?" to
                "Kartu 'Asisten Keuangan Pintar (Offline)' menganalisis pemasukan, pengeluaran, rasio hemat, limit kategori, dan tren 30 hari Anda 100% secara lokal menggunakan database 10.000 kalimat tanpa internet. Anda bisa mengganti 12 kepribadian (Si Analis, Si Roaster, Si Kawan Dekat, dll.) melalui tombol 'Gaya' di kartu asisten atau di Pengaturan.",
            "8. Bagaimana cara melakukan Backup Data Lengkap (.json)?" to
                "Buka Pengaturan -> bagian 'Data dan Penyimpanan' -> ketuk 'Pusat Backup, Restore & Penyimpanan' -> pilih 'Simpan File .JSON' atau 'Bagikan / Drive'. Seluruh dompet, transaksi, target tabungan, tagihan, dan pengaturan akan disimpan dengan checksum SHA-256.",
            "9. Bagaimana cara Restore (Memulihkan) data saat ganti HP?" to
                "Buka Pengaturan -> 'Data dan Penyimpanan' -> 'Pusat Backup, Restore & Penyimpanan' -> ketuk 'Pilih File Backup (.JSON) dari Perangkat'. Sistem akan memvalidasi keutuhan file terlebih dahulu, menampilkan ringkasan isi backup, dan memberi pilihan 'Ganti Semua' atau 'Gabungkan Tanpa Duplikat'.",
            "10. Bagaimana cara mengekspor laporan ke PDF atau Excel (.csv)?" to
                "Anda dapat mengekspor laporan tabel PDF modern maupun file Excel (.csv) melalui tombol 'Simpan Laporan' di bagian Riwayat Transaksi Beranda, atau melalui menu Pengaturan -> 'Data dan Penyimpanan'.",
            "11. Bagaimana jika aplikasi mengalami kendala atau saya butuh bantuan?" to
                "Coba bersihkan cache sementara melalui Pengaturan -> 'Data dan Penyimpanan' -> 'Bersihkan Cache Sementara' (tidak menghapus transaksi). Jika masih terkendala, buka 'Bantuan dan Masukan' -> 'Lapor Bug' atau hubungi Admin iwa.ye melalui email programerway@gmail.com."
        )
    } else {
        listOf(
            "1. How do I record income (Cash Inflow)?" to
                "Tap the floating money toggle button at the bottom of the Home dashboard, switch to 'Cash Inflow', pick an income stream (Salary, Freelance, Business, etc.), enter the amount, select the destination wallet, and tap '+ Deposit'.",
            "2. How do I record a daily expense?" to
                "Tap any category card below Total Balance or tap the bottom money button, choose the expense category, enter the amount, select which wallet to deduct from, and tap 'Deduct'.",
            "3. How do I add or edit expense categories?" to
                "In the 'Spend Limit' category section below Total Balance, tap '+ Category' to create a custom category, or tap any existing category card to edit its name, budget cap, and custom calendar date range.",
            "4. How does Total Balance work alongside Emergency & Target Savings?" to
                "The Total Balance card displays all your Banks, Digital Wallets, and Cash directly. Meanwhile, your Emergency Fund strip and Target Savings goals are tracked separately so your long-term savings are protected from daily spending.",
            "5. How do I configure Spend Limits and Calendar Date Ranges?" to
                "Tap any category card under 'Spend Limit'. You can set a Daily, Weekly, Monthly cap, or pick a Custom Date Range using the built-in calendar tool.",
            "6. How do I view reports and the Excel-style transaction table?" to
                "The 'Transaction History' section below categories groups records by date in a compact Excel-style table showing exact amounts, time, wallet source, and daily totals. Tap 'Save Report' on the table header or 'Charts' in the top bar for full analytics.",
            "7. How does the Smart Offline Financial Assistant work?" to
                "The 'Smart Financial Assistant (Offline)' analyzes your income, expenses, savings rate, category caps, and 30-day habits 100% locally using a 10,000-phrase SQLite database with zero internet required. You can customize its 12 personas (Analyst, Roaster, Bestie, etc.) anytime.",
            "8. How do I create a Full Data Backup (.json)?" to
                "Open Settings -> 'Data and Storage' -> 'Backup, Restore & Storage Center' -> tap 'Save .JSON File' or 'Share / Drive'. All wallets, transactions, goals, bills, and preferences are exported with a SHA-256 integrity checksum.",
            "9. How do I Restore my data when switching phones?" to
                "Open Settings -> 'Data and Storage' -> 'Backup, Restore & Storage Center' -> tap 'Select Backup File (.JSON) from Device'. The app verifies the SHA-256 checksum first and lets you either Replace All or Merge Without Duplicates.",
            "10. How do I export reports to PDF or Excel (.csv)?" to
                "You can export sleek PDF table statements or Excel (.csv) spreadsheets either from the 'Save Report' button on the Transaction History card or from Settings -> 'Data and Storage'.",
            "11. What should I do if I encounter an issue or need support?" to
                "First, try clearing temporary cache via Settings -> 'Data and Storage' -> 'Clear Temporary Cache Only' (this keeps all financial data intact). For further help, use 'Help and Feedback' -> 'Bug Report' or email Admin iwa.ye at programerway@gmail.com."
        )
    }
}

/**
 * Comprehensive 10-Section Privacy Policy & Data Safety Disclosure Dialog
 * Accurate to the real codebase architecture & Google Play Developer Program Policies.
 */
@Composable
fun ComprehensivePrivacyPolicyDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isId = language == AppLanguage.INDONESIAN

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("comprehensive_privacy_policy_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
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
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.16f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = EmeraldPrimary)
                            }
                        }
                        Column {
                            Text(
                                text = if (isId) "Kebijakan Privasi & Keamanan Data" else "Privacy Policy & Data Safety",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isId) "Arus Dompet • Transparan, Jujur & Sesuai Standar Google Play" else "Arus Dompet • Transparent & Google Play Compliant",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                val sections = if (isId) {
                    listOf(
                        "1. Komitmen Privasi Arus Dompet" to
                            "Arus Dompet dirancang dengan arsitektur Offline-First. Seluruh catatan keuangan, saldo dompet/bank, anggaran kategori, dan target tabungan adalah milik Anda sepenuhnya dan disimpan secara lokal di perangkat Anda.",
                        "2. Data yang Anda Masukkan di Aplikasi" to
                            "Data yang Anda catat meliputi: nama panggilan, preferensi negara & mata uang, daftar saldo dompet/bank/e-wallet yang Anda ketik secara manual (aplikasi TIDAK terhubung otomatis ke mutasi rekening perbankan Anda), riwayat transaksi pemasukan/pengeluaran, target tabungan, tagihan rutin, dan preferensi gaya bahasa asisten.",
                        "3. Cara Data Digunakan" to
                            "Seluruh data keuangan digunakan semata-mata di dalam perangkat Anda untuk menghitung saldo siap pakai, memantau batas limit kategori, menyusun tabel laporan PDF/Excel, serta menjalankan Asisten Keuangan Pintar Offline (database lokal 10.000 kalimat).",
                        "4. Penyimpanan Lokal & Keamanan PIN" to
                            "• Data transaksi dan profil disimpan di penyimpanan internal pribadi aplikasi (`SharedPreferences` lokal dan database `SQLite` `offline_financial_insight_v1.db`).\n• Jika Anda mengaktifkan Kunci PIN Aplikasi, PIN Anda dienkripsi satu arah menggunakan algoritma kriptografi SHA-256 dengan 16-byte Random Salt dan tidak pernah disimpan dalam bentuk teks biasa.",
                        "5. Sinkronisasi Cloud Opsional & Backup Mandiri" to
                            "• Secara default, aplikasi bekerja 100% lokal di perangkat Anda.\n• Jika Anda memilih masuk (Sign-In) dengan akun Google untuk fitur Sinkronisasi Cloud, data dompet, transaksi, dan target tabungan Anda disinkronkan melalui layanan Google Firebase Firestore milik proyek aplikasi agar dapat dipulihkan saat berganti perangkat.\n• Jika Anda membuat file Backup (.json), PDF, atau Excel (.csv), file tersebut disimpan di lokasi yang Anda pilih sendiri.",
                        "6. Apakah Data Dijual atau Dibagikan ke Pihak Ketiga?" to
                            "TIDAK. Arus Dompet TIDAK menjual, menyewakan, atau memperdagangkan data pribadi maupun data keuangan pengguna kepada pengiklan, pialang data, atau pihak ketiga mana pun.",
                        "7. Izin Perangkat (Permissions) yang Digunakan" to
                            "• Getar (`VIBRATE`): Digunakan untuk umpan balik getaran tombol opsional.\n• Internet (`INTERNET`): Hanya digunakan jika pengguna mengaktifkan autentikasi/sinkronisasi Cloud Firebase opsional.\n• Pemilih Foto Sistem (`PickVisualMedia`): Hanya digunakan secara opsional jika pengguna melampirkan screenshot pada laporan bug, tanpa meminta izin akses galeri penuh.",
                        "8. Hak Pengguna, Retensi & Cara Menghapus Data" to
                            "Data disimpan selama Anda menggunakan aplikasi. Anda memegang kendali penuh untuk:\n• Mengekspor atau mencadangkan seluruh data ke file `.json`, `.pdf`, atau `.csv`.\n• Menghapus transaksi satuan kapan saja.\n• Menghapus seluruh data keuangan secara permanen melalui menu Pengaturan -> Data dan Penyimpanan -> 'Reset / Hapus Seluruh Data Keuangan', atau dengan mencopot pemasangan (uninstall) aplikasi.",
                        "9. Pelaporan Masalah (Bug Report) yang Aman" to
                            "Saat Anda mengirim laporan masalah atau menghubungi admin melalui email, aplikasi hanya menyertakan informasi teknis dasar (versi aplikasi dan versi Android) dan tidak pernah melampirkan rincian saldo atau riwayat transaksi Anda.",
                        "10. Kontak Resmi Admin & Pengembang" to
                            "Jika Anda memiliki pertanyaan mengenai kebijakan privasi atau pengelolaan data di Arus Dompet, silakan hubungi:\n• Nama Admin: ${AdminSupportHelper.ADMIN_NAME}\n• Email Resmi: ${AdminSupportHelper.ADMIN_EMAIL}"
                    )
                } else {
                    listOf(
                        "1. Our Privacy Commitment" to
                            "Arus Dompet is built on an Offline-First architecture. Your financial records, wallet balances, category budgets, and savings goals belong strictly to you and are stored locally on your device.",
                        "2. Data You Enter in the App" to
                            "Data you manually enter includes: nickname, country & currency preference, manually entered wallet/bank balances (the app NEVER connects directly to your bank credentials), income/expense transactions, savings goals, mandatory bills, and offline assistant preferences.",
                        "3. How Your Data Is Used" to
                            "Your financial data is processed locally on your device to calculate safe-to-spend balances, track category limits, generate PDF/CSV reports, and run the 10,000-phrase Smart Offline Financial Assistant.",
                        "4. Local Storage & PIN Security" to
                            "• Financial records and profile settings are stored in private app storage (`SharedPreferences` and `SQLite` `offline_financial_insight_v1.db`).\n• When App PIN Lock is enabled, your PIN is one-way hashed using SHA-256 with a 16-byte cryptographic random salt and is never stored in plain text.",
                        "5. Optional Cloud Sync & Manual Backups" to
                            "• By default, the app operates 100% locally on your device.\n• If you optionally Sign In with Google for Cloud Sync, your wallets, transactions, and savings goals are synced via Google Firebase Firestore so you can restore them across devices.\n• Manual `.json`, `.pdf`, and `.csv` exports are saved only to folders or apps you explicitly choose.",
                        "6. Data Sharing & Third Parties" to
                            "Arus Dompet DOES NOT sell, rent, or trade your personal or financial data to advertisers, data brokers, or any third party.",
                        "7. Device Permissions Used" to
                            "• Vibration (`VIBRATE`): Used for optional tactile button feedback.\n• Internet (`INTERNET`): Used only when optional Firebase Cloud Sync is active.\n• System Photo Picker (`PickVisualMedia`): Used only if you optionally attach a screenshot to a bug report, requiring zero broad storage permissions.",
                        "8. Data Retention & How to Delete Your Data" to
                            "Your data remains on your device for as long as you use the app. You can delete individual records anytime, or permanently wipe all financial records via Settings -> Data and Storage -> 'Reset / Wipe All Financial Data', or by uninstalling the app.",
                        "9. Privacy-Safe Bug Reporting" to
                            "When contacting support or submitting a bug report, only basic device diagnostics (app version and Android OS version) are pre-filled. Your balances and transaction history are never attached.",
                        "10. Official Admin & Developer Contact" to
                            "For any questions regarding privacy or data protection in Arus Dompet, please contact:\n• Admin Name: ${AdminSupportHelper.ADMIN_NAME}\n• Official Email: ${AdminSupportHelper.ADMIN_EMAIL}"
                    )
                }

                sections.forEach { (title, body) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BcaRoyalBlue
                            )
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isId) "Saya Mengerti & Tutup" else "Got It & Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
