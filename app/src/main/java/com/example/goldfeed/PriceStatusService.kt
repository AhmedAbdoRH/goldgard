package com.example.goldfeed

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 8. PriceStatusService:
 * خدمة رصد حالة التغذية وتتبع سلامة وسرعة الاتصال وجودة الأسعار.
 *
 * المهام:
 * - مراقبة حالة الاتصال والتغذية اللحظية (Live vs Stale vs Cached vs Error).
 * - رصد زمن الوصول وفترة حداثة البيانات (Latency & Age).
 * - تسجيل تقارير المطابقة والتشخيص ومقارنة الأسعار مع المرجع المعتمد.
 */
class PriceStatusService {

    enum class FeedStatus {
        IDLE,
        CONNECTING,
        LIVE,
        CACHED,
        STALE_DATA_WARNING,
        ERROR
    }

    data class FeedStatusInfo(
        val status: FeedStatus = FeedStatus.IDLE,
        val statusTextArabic: String = "جاهز",
        val lastSuccessTimeMs: Long = 0L,
        val formattedLastSuccess: String = "",
        val sourceAgeSeconds: Long = 0L,
        val latencyMs: Long = 0L,
        val isStale: Boolean = false,
        val lastErrorMessage: String? = null,
        val latestComparisonReport: GoldPriceCalculator.ReferenceComparisonReport? = null
    )

    private val _statusInfo = MutableStateFlow(FeedStatusInfo())
    val statusInfo: StateFlow<FeedStatusInfo> = _statusInfo.asStateFlow()

    fun updateConnecting() {
        _statusInfo.value = _statusInfo.value.copy(
            status = FeedStatus.CONNECTING,
            statusTextArabic = "جارٍ الاتصال بالتغذية المعتمدة..."
        )
    }

    fun updateSuccess(
        sourceUpdatedAt: Long,
        latencyMs: Long,
        isFromCache: Boolean,
        snapshot: GoldPriceCalculator.CalculatedSnapshot
    ) {
        val now = System.currentTimeMillis()
        val ageSeconds = if (sourceUpdatedAt > 0L) (now - sourceUpdatedAt) / 1000L else 0L
        val isStale = ageSeconds > 90L // maxAllowedDataAgeSeconds = 90

        val status = when {
            isStale -> FeedStatus.STALE_DATA_WARNING
            isFromCache -> FeedStatus.CACHED
            else -> FeedStatus.LIVE
        }

        val statusText = when (status) {
            FeedStatus.LIVE -> "مباشر - تحديث لحظي"
            FeedStatus.CACHED -> "مُحدّث من الذاكرة المؤقتة"
            FeedStatus.STALE_DATA_WARNING -> "تنبيه: البيانات تجاوزت 90 ثانية"
            else -> "متصل"
        }

        val sdf = SimpleDateFormat("HH:mm:ss - yyyy/MM/dd", Locale.US)
        val formattedDate = sdf.format(Date(now))

        val report = GoldPriceCalculator.compareWithReference(snapshot)

        _statusInfo.value = FeedStatusInfo(
            status = status,
            statusTextArabic = statusText,
            lastSuccessTimeMs = now,
            formattedLastSuccess = formattedDate,
            sourceAgeSeconds = ageSeconds,
            latencyMs = latencyMs,
            isStale = isStale,
            lastErrorMessage = null,
            latestComparisonReport = report
        )
    }

    fun updateError(message: String, hasCachedData: Boolean) {
        _statusInfo.value = _statusInfo.value.copy(
            status = if (hasCachedData) FeedStatus.CACHED else FeedStatus.ERROR,
            statusTextArabic = if (hasCachedData) "تعذر التحديث (استخدام السعر السابق)" else "خطأ في جلب الأسعار",
            lastErrorMessage = message
        )
    }
}
