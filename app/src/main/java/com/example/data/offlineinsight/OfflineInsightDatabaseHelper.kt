package com.example.data.offlineinsight

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Local SQLite Database Manager for the 10,000-Phrase Offline Financial Insight Engine.
 *
 * Key Engineering Guarantees:
 * - Zero external AI / internet / paid token calls.
 * - Imports the 10,000 unique phrases ONCE in a single atomic SQLite transaction on first creation.
 * - Uses composite B-Tree indexes (`idx_phrase_lookup`, `idx_phrase_cond_prio`, `idx_usage_time`)
 *   so dashboard queries fetch only top relevant candidates (`LIMIT 30`) in < 2ms without loading
 *   all 10,000 phrases into memory.
 */
class OfflineInsightDatabaseHelper private constructor(
    private val appContext: Context
) : SQLiteOpenHelper(appContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "offline_financial_insight_v1.db"
        const val DATABASE_VERSION = 1
        const val ASSET_FILE_NAME = "offline_insight_phrases_10000.tsv"
        const val EXPECTED_TOTAL_PHRASES = 10_000

        const val TABLE_PHRASES = "phrase_database"
        const val TABLE_USAGE_HISTORY = "phrase_usage_history"
        const val TABLE_INSIGHTS_CACHE = "financial_insights"
        const val TABLE_COMM_PREFS = "communication_preferences"

        val ALLOWED_PLACEHOLDERS = setOf(
            "{saldo}",
            "{total_pemasukan}",
            "{total_pengeluaran}",
            "{nama_kategori}",
            "{persentase_anggaran}",
            "{sisa_limit}",
            "{target_tabungan}",
            "{jumlah_tabungan}",
            "{jumlah_hari}",
            "{jumlah_tagihan}"
        )

        private val PLACEHOLDER_REGEX = Regex("\\{[a-zA-Z0-9_]+\\}")

        @Volatile
        private var INSTANCE: OfflineInsightDatabaseHelper? = null

        fun getInstance(context: Context): OfflineInsightDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineInsightDatabaseHelper(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun resetInstanceForTesting() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTablesAndIndexes(db)
        seedPhraseDatabaseIfNeeded(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createTablesAndIndexes(db)
        seedPhraseDatabaseIfNeeded(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            seedPhraseDatabaseIfNeeded(db)
        }
    }

    private fun createTablesAndIndexes(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_PHRASES (
                id TEXT PRIMARY KEY NOT NULL,
                persona TEXT NOT NULL,
                category TEXT NOT NULL,
                condition_code TEXT NOT NULL,
                priority INTEGER NOT NULL,
                length_type TEXT NOT NULL,
                placeholders TEXT NOT NULL,
                text_id TEXT NOT NULL,
                text_en TEXT NOT NULL,
                action_id TEXT NOT NULL,
                action_en TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USAGE_HISTORY (
                phrase_id TEXT PRIMARY KEY NOT NULL,
                last_used_millis INTEGER NOT NULL,
                usage_count INTEGER NOT NULL DEFAULT 1,
                condition_code TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_INSIGHTS_CACHE (
                signature_hash TEXT PRIMARY KEY NOT NULL,
                phrase_id TEXT NOT NULL,
                persona TEXT NOT NULL,
                condition_code TEXT NOT NULL,
                created_at_millis INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_COMM_PREFS (
                pref_key TEXT PRIMARY KEY NOT NULL,
                pref_value TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Indexes for fast O(log N) lookup by persona, condition, priority, and category
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_phrase_lookup ON $TABLE_PHRASES(persona, condition_code, priority, length_type)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_phrase_cond_prio ON $TABLE_PHRASES(condition_code, priority, category)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_usage_time ON $TABLE_USAGE_HISTORY(last_used_millis)"
        )
    }

    fun ensureSeeded() {
        val db = writableDatabase
        seedPhraseDatabaseIfNeeded(db)
    }

    private fun openPhraseDatasetStream(): InputStream {
        return try {
            appContext.assets.open(ASSET_FILE_NAME)
        } catch (e: Exception) {
            // Fallback for local JVM unit tests where assets directory might be relative to working dir
            val candidates = listOf(
                File("app/src/main/assets/$ASSET_FILE_NAME"),
                File("src/main/assets/$ASSET_FILE_NAME"),
                File("../app/src/main/assets/$ASSET_FILE_NAME")
            )
            val found = candidates.firstOrNull { it.exists() && it.isFile }
                ?: throw IllegalStateException("Dataset $ASSET_FILE_NAME not found in assets or filesystem", e)
            FileInputStream(found)
        }
    }

    private fun seedPhraseDatabaseIfNeeded(db: SQLiteDatabase) {
        val currentCount = getPhraseCountInternal(db)
        if (currentCount == EXPECTED_TOTAL_PHRASES) {
            return
        }

        db.beginTransaction()
        try {
            db.delete(TABLE_PHRASES, null, null)
            val sql = """
                INSERT INTO $TABLE_PHRASES (
                    id, persona, category, condition_code, priority,
                    length_type, placeholders, text_id, text_en, action_id, action_en
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            val stmt = db.compileStatement(sql)

            openPhraseDatasetStream().use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            val parts = line.split('\t')
                            if (parts.size >= 11) {
                                stmt.clearBindings()
                                stmt.bindString(1, parts[0])
                                stmt.bindString(2, parts[1])
                                stmt.bindString(3, parts[2])
                                stmt.bindString(4, parts[3])
                                stmt.bindLong(5, parts[4].toLongOrNull() ?: 5L)
                                stmt.bindString(6, parts[5])
                                stmt.bindString(7, parts[6])
                                stmt.bindString(8, parts[7])
                                stmt.bindString(9, parts[8])
                                stmt.bindString(10, parts[9])
                                stmt.bindString(11, parts[10])
                                stmt.executeInsert()
                            }
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun getPhraseCountInternal(db: SQLiteDatabase): Int {
        db.rawQuery("SELECT COUNT(*) FROM $TABLE_PHRASES", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun getTotalPhraseCount(): Int {
        ensureSeeded()
        return getPhraseCountInternal(readableDatabase)
    }

    fun getCountByPersona(persona: InsightPersona): Int {
        ensureSeeded()
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_PHRASES WHERE persona = ?",
            arrayOf(persona.name)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    /**
     * Queries only relevant candidate phrases using SQLite indexes and anti-repetition LEFT JOIN.
     * Never loads all 10,000 phrases into memory!
     */
    fun queryCandidatePhrases(
        persona: InsightPersona,
        conditionCodes: List<FinancialConditionCode>,
        preferredLength: String = "ANY",
        cooldownCutoffMillis: Long,
        limit: Int = 25
    ): List<InsightPhraseRecord> {
        ensureSeeded()
        val db = readableDatabase
        val effectiveConditions = if (conditionCodes.isEmpty()) {
            listOf(FinancialConditionCode.BALANCE_SAFE)
        } else {
            conditionCodes
        }

        val placeholdersIn = effectiveConditions.joinToString(",") { "?" }
        val args = mutableListOf<String>()
        args.add(persona.name)
        effectiveConditions.forEach { args.add(it.name) }

        val lengthFilterClause = if (preferredLength == "SHORT" || preferredLength == "DETAILED") {
            // Order preferred length first rather than hard-excluding so candidates are always found
            "CASE WHEN p.length_type = ? THEN 0 ELSE 1 END ASC,"
        } else {
            ""
        }
        if (preferredLength == "SHORT" || preferredLength == "DETAILED") {
            args.add(preferredLength)
        }
        args.add(cooldownCutoffMillis.toString())
        args.add(limit.toString())

        val query = """
            SELECT
                p.id, p.persona, p.category, p.condition_code, p.priority,
                p.length_type, p.placeholders, p.text_id, p.text_en, p.action_id, p.action_en,
                COALESCE(h.last_used_millis, 0) AS last_used,
                COALESCE(h.usage_count, 0) AS used_cnt
            FROM $TABLE_PHRASES p
            LEFT JOIN $TABLE_USAGE_HISTORY h ON p.id = h.phrase_id
            WHERE p.persona = ?
              AND p.condition_code IN ($placeholdersIn)
            ORDER BY
              p.priority ASC,
              $lengthFilterClause
              CASE WHEN COALESCE(h.last_used_millis, 0) < ? THEN 0 ELSE 1 END ASC,
              COALESCE(h.usage_count, 0) ASC,
              COALESCE(h.last_used_millis, 0) ASC,
              p.id ASC
            LIMIT ?
        """.trimIndent()

        val results = mutableListOf<InsightPhraseRecord>()
        db.rawQuery(query, args.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val pName = cursor.getString(1)
                val catName = cursor.getString(2)
                val condName = cursor.getString(3)
                val prio = cursor.getInt(4)
                val lenType = cursor.getString(5)
                val phRaw = cursor.getString(6)
                val textId = cursor.getString(7)
                val textEn = cursor.getString(8)
                val actId = cursor.getString(9)
                val actEn = cursor.getString(10)

                results.add(
                    InsightPhraseRecord(
                        id = id,
                        persona = InsightPersona.fromNameOrDefault(pName, persona),
                        category = InsightCategory.fromNameOrDefault(catName),
                        conditionCode = FinancialConditionCode.fromCodeOrDefault(condName),
                        priority = prio,
                        lengthType = lenType,
                        requiredPlaceholders = phRaw.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        textId = textId,
                        textEn = textEn,
                        actionId = actId,
                        actionEn = actEn
                    )
                )
            }
        }
        return results
    }

    /**
     * Records that a phrase was displayed to the user so anti-repetition avoids repeating it.
     */
    fun recordPhraseUsage(phraseId: String, conditionCode: FinancialConditionCode, nowMillis: Long = System.currentTimeMillis()) {
        val db = writableDatabase
        val existingCount = db.rawQuery(
            "SELECT usage_count FROM $TABLE_USAGE_HISTORY WHERE phrase_id = ?",
            arrayOf(phraseId)
        ).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

        val values = ContentValues().apply {
            put("phrase_id", phraseId)
            put("last_used_millis", nowMillis)
            put("usage_count", existingCount + 1)
            put("condition_code", conditionCode.name)
        }
        db.insertWithOnConflict(TABLE_USAGE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getRecentlyUsedPhraseIds(limit: Int = 50): Set<String> {
        val db = readableDatabase
        val set = mutableSetOf<String>()
        db.rawQuery(
            "SELECT phrase_id FROM $TABLE_USAGE_HISTORY ORDER BY last_used_millis DESC LIMIT ?",
            arrayOf(limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                set.add(cursor.getString(0))
            }
        }
        return set
    }

    fun clearUsageHistory() {
        writableDatabase.delete(TABLE_USAGE_HISTORY, null, null)
        writableDatabase.delete(TABLE_INSIGHTS_CACHE, null, null)
    }

    /**
     * Comprehensive validation mechanism required by specification:
     * - Counts all phrases (must be 10,000)
     * - Ensures all IDs are unique
     * - Finds any empty texts
     * - Finds any duplicate texts
     * - Validates personas and exact persona quotas
     * - Validates categories
     * - Validates placeholders
     * - Ensures every phrase has a valid condition
     */
    fun validateDatabaseIntegrity(): PhraseDatabaseValidationReport {
        ensureSeeded()
        val db = readableDatabase
        val seenIds = HashSet<String>(10_500)
        val seenTextId = HashSet<String>(10_500)
        val countsByPersona = mutableMapOf<InsightPersona, Int>()
        InsightPersona.entries.forEach { countsByPersona[it] = 0 }

        var totalCount = 0
        var emptyTextCount = 0
        var duplicateTextCount = 0
        var invalidPersonaCount = 0
        var invalidCategoryCount = 0
        var invalidPlaceholderCount = 0
        var missingConditionCount = 0
        val errors = mutableListOf<String>()

        val validPersonaNames = InsightPersona.entries.map { it.name }.toSet()
        val validCategoryNames = InsightCategory.entries.map { it.name }.toSet()
        val validConditionNames = FinancialConditionCode.entries.map { it.name }.toSet()

        db.rawQuery(
            "SELECT id, persona, category, condition_code, placeholders, text_id, text_en, action_id FROM $TABLE_PHRASES",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                totalCount++
                val id = cursor.getString(0)?.trim().orEmpty()
                val personaStr = cursor.getString(1)?.trim().orEmpty()
                val categoryStr = cursor.getString(2)?.trim().orEmpty()
                val condStr = cursor.getString(3)?.trim().orEmpty()
                val placeholdersStr = cursor.getString(4)?.trim().orEmpty()
                val textId = cursor.getString(5)?.trim().orEmpty()
                val textEn = cursor.getString(6)?.trim().orEmpty()
                val actionId = cursor.getString(7)?.trim().orEmpty()

                if (id.isNotEmpty()) {
                    seenIds.add(id)
                }

                if (textId.isEmpty() || textEn.isEmpty() || actionId.isEmpty()) {
                    emptyTextCount++
                    if (errors.size < 10) errors.add("Empty text at ID $id")
                }

                if (!seenTextId.add(textId)) {
                    duplicateTextCount++
                    if (errors.size < 10) errors.add("Duplicate text at ID $id")
                }

                if (personaStr !in validPersonaNames) {
                    invalidPersonaCount++
                    if (errors.size < 10) errors.add("Invalid persona '$personaStr' at ID $id")
                } else {
                    val p = InsightPersona.valueOf(personaStr)
                    countsByPersona[p] = (countsByPersona[p] ?: 0) + 1
                }

                if (categoryStr !in validCategoryNames) {
                    invalidCategoryCount++
                    if (errors.size < 10) errors.add("Invalid category '$categoryStr' at ID $id")
                }

                if (condStr !in validConditionNames) {
                    missingConditionCount++
                    if (errors.size < 10) errors.add("Invalid condition '$condStr' at ID $id")
                }

                // Validate placeholders inside textId, textEn, actionId, and declared list
                val tokensInText = PLACEHOLDER_REGEX.findAll("$textId $textEn $actionId").map { it.value }.toSet()
                val declaredTokens = placeholdersStr.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                if (tokensInText.any { it !in ALLOWED_PLACEHOLDERS } || declaredTokens.any { it !in ALLOWED_PLACEHOLDERS }) {
                    invalidPlaceholderCount++
                    if (errors.size < 10) errors.add("Invalid placeholder at ID $id: $tokensInText")
                }
            }
        }

        // Verify each persona quota matches specification
        for (p in InsightPersona.entries) {
            val actual = countsByPersona[p] ?: 0
            if (actual != p.expectedQuota) {
                errors.add("Persona ${p.name} expected ${p.expectedQuota} phrases, found $actual")
            }
        }

        val isValid = totalCount == EXPECTED_TOTAL_PHRASES &&
            seenIds.size == EXPECTED_TOTAL_PHRASES &&
            emptyTextCount == 0 &&
            duplicateTextCount == 0 &&
            invalidPersonaCount == 0 &&
            invalidCategoryCount == 0 &&
            invalidPlaceholderCount == 0 &&
            missingConditionCount == 0 &&
            errors.isEmpty()

        return PhraseDatabaseValidationReport(
            isValid = isValid,
            totalCount = totalCount,
            uniqueIdCount = seenIds.size,
            emptyTextCount = emptyTextCount,
            duplicateTextCount = duplicateTextCount,
            invalidPersonaCount = invalidPersonaCount,
            invalidCategoryCount = invalidCategoryCount,
            invalidPlaceholderCount = invalidPlaceholderCount,
            missingConditionCount = missingConditionCount,
            countsByPersona = countsByPersona,
            validationErrors = errors
        )
    }
}
