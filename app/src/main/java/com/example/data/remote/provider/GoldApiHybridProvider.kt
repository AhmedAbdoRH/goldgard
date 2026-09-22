package com.example.data.remote.provider

import android.content.Context
import android.util.Log
import com.example.model.GoldPriceResponse
import com.example.util.GoldBullionPricingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * المزود اللحظي الحصري المعتمد لمصدر الذهب العالمي:
 * 1) GET https://api.gold-api.com/price/XAU بدون أي مفتاح أو Header تفويض، مع Cache-Control: no-cache.
 * 2) GET https://open.er-api.com/v6/latest/USD يُجلب مرة كل 24 ساعة ويُحفظ محلياً (للعرض فقط).
 */
class GoldApiHybridProvider(
    private val context: Context,
    private val getSd: () -> Double,
    private val getK: () -> Double,
    private val onOfficialUsdUpdated: (Double) -> Unit = {}
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val fetchMutex = Mutex()

    private var lastSuccessfulResponse: GoldPriceResponse? = null
    private var lastSuccessfulXau: Double = 0.0
    private var cachedOfficialUsd: Double = 52.0
    private var lastOfficialUsdFetchTime: Long = 0L

    companion object {
        private const val XAU_API_URL = "https://api.gold-api.com/price/XAU"
        private const val USD_API_URL = "https://open.er-api.com/v6/latest/USD"
        private const val PREFS_NAME = "gold_hybrid_prefs"
        private const val KEY_OFFICIAL_USD = "official_usd"
        private const val KEY_OFFICIAL_USD_TIME = "official_usd_time"
        private const val KEY_LAST_K = "calibration_k"
        private const val KEY_LAST_CALIBRATION_DATE = "last_calibration_date"
        private const val KEY_SAGHA_DOLLAR = "sagha_dollar"
    }

    init {
        // تحميل القيم المخزنة محلياً
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedOfficialUsd = prefs.getFloat(KEY_OFFICIAL_USD, 52.0f).toDouble()
            lastOfficialUsdFetchTime = prefs.getLong(KEY_OFFICIAL_USD_TIME, 0L)
        } catch (_: Exception) {}
    }

    suspend fun getLivePrice(): Result<GoldPriceResponse> = fetchMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // 1. فحص وتحديث سعر الدولار الرسمي (مرة كل 24 ساعة فقط)
                val now = System.currentTimeMillis()
                if (now - lastOfficialUsdFetchTime > 24 * 3600 * 1000L || cachedOfficialUsd <= 0.0) {
                    fetchOfficialUsdSilently()
                }

                // 2. جلب سعر الأونصة من المصدر العالمي الحصري:
                // GET https://api.gold-api.com/price/XAU
                // ممنوع إضافة أي مفاتيح أو headers باستثناء Cache-Control: no-cache
                val request = Request.Builder()
                    .url(XAU_API_URL)
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                }

                val bodyString = response.body?.string() ?: throw IllegalStateException("Empty body from XAU API")
                val json = JSONObject(bodyString)

                val xauPrice = json.optDouble("price", 0.0)
                if (xauPrice <= 100.0) {
                    throw IllegalStateException("Invalid XAU price received: $xauPrice")
                }

                val updatedAtString = json.optString("updatedAt", "")
                val updatedAtMillis = parseIsoTimestamp(updatedAtString)
                val isMarketClosed = (now - updatedAtMillis) > 2 * 3600 * 1000L

                lastSuccessfulXau = xauPrice

                val currentSd = getSd()
                val currentK = getK()

                // 3. تطبيق معادلة جولد بيليون الوحيدة المسموحة
                val calculated = GoldBullionPricingEngine.calculateAll(
                    xau = xauPrice,
                    sd = currentSd,
                    k = currentK,
                    officialUsd = cachedOfficialUsd
                )

                // تنسيق الوقت باللغة العربية
                val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
                val formattedTime = sdf.format(Date(now))

                val status = if (isMarketClosed) "closed" else "live"

                val finalResponse = GoldBullionPricingEngine.toResponse(
                    calculated = calculated,
                    timestamp = now,
                    status = status,
                    formattedTime = formattedTime
                )

                lastSuccessfulResponse = finalResponse
                Result.success(finalResponse)
            } catch (e: Exception) {
                Log.e("GoldApiHybrid", "Fetch error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    private fun fetchOfficialUsdSilently() {
        try {
            val usdRequest = Request.Builder()
                .url(USD_API_URL)
                .header("Cache-Control", "no-cache")
                .get()
                .build()

            val usdResponse = client.newCall(usdRequest).execute()
            if (usdResponse.isSuccessful) {
                val usdBody = usdResponse.body?.string() ?: return
                val usdJson = JSONObject(usdBody)
                val rates = usdJson.optJSONObject("rates")
                if (rates != null && rates.has("EGP")) {
                    val egpRate = rates.getDouble("EGP")
                    if (egpRate > 10.0) {
                        cachedOfficialUsd = egpRate
                        lastOfficialUsdFetchTime = System.currentTimeMillis()
                        onOfficialUsdUpdated(egpRate)
                        // حفظ في SharedPreferences
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit()
                            .putFloat(KEY_OFFICIAL_USD, egpRate.toFloat())
                            .putLong(KEY_OFFICIAL_USD_TIME, lastOfficialUsdFetchTime)
                            .apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("GoldApiHybrid", "Failed to fetch official USD (kept cached): ${e.message}")
        }
    }

    private fun parseIsoTimestamp(isoString: String?): Long {
        if (isoString.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            Instant.parse(isoString).toEpochMilli()
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(isoString.replace("Z", ""))?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun getLastSuccessfulResponse(): GoldPriceResponse? = lastSuccessfulResponse
    fun getLastKnownXau(): Double = lastSuccessfulXau
    fun getOfficialUsd(): Double = cachedOfficialUsd
}
