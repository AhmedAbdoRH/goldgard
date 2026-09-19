package com.example.util

import android.util.Log
import com.example.model.BullionItem
import com.example.model.GoldPriceResponse
import com.example.model.KaratSpread
import com.example.model.PricePair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

/**
 * محرك تسعير الذهب المعتمد وفق فلسفة جولد بيليون (Gold Bullion Standard Engine).
 * المُدخل الأساسي الوحيد: السعر الحالي لجرام عيار 21 (P21 mid = متوسط السوق).
 * مُدخل العملة المستقل: سعر الدولار الحالي (USD mid = متوسط السوق).
 *
 * نسب النقاء الهندسية:
 * mid_24 = round(P21 * 24/21) = round(P21 * 1.142857)
 * mid_22 = round(P21 * 22/21) = round(P21 * 1.047619)
 * mid_21 = round(P21)
 * mid_18 = round(P21 * 18/21) = round(P21 * 0.857143)
 * mid_14 = round(P21 * 14/21) = round(P21 * 0.666667)
 * mid_12 = round(P21 * 12/21) = round(P21 * 0.571429)
 * mid_9  = round(P21 * 9/21)  = round(P21 * 0.428571)
 *
 * معاملات الفجوة:
 * شراء (جديد / الأعلى) = round(mid_k * 1.0024)
 * بيع (مستعمل / الأدنى) = round(mid_k * 0.9976)
 *
 * شرط التحقق الإلزامي: شراء > بيع دائماً.
 */
object GoldBullionPricingEngine {

    const val BUY_FACTOR = 1.0024
    const val SELL_FACTOR = 0.9976
    const val USD_BUY_FACTOR = 1.001
    const val USD_SELL_FACTOR = 0.999

    data class KaratDerivation(
        val karat: Int,
        val ratio: Double,
        val mid: Double,
        val buy: Double,
        val sell: Double,
        val gap: Double
    )

    data class DiagnosticReport(
        val p21Input: Double,
        val usdMidInput: Double,
        val mid24: Double,
        val mid22: Double,
        val mid21: Double,
        val mid18: Double,
        val mid14: Double,
        val mid12: Double,
        val mid9: Double,
        val derivations: List<KaratDerivation>,
        val usdBuy: Double,
        val usdSell: Double,
        val ounceBuy: Double,
        val ounceSell: Double,
        val goldPoundBuy: Double,
        val goldPoundSell: Double,
        val nisabGoldValue: Double
    )

    /**
     * يحسب الزوج السعري (شراء وبيع) لعيار معين بناء على سعره الوسيط mid.
     * يطبق assertion لمنع أي حالة يكون فيها شراء <= بيع.
     */
    fun calculatePair(mid: Double): PricePair {
        val buy = Math.round(mid * BUY_FACTOR).toDouble()
        val sell = Math.round(mid * SELL_FACTOR).toDouble()

        if (buy <= sell) {
            val errMsg = "BUG: الأعمدة معكوسة أو شراء <= بيع ($buy <= $sell) لـ mid=$mid"
            Log.e("PRICING", errMsg)
            System.err.println(errMsg)
            // تصحيح فوري وقائي
            val correctedBuy = Math.max(buy, sell + 1.0)
            return PricePair(buy = correctedBuy, sell = sell)
        }

        return PricePair(buy = buy, sell = sell)
    }

    /**
     * يشتق الأسعار الوسيطة لجميع العيارات بناء على P21.
     */
    fun deriveMid(p21: Double, karat: Int): Double {
        val ratio = when (karat) {
            24 -> 24.0 / 21.0 // 1.142857
            22 -> 22.0 / 21.0 // 1.047619
            21 -> 1.0
            18 -> 18.0 / 21.0 // 0.857143
            14 -> 14.0 / 21.0 // 0.666667
            12 -> 12.0 / 21.0 // 0.571429
            9  -> 9.0 / 21.0  // 0.428571
            else -> karat.toDouble() / 21.0
        }
        return Math.round(p21 * ratio).toDouble()
    }

    /**
     * يشتق أسعار الدولار شراء وبيع من USDmid.
     */
    fun calculateUsdRates(usdMid: Double): Pair<Double, Double> {
        val buy = Math.round(usdMid * USD_BUY_FACTOR * 10.0) / 10.0
        val sell = Math.round(usdMid * USD_SELL_FACTOR * 10.0) / 10.0
        return Pair(buy, sell)
    }

    /**
     * يبني كائن GoldPriceResponse كاملاً بناء على P21 و USDmid.
     */
    fun buildGoldPriceResponse(
        p21: Double,
        usdMid: Double = 52.25,
        bullionMarginPerGram: Double = 0.0,
        customTime: String? = null,
        infoOuncePriceUsd: Double = 0.0
    ): GoldPriceResponse {
        val safeP21 = if (p21 > 100.0) p21 else 6340.0
        val safeUsdMid = if (usdMid > 10.0) usdMid else 52.25

        val mid24 = deriveMid(safeP21, 24)
        val mid22 = deriveMid(safeP21, 22)
        val mid21 = safeP21
        val mid18 = deriveMid(safeP21, 18)
        val mid14 = deriveMid(safeP21, 14)

        val pair24 = calculatePair(mid24)
        val pair22 = calculatePair(mid22)
        val pair21 = calculatePair(mid21)
        val pair18 = calculatePair(mid18)
        val pair14 = calculatePair(mid14)

        // الجنيه الذهب: شراء = buy_21 * 8، بيع = sell_21 * 8
        val poundPair = PricePair(
            buy = pair21.buy * 8.0,
            sell = pair21.sell * 8.0
        )

        // الأونصة بالجنيه = mid_24 * 31.1035 مع تطبيق فجوة 0.47%
        val ounceMid = mid24 * 31.1035
        val ounceBuy = Math.round(ounceMid * BUY_FACTOR).toDouble()
        val ounceSell = Math.round(ounceMid * SELL_FACTOR).toDouble()

        // الدولار
        val (usdBuy, usdSell) = calculateUsdRates(safeUsdMid)

        val now = System.currentTimeMillis()
        val timeFormatted = customTime ?: run {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale("ar", "EG"))
            sdf.format(Date(now))
        }

        return GoldPriceResponse(
            source = "جولد بيليون (المرجع المعتمد)",
            sourceType = "gold_bullion_mid_engine",
            currency = "EGP",
            ouncePrice = infoOuncePriceUsd,
            ounceAsk = infoOuncePriceUsd,
            ounceBid = infoOuncePriceUsd,
            usdToEgpRate = usdBuy,
            usdBuyRate = usdBuy,
            usdSellRate = usdSell,
            saghaUsdRate = safeUsdMid,
            gram24 = pair24,
            gram22 = pair22,
            gram21 = pair21,
            gram18 = pair18,
            gram14 = pair14,
            goldPoundPrice = poundPair,
            spread = KaratSpread(
                k24 = pair24.buy - pair24.sell,
                k22 = pair22.buy - pair22.sell,
                k21 = pair21.buy - pair21.sell,
                k18 = pair18.buy - pair18.sell,
                k14 = pair14.buy - pair14.sell
            ),
            timestamp = now,
            lastUpdated = timeFormatted,
            lastChecked = timeFormatted,
            change = 0.0,
            changePercent = 0.0,
            status = "live",
            isStale = false,
            error = null,
            isManualCalibration = true,
            bullionMarginPerGram = bullionMarginPerGram
        )
    }

    /**
     * يولد تقرير فحص كامل لقسم "فحص التسعير" في الإعدادات.
     */
    fun generateDiagnosticReport(p21: Double, usdMid: Double): DiagnosticReport {
        val safeP21 = if (p21 > 100.0) p21 else 6340.0
        val safeUsdMid = if (usdMid > 10.0) usdMid else 52.25

        val karats = listOf(24, 22, 21, 18, 14, 12, 9)
        val derivations = karats.map { k ->
            val mid = deriveMid(safeP21, k)
            val pair = calculatePair(mid)
            KaratDerivation(
                karat = k,
                ratio = k.toDouble() / 21.0,
                mid = mid,
                buy = pair.buy,
                sell = pair.sell,
                gap = pair.buy - pair.sell
            )
        }

        val d24 = derivations.first { it.karat == 24 }
        val d21 = derivations.first { it.karat == 21 }
        val (usdBuy, usdSell) = calculateUsdRates(safeUsdMid)

        val ounceMid = d24.mid * 31.1035
        val ounceBuy = Math.round(ounceMid * BUY_FACTOR).toDouble()
        val ounceSell = Math.round(ounceMid * SELL_FACTOR).toDouble()

        val poundBuy = d21.buy * 8.0
        val poundSell = d21.sell * 8.0

        // النصاب الشرعي = 85 جرام * عيار 24 بيع
        val nisabValue = 85.0 * d24.sell

        return DiagnosticReport(
            p21Input = safeP21,
            usdMidInput = safeUsdMid,
            mid24 = d24.mid,
            mid22 = derivations.first { it.karat == 22 }.mid,
            mid21 = safeP21,
            mid18 = derivations.first { it.karat == 18 }.mid,
            mid14 = derivations.first { it.karat == 14 }.mid,
            mid12 = derivations.first { it.karat == 12 }.mid,
            mid9 = derivations.first { it.karat == 9 }.mid,
            derivations = derivations,
            usdBuy = usdBuy,
            usdSell = usdSell,
            ounceBuy = ounceBuy,
            ounceSell = ounceSell,
            goldPoundBuy = poundBuy,
            goldPoundSell = poundSell,
            nisabGoldValue = nisabValue
        )
    }

    /**
     * السبائك عيار 24 بالأوزان القياسية (1، 2.5، 5، 10، 20، 50، 100، 250، 500 جرام).
     * شراء = round(buy_24 * وزن) + هامش
     * بيع = round(sell_24 * وزن)
     */
    fun getStandardBullions(buy24: Double, sell24: Double, marginPerGram: Double = 0.0): List<BullionItem> {
        val weights = listOf(
            1.0 to "سبيكة 1 جرام",
            2.5 to "سبيكة 2.5 جرام",
            5.0 to "سبيكة 5 جرام",
            10.0 to "سبيكة 10 جرام",
            20.0 to "سبيكة 20 جرام",
            50.0 to "سبيكة 50 جرام",
            100.0 to "سبيكة 100 جرام",
            250.0 to "سبيكة 250 جرام",
            500.0 to "سبيكة 500 جرام"
        )
        return weights.map { (w, name) ->
            val b = Math.round(buy24 * w).toDouble() + (marginPerGram * w)
            val s = Math.round(sell24 * w).toDouble()
            BullionItem(
                weightGrams = w,
                label = name,
                buyPrice = b,
                sellPrice = s
            )
        }
    }
}
