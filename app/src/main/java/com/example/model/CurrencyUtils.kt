package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val inrLocale = Locale("en", "IN")

    fun formatINR(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(inrLocale)
            if (format is DecimalFormat) {
                val symbols = format.decimalFormatSymbols ?: DecimalFormatSymbols(inrLocale)
                symbols.currencySymbol = "₹"
                format.decimalFormatSymbols = symbols
            }
            if (amount % 1.0 == 0.0) {
                format.maximumFractionDigits = 0
            } else {
                format.maximumFractionDigits = 2
            }
            format.format(amount)
        } catch (_: Exception) {
            val rounded = if (amount % 1.0 == 0.0) {
                String.format(Locale.US, "%.0f", amount)
            } else {
                String.format(Locale.US, "%.2f", amount)
            }
            "₹$rounded"
        }
    }
}
