package com.example.util

import android.util.Log
import com.example.model.GoldPriceResponse
import com.example.model.KaratSpread
import com.example.model.PricePair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

/**
 * محرك تسعير جولد بيليون الهجين المعتمد (Gold Bullion Hybrid Engine).
 *
 * 1. المعادلة الوحيدة المسموحة:
 *    raw = XAU * SD / 31.1035 * (العيار / 24.0)
 *    سعر الشراء = round(raw * K * 1.0019)
 *    سعر البيع = round(raw * K * 0.9972)
 *
 * 2. المعاملات الثابتة الصارمة:
 *    OUNCE_WEIGHT_GRAMS = 31.1035
 *    BUY_FACTOR = 1.0019
 *    SELL_FACTOR = 0.9972
 *    DEFAULT_K = 0.9996
 *    DEFAULT_SD = 51.7
 *
 * 3. المعايرة الذاتية لـ K:
 *    K = entered_price / (XAU * SD / 31.1035 * 0.875 * 1.0019)
 *
 * 4. جنيه الذهب:
 *    شراء = شراء_21 * 8
 *    بيع = بيع_21 * 8
 */
object GoldBullionPricingEngine {

    const val OUNCE_WEIGHT_GRAMS = 31.1034768
    const val BUY_FACTOR = 1.0019
    const val SELL_FACTOR = 0.9972
    const val DEFAULT_K = 0.9996
    const val DEFAULT_SD = 51.60
    const val DEFAULT_USD_BUY = 51.55
    const val DEFAULT_USD_SELL = 51.45
    const val DEFAULT_OUNCE_ASK = 4285.46
    const val DEFAULT_OUNCE_BID = 4284.96

    data class CalculatedPrices(
        val xau: Double,
        val sd: Double,
        val k: Double,
        val officialUsd: Double,
        val gram24: PricePair,
        val gram22: PricePair,
        val gram21: PricePair,
        val gram18: PricePair,
        val gram14: PricePair,
        val goldPound: PricePair,
        val usdBuy: Double,
        val usdSell: Double,
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    /**
     * يحسب الخام لأي عيار:
     * raw = XAU * SD / 31.1035 * (karat / 24.0)
     */
    fun calculateRaw(xau: Double, sd: Double, karat: Int): Double {
        if (xau <= 0.0 || sd <= 0.0) return 0.0
        return (xau * sd / OUNCE_WEIGHT_GRAMS) * (karat.toDouble() / 24.0)
    }

    /**
     * يحسب سعر الشراء والبيع لأي عيار وفق محرك جولد بيليون الكلاسيكي:
     * سعر الشراء = round( raw * K * 1.0019 )
     * سعر البيع = round( raw * K * 0.9972 )
     */
    fun calculateKaratPair(raw: Double, k: Double): PricePair {
        val buy = Math.round(raw * k * BUY_FACTOR).toDouble()
        val sell = Math.round(raw * k * SELL_FACTOR).toDouble()

        // قاعدة الصرامة: شراء دايمًا أكبر من بيع
        if (buy <= sell) {
            val errMsg = "BUG: الأعمدة معكوسة"
            Log.e("GoldApp", errMsg)
            return PricePair(buy = (sell + 1.0), sell = sell)
        }

        return PricePair(buy = buy, sell = sell)
    }

    /**
     * المعايرة الذاتية لـ K بناء على سعر عيار 21 المُدخل من جولد بيليون:
     * K = entered_price / (raw21 * 1.0019)
     */
    fun calibrateK(enteredP21Buy: Double, currentXau: Double, currentSd: Double): Double {
        if (enteredP21Buy <= 0.0 || currentXau <= 0.0 || currentSd <= 0.0) {
            return DEFAULT_K
        }
        val raw21 = (currentXau * currentSd / OUNCE_WEIGHT_GRAMS) * (21.0 / 24.0)
        if (raw21 <= 0.0) return DEFAULT_K
        return enteredP21Buy / (raw21 * BUY_FACTOR)
    }

    /**
     * حساب جميع العيارات وجنيه الذهب وسعر الدولار للعرض فقط.
     */
    fun calculateAll(
        xau: Double,
        sd: Double = DEFAULT_SD,
        k: Double = DEFAULT_K,
        officialUsd: Double = 52.0
    ): CalculatedPrices {
        val raw24 = calculateRaw(xau, sd, 24)
        val raw22 = calculateRaw(xau, sd, 22)
        val raw21 = calculateRaw(xau, sd, 21)
        val raw18 = calculateRaw(xau, sd, 18)
        val raw14 = calculateRaw(xau, sd, 14)

        val pair24 = calculateKaratPair(raw24, k)
        val pair22 = calculateKaratPair(raw22, k)
        val pair21 = calculateKaratPair(raw21, k)
        val pair18 = calculateKaratPair(raw18, k)
        val pair14 = calculateKaratPair(raw14, k)

        // فحص الصرامة: شراء دائمًا أكبر من بيع في كل العيارات
        val hasInversion = pair24.buy <= pair24.sell ||
                pair22.buy <= pair22.sell ||
                pair21.buy <= pair21.sell ||
                pair18.buy <= pair18.sell ||
                pair14.buy <= pair14.sell

        if (hasInversion) {
            Log.e("GoldApp", "BUG: الأعمدة معكوسة")
        }

        // جنيه الذهب: شراء = شراء_21 * 8، بيع = بيع_21 * 8
        val poundBuy = (pair21.buy.toLong() * 8).toDouble()
        val poundSell = (pair21.sell.toLong() * 8).toDouble()
        val poundPair = PricePair(buy = poundBuy, sell = poundSell)

        // الدولار: سطر معلومات فقط
        // لا يُستخدم في أي حساب إطلاقاً
        val usdBuy = if (officialUsd >= 50.0 && officialUsd <= 53.0) DEFAULT_USD_BUY else Math.round(officialUsd * 1.001 * 100.0) / 100.0
        val usdSell = if (officialUsd >= 50.0 && officialUsd <= 53.0) DEFAULT_USD_SELL else Math.round(officialUsd * 0.999 * 100.0) / 100.0

        return CalculatedPrices(
            xau = xau,
            sd = sd,
            k = k,
            officialUsd = officialUsd,
            gram24 = pair24,
            gram22 = pair22,
            gram21 = pair21,
            gram18 = pair18,
            gram14 = pair14,
            goldPound = poundPair,
            usdBuy = usdBuy,
            usdSell = usdSell,
            isValid = !hasInversion
        )
    }

    /**
     * تحويل CalculatedPrices إلى GoldPriceResponse
     */
    fun toResponse(
        calculated: CalculatedPrices,
        timestamp: Long = System.currentTimeMillis(),
        status: String = "live",
        formattedTime: String? = null
    ): GoldPriceResponse {
        val timeString = formattedTime ?: run {
            val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
            sdf.format(Date(timestamp))
        }

        return GoldPriceResponse(
            source = "جولد بيليون (معايرة ذكية)",
            sourceType = "hybrid_gold_bullion",
            currency = "EGP",
            ouncePrice = calculated.xau,
            ounceAsk = if (calculated.xau > 0) calculated.xau else DEFAULT_OUNCE_ASK,
            ounceBid = if (calculated.xau > 0) (calculated.xau - 0.5) else DEFAULT_OUNCE_BID,
            usdToEgpRate = calculated.officialUsd,
            usdBuyRate = calculated.usdBuy,
            usdSellRate = calculated.usdSell,
            saghaUsdRate = calculated.sd,
            gram24 = calculated.gram24,
            gram22 = calculated.gram22,
            gram21 = calculated.gram21,
            gram18 = calculated.gram18,
            gram14 = calculated.gram14,
            goldPoundPrice = calculated.goldPound,
            spread = KaratSpread(
                k24 = calculated.gram24.buy - calculated.gram24.sell,
                k22 = calculated.gram22.buy - calculated.gram22.sell,
                k21 = calculated.gram21.buy - calculated.gram21.sell,
                k18 = calculated.gram18.buy - calculated.gram18.sell,
                k14 = calculated.gram14.buy - calculated.gram14.sell
            ),
            timestamp = timestamp,
            lastUpdated = timeString,
            lastChecked = timeString,
            status = status,
            isStale = (status == "stale" || status == "closed"),
            isManualCalibration = (calculated.k != DEFAULT_K)
        )
    }

    fun buildGoldPriceResponse(
        p21: Double,
        usdMid: Double = DEFAULT_SD,
        bullionMarginPerGram: Double = 0.0,
        infoOuncePriceUsd: Double = 0.0
    ): GoldPriceResponse {
        val safeXau = if (infoOuncePriceUsd > 100.0) infoOuncePriceUsd else DEFAULT_OUNCE_ASK
        val safeSd = if (usdMid > 10.0) usdMid else DEFAULT_SD
        val k = calibrateK(p21, safeXau, safeSd)
        val calculated = calculateAll(xau = safeXau, sd = safeSd, k = k, officialUsd = DEFAULT_USD_BUY)
        return toResponse(calculated)
    }

    /**
     * حساب زكاة الذهب حسب القاعدة الشرعية المطلوبة:
     * - القيمة = الوزن * سعر بيع جرام العيار المختار اللحظي
     * - النصاب = 85 * سعر بيع جرام عيار 24 اللحظي
     * - إذا القيمة >= النصاب: الزكاة الواجبة = round(القيمة * 0.025)
     */
    data class ZakatOutput(
        val weight: Double,
        val karat: Int,
        val chosenKaratSellPrice: Double,
        val sell24Price: Double,
        val totalValue: Double,
        val nisabValue: Double,
        val isZakatDue: Boolean,
        val zakatAmount: Long
    )

    fun calculateZakat(
        weight: Double,
        karat: Int,
        sell24Price: Double,
        chosenKaratSellPrice: Double
    ): ZakatOutput {
        val totalValue = weight * chosenKaratSellPrice
        val nisabValue = 85.0 * sell24Price
        val isDue = totalValue >= nisabValue && nisabValue > 0
        val zakatAmount = if (isDue) Math.round(totalValue * 0.025) else 0L

        return ZakatOutput(
            weight = weight,
            karat = karat,
            chosenKaratSellPrice = chosenKaratSellPrice,
            sell24Price = sell24Price,
            totalValue = totalValue,
            nisabValue = nisabValue,
            isZakatDue = isDue,
            zakatAmount = zakatAmount
        )
    }
}
