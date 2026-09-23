package com.example.goldfeed

import com.example.model.GoldPriceResponse
import com.example.model.KaratSpread
import com.example.model.PricePair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * 4. GoldPriceCalculator:
 * محرك الحسابات الرياضية المعتمد الخالص لتطبيق "حارس الذهب".
 *
 * المعادلة الأساسية لشراء الذهب:
 * rawBuyPrice = ounceBuyUsd × saghaDollarBuyEgp ÷ 31.1034768 × (karat / 24)
 * buyPrice = floor(rawBuyPrice + buyAdjustmentEgp)
 *
 * معادلة بيع الذهب:
 * rawSellPrice = ounceSellUsd × dollarSellEgp ÷ 31.1034768 × (karat / 24)
 * sellPrice = floor(rawSellPrice + sellAdjustmentEgp)
 *
 * جنيه الذهب (8 جم عيار 21):
 * goldPoundRawBuy = buyPriceForKarat21 × 8
 * goldPoundRawSell = sellPriceForKarat21 × 8
 */
object GoldPriceCalculator {

    const val TROY_OUNCE_GRAMS = 31.1034768

    enum class RoundingMode {
        FLOOR,
        ROUND,
        CEIL;

        fun apply(value: Double): Double {
            return when (this) {
                FLOOR -> floor(value)
                ROUND -> round(value)
                CEIL -> ceil(value)
            }
        }
    }

    data class CalibrationConfig(
        val buyAdjustmentEgp: Double = 0.0,
        val sellAdjustmentEgp: Double = 0.0,
        val buyRounding: RoundingMode = RoundingMode.FLOOR,
        val sellRounding: RoundingMode = RoundingMode.FLOOR,
        val goldPoundRounding: RoundingMode = RoundingMode.FLOOR
    )

    data class RawFeedInputs(
        val ounceBuyUsd: Double,
        val ounceSellUsd: Double,
        val dollarBuyEgp: Double,
        val dollarSellEgp: Double,
        val saghaDollarBuyEgp: Double,
        val sourceUpdatedAt: Long = System.currentTimeMillis(),
        val sourceUpdatedIso: String = "",
        val fetchedAt: Long = System.currentTimeMillis()
    )

    data class CalculatedKaratPrice(
        val karat: Int,
        val rawBuy: Double,
        val buyPrice: Double,
        val rawSell: Double,
        val sellPrice: Double
    ) {
        val spread: Double get() = buyPrice - sellPrice
        fun toPricePair(): PricePair = PricePair(buy = buyPrice, sell = sellPrice)
    }

    data class CalculatedGoldPound(
        val rawBuy: Double,
        val buyPrice: Double,
        val rawSell: Double,
        val sellPrice: Double
    ) {
        val spread: Double get() = buyPrice - sellPrice
        fun toPricePair(): PricePair = PricePair(buy = buyPrice, sell = sellPrice)
    }

    data class CalculatedSnapshot(
        val inputs: RawFeedInputs,
        val karats: Map<Int, CalculatedKaratPrice>,
        val goldPound: CalculatedGoldPound,
        val calibration: CalibrationConfig,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val gram24: CalculatedKaratPrice get() = karats[24] ?: CalculatedKaratPrice(24, 0.0, 0.0, 0.0, 0.0)
        val gram22: CalculatedKaratPrice get() = karats[22] ?: CalculatedKaratPrice(22, 0.0, 0.0, 0.0, 0.0)
        val gram21: CalculatedKaratPrice get() = karats[21] ?: CalculatedKaratPrice(21, 0.0, 0.0, 0.0, 0.0)
        val gram18: CalculatedKaratPrice get() = karats[18] ?: CalculatedKaratPrice(18, 0.0, 0.0, 0.0, 0.0)
        val gram14: CalculatedKaratPrice get() = karats[14] ?: CalculatedKaratPrice(14, 0.0, 0.0, 0.0, 0.0)

        fun toGoldPriceResponse(statusText: String = "live_feed"): GoldPriceResponse {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val updatedDateStr = sdf.format(Date(inputs.sourceUpdatedAt))

            return GoldPriceResponse(
                gram24 = gram24.toPricePair(),
                gram22 = gram22.toPricePair(),
                gram21 = gram21.toPricePair(),
                gram18 = gram18.toPricePair(),
                gram14 = gram14.toPricePair(),
                lastUpdated = updatedDateStr,
                status = statusText,
                source = "GoldBullion Engine (Live Backend Feed)",
                currency = "EGP",
                usdToEgpRate = inputs.dollarBuyEgp,
                usdBuyRate = inputs.dollarBuyEgp,
                usdSellRate = inputs.dollarSellEgp,
                saghaUsdRate = inputs.saghaDollarBuyEgp,
                ouncePrice = inputs.ounceBuyUsd,
                ounceBid = inputs.ounceSellUsd,
                ounceAsk = inputs.ounceBuyUsd,
                goldPoundPrice = goldPound.toPricePair(),
                spread = KaratSpread(
                    k24 = gram24.spread,
                    k22 = gram22.spread,
                    k21 = gram21.spread,
                    k18 = gram18.spread,
                    k14 = gram14.spread
                )
            )
        }
    }

    data class KaratComparisonItem(
        val karat: Int,
        val rawBuy: Double,
        val roundedBuy: Double,
        val refBuy: Double,
        val buyDiff: Double,
        val rawSell: Double,
        val roundedSell: Double,
        val refSell: Double,
        val sellDiff: Double,
        val isWithinTolerance: Boolean
    )

    data class ReferenceComparisonReport(
        val items: List<KaratComparisonItem>,
        val poundRawBuy: Double,
        val poundRoundedBuy: Double,
        val poundRefBuy: Double,
        val poundBuyDiff: Double,
        val poundRawSell: Double,
        val poundRoundedSell: Double,
        val poundRefSell: Double,
        val poundSellDiff: Double,
        val isAllWithinTolerance: Boolean,
        val summary: String
    )

    // الجدول المرجعي لاختبار المطابقة (Gold Bullion Reference)
    val REFERENCE_GOLD_BULLION_PRICES: Map<Int, Pair<Double, Double>> = mapOf(
        24 to Pair(7109.0, 7086.0),
        22 to Pair(6516.0, 6495.0),
        21 to Pair(6220.0, 6200.0),
        18 to Pair(5331.0, 5314.0),
        14 to Pair(4147.0, 4133.0)
    )
    val REFERENCE_GOLD_POUND: Pair<Double, Double> = Pair(49760.0, 49600.0)

    /**
     * حساب جميع العيارات وجنيه الذهب وفق المعادلة الرسمية.
     */
    fun calculate(
        inputs: RawFeedInputs,
        calibration: CalibrationConfig = CalibrationConfig()
    ): CalculatedSnapshot {
        val supportedKarats = listOf(24, 22, 21, 18, 14)
        val karatMap = mutableMapOf<Int, CalculatedKaratPrice>()

        // 1. حساب كل عيار
        for (karat in supportedKarats) {
            // rawBuyPrice = ounceBuyUsd × saghaDollarBuyEgp ÷ 31.1034768 × (karat ÷ 24)
            val rawBuy = if (inputs.saghaDollarBuyEgp > 0.0 && inputs.ounceBuyUsd > 0.0) {
                (inputs.ounceBuyUsd * inputs.saghaDollarBuyEgp / TROY_OUNCE_GRAMS) * (karat.toDouble() / 24.0)
            } else 0.0

            val adjustedBuy = rawBuy + calibration.buyAdjustmentEgp
            val finalBuy = calibration.buyRounding.apply(adjustedBuy)

            // rawSellPrice = ounceSellUsd × dollarSellEgp ÷ 31.1034768 × (karat ÷ 24)
            val rawSell = if (inputs.dollarSellEgp > 0.0 && inputs.ounceSellUsd > 0.0) {
                (inputs.ounceSellUsd * inputs.dollarSellEgp / TROY_OUNCE_GRAMS) * (karat.toDouble() / 24.0)
            } else 0.0

            val adjustedSell = rawSell + calibration.sellAdjustmentEgp
            val finalSell = calibration.sellRounding.apply(adjustedSell)

            karatMap[karat] = CalculatedKaratPrice(
                karat = karat,
                rawBuy = rawBuy,
                buyPrice = finalBuy,
                rawSell = rawSell,
                sellPrice = finalSell
            )
        }

        // 2. حساب جنيه الذهب:
        // goldPoundRawBuy = buyPriceForKarat21 × 8
        // goldPoundRawSell = sellPriceForKarat21 × 8
        val k21 = karatMap[21] ?: CalculatedKaratPrice(21, 0.0, 0.0, 0.0, 0.0)
        val poundRawBuy = k21.buyPrice * 8.0
        val poundRawSell = k21.sellPrice * 8.0

        val poundBuy = calibration.goldPoundRounding.apply(poundRawBuy)
        val poundSell = calibration.goldPoundRounding.apply(poundRawSell)

        val goldPound = CalculatedGoldPound(
            rawBuy = poundRawBuy,
            buyPrice = poundBuy,
            rawSell = poundRawSell,
            sellPrice = poundSell
        )

        return CalculatedSnapshot(
            inputs = inputs,
            karats = karatMap,
            goldPound = goldPound,
            calibration = calibration,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * مقارنة اللقطة المحسوبة بالجدول المرجعي الموثق من Gold Bullion
     */
    fun compareWithReference(snapshot: CalculatedSnapshot): ReferenceComparisonReport {
        val items = mutableListOf<KaratComparisonItem>()
        var allWithinTolerance = true

        for ((karat, refPair) in REFERENCE_GOLD_BULLION_PRICES) {
            val calc = snapshot.karats[karat] ?: CalculatedKaratPrice(karat, 0.0, 0.0, 0.0, 0.0)
            val (refBuy, refSell) = refPair

            val buyDiff = calc.buyPrice - refBuy
            val sellDiff = calc.sellPrice - refSell

            // المطابقة داخل هامش 1 إلى 2 جنيه
            val isWithin = kotlin.math.abs(buyDiff) <= 2.0 && kotlin.math.abs(sellDiff) <= 2.0
            if (!isWithin) {
                allWithinTolerance = false
            }

            items.add(
                KaratComparisonItem(
                    karat = karat,
                    rawBuy = calc.rawBuy,
                    roundedBuy = calc.buyPrice,
                    refBuy = refBuy,
                    buyDiff = buyDiff,
                    rawSell = calc.rawSell,
                    roundedSell = calc.sellPrice,
                    refSell = refSell,
                    sellDiff = sellDiff,
                    isWithinTolerance = isWithin
                )
            )
        }

        val poundRef = REFERENCE_GOLD_POUND
        val poundBuyDiff = snapshot.goldPound.buyPrice - poundRef.first
        val poundSellDiff = snapshot.goldPound.sellPrice - poundRef.second

        val summary = buildString {
            appendLine("=== تقرير مطابقة معادلة Gold Bullion ===")
            for (item in items) {
                appendLine("عيار ${item.karat}:")
                appendLine("  الشراء: خام=${String.format(Locale.US, "%.2f", item.rawBuy)} | مقرب=${item.roundedBuy.toInt()} | مرجعي=${item.refBuy.toInt()} | الفرق=${item.buyDiff}")
                appendLine("  البيع:  خام=${String.format(Locale.US, "%.2f", item.rawSell)} | مقرب=${item.roundedSell.toInt()} | مرجعي=${item.refSell.toInt()} | الفرق=${item.sellDiff}")
                appendLine("  المطابقة داخل هامش 1-2 جنيه: ${if (item.isWithinTolerance) "✅ نعم" else "❌ لا"}")
            }
            appendLine("جنيه الذهب (8 جم عيار 21):")
            appendLine("  الشراء: ${snapshot.goldPound.buyPrice.toInt()} (مرجعي: ${poundRef.first.toInt()}, فرق: $poundBuyDiff)")
            appendLine("  البيع: ${snapshot.goldPound.sellPrice.toInt()} (مرجعي: ${poundRef.second.toInt()}, فرق: $poundSellDiff)")
            appendLine("النتيجة العامة: ${if (allWithinTolerance) "✅ جميع العيارات متطابقة ضمن الهامش المعتمد" else "⚠️ يوجد فرق يحتاج معايرة"}")
        }

        return ReferenceComparisonReport(
            items = items,
            poundRawBuy = snapshot.goldPound.rawBuy,
            poundRoundedBuy = snapshot.goldPound.buyPrice,
            poundRefBuy = poundRef.first,
            poundBuyDiff = poundBuyDiff,
            poundRawSell = snapshot.goldPound.rawSell,
            poundRoundedSell = snapshot.goldPound.sellPrice,
            poundRefSell = poundRef.second,
            poundSellDiff = poundSellDiff,
            isAllWithinTolerance = allWithinTolerance,
            summary = summary
        )
    }
}
