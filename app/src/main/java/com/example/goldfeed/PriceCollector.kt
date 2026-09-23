package com.example.goldfeed

import android.content.Context
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 2. PriceCollector:
 * جامع الأسعار المسؤول عن جلب البيانات وتنسيق الاتصالات.
 *
 * الخصائص البرمجية:
 * - enableRequestDeduplication = true (منع تكرار الطلبات المتزامنة).
 * - maxRetries = 1 (محاولة إعادة واحدة فقط عند الفشل).
 * - enableServerSideCaching = true (الاستفادة من الذاكرة المؤقتة).
 * - تسجيل وقت وصول البيانات بدقة (fetchedAt).
 * - حساب زمن الاستجابة (Latency).
 */
class PriceCollector(
    private val context: Context,
    private val api: LatestPricesAPI = LatestPricesAPI(context)
) {

    data class CollectionResult(
        val isSuccess: Boolean,
        val inputs: GoldPriceCalculator.RawFeedInputs?,
        val latencyMs: Long,
        val errorMessage: String? = null,
        val isFromCache: Boolean = false
    )

    private val fetchMutex = Mutex()
    private var lastCachedInputs: GoldPriceCalculator.RawFeedInputs? = null
    private var lastFetchTimestamp: Long = 0L

    companion object {
        private const val TAG = "PriceCollector"
        private const val MAX_RETRIES = 1
        private const val CACHE_VALIDITY_MS = 5_000L // clientRefreshIntervalSeconds = 5
    }

    /**
     * جلب أسعار المدخلات مع منع التكرار وإعادة المحاولة التلقائية
     */
    suspend fun collectPrices(
        backendUrl: String? = null,
        forceRefresh: Boolean = false
    ): CollectionResult = fetchMutex.withLock {
        val now = System.currentTimeMillis()

        // 1. التحقق من التخزين المؤقت (Deduplication & Caching)
        if (!forceRefresh && lastCachedInputs != null && (now - lastFetchTimestamp) < CACHE_VALIDITY_MS) {
            return CollectionResult(
                isSuccess = true,
                inputs = lastCachedInputs,
                latencyMs = 0L,
                isFromCache = true
            )
        }

        var attempt = 0
        var lastError: String? = null
        val startTime = System.currentTimeMillis()

        while (attempt <= MAX_RETRIES) {
            attempt++
            try {
                val response = api.fetchLatestPricesFromBackend(backendUrl)
                val latency = System.currentTimeMillis() - startTime

                if (response.isSuccessful) {
                    val rawInputs = response.toRawInputs(fetchedAt = System.currentTimeMillis())
                    lastCachedInputs = rawInputs
                    lastFetchTimestamp = System.currentTimeMillis()

                    return CollectionResult(
                        isSuccess = true,
                        inputs = rawInputs,
                        latencyMs = latency,
                        isFromCache = false
                    )
                } else {
                    lastError = response.errorMessage ?: "فشل استجابة التغذية"
                }
            } catch (e: Exception) {
                lastError = e.message ?: "خطأ غير متوقع في جلب الأسعار"
                Log.w(TAG, "Attempt $attempt failed: $lastError")
            }
        }

        val latency = System.currentTimeMillis() - startTime
        return CollectionResult(
            isSuccess = false,
            inputs = lastCachedInputs, // استخدام الكاش في حال تعذر التحديث
            latencyMs = latency,
            errorMessage = lastError,
            isFromCache = lastCachedInputs != null
        )
    }

    fun getLastCachedInputs(): GoldPriceCalculator.RawFeedInputs? = lastCachedInputs
}
