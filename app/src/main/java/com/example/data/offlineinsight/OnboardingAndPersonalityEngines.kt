package com.example.data.offlineinsight

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.data.UserFinancialProfile

/**
 * 1. ONBOARDING ENGINE
 * Mengelola informasi awal, profil, tujuan, dan preferensi komunikasi pengguna secara lokal.
 * - Semua pertanyaan profil dapat dilewati (skip).
 * - Gender dan usia hanya opsional dan TIDAK PERNAH digunakan untuk mengasumsikan gaya komunikasi.
 */
class OnboardingEngine(
    private val dbHelper: OfflineInsightDatabaseHelper
) {
    companion object {
        private const val KEY_IS_MANUAL_MODE = "is_manual_mode"
        private const val KEY_MANUAL_PERSONA = "manual_persona"
        private const val KEY_STYLE_PREF = "style_preference"
        private const val KEY_ALLOW_ROAST = "allow_roasting_sarcasm"
        private const val KEY_STRICTNESS = "strictness_level"
        private const val KEY_SENTENCE_LENGTH = "sentence_length"
        private const val KEY_FOCUS_PREF = "focus_preference"
        private const val KEY_AGE_RANGE = "age_range"
        private const val KEY_ACTIVITY_TYPE = "activity_type"
        private const val KEY_PERCEIVED_CONDITION = "self_perceived_condition"
        private const val KEY_PRIMARY_GOAL = "primary_financial_goal"
    }

    fun loadPreferences(): UserCommunicationPreferences {
        dbHelper.ensureSeeded()
        val db = dbHelper.readableDatabase
        val map = mutableMapOf<String, String>()
        db.rawQuery(
            "SELECT pref_key, pref_value FROM ${OfflineInsightDatabaseHelper.TABLE_COMM_PREFS}",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                map[cursor.getString(0)] = cursor.getString(1)
            }
        }

        if (map.isEmpty()) {
            return UserCommunicationPreferences()
        }

        return UserCommunicationPreferences(
            isManualPersonaMode = map[KEY_IS_MANUAL_MODE]?.toBooleanStrictOrNull() ?: false,
            manualPersona = InsightPersona.fromNameOrDefault(map[KEY_MANUAL_PERSONA], InsightPersona.SI_BESTIE),
            stylePreference = CommunicationStylePreference.fromNameOrDefault(map[KEY_STYLE_PREF]),
            allowRoastingAndSarcasm = map[KEY_ALLOW_ROAST]?.toBooleanStrictOrNull() ?: true,
            strictnessLevel = map[KEY_STRICTNESS] ?: "BALANCED",
            sentenceLength = map[KEY_SENTENCE_LENGTH] ?: "ANY",
            focusPreference = map[KEY_FOCUS_PREF] ?: "BALANCED",
            ageRange = map[KEY_AGE_RANGE] ?: "PREFER_NOT_TO_SAY",
            activityType = map[KEY_ACTIVITY_TYPE] ?: "EMPLOYEE",
            selfPerceivedCondition = map[KEY_PERCEIVED_CONDITION] ?: "STABLE",
            primaryFinancialGoal = map[KEY_PRIMARY_GOAL] ?: "CONTROL_EXPENSE"
        )
    }

    fun savePreferences(prefs: UserCommunicationPreferences) {
        val db = dbHelper.writableDatabase
        val entries = mapOf(
            KEY_IS_MANUAL_MODE to prefs.isManualPersonaMode.toString(),
            KEY_MANUAL_PERSONA to prefs.manualPersona.name,
            KEY_STYLE_PREF to prefs.stylePreference.name,
            KEY_ALLOW_ROAST to prefs.allowRoastingAndSarcasm.toString(),
            KEY_STRICTNESS to prefs.strictnessLevel,
            KEY_SENTENCE_LENGTH to prefs.sentenceLength,
            KEY_FOCUS_PREF to prefs.focusPreference,
            KEY_AGE_RANGE to prefs.ageRange,
            KEY_ACTIVITY_TYPE to prefs.activityType,
            KEY_PERCEIVED_CONDITION to prefs.selfPerceivedCondition,
            KEY_PRIMARY_GOAL to prefs.primaryFinancialGoal
        )

        db.beginTransaction()
        try {
            for ((k, v) in entries) {
                val cv = ContentValues().apply {
                    put("pref_key", k)
                    put("pref_value", v)
                }
                db.insertWithOnConflict(
                    OfflineInsightDatabaseHelper.TABLE_COMM_PREFS,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Sinkronisasi dari UserFinancialProfile kuesioner awal tanpa mengasumsikan kepribadian dari gender/usia.
     */
    fun syncFromUserFinancialProfile(profile: UserFinancialProfile, currentPrefs: UserCommunicationPreferences): UserCommunicationPreferences {
        val mappedGoal = when (profile.primaryFinancialGoal) {
            "BISA_MENABUNG" -> "ADD_SAVINGS"
            "DANA_DARURAT" -> "BUILD_EMERGENCY"
            "LUNASI_CICILAN" -> "PAY_DEBT"
            "BEBAS_BONCOS" -> "CONTROL_EXPENSE"
            else -> currentPrefs.primaryFinancialGoal
        }
        val mappedStrictness = when (profile.controlMode) {
            "KETAT" -> "STRICT"
            "MINGGUAN" -> "BALANCED"
            else -> currentPrefs.strictnessLevel
        }
        val updated = currentPrefs.copy(
            primaryFinancialGoal = mappedGoal,
            strictnessLevel = mappedStrictness
        )
        savePreferences(updated)
        return updated
    }
}

/**
 * 3. PERSONALITY ENGINE
 * Menentukan gaya komunikasi & persona berdasarkan preferensi pengguna dan kondisi keuangan aktual.
 *
 * Aturan Etika & Personalisasi:
 * - TIDAK PERNAH menentukan kepribadian berdasarkan gender atau usia.
 * - Jika pengguna memilih manual persona, gunakan pilihan tersebut (namun jika humor/sarkasme dimatikan
 *   secara eksplisit, alihkan otomatis dari SI_ROASTER/SI_SARKAS ke persona non-roast yang setara).
 * - Jika mode otomatis / gaya komunikasi dipilih, sesuaikan dengan tingkat ketegasan, fokus solusi/motivasi,
 *   dan kondisi keuangan saat ini.
 */
class PersonalityEngine {

    fun resolveActivePersona(
        prefs: UserCommunicationPreferences,
        primaryCondition: FinancialConditionCode,
        priorityLevel: Int
    ): InsightPersona {
        // 1. Jika pengguna memilih satu kepribadian secara manual:
        if (prefs.isManualPersonaMode) {
            val chosen = prefs.manualPersona
            if (!prefs.allowRoastingAndSarcasm && chosen.usesHumorOrRoast) {
                // Jika humor/roasting dimatikan, gunakan alternatif tegas/akrab tanpa roast
                return if (prefs.strictnessLevel == "STRICT") InsightPersona.SI_REALISTIS else InsightPersona.SI_BESTIE
            }
            return chosen
        }

        // 2. Berdasarkan Gaya Komunikasi Pilihan Pengguna:
        val basePersona = when (prefs.stylePreference) {
            CommunicationStylePreference.FUN_CASUAL -> {
                if (prefs.allowRoastingAndSarcasm) {
                    when {
                        priorityLevel <= 3 -> InsightPersona.SI_ROASTER
                        primaryCondition.defaultCategory == InsightCategory.APRESIASI -> InsightPersona.SI_HYPE
                        else -> InsightPersona.SI_BESTIE
                    }
                } else {
                    if (primaryCondition.defaultCategory == InsightCategory.APRESIASI) {
                        InsightPersona.SI_HYPE
                    } else {
                        InsightPersona.SI_BESTIE
                    }
                }
            }

            CommunicationStylePreference.FIRM_DIRECT -> {
                if (prefs.allowRoastingAndSarcasm && prefs.strictnessLevel == "STRICT" && priorityLevel <= 3) {
                    InsightPersona.SI_SARKAS
                } else if (prefs.focusPreference == "SOLUTION") {
                    InsightPersona.SI_STRATEGIS
                } else {
                    InsightPersona.SI_REALISTIS
                }
            }

            CommunicationStylePreference.SUPPORTIVE_GENTLE -> {
                when {
                    primaryCondition.defaultCategory == InsightCategory.APRESIASI -> InsightPersona.SI_HYPE
                    prefs.focusPreference == "MOTIVATION" -> InsightPersona.SI_MOTIVATOR
                    priorityLevel <= 3 -> InsightPersona.SI_EMPATI
                    else -> InsightPersona.SI_MENTOR
                }
            }

            CommunicationStylePreference.LOGICAL_DATA -> {
                if (primaryCondition.defaultCategory == InsightCategory.KEBIASAAN) {
                    InsightPersona.SI_DETEKTIF
                } else {
                    InsightPersona.SI_ANALIS
                }
            }

            CommunicationStylePreference.STRATEGIC_SOLUTION -> {
                when {
                    primaryCondition.defaultCategory == InsightCategory.TABUNGAN -> InsightPersona.SI_AMBISIUS
                    priorityLevel >= 5 && prefs.strictnessLevel == "GENTLE" -> InsightPersona.SI_MENTOR
                    else -> InsightPersona.SI_STRATEGIS
                }
            }

            CommunicationStylePreference.AUTO_ADAPTIVE -> {
                resolveAdaptivePersona(prefs, primaryCondition, priorityLevel)
            }
        }

        // Pastikan aturan anti-roast selalu dihormati
        if (!prefs.allowRoastingAndSarcasm && basePersona.usesHumorOrRoast) {
            return if (prefs.strictnessLevel == "STRICT") InsightPersona.SI_REALISTIS else InsightPersona.SI_BESTIE
        }

        return basePersona
    }

    private fun resolveAdaptivePersona(
        prefs: UserCommunicationPreferences,
        condition: FinancialConditionCode,
        priorityLevel: Int
    ): InsightPersona {
        // Kondisi Apresiasi / Pencapaian Positif
        if (condition.defaultCategory == InsightCategory.APRESIASI) {
            return when (prefs.focusPreference) {
                "SOLUTION" -> InsightPersona.SI_AMBISIUS
                "MOTIVATION" -> InsightPersona.SI_HYPE
                else -> InsightPersona.SI_MOTIVATOR
            }
        }

        // Kondisi Kritis (Prioritas 1 - 2)
        if (priorityLevel <= 2) {
            return when (prefs.strictnessLevel) {
                "GENTLE" -> InsightPersona.SI_EMPATI
                "STRICT" -> if (prefs.allowRoastingAndSarcasm) InsightPersona.SI_ROASTER else InsightPersona.SI_REALISTIS
                else -> if (prefs.focusPreference == "SOLUTION") InsightPersona.SI_STRATEGIS else InsightPersona.SI_REALISTIS
            }
        }

        // Kondisi Peringatan Menengah / Anggaran / Tagihan (Prioritas 3 - 4)
        if (priorityLevel in 3..4) {
            if (condition.defaultCategory == InsightCategory.KEBIASAAN) {
                return InsightPersona.SI_DETEKTIF
            }
            return when {
                prefs.allowRoastingAndSarcasm && prefs.strictnessLevel == "STRICT" -> InsightPersona.SI_SARKAS
                prefs.focusPreference == "SOLUTION" -> InsightPersona.SI_STRATEGIS
                prefs.focusPreference == "MOTIVATION" -> InsightPersona.SI_MOTIVATOR
                else -> InsightPersona.SI_BESTIE
            }
        }

        // Kondisi Tabungan / Pertumbuhan / Normal (Prioritas 5 - 7)
        return when (condition.defaultCategory) {
            InsightCategory.TABUNGAN -> InsightPersona.SI_AMBISIUS
            InsightCategory.KEBIASAAN -> InsightPersona.SI_DETEKTIF
            InsightCategory.ANGGARAN -> InsightPersona.SI_ANALIS
            else -> InsightPersona.SI_MENTOR
        }
    }

    /**
     * Daftar persona kandidat cadangan yang relevan jika pengguna ingin variasi lintas perspektif.
     */
    fun getFallbackPersonas(primary: InsightPersona, allowRoast: Boolean): List<InsightPersona> {
        return InsightPersona.entries.filter { p ->
            p != primary && (allowRoast || !p.usesHumorOrRoast)
        }
    }
}
