package com.example.data.remote.provider

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * محرك التسعير الرياضي اللحظي المعتمد:
 * 1. مصدر وحيد لتسعير الذهب: GET https://api.gold-api.com/price/XAU
 * 2. مصدر وحيد لسعر صرف الدولار (للعرض فقط): GET https://open.er-api.com/v6/latest/USD
 * 3. الثوابت الصارمة:
 *    - وحدة الأونصة: 31.1035 جرام
 *    - دولار الصاغة: 51.3
 *    - معامل البيع: 0.9943
 *    - تقريب الشراء: لأقرب 10 جنيه صحيح بدون كسور (round(raw_k / 10) * 10)
 *    - تقريب البيع: round(raw_k * 0.9943)
 */
object ExactLivePricingEngine {

    const val OUNCE_TO_GRAMS = 31.1035
    const val SAGHA_USD = 51.3
    const val SELL_FACTOR = 0.9943

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val PREFS_NAME = "exact_live_pricing_prefs"
    private const val KEY_USD_RATE = "cached_usd_official_rate"
    private const val KEY_USD_TIMESTAMP = "cached_usd_official_timestamp"

    data class CalculatedPrices(
        val xauUsd: Double,
        val updatedAtIso: String,
        val lastUpdatedTimeDisplay: String,
        // عيارات الذهب: 24, 22, 21, 18, 14 -> Pair(buy, sell)
        val karatPrices: Map<Int, Pair<Long, Long>>,
        // الجنيه والأونصة
        val goldPoundBuy: Long,
        val goldPoundSell: Long,
        val ounceEgpBuy: Long,
        val ounceEgpSell: Long,
        val ounceUsdRound: Long,
        // أسعار الدولار
        val officialUsdBuy: String,
        val officialUsdSell: String,
        val saghaUsdText: String = "51.3"
    )

    /**
     * جلب سعر الأونصة من المصدر المعتمد حصراً بدون أي كاش
     */
    suspend fun fetchLiveXau(): Pair<Double, String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.gold-api.com/price/XAU")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("Accept", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP Error: ${response.code}")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Empty response body")
            val json = JSONObject(body)
            val price = json.getDouble("price")
            val updatedAt = json.optString("updatedAt", "")
            Pair(price, updatedAt)
        }
    }

    /**
     * جلب سعر صرف الدولار الرسمي للعرض فقط وتخزينه لمدة 24 ساعة
     */
    suspend fun getOrFetchOfficialUsdRate(context: Context): Double = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFetch = prefs.getLong(KEY_USD_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        val cachedRate = prefs.getFloat(KEY_USD_RATE, 0f).toDouble()

        // لو موجود ومضى عليه أقل من 24 ساعة، استخدمه
        if (cachedRate > 0.0 && (now - lastFetch) < 24 * 3600 * 1000L) {
            return@withContext cachedRate
        }

        try {
            val request = Request.Builder()
                .url("https://open.er-api.com/v6/latest/USD")
                .header("Cache-Control", "no-cache")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val rates = json.optJSONObject("rates")
                        val egp = rates?.optDouble("EGP", 0.0) ?: 0.0
                        if (egp > 0.0) {
                            prefs.edit()
                                .putFloat(KEY_USD_RATE, egp.toFloat())
                                .putLong(KEY_USD_TIMESTAMP, now)
                                .apply()
                            return@withContext egp
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ExactLivePricing", "Failed to refresh official USD: ${e.message}")
        }

        return@withContext if (cachedRate > 0.0) cachedRate else 51.9
    }

    /**
     * تطبيق المعادلة الرياضية الدقيقة المستخرجة رياضياً
     */
    fun computePricing(xau: Double, updatedAtIso: String, officialUsdRate: Double): CalculatedPrices {
        // raw24 = ( XAU × SD ) ÷ 31.1035
        val raw24 = (xau * SAGHA_USD) / OUNCE_TO_GRAMS
        return computePricingFromRaw24(raw24, xau, updatedAtIso, officialUsdRate)
    }

    fun computePricingFromRaw24(
        raw24: Double,
        xau: Double,
        updatedAtIso: String,
        officialUsdRate: Double
    ): CalculatedPrices {
        val karats = listOf(24, 22, 21, 18, 14)
        val pricesMap = mutableMapOf<Int, Pair<Long, Long>>()

        for (k in karats) {
            // raw_k = raw24 × ( k ÷ 24 )
            val rawK = raw24 * (k.toDouble() / 24.0)

            // buy_k = round( raw_k ÷ 10 ) × 10
            val buyK = Math.round(rawK / 10.0) * 10L

            // sell_k = round( raw_k × 0.9943 )
            val sellK = Math.round(rawK * SELL_FACTOR)

            // اشتراط جوهري: شراء > بيع في كل صف. إذا انكسر الشرط اطبع في الكونسول: console.error("BUG: buy <= sell") وامنع عرض البطاقة.
            if (buyK <= sellK) {
                Log.e("LivePricingEngine", "BUG: buy <= sell for karat $k (buy: $buyK, sell: $sellK)")
            } else {
                pricesMap[k] = Pair(buyK, sellK)
            }
        }

        // عيار 21
        val buy21 = pricesMap[21]?.first ?: 0L
        val sell21 = pricesMap[21]?.second ?: 0L

        // عيار 24
        val buy24 = pricesMap[24]?.first ?: 0L
        val sell24 = pricesMap[24]?.second ?: 0L

        // جنيه الذهب: شراء = buy_21 × 8، بيع = sell_21 × 8
        val poundBuy = buy21 * 8L
        val poundSell = sell21 * 8L

        // أونصة بالجنيه: شراء = buy_24 × 31.1035، بيع = sell_24 × 31.1035
        val ounceEgpBuy = Math.round(buy24 * OUNCE_TO_GRAMS)
        val ounceEgpSell = Math.round(sell24 * OUNCE_TO_GRAMS)

        // أونصة بالدولار: XAU اللحظي (مقرب لأقرب 1 دولار صحيح)
        val ounceUsdRound = Math.round(xau)

        // وقت التحديث للعرض
        val timeDisplay = SimpleDateFormat("HH:mm:ss", Locale("ar")).format(Date())

        // تنسيق الدولار الرسمي
        val officialUsdBuyText = Math.round(officialUsdRate).toString()
        val officialUsdSellText = Math.round(officialUsdRate).toString()

        return CalculatedPrices(
            xauUsd = xau,
            updatedAtIso = updatedAtIso,
            lastUpdatedTimeDisplay = timeDisplay,
            karatPrices = pricesMap,
            goldPoundBuy = poundBuy,
            goldPoundSell = poundSell,
            ounceEgpBuy = ounceEgpBuy,
            ounceEgpSell = ounceEgpSell,
            ounceUsdRound = ounceUsdRound,
            officialUsdBuy = officialUsdBuyText,
            officialUsdSell = officialUsdSellText
        )
    }
}
