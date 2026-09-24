package com.example.goldfeed

import android.content.Context
import com.example.model.GoldPriceResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 1. GoldPriceFeed:
 * المغذي الموحد والواجهة الرئيسية (Facade) لنظام أسعار الذهب المعتمد في "حارس الذهب".
 *
 * يدمج بدقة واكتمال:
 * 1. GoldPriceFeed (الواجهة الموحدة)
 * 2. PriceCollector (جامع الأسعار)
 * 3. PriceValidator (مدقق صحة البيانات)
 * 4. GoldPriceCalculator (محرك الحساب الرياضي)
 * 5. GoldPriceRepository (مستودع البيانات وRoom DB)
 * 6. ScheduledPriceJob (المهمة المجدولة كل 60 ثانية)
 * 7. LatestPricesAPI (واجهة الاتصال بالباك إند)
 * 8. PriceStatusService (خدمة تتبع الحالة والتشخيص)
 */
class GoldPriceFeed(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    val api = LatestPricesAPI(context)
    val collector = PriceCollector(context, api)
    val statusService = PriceStatusService()
    val repository = GoldPriceRepository(context)
    val scheduledJob = ScheduledPriceJob(
        context = context,
        collector = collector,
        repository = repository,
        statusService = statusService
    )

    // تحويل اللقطة المحسوبة مباشرة إلى GoldPriceResponse متوافق مع كافة شاشات التطبيق
    val liveResponse: StateFlow<GoldPriceResponse> = repository.latestSnapshot
        .map { snapshot ->
            if (snapshot != null) {
                snapshot.toGoldPriceResponse()
            } else {
                // حالة أولية لحين اكتمال أول دورة
                val defaultInputs = GoldPriceCalculator.RawFeedInputs(
                    ounceBuyUsd = LatestPricesAPI.BENCHMARK_OUNCE_BUY,
                    ounceSellUsd = LatestPricesAPI.BENCHMARK_OUNCE_SELL,
                    dollarBuyEgp = LatestPricesAPI.BENCHMARK_DOLLAR_BUY,
                    dollarSellEgp = LatestPricesAPI.BENCHMARK_DOLLAR_SELL,
                    saghaDollarBuyEgp = LatestPricesAPI.BENCHMARK_SAGHA_DOLLAR
                )
                GoldPriceCalculator.calculate(defaultInputs).toGoldPriceResponse()
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = run {
                val initialInputs = GoldPriceCalculator.RawFeedInputs(
                    ounceBuyUsd = LatestPricesAPI.BENCHMARK_OUNCE_BUY,
                    ounceSellUsd = LatestPricesAPI.BENCHMARK_OUNCE_SELL,
                    dollarBuyEgp = LatestPricesAPI.BENCHMARK_DOLLAR_BUY,
                    dollarSellEgp = LatestPricesAPI.BENCHMARK_DOLLAR_SELL,
                    saghaDollarBuyEgp = LatestPricesAPI.BENCHMARK_SAGHA_DOLLAR
                )
                GoldPriceCalculator.calculate(initialInputs).toGoldPriceResponse()
            }
        )

    val latestSnapshot: StateFlow<GoldPriceCalculator.CalculatedSnapshot?> = repository.latestSnapshot
    val statusInfo: StateFlow<PriceStatusService.FeedStatusInfo> = statusService.statusInfo
    val calibrationConfig: StateFlow<GoldPriceCalculator.CalibrationConfig> = repository.calibrationConfig

    companion object {
        @Volatile
        private var INSTANCE: GoldPriceFeed? = null

        fun getInstance(context: Context): GoldPriceFeed {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoldPriceFeed(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * بدء دورة التحديث التلقائي كل 60 ثانية
     */
    fun startAutoRefresh() {
        scheduledJob.start()
    }

    /**
     * إيقاف دورة التحديث التلقائي
     */
    fun stopAutoRefresh() {
        scheduledJob.stop()
    }

    /**
     * طلب تحديث فوري (Pull-to-refresh)
     */
    suspend fun refreshNow(forceRefresh: Boolean = true): Boolean {
        return scheduledJob.executeCycleNow(forceRefresh = forceRefresh)
    }

    /**
     * تحديث معاملات المعايرة القابلة للضبط
     */
    fun updateCalibration(newConfig: GoldPriceCalculator.CalibrationConfig) {
        repository.updateCalibration(newConfig)
    }

    /**
     * الحصول على تقرير المقارنة المرجعية الحالي لجدول Gold Bullion
     */
    fun getReferenceComparison(): GoldPriceCalculator.ReferenceComparisonReport {
        val current = repository.latestSnapshot.value ?: run {
            val benchmarkInputs = GoldPriceCalculator.RawFeedInputs(
                ounceBuyUsd = LatestPricesAPI.BENCHMARK_OUNCE_BUY,
                ounceSellUsd = LatestPricesAPI.BENCHMARK_OUNCE_SELL,
                dollarBuyEgp = LatestPricesAPI.BENCHMARK_DOLLAR_BUY,
                dollarSellEgp = LatestPricesAPI.BENCHMARK_DOLLAR_SELL,
                saghaDollarBuyEgp = LatestPricesAPI.BENCHMARK_SAGHA_DOLLAR
            )
            GoldPriceCalculator.calculate(benchmarkInputs)
        }
        return GoldPriceCalculator.compareWithReference(current)
    }
}
