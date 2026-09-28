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
 * 1. المصدر الأساسي لتسعير الذهب: GET https://data-asg.goldprice.org/dbXRates/USD
 * 2. المصدر الاحتياطي (بعد 3 إخفاقات متتالية للمصدر الأساسي): GET https://api.gold-api.com/price/XAU
 * 3. مصدر سعر صرف الدولار: GET https://open.er-api.com/v6/latest/USD
 * 4. الثوابت الصارمة والمعايير:
 *    - وحدة الأونصة: 31.1035 جرام
 *    - دولار الصاغة: SD = USD_EGP × admin_sd_factor (الافتراضي 1.0 أو 51.3)
 *    - معامل البيع (kSellFactor): 0.9970 (أو 0.9943 للتوافق)
 *    - التأكد الصارم: شراء > بيع في كل صف
 */
object ExactLivePricingEngine {

    const val OUNCE_TO_GRAMS = 31.1035
    const val SAGHA_USD = 51.3
    const val SELL_FACTOR = 0.9943
    const val DEFAULT_K_SELL_FACTOR = 0.9970

    // معاملات قابلة للتعديل والتحكم
    @Volatile var adminSdFactor: Double = 1.0
    @Volatile var adminBuyFactor: Double = 1.0
    @Volatile var kSellFactor: Double = 0.9970

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val PREFS_NAME = "exact_live_pricing_prefs"
    private const val KEY_USD_RATE = "cached_usd_official_rate"
    private const val KEY_USD_TIMESTAMP = "cached_usd_official_timestamp"

    // تتبع الإخفاقات المتتالية للمصدر الأساسي
    @Volatile
    var primaryFailuresCount: Int = 0

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
     * جلب سعر الأونصة من المصدر الأساسي، أو الاحتياطي بعد 3 إخفاقات متتالية
     */
    suspend fun fetchLiveXau(): Pair<Double, String> = withContext(Dispatchers.IO) {
        // 1. المحاولة مع المصدر الأساسي ما لم يكن قد أخفق 3 مرات متتالية
        if (primaryFailuresCount < 3) {
            try {
                val primaryResult = fetchFromPrimaryGoldPrice()
                primaryFailuresCount = 0 // إعادة ضبط عداد الإخفاق
                return@withContext primaryResult
            } catch (e: Exception) {
                primaryFailuresCount++
                Log.w("ExactLivePricing", "Primary goldprice.org failed (#$primaryFailuresCount): ${e.message}")
            }
        }

        // 2. استخدام المصدر الاحتياطي (بعد 3 إخفاقات متتالية أو عند فشل الأساسي)
        try {
            val fallbackResult = fetchFromFallbackGoldApi()
            return@withContext fallbackResult
        } catch (e: Exception) {
            Log.e("ExactLivePricing", "Fallback gold-api.com also failed: ${e.message}")
            throw e
        }
    }

    private fun fetchFromPrimaryGoldPrice(): Pair<Double, String> {
        val request = Request.Builder()
            .url("https://data-asg.goldprice.org/dbXRates/USD")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Origin", "https://goldprice.org")
            .header("Referer", "https://goldprice.org/")
            .header("Accept", "application/json, text/plain, */*")
            .header("Cache-Control", "no-cache")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP Error: ${response.code}")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Empty response body")
            val json = JSONObject(body)
            val items = json.optJSONArray("items")
            if (items != null && items.length() > 0) {
                val item0 = items.getJSONObject(0)
                val xau = item0.optDouble("xauPrice", 0.0)
                val dateStr = json.optString("date", "")
                if (xau > 0.0) {
                    return Pair(xau, dateStr)
                }
            }
            throw IllegalStateException("Invalid data in primary response")
        }
    }

    private fun fetchFromFallbackGoldApi(): Pair<Double, String> {
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
            return Pair(price, updatedAt)
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
        // SD = officialUsdRate * adminSdFactor
        val effectiveSd = if (adminSdFactor > 0.0 && officialUsdRate > 0.0) officialUsdRate * adminSdFactor else SAGHA_USD
        // raw24 = ( XAU × SD ) ÷ 31.1035
        val raw24 = (xau * effectiveSd) / OUNCE_TO_GRAMS
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

            // sell_k = round( raw_k × SELL_FACTOR )
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

        val sdText = String.format(Locale.US, "%.1f", if (adminSdFactor > 0.0 && officialUsdRate > 0.0) officialUsdRate * adminSdFactor else SAGHA_USD)

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
            officialUsdSell = officialUsdSellText,
            saghaUsdText = sdText
        )
    }
}
