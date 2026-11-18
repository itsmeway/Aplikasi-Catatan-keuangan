package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ArusKasRepository(
    private val db: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeWallets(userId: String = requireUserId()): Flow<List<Wallet>> = flow {
        val path = "users/$userId/wallets"
        emitAll(
            db.collection("users").document(userId).collection("wallets")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Wallet::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String = requireUserId()): Flow<List<TransactionRecord>> = flow {
        val path = "users/$userId/transactions"
        emitAll(
            db.collection("users").document(userId).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TransactionRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.transactionDateMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeGoals(userId: String = requireUserId()): Flow<List<SavingsGoal>> = flow {
        val path = "users/$userId/goals"
        emitAll(
            db.collection("users").document(userId).collection("goals")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavingsGoal::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getWalletById(userId: String, walletId: String): Wallet? {
        val docRef = db.collection("users").document(userId).collection("wallets").document(walletId)
        return try {
            docRef.get().await().toObject(
                Wallet::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun upsertWallet(
        walletId: String? = null,
        name: String,
        walletType: String,
        balance: Long,
        colorHex: String,
        iconKey: String
    ): String {
        val uid = requireUserId()
        val cleanName = name.trim().take(60)
        require(cleanName.isNotBlank()) { "Wallet name must not be blank" }
        val safeType = walletType.uppercase().takeIf { it in VALID_WALLET_TYPES } ?: "EWALLET"
        val safeBalance = balance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
        val safeColor = colorHex.trim().take(12).ifBlank { "#10B981" }
        val safeIcon = iconKey.trim().take(40).ifBlank { "ewallet" }

        val collectionRef = db.collection("users").document(uid).collection("wallets")
        val docRef = if (walletId.isNullOrBlank()) collectionRef.document() else collectionRef.document(walletId)
        try {
            db.runTransaction { transaction ->
                val existing = if (!walletId.isNullOrBlank()) transaction.get(docRef) else null
                if (existing != null && existing.exists()) {
                    transaction.update(
                        docRef,
                        mapOf(
                            "name" to cleanName,
                            "walletType" to safeType,
                            "balance" to safeBalance,
                            "colorHex" to safeColor,
                            "iconKey" to safeIcon,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        docRef,
                        mapOf(
                            "userId" to uid,
                            "name" to cleanName,
                            "walletType" to safeType,
                            "balance" to safeBalance,
                            "colorHex" to safeColor,
                            "iconKey" to safeIcon,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                null
            }.await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateWalletBalance(wallet: Wallet, newBalance: Long) {
        val uid = requireUserId()
        require(wallet.id.isNotBlank()) { "Wallet ID must not be blank" }
        val safeBalance = newBalance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
        val docRef = db.collection("users").document(uid).collection("wallets").document(wallet.id)
        try {
            docRef.update(
                mapOf(
                    "balance" to safeBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateWalletDetails(wallet: Wallet, newName: String, newBalance: Long, newType: String) {
        val uid = requireUserId()
        require(wallet.id.isNotBlank()) { "Wallet ID must not be blank" }
        val cleanName = newName.trim().take(60)
        require(cleanName.isNotBlank()) { "Wallet name must not be blank" }
        val safeType = newType.uppercase().takeIf { it in VALID_WALLET_TYPES } ?: "EWALLET"
        val safeBalance = newBalance.coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
        val docRef = db.collection("users").document(uid).collection("wallets").document(wallet.id)
        try {
            docRef.update(
                mapOf(
                    "name" to cleanName,
                    "walletType" to safeType,
                    "balance" to safeBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteWallet(walletId: String) {
        val uid = requireUserId()
        require(walletId.isNotBlank()) { "Wallet ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("wallets").document(walletId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun recordTransactionAndAdjustWallet(
        wallet: Wallet,
        type: String, // EXPENSE, INCOME, SAVINGS_ALLOCATION, TRANSFER
        amount: Long,
        categoryKey: String,
        categoryLabel: String,
        frequencyTag: String,
        note: String,
        transactionDateMillis: Long = System.currentTimeMillis()
    ): String {
        val uid = requireUserId()
        require(wallet.id.isNotBlank()) { "Wallet ID must not be blank" }
        require(amount in 1L..MAX_MONETARY_AMOUNT) { "Amount must be between 1 and $MAX_MONETARY_AMOUNT" }
        val safeType = type.uppercase().takeIf { it in VALID_TX_TYPES } ?: "EXPENSE"
        val safeDate = if (transactionDateMillis > 0L) transactionDateMillis else System.currentTimeMillis()

        val txRef = db.collection("users").document(uid).collection("transactions").document()
        val walletRef = db.collection("users").document(uid).collection("wallets").document(wallet.id)
        val delta = if (safeType == "INCOME") amount else -amount

        val txPayload = mapOf(
            "userId" to uid,
            "type" to safeType,
            "amount" to amount,
            "categoryKey" to categoryKey.trim().ifBlank { "kebutuhan_pokok" }.take(50),
            "categoryLabel" to categoryLabel.trim().ifBlank { "Transaction" }.take(60),
            "frequencyTag" to frequencyTag.trim().ifBlank { "HARIAN" },
            "walletId" to wallet.id,
            "walletName" to wallet.name.trim().ifBlank { "Wallet" }.take(60),
            "note" to note.trim().ifBlank { categoryLabel }.take(300),
            "transactionDateMillis" to safeDate,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.runTransaction { transaction ->
                val walletSnap = transaction.get(walletRef)
                val liveBalance = walletSnap.getLong("balance") ?: wallet.balance
                val updatedBalance = clampSum(liveBalance, delta)
                transaction.set(txRef, txPayload)
                if (walletSnap.exists()) {
                    transaction.update(
                        walletRef,
                        mapOf(
                            "balance" to updatedBalance,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                null
            }.await()
            return txRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            throw e
        }
    }

    suspend fun deleteTransactionAndRevertWallet(
        tx: TransactionRecord,
        currentWallet: Wallet?
    ) {
        val uid = requireUserId()
        require(tx.id.isNotBlank()) { "Transaction ID must not be blank" }
        val txRef = db.collection("users").document(uid).collection("transactions").document(tx.id)
        val targetWalletId = tx.walletId.ifBlank { currentWallet?.id ?: "" }
        try {
            db.runTransaction { transaction ->
                val walletSnap = if (targetWalletId.isNotBlank()) {
                    val walletRef = db.collection("users").document(uid).collection("wallets").document(targetWalletId)
                    walletRef to transaction.get(walletRef)
                } else null

                transaction.delete(txRef)

                if (walletSnap != null && walletSnap.second.exists() && tx.type != "TRANSFER") {
                    val liveBalance = walletSnap.second.getLong("balance") ?: (currentWallet?.balance ?: 0L)
                    val revertDelta = if (tx.type == "INCOME") -tx.amount else tx.amount
                    val updatedBalance = clampSum(liveBalance, revertDelta)
                    transaction.update(
                        walletSnap.first,
                        mapOf(
                            "balance" to updatedBalance,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                null
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, txRef.path)
            throw e
        }
    }

    suspend fun createSavingsGoal(
        title: String,
        targetAmount: Long,
        savedAmount: Long = 0L,
        dailyRecommendation: Long = 10L,
        note: String,
        iconKey: String = "laptop"
    ): String {
        val uid = requireUserId()
        val cleanTitle = title.trim().take(80)
        require(cleanTitle.isNotBlank()) { "Goal title must not be blank" }
        val safeTarget = targetAmount.coerceIn(1L, MAX_MONETARY_AMOUNT)
        val safeSaved = savedAmount.coerceIn(0L, MAX_MONETARY_AMOUNT)
        val safeDaily = dailyRecommendation.coerceIn(0L, MAX_MONETARY_AMOUNT)
        val docRef = db.collection("users").document(uid).collection("goals").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to cleanTitle,
            "targetAmount" to safeTarget,
            "savedAmount" to safeSaved,
            "dailyRecommendation" to safeDaily,
            "note" to note.trim().take(300),
            "iconKey" to iconKey.trim().take(40).ifBlank { "laptop" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun allocateToGoalFromWallet(
        goal: SavingsGoal,
        wallet: Wallet,
        amount: Long,
        note: String
    ) {
        val uid = requireUserId()
        require(goal.id.isNotBlank()) { "Goal ID must not be blank" }
        require(wallet.id.isNotBlank()) { "Wallet ID must not be blank" }
        require(amount in 1L..MAX_MONETARY_AMOUNT) { "Savings amount must be between 1 and $MAX_MONETARY_AMOUNT" }
        val goalRef = db.collection("users").document(uid).collection("goals").document(goal.id)
        val walletRef = db.collection("users").document(uid).collection("wallets").document(wallet.id)
        val txRef = db.collection("users").document(uid).collection("transactions").document()

        val txPayload = mapOf(
            "userId" to uid,
            "type" to "SAVINGS_ALLOCATION",
            "amount" to amount,
            "categoryKey" to "tabungan_impian",
            "categoryLabel" to "Tabungan: ${goal.title}".take(60),
            "frequencyTag" to "TABUNGAN",
            "walletId" to wallet.id,
            "walletName" to wallet.name.trim().ifBlank { "Wallet" }.take(60),
            "note" to note.trim().ifBlank { "Setor ke impian ${goal.title}" }.take(300),
            "transactionDateMillis" to System.currentTimeMillis(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.runTransaction { transaction ->
                val goalSnap = transaction.get(goalRef)
                val walletSnap = transaction.get(walletRef)
                val liveSaved = goalSnap.getLong("savedAmount") ?: goal.savedAmount
                val liveBalance = walletSnap.getLong("balance") ?: wallet.balance
                val updatedSaved = clampSum(liveSaved, amount).coerceIn(0L, MAX_MONETARY_AMOUNT)
                val updatedBalance = clampSum(liveBalance, -amount)

                if (goalSnap.exists()) {
                    transaction.update(
                        goalRef,
                        mapOf(
                            "savedAmount" to updatedSaved,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                if (walletSnap.exists()) {
                    transaction.update(
                        walletRef,
                        mapOf(
                            "balance" to updatedBalance,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                transaction.set(txRef, txPayload)
                null
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, goalRef.path)
            throw e
        }
    }

    suspend fun deleteSavingsGoal(goalId: String) {
        val uid = requireUserId()
        require(goalId.isNotBlank()) { "Goal ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun seedStarterSetup(
        walletBalances: Map<String, Long>,
        initialGoalTitle: String,
        initialGoalTarget: Long,
        initialGoalDaily: Long,
        initialGoalNote: String
    ) {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        try {
            val batch = db.batch()
            PresetCategories.defaultStarterWallets.forEach { preset ->
                val ref = userDoc.collection("wallets").document(preset.id)
                val customBal = (walletBalances[preset.id] ?: preset.balance)
                    .coerceIn(-MAX_MONETARY_AMOUNT, MAX_MONETARY_AMOUNT)
                batch.set(
                    ref,
                    mapOf(
                        "userId" to uid,
                        "name" to preset.name.take(60),
                        "walletType" to preset.walletType,
                        "balance" to customBal,
                        "colorHex" to preset.colorHex,
                        "iconKey" to preset.iconKey,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (initialGoalTitle.isNotBlank() && initialGoalTarget > 0) {
                val goalRef = userDoc.collection("goals").document("goal_starter_laptop")
                batch.set(
                    goalRef,
                    mapOf(
                        "userId" to uid,
                        "title" to initialGoalTitle.trim().take(80),
                        "targetAmount" to initialGoalTarget.coerceIn(1L, MAX_MONETARY_AMOUNT),
                        "savedAmount" to 0L,
                        "dailyRecommendation" to initialGoalDaily.coerceIn(1L, MAX_MONETARY_AMOUNT),
                        "note" to initialGoalNote.trim().ifBlank { "Fokus sisihkan dari penghasilan!" }.take(300),
                        "iconKey" to "laptop",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/wallets")
            throw e
        }
    }

    private fun clampSum(current: Long, delta: Long): Long {
        val result = java.math.BigInteger.valueOf(current).add(java.math.BigInteger.valueOf(delta))
        val min = java.math.BigInteger.valueOf(-MAX_MONETARY_AMOUNT)
        val max = java.math.BigInteger.valueOf(MAX_MONETARY_AMOUNT)
        return result.coerceIn(min, max).toLong()
    }

    companion object {
        const val MAX_MONETARY_AMOUNT = 1_000_000_000_000L
        private val VALID_WALLET_TYPES = setOf("BANK", "EWALLET", "CASH")
        private val VALID_TX_TYPES = setOf("EXPENSE", "INCOME", "SAVINGS_ALLOCATION", "TRANSFER")
    }
}
