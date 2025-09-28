package com.freshly.app.utils

import java.text.NumberFormat
import java.util.Locale

object PriceUtil {
    private val formatter: NumberFormat by lazy {
        // Use Indian locale for thousand separators 12,500
        NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
    }

    /**
     * Returns price formatted as: "Rs. 12,500" or with decimals when needed: "Rs. 12,500.50"
     */
    fun formatPrice(amount: Double?): String {
        val value = amount ?: 0.0
        val formatted = formatter.format(value)
        return "Rs. $formatted"
    }
}
