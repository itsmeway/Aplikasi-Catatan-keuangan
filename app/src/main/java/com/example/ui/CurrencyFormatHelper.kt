package com.example.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.WorldCountryCurrencyRegistry
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a raw numeric string into thousand-separated currency format.
 * Preserves Indonesian dot-separated methods (`formatDigits`, `formatWithPrefix`)
 * for unit tests while adding country-sensitive formatting for global currencies.
 */
object CurrencyInputFormatter {
    private val idSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val idDecimalFormat = DecimalFormat("#,###", idSymbols)

    /**
     * Formats raw digits into dotted thousands, e.g. "50000" -> "50.000".
     */
    fun formatDigits(rawDigits: String): String {
        val clean = rawDigits.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val num = clean.take(13).toLongOrNull()?.coerceAtMost(ArusKasViewModel.MAX_MONETARY_AMOUNT) ?: return clean
        return idDecimalFormat.format(num)
    }

    /**
     * Formats raw digits according to the specified country's thousands separator (',' or '.').
     */
    fun formatDigitsForCountry(
        rawDigits: String,
        countryCode: String = WorldCountryCurrencyRegistry.activeCountryCode
    ): String {
        val clean = rawDigits.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val num = clean.take(13).toLongOrNull()?.coerceAtMost(ArusKasViewModel.MAX_MONETARY_AMOUNT) ?: return clean
        val profile = WorldCountryCurrencyRegistry.byCountryCode(countryCode)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = profile.thousandsSeparator
            decimalSeparator = if (profile.thousandsSeparator == '.') ',' else '.'
        }
        return DecimalFormat("#,###", symbols).format(num)
    }

    /**
     * Returns the formatted string with "Rp " prefix.
     * E.g. "50000" -> "Rp 50.000"
     */
    fun formatWithPrefix(rawDigits: String): String {
        val formatted = formatDigits(rawDigits)
        return if (formatted.isNotEmpty()) "Rp $formatted" else ""
    }

    /**
     * Returns the formatted string with the active country's currency symbol prefix.
     */
    fun formatWithActiveCurrency(
        rawDigits: String,
        countryCode: String = WorldCountryCurrencyRegistry.activeCountryCode
    ): String {
        val clean = rawDigits.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val num = clean.toLongOrNull() ?: return ""
        return WorldCountryCurrencyRegistry.formatCurrency(num, countryCode)
    }

    /**
     * Returns the currency input prefix string for OutlinedTextField (e.g. "$ ", "Rp ", "£ ", "€ ", "¥ ").
     */
    fun activeCurrencyPrefix(
        countryCode: String = WorldCountryCurrencyRegistry.activeCountryCode
    ): String {
        val sym = WorldCountryCurrencyRegistry.byCountryCode(countryCode).currencySymbol
        return "$sym "
    }

    /**
     * Extracts pure digits as Long from any string (including formatted like "Rp 50.000" or "$50,000").
     */
    fun parseToLong(text: String): Long {
        val clean = text.filter { it.isDigit() }.take(13)
        return (clean.toLongOrNull() ?: 0L).coerceIn(0L, ArusKasViewModel.MAX_MONETARY_AMOUNT)
    }
}

/**
 * VisualTransformation for Jetpack Compose OutlinedTextField.
 * Dynamically formats raw digits using the active country's thousands separator (',' for USD/GBP/EUR/SGD/JPY, '.' for IDR/BRL).
 */
class RupiahThousandsVisualTransformation(
    private val customSeparator: Char? = null
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val clean = originalText.filter { it.isDigit() }
        if (clean.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val num = clean.take(15).toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
        val sep = customSeparator ?: WorldCountryCurrencyRegistry.getActiveProfile().thousandsSeparator
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = sep
            decimalSeparator = if (sep == '.') ',' else '.'
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        val formatted = decimalFormat.format(num)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, clean.length)
                val digitsBefore = clamped
                if (digitsBefore == 0) return 0
                val totalDigits = clean.length
                val sepsTotal = (totalDigits - 1) / 3
                val firstGroupLen = totalDigits - sepsTotal * 3
                val sepsBefore = if (digitsBefore <= firstGroupLen) {
                    0
                } else {
                    1 + (digitsBefore - firstGroupLen - 1) / 3
                }
                return (digitsBefore + sepsBefore).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val transformedSub = formatted.substring(0, clamped)
                val digitsInSub = transformedSub.count { it.isDigit() }
                return digitsInSub.coerceAtMost(clean.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
