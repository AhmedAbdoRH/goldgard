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

    fun stop() {
        job?.cancel()
        job = null
        Log.d(TAG, "Stopped ScheduledPriceJob")
    }

    suspend fun executeCycleNow(forceRefresh: Boolean = true): Boolean = withContext(Dispatchers.Default) {
        executeCycle(forceRefresh = forceRefresh)
    }

    private suspend fun executeCycle(forceRefresh: Boolean): Boolean {
        if (isRunningCycle) return false
        isRunningCycle = true
        statusService.updateConnecting()

        try {
            val collectionResult = collector.collectPrices(forceRefresh = forceRefresh)
            if (!collectionResult.isSuccess || collectionResult.inputs == null) {
                statusService.updateError(
                    collectionResult.errorMessage ?: "تعذر استلام أسعار السوق",
                    hasCachedData = repository.latestSnapshot.value != null
                )
                return false
            }

            val inputs = collectionResult.inputs

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

            val calibration = repository.calibrationConfig.value
            val snapshot = GoldPriceCalculator.calculate(inputs, calibration)

            val snapshotValidation = PriceValidator.validateCalculatedSnapshot(snapshot)
            if (!snapshotValidation.isValid) {
                statusService.updateError(
                    snapshotValidation.errorMessage ?: "خلل في المخرجات المحسوبة",
                    hasCachedData = repository.latestSnapshot.value != null
                )
                return false
            }

            repository.saveAndPublishSnapshot(snapshot, latencyMs = collectionResult.latencyMs)

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
