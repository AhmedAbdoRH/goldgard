package com.example.util

import java.util.Locale

/**
 * Unified Gold Price Formatter enforcing the exact user specification:
 * 1. All gold prices (Grams 24, 22, 21, 18, 14, Ounce, Gold Pound, Bullions): strictly rounded to nearest integer with NO decimals.
 * 2. Currency rates (USD): 2 decimal places.
 */
object GoldPriceFormatter {

    /**
     * Formats gram prices (24, 22, 21, 18, 14) strictly as rounded whole integer.
     * Example: 6300 or 6330 or 6219 (no decimals like .0 or .5 and no grouping separator confusing Arabic numerals).
     */
    fun formatGram(price: Double): String {
        if (price <= 0.0) return "0"
        return Math.round(price).toString()
    }

    /**
     * Formats whole unit prices (Ounce in EGP, Gold Pound, Bullions/Ingots) with no fractions, rounded to nearest integer.
     * Example: 221810 or 50400 or 50640
     */
    fun formatWhole(price: Double): String {
        if (price <= 0.0) return "0"
        return Math.round(price).toString()
    }

    /**
     * Formats USD exchange rate or ounce USD with 2 decimal places.
     * Example: 51.70 or 52.30
     */
    fun formatUsd(amount: Double): String {
        if (amount <= 0.0) return "0.00"
        return String.format(Locale.US, "%,.2f", amount)
    }

    /**
     * Formats USD rate showing buy and sell (matching Golden Bullion: 52.30 شراء / 52.20 بيع).
     */
    fun formatUsdBuySell(buy: Double, sell: Double): String {
        val b = if (buy > 0.0) buy else 52.30
        val s = if (sell > 0.0) sell else 52.20
        return "${formatUsd(b)} شراء / ${formatUsd(s)} بيع"
    }
}
