package com.example.goldfeed

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 6. ScheduledPriceJob:
 * المهمة التلقائية الدورية لجلب أسعار الذهب كل 60 ثانية.
 *
 * خطوات كل تشغيل:
 * 1. الاتصال بـ API الرسمي لمصدر الأسعار عبر PriceCollector.
 * 2. قراءة: أونصة الشراء، أونصة البيع، دولار الشراء، دولار البيع، دولار الصاغة، وقت التحديث.
 * 3. تخزين وقت وصول البيانات (fetchedAt).
 * 4. التحقق من صحة القيم عبر PriceValidator.
 * 5. حساب جميع العيارات بواسطة GoldPriceCalculator.
 * 6. حساب جنيه الذهب (8 جم عيار 21).
 * 7. حفظ اللقطة في قاعدة بيانات Room عبر GoldPriceRepository.
 * 8. تحديث آخر سعر صحيح في التطبيق.
 * 9. إرسال حالة النجاح أو الفشل إلى PriceStatusService.
 *
 * الإعدادات المعتمدة:
 * - sourceRefreshIntervalSeconds: 60
 * - maxAllowedDataAgeSeconds: 90
 */
class ScheduledPriceJob(
    private val context: Context,
    private val collector: PriceCollector = PriceCollector(context),
    private val repository: GoldPriceRepository = GoldPriceRepository(context),
    private val statusService: PriceStatusService = PriceStatusService(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private var job: Job? = null
    private var isRunningCycle = false

    companion object {
        private const val TAG = "ScheduledPriceJob"
        const val REFRESH_INTERVAL_SECONDS = 60L
        const val MAX_ALLOWED_DATA_AGE_SECONDS = 90L
    }

    /**
     * بدء دورة التحديث التلقائي كل 60 ثانية
     */
    fun start() {
        if (job?.isActive == true) return
        job = coroutineScope.launch {
            Log.d(TAG, "Starting ScheduledPriceJob (interval: ${REFRESH_INTERVAL_SECONDS}s)")
            while (isActive) {
                try {
                    executeCycle(forceRefresh = false)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Unhandled error in price job cycle: ${e.message}")
                }
                delay(REFRESH_INTERVAL_SECONDS * 1000L)
            }
        }
    }

    /**
     * إيقاف الجدولة
     */
    fun stop() {
        job?.cancel()
        job = null
        Log.d(TAG, "Stopped ScheduledPriceJob")
    }

    /**
     * تنفيذ دورة تحديث فورية يدوياً (Pull-to-refresh)
     */
    suspend fun executeCycleNow(forceRefresh: Boolean = true): Boolean = withContext(Dispatchers.Default) {
        executeCycle(forceRefresh = forceRefresh)
    }

    private suspend fun executeCycle(forceRefresh: Boolean): Boolean {
        if (isRunningCycle) return false
        isRunningCycle = true
        statusService.updateConnecting()

        try {
            // 1. اتصل بـ API الرسمي واقرأ المدخلات
            val collectionResult = collector.collectPrices(forceRefresh = forceRefresh)
            if (!collectionResult.isSuccess || collectionResult.inputs == null) {
                statusService.updateError(
                    collectionResult.errorMessage ?: "تعذر استلام أسعار السوق",
                    hasCachedData = repository.latestSnapshot.value != null
                )
                return false
            }

            val inputs = collectionResult.inputs

            // 2. تحقق من صحة القيم وعمر البيانات (Data Validation)
            val validation = PriceValidator.validateInputs(
                inputs = inputs,
                maxAllowedDataAgeSeconds = MAX_ALLOWED_DATA_AGE_SECONDS
            )
            if (!validation.isValid) {
                statusService.updateError(
                    validation.errorMessage ?: "خطأ في التحقق من البيانات",
                    hasCachedData = repository.latestSnapshot.value != null
                )
                return false
            }

            // 3. احسب جميع العيارات وجنيه الذهب
            val calibration = repository.calibrationConfig.value
            val snapshot = GoldPriceCalculator.calculate(inputs, calibration)

            // 4. التحقق من سلامة المخرجات (شراء > بيع)
            val snapshotValidation = PriceValidator.validateCalculatedSnapshot(snapshot)
            if (!snapshotValidation.isValid) {
                statusService.updateError(
                    snapshotValidation.errorMessage ?: "خلل في المخرجات المحسوبة",
                    hasCachedData = repository.latestSnapshot.value != null
                )
                return false
            }

            // 5. احفظ اللقطة في قاعدة البيانات وحدّث آخر سعر صحيح
            repository.saveAndPublishSnapshot(snapshot, latencyMs = collectionResult.latencyMs)

            // 6. أرسل حالة النجاح للخدمة
            statusService.updateSuccess(
                sourceUpdatedAt = inputs.sourceUpdatedAt,
                latencyMs = collectionResult.latencyMs,
                isFromCache = collectionResult.isFromCache,
                snapshot = snapshot
            )

            Log.d(TAG, "Cycle completed: 21k Buy=${snapshot.gram21.buyPrice}, Sell=${snapshot.gram21.sellPrice}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Cycle exception: ${e.message}", e)
            statusService.updateError(e.message ?: "استثناء أثناء الحساب", hasCachedData = repository.latestSnapshot.value != null)
            return false
        } finally {
            isRunningCycle = false
        }
    }
}
