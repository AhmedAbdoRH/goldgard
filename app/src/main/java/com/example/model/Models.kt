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
            return DecimalFormat("#.###", DecimalFormatSymbols(Locale.US)).format(Math.round(amount))
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

data class GoldPrice(
    val karat: Int,
    val buyPrice: Double,   // سعر الشراء (المستهلك يشتري من المحل)
    val sellPrice: Double,  // سعر البيع (المستهلك يبيع للمحل)
    val change24h: Double = 0.0
)

data class GoldSettings(
    val selectedCountry: Country = Country.EGYPT,
    val defaultBuyMakingPercent: Double = 7.0,
    val defaultSellDeductionPercent: Double = 2.0,
    val defaultStampFeePerGram: Double = 10.0,
    val defaultDamagedPercent: Double = 0.0,
    val defaultSaghaUsd: Double = 51.7
)

data class BuyCalculationResult(
    val rawGoldPrice: Double = 0.0,
    val makingTotal: Double = 0.0,
    val makingPerGram: Double = 0.0,
    val stampTotal: Double = 0.0,
    val extraFees: Double = 0.0,
    val totalFairPrice: Double = 0.0,
    val shopPrice: Double = 0.0,
    val difference: Double = 0.0,
    val differencePercent: Double = 0.0,
    val comparisonStatus: ComparisonStatus = ComparisonStatus.NONE
)

data class SellCalculationResult(
    val rawGoldPrice: Double = 0.0,
    val deductionPercent: Double = 0.0,
    val deductionPerGram: Double = 0.0,
    val damagedPercent: Double = 0.0,
    val damagedValue: Double = 0.0,
    val netGramPrice: Double = 0.0,
    val weight: Double = 0.0,
    val netWeight: Double = 0.0,
    val stonesWeight: Double = 0.0,
    val deductedStones: Boolean = true,
    val expectedTotalPayout: Double = 0.0,
    val shopOffer: Double = 0.0,
    val difference: Double = 0.0,
    val differencePercent: Double = 0.0,
    val comparisonStatus: ComparisonStatus = ComparisonStatus.NONE
)
