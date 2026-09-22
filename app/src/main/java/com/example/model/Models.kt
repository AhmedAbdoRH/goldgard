package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class PriceDirection {
    UP, DOWN, STABLE
}

enum class Country(
    val code: String,
    val nameAr: String,
    val flag: String,
    val currencySymbol: String,
    val currencyNameAr: String,
    val currencyCode: String,
    val isaghaEndpoint: String,
    val defaultBenchmark21Buy: Double,
    val defaultBenchmark21Sell: Double,
    val defaultStampFee: Double,
    val decimalPlaces: Int
) {
    EGYPT(
        code = "EG",
        nameAr = "مصر",
        flag = "🇪🇬",
        currencySymbol = "ج.م",
        currencyNameAr = "جنيه مصري",
        currencyCode = "EGP",
        isaghaEndpoint = "https://market.isagha.com/prices",
        defaultBenchmark21Buy = 6325.0,
        defaultBenchmark21Sell = 6275.0,
        defaultStampFee = 10.0,
        decimalPlaces = 0
    );

    fun formatPrice(amount: Double): String {
        if (decimalPlaces == 0) {
            return DecimalFormat("#.###", DecimalFormatSymbols(Locale.US)).format(Math.round(amount)).replace('.', ',')
        }
        val pattern = when (decimalPlaces) {
            2 -> "#,##0.00"
            3 -> "#,##0.000"
            else -> "#,##0.##"
        }
        return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(amount)
    }

    fun formatWithCurrency(amount: Double): String {
        return "${formatPrice(amount)} $currencySymbol"
    }

    companion object {
        fun fromCode(code: String?): Country {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: EGYPT
        }
    }
}
