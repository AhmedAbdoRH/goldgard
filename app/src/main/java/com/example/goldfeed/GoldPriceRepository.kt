package com.example.goldfeed

import android.content.Context
import android.util.Log
import com.example.data.local.GoldDatabase
import com.example.data.local.PriceSnapshotEntity
import com.example.model.GoldPriceResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 5. GoldPriceRepository:
 * مستودع بيانات أسعار الذهب والوسيط بين قاعدة البيانات والذاكرة والتطبيق.
 *
 * المهام:
 * - حفظ كل لقطة أسعار صحيحة ومحسوبة في قاعدة بيانات Room المحلية.
 * - استعادة آخر سعر صحيح ومحفوظ فور بدء تشغيل التطبيق.
 * - توفير تدفقات الحالة (StateFlow) المحدثة تلقائياً للواجهة (UI).
 * - الاحتفاظ بإعدادات المعايرة القابلة للتعديل والتشخيص.
 */
class GoldPriceRepository(
    private val context: Context,
    private val database: GoldDatabase = GoldDatabase.getDatabase(context),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val priceSnapshotDao = database.priceSnapshotDao()

    private val _latestSnapshot = MutableStateFlow<GoldPriceCalculator.CalculatedSnapshot?>(null)
    val latestSnapshot: StateFlow<GoldPriceCalculator.CalculatedSnapshot?> = _latestSnapshot.asStateFlow()

    private val _calibrationConfig = MutableStateFlow(GoldPriceCalculator.CalibrationConfig())
    val calibrationConfig: StateFlow<GoldPriceCalculator.CalibrationConfig> = _calibrationConfig.asStateFlow()

    init {
        // تحميل آخر لقطة محفوظة من قاعدة البيانات عند البدء
        scope.launch {
            loadInitialSnapshotFromDatabase()
        }
    }

    private suspend fun loadInitialSnapshotFromDatabase() = withContext(Dispatchers.IO) {
        try {
            val entity = priceSnapshotDao.getLatestSnapshotDirect()
            if (entity != null) {
                val inputs = GoldPriceCalculator.RawFeedInputs(
                    ounceBuyUsd = entity.ounceBuyUsd,
                    ounceSellUsd = entity.ounceSellUsd,
                    dollarBuyEgp = entity.dollarBuyEgp,
                    dollarSellEgp = entity.dollarSellEgp,
                    saghaDollarBuyEgp = entity.saghaDollarBuyEgp,
                    sourceUpdatedAt = entity.sourceUpdatedAt,
                    sourceUpdatedIso = entity.sourceUpdatedIso,
                    fetchedAt = entity.timestamp
                )
                val snapshot = GoldPriceCalculator.calculate(inputs, _calibrationConfig.value)
                _latestSnapshot.value = snapshot
                Log.d(TAG, "Restored latest snapshot from Room DB: 21k Buy=${snapshot.gram21.buyPrice}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load snapshot from DB: ${e.message}")
        }
    }

    /**
     * حفظ اللقطة المحسوبة في قاعدة بيانات Room وتحديث الذاكرة
     */
    suspend fun saveAndPublishSnapshot(
        snapshot: GoldPriceCalculator.CalculatedSnapshot,
        latencyMs: Long = 0L
    ) = withContext(Dispatchers.IO) {
        _latestSnapshot.value = snapshot

        try {
            val entity = PriceSnapshotEntity(
                timestamp = System.currentTimeMillis(),
                sourceUpdatedAt = snapshot.inputs.sourceUpdatedAt,
                sourceUpdatedIso = snapshot.inputs.sourceUpdatedIso,
                ounceBuyUsd = snapshot.inputs.ounceBuyUsd,
                ounceSellUsd = snapshot.inputs.ounceSellUsd,
                dollarBuyEgp = snapshot.inputs.dollarBuyEgp,
                dollarSellEgp = snapshot.inputs.dollarSellEgp,
                saghaDollarBuyEgp = snapshot.inputs.saghaDollarBuyEgp,
                buy24 = snapshot.gram24.buyPrice,
                sell24 = snapshot.gram24.sellPrice,
                buy22 = snapshot.gram22.buyPrice,
                sell22 = snapshot.gram22.sellPrice,
                buy21 = snapshot.gram21.buyPrice,
                sell21 = snapshot.gram21.sellPrice,
                buy18 = snapshot.gram18.buyPrice,
                sell18 = snapshot.gram18.sellPrice,
                buy14 = snapshot.gram14.buyPrice,
                sell14 = snapshot.gram14.sellPrice,
                goldPoundBuy = snapshot.goldPound.buyPrice,
                goldPoundSell = snapshot.goldPound.sellPrice,
                status = "SUCCESS",
                feedLatencyMs = latencyMs
            )
            priceSnapshotDao.insertSnapshot(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert snapshot into Room: ${e.message}")
        }
    }

    fun updateCalibration(newConfig: GoldPriceCalculator.CalibrationConfig) {
        _calibrationConfig.value = newConfig
        _latestSnapshot.value?.let { current ->
            val recalculated = GoldPriceCalculator.calculate(current.inputs, newConfig)
            _latestSnapshot.value = recalculated
        }
    }

    fun getRecentSnapshots(limit: Int = 30): Flow<List<PriceSnapshotEntity>> {
        return priceSnapshotDao.getRecentSnapshots(limit)
    }

    companion object {
        private const val TAG = "GoldPriceRepository"
    }
}
